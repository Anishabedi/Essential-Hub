package com.example.useressentialhub   // admin app mein: com.example.adminessentialhub

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessaging

/**
 * Firebase mein: FcmTokens/<uid>/<token> = true
 * Ek uid ke kai phone ho sakte hain, isliye token key hai, value nahi.
 * User aur admin dono ka uid alag hai, to ek hi node dono apps ke liye chalta hai.
 */
object FcmTokenManager {

    private fun ref(uid: String) =
        FirebaseDatabase.getInstance().getReference("FcmTokens/$uid")

    // Login ke baad / app khulte hi
    fun saveCurrentToken() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
            ref(uid).child(token).setValue(true)
        }
    }

    // FirebaseMessagingService.onNewToken se
    fun saveToken(token: String) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        ref(uid).child(token).setValue(true)
    }

    /**
     * Logout se PEHLE call karo (signOut ke baad uid nahi milta).
     * Token hatata hai, aur device ka token bhi bekaar kar deta hai, taaki is phone par
     * doosra user login kare to purane user ke notification na aayein.
     * Net na ho to 3 second baad logout aage badh jata hai (atakta nahi).
     */
    fun removeCurrentToken(onDone: () -> Unit) {
        var finished = false
        fun finish() {
            if (finished) return
            finished = true
            FirebaseMessaging.getInstance().deleteToken()
            onDone()
        }

        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid == null) {
            finish()
            return
        }

        Handler(Looper.getMainLooper()).postDelayed({ finish() }, 3000)

        FirebaseMessaging.getInstance().token
            .addOnSuccessListener { token ->
                ref(uid).child(token).removeValue().addOnCompleteListener { finish() }
            }
            .addOnFailureListener { finish() }
    }
}

// Home / main screen mein ek baar daal do: token save + Android 13+ par notification permission
@Composable
fun NotificationSetup() {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }

    LaunchedEffect(Unit) {
        FcmTokenManager.saveCurrentToken()
        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}