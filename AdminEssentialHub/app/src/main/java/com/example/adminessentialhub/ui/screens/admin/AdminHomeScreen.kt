package com.example.adminessentialhub.ui.screens.admin

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.adminessentialhub.data.Constant
import com.example.adminessentialhub.routes.AdminRoutes
import com.example.adminessentialhub.ui.components.ProductItem
import com.example.adminessentialhub.viewmodel.ProductViewModel

// "All" chip ka naam Constant.allProductsCategory mein bilkul yahi hona chahiye
private const val ALL = "All"

@Composable
fun AdminHomeScreen(navController: NavHostController) {
    var searchText by remember { mutableStateOf("") }

    // Default "All": screen khulte hi sabhi products random order mein
    var selectedCategory by remember { mutableStateOf(ALL) }

    val productViewModel: ProductViewModel = viewModel()

    // Sirf tab chalega jab selectedCategory badlega, recomposition pe nahi
    LaunchedEffect(selectedCategory) {
        if (selectedCategory == ALL) productViewModel.loadAllProducts()
        else productViewModel.loadCategory(selectedCategory)
    }

    val query = searchText.trim()
    val isSearching = query.isNotEmpty()

    // Search shuru hote hi sabhi categories ke sabhi products ek baar laao
    LaunchedEffect(isSearching) {
        if (isSearching) productViewModel.loadSearchPool()
    }

    // Search chal rahi ho toh category ignore: poore store mein dhundho
    val sourceProducts = when {
        isSearching -> productViewModel.searchPool.value
        selectedCategory == ALL -> productViewModel.allProducts.value
        else -> productViewModel.categoryProducts.value
    }

    val showLoading =
        if (isSearching) productViewModel.isSearchLoading.value
        else productViewModel.isListLoading.value

    // ---------- Search filter ----------
    val filteredProducts = sourceProducts.filter {
        query.isEmpty() ||
                it.productTitle?.contains(query, ignoreCase = true) == true ||
                it.productCategory?.contains(query, ignoreCase = true) == true
    }

    Column(modifier = Modifier.fillMaxSize().background(Color.White)) {

        // ---------- Header ----------
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF001F54))
                .padding(16.dp)
        ) {
            Text(
                text = "Home",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // ---------- Search bar ----------
        OutlinedTextField(
            value = searchText,
            onValueChange = { searchText = it },
            placeholder = { Text("Search") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        // ---------- Categories (horizontal) ----------
        LazyRow(
            modifier = Modifier.padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(Constant.allProductsCategory) { category ->
                val isSelected = category.name == selectedCategory

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clickable {
                            // Selected chip dobara dabane pe "All" pe wapas
                            selectedCategory = if (isSelected) ALL else category.name
                        }
                        .padding(4.dp)
                ) {
                    Image(
                        painter = painterResource(id = category.imageRes),
                        contentDescription = category.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .border(
                                2.dp,
                                if (isSelected) Color(0xFF001F54) else Color.Transparent,
                                RoundedCornerShape(12.dp)
                            )
                            .background(Color(0xFFE8EAF6))
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = category.name,
                        fontSize = 11.sp,
                        color = if (isSelected) Color(0xFF001F54) else Color.Black,
                        maxLines = 2,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.width(60.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // ---------- Products area: loading / empty message / grid ----------
        when {
            // 1. Firebase ka jawab aane tak spinner (warna "No product" galat chamkega)
            showLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFF001F54))
                }
            }

            // 2. Koi product nahi mila
            filteredProducts.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = when {
                            query.isNotEmpty() -> "No products found for \"$query\""
                            selectedCategory == ALL -> "No products added yet"
                            else -> "No product added in this category"
                        },
                        fontSize = 15.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // 3. Products grid
            else -> {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // key: shuffle ya update hone pe har card apni jagah pehchana jata hai
                    items(filteredProducts, key = { it.productRandomId }) { product ->
                        ProductItem(
                            product = product,
                            onEditClick = {
                                navController.navigate(AdminRoutes.editProduct(product.productRandomId))
                            }
                        )
                    }
                }
            }
        }
    }
}