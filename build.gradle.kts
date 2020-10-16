group = "me.jakejmattson"
version = "3.0.0"

plugins {
    kotlin("jvm") version "1.4.10"
    id("org.openjfx.javafxplugin") version "0.0.9"
    id("com.github.johnrengelman.shadow") version "6.1.0"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("no.tornado:tornadofx:1.7.20")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.9")
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }

    shadowJar {
        archiveFileName.set("AnyToImage-${version}.jar")
        manifest {
            attributes(
                "Main-Class" to "me.jakejmattson.anytoimage.ConversionControllerKt"
            )
        }
    }
}

javafx {
    version = "15"
    modules("javafx.controls")
}
