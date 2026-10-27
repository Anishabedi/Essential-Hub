package com.example.useressentialhub.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.useressentialhub.models.Order
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
    var hasError by mutableStateOf(false)
        private set

    private var ref: DatabaseReference? = null
    private var listener: ValueEventListener? = null

    // Listener hai, get() nahi: admin status badle to user ki screen apne aap update ho jaye
    fun start() {
        if (listener != null) return
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid == null) {
            isLoading = false
            return
        }

        val r = FirebaseDatabase.getInstance().getReference("Orders/ByUser/$uid")
        val l = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                orders = snapshot.children
                    // ek kharab order poori list ko crash na kare
                    .mapNotNull { runCatching { it.getValue(Order::class.java) }.getOrNull() }
                    .sortedByDescending { it.timestamp }
                hasError = false
                isLoading = false
            }

            override fun onCancelled(error: DatabaseError) {
                hasError = true
                isLoading = false
            }
        }
        r.addValueEventListener(l)
        ref = r
        listener = l
    }

    override fun onCleared() {
        listener?.let { ref?.removeEventListener(it) }
    }
}