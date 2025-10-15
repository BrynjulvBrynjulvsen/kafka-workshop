# Kafka Connect

Kafka ships with a rich ecosystem of tooling. Kafka Connect is the framework the community leans on when data has to flow
between Kafka and external systems (databases, object stores, SaaS APIs, …) without hand-rolling a bespoke consumer/producer
for every integration.

## Learning goals
- Understand how the workshop’s Kafka Connect worker is configured.
- Post a JDBC sink connector through the REST API and verify data lands in Postgres.
- Inspect connector status when things go wrong.

## Before you start
- Launch the base stack: `docker compose up -d` (or `docker compose -f docker-compose.yml up -d` to be explicit).
- Confirm the Connect worker is reachable at `http://localhost:18083` — that port maps to the container’s `8083` listener.
- Ensure the Avro topic from the serdes exercise (default `schema-using-topic`) has some records you can replay.

## 1. Meet the Kafka Connect worker
- Open [`docker-compose.yml`](../docker-compose.yml) and locate the `kafkaconnect` service. Note that it:
  - Boots with the Confluent `cp-kafka-connect:7.5.2` image.
  - Uses Avro for value conversion (`CONNECT_VALUE_CONVERTER_*`) pointing at Schema Registry on `schemaregistry1:8085`.
  - Installs the [Kafka Connect JDBC sink](https://docs.confluent.io/kafka-connectors/jdbc/current/sink-connector/overview.html) during start-up via `confluent-hub install …`.
  - Exposes its REST API on `localhost:18083`.

> If the container exits immediately, check `docker compose logs kafkaconnect`. The most common failure is the connector
download timing out — rerun `docker compose up` once the network hiccup clears.

## 2. Craft a JDBC sink configuration
- Review the connector options in the [property reference](https://docs.confluent.io/kafka-connectors/jdbc/current/sink-connector/sink_config_options.html).
- Fill in a JSON payload that points at the Postgres container (host `postgres`, port `5432`, database `workshop`, user `user`, password `password` — see the compose file).
- Suggested minimal config:

```json
{
  "name": "jdbc-sink",
  "config": {
    "connector.class": "io.confluent.connect.jdbc.JdbcSinkConnector",
    "topics": "schema-using-topic",
    "connection.url": "jdbc:postgresql://postgres:5432/workshop",
    "connection.user": "user",
    "connection.password": "password",
    "auto.create": "true",
    "auto.evolve": "true",
    "insert.mode": "insert",
    "key.converter": "org.apache.kafka.connect.storage.StringConverter",
    "value.converter": "io.confluent.connect.avro.AvroConverter",
    "value.converter.schema.registry.url": "http://schemaregistry1:8085"
  }
}
```

Feel free to tweak the `topics`, primary-key strategy, or table naming to fit your earlier exercises.

## 3. Post the connector
- Use the REST API (or an HTTP client like `httpie`/`curl`) to create it:

```bash
curl -X POST http://localhost:18083/connectors \
  -H 'Content-Type: application/json' \
  -d @connector.json
```

> Tip: the repository includes a ready-to-run example in `kafkaconnect_solution/create_connector.http`.

- Inspect the connector status:

```bash
curl http://localhost:18083/connectors/jdbc-sink/status | jq
```

Expect to see both the connector and its single task in the `RUNNING` state.

## 4. Validate the sink
- Produce a few Avro records to your source topic (reuse the producer from the serdes exercise or `kcat`).
- Connect to Postgres:

```bash
docker compose exec postgres psql -U user -d workshop
```

- List tables (`\dt`) and query the one named after your topic. With `auto.create=true`, the connector mirrors the Avro
schema into a matching table automatically.

## 5. Iterating & cleanup
- Update the connector configuration with `PUT /connectors/<name>/config` if you need to adjust settings.
- Pause, resume, or restart using the REST endpoints documented in the [Connect REST guide](https://docs.confluent.io/platform/current/connect/references/restapi.html).
- Remove the connector when you are done:

```bash
curl -X DELETE http://localhost:18083/connectors/jdbc-sink
```

If your table never shows up, fetch the task status (`…/status`) or recent errors (`…/status | jq '.tasks[0].state'`). The
stack traces there usually point directly at misconfigured connection URLs or authentication details.
