package com.example.useressentialhub.models

import androidx.annotation.DrawableRes


data class CategoryGroup(
    val title: String,
    val items: List<Category>
)
data class Category(
    val name: String,
    @DrawableRes val imageRes: Int
)
