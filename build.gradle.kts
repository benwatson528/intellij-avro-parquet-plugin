import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

fun properties(key: String) = project.findProperty(key).toString()

plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.6.10"
    id("org.jetbrains.intellij") version "1.4.0"
}

group = properties("pluginGroup")
version = properties("pluginVersion")

repositories {
    mavenCentral()
}

dependencies {
    //Avro dependencies
    implementation("org.apache.avro:avro:1.10.2")
    implementation("org.xerial.snappy:snappy-java:1.1.8.4")

    //Parquet dependencies
    implementation("org.apache.parquet:parquet-avro:1.12.0")
    implementation("org.apache.parquet:parquet-column:1.12.0")
    implementation("org.apache.parquet:parquet-hadoop:1.12.0")
    implementation("org.apache.parquet:parquet-format-structures:1.12.0")
    implementation("org.apache.hadoop:hadoop-client:3.3.0")

    //External dependencies
    implementation("com.google.code.gson:gson:2.8.6")
    implementation("com.google.guava:guava:23.6-jre")
    implementation("com.fifesoft:rsyntaxtextarea:3.1.1")
    implementation("com.github.wnameless.json:json-flattener:0.11.1")

    //Test dependencies
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.7.0")
    testImplementation("org.assertj:assertj-core:3.18.1")
}


// Configure Gradle IntelliJ Plugin - read more: https://github.com/JetBrains/gradle-intellij-plugin
intellij {
    pluginName.set(properties("pluginName"))
    version.set(properties("platformVersion"))
    type.set(properties("platformType"))
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
        pluginDescription.set(properties("pluginDescription"))
        changeNotes.set(properties("changeNotes"))
    }
}
