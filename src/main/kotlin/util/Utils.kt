package com.iponomarev.util

import io.ktor.server.config.ApplicationConfig
import java.net.URI
import java.security.MessageDigest

const val BASE62_CHARS = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"

/**
 * Generates a short Base62 encoded string based on the SHA-256 hash of the input path.
 *
 * The function hashes the input string (`path`) using SHA-256, takes the first 8 bytes of the hash,
 * converts them into a numeric value, and then encodes that number into a Base62 string of the specified length.
 *
 * @param url the input string to hash and encode (typically a URL path).
 * @param length the desired length of the output short code (default is 6).
 * @return a Base62 encoded a short string representing the hash of the input path.
 */
fun generateShortBase62Code(url: String, length: Int = 6): String {
    val digest = MessageDigest.getInstance("SHA-256")
    val hashBytes = digest.digest(url.toByteArray(Charsets.UTF_8))

    var num = 0L
    for (i in 0 until 8) {
        num = (num shl 8) or (hashBytes[i].toInt() and 0xFF).toLong()
    }

    val sb = StringBuilder()
    repeat(length) {
        sb.append(BASE62_CHARS[num.mod(62)])
        num /= 62
    }
    return sb.toString()
}

fun formatShortUrl(host: String, shortUrlCode: String) =
    "$host/$shortUrlCode"

fun normalizeUrlHost(url: String): String {
    val uri = URI(url)
    val normalizedUri = URI(
        uri.scheme?.lowercase(),
        uri.userInfo,
        uri.host?.lowercase(),
        uri.port,
        uri.path,
        uri.query,
        uri.fragment
    )
    return normalizedUri.toString()
}

fun getEnvOrConfig(configKey: String, envKey: String, config: ApplicationConfig): String {
    return System.getenv(envKey) ?: config.property(configKey).getString()
}