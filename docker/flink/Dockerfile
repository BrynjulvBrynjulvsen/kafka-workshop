ARG FLINK_VERSION=1.20.0
FROM flink:${FLINK_VERSION}-scala_2.12-java17

ARG FLINK_VERSION
ARG FLINK_CONNECTOR_KAFKA_VERSION=3.4.0-1.20
ARG FLINK_CONNECTOR_KAFKA_VERSION
ARG KAFKA_CLIENT_VERSION=3.9.1

USER root

RUN set -eux; \
    rm -f "${FLINK_HOME}/lib/kafka-clients-"* || true; \
    wget -q "https://repo1.maven.org/maven2/org/apache/flink/flink-connector-kafka/${FLINK_CONNECTOR_KAFKA_VERSION}/flink-connector-kafka-${FLINK_CONNECTOR_KAFKA_VERSION}.jar" \
      -O "${FLINK_HOME}/lib/flink-connector-kafka-${FLINK_CONNECTOR_KAFKA_VERSION}.jar"; \
    wget -q "https://repo1.maven.org/maven2/org/apache/flink/flink-connector-base/${FLINK_VERSION}/flink-connector-base-${FLINK_VERSION}.jar" \
      -O "${FLINK_HOME}/lib/flink-connector-base-${FLINK_VERSION}.jar"; \
    wget -q "https://repo1.maven.org/maven2/org/apache/kafka/kafka-clients/${KAFKA_CLIENT_VERSION}/kafka-clients-${KAFKA_CLIENT_VERSION}.jar" \
      -O "${FLINK_HOME}/lib/kafka-clients-${KAFKA_CLIENT_VERSION}.jar"

USER flink