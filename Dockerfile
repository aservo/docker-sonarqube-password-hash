# Build image

FROM maven:3.6.0-jdk-11-slim AS build

COPY src /home/app/src
COPY pom.xml /home/app

RUN mvn -f /home/app/pom.xml clean package --batch-mode

# Execution image

FROM eclipse-temurin:11-jre-jammy

ENV DEBIAN_FRONTEND=noninteractive

ARG CLI_JAR=/usr/local/lib/sonarqube-password-hash.jar
ARG CLI_BIN=/usr/local/bin/sonarqube-password-hash

RUN apt-get update && \
    apt-get --no-install-recommends --yes install \
        postgresql-client \
        && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/* /tmp/* /var/tmp/*

COPY --from=build /home/app/target/sonarqube-password-hash-0.0.0-jar-with-dependencies.jar ${CLI_JAR}

RUN echo "#!/bin/env bash\nset -o pipefail\nset -e\njava -jar ${CLI_JAR} \$@" > ${CLI_BIN} && \
    chmod +x ${CLI_BIN}
