package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "accounts")
data class AccountEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val type: String = "Bank", // Cash, Bank, Savings, Wallet, Other
    val openingBalance: Double = 0.0,
    val currency: String = "₹",
    val colorHex: String = "#8B5CF6",
    val isDefault: Boolean = false
)

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: String, // Income, Expense, Transfer
    val amount: Double,
    val date: String, // YYYY-MM-DD
    val category: String, // Food, Transport, Education, Shopping, Bills, Salary, Freelance, etc.
    val accountId: Long,
    val targetAccountId: Long? = null, // for Transfer
    val description: String = "",
    val paymentMethod: String = "UPI / Card",
    val relatedPerson: String = "",
    val receiptUri: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "budgets")
data class BudgetEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val category: String,
    val monthlyLimit: Double,
    val currency: String = "₹"
)
