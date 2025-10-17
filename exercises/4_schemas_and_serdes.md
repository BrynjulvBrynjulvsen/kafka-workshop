# Schemas and SerDes

> Move beyond raw strings by producing and consuming strongly typed Avro messages with the schema registry.

## Learning goals
- Understand why schemas matter for Kafka interoperability.
- Generate Avro classes and use them when producing and consuming.
- Inspect schemas via the schema registry REST API.

## Before you start
- Ensure the schema registry container is running (`docker compose up` includes it on port 8085).
- Run `./gradlew build` once to generate Avro classes into `build/generated-main-avro-java` if you changed schemas.

## Step-by-step path

### 1. Implement the Kotlin serialization exercise
- File: [`src/exercises/kotlin/tasks/serdes/1_serialization_deserialization.kt`](../src/exercises/kotlin/tasks/serdes/1_serialization_deserialization.kt).
- **What to implement**:
  - Use `BarebonesKafkaClients.getAvroProducer<WorkshopStatusMessage>()` to send a few messages to `Constants.AVRO_TOPIC_NAME`.
  - Use `getAvroConsumer<WorkshopStatusMessage>()` to subscribe, poll, and print a field or two from the deserialized record.
  - Commit offsets (`commitAsync()` is fine) so rerunning skips already processed records.
- **Verify**:
  - Run `./gradlew runKotlinClass -PmainClass=tasks.serdes._1_serialization_deserializationKt`.
  - You should see log lines containing `WorkshopStatusMessage(...)`. Use the CLI consumer without schema support to confirm it prints encoded bytes - an illustration of why serdes matter.

### 2. Inspect the schema registry
> These commands use the schema registry running at `http://localhost:8085` (forwarded from the Docker container).

1. **List subjects**
   ```bash
   curl localhost:8085/subjects/
   ```
   _Checkpoint_: Expect entries like `schema-using-topic-value` after running the Kotlin producer.

2. **List subject versions**
   ```bash
   curl localhost:8085/subjects/schema-using-topic-value/versions
   ```
   _What it shows_: Each number corresponds to a schema evolution event.

3. **Inspect a specific version**
   ```bash
   curl localhost:8085/subjects/schema-using-topic-value/versions/1
   ```
   The payload includes the Avro schema, id, and compatibility type.

### 3. (Optional) Post a new schema version
- Modify `WorkshopStatusMessage.avsc` (for example, add a nullable field with a default).
- Run `./gradlew generateAvroJava` to regenerate classes.
- Post using:
  ```bash
  curl -X POST localhost:8085/subjects/schema-using-topic-value/versions \
    -H "Content-Type: application/json" \
    -d '{ "schema":"{\"type\":\"record\",\"name\":\"WorkshopStatusMessage\",\"namespace\":\"io.bekk.publisher\",\"fields\":[{\"name\":\"message\",\"type\":\"string\"},{\"name\":\"likes\",\"type\":[\"null\",\"int\"],\"default\":null}]}", "schemaType": "AVRO"}'
  ```
- Verify the new version appears and that the Kotlin consumer still deserializes (thanks to backwards compatibility).

### 4. (Optional) Work with Avro data from the CLI
Many Kafka distributions do not bundle Avro-aware console tools. [`kcat`](https://github.com/edenhill/kcat) fills the gap:

```bash
kcat -C -b localhost:9094 -t schema-using-topic -r localhost:8085 -s value=avro -e
```

The `-r` flag points to the schema registry and `-s value=avro` tells `kcat` to deserialize record values.

## Troubleshooting tips
- **`ClassNotFoundException` for Avro classes**: re-run `./gradlew generateAvroJava` and ensure your IDE includes the generated sources.
- **`io.confluent.kafka.schemaregistry.client.rest.exceptions.RestClientException: Unauthorized`**: make sure you are pointing to `http://localhost:8085` (no auth required in this setup).
- **Consumer fails with `Unknown magic byte`**: you pointed an Avro deserializer at data that was written as plain strings (or any non-Confluent Avro format). Either produce using the Avro serializer or swap back to the string deserializer for that topic.
- **Output looks like gibberish**: you consumed Avro-encoded bytes with the plain string deserializer. Switch to the Avro consumer helper or a tool like `kcat -s value=avro` so the schema registry can translate the payload.

## Looking ahead
With schemas under control, you can explore storage behaviour in [exercise 5](5_deletion_policy.md) or build connectors in exercise 6 later on.
