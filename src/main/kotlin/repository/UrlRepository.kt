package com.iponomarev.repository

import com.iponomarev.repository.entity.UrlEntity

interface UrlRepository {
    fun insertUrl(url: String, shortUrlCode: String): UrlEntity
    fun findByUrl(url: String): UrlEntity?
    fun findByShortUrlCode(shortUrlCode: String): UrlEntity?
}