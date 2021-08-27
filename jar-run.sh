#!/bin/bash

export OTEL_SERVICE_NAME=quarkus-grpc-otel

java -jar target/quarkus-app/quarkus-run.jar

exit 0
