package com.iponomarev

import com.iponomarev.repository.DatabaseFactory
import com.iponomarev.util.AppLogger
import com.iponomarev.util.getEnvOrConfig
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationStarted
import io.ktor.server.application.ApplicationStopped
import org.koin.core.context.GlobalContext

fun Application.configureAppLifecycle(skipDatabaseInit: Boolean) {
    this.monitor.subscribe(ApplicationStarted) {
        val envMarker = getEnvOrConfig("env_marker", "ENV_MARKER", environment.config)
        val appHost = getEnvOrConfig("app.host", "APP_HOST", environment.config)
        val appVersion = getEnvOrConfig("app.version", "APP_VERSION", environment.config)
        val javaVersion = System.getProperty("java.version")
        val ktVersion = KotlinVersion.CURRENT

        AppLogger.log.info(
            """
            |
            |Welcome to URL-shortener service!
            |--------------------------------
            |Version: $appVersion
            |Environment: $envMarker
            |Host: $appHost
            |Java version: $javaVersion
            |Kotlin version: $ktVersion
            |Author: Ivan Ponomarev
            |Email: vankap0n@gmail.com
            |License: MIT
            |--------------------------------
            |Application started successfully!
            """.trimMargin()
        )

        if (!skipDatabaseInit) {
            DatabaseFactory.init(environment.config)
        }
    }

    this.monitor.subscribe(ApplicationStopped) {
        DatabaseFactory.close()

        if (GlobalContext.getOrNull() != null) {
            GlobalContext.stopKoin()
        }
    }
}