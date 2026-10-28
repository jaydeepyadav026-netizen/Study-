package com.example.ui.screens.settings

import androidx.compose.animation.*
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.components.GlowCard
import com.example.ui.theme.*
import com.example.viewmodel.LifeOsViewModel

@Composable
fun SettingsScreen(
    viewModel: LifeOsViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val userName by viewModel.userName.collectAsState()
    val currency by viewModel.currency.collectAsState()
    val isDarkTheme by viewModel.isDarkTheme.collectAsState()

    var showEditNameDialog by remember { mutableStateOf(false) }
    var showCurrencyDialog by remember { mutableStateOf(false) }

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
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "System Settings",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = 8.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Profile Card
            item {
                GlowCard(borderColor = ElectricViolet) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = ElectricVioletDark,
                            border = androidx.compose.foundation.BorderStroke(2.dp, ElectricCyan),
                            modifier = Modifier.size(56.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = userName.take(1).uppercase(),
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(userName, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                            Text("LIFE OS Administrator", fontSize = 12.sp, color = ElectricCyan)
                        }
                        IconButton(onClick = { showEditNameDialog = true }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit Name", tint = TextMuted)
                        }
                    }
                }
            }

            // Preferences Group
            item {
                Text("Preferences & Appearance", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                Spacer(modifier = Modifier.height(6.dp))

                GlowCard {
                    // Dark theme toggle
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.DarkMode, contentDescription = null, tint = ElectricViolet)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("Midnight Dark Theme", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                                Text("Deep slate surfaces with glowing accents", fontSize = 11.sp, color = TextMuted)
                            }
                        }
                        Switch(
                            checked = isDarkTheme,
                            onCheckedChange = { viewModel.toggleTheme() }
                        )
                    }

                    Divider(color = DarkSlateBorder.copy(alpha = 0.5f), modifier = Modifier.padding(vertical = 8.dp))

                    // Currency selector
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showCurrencyDialog = true }
                            .padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CurrencyExchange, contentDescription = null, tint = AccentGreen)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("System Currency", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                                Text("Currently: $currency (Default INR)", fontSize = 11.sp, color = TextMuted)
                            }
                        }
                        Text(currency, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = AccentGreen)
                    }
                }
            }

            // Privacy & Data Storage
            item {
                Text("Data Architecture & Privacy", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                Spacer(modifier = Modifier.height(6.dp))

                GlowCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Security, contentDescription = null, tint = ElectricCyan)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("100% On-Device Persistent Storage", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                            Text("Your personal data, notes, balances, and contacts reside securely in your local Room SQLite database.", fontSize = 11.sp, color = TextMuted)
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CloudOff, contentDescription = null, tint = AccentAmber)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("Zero Data Telemetry", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                            Text("No unauthorized external transfers or cloud synchronizations.", fontSize = 11.sp, color = TextMuted)
                        }
                    }
                }
            }

            // System Information
            item {
                GlowCard {
                    Text("LIFE OS System Information", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Version: 1.0.0 (Production Build)", fontSize = 12.sp, color = TextMuted)
                    Text("Architecture: Jetpack Compose + Room Local Storage + Kotlin Flow", fontSize = 12.sp, color = TextMuted)
                    Text("Tagline: \"Your Life. Your Rules. Your System.\"", fontSize = 12.sp, color = ElectricCyan)
                }
            }
        }
    }

    // Edit Name Dialog
    if (showEditNameDialog) {
        var tempName by remember { mutableStateOf(userName) }
        Dialog(onDismissRequest = { showEditNameDialog = false }) {
            Surface(shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surface, border = androidx.compose.foundation.BorderStroke(1.dp, DarkSlateBorder), modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Edit Profile Name", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = tempName,
                        onValueChange = { tempName = it },
                        label = { Text("Your Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { showEditNameDialog = false }) { Text("Cancel") }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (tempName.isNotBlank()) {
                                    viewModel.setUserName(tempName.trim())
                                    showEditNameDialog = false
                                }
                            }
                        ) {
                            Text("Save")
                        }
                    }
                }
            }
        }
    }

    // Currency Dialog
    if (showCurrencyDialog) {
        Dialog(onDismissRequest = { showCurrencyDialog = false }) {
            Surface(shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surface, border = androidx.compose.foundation.BorderStroke(1.dp, DarkSlateBorder), modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Select Currency", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))
                    val currencies = listOf("₹" to "INR (₹)", "$" to "USD ($)", "€" to "EUR (€)", "£" to "GBP (£)", "¥" to "JPY (¥)")
                    currencies.forEach { (symbol, label) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    viewModel.setCurrency(symbol)
                                    showCurrencyDialog = false
                                }
                                .padding(vertical = 10.dp, horizontal = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(label, fontSize = 14.sp)
                            if (currency == symbol) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = AccentGreen)
                            }
                        }
                    }
                }
            }
        }
    }
}
