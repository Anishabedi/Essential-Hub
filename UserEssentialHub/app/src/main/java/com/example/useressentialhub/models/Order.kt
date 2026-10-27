package com.example.useressentialhub.models

// placeOrder() jo Firebase mein likhta hai (Orders/ByUser/<uid>/<orderId>) uski keys se bilkul match
data class OrderItem(
    val productId: String = "",
    val title: String? = null,
    val price: Double? = null,
    val quantity: Int = 0,
    val unit: String? = null,
    val productQuantity: Double? = null,
    val image: String? = null,
    val adminUid: String = ""
)

data class Order(
    val orderId: String = "",
    val userUid: String = "",
    val userAddress: String = "",
    val userPhoneNumber: String = "",
    val timestamp: Long = 0L,
    val subTotal: Double = 0.0,
    val deliveryCharge: Double = 0.0,
    val grandTotal: Double = 0.0,
    val items: List<OrderItem> = emptyList(),
    // adminUid -> status. Ek order mein kai admins ke products ho sakte hain.
    val adminStatus: Map<String, String> = emptyMap()
) {
    // Function hai (getter nahi), taaki Firebase ise field na samjhe
    fun overallStatus(): String {
        val s = adminStatus.values.distinct()
        return when {
            s.isEmpty() -> "Placed"
            s.size == 1 -> s.first()
            else -> "In progress"
        }
    }
}