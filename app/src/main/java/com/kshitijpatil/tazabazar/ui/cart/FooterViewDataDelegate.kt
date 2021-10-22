package com.kshitijpatil.tazabazar.ui.cart

interface FooterViewDataDelegate {
    var footerViewData: FooterViewData

    fun updateSubTotal(subTotal: Float) {
        val newCosting = footerViewData.costing.copy(subTotal = subTotal)
        val value = footerViewData.copy(costing = newCosting)
        setFooterViewDataIfChanged(value)
    }

    fun updateDeliveryCharges(deliveryCharges: Float) {
        val newCosting = footerViewData.costing.copy(delivery = deliveryCharges)
        val value = footerViewData.copy(costing = newCosting)
        setFooterViewDataIfChanged(value)
    }

    fun updateDiscount(discount: Float) {
        val newCosting = footerViewData.costing.copy(discount = discount)
        val value = footerViewData.copy(costing = newCosting)
        setFooterViewDataIfChanged(value)
    }

    fun setPlaceOrderEnabled(enabled: Boolean) {
        val value = footerViewData.copy(placeOrderEnabled = enabled)
        setFooterViewDataIfChanged(value)
    }

    fun setPlaceOrderVisible(isVisible: Boolean) {
        val value = footerViewData.copy(placeOrderVisible = isVisible)
        setFooterViewDataIfChanged(value)
    }

    private fun setFooterViewDataIfChanged(value: FooterViewData) {
        if (value != footerViewData) {
            onDataChanged()
            footerViewData = value
        }
    }

    fun onDataChanged() {}
}