# Kafka connect

One of Kafka's strengths lies in its ecosystem. A number of mature tools that provide Kafka with more advanced 
or convenient capabilities are readily available. One such tool is Kafka Connect.

Kafka connect is a framework for streaming data between Kafka and other systems. A common use case is needing to stream
data between Kafka and another data store, such as a database. While it simple enough to implement a consumer that does this,
doing so can quickly become unwieldy at scale.

Kafka connect provides a pluggable, declarative way to accomplish this instead. Let's work through a small example, wherein
we'll set up a simple Kafka JDBC sink connector to automatically write all data entering our Avro topic from before to
a Postgres database table.

## Setting up a Kafka connector
Examine the [docker compose](../docker-compose.yml) file. Note that in addition to our Kafka and schema registry containers,
a `kafkaconnect` container is provided. Take particular note that it gets configured to use Avro for values, pulling its
schemas from the schema registry. Also note the `command` configuration. Kafka Connect comes with no pre-installed connectors,
so we're installing the [JDBC sink connector](https://docs.confluent.io/kafka-connectors/jdbc/current/sink-connector/overview.html#configuration-properties)
during container setup.

### Exercise: Post a connector to Kafka Connect
Have a look at the [configuration properties](https://docs.confluent.io/kafka-connectors/jdbc/current/sink-connector/sink_config_options.html#sink-config-options)
for our chosen connector. Connectors are managed through Kafka Connect's [REST API](https://docs.confluent.io/platform/current/connect/references/restapi.html).
Try crafting your own connector config for sinking our topic `schema-using-topic` (or your own chosen name) from before to the
Postgres container specified in our docker compose file, and posting it to Kafka Connect. 

Once that is done, try posting some more Avro messages to the topic. If everything has gone well, you should see a new
table, by default named after our topic, with a database schema automatically created to match that of our topic. Try running
a few `SELECT` statements to verify.

>If our new table does not appear, something has probably gone wrong. Inspect the connector using the proper GET endpoint,
and look at the reported status.