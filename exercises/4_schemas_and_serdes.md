# Schemas in Kafka

## Introduction
Kafka itself attaches no special meaning to record values - they are essentially just byte arrays. In the exercises up
to this point, we've simply treated messages as strings, using a string deserializer behind the scenes. This is all
well and good on a small scale, but in real-world applications we will frequently need to coordinate schemas and their
evolution across a significant number of applications, teams and individuals. Enter the schema registry.

## Schema registry
The schema registry is a separate supporting application frequently deployed alongside Kafka. It enables coordinated
exchange of schemas between producers and consumers, enforcement of schema compliance for records and compatibility
rules for new schema versions. 

The schema registry is supported by most Kafka client libraries, and build plugins exist for build systems such as 
Maven and Gradle. Several formats are supported, including Json, Protobuf and Avro. The examples found in this workshop
use Avro, as this is an efficient binary format suitable for production loads.

## Code exercise
[Implement](../src/exercises/kotlin/tasks/serdes/1_serialization_deserialization.kt) simple serialization and deserialization
of Avro messages. Feel free to use this [schema](../src/main/avro/WorkshopStatusMessage.avsc), or create your own in this directory

> When using the BarebonesKafkaClients, try using the getAvroConsumer/getAvroProducer functions to get started


> This workshop generates Java classes using the [gradle avro plugin](https://github.com/davidmc24/gradle-avro-plugin). 
> Its default location for schema files is src/main/avro. The plugin provides a generateAvroJava task, which places
> generated classes under `build/generated-main-avro-java`. If you modify or add your own schemas, run the generateAvroJava
> task to (re)generate POJO classes.

## Schema registry API
The schema registry provides a robust [api](https://docs.confluent.io/platform/current/schema-registry/develop/api.html).
Use the API to have a look at the schema(s) created by the code exercise above.


### Inspecting schemas using the API
First, list the *subjects* present in your schema registry.
<details>

> `curl localhost:8085/subjects/`
</details>

> The schema registry organizes schemas by *subject*. There are several provided [subject naming strategies](https://docs.confluent.io/cloud/current/sr/fundamentals/serdes-develop/index.html#configuration-details)
> available to determine how schemas for a given record are resolved, as well as an implementable interface. For now,
> let's use the defaut `TopicNameStrategy`. This uses subject names on the form `<topicname>-subject` and `<topicname>-key`.

You'll likely see something along the lines of `["schema-using-topic-value"]`. Next, look at the list of versions for your
subject.

<details>

> curl localhost:8085/subjects/schema-using-topic-value/versions
</details>

Finally, have a look at the specific schema:

<details>

> curl localhost:8085/subjects/schema-using-topic-value/versions/1
</details>

### Posting new subject versions using the API
The commonplace way of posting new subject version is through build plugins, client libraries or some other structured means.
It may, however, be illustrative to experiment with creating new schema versions using the API.

Try posting a new version of the `schema-using-topic-value` subject (or one of the other subjects you've created).

> If you get an error stating that your schema is incompatible with, recall that the default backwards compatibility policy
> is backwards compatibility. Under this policy, new fields must be nullable and/or have a default value set.

#### Example
<details>

```
curl -X POST localhost:8085/subjects/schema-using-topic-value/versions \
-H "Content-Type: application/json" \
-d '{ "schema":"{\"type\":\"record\",\"name\":\"WorkshopStatusMessage\",\"namespace\":\"io.bekk.publisher\",\"fields\":[{\"name\":\"message\",\"type\":\"string\"},{\"name\":\"likes\",\"type\":[\"null\",\"string\"],\"default\":null}]}", "schemaType": "AVRO"}'
```
</details>

## Console tools
While some distributions come with tools for reading/writing Avro records using the schema registry (unlike the regular
`kafka-console-*` tools), many do not. A powerful, freely available command-line tool that fills this niche is `kcat`. Most
package managers, such as apt-get or homebrew, have packages for this tool.

### Exercise
Try reading and writing to `schema-using-topic` using the `kcat` tool.

#### Example
<details>

`kcat -C -b localhost:9094 -t schema-using-topic -r localhost:8085 -s value=avro -e`
</details>