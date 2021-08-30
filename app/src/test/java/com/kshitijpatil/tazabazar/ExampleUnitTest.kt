package com.kshitijpatil.tazabazar

import org.junit.Assert.assertEquals
import org.junit.Test
import org.threeten.bp.LocalDateTime
import org.threeten.bp.temporal.ChronoUnit

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {

    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun test_time_difference() {
        val now = LocalDateTime.now()
        val later = now.plusHours(2).plusMinutes(30)
        println("Minutes elapsed: ${ChronoUnit.MINUTES.between(now, later)}")
    }
}