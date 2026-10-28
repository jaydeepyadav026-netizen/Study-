package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "debts")
data class DebtEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val personName: String,
    val contactInfo: String = "",
    val type: String, // Lent (Owed to user), Borrowed (User owes)
    val originalAmount: Double,
    val repaidAmount: Double = 0.0,
    val outstandingAmount: Double, // originalAmount - repaidAmount
    val transactionDate: String, // YYYY-MM-DD
    val dueDate: String = "", // Expected repayment date
    val description: String = "",
    val status: String = "Pending", // Pending, Partially Repaid, Settled, Overdue
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "debt_repayments")
data class DebtRepaymentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val debtId: Long,
    val amount: Double,
    val date: String, // YYYY-MM-DD
    val note: String = "",
    val accountId: Long? = null // Optional linked financial account
)
