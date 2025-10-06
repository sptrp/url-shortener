package com.iponomarev

import com.iponomarev.util.getEnvOrConfig
import io.ktor.server.application.Application

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    val skipDatabaseInit = getEnvOrConfig("db.skipInitialisation", "DB_SKIP_INITIALISATION", environment.config).toBoolean()
    val skipMetrics = getEnvOrConfig("app.skipMetrics", "SKIP_METRICS", environment.config).toBoolean()

    configureAppLifecycle(skipDatabaseInit)
    configureMonitoring(skipMetrics)
    configureDI(skipMetrics)
    configureExceptionHandling()
    configureSerialization()
    configureRouting()
}

