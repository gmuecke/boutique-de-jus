@echo off
set JVM_OPTS=-Xmx512M -Xms256M
rem DEBUG_MODE=-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=
set JAVA_OPTS=%DEBUG_MODE% %JVM_OPTS%
java %JAVA_OPTS% -jar bdj-server.jar config.json
