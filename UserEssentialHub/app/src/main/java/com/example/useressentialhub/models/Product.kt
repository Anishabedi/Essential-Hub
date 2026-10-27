package com.example.useressentialhub.models

data class Product(
    var productRandomId: String = "",
    var productTitle: String? = null,
    var productQuantity: Double? = null,
    var productUnit: String? = null,
    var productCategory: String? = null,
    var productPrice: Double? = null,
    var productStock: Int? = null,
    var productImageUris: ArrayList<String?>? = null,
    var adminUid: String? = null,
    var productType:String? = null,
    var itemCount: Int? = null
)

