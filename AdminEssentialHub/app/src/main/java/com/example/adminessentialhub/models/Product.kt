package com.example.adminessentialhub.models

import java.util.UUID

data class Product(
    var productRandomId: String = UUID.randomUUID().toString(),
    var productTitle: String? = null,
    var productQuantity: Double? = null,
    var productUnit: String? = null,
    var productPrice: Double? = null,
    var productStock: Int? = null,
    var productCategory: String? = null,
    var productType: String? = null,
    var itemCount: Int? = null,
    var adminUid: String? = null,
    var productImageUris: ArrayList<String?>? = null
)

