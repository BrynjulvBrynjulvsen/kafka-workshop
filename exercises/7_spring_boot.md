# Spring Boot

> Wire Kafka into a Spring Boot application so you can lean on the familiar annotation-driven programming model.

## Learning goals
- Explore the baseline Spring Boot project that ships with the workshop.
- Produce and consume Avro payloads using Spring’s Kafka abstractions.
- Experiment with listener concurrency, error handling, and application-to-application messaging.

## Before you start
- Ensure the core stack is running: `docker compose up`.
- Build the project once (`./gradlew build`) so IntelliJ/IDEA indexes dependencies.
- The sample app lives under [`src/main/kotlin/io/bekk`](../src/main/kotlin/io/bekk). Open it in your IDE so you can navigate beans and configuration.

## Walkthrough

### 1. Inspect the starter application
- Entry point: `src/main/kotlin/io/bekk/Application.kt` bootstraps the app and registers configuration properties from `KafkaProps`.
- `KafkaConfig` defines two Kafka templates (plain string + Avro), listener container factories, and an optional dead-letter handler. The comments in the file call out that this wiring is workshop-only—remember to harden it before production.
- `WorkshopKafkaProducer` exposes `sendPlainText` and `sendStatusUpdate` so you can emit both string and Avro payloads without fuss.
- `FeedRepository` has two listeners:
  - `workshop-status-message` (Avro payloads deserialized into the generated `WorkshopStatusMessage` type)
  - `hello-world` (plain strings)
- `WorkshopFeedController` exposes REST endpoints to read in-memory feeds and to publish `hello-world` messages.

_Checkpoint_: Run `./gradlew bootRun`, hit `http://localhost:3000/status-feed/`, and confirm the feeds update as messages arrive.

### 2. Emit Avro events from the application
- The Avro schema `src/main/avro/WorkshopStatusMessage.avsc` already generates the `WorkshopStatusMessage` class. Inject `KafkaTemplate<String, WorkshopStatusMessage>` (Spring wires this bean via `KafkaConfig.avroKafkaTemplate`).
- Create a new service (or extend the controller) that publishes `WorkshopStatusMessage` objects to the `workshop-status-message` topic—either on a schedule (`@Scheduled`) or via a new REST endpoint. Reuse `WorkshopKafkaProducer.sendStatusUpdate(...)` if you do not want to inject the template directly.

_Verify_: Produce a few messages and refresh `/status-feed/`; each entry should show the structured payload.

### 3. Scale the status listener with concurrency
- Add `concurrency = "3"` (or similar) to the `@KafkaListener` handling `workshop-status-message`. Because the bean uses the shared `listenerFactory`, this will spin up multiple threads in the same group.
- Log the current thread and partition (`Thread.currentThread().name`, `partition`) inside `receiveStatusFeedRecord` to watch the distribution.
- Optional: slow one handler (`Thread.sleep`) and observe how Spring/Kafka rebalances work across the concurrent listeners.

_Checkpoint_: While producing data, confirm the feed shows records from different partitions handled by different threads.

### 4. Harden error handling
- Set `app.kafka.dltEnabled=true` in `application.yml` to enable the `DefaultErrorHandler` + dead-letter publishing logic baked into `KafkaConfig`.
- Use the console producer to send an invalid payload to `workshop-status-message`:
  ```bash
  docker compose exec kafka1 kafka-console-producer --bootstrap-server kafka1:9092 --topic workshop-status-message
  ```
  Paste a raw string (not Avro) and press Enter.
- Watch the application logs; the bad record should be routed to the `${topic}-dlt` topic. Confirm with:
  ```bash
  docker compose exec kafka1 kafka-console-consumer \
    --bootstrap-server kafka1:9092 \
    --topic workshop-status-message-dlt \
    --from-beginning --timeout-ms 2000
  ```

### 5. Trigger cross-application messaging (optional stretch)
- Spin up a second Spring Boot service that consumes the same Avro topic and exposes its own state (for example, per-status counts).
- From the first app, publish commands/events and let the second app update its state based on what was received.
- Discuss deployment considerations: app IDs, consumer group coordination, schema evolution between services.

## Stretch ideas
- Integrate Spring Cloud Stream and compare its binder abstraction with Spring Kafka’s template/listener approach.
- Add observability: hook in Micrometer to expose consumer lag metrics or create a Grafana dashboard.
- Package the app into a Docker image and deploy it alongside the workshop stack using `docker compose`.

## Clean up
- Stop the running application with `Ctrl+C` or `./gradlew --stop`.
- If you created additional topics for retries/dead-letter queues, remove them with `kafka-topics --delete` to keep the cluster tidy.
