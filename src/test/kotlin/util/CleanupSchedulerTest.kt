package util

import com.iponomarev.repository.UrlRepository
import com.iponomarev.util.CleanupScheduler
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class CleanupSchedulerTest {
    private lateinit var mockRepository: UrlRepository

    @BeforeEach
    fun setup() {
        mockRepository = mockk(relaxed = true)
        CleanupScheduler.stop()
    }

    @AfterEach
    fun tearDown() {
        CleanupScheduler.stop()
        clearAllMocks()
    }

    @Test
    fun `start should initialize scheduler without throwing`() {
        every { mockRepository.deleteExpiredUrls() } returns 5

        assertDoesNotThrow {
            CleanupScheduler.start(mockRepository, intervalHours = 24)
        }
    }

    @Test
    fun `start with custom interval should work`() {
        every { mockRepository.deleteExpiredUrls() } returns 0

        assertDoesNotThrow {
            CleanupScheduler.start(mockRepository, intervalHours = 6)
            CleanupScheduler.stop()
        }

        assertDoesNotThrow {
            CleanupScheduler.start(mockRepository, intervalHours = 48)
            CleanupScheduler.stop()
        }
    }

    @Test
    fun `stop should cancel timer and be idempotent`() {
        every { mockRepository.deleteExpiredUrls() } returns 0
        CleanupScheduler.start(mockRepository)

        assertDoesNotThrow {
            CleanupScheduler.stop()
            CleanupScheduler.stop()
            CleanupScheduler.stop()
        }
    }

    @Test
    fun `stop without prior start should not throw`() {
        assertDoesNotThrow {
            CleanupScheduler.stop()
        }
    }

    @Test
    fun `scheduler handles repository exceptions gracefully`() {
        every { mockRepository.deleteExpiredUrls() } throws RuntimeException("Database connection failed")

        assertDoesNotThrow {
            CleanupScheduler.start(mockRepository)
            Thread.sleep(100)
            CleanupScheduler.stop()
        }
    }

    @Test
    fun `multiple start calls should not throw`() {
        every { mockRepository.deleteExpiredUrls() } returns 0

        assertDoesNotThrow {
            CleanupScheduler.start(mockRepository, intervalHours = 1)
            CleanupScheduler.start(mockRepository, intervalHours = 2)
            CleanupScheduler.stop()
        }
    }

    @Test
    fun `start and stop cycle should work multiple times`() {
        every { mockRepository.deleteExpiredUrls() } returns 3

        repeat(3) {
            assertDoesNotThrow {
                CleanupScheduler.start(mockRepository, intervalHours = 12)
                Thread.sleep(50)
                CleanupScheduler.stop()
            }
        }
    }

    @Test
    fun `scheduler should work with very large interval`() {
        every { mockRepository.deleteExpiredUrls() } returns 0

        assertDoesNotThrow {
            CleanupScheduler.start(mockRepository, intervalHours = 168)
            CleanupScheduler.stop()
        }
    }
}