package com.example.useressentialhub.data.local

import androidx.room.Entity
import com.example.useressentialhub.models.Product

@Entity(tableName = "cart_items", primaryKeys = ["userUid", "productId"])
data class CartEntity(
    val userUid: String,
    val productId: String,
    val title: String?,
    val price: Double?,
    val productQuantity: Double?,   // jaise "70 gm" ka 70
    val unit: String?,
    val stock: Int,
    val imageUrl: String?,          // cart mein sirf pehli image chahiye
    val category: String?,
    val cartQuantity: Int,           // cart mein kitne piece
    val adminUid: String?
)

fun Product.toCartEntity(quantity: Int, userUid: String) = CartEntity(
    userUid = userUid,
    productId = productRandomId,
    title = productTitle,
    price = productPrice,
    productQuantity = productQuantity,
    unit = productUnit,
    stock = productStock ?: 0,
    imageUrl = productImageUris?.firstOrNull { it != null },
    category = productCategory,
    cartQuantity = quantity,
    adminUid = adminUid
)

fun CartEntity.toProduct() = Product(
    productRandomId = productId,
    productTitle = title,
    productQuantity = productQuantity,
    productUnit = unit,
    productPrice = price,
    productStock = stock,
    productCategory = category,
    productImageUris = imageUrl?.let { arrayListOf<String?>(it) },
    adminUid = adminUid
)

