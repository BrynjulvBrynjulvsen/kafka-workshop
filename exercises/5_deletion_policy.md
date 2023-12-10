# Cleanup Policies



## Policies
Storage space for a Kafka cluster can quickly grow pricey. Fortunately, Kafka comes with some out-of-the box strategies
for handling this. There are two primary policies to consider:
* *DELETE* will dispose of records older than the topic's specified retention value
* *COMPACT*, also known as log compaction, will retain only the latest message with a given key
  * That is, posting a record with a previous given key will instruct Kafka to get rid of any older records with the same key. A `null` record value for a key, known as a tombstone record, will retain no records for that key

### When does deletion happen?
The cleanup policies are not designed to be relied upon for business logic - they're designed to reclaim storage space only.
There is no guarantee of when a particular record will be deleted, and such deletion can only happen to an inactive segment.

## Log compaction exercise
While you should never rely on log compaction for your business logic, it is possible to force compaction for the purposes of an example.

### Setup
Create a topic with the compaction policy set, then set some rather unorthodox configuration:
```
kafka-topics --create --topic log-compact-example --partitions 6 --replication-factor 1 --bootstrap-server kafka1:9092 --config cleanup.policy=compact
kafka-configs --alter --entity-type topics --entity-name log-compact-example --add-config 'max.compaction.lag.ms=100, min.cleanable.dirty.ratio=0.0, segment.ms=100, delete.retention.ms=100' --bootstrap-server kafka1:9092
```
This will force Kafka to create a new log segment the next received message and check if it should run log compaction on the now-inactive one every 100 milliseconds.
The caveat, apart from terrible performance, is that you need to perform some very specific steps in order to then see the behavior in action:

* Write a few records with the same key
* Wait a few seconds
* Write a new record to force Kafka to create a new segment, so it can run log compaction on the now-inactive old segment
* Wait a few more seconds to allow log compaction to finish
* Consume the topic and note that only the last 1-2 written messages are likely to remain

### Code exercise
[Implement](../src/exercises/kotlin/tasks/cleanup/1_logCompaction.kt) the above in code. Or just look at and run the [solution](../src/exercises/kotlin/tasks/cleanup/suggested_solutions/1_logCompaction.kt), since this isn't something you should ever do in the wild.
