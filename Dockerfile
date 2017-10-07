FROM openjdk:8-jre
MAINTAINER Massimo Gengarelli <massimo.gengarelli@gmail.com>
COPY target/TrollsGames* /usr/lib/
RUN mkdir /etc/TrollsGames/

COPY trolls.db /etc/TrollsGames/
COPY configuration/application.properties /etc/TrollsGames/

ENTRYPOINT ["/usr/bin/java", "-jar", "/usr/lib/TrollsGames*.jar"]