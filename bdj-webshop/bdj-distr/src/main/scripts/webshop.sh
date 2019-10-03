#!/usr/bin/env bash
DB_OPTS="-Ddb.threads=4 -Ddb.timeslice=10"
HTTP_OPTS="-Djetty.http.port=8081 -Djetty.threads.min=16 -Djetty.threads.max=64"
JVM_OPTS="-Xmx512M -Xms256M"
DEBUG_MODE=
DEPLOY_WAR=
# DEBUG_MODE="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address="
# DEPLOY_WAR="-Djetty.deploy.war=bdj-webshop.war"

JAVA_OPTS="${DEBUG_MODE} ${JVM_OPTS} ${DB_OPTS} ${HTTP_OPTS} ${DEPLOY_WAR}"

java ${JAVA_OPTS} -jar bdj-server.jar
