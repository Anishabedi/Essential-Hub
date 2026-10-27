package com.example.useressentialhub.ui.screens.user

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.useressentialhub.viewmodels.UserViewModel

private val HeaderNavy = Color(0xFF001F54)

@Composable
fun CategoryProductsScreen(
    navController: NavHostController,
    categoryName: String
) {
    val viewModel: UserViewModel = viewModel()

    // categoryName badalne pe hi dobara load hoga, recomposition pe nahi
    LaunchedEffect(categoryName) { viewModel.loadCategory(categoryName) }

    // Status bar ke icons navy patti ke upar safed dikhein
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

    val products = viewModel.categoryProducts.value

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {

        // ---------- Status bar wali patti: gehra navy ----------
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(HeaderNavy)
                .statusBarsPadding()
        )

        // ---------- Title patti ----------
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(HeaderNavy)
                .padding(start = 8.dp, end = 16.dp, top = 4.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Text(
                text = categoryName,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // ---------- Loading / error / khaali / grid ----------
        when {
            viewModel.isCategoryLoading.value -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }

            viewModel.hasCategoryError.value ->
                CategoryCenterMessage("Could not load products. Check your internet.")

            products.isEmpty() ->
                CategoryCenterMessage("No product added in this category")

            else -> {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp),
                    contentPadding = PaddingValues(top = 12.dp, bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(products) { product ->
                        // Yeh card SearchScreen.kt mein bana hua hai (same package)
                        UserProductCard(
                            product = product
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryCenterMessage(text: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
    }
}