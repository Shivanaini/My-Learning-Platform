@echo off
REM Starts all services in separate windows (build first: mvn clean package -DskipTests)
start "discovery-server" java -jar discovery-server\target\discovery-server.jar
timeout /t 15 /nobreak > nul
start "auth-service" java -jar auth-service\target\auth-service.jar
start "lecture-service" java -jar lecture-service\target\lecture-service.jar
start "ui-service" java -jar ui-service\target\ui-service.jar
timeout /t 10 /nobreak > nul
start "api-gateway" java -jar api-gateway\target\api-gateway.jar
echo All services starting. Open http://localhost:8080 in about 30 seconds.
