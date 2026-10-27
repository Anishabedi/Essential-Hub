package com.example.adminessentialhub.routes.navhost

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.adminessentialhub.ui.animations.NavAnimations
import com.example.adminessentialhub.ui.screens.auth.OtpScreen
import com.example.adminessentialhub.ui.screens.auth.SignInScreen
import com.example.adminessentialhub.routes.AdminRoutes
import com.example.adminessentialhub.ui.screens.admin.AdminMainScreen
import com.example.adminessentialhub.ui.screens.auth.SplashScreen

@Composable
fun AppNavigation() {
    val navController: NavHostController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = NavRoutes.SPLASH
    ) {
        composable(NavRoutes.SPLASH,exitTransition = NavAnimations.exitToLeft) {
            SplashScreen(navController = navController)
        }
        composable(
            route = NavRoutes.SIGN_IN,
            enterTransition = NavAnimations.enterFromRight,
            exitTransition = NavAnimations.exitToLeft,
            popEnterTransition = NavAnimations.enterFromLeft,
            popExitTransition = NavAnimations.exitToRight
        ) {
            SignInScreen(navController = navController)
        }

        composable(
            route = NavRoutes.OTP,
            enterTransition = NavAnimations.enterFromRight,
            exitTransition = NavAnimations.exitToLeft,
            popEnterTransition = NavAnimations.enterFromLeft,
            popExitTransition = NavAnimations.exitToRight
        ) {
            OtpScreen(navController = navController)
        }

        composable(AdminRoutes.GRAPH) {
            AdminMainScreen()
        }


    }
}

