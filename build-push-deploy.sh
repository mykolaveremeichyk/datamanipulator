#!/bin/bash

# This script builds docker image, tags it to be pushed into the local docker registry.
# Prerequisites:
# - Install docker local registry.
# - Update a port if your registry port differs from default one (5000).

docker build -t datamanipulator:0.0.1 .
docker tag datamanipulator:0.0.1 localhost:5001/datamanipulator:0.0.1
docker push localhost:5001/datamanipulator:0.0.1
kubectl delete sparkapplication datamanipulator-spark-application -n spark
kubectl apply -f k8s/spark-pi-spark-application.yaml -n spark