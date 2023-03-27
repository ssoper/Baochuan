import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    val kotlinVersion = "1.5.30"
    kotlin("jvm") version kotlinVersion
    id("org.jetbrains.kotlin.plugin.serialization") version kotlinVersion
}

group = "com.seansoper"
version = "1.0"

val javaVersion = JavaVersion.VERSION_11
java {
    targetCompatibility = javaVersion
    sourceCompatibility = javaVersion
    withSourcesJar()
    withJavadocJar()
}

repositories {
    mavenCentral()
}

dependencies {
    // Ktor
    val ktorVersion = "1.6.5"
    implementation("io.ktor:ktor-server-core:$ktorVersion")
    implementation("io.ktor:ktor-server-netty:$ktorVersion")
    implementation("io.ktor:ktor-serialization:$ktorVersion")
    implementation("ch.qos.logback:logback-classic:1.2.7")

    // Jackson, keep at this version until Batil’s dep on JavaRx (via pl.wendigo.chrome) is updated
    val jacksonVersion = "2.12.5"
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonVersion")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:$jacksonVersion")

    // APIs
    implementation("net.jacobpeterson:alpaca-java:8.3.2")
    implementation("com.seansoper:batil:1.0.4")

    // Database
    implementation("org.ktorm:ktorm-core:3.4.1")
    implementation("org.mariadb.jdbc:mariadb-java-client:2.7.4")
    implementation("com.zaxxer:HikariCP:5.0.0")

    testImplementation("io.ktor:ktor-server-test-host:$ktorVersion")
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnit()
}

tasks.withType<KotlinCompile>() {
    kotlinOptions.jvmTarget = JavaVersion.VERSION_11.toString()
}

// Database tasks

val dbTasksGroup = "Database"
val dbName = project.property("com.seansoper.baochuan.db.name")
val dbUsername = project.property("com.seansoper.baochuan.db.username")
val dbPassword = project.property("com.seansoper.baochuan.db.password")

tasks.register("createTables") {
    group = dbTasksGroup

    doLast {
        val dir = "${projectDir}/data"
        exec {
            commandLine("sh", "-c", "mysql -u$dbUsername -p$dbPassword $dbName < $dir/create_tickers.sql")
        }
        exec {
            commandLine("sh", "-c", "mysql -u$dbUsername -p$dbPassword $dbName < $dir/create_tags.sql")
        }
        exec {
            commandLine("sh", "-c", "mysql -u$dbUsername -p$dbPassword $dbName < $dir/create_tickers_tags.sql")
            logger.info("Tables created")
        }
    }
}

tasks.register("dropTables") {
    group = dbTasksGroup

    doLast {
        exec {
            val dir = "${projectDir}/data"
            commandLine("sh", "-c", "mysql -u$dbUsername -p$dbPassword $dbName < $dir/drop_tables.sql")
            logger.info("Tables dropped")
        }
    }
}

tasks.register<GradleBuild>("resetData") {
    group = dbTasksGroup
    tasks = listOf("dropTables", "createTables")
}
