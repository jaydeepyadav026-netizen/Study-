package com.example.ui.screens.goals

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import com.example.data.local.entity.GoalEntity
import com.example.data.local.entity.HabitEntity
import com.example.ui.components.GlowCard
import com.example.ui.theme.*
import com.example.viewmodel.LifeOsViewModel

enum class GoalsHabitsTab {
    HABITS, GOALS
}

@Composable
fun GoalsScreen(
    viewModel: LifeOsViewModel,
    modifier: Modifier = Modifier
) {
    val goals by viewModel.allGoals.collectAsState()
    val habits by viewModel.allHabits.collectAsState()
    val todayHabitLogs by viewModel.todayHabitLogs.collectAsState()

    var activeTab by remember { mutableStateOf(GoalsHabitsTab.HABITS) }
    var showAddGoalDialog by remember { mutableStateOf(false) }
    var showAddHabitDialog by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (activeTab == GoalsHabitsTab.HABITS) showAddHabitDialog = true
                    else showAddGoalDialog = true
                },
                containerColor = ElectricViolet,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.padding(bottom = 80.dp).testTag("fab_add_goal_habit")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        },
        containerColor = Color.Transparent
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                Text(
                    text = "Goals & Habits",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Daily consistency, streaks, and long-term milestones",
                    fontSize = 11.sp,
                    color = TextMuted
                )
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    FilterChip(
                        selected = activeTab == GoalsHabitsTab.HABITS,
                        onClick = { activeTab = GoalsHabitsTab.HABITS },
                        label = { Text("Daily Habits", fontSize = 12.sp) }
                    )
                    FilterChip(
                        selected = activeTab == GoalsHabitsTab.GOALS,
                        onClick = { activeTab = GoalsHabitsTab.GOALS },
                        label = { Text("Life Goals", fontSize = 12.sp) }
                    )
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = 8.dp, bottom = 120.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (activeTab == GoalsHabitsTab.HABITS) {
                    if (habits.isEmpty()) {
                        item {
                            Text("No habits registered. Tap + to start a streak!", fontSize = 13.sp, color = TextMuted, modifier = Modifier.padding(vertical = 20.dp))
                        }
                    } else {
                        items(habits, key = { it.id }) { habit ->
                            val isCompletedToday = todayHabitLogs.any { it.habitId == habit.id }

                            GlowCard(borderColor = if (isCompletedToday) AccentGreen else ElectricCyan) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Checkoff circle button
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(CircleShape)
                                            .background(if (isCompletedToday) AccentGreen else DarkSlateSurfaceVariant)
                                            .clickable { viewModel.toggleHabitToday(habit, isCompletedToday) }
                                            .testTag("toggle_habit_${habit.id}"),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = if (isCompletedToday) Icons.Default.Check else Icons.Default.LocalFireDepartment,
                                            contentDescription = "Toggle",
                                            tint = if (isCompletedToday) Color.White else AccentAmber,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(14.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(habit.title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = "${habit.currentStreak} day streak 🔥",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = AccentAmber
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "Best: ${habit.bestStreak}d",
                                                fontSize = 11.sp,
                                                color = TextMuted
                                            )
                                        }
                                    }

                                    IconButton(onClick = { viewModel.deleteHabit(habit) }) {
                                        Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = TextMuted)
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // Goals
                    if (goals.isEmpty()) {
                        item {
                            Text("No goals created yet. Tap + to set a target!", fontSize = 13.sp, color = TextMuted, modifier = Modifier.padding(vertical = 20.dp))
                        }
                    } else {
                        items(goals, key = { it.id }) { goal ->
                            val progress = if (goal.targetValue > 0) (goal.currentValue / goal.targetValue).toFloat().coerceIn(0f, 1f) else 0f
                            val isAchieved = progress >= 1f

                            GlowCard(borderColor = if (isAchieved) AccentGreen else ElectricViolet) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(goal.title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                                        if (goal.description.isNotBlank()) {
                                            Text(goal.description, fontSize = 12.sp, color = TextMuted)
                                        }
                                        if (goal.targetDate.isNotBlank()) {
                                            Text("Target: ${goal.targetDate}", fontSize = 11.sp, color = ElectricCyan)
                                        }
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isAchieved) AccentGreen.copy(alpha = 0.2f) else ElectricViolet.copy(alpha = 0.2f)
                                    ) {
                                        Text(
                                            text = "${(progress * 100).toInt()}%",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isAchieved) AccentGreen else ElectricVioletLight,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }

                                    IconButton(onClick = { viewModel.deleteGoal(goal) }) {
                                        Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = TextMuted)
                                    }
                                }
                                Spacer(modifier = Modifier.height(10.dp))

                                LinearProgressIndicator(
                                    progress = { progress },
                                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                                    color = if (isAchieved) AccentGreen else ElectricViolet,
                                    trackColor = DarkSlateSurfaceVariant
                                )

                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${goal.currentValue.toInt()} / ${goal.targetValue.toInt()} (${goal.progressType})",
                                        fontSize = 11.sp,
                                        color = TextSubtle
                                    )

                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        IconButton(
                                            onClick = {
                                                val step = if (goal.progressType == "Percent") 5.0 else 1.0
                                                val newVal = (goal.currentValue + step).coerceAtMost(goal.targetValue)
                                                viewModel.saveGoal(goal.copy(currentValue = newVal))
                                            },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(Icons.Default.Add, contentDescription = "Increment", tint = ElectricCyan, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Add Habit Dialog
    if (showAddHabitDialog) {
        var title by remember { mutableStateOf("") }
        Dialog(onDismissRequest = { showAddHabitDialog = false }) {
            Surface(shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surface, border = androidx.compose.foundation.BorderStroke(1.dp, DarkSlateBorder), modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Add Daily Habit", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Habit Title (e.g. 20 min Reading, Drink 3L Water)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("habit_title_input")
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { showAddHabitDialog = false }) { Text("Cancel") }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (title.isNotBlank()) {
                                    viewModel.saveHabit(HabitEntity(title = title.trim()))
                                    showAddHabitDialog = false
                                }
                            },
                            enabled = title.isNotBlank()
                        ) {
                            Text("Create")
                        }
                    }
                }
            }
        }
    }

    // Add Goal Dialog
    if (showAddGoalDialog) {
        var title by remember { mutableStateOf("") }
        var description by remember { mutableStateOf("") }
        var targetValue by remember { mutableStateOf("100") }
        var targetDate by remember { mutableStateOf("2026-12-31") }
        var progressType by remember { mutableStateOf("Percent") }

        Dialog(onDismissRequest = { showAddGoalDialog = false }) {
            Surface(shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surface, border = androidx.compose.foundation.BorderStroke(1.dp, DarkSlateBorder), modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("New Goal", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Goal Title") }, singleLine = true, modifier = Modifier.fillMaxWidth().testTag("goal_title_input"))
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = targetValue, onValueChange = { targetValue = it.filter { ch -> ch.isDigit() } }, label = { Text("Target Value") }, singleLine = true, modifier = Modifier.weight(1f))
                        OutlinedTextField(value = targetDate, onValueChange = { targetDate = it }, label = { Text("Target Date") }, singleLine = true, modifier = Modifier.weight(1f))
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { showAddGoalDialog = false }) { Text("Cancel") }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (title.isNotBlank()) {
                                    viewModel.saveGoal(
                                        GoalEntity(
                                            title = title.trim(),
                                            description = description.trim(),
                                            targetDate = targetDate.trim(),
                                            targetValue = targetValue.toDoubleOrNull() ?: 100.0,
                                            progressType = progressType
                                        )
                                    )
                                    showAddGoalDialog = false
                                }
                            },
                            enabled = title.isNotBlank()
                        ) {
                            Text("Create Goal")
                        }
                    }
                }
            }
        }
    }
}
