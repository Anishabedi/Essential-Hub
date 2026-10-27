package com.example.useressentialhub.viewmodel

import android.app.Activity
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.example.useressentialhub.models.Users
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
                    saveUserIfNew(onError)
                } else {
                    isLoading.value = false
                    onError(task.exception?.message ?: "Galat OTP")
                }
            }
    }

    // Naya user ho to uska node banao. Read ya write fail ho to error dikhao (crash nahi).
    private fun saveUserIfNew(onError: (String) -> Unit) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid == null) {
            isLoading.value = false
            onError("Login nahi hua")
            return
        }
        val ref = FirebaseDatabase.getInstance()
            .getReference("AllUsers").child("Users").child(uid)

        ref.get().addOnCompleteListener { readTask ->
            if (!readTask.isSuccessful) {
                isLoading.value = false
                onError(readTask.exception?.message ?: "User data nahi mila")
                return@addOnCompleteListener
            }
            if (readTask.result.exists()) {
                isLoading.value = false
                isSignedInSuccessfully.value = true
            } else {
                ref.setValue(Users(uid = uid, user_phoneNumber = savedPhoneNumber, user_address = ""))
                    .addOnCompleteListener { writeTask ->
                        isLoading.value = false
                        if (writeTask.isSuccessful) {
                            isSignedInSuccessfully.value = true
                        } else {
                            onError(writeTask.exception?.message ?: "User save nahi hua")
                        }
                    }
            }
        }
    }

    fun resetState() {
        isSignedInSuccessfully.value = false
        isACurrentUser.value = false
        verificationId = null
    }

}
