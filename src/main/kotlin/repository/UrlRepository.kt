package com.iponomarev.repository

import com.iponomarev.repository.entity.UrlEntity
import java.time.Instant

interface UrlRepository {
    fun insertUrl(url: String, shortUrlCode: String, expiresAt: Instant): UrlEntity
    fun findByUrl(url: String): UrlEntity?
    fun findByShortUrlCode(shortUrlCode: String): UrlEntity?
    fun deleteExpiredUrls(): Int
}