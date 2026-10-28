package com.example.ui.screens.money

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.local.entity.AccountEntity
import com.example.data.local.entity.TransactionEntity
import com.example.ui.components.GlowCard
import com.example.ui.components.QuickTransactionDialog
import com.example.ui.theme.*
import com.example.viewmodel.LifeOsViewModel
import java.time.LocalDate

@Composable
fun MoneyScreen(
    viewModel: LifeOsViewModel,
    modifier: Modifier = Modifier
) {
    val accounts by viewModel.allAccounts.collectAsState()
    val transactions by viewModel.allTransactions.collectAsState()
    val currency by viewModel.currency.collectAsState()

    var showAddTransactionDialog by remember { mutableStateOf(false) }
    var isExpenseTransaction by remember { mutableStateOf(true) }
    var showAccountDialog by remember { mutableStateOf(false) }
    var selectedCategoryFilter by remember { mutableStateOf<String?>(null) }
    var showCsvExportDialog by remember { mutableStateOf(false) }

    // Account balances calculation
    val accountBalances = remember(accounts, transactions) {
        accounts.associate { acc ->
            val income = transactions.filter { it.accountId == acc.id && it.type == "Income" }.sumOf { it.amount }
            val expense = transactions.filter { it.accountId == acc.id && it.type == "Expense" }.sumOf { it.amount }
            val transferIn = transactions.filter { it.targetAccountId == acc.id && it.type == "Transfer" }.sumOf { it.amount }
            val transferOut = transactions.filter { it.accountId == acc.id && it.type == "Transfer" }.sumOf { it.amount }
            acc.id to (acc.openingBalance + income + transferIn - expense - transferOut)
        }
    }

    val totalNetAssets = remember(accountBalances) {
        accountBalances.values.sum()
    }

    val currentMonthPrefix = remember { LocalDate.now().toString().take(7) }
    val monthTransactions = remember(transactions, currentMonthPrefix) {
        transactions.filter { it.date.startsWith(currentMonthPrefix) }
    }
    val totalIncome = remember(monthTransactions) {
        monthTransactions.filter { it.type == "Income" }.sumOf { it.amount }
    }
    val totalExpense = remember(monthTransactions) {
        monthTransactions.filter { it.type == "Expense" }.sumOf { it.amount }
    }

    // Category breakdown
    val categoryExpenses = remember(monthTransactions) {
        monthTransactions
            .filter { it.type == "Expense" }
            .groupBy { it.category }
            .mapValues { entry -> entry.value.sumOf { it.amount } }
            .toList()
            .sortedByDescending { it.second }
    }

    // Filtered transactions
    val filteredTransactions = remember(transactions, selectedCategoryFilter) {
        if (selectedCategoryFilter == null) transactions
        else transactions.filter { it.category.equals(selectedCategoryFilter, ignoreCase = true) }
    }

    Scaffold(
        floatingActionButton = {
            Column(
                modifier = Modifier.padding(bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalAlignment = Alignment.End
            ) {
                FloatingActionButton(
                    onClick = {
                        isExpenseTransaction = false
                        showAddTransactionDialog = true
                    },
                    containerColor = AccentGreen,
                    contentColor = Color.White,
                    shape = CircleShape,
                    modifier = Modifier.size(44.dp).testTag("fab_add_income")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Income")
                }
                FloatingActionButton(
                    onClick = {
                        isExpenseTransaction = true
                        showAddTransactionDialog = true
                    },
                    containerColor = AccentRed,
                    contentColor = Color.White,
                    shape = CircleShape,
                    modifier = Modifier.testTag("fab_add_expense")
                ) {
                    Icon(Icons.Default.Remove, contentDescription = "Expense")
                }
            }
        },
        containerColor = Color.Transparent
    ) { padding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 8.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header with CSV Export button
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Money Manager",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "Personal cash ledger & tracked accounts",
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    }

                    OutlinedButton(
                        onClick = { showCsvExportDialog = true },
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Export CSV", fontSize = 11.sp)
                    }
                }
            }

            // Total Net Assets Card
            item {
                GlowCard(borderColor = ElectricCyan) {
                    Text("Total Net Assets", fontSize = 12.sp, color = TextMuted)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "$currency${"%,.2f".format(totalNetAssets)}",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = DarkSlateSurfaceVariant,
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("Monthly Income", fontSize = 10.sp, color = TextMuted)
                                Text("+$currency${"%,.0f".format(totalIncome)}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = AccentGreen)
                            }
                        }
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = DarkSlateSurfaceVariant,
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("Monthly Expense", fontSize = 10.sp, color = TextMuted)
                                Text("-$currency${"%,.0f".format(totalExpense)}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = AccentRed)
                            }
                        }
                    }
                }
            }

            // Accounts Carousel
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Tracked Accounts", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    TextButton(onClick = { showAccountDialog = true }) {
                        Text("+ Add Account", fontSize = 12.sp, color = ElectricViolet)
                    }
                }

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    items(accounts, key = { it.id }) { acc ->
                        val balance = accountBalances[acc.id] ?: acc.openingBalance
                        val isLowBalance = balance < 1000.0

                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = DarkSlateSurface,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isLowBalance) AccentAmber.copy(alpha = 0.5f) else DarkSlateBorder
                            ),
                            modifier = Modifier.width(180.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(acc.name, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextWhite, maxLines = 1)
                                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(if (acc.type == "Cash") AccentGreen else ElectricViolet))
                                }
                                Text(acc.type, fontSize = 10.sp, color = TextMuted)
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "$currency${"%,.2f".format(balance)}",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isLowBalance) AccentAmber else TextWhite
                                )
                                if (isLowBalance) {
                                    Text("Low Balance Warning", fontSize = 9.sp, color = AccentAmber)
                                }
                            }
                        }
                    }
                }
            }

            // Category Breakdown Chart (Compose Native Canvas/Progress)
            if (categoryExpenses.isNotEmpty()) {
                item {
                    GlowCard {
                        Text("Expense Breakdown (This Month)", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(10.dp))

                        val totalExp = totalExpense.coerceAtLeast(1.0)
                        categoryExpenses.take(5).forEach { (cat, amount) ->
                            val pct = (amount / totalExp).toFloat()
                            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(cat, fontSize = 12.sp, color = TextWhite)
                                    Text("$currency${"%,.0f".format(amount)} (${(pct * 100).toInt()}%)", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextMuted)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                LinearProgressIndicator(
                                    progress = { pct },
                                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
                                    color = when (cat.lowercase()) {
                                        "food" -> AccentAmber
                                        "education" -> ElectricViolet
                                        "transport" -> ElectricCyan
                                        "shopping" -> AccentRed
                                        else -> AccentGreen
                                    },
                                    trackColor = DarkSlateSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // Transactions History
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Transactions", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    if (selectedCategoryFilter != null) {
                        TextButton(onClick = { selectedCategoryFilter = null }) {
                            Text("Clear filter (${selectedCategoryFilter})", fontSize = 11.sp, color = ElectricCyan)
                        }
                    }
                }
            }

            if (filteredTransactions.isEmpty()) {
                item {
                    Text("No transactions recorded yet.", fontSize = 13.sp, color = TextMuted, modifier = Modifier.padding(vertical = 12.dp))
                }
            } else {
                items(filteredTransactions, key = { it.id }) { tx ->
                    val isExp = tx.type == "Expense"
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = DarkSlateSurface,
                        border = androidx.compose.foundation.BorderStroke(1.dp, DarkSlateBorder.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(if (isExp) AccentRed.copy(alpha = 0.15f) else AccentGreen.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isExp) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                                    contentDescription = null,
                                    tint = if (isExp) AccentRed else AccentGreen,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(tx.description.ifBlank { tx.category }, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TextWhite)
                                Text("${tx.date} • ${tx.category} • ${tx.paymentMethod}", fontSize = 11.sp, color = TextMuted)
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "${if (isExp) "-" else "+"}$currency${"%,.2f".format(tx.amount)}",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isExp) AccentRed else AccentGreen
                                )
                            }

                            IconButton(
                                onClick = { viewModel.deleteTransaction(tx) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = TextMuted, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }
    }

    // Add Transaction Dialog
    if (showAddTransactionDialog) {
        QuickTransactionDialog(
            viewModel = viewModel,
            isExpense = isExpenseTransaction,
            onDismiss = { showAddTransactionDialog = false }
        )
    }

    // Add Account Dialog
    if (showAccountDialog) {
        var accountName by remember { mutableStateOf("") }
        var accountType by remember { mutableStateOf("Bank") }
        var initialBalance by remember { mutableStateOf("") }

        Dialog(onDismissRequest = { showAccountDialog = false }) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkSlateBorder),
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Add Account", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = accountName,
                        onValueChange = { accountName = it },
                        label = { Text("Account Name (e.g. HDFC Bank, Cash)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = initialBalance,
                        onValueChange = { initialBalance = it.filter { ch -> ch.isDigit() || ch == '.' } },
                        label = { Text("Opening Balance (₹)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("Bank", "Cash", "Savings", "Wallet").forEach { t ->
                            FilterChip(
                                selected = accountType == t,
                                onClick = { accountType = t },
                                label = { Text(t, fontSize = 11.sp) }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { showAccountDialog = false }) { Text("Cancel") }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (accountName.isNotBlank()) {
                                    viewModel.saveAccount(
                                        AccountEntity(
                                            name = accountName.trim(),
                                            type = accountType,
                                            openingBalance = initialBalance.toDoubleOrNull() ?: 0.0,
                                            currency = currency
                                        )
                                    )
                                    showAccountDialog = false
                                }
                            },
                            enabled = accountName.isNotBlank()
                        ) {
                            Text("Create")
                        }
                    }
                }
            }
        }
    }

    // CSV Export Dialog
    if (showCsvExportDialog) {
        Dialog(onDismissRequest = { showCsvExportDialog = false }) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkSlateBorder),
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = AccentGreen)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("CSV Ledger Export", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "LIFE OS has prepared your ${transactions.size} transactions for CSV backup.\n\nSummary:\n• Tracked Balance: $currency${"%,.2f".format(totalNetAssets)}\n• Total Income: $currency${"%,.2f".format(totalIncome)}\n• Total Expense: $currency${"%,.2f".format(totalExpense)}\n\nFormat: Date,Type,Category,Amount,Description",
                        fontSize = 13.sp,
                        color = TextMuted
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { showCsvExportDialog = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Export Ready (Close)")
                    }
                }
            }
        }
    }
}
