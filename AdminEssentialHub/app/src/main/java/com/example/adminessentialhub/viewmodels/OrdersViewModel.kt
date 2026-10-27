package com.example.adminessentialhub.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.adminessentialhub.models.Order
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class OrdersViewModel : ViewModel() {

    var orders by mutableStateOf<List<Order>>(emptyList())
        private set
    var isLoading by mutableStateOf(true)
        private set
    var error by mutableStateOf<String?>(null)
        private set

    // Jis order par abhi kaam chal raha hai (button disable karne ke liye)
    var busyOrderId by mutableStateOf<String?>(null)
        private set

    private val db = FirebaseDatabase.getInstance().reference
    private var ref: DatabaseReference? = null
    private var listener: ValueEventListener? = null

    init {
        listenToOrders()
    }

    // Sirf is admin ke orders: Orders/ByAdmin/<apna uid>. Naya order aate hi list apne aap badal jati hai.
    private fun listenToOrders() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid == null) {
            isLoading = false
            error = "Login nahi hai"
            return
        }
        val r = db.child("Orders/ByAdmin/$uid")
        val l = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                orders = snapshot.children
                    .mapNotNull { it.getValue(Order::class.java) }
                    .sortedByDescending { it.timestamp }
                error = null
                isLoading = false
            }

            override fun onCancelled(e: DatabaseError) {
                error = e.message
                isLoading = false
            }
        }
        r.addValueEventListener(l)
        ref = r
        listener = l
    }

    // Reject / Delivered: sirf status badlo (2 jagah, ek saath)
    fun setStatus(order: Order, newStatus: String, onDone: (Boolean, String) -> Unit) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
            ?: return onDone(false, "Login nahi hai")
        busyOrderId = order.orderId

        val updates = hashMapOf<String, Any?>(
            "Orders/ByAdmin/$uid/${order.orderId}/status" to newStatus,
            "Orders/ByUser/${order.userUid}/${order.orderId}/adminStatus/$uid" to newStatus
        )

        db.updateChildren(updates)
            .addOnSuccessListener {
                busyOrderId = null
                onDone(true, "Order $newStatus ho gaya")
            }
            .addOnFailureListener {
                busyOrderId = null
                onDone(false, it.message ?: "Status nahi badla")
            }
    }

    // Accept: pehle stock check, phir stock kam + status "Accepted", sab ek saath (ya to sab hoga ya kuch nahi)
    fun accept(order: Order, onDone: (Boolean, String) -> Unit) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
            ?: return onDone(false, "Login nahi hai")
        if (order.items.isEmpty()) return onDone(false, "Order mein products nahi hain")
        busyOrderId = order.orderId

        fun fail(msg: String) {
            busyOrderId = null
            onDone(false, msg)
        }

        // Har product ka abhi ka data padho (stock, category, type)
        val reads = order.items.map { db.child("Admin/AllProducts/${it.productId}").get() }

        Tasks.whenAllSuccess<DataSnapshot>(reads)
            .addOnSuccessListener { snaps ->
                val updates = hashMapOf<String, Any?>()

                for ((i, item) in order.items.withIndex()) {
                    val snap = snaps[i]
                    if (!snap.exists()) {
                        fail("${item.title ?: "Product"} ab store mein nahi hai")
                        return@addOnSuccessListener
                    }
                    val stock = (snap.child("productStock").value as? Number)?.toInt() ?: 0
                    if (stock < item.quantity) {
                        fail("${item.title ?: "Product"}: stock sirf $stock bacha hai")
                        return@addOnSuccessListener
                    }

                    val newStock = stock - item.quantity
                    val id = item.productId
                    // Product 3 jagah save hota hai, teeno jagah stock badlo
                    updates["Admin/AllProducts/$id/productStock"] = newStock
                    snap.child("productCategory").getValue(String::class.java)
                        ?.takeIf { it.isNotBlank() }
                        ?.let { updates["Admin/ProductCategory/$it/$id/productStock"] = newStock }
                    snap.child("productType").getValue(String::class.java)
                        ?.takeIf { it.isNotBlank() }
                        ?.let { updates["Admin/ProductType/$it/$id/productStock"] = newStock }
                }

                updates["Orders/ByAdmin/$uid/${order.orderId}/status"] = "Accepted"
                updates["Orders/ByUser/${order.userUid}/${order.orderId}/adminStatus/$uid"] = "Accepted"

                db.updateChildren(updates)
                    .addOnSuccessListener {
                        busyOrderId = null
                        onDone(true, "Order accept ho gaya, stock kam ho gaya")
                    }
                    .addOnFailureListener { fail(it.message ?: "Accept nahi hua") }
            }
            .addOnFailureListener { fail(it.message ?: "Product ka data nahi mila") }
    }

    override fun onCleared() {
        listener?.let { ref?.removeEventListener(it) }
        super.onCleared()
    }
}