package com.example.useressentialhub.data.payment

import android.os.Handler
import android.os.Looper
import com.google.firebase.auth.FirebaseAuth
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/**
 * App yahan sirf apne backend se baat karta hai.
 * PhonePe ki secret keys (client secret) KABHI app mein nahi rakhni, wo backend mein rehti hain.
 *
 * Backend ke 2 endpoints chahiye:
 *   POST  {BASE_URL}/createOrder   body: {"amountPaise": 12345}
 *         -> {"merchantOrderId": "...", "orderId": "...", "token": "..."}
 *   POST  {BASE_URL}/orderStatus   body: {"merchantOrderId": "..."}
 *         -> {"state": "COMPLETED" | "FAILED" | "PENDING"}
 */
object PaymentApi {

    // Backend ban jaye tab yahan uska link daalna (https hona chahiye)
    const val BASE_URL = "https://YOUR-BACKEND-URL"

    // PhonePe ka test Merchant ID (ye secret nahi hota, SDK init ke liye chahiye)
    const val MERCHANT_ID = "YOUR_TEST_MERCHANT_ID"
    const val PAYMENT_ENABLED = false
    private val main = Handler(Looper.getMainLooper())

    data class PaymentOrder(
        val merchantOrderId: String,
        val orderId: String,
        val token: String
    )

    // Rupaye ko paise mein badal ke order banao. onDone(order, error)
    fun createOrder(amountPaise: Long, onDone: (PaymentOrder?, String?) -> Unit) {
        post("createOrder", JSONObject().put("amountPaise", amountPaise)) { json, err ->
            if (json == null) {
                onDone(null, err)
                return@post
            }
            val merchantOrderId = json.optString("merchantOrderId")
            val orderId = json.optString("orderId")
            val token = json.optString("token")
            if (merchantOrderId.isBlank() || orderId.isBlank() || token.isBlank()) {
                onDone(null, "Server se order details nahi mili")
            } else {
                onDone(PaymentOrder(merchantOrderId, orderId, token), null)
            }
        }
    }

    // Payment ka asli status backend se pucho. onDone(state, error)
    fun checkStatus(merchantOrderId: String, onDone: (String?, String?) -> Unit) {
        post("orderStatus", JSONObject().put("merchantOrderId", merchantOrderId)) { json, err ->
            if (json == null) onDone(null, err)
            else onDone(json.optString("state"), null)
        }
    }

    private fun post(path: String, body: JSONObject, onDone: (JSONObject?, String?) -> Unit) {
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            onDone(null, "Login nahi hai")
            return
        }
        // Firebase login token bhejte hain, taaki backend jaan sake ki request asli user ki hai
        user.getIdToken(false)
            .addOnSuccessListener { result ->
                Thread {
                    try {
                        val conn = (URL("$BASE_URL/$path").openConnection() as HttpURLConnection).apply {
                            requestMethod = "POST"
                            doOutput = true
                            connectTimeout = 15000
                            readTimeout = 30000
                            setRequestProperty("Content-Type", "application/json")
                            setRequestProperty("Authorization", "Bearer ${result.token}")
                        }
                        conn.outputStream.use { it.write(body.toString().toByteArray()) }

                        val code = conn.responseCode
                        val stream = if (code in 200..299) conn.inputStream else conn.errorStream
                        val text = stream?.bufferedReader()?.use { it.readText() }.orEmpty()
                        val json = runCatching { JSONObject(text) }.getOrNull()

                        main.post {
                            if (code in 200..299 && json != null) onDone(json, null)
                            else onDone(null, json?.optString("error")?.ifBlank { null } ?: "Server error ($code)")
                        }
                    } catch (e: Exception) {
                        main.post { onDone(null, e.message ?: "Network error") }
                    }
                }.start()
            }
            .addOnFailureListener { onDone(null, "Login token nahi mila") }
    }
}