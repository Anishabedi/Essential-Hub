package com.example.adminessentialhub.routes

object AdminRoutes {
    const val GRAPH = "admin_graph"
    const val HOME = "home"
    const val ADD_PRODUCT = "add_product"
    const val ORDERS = "orders"

    const val EDIT_PRODUCT = "edit_product/{productId}"
    fun editProduct(id: String) = "edit_product/$id"
}