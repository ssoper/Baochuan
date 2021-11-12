import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.5.30"
}

group = "com.seansoper"
version = "1.0"

repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

// Can’t update to latest (1.6.5) until issue resolved in Polygon library
// https://github.com/polygon-io/client-jvm/issues/25
val ktorVersion = "1.3.1"

dependencies {
    implementation("io.ktor:ktor-server-core:$ktorVersion")
    implementation("io.ktor:ktor-server-netty:$ktorVersion")
    implementation("io.ktor:ktor-serialization:$ktorVersion")
    implementation("ch.qos.logback:logback-classic:1.2.6")

    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.13.0")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.13.0")

    implementation("com.github.polygon-io:client-jvm:v2.0.0")
    implementation("com.squareup.okhttp3:okhttp:4.9.2")

    testImplementation("io.ktor:ktor-server-test-host:$ktorVersion")
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnit()
}

tasks.withType<KotlinCompile>() {
    kotlinOptions.jvmTarget = JavaVersion.VERSION_11.toString()
}