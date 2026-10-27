package com.example.useressentialhub.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.useressentialhub.data.ItemImages
import com.example.useressentialhub.routes.NavRoutes
import com.example.useressentialhub.routes.UserRoutes
import com.example.useressentialhub.ui.components.ScatteredBackground
import com.example.useressentialhub.viewmodel.AuthViewModel
import androidx.activity.ComponentActivity
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(navController: NavHostController) {
    val context = LocalContext.current
    val authViewModel: AuthViewModel = viewModel(viewModelStoreOwner = context as ComponentActivity)

    LaunchedEffect(Unit) {
        delay(2000)
        if (authViewModel.isACurrentUser.value) {
            navController.navigate(UserRoutes.GRAPH) {
                popUpTo(NavRoutes.SPLASH) { inclusive = true }
            }
        } else {
            navController.navigate(NavRoutes.SIGN_IN) {
                popUpTo(NavRoutes.SPLASH) { inclusive = true }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF001F54))   // Navy background
    ) {

        // ---------- Background scattered images animation ----------
        ScatteredBackground(images = ItemImages.list)

        // ---------- Center overlay: App name (semi-transparent card ya direct text) ----------
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF001F54).copy(alpha = 0.75f)),  // Navy overlay - images ke upar halka dark tint
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Essential Hub",
                    fontSize = 45.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Text(
                    text = "everyone's need",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color.White.copy(alpha = 0.85f)
                )
            }
        }
    }
}