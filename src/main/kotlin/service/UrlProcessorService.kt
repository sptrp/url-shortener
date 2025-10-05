package com.iponomarev.service

import com.iponomarev.util.generateShortBase62Code
import java.net.MalformedURLException
import java.net.URL

/**
 * Service class responsible for processing URLs, including validation and shortening.
 *
 * @property urlDatabaseService the database service used for storing and retrieving URL data.
 */
class UrlProcessorService(
    private val urlDatabaseService: UrlDatabaseService = UrlDatabaseService()
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
     * @param url the original URL to shorten.
     * @return the short URL code associated with the given URL.
     */
    fun getShortURLCodeOrCreateNew(url: String): String {
        urlDatabaseService.findByUrl(url)?.let { persistedUrl ->
            return persistedUrl.shortUrlCode
        }

        val shortUrlCode = generateShortBase62Code(URL(url).path)
        val inserted = urlDatabaseService.insertUrl(url, shortUrlCode)

        return inserted.shortUrlCode
    }

    /**
     * Retrieves the original URL associated with a given short URL code.
     *
     * @param shortUrlCode the code representing the shortened URL.
     * @return the original URL if found, or null if no matching URL exists.
     */
    fun getOriginalURL(shortUrlCode: String): String? =
        urlDatabaseService.findByShortUrlCode(shortUrlCode)?.url
}