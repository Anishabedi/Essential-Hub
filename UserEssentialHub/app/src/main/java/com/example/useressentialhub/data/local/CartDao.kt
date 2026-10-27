package com.example.useressentialhub.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface CartDao {

    // Sirf is user ke cart wali rows
    @Query("SELECT * FROM cart_items WHERE userUid = :uid")
    suspend fun getAll(uid: String): List<CartEntity>

    // Naya item ya purane item ki quantity: dono ek hi call se (REPLACE)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: CartEntity)

    @Query("DELETE FROM cart_items WHERE userUid = :uid AND productId = :productId")
    suspend fun delete(uid: String, productId: String)

    @Query("DELETE FROM cart_items WHERE userUid = :uid")
    suspend fun clear(uid: String)
}