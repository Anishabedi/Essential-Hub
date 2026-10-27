package com.example.useressentialhub.viewmodels

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.example.useressentialhub.data.local.CartDatabase
import com.example.useressentialhub.data.local.toCartEntity
import com.example.useressentialhub.data.local.toProduct
import com.example.useressentialhub.models.CartItem
import com.example.useressentialhub.models.Product
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock



class CartViewModel(application: Application) : AndroidViewModel(application) {

    // productId -> item. SnapshotStateMap hai, isliye badalte hi bar aur cards apne aap update hote hain.
    private val _items = mutableStateMapOf<String, CartItem>()
    private val dao = CartDatabase.get(application).cartDao()
    private val writeLock = Mutex()
    val items: Map<String, CartItem> get() = _items

    var address by mutableStateOf<String?>(null)
        private set
    var addressLoaded by mutableStateOf(false)
        private set

    // Ye cart kis user ka hai. Login/logout par badalta hai.
    private var cartOwnerUid: String? = null

    // Home khulte hi call karo. User badla ho to purana cart + address saaf karke naye user ka cart load karta hai.
    fun syncWithCurrentUser() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid == cartOwnerUid) return
        cartOwnerUid = uid
        _items.clear()
        address = null
        addressLoaded = false
        if (uid == null) return

        viewModelScope.launch {
            val rows = dao.getAll(uid)
            // Load hote-hote user badal gaya ho to purane user ki rows mat dalo
            if (cartOwnerUid != uid) return@launch
            rows.forEach { row ->
                // Agar user ne load hone se pehle hi kuch add kar diya ho, toh use mat badalo
                if (!_items.containsKey(row.productId)) {
                    _items[row.productId] = CartItem(row.toProduct(), row.cartQuantity)
                }
            }
        }
    }

    val totalItems: Int
        get() = _items.values.sumOf { it.quantity }

    val totalPrice: Double
        get() = _items.values.sumOf { (it.product.productPrice ?: 0.0) * it.quantity }

    fun quantityOf(productId: String): Int = _items[productId]?.quantity ?: 0

    // ADD aur "+" dono yahi: stock se zyada nahi ja sakte
    fun add(product: Product) {
        val uid = cartOwnerUid ?: return
        val id = product.productRandomId
        val stock = product.productStock ?: 0
        val current = quantityOf(id)
        if (current < stock) {
            val newQty = current + 1
            _items[id] = CartItem(product, newQty)
            save { dao.upsert(product.toCartEntity(newQty, uid)) }
        }
    }

    // "-" : 1 reh jaye toh item cart se hat jata hai
    fun decrease(product: Product) {
        val uid = cartOwnerUid ?: return
        val id = product.productRandomId
        val current = quantityOf(id)
        if (current <= 1) {
            _items.remove(id)
            save { dao.delete(uid, id) }
        } else {
            val newQty = current - 1
            _items[id] = CartItem(product, newQty)
            save { dao.upsert(product.toCartEntity(newQty, uid)) }
        }
    }

    fun clear() {
        val uid = cartOwnerUid
        _items.clear()
        if (uid != null) save { dao.clear(uid) }
    }

    fun loadAddress() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid == null) { addressLoaded = true; return }
        FirebaseDatabase.getInstance()
            .getReference("AllUsers/Users/$uid/user_address")
            .get()
            .addOnSuccessListener {
                address = it.getValue(String::class.java)
                addressLoaded = true
            }
            .addOnFailureListener { addressLoaded = true }
    }

    fun saveAddress(newAddress: String, onDone: (Boolean, String) -> Unit) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
            ?: return onDone(false, "Login nahi hai")

        FirebaseDatabase.getInstance()
            .getReference("AllUsers/Users/$uid/user_address")
            .setValue(newAddress)
            .addOnSuccessListener {
                address = newAddress
                onDone(true, "Address save ho gaya")
            }
            .addOnFailureListener { onDone(false, it.message ?: "Address save nahi hua") }
    }

    fun placeOrder(deliveryCharge: Double, onDone: (Boolean, String) -> Unit) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
            ?: return onDone(false, "Login nahi hai")
        val cartItems = _items.values.toList()
        if (cartItems.isEmpty()) return onDone(false, "Cart khaali hai")
        if (cartItems.any { it.product.adminUid.isNullOrEmpty() }) {
            return onDone(false, "Kisi product mein adminUid nahi hai")
        }

        val db = FirebaseDatabase.getInstance().reference

        db.child("AllUsers/Users/$uid").get().addOnSuccessListener { snap ->
            val orderAddress = snap.child("user_address").value?.toString().orEmpty()
            // Firebase mein number na mile to login wala number use karo
            val phone = snap.child("user_phoneNumber").value?.toString().orEmpty()
                .ifBlank { FirebaseAuth.getInstance().currentUser?.phoneNumber.orEmpty() }
            if (orderAddress.isBlank()) {
                onDone(false, "Pehle apna address add karo")
                return@addOnSuccessListener
            }

            val orderId = db.child("Orders/ByUser/$uid").push().key
                ?: return@addOnSuccessListener onDone(false, "Order id nahi bani")

            fun itemMap(ci: CartItem) = mapOf(
                "productId" to ci.product.productRandomId,
                "adminUid" to ci.product.adminUid,
                "title" to ci.product.productTitle,
                "price" to ci.product.productPrice,
                "quantity" to ci.quantity,
                "unit" to ci.product.productUnit,
                "productQuantity" to ci.product.productQuantity,
                "image" to ci.product.productImageUris?.firstOrNull { img -> img != null }
            )

            val updates = hashMapOf<String, Any?>()
            val adminStatus = hashMapOf<String, Any>()

            // Har admin ko sirf uske products wala hissa
            cartItems.groupBy { it.product.adminUid!! }.forEach { (adminUid, list) ->
                adminStatus[adminUid] = "Placed"
                updates["Orders/ByAdmin/$adminUid/$orderId"] = mapOf(
                    "orderId" to orderId,
                    "userUid" to uid,
                    "adminUid" to adminUid,
                    "userAddress" to orderAddress,
                    "userPhoneNumber" to phone,
                    "status" to "Placed",
                    "timestamp" to ServerValue.TIMESTAMP,
                    "totalPrice" to list.sumOf { (it.product.productPrice ?: 0.0) * it.quantity },
                    "items" to list.map { itemMap(it) }
                )
            }

            // User ko poora order, ek hi jagah
            val subTotal = cartItems.sumOf { (it.product.productPrice ?: 0.0) * it.quantity }
            updates["Orders/ByUser/$uid/$orderId"] = mapOf(
                "orderId" to orderId,
                "userUid" to uid,
                "userAddress" to orderAddress,
                "userPhoneNumber" to phone,
                "timestamp" to ServerValue.TIMESTAMP,
                "subTotal" to subTotal,
                "deliveryCharge" to deliveryCharge,
                "grandTotal" to subTotal + deliveryCharge,
                "items" to cartItems.map { itemMap(it) },
                "adminStatus" to adminStatus
            )

            db.updateChildren(updates)
                .addOnSuccessListener { clear(); onDone(true, "Order place ho gaya") }
                .addOnFailureListener { onDone(false, it.message ?: "Order fail ho gaya") }
        }.addOnFailureListener { onDone(false, it.message ?: "User data nahi mila") }
    }

    private fun save(block: suspend () -> Unit) {
        viewModelScope.launch { writeLock.withLock { block() } }
    }
}

// Screens ko cart dene ka tareeka (har composable ke parameter mein pass karne se bachne ke liye)
val LocalCart = staticCompositionLocalOf<CartViewModel> {
    error("CartViewModel provide nahi kiya (AppNavigation mein CompositionLocalProvider chahiye)")
}