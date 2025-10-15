@file:Suppress("UNREACHABLE_CODE", "UNUSED_VARIABLE")

package tasks.flink

import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment
import org.apache.flink.table.api.EnvironmentSettings
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment

// Step 5 – Recreate the status-count pipeline using Flink SQL.
//
// Goal: express the windowed status counts declaratively with SQL and sink the
// results back to Kafka (e.g. a `flink-aggregates-sql` topic).
//
// Suggested steps:
// 1. Create a `StreamExecutionEnvironment` and matching `StreamTableEnvironment` in streaming mode.
// 2. Register a Kafka source table that consumes JSON orders from `partitioned-topic`.
//    - Add a computed processing-time column (`proc_time AS PROCTIME()`) for the window descriptor.
//    - Use `'value.format' = 'json'` (plus any JSON-specific options) so the Table API can deserialize the payload.
// 3. Register a Kafka sink table for the status aggregates (topic `flink-aggregates-sql`) using the JSON format.
//    - Enable auto topic creation if you prefer (`'properties.allow.auto.create.topics' = 'true'`).
// 4. Issue an `INSERT INTO … SELECT` that applies a tumbling processing-time window (e.g. `INTERVAL '15' SECOND`) and counts per status.
// 5. Enable checkpointing (`env.enableCheckpointing(...)`), submit the INSERT, and block on the returned `TableResult.jobClient`.
//
// Execute with:
// ./gradlew runKotlinClass -PmainClass=tasks.flink._5_StatusCountsWithSqlKt
fun main() {
    val env = StreamExecutionEnvironment.getExecutionEnvironment()
    env.config.disableClosureCleaner()

    val settings = EnvironmentSettings.newInstance().inStreamingMode().build()
    val tableEnv = StreamTableEnvironment.create(env, settings)

    // TODO: Define the source table for orders (connector 'kafka', format 'json').
    // tableEnv.executeSql("""
    // CREATE TABLE orders (...)
    // WITH (...)
    // """)

    // TODO: Register the sink table for aggregates (topic flink-aggregates-sql).
    // tableEnv.executeSql("""
    // CREATE TABLE status_aggregates (...)
    // WITH (...)
    // """)

    // TODO: Submit the INSERT statement that performs the tumbling window aggregation and counts per status.
    // val result = tableEnv.executeSql("""
    // INSERT INTO status_aggregates
    // SELECT ...
    // """)

    // TODO: Block on the job so the program keeps running.
    // val jobClient = result.jobClient.get()
    // println("Submitted job: ${jobClient.jobID}")
    // jobClient.getJobExecutionResult().get()
}
