#!/bin/bash
cd alarm-service
export ALARM_SERVICE_IMAGE=$1
export ALARM_SERVICE_VERSION=$2
docker-compose up -d --no-deps alarmservice
