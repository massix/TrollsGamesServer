#!/bin/bash

DEPLOY_ENV="$1"

function build_backoffice() {
    cd frontend
    npm install
    local BUILD_ENV="prod"

    case ${DEPLOY_ENV} in
        develop)
            BUILD_ENV="staging"
            ;;
        *)
            BUILD_ENV="dev"
            ;;
    esac
    echo "Building for ${BUILD_ENV}"
    node_modules/@angular/cli/bin/ng build --prod -e ${BUILD_ENV}
}

build_backoffice