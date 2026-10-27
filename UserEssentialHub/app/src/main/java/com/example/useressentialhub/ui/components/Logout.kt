package com.example.useressentialhub.ui.components

import androidx.navigation.NavHostController
import com.example.useressentialhub.FcmTokenManager
import com.example.useressentialhub.routes.NavRoutes
import com.example.useressentialhub.viewmodels.CartViewModel
import com.google.firebase.auth.FirebaseAuth

/**
 * Logout: notification token hatao (signOut se PEHLE, warna uid nahi milega),
 * Firebase se sign out, cart + address memory se saaf, aur Sign In screen par
 * (poora back stack hata ke, taaki back dabane se wapas Home na aaye).
 *
 * Profile screen mein use:
 *   val cart = LocalCart.current
 *   Button(onClick = { logoutUser(navController, cart) }) { Text("Logout") }
 */
fun logoutUser(navController: NavHostController, cart: CartViewModel) {
    FcmTokenManager.removeCurrentToken {
        FirebaseAuth.getInstance().signOut()
        cart.syncWithCurrentUser()   // user null ho gaya, to ye cart aur address saaf kar deta hai
        navController.navigate(NavRoutes.SIGN_IN) {
            popUpTo(navController.graph.id) { inclusive = true }
        }
    }
}




// val cart = LocalCart.current
//Button(onClick = { logoutUser(navController, cart) }) { Text("Logout") }