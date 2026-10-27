package com.example.useressentialhub.ui.screens.user

import android.app.Activity
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.useressentialhub.ui.components.CartButton
import com.example.useressentialhub.viewmodels.LocalCart
import com.example.useressentialhub.routes.UserRoutes
import com.phonepe.intent.sdk.api.PhonePeKt
import com.google.firebase.auth.FirebaseAuth
import com.example.useressentialhub.data.payment.PaymentApi
import com.phonepe.intent.sdk.api.models.PhonePeEnvironment
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import kotlin.math.roundToLong

// Delivery rule: itne se upar free, warna itna charge (yahan se badal sakti ho)
private const val FREE_DELIVERY_ABOVE = 99.0
private const val DELIVERY_FEE = 25.0

private val HeaderNavy = Color(0xFF001F54)
private val PhonePePurple = Color(0xFF5F259F)

@Composable
fun CheckoutScreen(navController: NavHostController) {
    val cart = LocalCart.current
    val context = LocalContext.current
    var placing by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { cart.loadAddress() }
    LaunchedEffect(Unit) {
        if (!PaymentApi.PAYMENT_ENABLED) return@LaunchedEffect
        val activity = context as? Activity ?: return@LaunchedEffect
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: "guest"

        val ok = PhonePeKt.init(
            context = activity,
            merchantId = PaymentApi.MERCHANT_ID,
            flowId = uid,
            phonePeEnvironment = PhonePeEnvironment.SANDBOX,
            enableLogging = true,    // test mein true, baad mein false karna
            appId = null
        )
        if (!ok) {
            Toast.makeText(context, "PhonePe SDK init nahi hua", Toast.LENGTH_SHORT).show()
        }
    }

    // Status bar ke icons navy header ke upar safed dikhein
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

    // Naam ke hisaab se sort: list ka order baar-baar na badle
    val items = cart.items.values.sortedBy { it.product.productTitle ?: "" }

    val subTotal = cart.totalPrice
    val delivery = if (items.isEmpty() || subTotal >= FREE_DELIVERY_ABOVE) 0.0 else DELIVERY_FEE
    val grandTotal = subTotal + delivery


    // Sab rang theme se: light aur dark dono mein category screen jaisa
    val screenBg = MaterialTheme.colorScheme.background
    val cardColor = MaterialTheme.colorScheme.surfaceVariant   // product card jaisa rang
    val textMain = MaterialTheme.colorScheme.onBackground
    val textSub = MaterialTheme.colorScheme.onSurfaceVariant

    var paidMerchantOrderId by remember { mutableStateOf<String?>(null) }
    var pendingMerchantOrderId by remember { mutableStateOf<String?>(null) }


    fun startOrder() {
        placing = true
        cart.placeOrder(delivery) { ok, msg ->
            val wasPaid = paidMerchantOrderId != null
            placing = false
            val text = when {
                ok && wasPaid -> "Payment ho gayi, order place ho gaya"
                !ok && wasPaid -> "Payment ho chuki hai par order save nahi hua. Place Order dobara dabao"
                else -> msg
            }
            Toast.makeText(context, text, Toast.LENGTH_LONG).show()
            if (ok) {
                paidMerchantOrderId = null
                navController.popBackStack()
            }
        }
    }


// PhonePe screen se wapas aane par: asli status backend se pucho
    val paymentLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        val moid = pendingMerchantOrderId
        if (moid == null) {
            placing = false
        } else {
            PaymentApi.checkStatus(moid) { state, err ->
                when (state) {
                    "COMPLETED" -> {
                        paidMerchantOrderId = moid
                        startOrder()          // payment ho gayi, ab order banao
                    }
                    "FAILED" -> {
                        placing = false
                        Toast.makeText(
                            context,
                            "Payment declined, order nahi hua",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    else -> {
                        placing = false
                        Toast.makeText(
                            context,
                            err ?: "Payment complete nahi hui, order nahi hua. Agar paise kat gaye hain to thodi der baad check karo",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }
    }

    fun startPayment() {
        val activity = context as? Activity ?: return
        placing = true
        // PhonePe paise mein leta hai (₹1 = 100 paise, kam se kam 100)
        PaymentApi.createOrder((grandTotal * 100).roundToLong()) { order, err ->
            if (order == null) {
                placing = false
                Toast.makeText(context, err ?: "Payment shuru nahi hui", Toast.LENGTH_SHORT).show()
                return@createOrder
            }
            pendingMerchantOrderId = order.merchantOrderId
            try {
                PhonePeKt.startCheckoutPage(
                    context = activity,
                    token = order.token,
                    orderId = order.orderId,
                    activityResultLauncher = paymentLauncher
                )
            } catch (e: Exception) {
                placing = false
                Toast.makeText(context, "Payment shuru nahi hui", Toast.LENGTH_SHORT).show()
            }
        }
    }


    fun onPlaceOrderClick() {
        if (!cart.addressLoaded) {
            Toast.makeText(context, "Ek second...", Toast.LENGTH_SHORT).show()
            return
        }
        if (cart.address.isNullOrBlank()) navController.navigate(UserRoutes.ADDRESS)
        else if (paidMerchantOrderId != null) startOrder()      // payment ho chuki hai, sirf order banao
        else if (PaymentApi.PAYMENT_ENABLED) startPayment()
        else startOrder()
    }

    Column(modifier = Modifier.fillMaxSize().background(screenBg)) {

        // ---------- Header: navy, jaisa category screen mein ----------
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(HeaderNavy)
                .statusBarsPadding()
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(HeaderNavy)
                .padding(start = 8.dp, end = 16.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier.size(40.dp)
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Text(
                text = "Checkout",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        if (items.isEmpty()) {
            Box(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Your cart is empty",
                    fontSize = 15.sp,
                    color = textSub
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // ---------- Card 1: cart ke saare items ----------
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = cardColor),
                        elevation = CardDefaults.cardElevation(0.dp)
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                            items.forEachIndexed { index, item ->
                                val product = item.product
                                val image = product.productImageUris?.firstOrNull { it != null }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(64.dp)
                                            .clip(RoundedCornerShape(10.dp))
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

                                    Spacer(Modifier.width(12.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = product.productTitle ?: "",
                                            color = textMain,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Medium,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "${product.productQuantity.checkoutClean()} ${product.productUnit ?: ""}",
                                            color = textSub,
                                            fontSize = 12.sp
                                        )
                                        Text(
                                            text = "₹${((product.productPrice ?: 0.0) * item.quantity).checkoutClean()}",
                                            color = textMain,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    Spacer(Modifier.width(8.dp))

                                    CartButton(product)
                                }

                                // Do items ke beech: gap, line, gap
                                if (index < items.lastIndex) {
                                    Spacer(Modifier.height(20.dp))
                                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                                    Spacer(Modifier.height(20.dp))
                                }
                            }
                        }
                    }
                }

                // ---------- Card 2: Bill Details ----------
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = cardColor),
                        elevation = CardDefaults.cardElevation(0.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Bill Details",
                                color = textMain,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(14.dp))

                            BillRow(
                                icon = Icons.Default.List,
                                label = "Sub Total",
                                value = subTotal.checkoutClean(),
                                color = textMain
                            )
                            Spacer(Modifier.height(10.dp))
                            BillRow(
                                icon = Icons.Default.LocationOn,
                                label = "Delivery Charge",
                                value = if (delivery == 0.0) "Free" else delivery.checkoutClean(),
                                color = textMain
                            )
                            Spacer(Modifier.height(14.dp))
                            BillRow(
                                icon = null,
                                label = "Grand Total",
                                value = grandTotal.checkoutClean(),
                                color = textMain,
                                bold = true
                            )
                        }
                    }
                }
            }

            // ---------- Neeche: CartBar jaisa bar (PhonePe UPI + Place Order) ----------
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp),
                shadowElevation = 12.dp
            ) {
                Row(
                    modifier = Modifier
                        .navigationBarsPadding()
                        .padding(13.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Abhi sirf dikhane ke liye, asli payment baad mein jodenge
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(PhonePePurple),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("पे", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = "PhonePe UPI",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 14.sp,
                        modifier = Modifier.weight(1f)
                    )

                    // Next button jaisa: light mein navy + safed text, dark mein safed + kaala text
                    val primary = MaterialTheme.colorScheme.primary
                    val onPrimary = MaterialTheme.colorScheme.onPrimary
                    Row(
                        modifier = Modifier
                            .weight(1.2f)
                            .height(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (placing) primary.copy(alpha = 0.5f) else primary)
                            .then(
                                if (placing) Modifier
                                else Modifier.clickable { onPlaceOrderClick() }
                            ),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (placing) "Placing..." else "Place Order",
                            color = onPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        if (!placing) {
                            Spacer(Modifier.width(4.dp))
                            Icon(
                                Icons.Default.KeyboardArrowRight,
                                contentDescription = null,
                                tint = onPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }

    }



}

@Composable
private fun BillRow(
    icon: ImageVector?,
    label: String,
    value: String,
    color: Color,
    bold: Boolean = false
) {
    val weight = if (bold) FontWeight.Bold else FontWeight.Medium
    val size = if (bold) 16.sp else 14.sp
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(10.dp))
        }
        Text(
            text = label,
            color = color,
            fontSize = size,
            fontWeight = weight,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            color = color,
            fontSize = size,
            fontWeight = weight
        )
    }
}

// 205.0 ki jagah 205, 12.5 jaisa tha waisa
private fun Double?.checkoutClean(): String {
    if (this == null) return ""
    return if (this % 1.0 == 0.0) this.toLong().toString()
    else "%.2f".format(this).trimEnd('0').trimEnd('.')
}