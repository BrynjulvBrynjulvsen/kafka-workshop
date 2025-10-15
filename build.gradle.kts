import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val kotlin_version: String by project
val kafka_version: String by project
val confluent_version: String by project
val flink_version: String by project
val flink_connector_kafka_version: String by project

buildscript {
    val kotlin_version: String by project
    repositories {
        mavenCentral()
    }

    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlin_version")
        classpath("com.github.jengelman.gradle.plugins:shadow:5.0.0")
        classpath("org.jetbrains.kotlin:kotlin-serialization:$kotlin_version")
        classpath("org.owasp:dependency-check-gradle:7.1.1")
        classpath("com.github.ben-manes:gradle-versions-plugin:0.42.0")
    }
}

plugins {
    id("org.springframework.boot") version "3.2.0"
    id("io.spring.dependency-management") version "1.1.4"
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.21"
    id("com.github.davidmc24.gradle.plugin.avro") version "1.9.1"
    idea
//    id("org.jlleitschuh.gradle.ktlint") version "10.3.0"
    kotlin("jvm") version "1.9.21"
    kotlin("plugin.spring") version "1.9.21"
    application
}

/*application {
    mainClass.value("_1_MultiMemberConsumerGroupKt")
}*/

val jar by tasks.getting(Jar::class) {
    manifest {
        attributes["Main-Class"] = "io.bekk.ApplicationKt"
    }
}

repositories {
    mavenCentral()
    mavenLocal()
    maven { url = uri("https://packages.confluent.io/maven/") }
}

sourceSets {
    create("exercises") {
        compileClasspath += sourceSets.main.get().output
        runtimeClasspath += sourceSets.main.get().output
    }
}

val exercisesImplementation by configurations.getting {
    extendsFrom(configurations.implementation.get())
}



dependencies {
    implementation(kotlin("stdlib"))
    implementation("org.apache.kafka:kafka-clients:$kafka_version")
    implementation("io.confluent:kafka-avro-serializer:$confluent_version")
    implementation("org.springframework.kafka:spring-kafka:3.3.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.1")
    implementation("org.apache.avro:avro:1.11.4")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")
    implementation("org.apache.flink:flink-streaming-java:$flink_version")
    implementation("org.apache.flink:flink-clients:$flink_version")
    implementation("org.apache.flink:flink-connector-kafka:$flink_connector_kafka_version")
    implementation("org.apache.flink:flink-connector-base:$flink_version")
    implementation("org.apache.flink:flink-table-api-java-bridge:$flink_version")
    implementation("org.apache.flink:flink-table-runtime:$flink_version")
    implementation("org.apache.flink:flink-table-planner-loader:$flink_version")
    implementation("org.apache.flink:flink-json:$flink_version")
    implementation("org.apache.kafka:kafka-streams:$kafka_version")

    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-cache")
    implementation("org.springframework.boot:spring-boot-starter-validation")

    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}

java.sourceCompatibility = JavaVersion.VERSION_17
java.targetCompatibility = JavaVersion.VERSION_17

val compileKotlin: KotlinCompile by tasks

compileKotlin.dependsOn(tasks.generateAvroJava)

val serverOutputDir = project.layout.buildDirectory.dir("generated-api")
sourceSets {

    if (!gradle.startParameter.taskNames.any { it.lowercase().contains("ktLint") }) {
        val main by getting
        main.java.srcDir("${serverOutputDir.get()}/src/main/kotlin")
    }
}


tasks.register<JavaExec>("runKotlinClass") {
    dependsOn("compileKotlin")
    group = "application"
    description = "Runs a specified Kotlin class"
    classpath = sourceSets["exercises"].runtimeClasspath
    mainClass.set(project.findProperty("mainClass") as String?)
}

val flinkShadow by tasks.registering(Jar::class) {
    group = "distribution"
    description = "Packages the Flink exercise code into an executable fat jar"
    archiveClassifier.set("flink-exercises")
    from(sourceSets["exercises"].output)
    dependsOn("compileKotlin")

    manifest {
        attributes["Main-Class"] = "tasks.flink._4_SinkStatusCountsToKafkaKt"
    }

    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    val runtimeJars = configurations["runtimeClasspath"].filter { file ->
        if (!file.name.endsWith(".jar")) return@filter false
        val name = file.name
        name != "kafka-clients-${kafka_version}.jar" && !name.startsWith("kafka-streams")
    }

    from({ runtimeJars.map { zipTree(it) } })
}
