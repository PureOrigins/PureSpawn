import java.util.*

plugins {
    kotlin("jvm") version "2.0.20"
    kotlin("plugin.serialization") version "2.0.20"
    id("net.minecrell.plugin-yml.bukkit") version "0.6.0"
    `maven-publish`
}

group = "it.pureorigins"
version = "1.0.0"

bukkit {
    name = project.name
    version = project.version.toString()
    main = "it.pureorigins.${project.name.lowercase(Locale.getDefault())}.${project.name}"
    apiVersion = "1.21.1"
    depend = listOf("PureCommon")
}

repositories {
    mavenCentral()
    maven("https://jitpack.io")
    maven("https://repo.purpurmc.org/snapshots")
}

dependencies {
    compileOnly("com.github.PureOrigins", "PureCommon", "0.3.10")
    compileOnly("org.purpurmc.purpur", "purpur-api", "1.21-R0.1-SNAPSHOT")
}

afterEvaluate {
    tasks {
        jar {
            archiveClassifier.set("")
        }

        compileKotlin {
            kotlinOptions.jvmTarget = "21"
        }
    }
}

kotlin {
    jvmToolchain {
        this.languageVersion.set(JavaLanguageVersion.of(21))
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "com.github.PureOrigins"
            artifactId = project.name
            version = project.version.toString()

            afterEvaluate {
                from(components["kotlin"])
                artifact(tasks["kotlinSourcesJar"])
            }
        }
    }
}