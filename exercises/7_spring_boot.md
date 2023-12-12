# Spring Boot

Kafka enjoys solid framework support, including in Spring. If you've reached this far in the workshop and want to
try your hand at some more complex Kafka usage, a [basic Spring Boot application](../src/main/kotlin/io/bekk) has been set up for you to extend
and experiment with.

## Code exercises and challenges
1. The application uses simple string messages. Create a schema and implement a producer using this schema. Then, implement a `@KafkaListener` using this schema to populate a local in-memory state storage with what you receive from Kafka
2. Try parallelizing without starting separate applications, using the `@KafkaListener` concurrency parameter
3. Using a console producer, try writing a non-Avro message to your topic from step 1. What happens? Try implementing a `@KafkaListener` with `@RetryableTopic`-handling.
4. Try writing another application, and trigger changes in one application by sending messages from the first.
