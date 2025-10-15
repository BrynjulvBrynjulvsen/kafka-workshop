package tasks.flink.suggestedSolutions

import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment
import org.apache.flink.table.api.EnvironmentSettings
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment
import tasks.Constants

fun main() {
    val env = StreamExecutionEnvironment.getExecutionEnvironment()
    env.config.disableClosureCleaner()
    env.enableCheckpointing(5_000)

    val settings = EnvironmentSettings.newInstance().inStreamingMode().build()
    val tableEnv = StreamTableEnvironment.create(env, settings)

    tableEnv.executeSql(
        """
        CREATE TABLE orders (
          customer STRING,
          status STRING,
          region STRING,
          amount DOUBLE,
          ts STRING,
          tsMillis BIGINT,
          proc_time AS PROCTIME()
        ) WITH (
          'connector' = 'kafka',
          'topic' = '${Constants.PARTITIONED_TOPIC}',
          'properties.bootstrap.servers' = 'localhost:9094',
          'properties.group.id' = 'flink-sql-status-counts',
          'properties.auto.offset.reset' = 'earliest',
          'scan.startup.mode' = 'earliest-offset',
          'value.format' = 'json',
          'value.json.ignore-parse-errors' = 'true',
          'value.json.timestamp-format.standard' = 'ISO-8601'
        )
        """
    )

    tableEnv.executeSql(
        """
        CREATE TABLE status_aggregates (
          window_start TIMESTAMP_LTZ(3),
          window_end TIMESTAMP_LTZ(3),
          status STRING,
          order_count BIGINT
        ) WITH (
          'connector' = 'kafka',
          'topic' = 'flink-aggregates-sql',
          'properties.bootstrap.servers' = 'localhost:9094',
          'properties.allow.auto.create.topics' = 'true',
          'value.format' = 'json',
          'value.json.timestamp-format.standard' = 'ISO-8601',
          'sink.delivery-guarantee' = 'at-least-once'
        )
        """
    )

    val result = tableEnv.executeSql(
        """
        INSERT INTO status_aggregates
        SELECT
          window_start,
          window_end,
          status,
          COUNT(*) AS order_count
        FROM TABLE(
          TUMBLE(TABLE orders, DESCRIPTOR(proc_time), INTERVAL '15' SECOND)
        )
        GROUP BY window_start, window_end, status
        """
    )

    val jobClient = result.jobClient.get()
    println("Submitted Flink SQL job: ${jobClient.jobID}")
    jobClient.getJobExecutionResult().get()
}
