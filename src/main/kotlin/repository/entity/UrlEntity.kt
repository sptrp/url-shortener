package com.iponomarev.repository.entity

import com.iponomarev.repository.table.Urls
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class UrlEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<UrlEntity>(Urls)

    var url by Urls.url
    var shortUrlCode by Urls.shortUrlCode
    var createdAt by Urls.createdAt
    var expiresAt by Urls.expiresAt
}