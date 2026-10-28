package com.example.ui.screens.study

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
import com.example.data.local.entity.*
import com.example.ui.components.GlowCard
import com.example.ui.theme.*
import com.example.viewmodel.LifeOsViewModel
import kotlinx.coroutines.delay
import java.time.LocalDate

enum class StudyTab {
    TIMETABLE, SUBJECTS, ASSIGNMENTS, EXAMS, TIMER
}

@Composable
fun StudyScreen(
    viewModel: LifeOsViewModel,
    modifier: Modifier = Modifier
) {
    val subjects by viewModel.allSubjects.collectAsState()
    val timetableSlots by viewModel.allTimetableSlots.collectAsState()
    val assignments by viewModel.allAssignments.collectAsState()
    val exams by viewModel.allExams.collectAsState()
    val studySessions by viewModel.allStudySessions.collectAsState()

    var activeTab by remember { mutableStateOf(StudyTab.TIMETABLE) }
    var selectedDayOfWeek by remember { mutableStateOf(LocalDate.now().dayOfWeek.value) }

    // Dialogs
    var showSubjectDialog by remember { mutableStateOf(false) }
    var showAssignmentDialog by remember { mutableStateOf(false) }
    var showExamDialog by remember { mutableStateOf(false) }
    var showTimetableSlotDialog by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            if (activeTab != StudyTab.TIMER) {
                FloatingActionButton(
                    onClick = {
                        when (activeTab) {
                            StudyTab.TIMETABLE -> showTimetableSlotDialog = true
                            StudyTab.SUBJECTS -> showSubjectDialog = true
                            StudyTab.ASSIGNMENTS -> showAssignmentDialog = true
                            StudyTab.EXAMS -> showExamDialog = true
                            StudyTab.TIMER -> {}
                        }
                    },
                    containerColor = ElectricViolet,
                    contentColor = Color.White,
                    shape = CircleShape,
                    modifier = Modifier.padding(bottom = 80.dp).testTag("fab_add_study_item")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add")
                }
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
                    text = "Study & College Hub",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Classes, assignments, exams, and Pomodoro study timer",
                    fontSize = 11.sp,
                    color = TextMuted
                )
            }

            // Tab bar
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(StudyTab.values()) { tab ->
                    val isSelected = activeTab == tab
                    FilterChip(
                        selected = isSelected,
                        onClick = { activeTab = tab },
                        label = {
                            Text(
                                text = tab.name.lowercase().replaceFirstChar { it.uppercase() },
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

            when (activeTab) {
                StudyTab.TIMETABLE -> TimetableSection(timetableSlots, selectedDayOfWeek, onDaySelect = { selectedDayOfWeek = it }, onDeleteSlot = { viewModel.deleteTimetableSlot(it) })
                StudyTab.SUBJECTS -> SubjectsSection(subjects, onDeleteSubject = { viewModel.deleteSubject(it) })
                StudyTab.ASSIGNMENTS -> AssignmentsSection(assignments, onToggle = { viewModel.saveAssignment(it.copy(isSubmitted = !it.isSubmitted)) }, onDelete = { viewModel.deleteAssignment(it) })
                StudyTab.EXAMS -> ExamsSection(exams, onDelete = { viewModel.deleteExam(it) })
                StudyTab.TIMER -> StudyTimerSection(viewModel, subjects)
            }
        }
    }

    // Dialog for adding Subject
    if (showSubjectDialog) {
        var name by remember { mutableStateOf("") }
        var code by remember { mutableStateOf("") }
        var teacher by remember { mutableStateOf("") }
        var room by remember { mutableStateOf("") }
        var targetHours by remember { mutableStateOf("6") }

        Dialog(onDismissRequest = { showSubjectDialog = false }) {
            Surface(shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surface, border = androidx.compose.foundation.BorderStroke(1.dp, DarkSlateBorder), modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Add Subject", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Subject Name") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = code, onValueChange = { code = it }, label = { Text("Subject Code (e.g. CS-201)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = teacher, onValueChange = { teacher = it }, label = { Text("Professor / Teacher") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = room, onValueChange = { room = it }, label = { Text("Classroom / Lab") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { showSubjectDialog = false }) { Text("Cancel") }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(onClick = {
                            if (name.isNotBlank()) {
                                viewModel.saveSubject(SubjectEntity(name = name.trim(), code = code.trim(), teacher = teacher.trim(), classroom = room.trim(), targetWeeklyHours = targetHours.toDoubleOrNull() ?: 6.0))
                                showSubjectDialog = false
                            }
                        }) { Text("Add Subject") }
                    }
                }
            }
        }
    }

    // Dialog for adding Assignment
    if (showAssignmentDialog) {
        var title by remember { mutableStateOf("") }
        var dueDate by remember { mutableStateOf(LocalDate.now().plusDays(3).toString()) }
        var selectedSubject by remember { mutableStateOf(subjects.firstOrNull()?.name ?: "General") }

        Dialog(onDismissRequest = { showAssignmentDialog = false }) {
            Surface(shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surface, border = androidx.compose.foundation.BorderStroke(1.dp, DarkSlateBorder), modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Add Assignment", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Assignment Title") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = dueDate, onValueChange = { dueDate = it }, label = { Text("Due Date (YYYY-MM-DD)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { showAssignmentDialog = false }) { Text("Cancel") }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(onClick = {
                            if (title.isNotBlank()) {
                                viewModel.saveAssignment(AssignmentEntity(subjectId = 0L, subjectName = selectedSubject, title = title.trim(), dueDate = dueDate.trim()))
                                showAssignmentDialog = false
                            }
                        }) { Text("Save") }
                    }
                }
            }
        }
    }

    // Dialog for Exam
    if (showExamDialog) {
        var title by remember { mutableStateOf("") }
        var examDate by remember { mutableStateOf(LocalDate.now().plusDays(7).toString()) }
        var room by remember { mutableStateOf("Hall A") }
        var selectedSubject by remember { mutableStateOf(subjects.firstOrNull()?.name ?: "General") }

        Dialog(onDismissRequest = { showExamDialog = false }) {
            Surface(shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surface, border = androidx.compose.foundation.BorderStroke(1.dp, DarkSlateBorder), modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Add Examination", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Exam Name") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = examDate, onValueChange = { examDate = it }, label = { Text("Date (YYYY-MM-DD)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = room, onValueChange = { room = it }, label = { Text("Hall / Room") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { showExamDialog = false }) { Text("Cancel") }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(onClick = {
                            if (title.isNotBlank()) {
                                viewModel.saveExam(ExamEntity(subjectId = 0L, subjectName = selectedSubject, title = title.trim(), examDate = examDate.trim(), room = room.trim()))
                                showExamDialog = false
                            }
                        }) { Text("Save Exam") }
                    }
                }
            }
        }
    }

    // Dialog for Timetable Slot
    if (showTimetableSlotDialog) {
        var subjectName by remember { mutableStateOf(subjects.firstOrNull()?.name ?: "Mathematics") }
        var startTime by remember { mutableStateOf("09:00") }
        var endTime by remember { mutableStateOf("10:15") }
        var room by remember { mutableStateOf("Room 101") }

        Dialog(onDismissRequest = { showTimetableSlotDialog = false }) {
            Surface(shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surface, border = androidx.compose.foundation.BorderStroke(1.dp, DarkSlateBorder), modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Add Class Slot", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(value = subjectName, onValueChange = { subjectName = it }, label = { Text("Subject Name") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = startTime, onValueChange = { startTime = it }, label = { Text("Start Time") }, singleLine = true, modifier = Modifier.weight(1f))
                        OutlinedTextField(value = endTime, onValueChange = { endTime = it }, label = { Text("End Time") }, singleLine = true, modifier = Modifier.weight(1f))
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = room, onValueChange = { room = it }, label = { Text("Classroom") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { showTimetableSlotDialog = false }) { Text("Cancel") }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(onClick = {
                            if (subjectName.isNotBlank()) {
                                viewModel.saveTimetableSlot(TimetableSlotEntity(subjectId = 0L, subjectName = subjectName.trim(), dayOfWeek = selectedDayOfWeek, startTime = startTime.trim(), endTime = endTime.trim(), room = room.trim()))
                                showTimetableSlotDialog = false
                            }
                        }) { Text("Add Class") }
                    }
                }
            }
        }
    }
}

@Composable
fun TimetableSection(
    slots: List<TimetableSlotEntity>,
    selectedDay: Int,
    onDaySelect: (Int) -> Unit,
    onDeleteSlot: (TimetableSlotEntity) -> Unit
) {
    val dayNames = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    val daySlots = slots.filter { it.dayOfWeek == selectedDay }

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            dayNames.forEachIndexed { idx, name ->
                val dayNum = idx + 1
                val isSelected = dayNum == selectedDay
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) ElectricViolet else DarkSlateSurfaceVariant)
                        .clickable { onDaySelect(dayNum) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(name, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (isSelected) Color.White else TextMuted)
                }
            }
        }

        LazyColumn(contentPadding = PaddingValues(bottom = 120.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            if (daySlots.isEmpty()) {
                item {
                    Text("No lectures scheduled for this day.", fontSize = 13.sp, color = TextMuted, modifier = Modifier.padding(vertical = 20.dp))
                }
            } else {
                items(daySlots, key = { it.id }) { slot ->
                    GlowCard(borderColor = ElectricCyan) {
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(slot.subjectName, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                                Text("${slot.startTime} - ${slot.endTime} • ${slot.room}", fontSize = 12.sp, color = ElectricCyan)
                                if (slot.teacher.isNotBlank()) {
                                    Text("Teacher: ${slot.teacher}", fontSize = 11.sp, color = TextMuted)
                                }
                            }
                            IconButton(onClick = { onDeleteSlot(slot) }) {
                                Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = TextMuted)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SubjectsSection(subjects: List<SubjectEntity>, onDeleteSubject: (SubjectEntity) -> Unit) {
    LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp), contentPadding = PaddingValues(bottom = 120.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        if (subjects.isEmpty()) {
            item { Text("No subjects registered yet. Tap + to add one.", fontSize = 13.sp, color = TextMuted, modifier = Modifier.padding(vertical = 20.dp)) }
        } else {
            items(subjects, key = { it.id }) { s ->
                GlowCard {
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(s.name, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                                if (s.code.isNotBlank()) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(shape = RoundedCornerShape(4.dp), color = ElectricViolet.copy(alpha = 0.2f)) {
                                        Text(s.code, fontSize = 10.sp, color = ElectricVioletLight, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                                    }
                                }
                            }
                            if (s.teacher.isNotBlank() || s.classroom.isNotBlank()) {
                                Text("${s.teacher} • ${s.classroom}", fontSize = 12.sp, color = TextMuted)
                            }
                            Text("Target: ${s.targetWeeklyHours.toInt()} hrs/week", fontSize = 11.sp, color = ElectricCyan)
                        }
                        IconButton(onClick = { onDeleteSubject(s) }) {
                            Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = TextMuted)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AssignmentsSection(assignments: List<AssignmentEntity>, onToggle: (AssignmentEntity) -> Unit, onDelete: (AssignmentEntity) -> Unit) {
    LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp), contentPadding = PaddingValues(bottom = 120.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        if (assignments.isEmpty()) {
            item { Text("No assignments recorded.", fontSize = 13.sp, color = TextMuted, modifier = Modifier.padding(vertical = 20.dp)) }
        } else {
            items(assignments, key = { it.id }) { a ->
                GlowCard(borderColor = if (a.isSubmitted) AccentGreen else AccentAmber) {
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = a.isSubmitted, onCheckedChange = { onToggle(a) }, colors = CheckboxDefaults.colors(checkedColor = AccentGreen))
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(a.title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                            Text("${a.subjectName} • Due: ${a.dueDate}", fontSize = 12.sp, color = if (a.isSubmitted) AccentGreen else AccentAmber)
                        }
                        IconButton(onClick = { onDelete(a) }) {
                            Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = TextMuted)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ExamsSection(exams: List<ExamEntity>, onDelete: (ExamEntity) -> Unit) {
    LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp), contentPadding = PaddingValues(bottom = 120.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        if (exams.isEmpty()) {
            item { Text("No upcoming examinations booked.", fontSize = 13.sp, color = TextMuted, modifier = Modifier.padding(vertical = 20.dp)) }
        } else {
            items(exams, key = { it.id }) { e ->
                GlowCard(borderColor = AccentRed) {
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(36.dp).clip(CircleShape).background(AccentRed.copy(alpha = 0.2f)), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.School, contentDescription = null, tint = AccentRed, modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(e.title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                            Text("${e.subjectName} • ${e.examDate} at ${e.startTime}", fontSize = 12.sp, color = AccentAmber)
                            if (e.room.isNotBlank()) Text("Hall: ${e.room}", fontSize = 11.sp, color = TextMuted)
                        }
                        IconButton(onClick = { onDelete(e) }) {
                            Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = TextMuted)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StudyTimerSection(viewModel: LifeOsViewModel, subjects: List<SubjectEntity>) {
    var timerRunning by remember { mutableStateOf(false) }
    var elapsedSeconds by remember { mutableStateOf(0) }
    var selectedSubject by remember { mutableStateOf(subjects.firstOrNull()?.name ?: "Self Study") }
    var isPomodoro by remember { mutableStateOf(true) }

    LaunchedEffect(timerRunning) {
        while (timerRunning) {
            delay(1000L)
            elapsedSeconds += 1
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Study Focus Timer", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextWhite)
        Text(if (isPomodoro) "25-min Pomodoro Mode" else "Free Study Session", fontSize = 12.sp, color = ElectricCyan)
        Spacer(modifier = Modifier.height(24.dp))

        // Timer Circle Display
        Surface(
            shape = CircleShape,
            color = DarkSlateSurface,
            border = androidx.compose.foundation.BorderStroke(3.dp, if (timerRunning) ElectricViolet else DarkSlateBorder),
            modifier = Modifier.size(200.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    val minutes = elapsedSeconds / 60
                    val seconds = elapsedSeconds % 60
                    Text(
                        text = String.format("%02d:%02d", minutes, seconds),
                        fontSize = 42.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite
                    )
                    Text(
                        text = if (timerRunning) "Active Session" else "Paused / Ready",
                        fontSize = 11.sp,
                        color = if (timerRunning) AccentGreen else TextMuted
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))

        // Subject selector
        Text("Active Subject: $selectedSubject", fontSize = 13.sp, color = TextMuted)
        Spacer(modifier = Modifier.height(16.dp))

        // Controls
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Button(
                onClick = { timerRunning = !timerRunning },
                colors = ButtonDefaults.buttonColors(containerColor = if (timerRunning) AccentAmber else ElectricViolet)
            ) {
                Icon(if (timerRunning) Icons.Default.Pause else Icons.Default.PlayArrow, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text(if (timerRunning) "Pause" else "Start Timer")
            }

            if (elapsedSeconds > 0) {
                OutlinedButton(
                    onClick = {
                        val duration = (elapsedSeconds / 60).coerceAtLeast(1)
                        viewModel.logStudySession(selectedSubject, 0L, duration, "Pomodoro study session")
                        timerRunning = false
                        elapsedSeconds = 0
                    }
                ) {
                    Icon(Icons.Default.Check, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Finish & Save")
                }
            }
        }
    }
}
