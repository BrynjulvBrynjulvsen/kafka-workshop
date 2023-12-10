# Kafka Consumer Groups

1. [Introduction](#introduction-to-consumer-groups)
2. [Code exercises](#code-exercises)
3. [The kafka-consumer-groups tool](#console-tool-kafka-consumer-groups)

## Introduction to consumer groups
These exercises will familiarize you with kafka consumer groups. Consumer groups enable you to coordinate separate consumer processes 
working as a larger unit, allowing you to parallelize consumption and processing of messages as well as resuming an interrupted
consumer. As a consumer processes messages, it will acknowledge back to the Kafka cluster that it has finished processing a given message - 
this is known as committing an offset.

Consumer groups have two primary functions:
* Dividing [partitions](3_partitions_and_ordering.md) of a topic between processes working as part of a larger whole (ie threads in an application)
  * For now, try working on a single-partition topic. Partitions are explained in more depth later.
* Storing progress on each partition for each consumer group, allowing for handoff and resumption



## Code Exercises
These [consumer group related exercises](../src/exercises/kotlin/tasks/consumergroups) provide a hands-on 
introduction to the consumer group concept.
1. [Coordinating multiple members of a consumer group](../src/exercises/kotlin/tasks/consumergroups/1_MultiMemberConsumerGroup.kt)
2. [Committing offsets](../src/exercises/kotlin/tasks/consumergroups/2_OffsetCommitting.kt)

## Console Tool: `kafka-consumer-groups` 
This next section will familiarize you with the kafka-consumer-groups tool, an essential utility for managing consumer groups and their offsets.
### Setup
#### Populate a topic
First, populate a topic with some messages. You may already have done this as a side effect of the code exercises; otherwise,
produce some using [kafka console clients](1_producers_and_consumers.md).

#### Commit some consumer offsets
After the topic is populated, consume some messages and commit offsets. Again, you may have already done this during the code exercises.
Otherwise, go ahead and consume the messages from the setup step by using `kafka-console-consumer` with a `--group` argument
in order to commit some offsets.

<details>

> `kafka-console-consumer --bootstrap-server kafka1:9092 --from-beginning --topic hello-world --group my-group` 
</details>


### List consumer groups
First, try listing all the consumer groups on your cluster using the `kafka-consumer-groups` tool. Observe that your group from
the previous step appears, as well as (potentially) any other groups you might have committed offsets for (such as console consumer sessions)

<details>

>`kafka-consumer-groups --bootstrap-server kafka1:9092 --describe --group my-group`
</details>

>**Notice that no topic names are output from this command. It is relatively easy to look up
> which topics a consumer group has offsets committed for due to the way offsets are stored. The inverse requires a more costly set of operations
> and some scripting work**

### Inspect consumer group details
Now try listing out the details of your consumer group. 

<details>

>`kafka-consumer-groups --bootstrap-server kafka1:9092 --describe --group my-group`
</details>

The resulting table provides some useful information:
* _Topic_
* _Partition_
* _Current-offset_ is the last committed offset (in absolute terms) by this consumer group for this topic-partition pairing
* _Log-end-offset_ is the current highest offset for this partition (again in absolute terms)
* _Lag_ is the relative difference between the previous two values
  * This is useful as a metric, as it is indicative of processing having stopped or that a consumer is otherwise struggling to keep up
* _Consumer-id_ is a unique identifier for the actual client process assigned this partition for this consumer group
  * This can be useful while debugging and examining performance, as it lets you know which partitions are assigned to the same clients (if any)
* _Host_ refers to the hostname of this consumer
* _Client-id_ is a label with no impact on execution. This can be useful when troubleshooting a client outside your direct control, as it may offer a hint as to what sort of client library they are using.

>**Offsets are stored on a per-topic-per-partition-per-consumer-group basis**

### Manually replace a consumer group offset by offset
Try changing the offset of your consumer group to an absolute value using the `--reset-offsets` argument. Note that in order to
alter offsets in this manner you cannot have active clients running.

<details>

> `kafka-consumer-groups --bootstrap-server kafka1:9092 --topic hello-world:0 --group my-group --reset-offsets --to-offset 1 --execute`
> 
> The 0 after the topic name indicates which partition to operate on. This is not important for single-partition topics like we're working on here, but will be relevant in most real-world cases. 
> 
> Note the `--execute` parameter. In production, always do a `--dry-run` first. If neither parameter is supplied, `--dry-run` is the current default behavior.
</details>

Consume this topic with the given consumer group id, and notice that some messages you previously received are received again.

### Manually replace consumer group offset by date
It is frequently difficult to pinpoint the identity of a message you need to process again down to a specific offset. In these cases,
it is useful to reset the offsets based on timestamps instead.

>**This is possible thanks to Kafka records all having timestamps as part of their metadata. Keep in mind that the timestamp you provide for the reset command 
> is in the context of the system time of the Kafka cluster at the time the record was produced**

* Produce some messages in batches, and consume them
* Shut down any consumers using your group
* Use the `kafka-consumer-groups` tool to reset the offset to a timestamp in
between the time you produced the messages.
  <details>
  
  `kafka-consumer-groups --bootstrap-server kafka1:9092 --topic hello-world --group my-group --reset-offsets --to-datetime 2023-12-10T11:50:00.000 --execute` 
  </details>

* Start a consumer with the newly reset consumer group. Notice that any messages produced after the timestamp specified in the step above are received again.

## Console exercise and next step
Now that you've gotten a basic introduction to consumer groups, let's experiment a little. Try creating several consumers with the
same consumer group ID against the same topic. Produce some messages to the topic, and observe what happens.

If you've followed these exercises in order, you might notice that only one of your consumers receives messages. Use the consumer group
tool described above to describe the state of the group. Can you tell what's going on? Try terminating the consumer that got messages, 
and produce some more. Note that the previously idle consumer now receives messages.

This happens because we've so far been working with a single partition only. Let's figure out how to get more done in
parallel in [partitions and ordering](3_partitions_and_ordering.md).