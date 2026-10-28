package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "shopping_items")
data class ShoppingItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val quantity: String = "1",
    val estimatedPrice: Double = 0.0,
    val category: String = "Groceries", // Groceries, Essentials, Electronics, Clothes, Books, Other
    val priority: String = "Medium", // Low, Medium, High
    val store: String = "",
    val isPurchased: Boolean = false,
    val notes: String = ""
)

@Entity(tableName = "wishlist_items")
data class WishlistItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val description: String = "",
    val expectedPrice: Double = 0.0,
    val priority: String = "Medium", // High, Medium, Low
    val targetDate: String = "",
    val link: String = "",
    val savedAmount: Double = 0.0
)
