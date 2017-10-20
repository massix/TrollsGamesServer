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
    cp target/TrollsGames-${TRAVIS_TAG}.jar /home/massi/tg/TrollsGames.jar
    cp configuration/application.prod.properties /home/massi/tg/data/application.properties
    docker restart trolls
EOF
}

function cleanup_build() {
    ssh prod "rm -fr tg/archives/build"
}

deploy_tgz
untar_tgz
restart_docker
cleanup_build