package com.example.useressentialhub.ui.screens.user

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.useressentialhub.models.Order
import com.example.useressentialhub.viewmodels.OrdersViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun OrdersScreen(navController: NavHostController) {
    val viewModel: OrdersViewModel = viewModel()
    LaunchedEffect(Unit) { viewModel.start() }

    LightStatusBarIcons()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        AccountHeader(title = "Your Orders", onBack = { navController.popBackStack() })

        when {
            viewModel.isLoading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }

            viewModel.hasError ->
                OrdersMessage("Could not load orders. Check your internet.")

            viewModel.orders.isEmpty() ->
                OrdersMessage("You haven't placed any orders yet")

            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(viewModel.orders) { order -> OrderCard(order) }
                }
            }
        }
    }
}

@Composable
private fun OrderCard(order: Order) {
    val textMain = MaterialTheme.colorScheme.onBackground
    val textSub = MaterialTheme.colorScheme.onSurfaceVariant
    val divider = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // ---- Order id + date + status ----
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = formatOrderDate(order.timestamp),
                        color = textMain,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
//                    Text(
//                        text = formatOrderDate(order.timestamp),
//                        color = textSub,
//                        fontSize = 12.sp
//                    )
                }
                StatusChip(order.overallStatus())
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(
                        imageVector = if (expanded) Icons.Default.KeyboardArrowUp
                        else Icons.Default.KeyboardArrowDown,
                        contentDescription = if (expanded) "Hide items" else "Show items",
                        tint = textMain
                    )
                }
            }

            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = divider)
            Spacer(Modifier.height(12.dp))

            // ---- Items (sirf arrow neeche hone par) ----
            if (expanded) {
                order.items.forEach { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            if (!item.image.isNullOrBlank()) {
                                AsyncImage(
                                    model = item.image,
                                    contentDescription = item.title,
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                        Spacer(Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.title ?: "",
                                color = textMain,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "Qty: ${item.quantity}",
                                color = textSub,
                                fontSize = 12.sp
                            )
                        }
                        Text(
                            text = "₹${((item.price ?: 0.0) * item.quantity).cleanPrice()}",
                            color = textSub,
                            fontSize = 13.sp
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))
                HorizontalDivider(color = divider)
                Spacer(Modifier.height(12.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Delivery Charges",
                        color = textMain,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Normal,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "₹${order.deliveryCharge.cleanPrice()}",
                        color = textMain,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Normal
                    )
                }

                Spacer(Modifier.height(12.dp))
                HorizontalDivider(color = divider)
                Spacer(Modifier.height(12.dp))
            }
            // ---- Total ----
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Total",
                    color = textMain,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "₹${order.grandTotal.cleanPrice()}",
                    color = textMain,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun StatusChip(status: String) {
    // Delivered hara, Cancelled lal, baaki sab theme ke primary rang mein
    val (bg, fg) = when (status.trim().lowercase()) {
        "delivered" -> Color(0xFF1B5E20) to Color.White
        "dispatched" -> Color(0xFF8E44AD) to Color.White
        "cancelled", "canceled", "rejected" -> Color(0xFFB71C1C) to Color.White
        else -> MaterialTheme.colorScheme.primary to MaterialTheme.colorScheme.onPrimary
    }
    Box(
        modifier = Modifier
            .background(bg, RoundedCornerShape(50))
            .padding(horizontal = 12.dp, vertical = 5.dp)
    ) {
        Text(text = status, color = fg, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun OrdersMessage(text: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 15.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

private fun formatOrderDate(timestamp: Long): String {
    if (timestamp <= 0L) return ""
    return SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(timestamp))
}

// 205.0 ki jagah 205, 12.5 jaisa tha waisa
private fun Double.cleanPrice(): String =
    if (this % 1.0 == 0.0) this.toLong().toString()
    else "%.2f".format(this).trimEnd('0').trimEnd('.')