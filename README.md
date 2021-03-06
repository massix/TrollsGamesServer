# TrollsGamesServer

This is the server for the TrollsGames project.

## Build status

| branch  | status  |
| :------ | ------: |
| develop | [![Build Status](https://travis-ci.org/massix/TrollsGamesServer.svg?branch=develop)](https://travis-ci.org/massix/TrollsGamesServer) |
| master  | [![Build Status](https://travis-ci.org/massix/TrollsGamesServer.svg?branch=master)](https://travis-ci.org/massix/TrollsGamesServer) |

## Compile
1. Compile the whole package using `mvn package`
1. Run the server locally by issuing `java -Dspring.profiles.active=local -jar target/*.jar`

## Documentation
Swagger documentation is available at `http://localhost:8080/swagger-ui.html`

## Testing
A Postman suite is included in the package. Run it from the command line with the following command:
`$ newman run tg.allusers.json --global-var "server=http://localhost:8080"`
