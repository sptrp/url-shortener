package com.iponomarev.database

import com.iponomarev.database.table.Urls
import io.ktor.server.config.ApplicationConfig
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

object DatabaseFactory {
    fun init(config: ApplicationConfig) {
        val user = config.property("db.user").getString()
        val password = config.property("db.password").getString()
        val driver = config.property("db.driver").getString()
        val url = config.property("db.url").getString()

        Database.connect(
            url = url,
            driver = driver,
            user = user,
            password = password
        )
        transaction {
            SchemaUtils.create(Urls) // Create tables here
        }
    }
}
