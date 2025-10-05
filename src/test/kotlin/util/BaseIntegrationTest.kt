package util

import com.iponomarev.repository.table.Urls
import com.iponomarev.module
import com.iponomarev.repository.UrlRepository
import com.iponomarev.service.UrlDatabaseService
import com.typesafe.config.ConfigFactory
import io.ktor.server.config.ApplicationConfig
import io.ktor.server.config.HoconApplicationConfig
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class BaseIntegrationTest {

    protected lateinit var urlDatabaseService: UrlRepository
    private lateinit var testConfig: ApplicationConfig

    @BeforeAll
    fun setupDatabase() {
        testConfig = HoconApplicationConfig(ConfigFactory.load("application-test.conf"))
        val user = testConfig.property("db.user").getString()
        val password = testConfig.property("db.password").getString()
        val driver = testConfig.property("db.driver").getString()
        val url = testConfig.property("db.url").getString()

        Database.connect(
            url = url,
            driver = driver,
            user = user,
            password = password
        )
        transaction {
            SchemaUtils.create(Urls)
        }

        urlDatabaseService = UrlDatabaseService()
    }

    fun testing(block: suspend ApplicationTestBuilder.() -> Unit) = testApplication {
        environment {
            config = testConfig
        }
        application {
            module()
        }
        block()
    }
}

