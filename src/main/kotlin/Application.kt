package com.iponomarev

import com.iponomarev.database.DatabaseFactory
import com.iponomarev.routing.configureRouting
import io.ktor.server.application.Application

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module(skipDatabaseInit: Boolean = false) {
    configureSerialization()
    configureMonitoring()
    configureRouting()

    if (!skipDatabaseInit) {
        DatabaseFactory.init(environment.config)
    }
}

