package com.example.ui.screens.notes

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
import com.example.data.local.entity.DiaryEntity
import com.example.data.local.entity.NoteEntity
import com.example.ui.components.GlowCard
import com.example.ui.theme.*
import com.example.viewmodel.LifeOsViewModel
import java.time.LocalDate

enum class NotesTab {
    NOTES, DIARY, VAULT
}

@Composable
fun NotesScreen(
    viewModel: LifeOsViewModel,
    modifier: Modifier = Modifier
) {
    val notes by viewModel.activeNotes.collectAsState()
    val diaryEntries by viewModel.allDiaryEntries.collectAsState()

    var activeTab by remember { mutableStateOf(NotesTab.NOTES) }
    var showCreateNoteDialog by remember { mutableStateOf(false) }
    var showCreateDiaryDialog by remember { mutableStateOf(false) }
    var selectedCategoryFilter by remember { mutableStateOf<String?>(null) }

    val filteredNotes = remember(notes, selectedCategoryFilter, activeTab) {
        if (activeTab == NotesTab.VAULT) {
            notes.filter { it.isPrivate || it.category == "Reference" || it.category == "Vault" }
        } else {
            if (selectedCategoryFilter == null) notes
            else notes.filter { it.category.equals(selectedCategoryFilter, ignoreCase = true) }
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (activeTab == NotesTab.DIARY) showCreateDiaryDialog = true
                    else showCreateNoteDialog = true
                },
                containerColor = ElectricCyan,
                contentColor = Color.Black,
                shape = CircleShape,
                modifier = Modifier.padding(bottom = 80.dp).testTag("fab_add_note")
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
                    text = "Notes & Private Vault",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Personal reflections, knowledge vault, and daily journal",
                    fontSize = 11.sp,
                    color = TextMuted
                )
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = activeTab == NotesTab.NOTES,
                        onClick = { activeTab = NotesTab.NOTES },
                        label = { Text("All Notes", fontSize = 12.sp) }
                    )
                    FilterChip(
                        selected = activeTab == NotesTab.DIARY,
                        onClick = { activeTab = NotesTab.DIARY },
                        label = { Text("Personal Diary", fontSize = 12.sp) }
                    )
                    FilterChip(
                        selected = activeTab == NotesTab.VAULT,
                        onClick = { activeTab = NotesTab.VAULT },
                        label = { Text("Knowledge Vault 🔒", fontSize = 12.sp) }
                    )
                }
            }

            if (activeTab == NotesTab.NOTES) {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    item {
                        AssistChip(
                            onClick = { selectedCategoryFilter = null },
                            label = { Text("All", fontSize = 11.sp) },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = if (selectedCategoryFilter == null) ElectricViolet.copy(alpha = 0.2f) else Color.Transparent
                            )
                        )
                    }
                    val cats = listOf("Ideas", "General", "Learning", "Plans", "Reference")
                    items(cats) { c ->
                        val isSel = selectedCategoryFilter == c
                        AssistChip(
                            onClick = { selectedCategoryFilter = if (isSel) null else c },
                            label = { Text(c, fontSize = 11.sp) },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = if (isSel) ElectricViolet.copy(alpha = 0.2f) else Color.Transparent
                            )
                        )
                    }
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = 8.dp, bottom = 120.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (activeTab == NotesTab.DIARY) {
                    if (diaryEntries.isEmpty()) {
                        item {
                            Text("No diary entries yet. Tap + to record your daily reflection.", fontSize = 13.sp, color = TextMuted, modifier = Modifier.padding(vertical = 20.dp))
                        }
                    } else {
                        items(diaryEntries, key = { it.id }) { entry ->
                            GlowCard(borderColor = ElectricViolet) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(entry.mood.take(2), fontSize = 20.sp)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(entry.title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                                            Text(entry.date, fontSize = 11.sp, color = ElectricCyan)
                                        }
                                    }
                                    IconButton(onClick = { viewModel.deleteDiaryEntry(entry) }) {
                                        Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = TextMuted)
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(entry.content, fontSize = 13.sp, color = TextWhite.copy(alpha = 0.9f))
                            }
                        }
                    }
                } else {
                    // Notes or Vault
                    if (filteredNotes.isEmpty()) {
                        item {
                            Text("No entries found in this view.", fontSize = 13.sp, color = TextMuted, modifier = Modifier.padding(vertical = 20.dp))
                        }
                    } else {
                        items(filteredNotes, key = { it.id }) { note ->
                            GlowCard(borderColor = if (note.isPinned) ElectricViolet else DarkSlateBorder) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        if (note.isPinned) {
                                            Icon(Icons.Default.PushPin, contentDescription = "Pinned", tint = ElectricViolet, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                        }
                                        Text(note.title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Surface(shape = RoundedCornerShape(4.dp), color = DarkSlateSurfaceVariant) {
                                            Text(note.category, fontSize = 10.sp, color = ElectricCyan, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                        }
                                        IconButton(onClick = { viewModel.deleteNote(note) }) {
                                            Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = TextMuted, modifier = Modifier.size(18.dp))
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(note.content, fontSize = 13.sp, color = TextMuted, maxLines = 4)

                                if (note.excludeFromAi) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("🔒 Excluded from AI processing", fontSize = 10.sp, color = AccentAmber)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Create Note Dialog
    if (showCreateNoteDialog) {
        var title by remember { mutableStateOf("") }
        var content by remember { mutableStateOf("") }
        var category by remember { mutableStateOf(if (activeTab == NotesTab.VAULT) "Vault" else "General") }
        var isPrivate by remember { mutableStateOf(activeTab == NotesTab.VAULT) }
        var excludeFromAi by remember { mutableStateOf(false) }

        Dialog(onDismissRequest = { showCreateNoteDialog = false }) {
            Surface(shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surface, border = androidx.compose.foundation.BorderStroke(1.dp, DarkSlateBorder), modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("New Note", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") }, singleLine = true, modifier = Modifier.fillMaxWidth().testTag("note_title_input"))
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = content, onValueChange = { content = it }, label = { Text("Note content") }, minLines = 4, modifier = Modifier.fillMaxWidth().testTag("note_content_input"))
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = excludeFromAi, onCheckedChange = { excludeFromAi = it })
                        Text("Exclude from external AI processing", fontSize = 12.sp, color = TextMuted)
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { showCreateNoteDialog = false }) { Text("Cancel") }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (title.isNotBlank() || content.isNotBlank()) {
                                    viewModel.saveNote(
                                        NoteEntity(
                                            title = title.ifBlank { "Quick Note" },
                                            content = content,
                                            category = category,
                                            isPrivate = isPrivate,
                                            excludeFromAi = excludeFromAi
                                        )
                                    )
                                    showCreateNoteDialog = false
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

    // Create Diary Entry Dialog
    if (showCreateDiaryDialog) {
        var title by remember { mutableStateOf("") }
        var content by remember { mutableStateOf("") }
        var selectedMood by remember { mutableStateOf("😊 Great") }

        Dialog(onDismissRequest = { showCreateDiaryDialog = false }) {
            Surface(shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surface, border = androidx.compose.foundation.BorderStroke(1.dp, DarkSlateBorder), modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("New Diary Entry", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("How was your day?", fontSize = 12.sp, color = TextMuted)
                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        listOf("😊 Great", "😌 Good", "😐 Neutral", "😔 Down", "😤 Stressed").forEach { m ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (selectedMood == m) ElectricViolet.copy(alpha = 0.3f) else Color.Transparent)
                                    .clickable { selectedMood = m }
                                    .padding(6.dp)
                            ) {
                                Text(m.take(2), fontSize = 22.sp)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title / Summary") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = content, onValueChange = { content = it }, label = { Text("Thoughts, reflections, lessons...") }, minLines = 4, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { showCreateDiaryDialog = false }) { Text("Cancel") }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (content.isNotBlank() || title.isNotBlank()) {
                                    viewModel.saveDiaryEntry(
                                        DiaryEntity(
                                            date = LocalDate.now().toString(),
                                            title = title.ifBlank { "Daily Entry" },
                                            content = content,
                                            mood = selectedMood
                                        )
                                    )
                                    showCreateDiaryDialog = false
                                }
                            },
                            enabled = content.isNotBlank() || title.isNotBlank()
                        ) {
                            Text("Save Entry")
                        }
                    }
                }
            }
        }
    }
}
