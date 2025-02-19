# Kafka-workshop

This workshop seeks to instill a basic working knowledge of Kafka in a few hours' worth of practice. You can expect
to have a basic grasp of basic consumers, consumer groups, partitioning, schemas and cleanup policies by the time you
make it through the exercises provided. You will also have a basic knowledge of some of Kafka's more useful command line tools.
If you want to try your hand at implementing usage of Kafka in a major framework, a basic Spring Boot setup and accompanying exercises are also provided.

## Prerequisites
* docker
  * alternatively, a locally installed and configured Kafka + schema registry
* Strongly recommended: An IDE capable of using Gradle and Kotlin
  * A runKotlinClass Gradle task is provided as a fallback
    * <details>

      >For example, `./gradlew runKotlinClass -PmainClass=tasks.suggested_solutions._2_CreateConsumerKt`
      </details>
* Bash

## Setup
Before we get started, there are a few necessary introductory steps:
* Create your Kafka cluster by executing `docker compose up`
* Once the cluster is up and running, execute the [setup script](exercise_setup/create_topics.sh) to set up topics used in exercises

## Topics
Once setup is complete, find the exercises and further instructions here:
1. [Producers and consumers](exercises/1_producers_and_consumers.md)
2. [Consumer groups](exercises/2_kafka-consumer-groups.md)
3. [Partitions and ordering](exercises/3_partitions_and_ordering.md)
4. [Schemas and serialization/deserialization](exercises/4_schemas_and_serdes.md)
5. [Deletion policy and log compaction](exercises/5_deletion_policy.md)
6. [Kafka connect](exercises/6_kafka_connect.md)
7. [Spring Kafka using Spring Boot](exercises/7_spring_boot.md)
