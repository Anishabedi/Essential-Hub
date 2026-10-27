package com.example.useressentialhub.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Video ke llCart ka Compose version: neeche chipka bar, upar ke kone 26dp gol.
// Baayein aadhe mein cart icon + "N ITEM", daayein aadhe mein "Next" button (video jaisa 0.5 / 0.5).
@Composable
fun CartBar(
    itemCount: Int,
    onViewCartClick: () -> Unit,
    onNextClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val primary = MaterialTheme.colorScheme.primary
    val onPrimary = MaterialTheme.colorScheme.onPrimary
    var showSheet by remember { mutableStateOf(false) }


    if (showSheet) {
        CartBottomSheet(
            onDismiss = { showSheet = false },
            onNext = onNextClick
        )
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp),
        shadowElevation = 12.dp
    ) {
        Row(
            // navigationBarsPadding: bar phone ke neeche wale system bar ke peeche tak failta hai
            modifier = Modifier
                .navigationBarsPadding()
                .padding(13.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // ---- Baayein aadhe: cart icon + N ITEM + chhota arrow ----
            Row(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { showSheet = true },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(39.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(primary.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ShoppingCart,
                        contentDescription = "Cart",
                        tint = primary,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(Modifier.width(13.dp))

                Text(
                    text = if (itemCount == 1) "$itemCount ITEM" else "$itemCount ITEMS",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.width(6.5.dp))

                Icon(
                    imageVector = Icons.Default.KeyboardArrowUp,
                    contentDescription = "View cart",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(Modifier.width(12.dp))

            // ---- Daayein aadhe: Next button ----
            Row(
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(primary)
                    .clickable { onNextClick() },
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Next",
                    color = onPrimary,
                    fontSize = 16.9.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.width(6.5.dp))
                Icon(
                    imageVector = Icons.Default.KeyboardArrowRight,
                    contentDescription = null,
                    tint = onPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}