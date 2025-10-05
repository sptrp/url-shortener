package com.iponomarev

import com.iponomarev.routing.configureRouting
import io.ktor.server.application.Application

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    val skipDatabaseInit = environment.config.propertyOrNull("db.skipInitialisation")?.getString()?.toBoolean()
        ?: true

    configureAppLifecycle(skipDatabaseInit)
    configureDI()
    configureSerialization()
    configureMonitoring()
    configureRouting()
}

