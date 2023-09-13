group = "me.jakejmattson"
version = "3.0.0"

plugins {
    kotlin("jvm") version "1.9.10"
    id("org.openjfx.javafxplugin") version "0.1.0"
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("com.github.ben-manes.versions") version "0.48.0"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("no.tornado:tornadofx:1.7.20")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
}

kotlin {
    jvmToolchain(11)
}

tasks {
    shadowJar {
        archiveFileName.set("AnyToImage-${version}.jar")
        manifest {
            attributes("Main-Class" to "me.jakejmattson.anytoimage.ConversionControllerKt")
        }
    }
}

javafx {
    version = "20"
    modules("javafx.controls")
}
