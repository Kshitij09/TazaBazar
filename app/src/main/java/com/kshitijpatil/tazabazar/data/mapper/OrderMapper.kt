package com.kshitijpatil.tazabazar.data.mapper

import com.kshitijpatil.tazabazar.api.dto.OrderLineDto
import com.kshitijpatil.tazabazar.api.dto.OrderResponse
import com.kshitijpatil.tazabazar.data.local.TazaBazarTypeConverters
import com.kshitijpatil.tazabazar.model.Order
import com.kshitijpatil.tazabazar.model.OrderLine
import com.kshitijpatil.tazabazar.model.OrderStatus
import org.threeten.bp.DateTimeException
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneId

class OrderResponseToOrderMapper(private val orderLineMapper: OrderLineDtoToOrderLine) :
    Mapper<OrderResponse, Order> {

    @Throws(DateTimeException::class, IllegalArgumentException::class)
    override fun map(from: OrderResponse): Order {
        val orderLocalDateTime = getLocalDateTimeFrom(from.createdAt)
        val orderStatus = OrderStatus.valueOf(from.status)
        val orderLines = from.orderLines.map(orderLineMapper::map)
        return Order(orderLocalDateTime, from.id, orderLines, orderStatus)
    }

    /**
     * reference: [https://stackoverflow.com/a/67919362/6738702]
     * @return [LocalDateTime] instance parsed from the given ISO formatted OffsetDateTime
     * @throws DateTimeException Failure in parsing and converting ISO formatted OffsetDateTime to [LocalDateTime]
     */
    private fun getLocalDateTimeFrom(offsetDateTimeRaw: String): LocalDateTime {
        val orderOffsetDateTime = TazaBazarTypeConverters.toOffsetDateTime(offsetDateTimeRaw)
        val orderZoned = orderOffsetDateTime.atZoneSameInstant(ZoneId.systemDefault())
        return orderZoned.toLocalDateTime()
    }
}

class OrderLineDtoToOrderLine : Mapper<OrderLineDto, OrderLine> {
    override fun map(from: OrderLineDto): OrderLine {
        return OrderLine(
            inventoryId = from.inventoryId,
            quantity = from.quantity
        )
    }

}