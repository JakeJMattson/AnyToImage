group = "me.jakejmattson"
version = "2.0.0"

plugins {
    kotlin("jvm") version "1.4.10"
    id("org.openjfx.javafxplugin") version "0.0.9"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("no.tornado:tornadofx:1.7.20")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.9")
}

tasks.compileKotlin {
    kotlinOptions.jvmTarget = "1.8"
}

javafx {
    version = "15"
    modules("javafx.controls")
}