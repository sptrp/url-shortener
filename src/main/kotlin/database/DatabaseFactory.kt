package com.iponomarev.database

import com.iponomarev.database.table.Urls
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

object DatabaseFactory {
    fun init() {
        configureDatabase()
        transaction {
            SchemaUtils.create(Urls) // Create tables here
        }
    }
}

fun configureDatabase() {
    Database.connect(
        url = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;",
        driver = "org.h2.Driver",
        user = "root",
        password = ""
    )
}