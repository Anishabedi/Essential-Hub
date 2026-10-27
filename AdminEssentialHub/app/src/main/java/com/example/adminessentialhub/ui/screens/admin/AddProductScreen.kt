package com.example.adminessentialhub.ui.screens.admin

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.adminessentialhub.Utils
import com.example.adminessentialhub.data.Constant
import com.example.adminessentialhub.ui.components.ItemImageSelection
import com.example.adminessentialhub.rememberImagePicker
import com.example.adminessentialhub.viewmodel.ProductViewModel



fun Double?.orderClean(): String {
    if (this == null) return ""
    return if (this % 1.0 == 0.0) this.toLong().toString() else this.toString()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProductScreen(navController: NavHostController, productId: String? = null) {
    val isEdit = productId != null
    var oldId by remember { mutableStateOf<String?>(null) }
    var oldCategory by remember { mutableStateOf<String?>(null) }
    var oldType by remember { mutableStateOf<String?>(null) }
    var existingImageUrls by remember { mutableStateOf<List<String>>(emptyList()) }
    var title by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var stock by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("") }

    var unitExpanded by remember { mutableStateOf(false) }
    var categoryExpanded by remember { mutableStateOf(false) }
    var typeExpanded by remember { mutableStateOf(false) }

    var selectedImages by remember { mutableStateOf<List<Uri>>(emptyList()) }
    val context = LocalContext.current
    val openImagePicker = rememberImagePicker(selectedImages) { selectedImages = it }

    val productViewModel: ProductViewModel = viewModel()

    LaunchedEffect(productId) {
        if (productId != null) {
            productViewModel.getProduct(productId) { p ->
                if (p != null) {
                    oldId = p.productRandomId
                    oldCategory = p.productCategory
                    oldType = p.productType

                    title = p.productTitle ?: ""
                    quantity = p.productQuantity.orderClean()
                    unit = p.productUnit ?: ""
                    price = p.productPrice.orderClean()
                    stock = p.productStock?.toString() ?: ""
                    category = p.productCategory ?: ""
                    type = p.productType ?: ""
                    existingImageUrls = p.productImageUris?.filterNotNull() ?: emptyList()
                }
            }
        }
    }



    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {

            // ---------- Header ----------
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF001F54))
                    .padding(vertical = 16.dp, horizontal = 20.dp)
            ) {
                Text(
                    text = if (isEdit) "Edit Product" else "Add Product",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // ---------- Form Card ----------
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .background(Color.White, RoundedCornerShape(16.dp))
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Text(
                    text = if (isEdit) "Update product details!" else "Please fill product details!",
                    color = Color(0xFF6650a4),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Product Title") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = quantity,
                        onValueChange = { quantity = it },
                        label = { Text("Quantity") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    ExposedDropdownMenuBox(
                        expanded = unitExpanded,
                        onExpandedChange = { unitExpanded = it },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = unit,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Unit") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = unitExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = unitExpanded,
                            onDismissRequest = { unitExpanded = false }
                        ) {
                            Constant.allUnitsOfProducts.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option) },
                                    onClick = {
                                        unit = option
                                        unitExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = price,
                        onValueChange = { price = it },
                        label = { Text("Price (in rupees)") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = stock,
                        onValueChange = { stock = it },
                        label = { Text("No. of Stock") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                ExposedDropdownMenuBox(
                    expanded = categoryExpanded,
                    onExpandedChange = { categoryExpanded = it }
                ) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Product Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = categoryExpanded,
                        onDismissRequest = { categoryExpanded = false }
                    ) {
                        Constant.allProductsCategory.forEach { categoryItem ->
                            DropdownMenuItem(
                                text = { Text(categoryItem.name) },
                                onClick = {
                                    category = categoryItem.name
                                    categoryExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                ExposedDropdownMenuBox(
                    expanded = typeExpanded,
                    onExpandedChange = { typeExpanded = it }
                ) {
                    OutlinedTextField(
                        value = type,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Product Type") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = typeExpanded,
                        onDismissRequest = { typeExpanded = false }
                    ) {
                        Constant.allProductsType.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    type = option
                                    typeExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // ---------- Image select header + button ----------
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Please select some images",
                        color = Color(0xFF6650a4),
                        fontSize = 13.sp
                    )

                    IconButton(onClick = openImagePicker) {
                        Icon(
                            imageVector = Icons.Default.AddPhotoAlternate,
                            contentDescription = "Select images",
                            tint = Color(0xFF001F54)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (isEdit && selectedImages.isEmpty() && existingImageUrls.isNotEmpty()) {
                    Text(
                        text = "Current images",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        items(existingImageUrls) { url ->
                            AsyncImage(
                                model = url,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(RoundedCornerShape(8.dp))
                            )
                        }
                    }
                }

                // ---------- Selected images (RecyclerView jaisa, LazyRow) ----------
                if (selectedImages.isNotEmpty()) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(selectedImages) { uri ->
                            ItemImageSelection(
                                imageUri = uri,
                                onRemoveClick = {
                                    selectedImages = selectedImages.filter { it != uri }
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {

                        val imagesForValidation =
                            if (isEdit && selectedImages.isEmpty() && existingImageUrls.isNotEmpty())
                                listOf(Uri.EMPTY)
                            else selectedImages

                        val product = Utils.validateProduct(
                            context, title, quantity, unit, price, stock, category, type, imagesForValidation
                        )
                        if (product != null) {
                            if (isEdit) {
                                val id = oldId
                                if (id != null) {
                                    productViewModel.updateProduct(
                                        oldId = id,
                                        oldCategory = oldCategory,
                                        oldType = oldType,
                                        newProduct = product,
                                        newImages = selectedImages,
                                        existingUrls = existingImageUrls,
                                        onSuccess = {
                                            Utils.showToast(context, "Product updated")
                                            navController.popBackStack()
                                        },
                                        onError = { Utils.showToast(context, it) }
                                    )
                                }
                            } else {
                                productViewModel.saveProduct(
                                    product, selectedImages,
                                    onSuccess = {
                                        Utils.showToast(context, "Product added")
                                        title = ""; quantity = ""; unit = ""; price = ""
                                        stock = ""; category = ""; type = ""
                                        selectedImages = emptyList()
                                    },
                                    onError = { Utils.showToast(context, it) }
                                )
                            }
                        }
                    },
                    enabled = !productViewModel.isLoading.value && !(isEdit && oldId == null),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF001F54))
                ) {
                    Text(
                        text = if (isEdit) "Save Changes" else "Add Product",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (productViewModel.isLoading.value) {
                    Spacer(modifier = Modifier.height(8.dp))
                    CircularProgressIndicator()
                }
            }
        }
    }
}
