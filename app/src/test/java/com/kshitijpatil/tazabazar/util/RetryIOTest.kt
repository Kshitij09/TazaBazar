package com.kshitijpatil.tazabazar.util

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Test
import java.util.concurrent.atomic.AtomicInteger

class RetryIOTest {
    @Test
    fun retryIO_worksAtFirstAttempt() {
        val retriesCounter = AtomicInteger(0)
        val data = "some-i/o-result"
        runBlocking {
            val result = retryIO {
                retriesCounter.getAndIncrement()
                data
            }
            assertThat(result).isEqualTo(data)
            assertThat(retriesCounter.get()).isEqualTo(1)
        }
    }

    @Test
    fun retryIO_worksAfterNAttempts() {
        val retriesCounter = AtomicInteger(0)
        val data = "some-i/o-result"
        runBlockingTest {
            val result = retryIO {
                val currentCount = retriesCounter.getAndIncrement()
                if (currentCount < 5) error("Nope")
                data
            }
            assertThat(result).isEqualTo(data)
            val expectedAttempts = 5 + 1 // failed + succeeded
            assertThat(retriesCounter.get()).isEqualTo(expectedAttempts)
        }
    }

    @Test
    fun retryIO_whenMaxAttemptsExceeded_failsWithError() {
        val retriesCounter = AtomicInteger(0)
        runBlockingTest {
            val result = runCatching {
                retryIO(times = 5) {
                    val currentCount = retriesCounter.getAndIncrement()
                    if (currentCount < 10) error("Nope")
                    1
                }
            }
            assertThat(result.isFailure).isTrue()
            assertThat(retriesCounter.get()).isEqualTo(5)
        }
    }
}