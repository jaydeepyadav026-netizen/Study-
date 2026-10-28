package com.example.ui.screens.shopping

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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.local.entity.ShoppingItemEntity
import com.example.data.local.entity.WishlistItemEntity
import com.example.ui.components.GlowCard
import com.example.ui.theme.*
import com.example.viewmodel.LifeOsViewModel

enum class ShoppingTab {
    SHOPPING_LIST, WISHLIST
}

@Composable
fun ShoppingScreen(
    viewModel: LifeOsViewModel,
    modifier: Modifier = Modifier
) {
    val shoppingItems by viewModel.allShoppingItems.collectAsState()
    val wishlistItems by viewModel.allWishlistItems.collectAsState()
    val accounts by viewModel.allAccounts.collectAsState()
    val currency by viewModel.currency.collectAsState()

    var activeTab by remember { mutableStateOf(ShoppingTab.SHOPPING_LIST) }
    var showAddItemDialog by remember { mutableStateOf(false) }
    var showAddWishlistDialog by remember { mutableStateOf(false) }

    val totalEstimated = remember(shoppingItems) {
        shoppingItems.filter { !it.isPurchased }.sumOf { it.estimatedPrice }
    }
    val totalPurchased = remember(shoppingItems) {
        shoppingItems.filter { it.isPurchased }.sumOf { it.estimatedPrice }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (activeTab == ShoppingTab.SHOPPING_LIST) showAddItemDialog = true
                    else showAddWishlistDialog = true
                },
                containerColor = ElectricViolet,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.padding(bottom = 80.dp).testTag("fab_add_shopping")
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
                    text = "Shopping & Wishlist",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Track supplies, groceries, and future milestone purchases",
                    fontSize = 11.sp,
                    color = TextMuted
                )
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    FilterChip(
                        selected = activeTab == ShoppingTab.SHOPPING_LIST,
                        onClick = { activeTab = ShoppingTab.SHOPPING_LIST },
                        label = { Text("Shopping List", fontSize = 12.sp) }
                    )
                    FilterChip(
                        selected = activeTab == ShoppingTab.WISHLIST,
                        onClick = { activeTab = ShoppingTab.WISHLIST },
                        label = { Text("Life Wishlist", fontSize = 12.sp) }
                    )
                }
            }

            if (activeTab == ShoppingTab.SHOPPING_LIST) {
                // Total calculation card
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = DarkSlateSurface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkSlateBorder),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Estimated Pending", fontSize = 11.sp, color = TextMuted)
                            Text("$currency${"%,.0f".format(totalEstimated)}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Already Bought", fontSize = 11.sp, color = TextMuted)
                            Text("$currency${"%,.0f".format(totalPurchased)}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = AccentGreen)
                        }
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
                if (activeTab == ShoppingTab.SHOPPING_LIST) {
                    if (shoppingItems.isEmpty()) {
                        item {
                            Text("Your shopping list is empty. Tap + to add items.", fontSize = 13.sp, color = TextMuted, modifier = Modifier.padding(vertical = 20.dp))
                        }
                    } else {
                        items(shoppingItems, key = { it.id }) { item ->
                            GlowCard(borderColor = if (item.isPurchased) DarkSlateBorder else ElectricCyan) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = item.isPurchased,
                                        onCheckedChange = {
                                            viewModel.toggleShoppingItemPurchased(
                                                item = item,
                                                createExpense = true,
                                                accountId = accounts.firstOrNull()?.id
                                            )
                                        },
                                        colors = CheckboxDefaults.colors(checkedColor = AccentGreen)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = item.name,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            textDecoration = if (item.isPurchased) TextDecoration.LineThrough else null,
                                            color = if (item.isPurchased) TextMuted else TextWhite
                                        )
                                        Text(
                                            text = "Qty: ${item.quantity} • ${item.category}",
                                            fontSize = 12.sp,
                                            color = TextMuted
                                        )
                                    }

                                    if (item.estimatedPrice > 0) {
                                        Text(
                                            text = "$currency${"%,.0f".format(item.estimatedPrice)}",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (item.isPurchased) TextMuted else ElectricCyan
                                        )
                                    }

                                    IconButton(onClick = { viewModel.deleteShoppingItem(item) }) {
                                        Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = TextMuted)
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // Wishlist
                    if (wishlistItems.isEmpty()) {
                        item {
                            Text("No wishlist items saved yet. Dream big!", fontSize = 13.sp, color = TextMuted, modifier = Modifier.padding(vertical = 20.dp))
                        }
                    } else {
                        items(wishlistItems, key = { it.id }) { wish ->
                            val progress = if (wish.expectedPrice > 0) (wish.savedAmount / wish.expectedPrice).toFloat().coerceIn(0f, 1f) else 0f
                            GlowCard(borderColor = ElectricViolet) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(wish.name, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                                        if (wish.description.isNotBlank()) Text(wish.description, fontSize = 12.sp, color = TextMuted)
                                        Text(
                                            text = "Expected: $currency${"%,.0f".format(wish.expectedPrice)}",
                                            fontSize = 12.sp,
                                            color = ElectricCyan
                                        )
                                    }
                                    IconButton(onClick = { viewModel.deleteWishlistItem(wish) }) {
                                        Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = TextMuted)
                                    }
                                }

                                if (wish.expectedPrice > 0) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Savings progress: $currency${"%,.0f".format(wish.savedAmount)}", fontSize = 11.sp, color = TextMuted)
                                        Text("${(progress * 100).toInt()}%", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AccentGreen)
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
        }
    }

    // Add Shopping Item Dialog
    if (showAddItemDialog) {
        var name by remember { mutableStateOf("") }
        var quantity by remember { mutableStateOf("1") }
        var price by remember { mutableStateOf("") }
        var category by remember { mutableStateOf("Groceries") }

        Dialog(onDismissRequest = { showAddItemDialog = false }) {
            Surface(shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surface, border = androidx.compose.foundation.BorderStroke(1.dp, DarkSlateBorder), modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Add Shopping Item", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Item Name") }, singleLine = true, modifier = Modifier.fillMaxWidth().testTag("shopping_item_name_input"))
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = quantity, onValueChange = { quantity = it }, label = { Text("Quantity") }, singleLine = true, modifier = Modifier.weight(1f))
                        OutlinedTextField(value = price, onValueChange = { price = it.filter { ch -> ch.isDigit() || ch == '.' } }, label = { Text("Est. Price (₹)") }, singleLine = true, modifier = Modifier.weight(1f))
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { showAddItemDialog = false }) { Text("Cancel") }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (name.isNotBlank()) {
                                    viewModel.saveShoppingItem(
                                        ShoppingItemEntity(
                                            name = name.trim(),
                                            quantity = quantity.trim(),
                                            estimatedPrice = price.toDoubleOrNull() ?: 0.0,
                                            category = category
                                        )
                                    )
                                    showAddItemDialog = false
                                }
                            },
                            enabled = name.isNotBlank()
                        ) {
                            Text("Add")
                        }
                    }
                }
            }
        }
    }

    // Add Wishlist Item Dialog
    if (showAddWishlistDialog) {
        var name by remember { mutableStateOf("") }
        var price by remember { mutableStateOf("") }
        var description by remember { mutableStateOf("") }

        Dialog(onDismissRequest = { showAddWishlistDialog = false }) {
            Surface(shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surface, border = androidx.compose.foundation.BorderStroke(1.dp, DarkSlateBorder), modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Add Wishlist Item", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Item / Goal Purchase") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = price, onValueChange = { price = it.filter { ch -> ch.isDigit() || ch == '.' } }, label = { Text("Expected Price (₹)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description / Reason") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { showAddWishlistDialog = false }) { Text("Cancel") }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (name.isNotBlank()) {
                                    viewModel.saveWishlistItem(
                                        WishlistItemEntity(
                                            name = name.trim(),
                                            expectedPrice = price.toDoubleOrNull() ?: 0.0,
                                            description = description.trim()
                                        )
                                    )
                                    showAddWishlistDialog = false
                                }
                            },
                            enabled = name.isNotBlank()
                        ) {
                            Text("Save Wish")
                        }
                    }
                }
            }
        }
    }
}
