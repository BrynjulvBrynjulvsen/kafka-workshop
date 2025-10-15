# Kafka-workshop

This workshop seeks to instill a basic working knowledge of Kafka in a few hours' worth of practice. You can expect
to have a basic grasp of consumers, consumer groups, partitioning, schemas and cleanup policies by the time you
make it through the exercises provided. You will also pick up useful Kafka CLI commands, and there are optional
Spring Boot exercises if you want to try Kafka inside a full framework.

## Quick Start
1. **Start the local stack**
   ```bash
   docker compose up
   ```
   Keep the terminal open so the brokers, schema registry and supporting services stay up.
2. **Seed workshop topics once brokers respond**
   ```bash
   ./exercise_setup/create_topics.sh
   ```
3. **Verify the cluster responds to CLI tools**
   ```bash
   docker compose exec kafka1 kafka-topics --bootstrap-server kafka1:9092 --list
   ```
   Seeing topics such as `hello-world` and `partitioned-topic` confirms the setup script succeeded.
4. **Run a Kotlin smoke test to ensure Gradle wiring works**
   ```bash
   ./gradlew runKotlinClass -PmainClass=tasks.suggested_solutions._1_CreateProducerKt
   ```
   The task should finish without errors after producing a single test message, proving your toolchain is ready for the coding exercises.

### Optional tooling: `kcat`
Some later exercises demonstrate Avro-aware CLI consumption using [`kcat`](https://github.com/edenhill/kcat). Install it now if you want that tooling ready:

- macOS (Homebrew): `brew install kcat`
- Debian/Ubuntu: `sudo apt-get install kcat`
- Other platforms: consult the project README for binaries or build instructions.

Verify the install with `kcat -V`. You can skip this if you prefer to rely solely on the provided Kotlin code and Kafka console clients.

### Optional services: Flink
The Apache Flink exercises rely on a lightweight Flink jobmanager/taskmanager duo. A custom image baked from the official `flink:1.20.0-scala_2.12-java17` base is used so the Kafka connector jars (`flink-connector-kafka:3.4.0-1.20`) are pre-installed (and the build works on both Intel and Apple Silicon).

Build and start them only when needed:

```bash
docker compose -f docker-compose.yml -f docker-compose.flink.yml build
docker network create kafkaworkshop
docker compose -f docker-compose.yml -f docker-compose.flink.yml up -d flink-jobmanager flink-taskmanager
```

Stop them again with `docker compose ... down` so they do not consume resources once you finish the module.

## Prerequisites
* Docker
  * Alternatively, a locally installed and configured Kafka + schema registry
  * Optional: Pre-pull all workshop container images (including the custom Flink build) ahead of time with `<./exercise_setup/pull_images.sh>`
* Strongly recommended: An IDE capable of using Gradle and Kotlin
  * A `runKotlinClass` Gradle task is provided as a fallback (see the Quick Start smoke test for an example)
* Bash

## Workshop Roadmap
| # | Module | Focus for newcomers                   | Kotlin entry point |
| - | - |---------------------------------------| - |
| 1 | [Producers and consumers](exercises/1_producers_and_consumers.md) | Basic produce/poll/commit flow and CLI inspection | `src/exercises/kotlin/tasks/basics` |
| 2 | [Consumer groups](exercises/2_kafka-consumer-groups.md) | Coordinated consumption and offset management | `src/exercises/kotlin/tasks/consumergroups` |
| 3 | [Partitions and ordering](exercises/3_partitions_and_ordering.md) | Keys, ordering guarantees and horizontal scaling | `src/exercises/kotlin/tasks/partitions` |
| 4 | [Schemas and serdes](exercises/4_schemas_and_serdes.md) | Working with schema registry and Avro serdes | `src/exercises/kotlin/tasks/serdes` |
| 5 | [Deletion policy and log compaction](exercises/5_deletion_policy.md) | Kafka retention strategies and compaction behaviour | `src/exercises/kotlin/tasks/cleanup` |
| 6 | [Kafka Connect](exercises/6_kafka_connect.md) | Declarative data movement with Kafka Connect | `kafkaconnect_solution/` |
| 7 | [Spring Kafka using Spring Boot](exercises/7_spring_boot.md) | Applying Kafka concepts inside Spring Boot | `src/main/kotlin/io/bekk` |
| 8 | [Apache Flink](exercises/8_apache_flink.md) | Stateful stream processing with DataStream + SQL Table API | `src/exercises/kotlin/tasks/flink` |
| 8.5 | [Flink cluster & SQL Gateway](exercises/8_5_flink_cluster.md) | Deploying the workshop pipeline to the standalone cluster & SQL Gateway | `docker-compose.flink.yml`, `exercise_setup/submit_flink_job.sh` |
| 9 | [Kafka Streams](exercises/9_kafka_streams.md) | Embedded stream processing with the Kafka Streams DSL | `src/exercises/kotlin/tasks/kafkastreams` |
| 10 | [Authentication and ACLs](exercises/10_kafka_authentication.md) | Enabling SCRAM and controlling access with ACLAuthorizer | `src/exercises/kotlin/tasks/auth` |


Work through the modules in order if you're new to Kafka, as each one builds on the ideas established earlier. Once setup is complete, dive into the exercise guides linked above for detailed steps, console explorations and Kotlin TODOs.
