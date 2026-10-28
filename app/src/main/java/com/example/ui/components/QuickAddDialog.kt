package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.local.entity.*
import com.example.ui.theme.*
import com.example.viewmodel.LifeOsViewModel
import java.time.LocalDate

enum class QuickActionType {
    NONE, TASK, EXPENSE, INCOME, EVENT, NOTE, DEBT, STUDY
}

@Composable
fun QuickAddModal(
    viewModel: LifeOsViewModel,
    onDismiss: () -> Unit
) {
    var selectedAction by remember { mutableStateOf(QuickActionType.NONE) }

    if (selectedAction == QuickActionType.NONE) {
        Dialog(onDismissRequest = onDismiss) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface,
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkSlateBorder),
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Quick Create",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }
                    Text(
                        text = "Add to your personal operating system",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    val actions = listOf(
                        Triple("New Task", Icons.Default.CheckCircle, QuickActionType.TASK),
                        Triple("Log Expense", Icons.Default.TrendingDown, QuickActionType.EXPENSE),
                        Triple("Add Income", Icons.Default.TrendingUp, QuickActionType.INCOME),
                        Triple("New Event", Icons.Default.Event, QuickActionType.EVENT),
                        Triple("Quick Note", Icons.Default.EditNote, QuickActionType.NOTE),
                        Triple("Udhar / Debt", Icons.Default.ReceiptLong, QuickActionType.DEBT),
                        Triple("Study Session", Icons.Default.School, QuickActionType.STUDY)
                    )

                    actions.forEach { (label, icon, type) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { selectedAction = type }
                                .padding(horizontal = 12.dp, vertical = 10.dp)
                                .testTag("quick_action_${type.name.lowercase()}"),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(ElectricViolet.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = ElectricViolet,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = label,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    } else {
        when (selectedAction) {
            QuickActionType.TASK -> QuickTaskDialog(viewModel) { onDismiss() }
            QuickActionType.EXPENSE -> QuickTransactionDialog(viewModel, isExpense = true) { onDismiss() }
            QuickActionType.INCOME -> QuickTransactionDialog(viewModel, isExpense = false) { onDismiss() }
            QuickActionType.EVENT -> QuickEventDialog(viewModel) { onDismiss() }
            QuickActionType.NOTE -> QuickNoteDialog(viewModel) { onDismiss() }
            QuickActionType.DEBT -> QuickDebtDialog(viewModel) { onDismiss() }
            QuickActionType.STUDY -> QuickStudyDialog(viewModel) { onDismiss() }
            QuickActionType.NONE -> {}
        }
    }
}

@Composable
fun QuickTaskDialog(viewModel: LifeOsViewModel, onDismiss: () -> Unit) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Personal") }
    var priority by remember { mutableStateOf("Medium") }
    var time by remember { mutableStateOf("10:00") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkSlateBorder),
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("New Task", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Task Title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("quick_task_title")
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Notes / Details (Optional)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Priority Selector
                Text("Priority", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Low", "Medium", "High", "Urgent").forEach { p ->
                        FilterChip(
                            selected = priority == p,
                            onClick = { priority = p },
                            label = { Text(p, fontSize = 11.sp) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (title.isNotBlank()) {
                                viewModel.saveTask(
                                    TaskEntity(
                                        title = title.trim(),
                                        description = description.trim(),
                                        date = LocalDate.now().toString(),
                                        startTime = time,
                                        category = category,
                                        priority = priority
                                    )
                                )
                                onDismiss()
                            }
                        },
                        enabled = title.isNotBlank(),
                        modifier = Modifier.testTag("save_quick_task")
                    ) {
                        Text("Create Task")
                    }
                }
            }
        }
    }
}

@Composable
fun QuickTransactionDialog(viewModel: LifeOsViewModel, isExpense: Boolean, onDismiss: () -> Unit) {
    var amountText by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(if (isExpense) "Food" else "Salary") }
    val accounts by viewModel.allAccounts.collectAsState()
    var selectedAccountId by remember { mutableStateOf(accounts.firstOrNull()?.id ?: 1L) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkSlateBorder),
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = if (isExpense) "Record Expense" else "Add Income",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isExpense) AccentRed else AccentGreen
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it.filter { ch -> ch.isDigit() || ch == '.' } },
                    label = { Text("Amount (₹)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("transaction_amount_input")
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("transaction_desc_input")
                )
                Spacer(modifier = Modifier.height(8.dp))

                val categories = if (isExpense) {
                    listOf("Food", "Transport", "Shopping", "Education", "Bills", "Health", "Other")
                } else {
                    listOf("Salary", "Freelance", "Allowance", "Gifts", "Refunds", "Other")
                }

                Text("Category", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    categories.take(4).forEach { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat, fontSize = 11.sp) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val amount = amountText.toDoubleOrNull() ?: 0.0
                            if (amount > 0) {
                                viewModel.saveTransaction(
                                    TransactionEntity(
                                        type = if (isExpense) "Expense" else "Income",
                                        amount = amount,
                                        date = LocalDate.now().toString(),
                                        category = category,
                                        accountId = selectedAccountId,
                                        description = description.ifBlank { category }
                                    )
                                )
                                onDismiss()
                            }
                        },
                        enabled = (amountText.toDoubleOrNull() ?: 0.0) > 0,
                        modifier = Modifier.testTag("save_transaction_button")
                    ) {
                        Text("Record")
                    }
                }
            }
        }
    }
}

@Composable
fun QuickEventDialog(viewModel: LifeOsViewModel, onDismiss: () -> Unit) {
    var title by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var startTime by remember { mutableStateOf("14:00") }
    var endTime by remember { mutableStateOf("15:00") }
    var eventType by remember { mutableStateOf("Personal") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkSlateBorder),
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("New Calendar Event", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Event Title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Location / Link (Optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Personal", "Lecture", "Exam", "Appointment", "Bill").forEach { t ->
                        FilterChip(
                            selected = eventType == t,
                            onClick = { eventType = t },
                            label = { Text(t, fontSize = 11.sp) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (title.isNotBlank()) {
                                viewModel.saveEvent(
                                    EventEntity(
                                        title = title.trim(),
                                        date = LocalDate.now().toString(),
                                        startTime = startTime,
                                        endTime = endTime,
                                        eventType = eventType,
                                        location = location.trim()
                                    )
                                )
                                onDismiss()
                            }
                        },
                        enabled = title.isNotBlank()
                    ) {
                        Text("Add Event")
                    }
                }
            }
        }
    }
}

@Composable
fun QuickNoteDialog(viewModel: LifeOsViewModel, onDismiss: () -> Unit) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("General") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkSlateBorder),
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Quick Note", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("Content") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (title.isNotBlank() || content.isNotBlank()) {
                                viewModel.saveNote(
                                    NoteEntity(
                                        title = title.ifBlank { "Quick Note" },
                                        content = content,
                                        category = category
                                    )
                                )
                                onDismiss()
                            }
                        },
                        enabled = title.isNotBlank() || content.isNotBlank()
                    ) {
                        Text("Save Note")
                    }
                }
            }
        }
    }
}

@Composable
fun QuickDebtDialog(viewModel: LifeOsViewModel, onDismiss: () -> Unit) {
    var personName by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("Lent") } // Lent (Give), Borrowed (Take)
    var description by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkSlateBorder),
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Udhar / Debt Record", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = type == "Lent",
                        onClick = { type = "Lent" },
                        label = { Text("Money Lent (Owed to me)") }
                    )
                    FilterChip(
                        selected = type == "Borrowed",
                        onClick = { type = "Borrowed" },
                        label = { Text("Money Borrowed (I owe)") }
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = personName,
                    onValueChange = { personName = it },
                    label = { Text("Person Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it.filter { ch -> ch.isDigit() || ch == '.' } },
                    label = { Text("Amount (₹)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Reason / Notes") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val amount = amountText.toDoubleOrNull() ?: 0.0
                            if (personName.isNotBlank() && amount > 0) {
                                viewModel.saveDebt(
                                    DebtEntity(
                                        personName = personName.trim(),
                                        type = type,
                                        originalAmount = amount,
                                        outstandingAmount = amount,
                                        transactionDate = LocalDate.now().toString(),
                                        dueDate = LocalDate.now().plusDays(7).toString(),
                                        description = description.trim()
                                    )
                                )
                                onDismiss()
                            }
                        },
                        enabled = personName.isNotBlank() && (amountText.toDoubleOrNull() ?: 0.0) > 0
                    ) {
                        Text("Record")
                    }
                }
            }
        }
    }
}

@Composable
fun QuickStudyDialog(viewModel: LifeOsViewModel, onDismiss: () -> Unit) {
    val subjects by viewModel.allSubjects.collectAsState()
    var selectedSubject by remember { mutableStateOf(subjects.firstOrNull()?.name ?: "Discrete Mathematics") }
    var durationMinutes by remember { mutableStateOf("60") }
    var notes by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkSlateBorder),
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Log Study Session", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))

                Text("Subject", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val availableSubjects = if (subjects.isNotEmpty()) subjects.map { it.name } else listOf("Mathematics", "Computer Science", "Physics")
                    availableSubjects.take(3).forEach { s ->
                        FilterChip(
                            selected = selectedSubject == s,
                            onClick = { selectedSubject = s },
                            label = { Text(s, fontSize = 11.sp) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = durationMinutes,
                    onValueChange = { durationMinutes = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Duration (Minutes)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Topics Covered / Session Notes") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val mins = durationMinutes.toIntOrNull() ?: 45
                            viewModel.logStudySession(
                                subjectName = selectedSubject,
                                subjectId = 0L,
                                durationMinutes = mins,
                                notes = notes.trim()
                            )
                            onDismiss()
                        }
                    ) {
                        Text("Log Session")
                    }
                }
            }
        }
    }
}
