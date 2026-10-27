package com.example.useressentialhub.viewmodels

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.useressentialhub.models.Product
import com.google.firebase.database.FirebaseDatabase

class UserViewModel: ViewModel(){
    var allProducts = mutableStateOf<List<Product>>(emptyList())
        private set

    var isLoading = mutableStateOf(false)
        private set

    var hasError = mutableStateOf(false)
        private set

    var categoryProducts = mutableStateOf<List<Product>>(emptyList())
        private set

    // true se shuru: screen khulte hi "No product added" ek pal ke liye na chamke
    var isCategoryLoading = mutableStateOf(true)
        private set

    var hasCategoryError = mutableStateOf(false)
        private set


    fun loadAllProducts() {
        isLoading.value = true
        hasError.value = false

        FirebaseDatabase.getInstance().getReference("Admin/AllProducts")
            .get()
            .addOnSuccessListener { snapshot ->
                allProducts.value = snapshot.children
                    .mapNotNull { it.getValue(Product::class.java) }
                isLoading.value = false
            }
            .addOnFailureListener {
                hasError.value = true
                isLoading.value = false
            }
    }


    fun loadCategory(categoryName: String) {
        isCategoryLoading.value = true
        hasCategoryError.value = false

        FirebaseDatabase.getInstance()
            .getReference("Admin/ProductCategory/$categoryName")
            .get()
            .addOnSuccessListener { snapshot ->
                categoryProducts.value = snapshot.children
                    .mapNotNull { it.getValue(Product::class.java) }
                isCategoryLoading.value = false
            }
            .addOnFailureListener {
                hasCategoryError.value = true
                isCategoryLoading.value = false
            }
    }

}