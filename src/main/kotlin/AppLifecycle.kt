package com.iponomarev

import com.iponomarev.repository.DatabaseFactory
import com.iponomarev.util.getEnvOrConfig
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationStarted
import io.ktor.server.application.ApplicationStopped
import org.koin.core.context.GlobalContext

fun Application.configureAppLifecycle(skipDatabaseInit: Boolean) {
    this.monitor.subscribe(ApplicationStarted) {
        val envMarker = getEnvOrConfig("env_marker", "ENV_MARKER", environment.config)
        val appHost = getEnvOrConfig("app.host", "APP_HOST", environment.config)

        environment.log.info(
            """
                Welcome to URL-shortener service!
                ---
                $envMarker IS LOADED
                ---
                Service is hosted on $appHost
                ---
            """.trimIndent()
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