package com.iponomarev

import com.codahale.metrics.MetricRegistry
import com.codahale.metrics.Slf4jReporter
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.application.log
import io.ktor.server.metrics.dropwizard.DropwizardMetrics
import io.ktor.server.plugins.calllogging.CallLogging
import io.ktor.server.request.path
import org.slf4j.event.Level
import java.util.concurrent.TimeUnit

lateinit var appMetricsRegistry: MetricRegistry

fun Application.configureMonitoring(skipMetrics: Boolean) {
    if (!skipMetrics) {
        install(DropwizardMetrics) {
            appMetricsRegistry = registry

            Slf4jReporter.forRegistry(registry)
                .outputTo(this@configureMonitoring.log)
                .convertRatesTo(TimeUnit.SECONDS)
                .convertDurationsTo(TimeUnit.MILLISECONDS)
                .build()
                .start(10, TimeUnit.SECONDS)
        }
    }

    install(CallLogging) {
        level = Level.INFO
        filter { call -> call.request.path().startsWith("/") }
    }
}
