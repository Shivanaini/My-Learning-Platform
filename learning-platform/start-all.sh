#!/usr/bin/env bash
# Starts all services in the background (build first: mvn clean package -DskipTests)
set -e
mkdir -p logs
start() { echo "Starting $1..."; nohup java -jar "$1/target/$1.jar" > "logs/$1.log" 2>&1 & }
start discovery-server; sleep 15
start auth-service
start lecture-service
start ui-service
sleep 10
start api-gateway
echo "All services starting. Logs in ./logs. Open http://localhost:8080 in ~30 seconds."
