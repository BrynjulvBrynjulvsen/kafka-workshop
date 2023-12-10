# Kafka Producers and Consumers
These introductory exercises will illustrate Kafka's most basic functionality: Producing and consuming messages.

1. [Console Consumer](#the-kafka-console-consumer-and-kafka-console-producer-tools)
2. [Code Exercises](#code-exercises)

## The `kafka-console-consumer` and `kafka-console-producer` tools
Kafka comes with a set of built-in console tools. These are useful for inspecting the state of your Kafka cluster, topics,
consumer groups, clients and more. 

The perhaps most basic components of this toolbox are console implementations of basic Kafka consumers and producers. 

### Setting up the console tools
Most of the console tools we'll discuss conveniently come bundled with the Kafka continer. It is suggested you use these
for ease of setup. Assuming you've got the docker-compose setup running, execute `docker-compose exec kafka1 /bin/bash` 
from the directory where docker-compose.yml is located to get started.

### Kafka-console-producer
 For this first exercise, we'll start out with some simple producing and consuming. 
 Use `kafka-console-producer` to produce messages to a topic. Either use the hello-world topic created during setup, or
create your own using `kafka-topics` (see the [setup script](../exercise_setup/create_topics.sh) for inspiration).

>**_Hint: you need to refer to the bootstrap server by the host name of its published listener
(that is, kafka1, not localhost - even though you are in fact running the command from the broker itself)._**

#### Example usage
<details>

> `kafka-console-producer --bootstrap-server kafka1:9092 --topic hello-world`, then input messages interactively.

Alternatively, `echo "Test123" | kafka-console-producer --bootstrap-server kafka1:9092 --topic hello-world` to input a single message.
</details>

### Kafka-console-consumer
Next, try consuming the messages produced in the previous step using `kafka-console-consumer`. Observe that messages produced during the previous step are printed. 

>**_Hint: By default, the console consumer starts consuming from the most recent offset. To consume messages from the beginning, you will need to add the `--from-beginning` argument._** 

#### Example usage
<details>

> `kafka-console-consumer --bootstrap-server kafka1:9092 --from-beginning --topic hello-world`
</details>

### Listen continuously
The `kafka-console-consumer` will keep listening until terminated using `ctrl+c`. With the consumer listening, try opening a separate
terminal session and produce some messages to the topic. Observe that these are consumed continuously in your consumer session.

## Code Exercises
1. [Creating a basic producer application](../src/exercises/kotlin/tasks/basics/1_CreateProducer.kt)
2. [Creating a basic consumer application](../src/exercises/kotlin/tasks/basics/2_CreateConsumer.kt)
3. [Continuously consuming messages as they are produced](../src/exercises/kotlin/tasks/basics/3_LongRunningConsumer.kt)