#!/usr/bin/env bash
JVM_OPTS="-Xmx512M -Xms256M"
DEBUG_MODE=
# DEBUG_MODE="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address="

JAVA_OPTS="${DEBUG_MODE} ${JVM_OPTS}"

java ${JAVA_OPTS} -jar bdj-server.jar config.json
