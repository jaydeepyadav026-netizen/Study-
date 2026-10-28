package com.example.ui.screens.search

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
import com.example.ui.components.GlowCard
import com.example.ui.theme.*
import com.example.viewmodel.LifeOsViewModel

enum class SearchFilterType {
    ALL, TASKS, TRANSACTIONS, EVENTS, DEBTS, NOTES, SHOPPING, CONTACTS
}

@Composable
fun GlobalSearchScreen(
    viewModel: LifeOsViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var query by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(SearchFilterType.ALL) }

    val allTasks by viewModel.allTasks.collectAsState()
    val allTransactions by viewModel.allTransactions.collectAsState()
    val allEvents by viewModel.allEvents.collectAsState()
    val allDebts by viewModel.allDebts.collectAsState()
    val allNotes by viewModel.activeNotes.collectAsState()
    val allShopping by viewModel.allShoppingItems.collectAsState()
    val allContacts by viewModel.allContacts.collectAsState()
    val currency by viewModel.currency.collectAsState()

    val q = query.trim().lowercase()

    val matchedTasks = remember(allTasks, q) {
        if (q.isBlank()) emptyList()
        else allTasks.filter { it.title.lowercase().contains(q) || it.description.lowercase().contains(q) || it.category.lowercase().contains(q) }
    }
    val matchedTransactions = remember(allTransactions, q) {
        if (q.isBlank()) emptyList()
        else allTransactions.filter { it.description.lowercase().contains(q) || it.category.lowercase().contains(q) }
    }
    val matchedEvents = remember(allEvents, q) {
        if (q.isBlank()) emptyList()
        else allEvents.filter { it.title.lowercase().contains(q) || it.description.lowercase().contains(q) }
    }
    val matchedDebts = remember(allDebts, q) {
        if (q.isBlank()) emptyList()
        else allDebts.filter { it.personName.lowercase().contains(q) || it.description.lowercase().contains(q) }
    }
    val matchedNotes = remember(allNotes, q) {
        if (q.isBlank()) emptyList()
        else allNotes.filter { it.title.lowercase().contains(q) || it.content.lowercase().contains(q) }
    }
    val matchedShopping = remember(allShopping, q) {
        if (q.isBlank()) emptyList()
        else allShopping.filter { it.name.lowercase().contains(q) }
    }
    val matchedContacts = remember(allContacts, q) {
        if (q.isBlank()) emptyList()
        else allContacts.filter { it.name.lowercase().contains(q) || it.phone.lowercase().contains(q) }
    }

    val totalMatches = matchedTasks.size + matchedTransactions.size + matchedEvents.size + matchedDebts.size + matchedNotes.size + matchedShopping.size + matchedContacts.size

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Text(
                text = "Global Search",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            placeholder = { Text("Search tasks, money, notes, debts, contacts...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = ElectricCyan) },
            trailingIcon = {
                if (query.isNotBlank()) {
                    IconButton(onClick = { query = "" }) {
                        Icon(Icons.Default.Close, contentDescription = "Clear")
                    }
                }
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("global_search_text_field")
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Filter Pills
        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            items(SearchFilterType.values()) { filter ->
                FilterChip(
                    selected = selectedType == filter,
                    onClick = { selectedType = filter },
                    label = { Text(filter.name.lowercase().replaceFirstChar { it.uppercase() }, fontSize = 11.sp) }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (query.isBlank()) {
            Column(
                modifier = Modifier.fillMaxSize().padding(top = 40.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(Icons.Default.Search, contentDescription = null, tint = TextMuted, modifier = Modifier.size(48.dp))
                Spacer(modifier = Modifier.height(8.dp))
                Text("Search entire LIFE OS system", fontSize = 14.sp, color = TextMuted)
                Text("Quickly locate tasks, money transactions, debts, or contacts", fontSize = 12.sp, color = TextSubtle)
            }
        } else if (totalMatches == 0) {
            Column(
                modifier = Modifier.fillMaxSize().padding(top = 40.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("No results found for \"$query\"", fontSize = 14.sp, color = TextMuted)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(top = 8.dp, bottom = 120.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Tasks
                if (selectedType == SearchFilterType.ALL || selectedType == SearchFilterType.TASKS) {
                    items(matchedTasks) { t ->
                        GlowCard(borderColor = ElectricViolet) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(shape = RoundedCornerShape(4.dp), color = ElectricViolet.copy(alpha = 0.2f)) {
                                    Text("TASK", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = ElectricVioletLight, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(t.title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                                    Text("${t.date} • ${t.category} • ${t.priority}", fontSize = 11.sp, color = TextMuted)
                                }
                            }
                        }
                    }
                }

                // Transactions
                if (selectedType == SearchFilterType.ALL || selectedType == SearchFilterType.TRANSACTIONS) {
                    items(matchedTransactions) { tx ->
                        GlowCard(borderColor = if (tx.type == "Expense") AccentRed else AccentGreen) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(shape = RoundedCornerShape(4.dp), color = if (tx.type == "Expense") AccentRed.copy(alpha = 0.2f) else AccentGreen.copy(alpha = 0.2f)) {
                                    Text("MONEY", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = if (tx.type == "Expense") AccentRed else AccentGreen, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(tx.description.ifBlank { tx.category }, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                                    Text("${tx.date} • ${tx.type}", fontSize = 11.sp, color = TextMuted)
                                }
                                Text("$currency${"%,.2f".format(tx.amount)}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = if (tx.type == "Expense") AccentRed else AccentGreen)
                            }
                        }
                    }
                }

                // Debts
                if (selectedType == SearchFilterType.ALL || selectedType == SearchFilterType.DEBTS) {
                    items(matchedDebts) { d ->
                        GlowCard(borderColor = ElectricCyan) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(shape = RoundedCornerShape(4.dp), color = ElectricCyan.copy(alpha = 0.2f)) {
                                    Text("UDHAR", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = ElectricCyan, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("${d.personName} (${d.type})", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                                    Text("Outstanding: $currency${"%,.0f".format(d.outstandingAmount)}", fontSize = 11.sp, color = TextMuted)
                                }
                            }
                        }
                    }
                }

                // Notes
                if (selectedType == SearchFilterType.ALL || selectedType == SearchFilterType.NOTES) {
                    items(matchedNotes) { n ->
                        GlowCard {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(shape = RoundedCornerShape(4.dp), color = DarkSlateSurfaceVariant) {
                                    Text("NOTE", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = TextMuted, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(n.title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                                    Text(n.content.take(60), fontSize = 11.sp, color = TextMuted)
                                }
                            }
                        }
                    }
                }

                // Contacts
                if (selectedType == SearchFilterType.ALL || selectedType == SearchFilterType.CONTACTS) {
                    items(matchedContacts) { c ->
                        GlowCard {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(shape = RoundedCornerShape(4.dp), color = DarkSlateSurfaceVariant) {
                                    Text("CONTACT", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = ElectricCyan, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(c.name, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                                    Text("${c.relationship} • ${c.phone}", fontSize = 11.sp, color = TextMuted)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
