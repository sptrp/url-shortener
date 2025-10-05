package com.iponomarev

import com.iponomarev.repository.UrlRepository
import com.iponomarev.service.UrlDatabaseService
import com.iponomarev.service.UrlProcessorService
import io.ktor.server.application.Application
import io.ktor.server.application.install
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin

val appModule = module {
    single<UrlRepository> { UrlDatabaseService() }
    single { UrlProcessorService(get()) }
}

fun Application.configureDI() {
    install(Koin) {
        modules(appModule)
    }
}