package com.iponomarev

import com.iponomarev.repository.DatabaseFactory
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationStarted
import io.ktor.server.application.ApplicationStopped

fun Application.configureAppLifecycle(skipDatabaseInit: Boolean) {
    this.monitor.subscribe(ApplicationStarted) {
        if (!skipDatabaseInit) {
            DatabaseFactory.init(environment.config)
        }
    }

    this.monitor.subscribe(ApplicationStopped) {
        DatabaseFactory.close()
    }
}