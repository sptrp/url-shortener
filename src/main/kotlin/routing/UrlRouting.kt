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
import io.ktor.server.plugins.origin
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.response.respondRedirect
import io.ktor.server.response.respondText
import io.ktor.server.routing.RoutingCall
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import io.ktor.util.reflect.TypeInfo
import org.koin.ktor.ext.inject

/**
 * Configures the HTTP routing for the url-shortener application.
 *
 * Sets up:
 * - POST [API_URL]/shortUrl to receive a [RequestDto] and respond with a [ResponseDto] containing the shortened URL.
 * - GET [API_URL]/{shortUrlCode} to retrieve the original URL as a [ResponseDto] in JSON format.
 * - GET /{shortUrlCode} as a permanent redirect (HTTP 301) to the original URL.
 * - GET /healthcheck endpoint for simple health status.
 *
 * Installs the [StatusPages] plugin handling common exceptions:
 * - [IllegalArgumentException] returns HTTP 400 with details.
 * - [NoSuchElementException] returns HTTP 404 with details.
 * - Other exceptions return HTTP 500 with a generic error.
 *
 * @receiver Application the Ktor application instance to configure routing on.
 *
 * @see [RequestDto]
 * @see [ResponseDto]
 * @see [UrlProcessorService]
 */
fun Application.configureRouting() {
    val configProvider = ConfigProvider(environment)
    val urlProcessorService by inject<UrlProcessorService>()
    val apiUrl = configProvider.appConfig.apiUrl

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

        route(apiUrl) {
            /**
             * POST API_URL/shortUrl
             * Receives JSON { "url": "<url>" } to shorten. @see [RequestDto]
             * Responds with [ResponseDto] containing shortened URL or error.
             * @see [UrlProcessorService]
             */
            post("/shortUrl") {
                val request = call.receive<RequestDto>()
                val url = request.url

                environment.log.info("POST $apiUrl/shortUrl from ${call.request.origin.remoteHost} (${call.request.headers["User-Agent"] ?: "Unknown"})")

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

            /**
             * GET API_URL/{shortUrlCode}
             * GET route to fetch the original URL, returns JSON [ResponseDto].
             * @see [UrlProcessorService]
             */
            get("/{shortUrlCode}") {
                environment.log.info("GET $apiUrl/{shortUrlCode} from ${call.request.origin.remoteHost} (${call.request.headers["User-Agent"] ?: "Unknown"})")

                val originalUrl = call.fetchOriginalUrl(urlProcessorService, call.parameters["shortUrlCode"]) ?: return@get
                call.respond(ResponseDto(success = true, url = originalUrl, error = null))
            }
        }

        /**
         * GET /{shortUrlCode}
         * Redirects to the original URL
         * Performs a permanent redirect (HTTP 301).
         */
        get("/{shortUrlCode}") {
            environment.log.info("GET /{shortUrlCode} from ${call.request.origin.remoteHost} (${call.request.headers["User-Agent"] ?: "Unknown"})")

            val originalUrl = call.fetchOriginalUrl(urlProcessorService, call.parameters["shortUrlCode"]) ?: return@get
            call.respondRedirect(originalUrl, permanent = true)
        }

        get("/healthcheck") {
            call.respondText("OK", contentType = ContentType.Text.Plain)
        }
    }
}

/**
 * Fetches the original URL associated with a given short URL code.
 * Validates input and sends appropriate HTTP responses with [ResponseDto] on failure cases.
 *
 * @param urlProcessorService The service used to look up the original URL.
 * @param shortUrlCode nullable short URL code.
 * @return The original URL if found, null if not found or input invalid (with response sent).
 */
private suspend fun RoutingCall.fetchOriginalUrl(urlProcessorService: UrlProcessorService, shortUrlCode: String?): String? {
    if (shortUrlCode == null) {
        respond(
            HttpStatusCode.BadRequest,
            ResponseDto(success = false, url = null, error = "Missing shortUrlCode")
        )
        return null
    }

    val originalUrl = urlProcessorService.getOriginalURL(shortUrlCode)
    if (originalUrl == null) {
        respond(
            HttpStatusCode.NotFound,
            ResponseDto(success = false, url = null, error = "Original URL not found")
        )
        return null
    }

    return originalUrl
}
