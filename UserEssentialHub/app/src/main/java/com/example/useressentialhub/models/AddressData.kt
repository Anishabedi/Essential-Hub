package com.example.useressentialhub.models

data class AddressData(
    val pinCode: String = "",
    val phone: String = "",
    val state: String = "",
    val district: String = "",
    val fullAddress: String = ""
) {
    // Baaki app (CartViewModel, admin) address ko String maanta hai, isliye ek line bana do
    fun toDisplayString() = "$fullAddress, $district, $state - $pinCode | Ph: $phone"
}