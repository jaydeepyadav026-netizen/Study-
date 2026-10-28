package com.example.ui.screens.planner

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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.local.entity.TaskEntity
import com.example.ui.components.GlowCard
import com.example.ui.components.PriorityBadge
import com.example.ui.theme.*
import com.example.viewmodel.LifeOsViewModel
import com.example.viewmodel.PlannerView
import java.time.LocalDate

@Composable
fun PlannerScreen(
    viewModel: LifeOsViewModel,
    modifier: Modifier = Modifier
) {
    val allTasks by viewModel.allTasks.collectAsState()
    val activeView by viewModel.plannerView.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryFilter by remember { mutableStateOf<String?>(null) }
    var selectedPriorityFilter by remember { mutableStateOf<String?>(null) }
    var editingTask by remember { mutableStateOf<TaskEntity?>(null) }
    var showCreateDialog by remember { mutableStateOf(false) }

    val today = remember { LocalDate.now().toString() }
    val tomorrow = remember { LocalDate.now().plusDays(1).toString() }
    val nextWeek = remember { LocalDate.now().plusDays(7).toString() }
    val thisMonth = remember { LocalDate.now().toString().take(7) }

    // Filter tasks based on view, category, priority, and search
    val filteredTasks = remember(allTasks, activeView, selectedCategoryFilter, selectedPriorityFilter, searchQuery) {
        allTasks.filter { task ->
            // View filter
            val matchesView = when (activeView) {
                PlannerView.TODAY -> task.date == today
                PlannerView.TOMORROW -> task.date == tomorrow
                PlannerView.THIS_WEEK -> task.date in today..nextWeek
                PlannerView.NEXT_WEEK -> task.date > nextWeek
                PlannerView.THIS_MONTH -> task.date.startsWith(thisMonth)
                PlannerView.ALL -> true
                PlannerView.OVERDUE -> !task.isCompleted && task.date < today
                PlannerView.COMPLETED -> task.isCompleted
            }
            // Category filter
            val matchesCategory = selectedCategoryFilter == null || task.category.equals(selectedCategoryFilter, ignoreCase = true)
            // Priority filter
            val matchesPriority = selectedPriorityFilter == null || task.priority.equals(selectedPriorityFilter, ignoreCase = true)
            // Search
            val matchesSearch = searchQuery.isBlank() || task.title.contains(searchQuery, ignoreCase = true) || task.description.contains(searchQuery, ignoreCase = true)

            matchesView && matchesCategory && matchesPriority && matchesSearch
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = ElectricViolet,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.padding(bottom = 80.dp).testTag("fab_add_task")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Task")
            }
        },
        containerColor = Color.Transparent
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Header & Search
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                Text(
                    text = "Smart Planner",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search tasks, descriptions...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextMuted) },
                    trailingIcon = {
                        if (searchQuery.isNotBlank()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear")
                            }
                        }
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("planner_search_input")
                )
            }

            // View Tabs Scrollable Row
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(PlannerView.values()) { view ->
                    val isSelected = activeView == view
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.setPlannerView(view) },
                        label = {
                            Text(
                                text = view.name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() },
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ElectricViolet,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            // Categories Filter Row
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                item {
                    AssistChip(
                        onClick = { selectedCategoryFilter = null },
                        label = { Text("All Categories", fontSize = 11.sp) },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = if (selectedCategoryFilter == null) ElectricCyan.copy(alpha = 0.2f) else Color.Transparent
                        )
                    )
                }
                val categories = listOf("Personal", "College", "Study", "Work", "Money", "Health", "Shopping")
                items(categories) { cat ->
                    val isCatSelected = selectedCategoryFilter == cat
                    AssistChip(
                        onClick = { selectedCategoryFilter = if (isCatSelected) null else cat },
                        label = { Text(cat, fontSize = 11.sp) },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = if (isCatSelected) ElectricCyan.copy(alpha = 0.2f) else Color.Transparent
                        )
                    )
                }
            }

            // Tasks List
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = 8.dp, bottom = 120.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (filteredTasks.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 40.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.TaskAlt,
                                contentDescription = null,
                                tint = TextMuted,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No tasks found in this view",
                                fontSize = 15.sp,
                                color = TextMuted
                            )
                            Text(
                                text = "Tap the + button to create a new task",
                                fontSize = 12.sp,
                                color = TextSubtle
                            )
                        }
                    }
                } else {
                    items(filteredTasks, key = { it.id }) { task ->
                        val isOverdue = !task.isCompleted && task.date < today
                        GlowCard(
                            borderColor = if (isOverdue) AccentRed else DarkSlateBorder,
                            onClick = { editingTask = task }
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = task.isCompleted,
                                    onCheckedChange = { viewModel.toggleTaskCompletion(task) },
                                    colors = CheckboxDefaults.colors(checkedColor = AccentGreen)
                                )
                                Spacer(modifier = Modifier.width(6.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = task.title,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null,
                                            color = if (task.isCompleted) TextMuted else TextWhite
                                        )
                                        if (isOverdue) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = AccentRed.copy(alpha = 0.2f)
                                            ) {
                                                Text(
                                                    text = "OVERDUE",
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = AccentRed,
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }

                                    if (task.description.isNotBlank()) {
                                        Text(
                                            text = task.description,
                                            fontSize = 12.sp,
                                            color = TextMuted,
                                            maxLines = 2
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(task.date, fontSize = 11.sp, color = TextSubtle)
                                        if (task.startTime.isNotBlank()) {
                                            Text(task.startTime, fontSize = 11.sp, color = ElectricCyan)
                                        }
                                        Text("•", fontSize = 10.sp, color = TextSubtle)
                                        Text(task.category, fontSize = 11.sp, color = ElectricVioletLight)
                                    }
                                }

                                PriorityBadge(task.priority)

                                IconButton(onClick = { viewModel.deleteTask(task) }) {
                                    Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = TextMuted, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Edit / Create Task Dialog
    if (showCreateDialog || editingTask != null) {
        val current = editingTask
        var title by remember(current) { mutableStateOf(current?.title ?: "") }
        var description by remember(current) { mutableStateOf(current?.description ?: "") }
        var date by remember(current) { mutableStateOf(current?.date ?: LocalDate.now().toString()) }
        var startTime by remember(current) { mutableStateOf(current?.startTime ?: "09:00") }
        var category by remember(current) { mutableStateOf(current?.category ?: "Personal") }
        var priority by remember(current) { mutableStateOf(current?.priority ?: "Medium") }
        var estimatedMinutes by remember(current) { mutableStateOf((current?.estimatedMinutes ?: 30).toString()) }

        Dialog(onDismissRequest = {
            showCreateDialog = false
            editingTask = null
        }) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkSlateBorder),
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = if (current != null) "Edit Task" else "Create Task",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Title") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("task_title_input")
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description") },
                        minLines = 2,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = date,
                            onValueChange = { date = it },
                            label = { Text("Date (YYYY-MM-DD)") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = startTime,
                            onValueChange = { startTime = it },
                            label = { Text("Time (HH:mm)") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))

                    Text("Priority", fontSize = 12.sp, color = TextMuted)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("Low", "Medium", "High", "Urgent").forEach { p ->
                            FilterChip(
                                selected = priority == p,
                                onClick = { priority = p },
                                label = { Text(p, fontSize = 10.sp) }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = {
                            showCreateDialog = false
                            editingTask = null
                        }) {
                            Text("Cancel")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (title.isNotBlank()) {
                                    val taskToSave = current?.copy(
                                        title = title.trim(),
                                        description = description.trim(),
                                        date = date.trim(),
                                        startTime = startTime.trim(),
                                        category = category,
                                        priority = priority,
                                        estimatedMinutes = estimatedMinutes.toIntOrNull() ?: 30
                                    ) ?: TaskEntity(
                                        title = title.trim(),
                                        description = description.trim(),
                                        date = date.trim(),
                                        startTime = startTime.trim(),
                                        category = category,
                                        priority = priority,
                                        estimatedMinutes = estimatedMinutes.toIntOrNull() ?: 30
                                    )
                                    viewModel.saveTask(taskToSave)
                                    showCreateDialog = false
                                    editingTask = null
                                }
                            },
                            enabled = title.isNotBlank(),
                            modifier = Modifier.testTag("save_task_submit")
                        ) {
                            Text(if (current != null) "Update" else "Create")
                        }
                    }
                }
            }
        }
    }
}
