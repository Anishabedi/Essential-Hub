package com.example.useressentialhub.data

import com.example.useressentialhub.R
import com.example.useressentialhub.models.Category
import com.example.useressentialhub.models.CategoryGroup

object Categories {
    val groups = listOf(
        CategoryGroup(
            "Grocery & Kitchen",
            listOf(
                Category("Vegetables & Fruits", R.drawable.vegetables_fruits),
                Category("Atta Rice & Dal", R.drawable.atta_rice_dal),
                Category("Ghee, Masala & Oil", R.drawable.ghee_masala_oil),
                Category("Dairy & Breakfast", R.drawable.dairy_breakfast),
                Category("Bakery & Biscuits", R.drawable.bakery_biscuits),
                Category("Dry Fruits and Cereals", R.drawable.dry_fruits_cereals),
                Category("Chicken Meat & Fish", R.drawable.chicken_meat_fish),
                Category("Kitchenware & Appliances", R.drawable.kitchenware_appliances)
            )
        ),
        CategoryGroup(
            "Snacks & Drinks",
            listOf(
                Category("Chips & Namkeen", R.drawable.chips_namkeen),
                Category("Sweets & Chocolates", R.drawable.sweet_chocolates),
                Category("Drinks & Juices", R.drawable.cold_drinks_juices),
                Category("Tea, Coffee & Milk Drinks", R.drawable.tea_coffee_health_drinks),
                Category("Instant & Frozen Food", R.drawable.instant_frozen_food),
                Category("Sauces & Spreads", R.drawable.sauces_spreads),
                Category("Paan Corner", R.drawable.paan_corner),
                Category("Ice Creams", R.drawable.icecream),
            )
        ),
        CategoryGroup(
            "Beauty & Personal Care",
            listOf(
                Category("Bath & Body", R.drawable.bath_body),
                Category("Hair", R.drawable.hair),
                Category("Skin & Face", R.drawable.skin_face),
                Category("Beauty and Cosmetics", R.drawable.beauty_cosmetics),
                Category("Feminine Hygiene", R.drawable.hygiene),
                Category("Baby Care", R.drawable.baby_care),
                Category("Pharma & Health", R.drawable.pharma_health),
                Category("Sexual Wellness", R.drawable.wellness),
            )
        ),
        CategoryGroup(
            "Household & Lifestyle",
            listOf(
                Category("Cleaning Essentials", R.drawable.cleaning_essential),
                Category("Home & Office", R.drawable.home_office),
                Category("Electronics", R.drawable.electronics),
                Category("Stationery", R.drawable.stationary_games)
            )
        ),
    )
}