package com.kshitijpatil.tazabazar.model

import org.threeten.bp.LocalDateTime

data class OrderLine(
    val inventoryId: Int,
    val quantity: Int
)

enum class OrderStatus {
    ACCEPTED, PENDING, DISPATCHED, DELIVERED, CANCELLED
}

data class Order(
    val createdAt: LocalDateTime,
    val orderId: String,
    val orderLines: List<OrderLine>,
    val status: OrderStatus,
    val total: Float = 0f
)