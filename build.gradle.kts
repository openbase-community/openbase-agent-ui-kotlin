plugins {
    id("org.jetbrains.kotlin.jvm") version "1.9.25"
    `java-library`
}

group = "community.openbase"
version = "0.1.0"

kotlin {
    jvmToolchain(17)
}

dependencies {
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.9.0")
}
