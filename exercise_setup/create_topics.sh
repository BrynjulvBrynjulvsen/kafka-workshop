#!/bin/bash
docker-compose exec kafka1 kafka-topics --create --topic hello-world --partitions 1 --replication-factor 1 --bootstrap-server kafka1:9092
docker-compose exec kafka1 kafka-topics --create --topic partitioned-topic --partitions 6 --replication-factor 1 --bootstrap-server kafka1:9092