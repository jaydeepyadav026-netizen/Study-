package com.example.ui.screens.udhar

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.data.local.entity.DebtEntity
import com.example.ui.components.GlowCard
import com.example.ui.components.QuickDebtDialog
import com.example.ui.theme.*
import com.example.viewmodel.LifeOsViewModel
import java.time.LocalDate

@Composable
fun UdharScreen(
    viewModel: LifeOsViewModel,
    modifier: Modifier = Modifier
) {
    val debts by viewModel.allDebts.collectAsState()
    val currency by viewModel.currency.collectAsState()

    var showAddDebtDialog by remember { mutableStateOf(false) }
    var repaymentDebt by remember { mutableStateOf<DebtEntity?>(null) }
    var selectedTypeFilter by remember { mutableStateOf<String?>(null) } // "Lent", "Borrowed" or null

    val totalLent = remember(debts) {
        debts.filter { it.type == "Lent" && it.status != "Settled" }.sumOf { it.outstandingAmount }
    }
    val totalBorrowed = remember(debts) {
        debts.filter { it.type == "Borrowed" && it.status != "Settled" }.sumOf { it.outstandingAmount }
    }

    val filteredDebts = remember(debts, selectedTypeFilter) {
        if (selectedTypeFilter == null) debts
        else debts.filter { it.type == selectedTypeFilter }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDebtDialog = true },
                containerColor = ElectricViolet,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.padding(bottom = 80.dp).testTag("fab_add_debt")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Debt")
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
            item {
                Text(
                    text = "Udhar & Debt Tracker",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Manage money lent & borrowed with partial repayment history",
                    fontSize = 11.sp,
                    color = TextMuted
                )
            }

            // Summary Card
            item {
                GlowCard(borderColor = ElectricViolet) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = AccentGreen.copy(alpha = 0.12f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, AccentGreen.copy(alpha = 0.3f)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("Owed to You (Lent)", fontSize = 11.sp, color = AccentGreen)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "$currency${"%,.2f".format(totalLent)}",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AccentGreen
                                )
                                Text("Receivable", fontSize = 10.sp, color = TextMuted)
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = AccentRed.copy(alpha = 0.12f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, AccentRed.copy(alpha = 0.3f)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("You Owe (Borrowed)", fontSize = 11.sp, color = AccentRed)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "$currency${"%,.2f".format(totalBorrowed)}",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AccentRed
                                )
                                Text("Payable", fontSize = 10.sp, color = TextMuted)
                            }
                        }
                    }
                }
            }

            // Filter Tabs
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedTypeFilter == null,
                        onClick = { selectedTypeFilter = null },
                        label = { Text("All Records") }
                    )
                    FilterChip(
                        selected = selectedTypeFilter == "Lent",
                        onClick = { selectedTypeFilter = "Lent" },
                        label = { Text("Money Lent") }
                    )
                    FilterChip(
                        selected = selectedTypeFilter == "Borrowed",
                        onClick = { selectedTypeFilter = "Borrowed" },
                        label = { Text("Money Borrowed") }
                    )
                }
            }

            if (filteredDebts.isEmpty()) {
                item {
                    Text("No debt records found.", fontSize = 13.sp, color = TextMuted, modifier = Modifier.padding(vertical = 12.dp))
                }
            } else {
                items(filteredDebts, key = { it.id }) { debt ->
                    val isLent = debt.type == "Lent"
                    val isSettled = debt.status == "Settled"
                    val isOverdue = !isSettled && debt.dueDate.isNotBlank() && debt.dueDate < LocalDate.now().toString()

                    GlowCard(borderColor = if (isOverdue) AccentRed else if (isLent) AccentGreen else AccentRed) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(if (isLent) AccentGreen.copy(alpha = 0.2f) else AccentRed.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isLent) Icons.Default.CallMade else Icons.Default.CallReceived,
                                    contentDescription = null,
                                    tint = if (isLent) AccentGreen else AccentRed,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(debt.personName, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = if (isSettled) AccentGreen.copy(alpha = 0.2f) else DarkSlateSurfaceVariant
                                    ) {
                                        Text(
                                            text = debt.status,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSettled) AccentGreen else ElectricCyan,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                if (debt.description.isNotBlank()) {
                                    Text(debt.description, fontSize = 12.sp, color = TextMuted)
                                }
                                Text(
                                    text = "Original: $currency${"%,.0f".format(debt.originalAmount)} • Repaid: $currency${"%,.0f".format(debt.repaidAmount)}",
                                    fontSize = 11.sp,
                                    color = TextSubtle
                                )
                                if (debt.dueDate.isNotBlank()) {
                                    Text(
                                        text = "Due: ${debt.dueDate}${if (isOverdue) " (OVERDUE)" else ""}",
                                        fontSize = 11.sp,
                                        color = if (isOverdue) AccentRed else TextMuted
                                    )
                                }
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "$currency${"%,.2f".format(debt.outstandingAmount)}",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSettled) AccentGreen else if (isLent) AccentGreen else AccentRed
                                )

                                if (!isSettled) {
                                    FilledTonalButton(
                                        onClick = { repaymentDebt = debt },
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                        modifier = Modifier.height(28.dp).padding(top = 4.dp).testTag("repay_button_${debt.id}")
                                    ) {
                                        Text("Repay", fontSize = 11.sp)
                                    }
                                }
                            }

                            IconButton(
                                onClick = { viewModel.deleteDebt(debt) },
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

    if (showAddDebtDialog) {
        QuickDebtDialog(viewModel = viewModel, onDismiss = { showAddDebtDialog = false })
    }

    // Repayment Dialog
    if (repaymentDebt != null) {
        val debt = repaymentDebt!!
        var repaymentAmountText by remember { mutableStateOf("") }
        var repaymentNote by remember { mutableStateOf("") }
        val accounts by viewModel.allAccounts.collectAsState()
        var selectedAccountId by remember { mutableStateOf<Long?>(accounts.firstOrNull()?.id) }

        Dialog(onDismissRequest = { repaymentDebt = null }) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkSlateBorder),
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Record Repayment", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text("For: ${debt.personName} (${debt.type})", fontSize = 12.sp, color = TextMuted)
                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Outstanding balance: $currency${"%,.2f".format(debt.outstandingAmount)}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = ElectricCyan
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = repaymentAmountText,
                        onValueChange = { repaymentAmountText = it.filter { ch -> ch.isDigit() || ch == '.' } },
                        label = { Text("Repayment Amount (₹)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("repayment_amount_input")
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = repaymentNote,
                        onValueChange = { repaymentNote = it },
                        label = { Text("Note (e.g. Part payment via UPI)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { repaymentDebt = null }) { Text("Cancel") }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val amount = repaymentAmountText.toDoubleOrNull() ?: 0.0
                                if (amount > 0 && amount <= debt.outstandingAmount) {
                                    viewModel.recordRepayment(
                                        debtId = debt.id,
                                        amount = amount,
                                        note = repaymentNote.ifBlank { "Partial repayment" },
                                        accountId = selectedAccountId
                                    )
                                    repaymentDebt = null
                                }
                            },
                            enabled = (repaymentAmountText.toDoubleOrNull() ?: 0.0) in 0.01..debt.outstandingAmount,
                            modifier = Modifier.testTag("submit_repayment_button")
                        ) {
                            Text("Confirm Repayment")
                        }
                    }
                }
            }
        }
    }
}
