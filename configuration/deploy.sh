#!/bin/bash

DEPLOY_ENV="$1"

function deploy_files() {
    local now=$(date '+%Y-%m-%d.%H:%M')
    echo "Deploying new Jar with date '${now}'"
    scp ${TRAVIS_BUILD_DIR}/target/TrollsGames*.jar prod:trolls-${DEPLOY_ENV}/archives/TrollsGames-${now}.jar
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
restart_docker