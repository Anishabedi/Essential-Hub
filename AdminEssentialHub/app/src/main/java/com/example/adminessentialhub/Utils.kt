package com.example.adminessentialhub

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import com.example.adminessentialhub.models.Product
import com.google.firebase.auth.FirebaseAuth

object Utils {

    fun showToast(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    fun validateProduct(
        context: Context,
        title: String,
        quantity: String,
        unit: String,
        price: String,
        stock: String,
        category: String,
        type: String,
        images: List<Uri>
    ): Product? {
        if (title.isBlank() || quantity.isBlank() || unit.isBlank() ||
            price.isBlank() || stock.isBlank() || category.isBlank() || type.isBlank()
        ) {
            showToast(context, "Empty fields are not allowed")
            return null
        }
        if (images.isEmpty()) {
            showToast(context, "Please upload some images")
            return null
        }

        val qty = quantity.replace(',', '.').toDoubleOrNull()
        val priceInt = price.replace(',', '.').toDoubleOrNull()
        val stockInt = stock.toIntOrNull()

        if (qty == null || priceInt == null || stockInt == null || !qty.isFinite() || !priceInt.isFinite() || qty <= 0 || priceInt < 0 || stockInt < 0) {
            showToast(context, "Enter correct Quantity, Price and Stock. ")
            return null
        }

        return Product(
            productTitle = title,
            productQuantity = qty,
            productUnit = unit,
            productPrice = priceInt,
            productStock = stockInt,
            productCategory = category,
            productType = type,
            itemCount = 0,
            adminUid = FirebaseAuth.getInstance().currentUser?.uid
        )
    }
}

const val MAX_IMAGES = 5

@Composable
fun rememberImagePicker(
    selectedImages: List<Uri>,
    onImagesChanged: (List<Uri>) -> Unit
): () -> Unit {
    val context = LocalContext.current
    val currentImages = rememberUpdatedState(selectedImages)

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = MAX_IMAGES)
    ) { uris ->
        if (uris.isNotEmpty()) {
            val combined = (currentImages.value + uris).distinct()
            if (combined.size > MAX_IMAGES) {
                Utils.showToast(context, "Maximum limit reached (sirf $MAX_IMAGES images)")
            }
            onImagesChanged(combined.take(MAX_IMAGES))
        }
    }

    return {
        if (currentImages.value.size >= MAX_IMAGES) {
            Utils.showToast(context, "Maximum limit reached")
        } else {
            launcher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        }
    }
}


//  nhvsn3ja