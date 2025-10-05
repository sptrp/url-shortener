package com.iponomarev.repository

import com.iponomarev.repository.table.Urls
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.config.ApplicationConfig
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

object DatabaseFactory {
    private var hikariDataSource: HikariDataSource? = null

    fun init(config: ApplicationConfig) {
        val hikariConfig = HikariConfig().apply {
            jdbcUrl = config.property("db.url").getString()
            driverClassName = config.property("db.driver").getString()
            username = config.property("db.user").getString()
            password = config.property("db.password").getString()
            maximumPoolSize = 5 // Adjust pool size as needed
            isAutoCommit = false
            transactionIsolation = "TRANSACTION_REPEATABLE_READ"
        }

        hikariDataSource = HikariDataSource(hikariConfig)
        Database.connect(hikariDataSource!!)

        transaction {
            SchemaUtils.create(Urls) // Create tables here
        }
    }

    fun close() {
        hikariDataSource?.close()
    }
}
