package com.iponomarev

import com.iponomarev.repository.DatabaseFactory
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationStarted
import io.ktor.server.application.ApplicationStopped
import org.koin.core.context.GlobalContext

fun Application.configureAppLifecycle(skipDatabaseInit: Boolean) {
    this.monitor.subscribe(ApplicationStarted) {
        val envMarker = environment.config.propertyOrNull("env_marker")?.getString() ?: "NO MARKER"
        println("$envMarker IS LOADED")

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