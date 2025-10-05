package com.iponomarev.routing

import com.iponomarev.config.ConfigProvider
import com.iponomarev.model.RequestDto
import com.iponomarev.model.ResponseDto
import com.iponomarev.service.UrlProcessorService
import com.iponomarev.service.UrlProcessorService.Companion.isValidUrl
import com.iponomarev.util.formatShortUrl
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import io.ktor.util.reflect.TypeInfo
import org.koin.ktor.ext.inject

const val API_URL = "/api/v1"

/**
 * Configures the HTTP routing for the url-shortener.
 *
 * Sets up routing for URL shortening, URL redirect by short code,
 * health check endpoint, and global exception handling.
 *
 * @receiver Application the url-shortener instance to configure
 */
fun Application.configureRouting() {
    val configProvider = ConfigProvider(environment)
    val urlProcessorService by inject<UrlProcessorService>()

    install(StatusPages) {
        exception<IllegalArgumentException> { call, cause ->
            call.application.environment.log.warn("Bad request: ${cause.message}")
            call.respond(
                HttpStatusCode.BadRequest,
                ResponseDto(success = false, url = null, error = cause.message ?: "Bad Request")
            )
        }

        exception<NoSuchElementException> { call, cause ->
            call.application.environment.log.warn("Not found: ${cause.message}")
            call.respond(
                HttpStatusCode.NotFound,
                ResponseDto(success = false, url = null, error = cause.message ?: "Not Found")
            )
        }

        exception<Throwable> { call, cause ->
            call.application.environment.log.error("Unhandled exception", cause)
            call.respond(
                HttpStatusCode.InternalServerError,
                ResponseDto(success = false, url = null, error = "Internal server error occurred. Please contact support.")
            )
        }
    }

    routing {

        route(API_URL) {
            post("/shortUrl") {
                val request = call.receive<RequestDto>()
                val url = request.url

                if (!isValidUrl(url)) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ResponseDto(success = false, url = null, error = "Invalid URL format")
                    )
                    return@post
                }

                val shortUrlCode = urlProcessorService.getShortURLCodeOrCreateNew(url)
                val shortUrl = formatShortUrl(configProvider.appConfig.host, shortUrlCode)
                call.respond(
                    ResponseDto(success = true, url = shortUrl, error = null),
                    typeInfo = TypeInfo(ResponseDto::class)
                )
            }
        }

        get("/{shortUrlCode}") {
            val shortUrlCode = call.parameters["shortUrlCode"]

            if (shortUrlCode == null) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    ResponseDto(success = false, url = null, error = "Missing shortUrlCode")
                )
                return@get
            }

            val originalUrl = urlProcessorService.getOriginalURL(shortUrlCode)
            if (originalUrl != null) {
                call.respond(
                    ResponseDto(success = true, url = originalUrl, error = null)
                )
            } else {
                call.respond(
                    HttpStatusCode.NotFound,
                    ResponseDto(success = false, url = null, error = "Original URL not found")
                )
            }
        }

        get("/healthcheck") {
            call.respondText("OK", contentType = ContentType.Text.Plain)
        }
    }
}
