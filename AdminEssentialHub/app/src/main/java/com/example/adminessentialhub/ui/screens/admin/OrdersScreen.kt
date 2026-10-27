package com.example.adminessentialhub.ui.screens.admin

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.adminessentialhub.models.Order
import com.example.adminessentialhub.viewmodels.OrdersViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val StatusFilters = listOf("All", "Placed", "Accepted", "Delivered","Dispatched", "Rejected")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrdersScreen(viewModel: OrdersViewModel = viewModel()) {
    val context = LocalContext.current
    var filter by remember { mutableStateOf("All") }

    val shown = remember(viewModel.orders, filter) {
        if (filter == "All") viewModel.orders else viewModel.orders.filter { it.status == filter }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Text(
            text = "Orders",
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
        )

        // Status ke hisaab se filter
        LazyRow(
            contentPadding = PaddingValues(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(StatusFilters) { name ->
                FilterChip(
                    selected = filter == name,
                    onClick = { filter = name },
                    label = { Text(name) }
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        when {
            viewModel.isLoading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            viewModel.error != null -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = viewModel.error ?: "",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(24.dp)
                    )
                }
            }

            shown.isEmpty() -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = if (filter == "All") "Abhi koi order nahi aaya" else "$filter wala koi order nahi",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(shown, key = { it.orderId }) { order ->
                        OrderCard(
                            order = order,
                            busy = viewModel.busyOrderId == order.orderId,
                            onAccept = {
                                viewModel.accept(order) { _, msg ->
                                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                }
                            },
                            onReject = {
                                viewModel.setStatus(order, "Rejected") { _, msg ->
                                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                }
                            },
                            onDelivered = {
                                viewModel.setStatus(order, "Delivered") { _, msg ->
                                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                }
                            },
                            onDispatched = {
                                viewModel.setStatus(order, "Dispatched") { _, msg ->
                                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                }
                            },
                        )
                    }
                }
            }
        }
    }
}

private fun formatDate(timestamp: Long): String {
    if (timestamp <= 0L) return ""
    return SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(timestamp))
}

// 205.0 ki jagah 205, 12.5 jaisa tha waisa
private fun Double?.displayNumber(): String {
    if (this == null) return ""
    return if (this % 1.0 == 0.0) this.toLong().toString()
    else "%.2f".format(this).trimEnd('0').trimEnd('.')
}

@Composable
private fun OrderCard(
    order: Order,
    busy: Boolean,
    onAccept: () -> Unit,
    onReject: () -> Unit,
    onDispatched: () -> Unit,
    onDelivered: () -> Unit
) {
    val context = LocalContext.current
    val textMain = MaterialTheme.colorScheme.onSurface
    val textSub = MaterialTheme.colorScheme.onSurfaceVariant
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth().animateContentSize(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {

            // ---------- Order number + status ----------
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Order #${order.orderId.takeLast(6).uppercase()}",
                        color = textMain,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = formatDate(order.timestamp),
                        color = textSub,
                        fontSize = 12.sp
                    )
                }
                StatusChip(order.status)
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(
                        imageVector = if (expanded) Icons.Default.KeyboardArrowUp
                        else Icons.Default.KeyboardArrowDown,
                        contentDescription = if (expanded) "Hide items" else "Show items",
                        tint = textMain
                    )
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 10.dp),
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            )

            // ---------- Products ----------

            if(expanded){
                order.items.forEach { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
                        ) {
                            if (!item.image.isNullOrBlank()) {
                                AsyncImage(
                                    model = item.image,
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
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
                                text = "${item.productQuantity.displayNumber()} ${item.unit ?: ""}",
                                color = textSub,
                                fontSize = 12.sp
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "x${item.quantity}",
                                color = textMain,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "₹${((item.price ?: 0.0) * item.quantity).displayNumber()}",
                                color = textSub,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 10.dp),
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                )

            }


            // ---------- Customer ----------
            Text("Deliver to", color = textSub, fontSize = 12.sp)
            Text(
                text = order.userAddress.ifBlank { "Address nahi mila" },
                color = textMain,
                fontSize = 14.sp
            )
            if (order.userPhoneNumber.isNotBlank()) {
                TextButton(
                    onClick = {
                        context.startActivity(
                            Intent(Intent.ACTION_DIAL, Uri.parse("tel:${order.userPhoneNumber}"))
                        )
                    },
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("Call ${order.userPhoneNumber}")
                }
            }

            Spacer(Modifier.height(6.dp))

            // ---------- Total ----------
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Total",
                    color = textMain,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "₹${order.totalPrice.displayNumber()}",
                    color = textMain,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // ---------- Buttons: status ke hisaab se ----------
            when (order.status) {
                "Placed" -> {
                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedButton(
                            onClick = onReject,
                            enabled = !busy,
                            modifier = Modifier.weight(1f)
                        ) { Text("Reject") }
                        Button(
                            onClick = onAccept,
                            enabled = !busy,
                            modifier = Modifier.weight(1f)
                        ) { Text(if (busy) "Wait..." else "Accept") }
                    }
                }

                "Accepted" -> {
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = onDelivered,
                        enabled = !busy,
                        modifier = Modifier.fillMaxWidth()
                    ) { Text(if (busy) "Wait..." else "Mark as Delivered") }
                }

                "Dispatched" -> {
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = onDelivered,
                        enabled = !busy,
                        modifier = Modifier.fillMaxWidth()
                    ) { Text(if (busy) "Wait..." else "Mark as Delivered") }
                }
            }
        }
    }
}

@Composable
private fun StatusChip(status: String) {
    val color = when (status) {
        "Placed" -> Color(0xFFE67E22)
        "Accepted" -> Color(0xFF2E75B6)
        "Delivered" -> Color(0xFF2E9E4F)
        "Dispatched" -> Color(0xFF8E44AD)
        "Rejected" -> Color(0xFFC0392B)
        else -> Color.Gray
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 12.dp, vertical = 4.dp)
    ) {
        Text(text = status, color = color, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

