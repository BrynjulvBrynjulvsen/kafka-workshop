# Kafka Partitions
1. [Introduction](#introduction)
2. [Partitions and consumer groups](#partitions-and-consumer-groups)
3. [Ordering and keys](#ordering-and-keys)
4. [Code exercises](#code-exercises)

## Introduction
In the previous section, we worked on a single partition. This simplifies some concepts, but restricts us to a single
consuming thread. This is because Kafka provides the ability to guarantee the order in which a subset of records on
a topic are received. Since messages are retrieved in offset order, this left us with a single consuming process. 

A single consuming thread is obviously not going to scale well in most production settings. Fortunately, Kafka's 
partitioning concept provides us with the tools to overcome this limitation. Each topic is divided into a number of *partitions*. When
messages are produced, they're assigned to one of the topic's partitions. 

It should be noted that the number of partitions chosen for a topic has many other implications as well, including cluster performance. For now, let's concentrate
on the client side of things.

## Partitions and consumer groups
When new consumers with a consumer group ID subscribe to a topic, Kafka will put that group in a *rebalancing* state.
While in this state, members of the group receive no further messages whilst Kafka reassigns that group's partitions as
evenly as possible amongst the group members. Once each member has acknowledged its new assignment, the group returns to
an *active* state and consumption resumes. This way, records may be consumed in parallel whilst still only being processed
once by the group.

### Exercise
Choose a topic with multiple partitions. `partitioned-topic` should already exist if you ran the setup script earlier.
* Create several consumers using the console-consumer tool
* Examine the group using the `kafka-consumer-groups` tool. Notice that the partitions have been divided
* Produce a number of records to the topic. Observe that each record is received once, but potentially by different consumers.

> Try using different keys to get your messages assigned to different partitions. `kafka-console-producer.sh --broker-list localhost:9092 --topic topic-name --property "parse.key=true" --property "key.separator=:"`
> lets you specify keys on the form of `key:value`

## Ordering and keys 

Though overridable, the default partitioning strategies make it non-obvious which discrete partition a given message will be 
written to. Instead, it guarantees us that messages with the same *key* will be written to the same partition. This provides a way to guarantee that a subset of records will be 
consumed in the same order as they were written.

> For those coming from a rdbms-background, the concept of keys might seem confusing initially. Keys are not necessarily 
> identifiers; rather, they indicate that something is contextually related. In some usages identical keys *may* indicate
> a superseding version of a resource (particularly when using the log compaction cleanup policy), but this is not required.

## Code exercise
[Implement](../src/exercises/kotlin/tasks/partitions/1_keys_and_ordering.kt) and demonstrate ordered consumption of a topic.
