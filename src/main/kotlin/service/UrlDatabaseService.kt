package com.iponomarev.service

import com.iponomarev.database.entity.UrlEntity
import com.iponomarev.database.table.Urls
import org.jetbrains.exposed.sql.transactions.transaction

class UrlDatabaseService {
    fun insertUrl(url: String, shortUrlCode: String): UrlEntity = transaction {
        UrlEntity.new {
            this.shortUrlCode = shortUrlCode
            this.url = url
        }
    }

    fun findByShortUrlCode(shortUrlCode: String): UrlEntity? = transaction {
        UrlEntity.find { Urls.shortUrlCode eq shortUrlCode }.firstOrNull()
    }

    fun findByUrl(url: String): UrlEntity? = transaction {
        UrlEntity.find { Urls.url eq url }.firstOrNull()
    }
}