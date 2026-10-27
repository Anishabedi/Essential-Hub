package com.example.useressentialhub.ui.screens.user

import android.app.Activity
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import com.example.useressentialhub.models.AddressData

private val HeaderNavy = Color(0xFF001F54)



/**
 * Stateless screen: Firebase / ViewModel ka kaam bahar se aayega.
 * initial   -> pehle se saved address (edit ke liye), null = naya
 * isSaving  -> Firebase write chal raha ho to true (button disable + loader)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddressScreen(
    initial: AddressData? = null,
    isSaving: Boolean = false,
    onBack: () -> Unit,
    onSave: (AddressData) -> Unit
) {
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

    var pin by remember { mutableStateOf(initial?.pinCode ?: "") }
    var phone by remember { mutableStateOf(initial?.phone ?: "") }
    var state by remember { mutableStateOf(initial?.state ?: "") }
    var district by remember { mutableStateOf(initial?.district ?: "") }
    var address by remember { mutableStateOf(initial?.fullAddress ?: "") }
    var submitted by remember { mutableStateOf(false) }

    val pinOk = pin.length == 6
    val phoneOk = phone.length == 10
    val stateOk = state.isNotBlank()
    val districtOk = district.isNotBlank()
    val addressOk = address.isNotBlank()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text("Delivery Address", color = Color.White, fontWeight = FontWeight.SemiBold)
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = HeaderNavy)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "Please enter your address",
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Spacer(Modifier.height(4.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                AddressField(
                    value = pin,
                    onValueChange = { if (it.length <= 6 && it.all(Char::isDigit)) pin = it },
                    label = "Pin Code",
                    keyboardType = KeyboardType.Number,
                    isError = submitted && !pinOk,
                    modifier = Modifier.weight(1f)
                )
                AddressField(
                    value = phone,
                    onValueChange = { if (it.length <= 10 && it.all(Char::isDigit)) phone = it },
                    label = "Phone No.",
                    keyboardType = KeyboardType.Phone,
                    isError = submitted && !phoneOk,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                AddressField(
                    value = state,
                    onValueChange = { state = it },
                    label = "State",
                    isError = submitted && !stateOk,
                    modifier = Modifier.weight(1f)
                )
                AddressField(
                    value = district,
                    onValueChange = { district = it },
                    label = "District",
                    isError = submitted && !districtOk,
                    modifier = Modifier.weight(1f)
                )
            }

            AddressField(
                value = address,
                onValueChange = { address = it },
                label = "Enter address (city, sector etc.)",
                isError = submitted && !addressOk,
                singleLine = false,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = {
                    submitted = true
                    if (pinOk && phoneOk && stateOk && districtOk && addressOk) {
                        onSave(AddressData(pin, phone, state.trim(), district.trim(), address.trim()))
                    }
                },
                enabled = !isSaving,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Add", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    }
}

@Composable
private fun AddressField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text,
    isError: Boolean = false,
    singleLine: Boolean = true
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, maxLines = 1) },
        isError = isError,
        singleLine = singleLine,
        minLines = if (singleLine) 1 else 2,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier,
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = MaterialTheme.colorScheme.onSurface,
            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
            focusedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
            unfocusedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
            focusedLabelColor = MaterialTheme.colorScheme.primary,
            unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
            cursorColor = MaterialTheme.colorScheme.primary
        )
    )
}
