package com.iponomarev.database.entity

import com.iponomarev.database.table.Urls
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class UrlEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<UrlEntity>(Urls)

    var url by Urls.url
    var shortUrlCode by Urls.shortUrlCode
}