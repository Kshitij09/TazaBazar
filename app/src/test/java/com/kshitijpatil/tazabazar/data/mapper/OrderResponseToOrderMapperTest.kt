package com.kshitijpatil.tazabazar.data.mapper

import com.google.common.truth.Truth.assertThat
import com.kshitijpatil.tazabazar.api.dto.OrderResponse
import com.kshitijpatil.tazabazar.data.OrderRepositoryImplTest
import com.kshitijpatil.tazabazar.data.local.TazaBazarTypeConverters
import com.kshitijpatil.tazabazar.di.MapperModule
import com.kshitijpatil.tazabazar.model.OrderStatus
import org.junit.Test
import org.threeten.bp.DateTimeException
import org.threeten.bp.LocalDateTime
import org.threeten.bp.OffsetDateTime
import org.threeten.bp.ZoneId

class OrderResponseToOrderMapperTest {
    private val orderMapper = MapperModule.orderMapper

    @Test
    fun map_happyPath() {
        val order = orderMapper.map(FakeData.validOrderResponse)
        assertThat(order).isNotNull()
        assertThat(order.createdAt).isEqualTo(FakeData.localDateTime1)
        assertThat(order.status).isEqualTo(OrderStatus.ACCEPTED)
    }

    @Test(expected = DateTimeException::class)
    fun map_malformedOffsetDateTime_throwsDateTimeException() {
        val testOrder = FakeData.validOrderResponse.copy(createdAt = "sjakgkb")
        orderMapper.map(testOrder)
    }

    @Test(expected = IllegalArgumentException::class)
    fun map_invalidStatusString_throwsIllegalArgumentException() {
        val testOrder = FakeData.validOrderResponse.copy(status = "random")
        orderMapper.map(testOrder)
    }

    object FakeData {
        private val nowOffsetDatetime = OffsetDateTime.now()
        private val offsetDateTime1Raw =
            TazaBazarTypeConverters.fromOffsetDateTime(nowOffsetDatetime)!!
        val localDateTime1: LocalDateTime =
            nowOffsetDatetime.atZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime()
        private const val validStatusString = "ACCEPTED"
        val validOrderResponse = OrderResponse(
            offsetDateTime1Raw,
            "order-01",
            emptyList(),
            validStatusString,
            OrderRepositoryImplTest.OrderSession.user.email
        )
    }
}