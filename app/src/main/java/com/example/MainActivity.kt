package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.LifeOsBottomBar
import com.example.ui.components.LifeOsTopBar
import com.example.ui.components.QuickAddModal
import com.example.ui.screens.ai.LifeAiScreen
import com.example.ui.screens.calendar.CalendarScreen
import com.example.ui.screens.contacts.ContactsScreen
import com.example.ui.screens.focus.FocusScreen
import com.example.ui.screens.goals.GoalsScreen
import com.example.ui.screens.home.HomeScreen
import com.example.ui.screens.money.MoneyScreen
import com.example.ui.screens.more.MoreScreen
import com.example.ui.screens.notes.NotesScreen
import com.example.ui.screens.planner.PlannerScreen
import com.example.ui.screens.search.GlobalSearchScreen
import com.example.ui.screens.settings.SettingsScreen
import com.example.ui.screens.shopping.ShoppingScreen
import com.example.ui.screens.study.StudyScreen
import com.example.ui.screens.udhar.UdharScreen
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.AppTab
import com.example.viewmodel.LifeOsViewModel
import com.example.viewmodel.SubScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: LifeOsViewModel = viewModel()
            val isDarkTheme by viewModel.isDarkTheme.collectAsState()

            MyApplicationTheme(darkTheme = isDarkTheme) {
                LifeOsApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun LifeOsApp(viewModel: LifeOsViewModel) {
    val currentTab by viewModel.currentTab.collectAsState()
    val currentSubScreen by viewModel.currentSubScreen.collectAsState()
    val userName by viewModel.userName.collectAsState()
    val showQuickAdd by viewModel.showQuickAddModal.collectAsState()

    // Handle back button smoothly
    BackHandler(enabled = currentSubScreen != SubScreen.NONE || currentTab != AppTab.HOME) {
        viewModel.navigateBack()
    }

    val isFullScreenSubScreen = currentSubScreen in listOf(
        SubScreen.SEARCH,
        SubScreen.AI_ASSISTANT,
        SubScreen.SETTINGS
    )

    Scaffold(
        topBar = {
            if (!isFullScreenSubScreen) {
                LifeOsTopBar(
                    userName = userName,
                    onSearchClick = { viewModel.navigateToSubScreen(SubScreen.SEARCH) },
                    onAiAssistantClick = { viewModel.navigateToSubScreen(SubScreen.AI_ASSISTANT) },
                    onProfileClick = { viewModel.navigateToSubScreen(SubScreen.SETTINGS) }
                )
            }
        },
        bottomBar = {
            if (currentSubScreen == SubScreen.NONE) {
                LifeOsBottomBar(
                    currentTab = currentTab,
                    onTabSelected = { tab -> viewModel.navigateToTab(tab) }
                )
            }
        },
        floatingActionButton = {
            // Global Quick-Add FAB on Home dashboard and secondary screens
            if (currentSubScreen == SubScreen.NONE && currentTab == AppTab.HOME) {
                FloatingActionButton(
                    onClick = { viewModel.openQuickAdd() },
                    containerColor = ElectricViolet,
                    contentColor = Color.White,
                    shape = CircleShape,
                    modifier = Modifier
                        .padding(bottom = 76.dp)
                        .testTag("global_quick_add_fab")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Quick Add")
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentSubScreen) {
                SubScreen.UDHAR -> UdharScreen(viewModel = viewModel)
                SubScreen.FOCUS -> FocusScreen(viewModel = viewModel)
                SubScreen.SHOPPING, SubScreen.WISHLIST -> ShoppingScreen(viewModel = viewModel)
                SubScreen.CONTACTS -> ContactsScreen(viewModel = viewModel)
                SubScreen.DIARY -> NotesScreen(viewModel = viewModel)
                SubScreen.SEARCH -> GlobalSearchScreen(viewModel = viewModel, onBack = { viewModel.navigateBack() })
                SubScreen.AI_ASSISTANT -> LifeAiScreen(viewModel = viewModel, onBack = { viewModel.navigateBack() })
                SubScreen.SETTINGS -> SettingsScreen(viewModel = viewModel, onBack = { viewModel.navigateBack() })
                SubScreen.NONE -> {
                    when (currentTab) {
                        AppTab.HOME -> HomeScreen(viewModel = viewModel)
                        AppTab.PLANNER -> PlannerScreen(viewModel = viewModel)
                        AppTab.CALENDAR -> CalendarScreen(viewModel = viewModel)
                        AppTab.MONEY -> MoneyScreen(viewModel = viewModel)
                        AppTab.STUDY -> StudyScreen(viewModel = viewModel)
                        AppTab.GOALS -> GoalsScreen(viewModel = viewModel)
                        AppTab.NOTES -> NotesScreen(viewModel = viewModel)
                        AppTab.MORE -> MoreScreen(viewModel = viewModel)
                    }
                }
            }
        }
    }

    if (showQuickAdd) {
        QuickAddModal(
            viewModel = viewModel,
            onDismiss = { viewModel.closeQuickAdd() }
        )
    }
}
