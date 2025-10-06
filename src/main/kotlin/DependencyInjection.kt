package com.iponomarev

import com.iponomarev.config.ConfigProvider
import com.iponomarev.repository.UrlRepository
import com.iponomarev.service.UrlDatabaseService
import com.iponomarev.service.UrlProcessorService
import io.ktor.server.application.Application
import io.ktor.server.application.install
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin

fun Application.configureDI(skipMetrics: Boolean) {
    install(Koin) {
        modules(
            module {
                single<UrlRepository> { UrlDatabaseService() }
                single<ConfigProvider> { ConfigProvider(environment) }
                if (!skipMetrics) {
                    single { UrlProcessorService(get(), get(), appMetricsRegistry) }
                } else {
                    single { UrlProcessorService(get(), get()) }
                }
            }
        )
    }
}