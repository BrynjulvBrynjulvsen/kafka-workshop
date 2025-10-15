package tasks

object KafkaConfig {
    private const val DEFAULT_BOOTSTRAP = "localhost:9094"
    private const val DOCKER_BOOTSTRAP = "kafka1:9092"

    val bootstrapServers: String =
        System.getenv("KAFKA_BOOTSTRAP_SERVERS")
            ?: System.getProperty("kafka.bootstrap.servers")
            ?: if (System.getenv("INSIDE_DOCKER") == "true") DOCKER_BOOTSTRAP else DEFAULT_BOOTSTRAP
}
