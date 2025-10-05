package com.iponomarev.repository.table

import org.jetbrains.exposed.dao.id.IntIdTable

object Urls : IntIdTable() {
    val url = varchar("url", 256)
    val shortUrlCode = varchar("short_url_code", 256)
}