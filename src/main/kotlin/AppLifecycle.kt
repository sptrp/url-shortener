package com.iponomarev

import com.iponomarev.repository.DatabaseFactory
import com.iponomarev.repository.UrlRepository
import com.iponomarev.util.AppLogger
import com.iponomarev.util.CleanupScheduler
import com.iponomarev.util.getEnvOrConfig
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationStarted
import io.ktor.server.application.ApplicationStopped
import org.koin.core.context.GlobalContext
import org.koin.ktor.ext.inject

fun Application.configureAppLifecycle(skipMetrics: Boolean, skipDatabaseInit: Boolean) {
    if (!skipDatabaseInit) { // Skip for test environment
        DatabaseFactory.init(environment.config)
    }

    this.monitor.subscribe(ApplicationStarted) {
        val envMarker = getEnvOrConfig("env_marker", "ENV_MARKER", environment.config)
        val appHost = getEnvOrConfig("app.host", "APP_HOST", environment.config)
        val appVersion = getEnvOrConfig("app.version", "APP_VERSION", environment.config)
        val metricsEnabled = if (skipMetrics) { "DISABLED" } else { "ENABLED" }
        val databaseCleanupEnabled = if (skipDatabaseInit) { "DISABLED" } else { "ENABLED" }

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
            |Database and cleanup: $databaseCleanupEnabled
            |Metrics: $metricsEnabled
            |Author: Ivan Ponomarev
            |Email: mailto:vankap0n@gmail.com
            |License: MIT
            |--------------------------------
            |Application started successfully!
            """.trimMargin()
        )

        if (!skipDatabaseInit) {
            val urlRepository by inject<UrlRepository>()
            CleanupScheduler.start(urlRepository)
        }
    }

    this.monitor.subscribe(ApplicationStopped) {
        AppLogger.log.info("Shutting down application...")
        CleanupScheduler.stop()
        DatabaseFactory.close()

        if (GlobalContext.getOrNull() != null) {
            GlobalContext.stopKoin()
        }
        AppLogger.log.info("Application stopped successfully")
    }
}