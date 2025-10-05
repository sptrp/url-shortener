package com.iponomarev.config

import io.ktor.server.application.ApplicationEnvironment

data class AppConfig(val host: String)

class ConfigProvider(environment: ApplicationEnvironment) {

    val appConfig: AppConfig

    init {
        val config = environment.config
        val host = config.property("app.host").getString()
        appConfig = AppConfig(host)
    }
}