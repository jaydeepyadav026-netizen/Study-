package com.example.ui.screens.focus

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
import com.example.ui.components.GlowCard
import com.example.ui.theme.*
import com.example.viewmodel.LifeOsViewModel

@Composable
fun FocusScreen(
    viewModel: LifeOsViewModel,
    modifier: Modifier = Modifier
) {
    val focusTimeRemaining by viewModel.focusTimeRemaining.collectAsState()
    val focusInitialTime by viewModel.focusInitialTime.collectAsState()
    val isRunning by viewModel.isFocusRunning.collectAsState()
    val taskName by viewModel.focusTaskName.collectAsState()
    val recentLogs by viewModel.recentFocusLogs.collectAsState()

    var customTaskName by remember { mutableStateOf(taskName) }

    val progress = if (focusInitialTime > 0) {
        (focusTimeRemaining.toFloat() / focusInitialTime.toFloat()).coerceIn(0f, 1f)
    } else 0f

    val minutes = focusTimeRemaining / 60
    val seconds = focusTimeRemaining % 60

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Distraction-Free Focus",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Zero clutter. Deep cognitive immersion.",
            fontSize = 11.sp,
            color = TextMuted
        )
        Spacer(modifier = Modifier.height(20.dp))

        // Timer Dial
        Surface(
            shape = CircleShape,
            color = DarkSlateSurface,
            border = androidx.compose.foundation.BorderStroke(4.dp, if (isRunning) ElectricViolet else DarkSlateBorder),
            modifier = Modifier.size(220.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = String.format("%02d:%02d", minutes, seconds),
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isRunning) "FOCUSING" else "PAUSED",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isRunning) ElectricCyan else TextMuted,
                        letterSpacing = 1.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Preset Duration Chips
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(15 to "15m", 25 to "25m (Pomodoro)", 45 to "45m", 60 to "60m").forEach { (mins, label) ->
                FilterChip(
                    selected = focusInitialTime == mins * 60,
                    onClick = { viewModel.setFocusPreset(mins, customTaskName) },
                    label = { Text(label, fontSize = 11.sp) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Task Name Input
        OutlinedTextField(
            value = customTaskName,
            onValueChange = {
                customTaskName = it
                viewModel.setFocusPreset(focusInitialTime / 60, it)
            },
            label = { Text("Focus Goal / Task Name") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("focus_task_input")
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Action Buttons
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Button(
                onClick = {
                    if (isRunning) viewModel.pauseFocusTimer()
                    else viewModel.startFocusTimer()
                },
                colors = ButtonDefaults.buttonColors(containerColor = if (isRunning) AccentAmber else ElectricViolet),
                modifier = Modifier.height(48.dp).testTag("focus_start_pause_button")
            ) {
                Icon(if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text(if (isRunning) "Pause" else "Start Focus")
            }

            if (focusTimeRemaining < focusInitialTime) {
                OutlinedButton(
                    onClick = { viewModel.finishFocusEarly() },
                    modifier = Modifier.height(48.dp)
                ) {
                    Icon(Icons.Default.Done, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Finish Early")
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Focus Session History
        if (recentLogs.isNotEmpty()) {
            Text("Recent Focus Sessions", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextWhite)
            Spacer(modifier = Modifier.height(8.dp))
            LazyColumn(
                modifier = Modifier.fillMaxWidth().weight(1f),
                contentPadding = PaddingValues(bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(recentLogs, key = { it.id }) { log ->
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = DarkSlateSurface,
                        border = androidx.compose.foundation.BorderStroke(1.dp, DarkSlateBorder.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(log.taskTitle, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = TextWhite)
                                Text(log.mode, fontSize = 10.sp, color = TextMuted)
                            }
                            Text("${log.durationMinutes} min", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = ElectricCyan)
                        }
                    }
                }
            }
        }
    }
}
