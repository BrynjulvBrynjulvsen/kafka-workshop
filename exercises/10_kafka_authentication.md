# Authentication with SCRAM and ACLs

## What you will learn
- Expose a dedicated SASL listener on the existing workshop cluster.
- Provision SCRAM-SHA-512 credentials and ACLs for a client principal.
- Interact with Kafka from the CLI and Kotlin using authenticated connections.

## Before you start
1. Create the shared Docker network once if you have not already done so:
   ```bash
   docker network create kafkaworkshop
   ```
2. Bring up the broker with the SCRAM-enabled override:
   ```bash
    docker compose \
    -f docker-compose.yml \
    -f docker-compose.scram.yml \
    up -d kafka1 schemaregistry1
 ```
  The override adds a `SASL_PLAINTEXT` listener exposed on `localhost:19096`, mounts `conf/kafka_server_jaas.conf` into the broker, and enables Kafka’s `StandardAuthorizer`.
  Feel free to inspect `conf/kafka_server_jaas.conf`—it declares the `KafkaServer` login context the broker requires in KRaft mode.
3. Provision credentials and ACLs:
   ```bash
   ./exercise_setup/create_scram_credentials.sh
   ```
   Environment variables on the command allow you to change usernames, passwords, topic (`TOPIC_NAME`) and group (`GROUP_ID`).

## CLI exercise
1. Inspect `exercise_setup/sasl-scram-client.properties`. It contains the minimal client configuration for the workshop principal.
2. Try to produce without auth and observe the failure (inside the container we target `kafka1:9096`; from the host the listener is reachable on `localhost:19096`):
  ```bash
  docker compose -f docker-compose.yml -f docker-compose.scram.yml exec kafka1 \
    kafka-console-producer --bootstrap-server kafka1:9096 \
    --topic scram-demo
  ```
  You should receive an authorization error because the SASL listener requires valid credentials.
3. Produce with SCRAM credentials:
  ```bash
  docker compose -f docker-compose.yml -f docker-compose.scram.yml exec kafka1 \
    kafka-console-producer --bootstrap-server kafka1:9096 \
    --topic scram-demo \
    --producer.config /tmp/workshop-client-sasl.properties
  ```
  (The setup script writes `/tmp/workshop-client-sasl.properties` inside the broker container. Alternatively, copy `exercise_setup/sasl-scram-client.properties` onto your PATH and reference it with `--producer.config`.)
4. In a separate shell, consume from the authenticated listener:
  ```bash
  docker compose -f docker-compose.yml -f docker-compose.scram.yml exec kafka1 \
    kafka-console-consumer --bootstrap-server kafka1:9096 \
    --topic scram-demo \
    --group scram-demo-group \
    --consumer.config /tmp/workshop-client-sasl.properties \
    --from-beginning --timeout-ms 10000
  ```
5. Explore how ACLs work by removing access and retrying:
```bash
docker compose -f docker-compose.yml -f docker-compose.scram.yml exec kafka1 \
  kafka-acls --bootstrap-server kafka1:9096 \
  --command-config /tmp/admin-admin.properties \
  --remove --allow-principal User:workshop-client \
  --topic scram-demo --operation Write --operation Read --operation Describe
```
Follow up with the console producer again to see the authorization failure, then re-run the credential setup script to restore the ACL.

## Kotlin exercise
1. Open `src/exercises/kotlin/tasks/auth/1_scram_client.kt`.
2. Replace the TODOs so that the producer and consumer connect to `localhost:19096` using the SCRAM credentials from the setup step.
3. Run your solution:
   ```bash
   ./gradlew runKotlinClass -PmainClass=tasks.auth._1_scram_clientKt
   ```
4. Verify the output shows a successfully acknowledged send and the same record consumed by your dedicated group.

## Clean-up
Stop the exercise containers when finished:
```bash
docker compose -f docker-compose.yml -f docker-compose.scram.yml down
```
