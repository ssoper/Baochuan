import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.5.30"
}

group = "com.seansoper"
version = "1.0"

repositories {
    mavenCentral()
}

dependencies {
    val ktorVersion = "1.6.5"

    implementation("io.ktor:ktor-server-core:$ktorVersion")
    implementation("io.ktor:ktor-server-netty:$ktorVersion")
    implementation("io.ktor:ktor-serialization:$ktorVersion")
    implementation("ch.qos.logback:logback-classic:1.2.7")

    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.13.0")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.13.0")

    implementation("net.jacobpeterson:alpaca-java:8.3.2")
    implementation("org.ktorm:ktorm-core:3.4.1")
    implementation("org.mariadb.jdbc:mariadb-java-client:2.7.4")

    testImplementation("io.ktor:ktor-server-test-host:$ktorVersion")
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnit()
}

tasks.withType<KotlinCompile>() {
    kotlinOptions.jvmTarget = JavaVersion.VERSION_11.toString()
}