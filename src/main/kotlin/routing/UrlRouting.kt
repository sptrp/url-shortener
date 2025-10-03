package com.iponomarev.routing

import com.iponomarev.model.RequestDto
import com.iponomarev.model.ResponseDto
import com.iponomarev.service.UrlProcessorService
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
import java.net.MalformedURLException
import java.net.URL

const val API_URL = "url-shortener/api/"

fun Application.configureRouting() {
    val urlProcessorService = UrlProcessorService()

    install(StatusPages) {
        exception<Throwable> { call, cause ->
            call.respondText(text = "500: $cause" , status = HttpStatusCode.InternalServerError)
        }
    }

    routing {
        route(API_URL) {
            post("/shortUrl") {
                val request = call.receive<RequestDto>()
                val url = request.url

                // URL validation
                try {
                    URL(url)
                } catch (e: MalformedURLException) {
                    call.respond(HttpStatusCode.BadRequest, "Invalid URL format")
                    return@post
                }

                val shortUrl = urlProcessorService.shorten(url)
                call.respond(ResponseDto(url = shortUrl), typeInfo = TypeInfo(ResponseDto::class))
            }

            get("/shortUrl/{hash}") {
                val id = call.parameters["hash"] ?: throw IllegalArgumentException("Hash parameter is required")

                call.respond(
                     ResponseDto(
                        url = urlProcessorService.unshorten(id)
                    ),
                    typeInfo = TypeInfo(ResponseDto::class)
                )
            }
        }
    }
}
