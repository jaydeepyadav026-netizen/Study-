package com.example.ui.screens.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.local.entity.EventEntity
import com.example.ui.components.GlowCard
import com.example.ui.theme.*
import com.example.viewmodel.LifeOsViewModel
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

enum class CalendarViewMode {
    MONTH, WEEK, AGENDA
}

@Composable
fun CalendarScreen(
    viewModel: LifeOsViewModel,
    modifier: Modifier = Modifier
) {
    val allEvents by viewModel.allEvents.collectAsState()
    val allTasks by viewModel.allTasks.collectAsState()
    val selectedDate by viewModel.selectedCalendarDate.collectAsState()

    var viewMode by remember { mutableStateOf(CalendarViewMode.MONTH) }
    var currentYearMonth by remember { mutableStateOf(YearMonth.now()) }
    var showCreateEventDialog by remember { mutableStateOf(false) }

    val selectedLocalDate = remember(selectedDate) {
        try { LocalDate.parse(selectedDate) } catch (e: Exception) { LocalDate.now() }
    }

    // Events and tasks for the selected date
    val dayEvents = remember(allEvents, selectedDate) {
        allEvents.filter { it.date == selectedDate }
    }
    val dayTasks = remember(allTasks, selectedDate) {
        allTasks.filter { it.date == selectedDate }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateEventDialog = true },
                containerColor = ElectricCyan,
                contentColor = Color.Black,
                shape = CircleShape,
                modifier = Modifier.padding(bottom = 80.dp).testTag("fab_add_event")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Event")
            }
        },
        containerColor = Color.Transparent
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Header with month navigation
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = currentYearMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Local offline storage active",
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { currentYearMonth = currentYearMonth.minusMonths(1) }) {
                        Icon(Icons.Default.ChevronLeft, contentDescription = "Prev Month")
                    }
                    IconButton(onClick = { currentYearMonth = currentYearMonth.plusMonths(1) }) {
                        Icon(Icons.Default.ChevronRight, contentDescription = "Next Month")
                    }
                }
            }

            // View Mode Selector
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    CalendarViewMode.MONTH to "Month",
                    CalendarViewMode.WEEK to "Week",
                    CalendarViewMode.AGENDA to "Agenda"
                ).forEach { (mode, label) ->
                    FilterChip(
                        selected = viewMode == mode,
                        onClick = { viewMode = mode },
                        label = { Text(label, fontSize = 12.sp) }
                    )
                }
            }

            // Month View Grid
            if (viewMode == CalendarViewMode.MONTH) {
                // Days of week header
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun").forEach { day ->
                        Text(
                            text = day,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextMuted,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // Month Calendar Days Grid
                val firstDayOfMonth = currentYearMonth.atDay(1)
                val dayOfWeekOffset = (firstDayOfMonth.dayOfWeek.value - 1) // Mon = 0, Sun = 6
                val daysInMonth = currentYearMonth.lengthOfMonth()
                val totalCells = ((dayOfWeekOffset + daysInMonth + 6) / 7) * 7

                LazyVerticalGrid(
                    columns = GridCells.Fixed(7),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    userScrollEnabled = false
                ) {
                    items(totalCells) { index ->
                        val dayNumber = index - dayOfWeekOffset + 1
                        val isValidDay = dayNumber in 1..daysInMonth
                        val cellDateStr = if (isValidDay) currentYearMonth.atDay(dayNumber).toString() else ""
                        val isSelected = isValidDay && cellDateStr == selectedDate
                        val isToday = isValidDay && cellDateStr == LocalDate.now().toString()

                        val hasEvents = isValidDay && allEvents.any { it.date == cellDateStr }
                        val hasTasks = isValidDay && allTasks.any { it.date == cellDateStr }

                        Box(
                            modifier = Modifier
                                .aspectRatio(1f)
                                .padding(2.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    when {
                                        isSelected -> ElectricViolet.copy(alpha = 0.3f)
                                        isToday -> ElectricCyan.copy(alpha = 0.15f)
                                        else -> Color.Transparent
                                    }
                                )
                                .then(
                                    if (isSelected) Modifier.border(1.dp, ElectricViolet, RoundedCornerShape(8.dp))
                                    else if (isToday) Modifier.border(1.dp, ElectricCyan, RoundedCornerShape(8.dp))
                                    else Modifier
                                )
                                .clickable(enabled = isValidDay) {
                                    if (isValidDay) viewModel.selectCalendarDate(cellDateStr)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isValidDay) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "$dayNumber",
                                        fontSize = 13.sp,
                                        fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) TextWhite else if (isToday) ElectricCyan else TextWhite.copy(alpha = 0.9f)
                                    )
                                    // Dots indicator
                                    if (hasEvents || hasTasks) {
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(2.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            if (hasEvents) {
                                                Box(modifier = Modifier.size(4.dp).clip(CircleShape).background(AccentAmber))
                                            }
                                            if (hasTasks) {
                                                Box(modifier = Modifier.size(4.dp).clip(CircleShape).background(ElectricCyan))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Selected Day Agenda / Event List
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "Schedule for ${selectedLocalDate.format(DateTimeFormatter.ofPattern("EEE, MMM d, yyyy"))}",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(bottom = 120.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (dayEvents.isEmpty() && dayTasks.isEmpty()) {
                    item {
                        Text(
                            text = "No events or tasks scheduled for this day.",
                            fontSize = 13.sp,
                            color = TextMuted,
                            modifier = Modifier.padding(vertical = 12.dp)
                        )
                    }
                }

                // Events
                items(dayEvents, key = { "event_${it.id}" }) { event ->
                    GlowCard(borderColor = AccentAmber) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(AccentAmber.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Event, contentDescription = null, tint = AccentAmber, modifier = Modifier.size(18.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(event.title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextWhite)
                                Text("${event.startTime} - ${event.endTime} • ${event.eventType}", fontSize = 12.sp, color = TextMuted)
                                if (event.location.isNotBlank()) {
                                    Text(event.location, fontSize = 11.sp, color = ElectricCyan)
                                }
                            }
                            IconButton(onClick = { viewModel.deleteEvent(event) }) {
                                Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = TextMuted, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }

                // Tasks for this date
                items(dayTasks, key = { "task_${it.id}" }) { task ->
                    GlowCard(borderColor = ElectricCyan) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = task.isCompleted,
                                onCheckedChange = { viewModel.toggleTaskCompletion(task) },
                                colors = CheckboxDefaults.colors(checkedColor = AccentGreen)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(task.title, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TextWhite)
                                Text("Task • ${task.startTime.ifBlank { "Anytime" }} • ${task.category}", fontSize = 11.sp, color = TextMuted)
                            }
                        }
                    }
                }
            }
        }
    }

    // New Event Dialog
    if (showCreateEventDialog) {
        var title by remember { mutableStateOf("") }
        var description by remember { mutableStateOf("") }
        var eventDate by remember { mutableStateOf(selectedDate) }
        var startTime by remember { mutableStateOf("10:00") }
        var endTime by remember { mutableStateOf("11:00") }
        var eventType by remember { mutableStateOf("Personal") }
        var location by remember { mutableStateOf("") }

        Dialog(onDismissRequest = { showCreateEventDialog = false }) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkSlateBorder),
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Add Calendar Event", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Event Title") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("event_title_input")
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = startTime,
                            onValueChange = { startTime = it },
                            label = { Text("Start Time") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = endTime,
                            onValueChange = { endTime = it },
                            label = { Text("End Time") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = location,
                        onValueChange = { location = it },
                        label = { Text("Location (Optional)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Event Type", fontSize = 12.sp, color = TextMuted)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("Personal", "Lecture", "Exam", "Bill", "Meeting").forEach { t ->
                            FilterChip(
                                selected = eventType == t,
                                onClick = { eventType = t },
                                label = { Text(t, fontSize = 10.sp) }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { showCreateEventDialog = false }) { Text("Cancel") }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (title.isNotBlank()) {
                                    viewModel.saveEvent(
                                        EventEntity(
                                            title = title.trim(),
                                            description = description.trim(),
                                            date = eventDate,
                                            startTime = startTime,
                                            endTime = endTime,
                                            eventType = eventType,
                                            location = location.trim()
                                        )
                                    )
                                    showCreateEventDialog = false
                                }
                            },
                            enabled = title.isNotBlank()
                        ) {
                            Text("Save Event")
                        }
                    }
                }
            }
        }
    }
}
