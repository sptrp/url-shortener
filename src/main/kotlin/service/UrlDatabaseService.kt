package com.iponomarev.service

import com.iponomarev.repository.entity.UrlEntity
import com.iponomarev.repository.table.Urls
import com.iponomarev.repository.UrlRepository
import org.jetbrains.exposed.sql.transactions.transaction

class UrlDatabaseService : UrlRepository {
    override fun insertUrl(url: String, shortUrlCode: String): UrlEntity = transaction {
        UrlEntity.new {
            this.shortUrlCode = shortUrlCode
            this.url = url
        }
    }

    override fun findByShortUrlCode(shortUrlCode: String): UrlEntity? = transaction {
        UrlEntity.find { Urls.shortUrlCode eq shortUrlCode }.firstOrNull()
    }

    override fun findByUrl(url: String): UrlEntity? = transaction {
        UrlEntity.find { Urls.url eq url }.firstOrNull()
    }
}