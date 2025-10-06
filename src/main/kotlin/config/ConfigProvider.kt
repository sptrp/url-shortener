package com.iponomarev.config

import io.ktor.server.config.ApplicationConfig

data class AppConfig(
    val host: String,
    val apiUrl: String,
    val skipMetrics: Boolean
)

class ConfigProvider(config: ApplicationConfig) {

    val appConfig: AppConfig

    init {
        val host = System.getenv("APP_HOST") ?: config.property("app.host").getString()
        val apiUrl = System.getenv("API_URL") ?: config.property("app.apiUrl").getString()
        val skipMetrics = System.getenv("SKIP_METRICS") ?: config.property("app.skipMetrics").getString()

        appConfig = AppConfig(
            host = host,
            apiUrl = apiUrl,
            skipMetrics = skipMetrics.toBoolean()
        )
    }
}