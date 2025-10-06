package com.iponomarev.repository

import com.iponomarev.repository.table.Urls
import com.iponomarev.util.getEnvOrConfig
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
            jdbcUrl = getEnvOrConfig("db.url", "DB_URL", config)
            driverClassName = getEnvOrConfig("db.driver", "DB_DRIVER", config)
            username = getEnvOrConfig("db.user", "DB_USER", config)
            password = getEnvOrConfig("db.password", "DB_PASSWORD", config)
            maximumPoolSize = (System.getenv("DB_MAXIMUM_POOL_SIZE") ?: config.property("db.maximumPoolSize").getString()).toInt()
            isAutoCommit = false
            transactionIsolation = "TRANSACTION_REPEATABLE_READ"
        }

        hikariDataSource = HikariDataSource(hikariConfig)
        Database.connect(hikariDataSource!!)

        transaction {
            SchemaUtils.create(Urls)
        }
    }

    fun close() {
        hikariDataSource?.close()
    }
}
