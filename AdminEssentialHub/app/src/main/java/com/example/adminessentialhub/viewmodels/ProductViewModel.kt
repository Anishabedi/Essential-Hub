package com.example.adminessentialhub.viewmodel

import android.net.Uri
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.example.adminessentialhub.models.Product
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener
import java.util.UUID
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

class ProductViewModel : ViewModel() {

    var isLoading = mutableStateOf(false)
        private set

    // Selected category ke products
    var categoryProducts = mutableStateOf<List<Product>>(emptyList())
        private set

    // "All" chip ke liye: max 60 products, random order mein
    var allProducts = mutableStateOf<List<Product>>(emptyList())
        private set


    // true jab tak Firebase se list ka pehla jawab nahi aata (khaali list aur loading ko alag karne ke liye)
    var isListLoading = mutableStateOf(false)
        private set

    // Search ke liye: sabhi categories ke sabhi products (60 ki limit ke bina)
    var searchPool = mutableStateOf<List<Product>>(emptyList())
        private set

    var isSearchLoading = mutableStateOf(false)
        private set

    private var currentRef: Query? = null
    private var currentListener: ValueEventListener? = null

    private var allRef: Query? = null
    private var allListener: ValueEventListener? = null

    // ---------------- ADD ----------------
    fun saveProduct(
        product: Product,
        images: List<Uri>,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (images.isEmpty()) {
            onError("Please select at least one image")
            return
        }

        isLoading.value = true

        uploadImages(
            images = images,
            onDone = { urls ->
                product.productImageUris = ArrayList(urls)
                saveToDatabase(product, onSuccess, onError)
            },
            onFail = { msg ->
                isLoading.value = false
                onError(msg)
            }
        )
    }

    // ---------------- EDIT ----------------
    fun getProduct(id: String, onResult: (Product?) -> Unit) {
        FirebaseDatabase.getInstance().getReference("Admin/AllProducts/$id")
            .get()
            .addOnSuccessListener { onResult(it.getValue(Product::class.java)) }
            .addOnFailureListener { onResult(null) }
    }

    fun updateProduct(
        oldId: String,
        oldCategory: String?,
        oldType: String?,
        newProduct: Product,
        newImages: List<Uri>,
        existingUrls: List<String>,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        isLoading.value = true

        if (newImages.isEmpty()) {
            // Nayi image nahi chuni: purani images rakho
            newProduct.productImageUris = ArrayList(existingUrls)
            updateInDatabase(oldId, oldCategory, oldType, newProduct, onSuccess, onError)
            return
        }

        uploadImages(
            images = newImages,
            onDone = { urls ->
                newProduct.productImageUris = ArrayList(urls)
                updateInDatabase(oldId, oldCategory, oldType, newProduct, onSuccess, onError)
            },
            onFail = { msg ->
                isLoading.value = false
                onError(msg)
            }
        )
    }

    private fun updateInDatabase(
        oldId: String,
        oldCategory: String?,
        oldType: String?,
        newProduct: Product,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        // id wahi rehni chahiye, warna edit ke naam pe naya product ban jayega
        newProduct.productRandomId = oldId

        val updates = hashMapOf<String, Any?>(
            "Admin/AllProducts/$oldId" to newProduct,
            "Admin/ProductCategory/${newProduct.productCategory}/$oldId" to newProduct,
            "Admin/ProductType/${newProduct.productType}/$oldId" to newProduct
        )

        // Category ya type badli ho toh purani jagah se hatao (null = delete)
        if (!oldCategory.isNullOrEmpty() && oldCategory != newProduct.productCategory) {
            updates["Admin/ProductCategory/$oldCategory/$oldId"] = null
        }
        if (!oldType.isNullOrEmpty() && oldType != newProduct.productType) {
            updates["Admin/ProductType/$oldType/$oldId"] = null
        }

        FirebaseDatabase.getInstance().reference
            .updateChildren(updates)
            .addOnSuccessListener {
                isLoading.value = false
                onSuccess()
            }
            .addOnFailureListener {
                isLoading.value = false
                onError(it.message ?: "Update failed")
            }
    }

    // ---------------- Cloudinary upload (add aur edit dono use karte hain) ----------------
    private fun uploadImages(
        images: List<Uri>,
        onDone: (List<String>) -> Unit,
        onFail: (String) -> Unit
    ) {
        val urls = arrayOfNulls<String>(images.size)
        val doneCount = AtomicInteger(0)
        val failed = AtomicBoolean(false)

        images.forEachIndexed { index, uri ->
            MediaManager.get().upload(uri)
                .option("folder", "essential_hub/products")
                .option("public_id", UUID.randomUUID().toString())
                .callback(object : UploadCallback {
                    override fun onStart(requestId: String) {}
                    override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {}
                    override fun onReschedule(requestId: String, error: ErrorInfo) {}

                    override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                        urls[index] = resultData["secure_url"] as? String
                        if (doneCount.incrementAndGet() == images.size && !failed.get()) {
                            val cleanUrls = urls.filterNotNull()
                            if (cleanUrls.isEmpty()) {
                                onFail("Image upload failed")
                            } else {
                                onDone(cleanUrls)
                            }
                        }
                    }

                    override fun onError(requestId: String, error: ErrorInfo) {
                        android.util.Log.e("CLOUDINARY", "code=${error.code} desc=${error.description}")
                        if (failed.compareAndSet(false, true)) {
                            onFail(error.description ?: "Image upload failed")
                        }
                    }
                })
                .dispatch()
        }
    }

    // ---------------- Listing ----------------
    fun loadCategory(categoryName: String) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        // Ek time pe ek hi listener
        allListener?.let { allRef?.removeEventListener(it) }
        currentListener?.let { currentRef?.removeEventListener(it) }

        // Pichhli category ka purana data na dikhe, aur "No product" ka message beech mein na chamke
        categoryProducts.value = emptyList()
        isListLoading.value = true


        val query = FirebaseDatabase.getInstance()
            .getReference("Admin/ProductCategory/$categoryName")
            .orderByChild("adminUid")
            .equalTo(uid)

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                categoryProducts.value =
                    snapshot.children.mapNotNull { it.getValue(Product::class.java) }
                isListLoading.value = false
            }
            override fun onCancelled(error: DatabaseError) {
                isListLoading.value = false
            }
        }

        query.addValueEventListener(listener)
        currentRef = query
        currentListener = listener
    }

    fun loadAllProducts() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        currentListener?.let { currentRef?.removeEventListener(it) }
        allListener?.let { allRef?.removeEventListener(it) }

        val query = FirebaseDatabase.getInstance()
            .getReference("Admin/AllProducts")
            .orderByChild("adminUid")
            .equalTo(uid)
            .limitToFirst(60)

        isListLoading.value = true
        val start = System.currentTimeMillis()

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                allProducts.value = snapshot.children
                    .mapNotNull { it.getValue(Product::class.java) }
                    .shuffled()
                isListLoading.value = false

                android.util.Log.d("PERF", "Data aaya: ${System.currentTimeMillis() - start} ms")
            }
            override fun onCancelled(error: DatabaseError) {
                isListLoading.value = false
            }
        }

        query.addValueEventListener(listener)
        allRef = query
        allListener = listener
    }

    // Ek baar padhta hai (listener nahi), isliye data bhaari nahi chalta rehta
    fun loadSearchPool() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        isSearchLoading.value = true

        FirebaseDatabase.getInstance().getReference("Admin/AllProducts")
            .orderByChild("adminUid")
            .equalTo(uid)
            .get()
            .addOnSuccessListener { snapshot ->
                searchPool.value = snapshot.children
                    .mapNotNull { it.getValue(Product::class.java) }
                isSearchLoading.value = false
            }
            .addOnFailureListener {
                isSearchLoading.value = false
            }
    }

    override fun onCleared() {
        currentListener?.let { currentRef?.removeEventListener(it) }
        allListener?.let { allRef?.removeEventListener(it) }
    }

    private fun saveToDatabase(
        product: Product,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val id = product.productRandomId
        val updates = hashMapOf<String, Any?>(
            "Admin/AllProducts/$id" to product,
            "Admin/ProductCategory/${product.productCategory}/$id" to product,
            "Admin/ProductType/${product.productType}/$id" to product
        )

        FirebaseDatabase.getInstance().reference
            .updateChildren(updates)
            .addOnSuccessListener {
                isLoading.value = false
                onSuccess()
            }
            .addOnFailureListener {
                isLoading.value = false
                onError(it.message ?: "Save failed")
            }
    }
}