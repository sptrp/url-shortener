package com.iponomarev.service

import com.codahale.metrics.MetricRegistry
import com.codahale.metrics.Timer
import com.iponomarev.config.ConfigProvider
import com.iponomarev.repository.UrlRepository
import com.iponomarev.util.Logging
import com.iponomarev.util.generateShortBase62Code
import com.iponomarev.util.getNowOffSet
import com.iponomarev.util.normalizeUrlHost
import java.time.OffsetDateTime
import java.time.temporal.ChronoUnit

/**
 * Service class responsible for processing URLs, including validation and shortening.
 *
 * @property urlRepository the database implementation used for storing and retrieving URL data.
 */
class UrlProcessorService(
    private val urlRepository: UrlRepository,
    private val configProvider: ConfigProvider,
    metricsRegistry: MetricRegistry? = null
) : Logging {
    private val urlCreatedCounter = metricsRegistry?.counter("url.created")
    private val urlLookupTimer = metricsRegistry?.timer("url.lookup.time")
    private val skipMetrics = configProvider.appConfig.skipMetrics

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
        val foundUrl = measureIfMetricsEnabled(urlLookupTimer) { urlRepository.findByUrl(url) }

        foundUrl?.let { persistedUrl ->
            log.debug("getShortURLCodeOrCreateNew: found persisted shortUrl {}", persistedUrl)
            return persistedUrl.shortUrlCode
        }

        val normalizedUrl = normalizeUrlHost(url)
        val shortUrlCode = generateShortBase62Code(normalizedUrl)

        val inserted = urlRepository.insertUrl(normalizedUrl, shortUrlCode, calculateExpirationTime())
        if (!skipMetrics) {
            urlCreatedCounter?.inc()
        }

        return inserted.shortUrlCode
    }

    /**
     * Retrieves the original URL associated with a given short URL code.
     *
     * @param shortUrlCode the code representing the shortened URL.
     * @return the original URL if found, or null if no matching URL exists.
     */
    fun getOriginalURL(shortUrlCode: String): String? =
        measureIfMetricsEnabled(urlLookupTimer) {
            urlRepository.findByShortUrlCode(shortUrlCode)?.url
        }

    private inline fun <T> measureIfMetricsEnabled(timer: Timer?, block: () -> T): T {
        if (skipMetrics || timer == null) {
            return block()
        }
        val context = timer.time()
        return try {
            block()
        } finally {
            context.stop()
        }
    }

    private fun calculateExpirationTime(): OffsetDateTime {
        val expirationDays = configProvider.appConfig.expirationTimeDays
        require(expirationDays > 0) { "Expiration time must be positive, got: $expirationDays" }
        return getNowOffSet().plus(expirationDays, ChronoUnit.DAYS)
    }
}