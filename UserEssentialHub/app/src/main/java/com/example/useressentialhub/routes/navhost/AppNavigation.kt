package com.example.useressentialhub.routes.navhost

import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.useressentialhub.routes.NavRoutes
import com.example.useressentialhub.ui.animations.NavAnimations
import com.example.useressentialhub.ui.screens.auth.OtpScreen
import com.example.useressentialhub.ui.screens.auth.SignInScreen
import com.example.useressentialhub.ui.screens.auth.SplashScreen
import androidx.navigation.compose.navigation
import androidx.navigation.navArgument
import com.example.useressentialhub.NotificationSetup
import com.example.useressentialhub.routes.UserRoutes
import com.example.useressentialhub.ui.components.CartBar
import com.example.useressentialhub.ui.screens.user.AddressScreen
import com.example.useressentialhub.ui.screens.user.CheckoutScreen
import com.example.useressentialhub.ui.screens.user.CategoryProductsScreen
import com.example.useressentialhub.ui.screens.user.HomeScreen
import com.example.useressentialhub.ui.screens.user.OrdersScreen
import com.example.useressentialhub.ui.screens.user.ProfileScreen
import com.example.useressentialhub.ui.screens.user.SearchScreen
import com.example.useressentialhub.viewmodels.CartViewModel
import com.example.useressentialhub.viewmodels.LocalCart

@Composable
fun AppNavigation() {
    val navController: NavHostController = rememberNavController()
    val cartViewModel: CartViewModel = viewModel()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showCartBar = currentRoute in setOf(UserRoutes.HOME, UserRoutes.SEARCH, UserRoutes.CATEGORY)
    val hasItems by remember { derivedStateOf { cartViewModel.totalItems > 0 } }

    CompositionLocalProvider(LocalCart provides cartViewModel) {
        Box(modifier = Modifier.fillMaxSize()){
            NavHost(
                navController = navController,
                startDestination = NavRoutes.SPLASH,
                modifier = Modifier.fillMaxSize()
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

                navigation(
                    route = UserRoutes.GRAPH,
                    startDestination = UserRoutes.HOME
                ) {
                    composable(UserRoutes.HOME) {
                        LaunchedEffect(Unit) {
                            cartViewModel.syncWithCurrentUser()

                        }
                        NotificationSetup()
                        HomeScreen(
                            onSearchClick = { navController.navigate(UserRoutes.SEARCH) },
                            onCategoryClick = { name -> navController.navigate(UserRoutes.category(name)) },
                            onProfileClick = { navController.navigate(UserRoutes.PROFILE) }
                        )
                    }
                    composable(UserRoutes.SEARCH) {
                        SearchScreen(navController = navController)
                    }
                    composable(
                        route = UserRoutes.CATEGORY,
                        arguments = listOf(navArgument("categoryName") { type = NavType.StringType })
                    ) { entry ->
                        CategoryProductsScreen(
                            navController = navController,
                            categoryName = Uri.decode(entry.arguments?.getString("categoryName") ?: "")
                        )
                    }

                    composable(UserRoutes.CART) {
                        CheckoutScreen(navController = navController)
                    }

                    composable(UserRoutes.PROFILE) { ProfileScreen(navController = navController) }
                    composable(UserRoutes.ORDERS) { OrdersScreen(navController = navController) }

                    composable(UserRoutes.ADDRESS) {
                        val ctx = LocalContext.current
                        var saving by remember { mutableStateOf(false) }
                        AddressScreen(
                            isSaving = saving,
                            onBack = { navController.popBackStack() },
                            onSave = { data ->
                                val text = data.toDisplayString()
                                saving = true
                                cartViewModel.saveAddress(text) { ok, msg ->
                                    saving = false
                                    if (ok) {
                                        if (navController.currentDestination?.route == UserRoutes.ADDRESS) {
                                            navController.popBackStack()
                                        }
                                    } else {
                                    Toast.makeText(ctx, msg, Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        )
                    }
                }
            }

            AnimatedVisibility(
                visible = showCartBar && hasItems,
                modifier = Modifier.align(Alignment.BottomCenter),
                enter = slideInVertically(tween(200)) { it } + fadeIn(tween(200)),
                exit = slideOutVertically(tween(200)) { it } + fadeOut(tween(200))
            ) {
                CartBar(
                    itemCount = cartViewModel.totalItems,
                    onViewCartClick = { },
                    onNextClick = { navController.navigate(UserRoutes.CART) }
                )
            }
        }
    }
}


