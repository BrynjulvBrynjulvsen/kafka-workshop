from kafka import KafkaConsumer
from uuid import uuid1


bootstrap_server = 'kafka-workshop-001-kafka-workshop.aivencloud.com:13816'
consumer = KafkaConsumer(bootstrap_servers=bootstrap_server, security_protocol='PLAINTEXT',
                         group_id=str(uuid1()), auto_offset_reset='earliest')

# Task_2
# TODO: Consume a message from the topic "hello-world"
