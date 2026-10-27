package com.example.adminessentialhub.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.adminessentialhub.models.BottomItem
import com.example.adminessentialhub.routes.AdminRoutes
import com.example.adminessentialhub.ui.animations.NavAnimations
import com.example.adminessentialhub.NotificationSetup

val bottomItems = listOf(
    BottomItem(AdminRoutes.HOME, "Home", Icons.Default.Home),
    BottomItem(AdminRoutes.ADD_PRODUCT, "Add Product", Icons.Default.Add),
    BottomItem(AdminRoutes.ORDERS, "Orders", Icons.Default.ShoppingCart),
)

@Composable
fun AdminMainScreen() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    Scaffold(
        topBar = {
            Box(
                Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF001F54))
                    .statusBarsPadding()
                    .height(0.dp)
            )
        },
        bottomBar = {
            if(currentRoute != AdminRoutes.EDIT_PRODUCT){
                NavigationBar(containerColor = Color(0xFF001F54)) {
                    bottomItems.forEach { item ->
                        NavigationBarItem(
                            selected = currentRoute == item.route,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(AdminRoutes.HOME) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(item.icon, contentDescription = item.label) },
                            label = { Text(item.label) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color.White,
                                selectedTextColor = Color.White,
                                unselectedIconColor = Color.White.copy(alpha = 0.6f),
                                unselectedTextColor = Color.White.copy(alpha = 0.6f),
                                indicatorColor = Color(0xFF3A5BA0)
                            )
                        )
                    }
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = AdminRoutes.HOME,
            modifier = Modifier.padding(padding)
        ) {
            composable(
                route = AdminRoutes.HOME,
                enterTransition = NavAnimations.fadeInTab,
                exitTransition = NavAnimations.fadeOutTab
            ) {
                NotificationSetup()
                AdminHomeScreen(navController = navController)
            }
            composable(
                route = AdminRoutes.ADD_PRODUCT,
                enterTransition = NavAnimations.fadeInTab,
                exitTransition = NavAnimations.fadeOutTab
            ) {
                AddProductScreen(navController = navController)
            }
            composable(
                route = AdminRoutes.ORDERS,
                enterTransition = NavAnimations.fadeInTab,
                exitTransition = NavAnimations.fadeOutTab
            ) {
                OrdersScreen()
            }
            composable(
                route = AdminRoutes.EDIT_PRODUCT,
                enterTransition = NavAnimations.fadeInTab,
                exitTransition = NavAnimations.fadeOutTab
            ) { entry ->
                AddProductScreen(
                    navController = navController,
                    productId = entry.arguments?.getString("productId")
                )
            }

        }
    }
}