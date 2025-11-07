plugins {
    kotlin("jvm") version "2.2.20"
}

group = "com.epic"
version = "1.0-SNAPSHOT"

repositories {
    maven {
        name = "papermc"
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.9-rc1-R0.1-SNAPSHOT")
}

tasks.test {
    useJUnitPlatform()
}

tasks.jar {
    manifest {
        attributes["paperweight-mappings-namespace"] = "mojang"
    }
    destinationDirectory.set(layout.buildDirectory.dir("testing/plugins"))
}

kotlin {
    jvmToolchain(24)
}