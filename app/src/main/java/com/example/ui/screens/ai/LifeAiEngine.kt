package com.example.ui.screens.ai

import java.time.LocalDate
import java.util.regex.Pattern

sealed class AiParsedResult {
    data class TaskAction(
        val title: String,
        val date: String,
        val time: String,
        val category: String,
        val priority: String
    ) : AiParsedResult()

    data class TransactionAction(
        val type: String, // Income, Expense
        val amount: Double,
        val category: String,
        val description: String,
        val date: String
    ) : AiParsedResult()

    data class DebtAction(
        val person: String,
        val type: String, // Lent, Borrowed
        val amount: Double,
        val dueDate: String,
        val description: String
    ) : AiParsedResult()

    data class StudySessionAction(
        val subject: String,
        val plannedMinutes: Int,
        val date: String
    ) : AiParsedResult()

    data class ShoppingItemAction(
        val name: String,
        val estimatedPrice: Double,
        val quantity: String
    ) : AiParsedResult()

    data class SummaryQuery(
        val queryType: String // "pending_tasks", "spending_summary", "general"
    ) : AiParsedResult()

    data class Unknown(val message: String) : AiParsedResult()
}

object LifeAiEngine {

    fun parseUserCommand(input: String): AiParsedResult {
        val text = input.trim()
        val lower = text.lowercase()
        val today = LocalDate.now().toString()
        val tomorrow = LocalDate.now().plusDays(1).toString()

        // 1. Spending summary / Pending tasks queries
        if (lower.contains("pending task") || lower.contains("pending kaam") || lower.contains("tasks dikhao") || lower.contains("show my tasks")) {
            return AiParsedResult.SummaryQuery("pending_tasks")
        }
        if (lower.contains("summarize my spending") || lower.contains("spending this month") || lower.contains("kharcha batao") || lower.contains("total expense")) {
            return AiParsedResult.SummaryQuery("spending_summary")
        }

        // 2. Money / Expense / Income: e.g. "Add ₹200 transport expense today" or "200 rs kharcha for auto"
        val amountRegex = "(?:₹|rs\\.?|inr)?\\s*([0-9]+(?:\\.[0-9]+)?)\\s*(?:₹|rs\\.?|inr|rupees)?"
        val isExpense = lower.contains("expense") || lower.contains("spent") || lower.contains("kharcha") || lower.contains("paid") || lower.contains("diye")
        val isIncome = lower.contains("income") || lower.contains("received") || lower.contains("salary") || lower.contains("aaye") || lower.contains("kamaye")

        if (isExpense || isIncome) {
            val matcher = Pattern.compile("(\\d+(?:\\.\\d+)?)").matcher(lower)
            if (matcher.find()) {
                val amount = matcher.group(1)?.toDoubleOrNull() ?: 0.0
                var category = "Other"
                if (lower.contains("food") || lower.contains("lunch") || lower.contains("dinner") || lower.contains("khana")) category = "Food"
                else if (lower.contains("transport") || lower.contains("auto") || lower.contains("cab") || lower.contains("bus") || lower.contains("metro") || lower.contains("petrol")) category = "Transport"
                else if (lower.contains("shopping") || lower.contains("cloth") || lower.contains("shoes")) category = "Shopping"
                else if (lower.contains("education") || lower.contains("book") || lower.contains("college") || lower.contains("fee")) category = "Education"
                else if (lower.contains("bill") || lower.contains("recharge") || lower.contains("electricity")) category = "Bills"

                val date = if (lower.contains("kal") || lower.contains("tomorrow")) tomorrow else today
                val type = if (isIncome) "Income" else "Expense"
                return AiParsedResult.TransactionAction(
                    type = type,
                    amount = amount,
                    category = category,
                    description = text,
                    date = date
                )
            }
        }

        // 3. Udhar / Debt: "Rahul se ₹500 lene hain next Friday" or "Borrowed 1000 from Rohit"
        val isLent = lower.contains("lene hain") || lower.contains("lent") || lower.contains("gave to") || lower.contains("diye the")
        val isBorrowed = lower.contains("dene hain") || lower.contains("borrowed") || lower.contains("liye the") || lower.contains("owe")

        if (isLent || isBorrowed) {
            val matcher = Pattern.compile("(\\d+(?:\\.\\d+)?)").matcher(lower)
            val amount = if (matcher.find()) matcher.group(1)?.toDoubleOrNull() ?: 0.0 else 0.0

            // Extract person name
            val words = text.split(" ")
            var person = "Friend"
            for (i in words.indices) {
                val w = words[i]
                if (w.equals("se", ignoreCase = true) || w.equals("ko", ignoreCase = true) || w.equals("from", ignoreCase = true) || w.equals("to", ignoreCase = true)) {
                    if (i > 0) person = words[i - 1].replace(Regex("[^a-zA-Z]"), "")
                }
            }

            val dueDate = if (lower.contains("kal") || lower.contains("tomorrow")) tomorrow else LocalDate.now().plusDays(7).toString()
            return AiParsedResult.DebtAction(
                person = person.ifBlank { "Contact" },
                type = if (isLent) "Lent" else "Borrowed",
                amount = amount,
                dueDate = dueDate,
                description = text
            )
        }

        // 4. Study Session: "Create a study session for mathematics tomorrow" or "Maths padhna hai kal"
        if (lower.contains("study session") || lower.contains("padhna") || lower.contains("study for")) {
            var subject = "General Study"
            if (lower.contains("math")) subject = "Discrete Mathematics"
            else if (lower.contains("operating system") || lower.contains("os")) subject = "Operating Systems"
            else if (lower.contains("network")) subject = "Computer Networks"
            else if (lower.contains("database") || lower.contains("dbms")) subject = "Database Systems"

            val date = if (lower.contains("kal") || lower.contains("tomorrow")) tomorrow else today
            return AiParsedResult.StudySessionAction(
                subject = subject,
                plannedMinutes = 60,
                date = date
            )
        }

        // 5. Shopping list: "Create a shopping list for tomorrow" or "Buy milk and bread"
        if (lower.contains("shopping") || lower.contains("buy ") || lower.contains("khareedna")) {
            val itemName = text.replace(Regex("(?i)add|buy|create|shopping|list|for|tomorrow|kal|to"), "").trim()
            return AiParsedResult.ShoppingItemAction(
                name = if (itemName.isNotBlank()) itemName else "Shopping Item",
                estimatedPrice = 100.0,
                quantity = "1"
            )
        }

        // 6. Reminder / Task: "Kal subah 8 baje college ke liye reminder lagao"
        if (lower.contains("reminder") || lower.contains("task") || lower.contains("yad dilao") || lower.contains("lagao")) {
            val date = if (lower.contains("kal") || lower.contains("tomorrow")) tomorrow else today
            var time = "09:00"
            val timeMatcher = Pattern.compile("(\\d{1,2})\\s*(?:baje|am|pm|:)").matcher(lower)
            if (timeMatcher.find()) {
                val hour = timeMatcher.group(1)?.toIntOrNull() ?: 9
                time = String.format("%02d:00", hour)
            }

            var category = "Personal"
            if (lower.contains("college") || lower.contains("assignment")) category = "College"
            else if (lower.contains("padh") || lower.contains("study")) category = "Study"
            else if (lower.contains("office") || lower.contains("work")) category = "Work"

            val title = text.replace(Regex("(?i)reminder|lagao|yad dilao|kal|subah|shaam|baje|set"), "").trim()
            return AiParsedResult.TaskAction(
                title = if (title.isNotBlank()) title else "Personal Reminder",
                date = date,
                time = time,
                category = category,
                priority = if (lower.contains("urgent") || lower.contains("important") || lower.contains("zaroori")) "Urgent" else "High"
            )
        }

        return AiParsedResult.Unknown("Could not recognize specific action. Try: 'Add ₹200 transport expense' or 'Kal subah 8 baje college reminder lagao'.")
    }
}
