#!/bin/bash

DEPLOY_ENV="$1"
DEPLOY_TARGET="$2"

function deploy_frontend() {
    local now=$(date '+%Y-%m-%d.%H.%M')
    echo "Building and deploying backoffice for environment ${DEPLOY_ENV} at ${now}"
    npm install -g @angular/cli
    cd frontend
    tar czf dist.tar.gz dist/

    scp dist.tar.gz prod:trolls-admin-${DEPLOY_ENV}/archive/dist-${now}.tar.gz
    ssh prod -- rm -fr trolls-admin-${DEPLOY_ENV}/dist/*
    scp dist/* prod:trolls-admin-${DEPLOY_ENV}/dist/
}

function restart_frontend() {
    ssh prod -- docker-compose -f trolls-admin-${DEPLOY_ENV}/docker-compose.yml down
    ssh prod -- docker-compose -f trolls-admin-${DEPLOY_ENV}/docker-compose.yml up -d
}

function deploy_backend() {
    local now=$(date '+%Y-%m-%d.%H.%M')
    echo "Deploying new Jar with date '${now}'"
    scp ${TRAVIS_BUILD_DIR}/target/TrollsGames*.jar prod:trolls-${DEPLOY_ENV}/archive/TrollsGames-${now}.jar
    scp ${TRAVIS_BUILD_DIR}/target/TrollsGames*.jar prod:trolls-${DEPLOY_ENV}/java/TrollsGames.jar
    scp ${TRAVIS_BUILD_DIR}/configuration/docker-compose-${DEPLOY_ENV}.yml prod:trolls-${DEPLOY_ENV}/
}

function restart_backend() {
    echo "Restarting dockers"
    ssh prod -- docker-compose -f trolls-${DEPLOY_ENV}/docker-compose-${DEPLOY_ENV}.yml down
    ssh prod -- docker-compose -f trolls-${DEPLOY_ENV}/docker-compose-${DEPLOY_ENV}.yml up -d
}

echo "Deploying for ${DEPLOY_ENV}"

if [[ ${DEPLOY_TARGET} == "backend" ]]; then
    deploy_backend
    restart_backend
elif [[ ${DEPLOY_TARGET} == "frontend" ]]; then
    deploy_frontend
    restart_frontend
fi
