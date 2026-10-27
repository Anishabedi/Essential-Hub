package com.example.adminessentialhub.viewmodel

import android.app.Activity
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.adminessentialhub.models.Admin
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.database.FirebaseDatabase
import java.util.concurrent.TimeUnit

class AuthViewModel : ViewModel() {
    var savedPhoneNumber: String = ""
    var isLoading = mutableStateOf(false)
        private set

    var verificationId: String? = null
        private set

    var isSignedInSuccessfully = mutableStateOf(false)
        private set

    var isACurrentUser = mutableStateOf(false)
        private set

    init {
        if (FirebaseAuth.getInstance().currentUser != null) {
            isACurrentUser.value = true
        }
    }

    fun sendOtp(
        activity: Activity,
        phoneNumber: String,
        onCodeSent: () -> Unit,
        onError: (String) -> Unit
    ) {
        savedPhoneNumber = phoneNumber
        isLoading.value = true

        val options = PhoneAuthOptions.newBuilder(FirebaseAuth.getInstance())
            .setPhoneNumber("+91$phoneNumber")
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    // Auto-verify: seedha sign in
                    signInWithCredential(credential, onError)
                }

                override fun onVerificationFailed(e: FirebaseException) {
                    isLoading.value = false
                    onError(e.message ?: "Verification failed")
                }

                override fun onCodeSent(id: String, token: PhoneAuthProvider.ForceResendingToken) {
                    isLoading.value = false
                    verificationId = id
                    onCodeSent()
                }
            })
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    fun verifyOtp(otp: String, onError: (String) -> Unit) {
        val id = verificationId
        if (id == null) {
            onError("Pehle OTP bhejo")
            return
        }
        val credential = PhoneAuthProvider.getCredential(id, otp)
        signInWithCredential(credential, onError)
    }

    private fun signInWithCredential(
        credential: PhoneAuthCredential,
        onError: (String) -> Unit
    ) {
        isLoading.value = true
        FirebaseAuth.getInstance().signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    saveUserIfNew()
                } else {
                    isLoading.value = false
                    onError(task.exception?.message ?: "Galat OTP")
                }
            }
    }

    private fun saveUserIfNew() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val ref = FirebaseDatabase.getInstance()
            .getReference("Admin").child("AdminInfo").child(uid)

        ref.get().addOnCompleteListener { snapshotTask ->
            val exists = snapshotTask.result?.exists() == true
            if (!exists) {
                ref.setValue(Admin(uid = uid, adminPhoneNumber = savedPhoneNumber))
            }
            isLoading.value = false
            isSignedInSuccessfully.value = true
        }
    }

    fun resetState() {
        isSignedInSuccessfully.value = false
        isACurrentUser.value = false
        verificationId = null
    }

}
// user ki uid phone number address