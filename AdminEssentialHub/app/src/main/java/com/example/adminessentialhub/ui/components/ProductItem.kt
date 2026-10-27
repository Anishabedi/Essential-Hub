package com.example.adminessentialhub.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.adminessentialhub.models.Product
import com.example.adminessentialhub.ui.screens.admin.orderClean

@Composable
fun ProductItem(
    product: Product,
    onEditClick: () -> Unit
) {
    val images = product.productImageUris?.filterNotNull() ?: emptyList()
    val pagerState = rememberPagerState(pageCount = { images.size.coerceAtLeast(1) })

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFCAD2ED).copy(alpha = 0.50f), RoundedCornerShape(13.dp))
            .padding(13.dp)
    ) {

        // ---------- Image Slider ----------
        if (images.isNotEmpty()) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(156.dp)
            ) { page ->
                AsyncImage(
                    model = images[page],
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(10.dp))
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(images.size) { index ->
                    val isSelected = pagerState.currentPage == index
                    Box(
                        modifier = Modifier
                            .padding(2.dp)
                            .size(if (isSelected) 7.dp else 5.dp)
                            .background(
                                if (isSelected) Color(0xFF001F54) else Color.Gray,
                                shape = CircleShape
                            )
                    )
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(156.dp)
                    .background(Color(0xFFE0E0E0), RoundedCornerShape(10.dp))
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // ---------- Title ----------
        Text(
            text = product.productTitle ?: "",
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(4.dp))

        // ---------- Quantity + Unit ----------
        Text(
            text = "${product.productQuantity.orderClean()} ${product.productUnit ?: ""}",
            fontSize = 12.sp,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(4.dp))

        // ---------- Price ----------
        Text(
            text = "₹${product.productPrice.orderClean().ifEmpty { "0" }}",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF001F54)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // ---------- Edit Button ----------
        Box(
            modifier = Modifier
                .align(Alignment.End)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFFB7B6CF).copy(alpha = 0.75f), RoundedCornerShape(8.dp))
                .clickable { onEditClick() }
                .padding(horizontal = 16.dp, vertical = 6.dp)
        ) {
            Text(
                text = "Edit",
                color = Color(0xFF31287D),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}