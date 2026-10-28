package com.example.ui.screens.home

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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.local.entity.NoteEntity
import com.example.data.local.entity.TaskEntity
import com.example.ui.components.GlowCard
import com.example.ui.components.PriorityBadge
import com.example.ui.theme.*
import com.example.viewmodel.AppTab
import com.example.viewmodel.DashboardWidgetConfig
import com.example.viewmodel.LifeOsViewModel
import com.example.viewmodel.SubScreen
import java.time.LocalDate

@Composable
fun HomeScreen(
    viewModel: LifeOsViewModel,
    modifier: Modifier = Modifier
) {
    val tasks by viewModel.todayTasks.collectAsState()
    val overdueTasks by viewModel.overdueTasks.collectAsState()
    val upcomingEvents by viewModel.upcomingEvents.collectAsState()
    val accounts by viewModel.allAccounts.collectAsState()
    val transactions by viewModel.allTransactions.collectAsState()
    val debts by viewModel.allDebts.collectAsState()
    val studySessions by viewModel.allStudySessions.collectAsState()
    val exams by viewModel.allExams.collectAsState()
    val assignments by viewModel.allAssignments.collectAsState()
    val goals by viewModel.allGoals.collectAsState()
    val dashboardConfig by viewModel.dashboardConfig.collectAsState()
    val currency by viewModel.currency.collectAsState()

    var showCustomizeDialog by remember { mutableStateOf(false) }

    // Financial calculations
    val totalRecordedBalance = remember(accounts, transactions) {
        val opening = accounts.sumOf { it.openingBalance }
        val income = transactions.filter { it.type == "Income" }.sumOf { it.amount }
        val expense = transactions.filter { it.type == "Expense" }.sumOf { it.amount }
        opening + income - expense
    }

    val currentMonthPrefix = remember { LocalDate.now().toString().take(7) }
    val monthTransactions = remember(transactions, currentMonthPrefix) {
        transactions.filter { it.date.startsWith(currentMonthPrefix) }
    }
    val monthIncome = remember(monthTransactions) {
        monthTransactions.filter { it.type == "Income" }.sumOf { it.amount }
    }
    val monthExpense = remember(monthTransactions) {
        monthTransactions.filter { it.type == "Expense" }.sumOf { it.amount }
    }

    val totalReceivable = remember(debts) {
        debts.filter { it.type == "Lent" && it.status != "Settled" }.sumOf { it.outstandingAmount }
    }
    val totalPayable = remember(debts) {
        debts.filter { it.type == "Borrowed" && it.status != "Settled" }.sumOf { it.outstandingAmount }
    }

    // Study minutes this week
    val totalStudyMinutes = remember(studySessions) {
        studySessions.sumOf { it.actualMinutes }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Tagline & Quick Stats Bar
        item {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkSlateBorder.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "COMMAND CENTER",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = ElectricCyan,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Your Life. Your Rules. Your System.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Customize dashboard button
                    TextButton(
                        onClick = { showCustomizeDialog = true },
                        modifier = Modifier.testTag("customize_dashboard_button")
                    ) {
                        Icon(
                            Icons.Default.Tune,
                            contentDescription = "Customize",
                            tint = ElectricViolet,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Customize", fontSize = 12.sp, color = ElectricViolet)
                    }
                }
            }
        }

        // Overdue Alert Banner if any
        if (overdueTasks.isNotEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = AccentRed.copy(alpha = 0.12f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AccentRed.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.WarningAmber,
                            contentDescription = "Overdue",
                            tint = AccentRed,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${overdueTasks.size} Overdue Task${if (overdueTasks.size > 1) "s" else ""}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = AccentRed
                            )
                            Text(
                                text = "Action required: reschedule or mark completed",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        TextButton(onClick = { viewModel.navigateToTab(AppTab.PLANNER) }) {
                            Text("Resolve", fontSize = 12.sp, color = AccentRed)
                        }
                    }
                }
            }
        }

        // Daily Summary Card
        if (dashboardConfig.showSummary) {
            item {
                val completedToday = tasks.count { it.isCompleted }
                val totalToday = tasks.size
                GlowCard(borderColor = ElectricViolet) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Daily Summary",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Surface(
                            shape = CircleShape,
                            color = ElectricViolet.copy(alpha = 0.2f),
                            modifier = Modifier.padding(4.dp)
                        ) {
                            Text(
                                text = "$completedToday / $totalToday Done",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = ElectricVioletLight,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Stat 1: Tasks
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.weight(1f).padding(vertical = 4.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("Tasks Remaining", fontSize = 10.sp, color = TextMuted)
                                Text("${totalToday - completedToday}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                            }
                        }
                        // Stat 2: Today's Spending
                        val todaySpending = transactions.filter { it.date == LocalDate.now().toString() && it.type == "Expense" }.sumOf { it.amount }
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.weight(1f).padding(vertical = 4.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("Today's Spend", fontSize = 10.sp, color = TextMuted)
                                Text("$currency${"%,.0f".format(todaySpending)}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = AccentRed)
                            }
                        }
                        // Stat 3: Study Logged
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.weight(1f).padding(vertical = 4.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("Study Time", fontSize = 10.sp, color = TextMuted)
                                Text("${totalStudyMinutes}m", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = ElectricCyan)
                            }
                        }
                    }
                }
            }
        }

        // Widget A: Today's Tasks
        if (dashboardConfig.showTasks) {
            item {
                GlowCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircleOutline, contentDescription = null, tint = ElectricViolet, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Today's Tasks", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                        TextButton(onClick = { viewModel.navigateToTab(AppTab.PLANNER) }) {
                            Text("View All", fontSize = 12.sp, color = ElectricViolet)
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))

                    if (tasks.isEmpty()) {
                        Text(
                            text = "No tasks scheduled for today. Tap + to add one!",
                            fontSize = 12.sp,
                            color = TextMuted,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    } else {
                        tasks.take(4).forEach { task ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { viewModel.toggleTaskCompletion(task) }
                                    .padding(vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = task.isCompleted,
                                    onCheckedChange = { viewModel.toggleTaskCompletion(task) },
                                    colors = CheckboxDefaults.colors(checkedColor = AccentGreen)
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = task.title,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null,
                                        color = if (task.isCompleted) TextMuted else TextWhite
                                    )
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        if (task.startTime.isNotBlank()) {
                                            Text(task.startTime, fontSize = 11.sp, color = ElectricCyan)
                                        }
                                        Text(task.category, fontSize = 11.sp, color = TextMuted)
                                    }
                                }
                                PriorityBadge(task.priority)
                            }
                        }
                    }
                }
            }
        }

        // Widget B: Money Overview
        if (dashboardConfig.showMoney) {
            item {
                GlowCard(borderColor = ElectricCyan) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = ElectricCyan, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Money Overview", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                        TextButton(onClick = { viewModel.navigateToTab(AppTab.MONEY) }) {
                            Text("Details", fontSize = 12.sp, color = ElectricCyan)
                        }
                    }
                    Text(
                        text = "Total Recorded Balance",
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                    Text(
                        text = "$currency${"%,.2f".format(totalRecordedBalance)}",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    // Income vs Expense this month
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = DarkSlateSurfaceVariant,
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.ArrowDownward, contentDescription = null, tint = AccentGreen, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text("Income (Month)", fontSize = 10.sp, color = TextMuted)
                                    Text("$currency${"%,.0f".format(monthIncome)}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = AccentGreen)
                                }
                            }
                        }
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = DarkSlateSurfaceVariant,
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.ArrowUpward, contentDescription = null, tint = AccentRed, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text("Expense (Month)", fontSize = 10.sp, color = TextMuted)
                                    Text("$currency${"%,.0f".format(monthExpense)}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = AccentRed)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    // Udhar split row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Owed to you: $currency${"%,.0f".format(totalReceivable)}",
                            fontSize = 11.sp,
                            color = AccentGreen
                        )
                        Text(
                            text = "You owe: $currency${"%,.0f".format(totalPayable)}",
                            fontSize = 11.sp,
                            color = AccentRed
                        )
                    }
                }
            }
        }

        // Widget C: Upcoming Schedule
        if (dashboardConfig.showSchedule) {
            item {
                GlowCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CalendarToday, contentDescription = null, tint = AccentAmber, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Upcoming Schedule", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                        TextButton(onClick = { viewModel.navigateToTab(AppTab.CALENDAR) }) {
                            Text("Calendar", fontSize = 12.sp, color = AccentAmber)
                        }
                    }
                    if (upcomingEvents.isEmpty()) {
                        Text("No upcoming events booked.", fontSize = 12.sp, color = TextMuted, modifier = Modifier.padding(vertical = 4.dp))
                    } else {
                        upcomingEvents.take(3).forEach { event ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(AccentAmber)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(event.title, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = TextWhite)
                                    Text("${event.date} • ${event.startTime} - ${event.endTime}", fontSize = 11.sp, color = TextMuted)
                                }
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = DarkSlateSurfaceVariant
                                ) {
                                    Text(
                                        text = event.eventType,
                                        fontSize = 10.sp,
                                        color = ElectricCyan,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Widget D: Study & College Progress
        if (dashboardConfig.showStudy) {
            item {
                GlowCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.School, contentDescription = null, tint = ElectricViolet, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Study & Academics", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                        TextButton(onClick = { viewModel.navigateToTab(AppTab.STUDY) }) {
                            Text("Study Hub", fontSize = 12.sp, color = ElectricViolet)
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = DarkSlateSurfaceVariant,
                            modifier = Modifier.weight(1f).padding(vertical = 4.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("Upcoming Exams", fontSize = 11.sp, color = TextMuted)
                                Text("${exams.size}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = AccentAmber)
                            }
                        }
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = DarkSlateSurfaceVariant,
                            modifier = Modifier.weight(1f).padding(vertical = 4.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("Assignments Due", fontSize = 11.sp, color = TextMuted)
                                Text("${assignments.count { !it.isSubmitted }}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = ElectricCyan)
                            }
                        }
                    }
                }
            }
        }

        // Widget E: Active Goals
        if (dashboardConfig.showGoals) {
            item {
                GlowCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Flag, contentDescription = null, tint = AccentGreen, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Goal Progress", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                        TextButton(onClick = { viewModel.navigateToTab(AppTab.GOALS) }) {
                            Text("All Goals", fontSize = 12.sp, color = AccentGreen)
                        }
                    }
                    if (goals.isEmpty()) {
                        Text("No goals set yet.", fontSize = 12.sp, color = TextMuted)
                    } else {
                        goals.take(2).forEach { goal ->
                            val progress = if (goal.targetValue > 0) (goal.currentValue / goal.targetValue).toFloat().coerceIn(0f, 1f) else 0f
                            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(goal.title, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = TextWhite)
                                    Text("${(progress * 100).toInt()}%", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AccentGreen)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                LinearProgressIndicator(
                                    progress = { progress },
                                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
                                    color = AccentGreen,
                                    trackColor = DarkSlateSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        // Widget F: Quick Note Box
        if (dashboardConfig.showQuickNotes) {
            item {
                var quickNoteText by remember { mutableStateOf("") }
                GlowCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.EditNote, contentDescription = null, tint = ElectricCyan, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Instant Scratchpad", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = quickNoteText,
                        onValueChange = { quickNoteText = it },
                        placeholder = { Text("Jot down an idea or reminder instantly...", fontSize = 13.sp) },
                        modifier = Modifier.fillMaxWidth().testTag("quick_note_scratchpad_input"),
                        trailingIcon = {
                            if (quickNoteText.isNotBlank()) {
                                IconButton(
                                    onClick = {
                                        viewModel.saveNote(
                                            NoteEntity(
                                                title = "Scratchpad Note",
                                                content = quickNoteText.trim(),
                                                category = "Ideas"
                                            )
                                        )
                                        quickNoteText = ""
                                    },
                                    modifier = Modifier.testTag("save_scratchpad_button")
                                ) {
                                    Icon(Icons.Default.Send, contentDescription = "Save", tint = ElectricCyan)
                                }
                            }
                        }
                    )
                }
            }
        }
    }

    // Dashboard Customization Dialog
    if (showCustomizeDialog) {
        Dialog(onDismissRequest = { showCustomizeDialog = false }) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkSlateBorder),
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Customize Dashboard", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text("Show or hide widgets on your main view", fontSize = 12.sp, color = TextMuted)
                    Spacer(modifier = Modifier.height(14.dp))

                    val widgets = listOf(
                        "Summary Card" to dashboardConfig.showSummary,
                        "Today's Tasks" to dashboardConfig.showTasks,
                        "Money Overview" to dashboardConfig.showMoney,
                        "Upcoming Schedule" to dashboardConfig.showSchedule,
                        "Study & Academics" to dashboardConfig.showStudy,
                        "Goal Progress" to dashboardConfig.showGoals,
                        "Instant Scratchpad" to dashboardConfig.showQuickNotes
                    )

                    widgets.forEach { (name, enabled) ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(name, fontSize = 14.sp)
                            Switch(
                                checked = enabled,
                                onCheckedChange = { checked ->
                                    val newConfig = when (name) {
                                        "Summary Card" -> dashboardConfig.copy(showSummary = checked)
                                        "Today's Tasks" -> dashboardConfig.copy(showTasks = checked)
                                        "Money Overview" -> dashboardConfig.copy(showMoney = checked)
                                        "Upcoming Schedule" -> dashboardConfig.copy(showSchedule = checked)
                                        "Study & Academics" -> dashboardConfig.copy(showStudy = checked)
                                        "Goal Progress" -> dashboardConfig.copy(showGoals = checked)
                                        "Instant Scratchpad" -> dashboardConfig.copy(showQuickNotes = checked)
                                        else -> dashboardConfig
                                    }
                                    viewModel.updateDashboardConfig(newConfig)
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { showCustomizeDialog = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Done")
                    }
                }
            }
        }
    }
}
