from kafka import KafkaProducer

bootstrap_server = 'localhost:9094'
producer = KafkaProducer(bootstrap_servers=bootstrap_server, security_protocol='PLAINTEXT')

# TODO: Produce a message to the topic "hello-world"
# producer.send()
# producer.flush()
