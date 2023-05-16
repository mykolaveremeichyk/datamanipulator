# Spark Cluster with Docker & docker-compose

# General

A simple spark standalone cluster for your testing environment purposes. Using *docker-compose* can provide a solution for setting up your Spark development environment with just a single command.

# Installation

The following steps will enable one to run the spark cluster's containers.

## Pre requisites

* Docker installed

* Docker compose  installed

* A spark Application Jar to play with
  * It should be placed into the *target/scala-2.12* folder. Typically a result of an *sbt assembly* task execution 

* Any additional, necessary for Spark job execution, ought to be placed into the *src/main/resources/cluster* folder. (Optional)
## Build the images

The first step to deploy the cluster will be the build of the custom images, these builds can be performed with the *build-images.sh* script.

The executions is as follows:

```sh
./build-images.sh
```

This will create the following docker images:

* spark-base:2.3.1: A base image based on java:alpine-jdk-8 wich ships scala, python3 and spark 2.3.1

* spark-master:2.3.1: A image based on the previously created spark image, used to create a spark master containers.

* spark-worker:2.3.1: A image based on the previously created spark image, used to create spark worker containers.

## Run the docker-compose

The final step to create your test cluster will be to run the compose file:

```sh
docker-compose up --scale spark-worker=3
```

## Validate the cluster

Cluster validation can be achieved accessing the spark UI on th URL master is exposing.

#### Spark Master UI

localhost:8080


# Resource Allocation

This cluster is shipped with three workers and one spark master, each of these has a particular set of resource allocation(basically RAM & cpu cores allocation).

* The default CPU cores allocation for each spark worker is 1 core.

* The default RAM for each spark-worker is 1024 MB.

* The default RAM allocation for spark executors is 256mb.

* The default RAM allocation for spark driver is 128mb

# Bound Volumes

To make app running easier, two volume mounts are shipped, description in the following chart:

Host Mount|Container Mount|Purposse
---|---|---
/mnt/spark-apps|/opt/spark-apps|Used to make available app's jars on all workers & master
/mnt/spark-data|/opt/spark-data| Used to make available app's data on all workers & master

# Run a sample application

## Ship jar & dependencies on the Workers and Master

A necessary step for a **spark-submit** command to work is to copy the application bundle into a master node, also any configuration file or input file if needed. Because docker volumes are used, one just has to copy their app .jar and configs into /opt/spark-apps, and input files into /opt/spark-data within the containers. In order to achieve this, place the bundled .jar file into the *target/scala-2.12* directory and any input files into *src/main/resources/local* directory

## Execute spark-submit command inside master docker node
Log into the master node container
* 
  ```docker exec -it <spark-master> bash```
Lastly, execute the job script
*
  ```sh spark/bin/./spark-submit --class <fully.qualified.driver.class.Name> --master spark://spark-master:7077 /opt/spark-apps/<JAR_file_name>.jar```

