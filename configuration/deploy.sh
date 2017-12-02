#!/bin/bash

DEPLOY_ENV="$1"

function build_and_deploy_backoffice() {
    local now=$(date '+%Y-%m-%d.%H.%M')
    echo "Building and deploying backoffice for environment ${DEPLOY_ENV} at ${now}"
    cd frontend
    npm install
    node_modules/@angular/cli/bin/ng build --prod -e ${DEPLOY_ENV}
    tar czf dist.tar.gz dist/

    scp dist.tar.gz prod:trolls-admin-${DEPLOY_ENV}/archive/dist-${now}.tar.gz
    ssh prod -- rm -fr trolls-admin-${DEPLOY_ENV}/dist/*
    scp dist/* prod:trolls-admin-${DEPLOY_ENV}/dist/
    ssh prod -- docker-compose -f trolls-admin-${DEPLOY_ENV}/docker-compose.yml restart
}

function deploy_files() {
    local now=$(date '+%Y-%m-%d.%H.%M')
    echo "Deploying new Jar with date '${now}'"
    scp ${TRAVIS_BUILD_DIR}/target/TrollsGames*.jar prod:trolls-${DEPLOY_ENV}/archive/TrollsGames-${now}.jar
    scp ${TRAVIS_BUILD_DIR}/target/TrollsGames*.jar prod:trolls-${DEPLOY_ENV}/java/TrollsGames.jar
    scp ${TRAVIS_BUILD_DIR}/configuration/docker-compose-${DEPLOY_ENV}.yml prod:trolls-${DEPLOY_ENV}/
}

function restart_docker() {
    echo "Restarting dockers"
    ssh prod -- docker-compose -f trolls-${DEPLOY_ENV}/docker-compose-${DEPLOY_ENV}.yml down
    ssh prod -- docker-compose -f trolls-${DEPLOY_ENV}/docker-compose-${DEPLOY_ENV}.yml up -d
}

echo "Deploying for ${DEPLOY_ENV}"

deploy_files
build_and_deploy_backoffice
restart_docker