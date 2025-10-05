package com.iponomarev.config

import io.ktor.server.application.ApplicationEnvironment

data class AppConfig(
    val host: String,
    val apiUrl: String
)

class ConfigProvider(environment: ApplicationEnvironment) {

    val appConfig: AppConfig

    init {
        val config = environment.config
        val host = System.getenv("APP_HOST") ?: config.property("app.host").getString()
        val apiUrl = System.getenv("API_URL") ?: config.property("app.apiUrl").getString()

        appConfig = AppConfig(
            host = host,
            apiUrl = apiUrl
        )
    }
}