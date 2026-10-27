package com.example.useressentialhub.routes

import android.net.Uri

object UserRoutes {
    const val GRAPH = "users_graph"
    const val HOME = "home"
    const val SEARCH = "search"

    const val ADDRESS = "address"

    const val CART = "cart"

    const val CATEGORY = "category/{categoryName}"
    fun category(name: String) = "category/${Uri.encode(name)}"

    const val PROFILE = "profile"
    const val ORDERS = "orders"
}