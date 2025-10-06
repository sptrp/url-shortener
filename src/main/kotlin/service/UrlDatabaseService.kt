package com.iponomarev.service

import com.iponomarev.repository.UrlRepository
import com.iponomarev.repository.entity.UrlEntity
import com.iponomarev.repository.table.Urls
import org.jetbrains.exposed.sql.SqlExpressionBuilder.lessEq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Instant

class UrlDatabaseService : UrlRepository {
    override fun insertUrl(url: String, shortUrlCode: String, expiresAt: Instant): UrlEntity = transaction {
        UrlEntity.new {
            this.shortUrlCode = shortUrlCode
            this.url = url
            this.createdAt = Instant.now()
            this.expiresAt = expiresAt
        }
    }

    override fun findByShortUrlCode(shortUrlCode: String): UrlEntity? = transaction {
        UrlEntity.find {
            (Urls.shortUrlCode eq shortUrlCode) and (Urls.expiresAt greater Instant.now())
        }.firstOrNull()
    }

    override fun findByUrl(url: String): UrlEntity? = transaction {
        UrlEntity.find {
            (Urls.url eq url) and (Urls.expiresAt greater Instant.now())
        }.firstOrNull()
    }

    override fun deleteExpiredUrls(): Int = transaction {
        Urls.deleteWhere { Urls.expiresAt lessEq Instant.now() }
    }

}