package com.example.useressentialhub.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.useressentialhub.viewmodels.LocalCart

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartBottomSheet(
    onDismiss: () -> Unit,
    onNext: () -> Unit
) {
    val cart = LocalCart.current
    val cartItems = cart.items.values.sortedBy { it.product.productTitle ?: "" }
    val sheetState = rememberModalBottomSheetState()

    // Cart khaali ho jaye to sheet band kar do
    LaunchedEffect(cartItems.isEmpty()) {
        if (cartItems.isEmpty()) onDismiss()
    }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState, containerColor = MaterialTheme.colorScheme.surfaceVariant) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .navigationBarsPadding()
        ) {
            Text(
                text = "Products in the cart",
                color = MaterialTheme.colorScheme.primary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Spacer(Modifier.height(12.dp))

            LazyColumn(modifier = Modifier.heightIn(max = 350.dp)) {
                items(cartItems, key = { it.product.productRandomId }) { item ->
                    val product = item.product
                    val image = product.productImageUris?.firstOrNull { it != null }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color.White)
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
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = product.productTitle ?: "",
                                fontSize = 14.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "₹${product.productPrice ?: 0.0}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        // + / - wala button, quantity bhi yahin dikhti hai
                        CartButton(product)
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            Button(
                onClick = {
                    onDismiss()
                    onNext()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Next")
            }
            Spacer(Modifier.height(12.dp))
        }
    }
}