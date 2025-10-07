package com.iponomarev.repository.table

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.timestampWithTimeZone

object Urls : IntIdTable() {
    val url = varchar("url", 256)
    val shortUrlCode = varchar("short_url_code", 256)
    val createdAt = timestampWithTimeZone("created_at")
    val expiresAt = timestampWithTimeZone("expires_at")
}