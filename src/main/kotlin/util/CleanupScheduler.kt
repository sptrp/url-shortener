package com.iponomarev.util

import com.iponomarev.repository.UrlRepository
import java.util.Timer
import java.util.concurrent.TimeUnit
import kotlin.concurrent.timer

object CleanupScheduler {
    private var cleanupTimer: Timer? = null

    fun start(urlRepository: UrlRepository, intervalHours: Long = 24) {
        val period = TimeUnit.HOURS.toMillis(intervalHours)
        val initialDelay = TimeUnit.HOURS.toMillis(1)

        cleanupTimer = timer(
            name = "url-cleanup-scheduler",
            daemon = true,
            initialDelay = initialDelay,
            period = period
        ) {
            try {
                AppLogger.log.info("Starting scheduled cleanup of expired URLs...")
                val deletedCount = urlRepository.deleteExpiredUrls()
                AppLogger.log.info("Cleanup completed: deleted $deletedCount expired URLs")
            } catch (e: Exception) {
                AppLogger.log.error("Error during cleanup job", e)
            }
        }

        AppLogger.log.info("Cleanup scheduler started (runs every $intervalHours hours)")
    }

    fun stop() {
        cleanupTimer?.cancel()
        cleanupTimer = null
        AppLogger.log.info("Cleanup scheduler stopped")
    }
}