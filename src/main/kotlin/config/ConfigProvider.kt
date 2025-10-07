package com.iponomarev.config

import io.ktor.server.config.ApplicationConfig

data class AppConfig(
    val host: String,
    val apiUrl: String,
    val skipMetrics: Boolean,
    val expirationTimeDays: Long
)

class ConfigProvider(config: ApplicationConfig) {

    val appConfig: AppConfig

    init {
        val host = System.getenv("APP_HOST") ?: config.property("app.host").getString()
        val apiUrl = System.getenv("API_URL") ?: config.property("app.apiUrl").getString()
        val skipMetrics = System.getenv("SKIP_METRICS") ?: config.property("app.skipMetrics").getString()
        val expirationTimeDays =
            System.getenv("DB_EXPIRATION_TIME_DAYS") ?: config.property("db.expirationTimeDays").getString()

        appConfig = AppConfig(
            host = host,
            apiUrl = apiUrl,
            skipMetrics = skipMetrics.toBoolean(),
            expirationTimeDays = expirationTimeDays.toLong()
        )
    }
}