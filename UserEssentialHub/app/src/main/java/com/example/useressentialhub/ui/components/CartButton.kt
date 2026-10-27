package com.example.useressentialhub.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.useressentialhub.models.Product
import com.example.useressentialhub.viewmodels.LocalCart

// Har product card ka ADD / "- 2 +" button. Teeno screens (Home, Search, Category) yahi use karte hain,
// aur sab ek hi cart se judte hain.
@Composable
fun CartButton(product: Product, modifier: Modifier = Modifier) {
    val cart = LocalCart.current
    val qty = cart.quantityOf(product.productRandomId)
    val stock = product.productStock ?: 0
    val primary = MaterialTheme.colorScheme.primary
    val onPrimary = MaterialTheme.colorScheme.onPrimary
    val shape = RoundedCornerShape(8.dp)

    when {
        // Stock khatam
        stock <= 0 -> {
            Box(
                modifier = modifier
                    .height(34.dp)
                    .widthIn(min = 64.dp)
                    .clip(shape)
                    .border(1.dp, Color.Gray, shape),
                contentAlignment = Alignment.Center
            ) {
                Text("Out", color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Abhi cart mein nahi
        qty == 0 -> {
            Box(
                modifier = modifier
                    .height(34.dp)
                    .widthIn(min = 64.dp)
                    .clip(shape)
                    .border(1.dp, primary, shape)
                    .clickable { cart.add(product) },
                contentAlignment = Alignment.Center
            ) {
                Text("ADD", color = primary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Cart mein hai: "-  2  +"
        else -> {
            Row(
                modifier = modifier
                    .height(34.dp)
                    .widthIn(min = 84.dp)
                    .clip(shape)
                    .background(primary),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(28.dp)
                        .clickable { cart.decrease(product) },
                    contentAlignment = Alignment.Center
                ) {
                    Text("-", color = onPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }

                Text("$qty", color = onPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)

                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(28.dp)
                        .clickable { cart.add(product) },   // stock se zyada nahi jaata (ViewModel rokta hai)
                    contentAlignment = Alignment.Center
                ) {
                    Text("+", color = onPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

