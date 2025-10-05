package com.iponomarev.service

import com.iponomarev.repository.UrlRepository
import com.iponomarev.util.generateShortBase62Code
import com.iponomarev.util.normalizeUrlHost
import java.net.MalformedURLException
import java.net.URL

/**
 * Service class responsible for processing URLs, including validation and shortening.
 *
 * @property urlRepository the database implementation used for storing and retrieving URL data.
 */
class UrlProcessorService(
    private val urlRepository: UrlRepository
) {
    companion object {
        /**
         * Validates a URL string.
         *
         * Uses `java.net.URL` to check if the provided URL string is well-formed.
         *
         * @param url the URL string to validate.
         * @return `true` if the URL is valid and well-formed, `false` otherwise.
         */
        fun isValidUrl(url: String): Boolean {
            return try {
                URL(url)
                true
            } catch (_: MalformedURLException) {
                false
            }
        }
    }

    /**
     * Retrieves an existing short URL code for the given URL or creates a new one.
     *
     * Checks if the URL already exists in the database; if so, returns its short code.
     * Otherwise, generates a new short code based on the URL's path, inserts it into the database,
     * and returns the new short code.
     *
     * The host part of url will be normalized.
     *
     * @param url the original URL to shorten.
     * @return the short URL code associated with the given URL.
     */
    fun getShortURLCodeOrCreateNew(url: String): String {
        urlRepository.findByUrl(url)?.let { persistedUrl ->
            return persistedUrl.shortUrlCode
        }

        val normalizedUrl = normalizeUrlHost(url)
        val shortUrlCode = generateShortBase62Code(normalizedUrl)
        val inserted = urlRepository.insertUrl(normalizedUrl, shortUrlCode)

        return inserted.shortUrlCode
    }

    /**
     * Retrieves the original URL associated with a given short URL code.
     *
     * @param shortUrlCode the code representing the shortened URL.
     * @return the original URL if found, or null if no matching URL exists.
     */
    fun getOriginalURL(shortUrlCode: String): String? =
        urlRepository.findByShortUrlCode(shortUrlCode)?.url
}