#!/bin/bash

DEPLOY_ENV="$1"

function build_backoffice() {
    cd frontend
    npm install
    node_modules/@angular/cli/bin/ng build --prod -e ${DEPLOY_ENV}
}

build_backoffice