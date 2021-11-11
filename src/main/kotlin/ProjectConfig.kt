package com.seansoper.baochuan

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.KotlinModule
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.jvm.Throws

data class PolygonConfig(
    val apiKey: String
)

data class ServerConfig(
    val port: Int = 8080
)

data class ProjectConfig(
    val polygon: PolygonConfig,
    val server: ServerConfig
) {
    companion object {
        @Throws
        fun parse(path: Path = Paths.get(System.getProperty("user.dir"), "baochuan.yaml")): ProjectConfig {
            val mapper = ObjectMapper(YAMLFactory())
            mapper.registerModule(KotlinModule())

            return Files.newBufferedReader(path).use {
                mapper.readValue(it, ProjectConfig::class.java)
            }
        }
    }
}
