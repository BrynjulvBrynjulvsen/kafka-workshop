# Kafka consumer groups console tool

This exercise will familiarize you with the kafka-consumer-groups tool, an essential utility for managing
consumer groups and their offsets. If you are not already familar with what a consumer group is, it is recommended that 
you perform the consumer group related exercises from the kotlin section first.


## Setup
### Populate a topic
First, populate a topic with some messages. You may already have done this as a side effect of prior exercises; otherwise,
produce some using kafka console clients.

### Commit some consumer offsets
After the topic is populated, consume some messages and commit offsets. Again, you may have already done this during prior exercises.
Otherwise, go ahead and consume the messages from the setup step by using `kafka-console-consumer` with a `--group` argument
in order to commit some offsets.

## List consumer groups

## Manually replace a consumer group offset by offset
Try

## Manually replace consumer group offset by date
* Try producing some messages in batches.
* Shut down any consumers using your group
* Use the kafka-consumer-groups tool to reset the offset to a timestamp in
between the time you produced the messages.
* Start a consumer with the newly reset consumer group. Notice that only messages after the timestamp specified in the step above are received.
* 