package com.example.ui.screens.more

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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.GlowCard
import com.example.ui.theme.*
import com.example.viewmodel.LifeOsViewModel
import com.example.viewmodel.SubScreen

data class MoreMenuItem(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val iconTint: Color,
    val targetSubScreen: SubScreen
)

@Composable
fun MoreScreen(
    viewModel: LifeOsViewModel,
    modifier: Modifier = Modifier
) {
    val menuItems = listOf(
        MoreMenuItem(
            title = "Udhar / Debts",
            description = "Track money lent, borrowed, and partial repayments",
            icon = Icons.Default.ReceiptLong,
            iconTint = AccentGreen,
            targetSubScreen = SubScreen.UDHAR
        ),
        MoreMenuItem(
            title = "Focus Mode",
            description = "Distraction-free fullscreen timer & Pomodoro",
            icon = Icons.Default.Timer,
            iconTint = ElectricViolet,
            targetSubScreen = SubScreen.FOCUS
        ),
        MoreMenuItem(
            title = "Shopping & Wishlist",
            description = "Manage supplies, groceries, and dream purchases",
            icon = Icons.Default.ShoppingCart,
            iconTint = ElectricCyan,
            targetSubScreen = SubScreen.SHOPPING
        ),
        MoreMenuItem(
            title = "People & Contacts",
            description = "Private address book and birthday tracking",
            icon = Icons.Default.People,
            iconTint = AccentAmber,
            targetSubScreen = SubScreen.CONTACTS
        ),
        MoreMenuItem(
            title = "Global Search",
            description = "Fast index search across tasks, money, notes, debts",
            icon = Icons.Default.Search,
            iconTint = ElectricCyanLight,
            targetSubScreen = SubScreen.SEARCH
        ),
        MoreMenuItem(
            title = "LIFE AI Assistant",
            description = "Natural language command center in English & Hinglish",
            icon = Icons.Default.AutoAwesome,
            iconTint = ElectricVioletLight,
            targetSubScreen = SubScreen.AI_ASSISTANT
        ),
        MoreMenuItem(
            title = "Settings & Customization",
            description = "Theme, currency, dashboard widgets, and privacy",
            icon = Icons.Default.Settings,
            iconTint = TextMuted,
            targetSubScreen = SubScreen.SETTINGS
        )
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Column(modifier = Modifier.padding(vertical = 8.dp)) {
            Text(
                text = "System Modules",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Advanced tools and system configuration",
                fontSize = 11.sp,
                color = TextMuted
            )
        }

        LazyColumn(
            contentPadding = PaddingValues(top = 8.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(menuItems) { item ->
                GlowCard(
                    onClick = { viewModel.navigateToSubScreen(item.targetSubScreen) },
                    modifier = Modifier.testTag("more_menu_${item.targetSubScreen.name.lowercase()}")
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(item.iconTint.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = null,
                                tint = item.iconTint,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.title,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextWhite
                            )
                            Text(
                                text = item.description,
                                fontSize = 11.sp,
                                color = TextMuted
                            )
                        }

                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "Navigate",
                            tint = TextMuted
                        )
                    }
                }
            }
        }
    }
}
