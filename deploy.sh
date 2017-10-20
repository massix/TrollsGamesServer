#!/bin/bash

function deploy_tgz() {
    ssh prod "mkdir -p tg/archives"
    scp ${TRAVIS_BUILD_DIR}/TrollsGames-${TRAVIS_TAG}.tgz prod:tg/archives/
}

function untar_tgz() {
    ssh prod <<EOF
    cd tg/archives
    mkdir build
    tar xzf TrollsGames-${TRAVIS_TAG}.tgz -C build/
EOF
}

function restart_docker() {
    ssh prod <<EOF
    cd tg/archives/build
    cp TrollsGames-${TRAVIS_TAG}.jar ${HOME}/tg
    docker restart trolls
EOF
}

deploy_tgz
untar_tgz
restart_docker
