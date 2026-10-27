package com.example.adminessentialhub.models

// Order ke andar ka ek product
data class OrderItem(
    val productId: String = "",
    val adminUid: String = "",
    val title: String? = null,
    val price: Double? = null,
    val quantity: Int = 0,
    val unit: String? = null,
    val productQuantity: Double? = null,
    val image: String? = null
)

// Firebase ke Orders/ByAdmin/<adminUid>/<orderId> ka data.
// Field ke naam wahi hain jo user app ke placeOrder() mein likhe gaye hain.
data class Order(
    val orderId: String = "",
    val userUid: String = "",
    val adminUid: String = "",
    val userAddress: String = "",
    val userPhoneNumber: String = "",
    val status: String = "Placed",
    val timestamp: Long = 0L,
    val totalPrice: Double = 0.0,
    val items: List<OrderItem> = emptyList()
)