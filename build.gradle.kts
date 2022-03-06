import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

fun properties(key: String) = project.findProperty(key).toString()

plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.6.10"
    id("org.jetbrains.intellij") version "1.4.0"
    id("maven-publish")
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

    patchPluginXml {
        version.set(properties("pluginVersion"))
        sinceBuild.set(properties("pluginSinceBuild"))
        untilBuild.set(properties("pluginUntilBuild"))
        pluginDescription.set(properties("pluginDescription"))
        changeNotes.set(properties("changeNotes"))
//        // Extract the <!-- Plugin description --> section from README.md and provide for the plugin's manifest
//        pluginDescription.set(
//                projectDir.resolve("README.md").readText().lines().run {
//                    val start = "<!-- Plugin description -->"
//                    val end = "<!-- Plugin description end -->"
//
//                    if (!containsAll(listOf(start, end))) {
//                        throw GradleException("Plugin description section not found in README.md:\n$start ... $end")
//                    }
//                    subList(indexOf(start) + 1, indexOf(end))
//                }.joinToString("\n").run { markdownToHTML(this) }
//        )
//
//        // Get the latest available change notes from the changelog file
//        changeNotes.set(provider {
//            changelog.run {
//                getOrNull(properties("pluginVersion")) ?: getLatest()
//            }.toHTML()
//        })
    }
}






//
//configurations.all {
//    exclude module: 'log4j'
//    exclude module: 'slf4j-log4j12'
//}

//test {
//    useJUnitPlatform()
//    testLogging {
//        events "passed", "skipped", "failed"
//    }
//}
//
//intellij {
//    updateSinceUntilBuild = false
//    version '2017.1.6'
//    patchPluginXml {
//        sinceBuild("171")
//        changeNotes """
//      Now works on M1 Macs."""
//    }
//}
//
//buildSearchableOptions.enabled = false
//
//publishing {
//    repositories {
//        maven {
//            name = "GitHubPackages"
//            url = uri("https://maven.pkg.github.com/benwatson528/intellij-avro-parquet-plugin")
//            credentials {
//                username = System.getenv("USERNAME")
//                password = System.getenv("TOKEN")
//            }
//        }
//    }
//    publications {
//        maven(MavenPublication) {
//            artifact file("build/distributions/intellij-avro-parquet-viewer-${project.version}.zip")
//        }
//    }
//}
