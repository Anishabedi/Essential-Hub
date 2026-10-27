package com.example.useressentialhub.ui.screens.user

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.navigation.NavHostController
import com.example.useressentialhub.routes.UserRoutes
import com.example.useressentialhub.ui.components.logoutUser
import com.example.useressentialhub.viewmodels.LocalCart
import com.google.firebase.auth.FirebaseAuth

private val HeaderNavy = Color(0xFF001F54)

@Composable
fun ProfileScreen(navController: NavHostController) {
    val cart = LocalCart.current
    var showLogoutDialog by remember { mutableStateOf(false) }

    // Login wala number, +91 hata ke (image mein 10 digit dikhta hai)
    val phone = FirebaseAuth.getInstance().currentUser?.phoneNumber.orEmpty().removePrefix("+91")

    LightStatusBarIcons()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        AccountHeader(title = "Profile", onBack = { navController.popBackStack() })

        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)) {
            Text(
                text = "My account",
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = phone,
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 16.sp
            )

            Spacer(Modifier.height(24.dp))

            Text(
                text = "YOUR INFORMATION",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 13.sp
            )
            Spacer(Modifier.height(4.dp))

            ProfileRow(
                icon = Icons.Default.ShoppingCart,
                label = "Your Orders",
                onClick = { navController.navigate(UserRoutes.ORDERS) }
            )
            ProfileRow(
                icon = Icons.Default.LocationOn,
                label = "Address Book",
                onClick = { navController.navigate(UserRoutes.ADDRESS) }
            )
            ProfileRow(
                icon = Icons.AutoMirrored.Filled.ExitToApp,
                label = "Log Out",
                onClick = { showLogoutDialog = true }
            )
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Log out?") },
            text = { Text("Kya aap is device se logout karna chahte hain?") },
            confirmButton = {
                TextButton(onClick = {
                    showLogoutDialog = false
                    logoutUser(navController, cart)
                }) { Text("Log Out") }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun ProfileRow(icon: ImageVector, label: String, onClick: () -> Unit) {
    val color = MaterialTheme.colorScheme.onBackground
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(16.dp))
        Text(
            text = label,
            color = color,
            fontSize = 15.sp,
            modifier = Modifier.weight(1f)
        )
        Icon(
            Icons.Default.KeyboardArrowRight,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(22.dp)
        )
    }
}

// Profile aur Orders dono screens ka navy header
@Composable
internal fun AccountHeader(title: String, onBack: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().background(HeaderNavy)) {
        Row(
            modifier = Modifier
                .statusBarsPadding()
                .padding(start = 8.dp, end = 16.dp, top = 4.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack, modifier = Modifier.size(40.dp)) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Text(
                text = title,
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}

// Status bar ke icons navy header ke upar safed dikhein; screen band hone par purani setting wapas
@Composable
internal fun LightStatusBarIcons() {
    val view = LocalView.current
    DisposableEffect(Unit) {
        val window = (view.context as? Activity)?.window
        if (window == null) {
            onDispose { }
        } else {
            val controller = WindowCompat.getInsetsController(window, view)
            val oldLight = controller.isAppearanceLightStatusBars
            controller.isAppearanceLightStatusBars = false
            onDispose { controller.isAppearanceLightStatusBars = oldLight }
        }
    }
}