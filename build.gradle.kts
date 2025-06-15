import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

fun properties(key: String) = project.findProperty(key).toString()

plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "2.1.21"
    id("org.jetbrains.intellij") version "1.17.4"
}

group = properties("pluginGroup")
version = properties("pluginVersion")

repositories {
    mavenCentral()
    maven {
        url = uri("https://jitpack.io")
    }
}

dependencies {
    //Avro dependencies
    implementation("org.apache.avro:avro:1.12.0")
    implementation("org.xerial.snappy:snappy-java:1.1.10.7")

    //Parquet dependencies
    implementation("org.apache.parquet:parquet-avro:1.15.2")
    implementation("org.apache.parquet:parquet-column:1.15.2")
    implementation("org.apache.parquet:parquet-hadoop:1.15.2")
    implementation("org.apache.parquet:parquet-format-structures:1.15.2")
    implementation("org.apache.hadoop:hadoop-client:3.4.1")


    //External dependencies
    implementation("com.google.code.gson:gson:2.13.1")
    implementation("com.google.guava:guava:33.4.8-jre")
    implementation("com.fifesoft:rsyntaxtextarea:3.6.0")
    implementation("com.github.wnameless.json:json-flattener:0.17.3")
    implementation("com.github.rdblue:brotli-codec:0.1.1")

    //Test dependencies
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.13.1")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.13.1")
    testImplementation("org.assertj:assertj-core:3.27.3")
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
