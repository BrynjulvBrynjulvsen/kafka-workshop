#!/bin/bash
# Kafka comes with a set of built-in console tools. These conveniently come bundled with Kafka, so for these exercises let's use those
# If you haven't done so already, start out by examining and running the script in exercise_setup to set up topics and see an
# example of how to run commands on your kafka container

# For this first exercise, we'll start out with some simple producing and consuming. You can either do this in the form of a script,
# or explore the container interactively by running "docker-compose exec kafka1 /bin/bash" from the kafka_cluster directory.

# First, produce some messages using kafka-console-producer
cd ../../../../kafka-cluster || exit
# Use kafka-console-producer to produce messages to a topic. Either use the hello-world topic created during setup, or
# create your own using kafka-topics.
# Hint: you need to refer to the bootstrap server by the host name of its published listener
# (that is, kafka1, not localhost - even though you are in fact running the command from the broker itself)
# If you're doing this interactively, simply input messages as you go. If doing it from a script, you may want to echo
# and pipe your messages to kafka-console-producer


# Then, consume some messages using kafka-console-consumer
cd ../../../../kafka-cluster || exit

# Observe that messages produced during the previous step are printed.


# If your exploring interactively, try running the producer and consumer in separate terminals.
# Note that messages are received by the consumer continuously as they are produced
