package com.kshitijpatil.tazabazar.model

data class ProductCategory(
    /** Unique identifier */
    val label: String,
    /** Localized name for the category */
    val name: String,
    /** [Product] sku is derived from this prefix */
    val skuPrefix: String
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ProductCategory

        if (label != other.label) return false

        return true
    }

    override fun hashCode(): Int {
        return label.hashCode()
    }
}
