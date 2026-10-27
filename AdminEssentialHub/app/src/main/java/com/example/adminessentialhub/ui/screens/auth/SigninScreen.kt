package com.example.adminessentialhub.ui.screens.auth

import android.app.Activity
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.adminessentialhub.Utils
import com.example.adminessentialhub.data.ItemImages
import com.example.adminessentialhub.routes.navhost.NavRoutes
import com.example.adminessentialhub.ui.components.LoadingDialog
import com.example.adminessentialhub.ui.components.ScatteredBackground
import com.example.adminessentialhub.viewmodel.AuthViewModel



@Composable
fun SignInScreen(navController: NavHostController) {
    var phoneNumber by remember { mutableStateOf("") }
    val context = LocalContext.current
    val authViewModel: AuthViewModel = viewModel(viewModelStoreOwner = context as ComponentActivity)
    val activity = context as Activity
    val isPhoneValid = phoneNumber.length == 10
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5).copy(alpha = 0.85f))
    ) {

        ScatteredBackground(images = ItemImages.list)

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF001F54).copy(alpha = 0.5f))   // Navy overlay
        )


        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center)   // 👈 Ab ye CENTER mein hai, bottom mein nahi
                .padding(horizontal = 20.dp)
                .background(Color.White, shape = RoundedCornerShape(20.dp))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Essential Hub",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF001F54)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Login or Sign up",
                fontSize = 16.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(20.dp))

            OutlinedTextField(
                value = phoneNumber,
                onValueChange = { input ->
                    if (input.length <= 10 && input.all { it.isDigit() }) {
                        phoneNumber = input
                    }
                },
                label = { Text("Enter mobile number") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    authViewModel.sendOtp(
                        activity = activity,
                        phoneNumber = phoneNumber,
                        onCodeSent = { navController.navigate(NavRoutes.OTP) },
                        onError = { error -> Utils.showToast(context, error) }
                    )
                },
                enabled = isPhoneValid,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF6650a4),
                    disabledContainerColor = Color(0xFF6650a4).copy(alpha = 0.3f)
                )
            ) {
                Text("Continue")
            }
        }
        if (authViewModel.isLoading.value) {
            LoadingDialog()
        }
    }
}

