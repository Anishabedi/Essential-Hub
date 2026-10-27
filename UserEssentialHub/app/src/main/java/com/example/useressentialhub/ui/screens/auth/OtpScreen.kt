package com.example.useressentialhub.ui.screens.auth

import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.useressentialhub.Utils
import com.example.useressentialhub.routes.NavRoutes
import com.example.useressentialhub.routes.UserRoutes
import com.example.useressentialhub.ui.components.LoadingDialog
import com.example.useressentialhub.viewmodel.AuthViewModel

@Composable
fun OtpScreen(navController: NavHostController) {
    var otp by remember { mutableStateOf("") }
    val isOtpValid = otp.length == 6
    val context = LocalContext.current
    val authViewModel: AuthViewModel = viewModel(viewModelStoreOwner = context as ComponentActivity)

    LaunchedEffect(authViewModel.isSignedInSuccessfully.value) {
        if (authViewModel.isSignedInSuccessfully.value) {
            Utils.showToast(context, "Login successful")
            navController.navigate(UserRoutes.GRAPH) {
                popUpTo(NavRoutes.SIGN_IN) { inclusive = true }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF001F54)),   // Simple navy background
        contentAlignment = Alignment.Center
    ) {
        IconButton(
            onClick = { navController.popBackStack() },
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = Color.White
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .background(Color.White, shape = RoundedCornerShape(20.dp))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "OTP Verification",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF001F54)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Enter the 6-digit code sent to your number",
                fontSize = 14.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = otp,
                onValueChange = { input ->
                    if (input.length <= 6 && input.all { it.isDigit() }) {
                        otp = input
                    }
                },
                label = { Text("Enter OTP") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                textStyle = TextStyle(
                    textAlign = TextAlign.Center,
                    fontSize = 20.sp,
                    letterSpacing = 8.sp
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    authViewModel.verifyOtp(otp = otp) { error ->
                        Utils.showToast(context, error)
                    }
                },
                enabled = isOtpValid,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF6650a4),
                    disabledContainerColor = Color(0xFF6650a4).copy(alpha = 0.3f)
                )
            ) {
                Text("Login")
            }

            Spacer(modifier = Modifier.height(12.dp))

            TextButton(onClick = { /* Resend OTP logic baad mein */ }) {
                Text(
                    text = "Resend OTP",
                    color = Color(0xFF001F54)
                )
            }
        }
        if (authViewModel.isLoading.value) {
            LoadingDialog()
        }
    }
}