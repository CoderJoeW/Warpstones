plugins {
    kotlin("jvm") version "2.2.20"
    id("com.gradleup.shadow") version "9.2.2"
}

group = "com.epic"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven {
        name = "papermc"
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }
}

dependencies {
    implementation(kotlin("stdlib"))
    compileOnly("io.papermc.paper:paper-api:1.21.10-R0.1-SNAPSHOT")
}

// JVM toolchain
kotlin {
    jvmToolchain(21)
}

tasks.named<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>("shadowJar") {
    archiveClassifier.set("")
    mergeServiceFiles()

    relocate("kotlin", "com.epic.warpstones.lib.kotlin")
    relocate("kotlinx", "com.epic.warpstones.lib.kotlinx")

    destinationDirectory.set(layout.buildDirectory.dir("testing/plugins"))
}

tasks.build {
    dependsOn(tasks.named("shadowJar"))
}

tasks.jar {
    enabled = false
}
