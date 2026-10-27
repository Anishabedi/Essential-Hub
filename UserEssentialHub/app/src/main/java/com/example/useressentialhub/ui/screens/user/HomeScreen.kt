package com.example.useressentialhub.ui.screens.user

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.useressentialhub.data.Categories
import com.example.useressentialhub.models.Product
import com.example.useressentialhub.ui.components.CartButton
import com.example.useressentialhub.ui.components.CategoryItem
import com.example.useressentialhub.viewmodels.UserViewModel

@Composable
fun HomeScreen(
    onSearchClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onCategoryClick: (String) -> Unit = {}
) {
    val viewModel: UserViewModel = viewModel()
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

    // Screen khulte hi Firebase se products laao
    LaunchedEffect(Unit) { viewModel.loadAllProducts() }

    // Dummy list ki jagah asli products. shuffled() remember ke andar hai,
    // taaki recomposition pe order baar-baar na badle.
    val allProducts = viewModel.allProducts.value
    val featuredProducts = remember(allProducts) { allProducts.shuffled().take(10) }

    val listState = rememberLazyListState()
    val maxHeader = 230.dp
    val minHeader = 90.dp
    val collapsePx = with(LocalDensity.current) { (maxHeader - minHeader).toPx() }

    // 0f = header poora khula, 1f = poora collapse
    val fraction by remember {
        derivedStateOf {
            if (listState.firstVisibleItemIndex > 0) 1f
            else (listState.firstVisibleItemScrollOffset / collapsePx).coerceIn(0f, 1f)
        }
    }
    val headerHeight = lerp(maxHeader, minHeader, fraction)

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(bottom = 12.dp)) {
        LazyColumn(state = listState, modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            item { Spacer(Modifier.height(maxHeader)) }

            // ---- Bestsellers (abhi asli products mein se random 10) ----
            if (featuredProducts.isNotEmpty()) {
                item {
                    Text(
                        "Bestsellers",
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
                    )
                }
                item {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(featuredProducts) { product -> ProductCard(product) }
                    }
                }
            }

            // ---- Shop by Category ----
            items(Categories.groups) { group ->
                Column {
                    Text(
                        group.title,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 16.dp, top = 20.dp, bottom = 8.dp)
                    )
                    group.items.chunked(4).forEach { rowItems ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp)
                        ) {
                            rowItems.forEach { category ->
                                CategoryItem(
                                    category = category,
                                    modifier = Modifier.weight(1f),
                                    onClick = { onCategoryClick(category.name) }
                                )
                            }
                            repeat(4 - rowItems.size) { Spacer(Modifier.weight(1f)) }
                        }
                    }
                }
            }
            item { Spacer(Modifier.height(100.dp)) }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(headerHeight)
                .background(
                    Color(0xFF001F54),
                    RoundedCornerShape(bottomStart = 39.dp, bottomEnd = 39.dp)
                )
        ) {
            Column(
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                val iconSize = lerp(40.dp, 0.dp, fraction)
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "Profile",
                    tint = Color.White,
                    modifier = Modifier
                        .size(iconSize)
                        .alpha(1f - fraction)
                        .align(Alignment.End)
                        .clickable(enabled = fraction < 0.5f) { onProfileClick() }
                )

                Spacer(Modifier.height(lerp(12.dp, 0.dp, fraction)))

                // Yeh sirf dikhawati bar hai: tap karne pe Search screen khulti hai
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .clickable { onSearchClick() },
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray)
                        Spacer(Modifier.width(10.dp))
                        Text("Search", color = Color.Gray, fontSize = 15.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun ProductCard(product: Product) {
    // Abhi count sirf is card ke andar hai (cart banne tak). Scroll karke wapas aane pe 0 ho sakta hai.

    val image = product.productImageUris?.firstOrNull { it != null }

    Card(
        modifier = Modifier.width(140.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.onPrimary
        ),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Box(
                modifier = Modifier
                    .height(90.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
            ) {
                if (image != null) {
                    AsyncImage(
                        model = image,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            Spacer(Modifier.height(6.dp))

            Text(
                text = product.productTitle ?: "",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "${product.productQuantity.toCleanString()} ${product.productUnit ?: ""}",
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                fontSize = 12.sp
            )
            Text(
                text = "₹${product.productPrice.toCleanString().ifEmpty { "0" }}",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )

            Spacer(Modifier.height(6.dp))

            CartButton(product, Modifier.fillMaxWidth())
        }
    }
}

// 205.0 ki jagah 205, 12.5 jaisa tha waisa
private fun Double?.toCleanString(): String {
    if (this == null) return ""
    return if (this % 1.0 == 0.0) this.toLong().toString() else this.toString()
}