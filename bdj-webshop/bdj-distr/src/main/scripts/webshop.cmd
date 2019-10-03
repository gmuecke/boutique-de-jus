@echo off
set DB_OPTS=-Ddb.threads=4 -Ddb.timeslice=10
set HTTP_OPTS=-Djetty.http.port=8081 -Djetty.threads.min=16 -Djetty.threads.max=64
rem HTTP_OPTS=%HTTP_OPTS% -Djetty.configFile=jetty.xml -Djetty.acceptQueue.size=10
set JVM_OPTS=-Xmx512M -Xms256M
rem DEBUG_MODE=-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=
rem DEPLOY_WAR=-Djetty.deploy.war=bdj-webshop.war
set JAVA_OPTS=%DEBUG_MODE% %JVM_OPTS% %DB_OPTS% %HTTP_OPTS% %DEPLOY_WAR%
java %JAVA_OPTS% -jar bdj-server.jar
