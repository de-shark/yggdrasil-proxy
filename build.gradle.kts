import java.io.ByteArrayOutputStream

plugins {
    id("java")
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "com.deshark"

fun getGitVersion(): String {
    return try {
        val stdout = ByteArrayOutputStream()
        exec {
            commandLine("git", "describe", "--tags", "--dirty", "--always")
            standardOutput = stdout
        }
        stdout.toString().trim().removePrefix("v")
    } catch (e: Exception) {
        "0.0.0-SNAPSHOT"
    }
}

version = getGitVersion()

repositories {
    mavenCentral()
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

dependencies {
    implementation("io.javalin:javalin-bundle:6.6.0")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.19.0")
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}

tasks.shadowJar {
    archiveBaseName.set("yggdrasil-proxy")
    archiveClassifier.set("")
    mergeServiceFiles()
    manifest {
        attributes["Main-Class"] = "com.deshark.Main"
    }
}

tasks.build {
    dependsOn(tasks.shadowJar)
}