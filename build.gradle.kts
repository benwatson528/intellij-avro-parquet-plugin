import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

fun properties(key: String) = project.findProperty(key).toString()

plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.7.21"
    id("org.jetbrains.intellij") version "1.10.0"
}

group = properties("pluginGroup")
version = properties("pluginVersion")

repositories {
    mavenCentral()
}

dependencies {
    //Avro dependencies
    implementation("org.apache.avro:avro:1.11.1")
    implementation("org.xerial.snappy:snappy-java:1.1.8.4")

    //Parquet dependencies
    implementation("org.apache.parquet:parquet-avro:1.12.3")
    implementation("org.apache.parquet:parquet-column:1.12.3")
    implementation("org.apache.parquet:parquet-hadoop:1.12.3")
    implementation("org.apache.parquet:parquet-format-structures:1.12.3")
    implementation("org.apache.hadoop:hadoop-client:3.3.4")

    //External dependencies
    implementation("com.google.code.gson:gson:2.10")
    implementation("com.google.guava:guava:31.1-jre")
    implementation("com.fifesoft:rsyntaxtextarea:3.3.0")
    implementation("com.github.wnameless.json:json-flattener:0.15.1")

    //Test dependencies
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.9.1")
    testImplementation("org.assertj:assertj-core:3.23.1")
}

configurations.implementation {
    exclude(module = "slf4j-api")
    exclude(module = "slf4j-log4j12")
}

// Configure Gradle IntelliJ Plugin - read more: https://github.com/JetBrains/gradle-intellij-plugin
intellij {
    pluginName.set(properties("pluginName"))
    version.set(properties("platformVersion"))
    type.set(properties("platformType"))
    updateSinceUntilBuild.set(false)
    sameSinceUntilBuild.set(true)
}

tasks {
    // Set the JVM compatibility versions
    properties("javaVersion").let {
        withType<JavaCompile> {
            sourceCompatibility = it
            targetCompatibility = it
        }
        withType<KotlinCompile> {
            kotlinOptions.jvmTarget = it
        }
    }

    wrapper {
        gradleVersion = properties("gradleVersion")
    }

    test {
        useJUnitPlatform()
        testLogging {
            events("passed", "skipped", "failed")
        }
    }

    patchPluginXml {
        version.set(properties("pluginVersion"))
        sinceBuild.set(properties("pluginSinceBuild"))
        changeNotes.set(properties("changeNotes"))
    }
}
