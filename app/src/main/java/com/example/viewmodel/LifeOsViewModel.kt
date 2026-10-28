package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.database.AppDatabase
import com.example.data.local.entity.*
import com.example.data.repository.LifeOsRepository
import com.example.ui.screens.ai.AiParsedResult
import com.example.ui.screens.ai.LifeAiEngine
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

enum class AppTab {
    HOME, PLANNER, CALENDAR, MONEY, STUDY, GOALS, NOTES, MORE
}

enum class SubScreen {
    NONE, UDHAR, FOCUS, SHOPPING, WISHLIST, CONTACTS, DIARY, SEARCH, SETTINGS, AI_ASSISTANT
}

enum class PlannerView {
    TODAY, TOMORROW, THIS_WEEK, NEXT_WEEK, THIS_MONTH, ALL, OVERDUE, COMPLETED
}

data class DashboardWidgetConfig(
    val showTasks: Boolean = true,
    val showSchedule: Boolean = true,
    val showMoney: Boolean = true,
    val showStudy: Boolean = true,
    val showGoals: Boolean = true,
    val showQuickNotes: Boolean = true,
    val showReminders: Boolean = true,
    val showSummary: Boolean = true,
    val isCompactLayout: Boolean = false
)

data class AiMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val text: String,
    val isUser: Boolean,
    val proposedAction: AiParsedResult? = null,
    val isActionExecuted: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

class LifeOsViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = LifeOsRepository(AppDatabase.getInstance(application))

    // Navigation State
    private val _currentTab = MutableStateFlow(AppTab.HOME)
    val currentTab: StateFlow<AppTab> = _currentTab.asStateFlow()

    private val _currentSubScreen = MutableStateFlow(SubScreen.NONE)
    val currentSubScreen: StateFlow<SubScreen> = _currentSubScreen.asStateFlow()

    fun navigateToTab(tab: AppTab) {
        _currentTab.value = tab
        _currentSubScreen.value = SubScreen.NONE
    }

    fun navigateToSubScreen(subScreen: SubScreen) {
        _currentSubScreen.value = subScreen
    }

    fun navigateBack(): Boolean {
        if (_currentSubScreen.value != SubScreen.NONE) {
            _currentSubScreen.value = SubScreen.NONE
            return true
        }
        if (_currentTab.value != AppTab.HOME) {
            _currentTab.value = AppTab.HOME
            return true
        }
        return false
    }

    // Settings & Preferences
    val allPreferences: StateFlow<List<UserPreferenceEntity>> = repository.allPreferences
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _currency = MutableStateFlow("₹")
    val currency: StateFlow<String> = _currency.asStateFlow()

    private val _userName = MutableStateFlow("Alex")
    val userName: StateFlow<String> = _userName.asStateFlow()

    private val _isDarkTheme = MutableStateFlow(true)
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    private val _dashboardConfig = MutableStateFlow(DashboardWidgetConfig())
    val dashboardConfig: StateFlow<DashboardWidgetConfig> = _dashboardConfig.asStateFlow()

    fun updateDashboardConfig(newConfig: DashboardWidgetConfig) {
        _dashboardConfig.value = newConfig
    }

    fun setCurrency(newCurrency: String) {
        _currency.value = newCurrency
        viewModelScope.launch { repository.setPreference("currency", newCurrency) }
    }

    fun setUserName(name: String) {
        _userName.value = name
        viewModelScope.launch { repository.setPreference("user_name", name) }
    }

    fun toggleTheme() {
        _isDarkTheme.value = !_isDarkTheme.value
    }

    // Tasks State
    val allTasks: StateFlow<List<TaskEntity>> = repository.allTasks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val todayStr: String = LocalDate.now().toString()
    val tomorrowStr: String = LocalDate.now().plusDays(1).toString()

    val todayTasks: StateFlow<List<TaskEntity>> = repository.getTasksByDate(todayStr)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val overdueTasks: StateFlow<List<TaskEntity>> = repository.getOverdueTasks(todayStr)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _plannerView = MutableStateFlow(PlannerView.TODAY)
    val plannerView: StateFlow<PlannerView> = _plannerView.asStateFlow()

    fun setPlannerView(view: PlannerView) {
        _plannerView.value = view
    }

    fun toggleTaskCompletion(task: TaskEntity) {
        viewModelScope.launch {
            repository.setTaskCompleted(task.id, !task.isCompleted)
        }
    }

    fun saveTask(task: TaskEntity) {
        viewModelScope.launch {
            if (task.id == 0L) repository.insertTask(task)
            else repository.updateTask(task)
        }
    }

    fun deleteTask(task: TaskEntity) {
        viewModelScope.launch { repository.deleteTask(task) }
    }

    // Calendar & Events
    val allEvents: StateFlow<List<EventEntity>> = repository.allEvents
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val upcomingEvents: StateFlow<List<EventEntity>> = repository.getUpcomingEvents(todayStr)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedCalendarDate = MutableStateFlow(LocalDate.now().toString())
    val selectedCalendarDate: StateFlow<String> = _selectedCalendarDate.asStateFlow()

    fun selectCalendarDate(date: String) {
        _selectedCalendarDate.value = date
    }

    fun saveEvent(event: EventEntity) {
        viewModelScope.launch {
            if (event.id == 0L) repository.insertEvent(event)
            else repository.updateEvent(event)
        }
    }

    fun deleteEvent(event: EventEntity) {
        viewModelScope.launch { repository.deleteEvent(event) }
    }

    // Finance State
    val allAccounts: StateFlow<List<AccountEntity>> = repository.allAccounts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTransactions: StateFlow<List<TransactionEntity>> = repository.allTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allBudgets: StateFlow<List<BudgetEntity>> = repository.allBudgets
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun saveAccount(account: AccountEntity) {
        viewModelScope.launch {
            if (account.id == 0L) repository.insertAccount(account)
            else repository.updateAccount(account)
        }
    }

    fun deleteAccount(account: AccountEntity) {
        viewModelScope.launch { repository.deleteAccount(account) }
    }

    fun saveTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            repository.insertTransaction(transaction)
        }
    }

    fun deleteTransaction(transaction: TransactionEntity) {
        viewModelScope.launch { repository.deleteTransaction(transaction) }
    }

    // Debts (Udhar)
    val allDebts: StateFlow<List<DebtEntity>> = repository.allDebts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun saveDebt(debt: DebtEntity) {
        viewModelScope.launch {
            if (debt.id == 0L) repository.insertDebt(debt)
            else repository.updateDebt(debt)
        }
    }

    fun deleteDebt(debt: DebtEntity) {
        viewModelScope.launch { repository.deleteDebt(debt) }
    }

    fun recordRepayment(debtId: Long, amount: Double, note: String, accountId: Long?) {
        viewModelScope.launch {
            repository.recordDebtRepayment(
                debtId = debtId,
                repaymentAmount = amount,
                date = LocalDate.now().toString(),
                note = note,
                targetAccountId = accountId
            )
        }
    }

    // Study & College
    val allSubjects: StateFlow<List<SubjectEntity>> = repository.allSubjects
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTimetableSlots: StateFlow<List<TimetableSlotEntity>> = repository.allTimetableSlots
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allAssignments: StateFlow<List<AssignmentEntity>> = repository.allAssignments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allExams: StateFlow<List<ExamEntity>> = repository.allExams
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allStudySessions: StateFlow<List<StudySessionEntity>> = repository.allStudySessions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun saveSubject(subject: SubjectEntity) {
        viewModelScope.launch { repository.insertSubject(subject) }
    }

    fun deleteSubject(subject: SubjectEntity) {
        viewModelScope.launch { repository.deleteSubject(subject) }
    }

    fun saveTimetableSlot(slot: TimetableSlotEntity) {
        viewModelScope.launch { repository.insertTimetableSlot(slot) }
    }

    fun deleteTimetableSlot(slot: TimetableSlotEntity) {
        viewModelScope.launch { repository.deleteTimetableSlot(slot) }
    }

    fun saveAssignment(assignment: AssignmentEntity) {
        viewModelScope.launch {
            if (assignment.id == 0L) repository.insertAssignment(assignment)
            else repository.updateAssignment(assignment)
        }
    }

    fun deleteAssignment(assignment: AssignmentEntity) {
        viewModelScope.launch { repository.deleteAssignment(assignment) }
    }

    fun saveExam(exam: ExamEntity) {
        viewModelScope.launch { repository.insertExam(exam) }
    }

    fun deleteExam(exam: ExamEntity) {
        viewModelScope.launch { repository.deleteExam(exam) }
    }

    fun logStudySession(subjectName: String, subjectId: Long, durationMinutes: Int, notes: String) {
        viewModelScope.launch {
            repository.logStudySession(
                StudySessionEntity(
                    subjectId = subjectId,
                    subjectName = subjectName,
                    date = LocalDate.now().toString(),
                    plannedMinutes = durationMinutes,
                    actualMinutes = durationMinutes,
                    notes = notes
                )
            )
        }
    }

    // Goals & Habits
    val allGoals: StateFlow<List<GoalEntity>> = repository.allGoals
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allHabits: StateFlow<List<HabitEntity>> = repository.allHabits
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val todayHabitLogs: StateFlow<List<HabitLogEntity>> = repository.getHabitLogsForDate(todayStr)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun saveGoal(goal: GoalEntity) {
        viewModelScope.launch {
            if (goal.id == 0L) repository.insertGoal(goal)
            else repository.updateGoal(goal)
        }
    }

    fun deleteGoal(goal: GoalEntity) {
        viewModelScope.launch { repository.deleteGoal(goal) }
    }

    fun saveHabit(habit: HabitEntity) {
        viewModelScope.launch {
            if (habit.id == 0L) repository.insertHabit(habit)
            else repository.updateHabit(habit)
        }
    }

    fun deleteHabit(habit: HabitEntity) {
        viewModelScope.launch { repository.deleteHabit(habit) }
    }

    fun toggleHabitToday(habit: HabitEntity, currentlyDone: Boolean) {
        viewModelScope.launch {
            repository.toggleHabit(habit, todayStr, currentlyDone)
        }
    }

    // Notes & Diary
    val activeNotes: StateFlow<List<NoteEntity>> = repository.activeNotes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allDiaryEntries: StateFlow<List<DiaryEntity>> = repository.allDiaryEntries
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun saveNote(note: NoteEntity) {
        viewModelScope.launch {
            if (note.id == 0L) repository.insertNote(note)
            else repository.updateNote(note)
        }
    }

    fun deleteNote(note: NoteEntity) {
        viewModelScope.launch { repository.deleteNote(note) }
    }

    fun saveDiaryEntry(entry: DiaryEntity) {
        viewModelScope.launch { repository.insertDiaryEntry(entry) }
    }

    fun deleteDiaryEntry(entry: DiaryEntity) {
        viewModelScope.launch { repository.deleteDiaryEntry(entry) }
    }

    // Shopping & Wishlist
    val allShoppingItems: StateFlow<List<ShoppingItemEntity>> = repository.allShoppingItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allWishlistItems: StateFlow<List<WishlistItemEntity>> = repository.allWishlistItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun saveShoppingItem(item: ShoppingItemEntity) {
        viewModelScope.launch {
            if (item.id == 0L) repository.insertShoppingItem(item)
            else repository.updateShoppingItem(item)
        }
    }

    fun toggleShoppingItemPurchased(item: ShoppingItemEntity, createExpense: Boolean = false, accountId: Long? = null) {
        viewModelScope.launch {
            val updated = item.copy(isPurchased = !item.isPurchased)
            repository.updateShoppingItem(updated)
            if (updated.isPurchased && createExpense && accountId != null && item.estimatedPrice > 0) {
                repository.insertTransaction(
                    TransactionEntity(
                        type = "Expense",
                        amount = item.estimatedPrice,
                        date = LocalDate.now().toString(),
                        category = "Shopping",
                        accountId = accountId,
                        description = "Bought: ${item.name}"
                    )
                )
            }
        }
    }

    fun deleteShoppingItem(item: ShoppingItemEntity) {
        viewModelScope.launch { repository.deleteShoppingItem(item) }
    }

    fun saveWishlistItem(item: WishlistItemEntity) {
        viewModelScope.launch {
            if (item.id == 0L) repository.insertWishlistItem(item)
            else repository.updateWishlistItem(item)
        }
    }

    fun deleteWishlistItem(item: WishlistItemEntity) {
        viewModelScope.launch { repository.deleteWishlistItem(item) }
    }

    // Contacts & Focus
    val allContacts: StateFlow<List<ContactEntity>> = repository.allContacts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentFocusLogs: StateFlow<List<FocusLogEntity>> = repository.recentFocusLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun saveContact(contact: ContactEntity) {
        viewModelScope.launch {
            if (contact.id == 0L) repository.insertContact(contact)
            else repository.updateContact(contact)
        }
    }

    fun deleteContact(contact: ContactEntity) {
        viewModelScope.launch { repository.deleteContact(contact) }
    }

    // Focus Timer State
    private var focusTimerJob: Job? = null
    private val _focusTimeRemaining = MutableStateFlow(25 * 60) // 25 min default
    val focusTimeRemaining: StateFlow<Int> = _focusTimeRemaining.asStateFlow()

    private val _focusInitialTime = MutableStateFlow(25 * 60)
    val focusInitialTime: StateFlow<Int> = _focusInitialTime.asStateFlow()

    private val _isFocusRunning = MutableStateFlow(false)
    val isFocusRunning: StateFlow<Boolean> = _isFocusRunning.asStateFlow()

    private val _focusTaskName = MutableStateFlow("General Focus")
    val focusTaskName: StateFlow<String> = _focusTaskName.asStateFlow()

    fun setFocusPreset(minutes: Int, taskName: String = "General Focus") {
        pauseFocusTimer()
        _focusInitialTime.value = minutes * 60
        _focusTimeRemaining.value = minutes * 60
        _focusTaskName.value = taskName
    }

    fun startFocusTimer() {
        if (_isFocusRunning.value) return
        _isFocusRunning.value = true
        focusTimerJob = viewModelScope.launch {
            while (_focusTimeRemaining.value > 0 && _isFocusRunning.value) {
                delay(1000L)
                _focusTimeRemaining.value -= 1
            }
            if (_focusTimeRemaining.value <= 0) {
                _isFocusRunning.value = false
                val completedMinutes = _focusInitialTime.value / 60
                repository.insertFocusLog(
                    FocusLogEntity(
                        taskTitle = _focusTaskName.value,
                        durationMinutes = completedMinutes,
                        mode = "Focus Session"
                    )
                )
            }
        }
    }

    fun pauseFocusTimer() {
        _isFocusRunning.value = false
        focusTimerJob?.cancel()
    }

    fun finishFocusEarly() {
        val elapsedSeconds = _focusInitialTime.value - _focusTimeRemaining.value
        val elapsedMinutes = (elapsedSeconds / 60).coerceAtLeast(1)
        pauseFocusTimer()
        _focusTimeRemaining.value = _focusInitialTime.value
        viewModelScope.launch {
            repository.insertFocusLog(
                FocusLogEntity(
                    taskTitle = _focusTaskName.value,
                    durationMinutes = elapsedMinutes,
                    mode = "Partial Focus"
                )
            )
        }
    }

    // Quick Add Dialog
    private val _showQuickAddModal = MutableStateFlow(false)
    val showQuickAddModal: StateFlow<Boolean> = _showQuickAddModal.asStateFlow()

    fun openQuickAdd() { _showQuickAddModal.value = true }
    fun closeQuickAdd() { _showQuickAddModal.value = false }

    // AI Assistant Chat & Actions
    private val _aiMessages = MutableStateFlow<List<AiMessage>>(
        listOf(
            AiMessage(
                text = "Welcome to LIFE OS Assistant. You can ask me in English, Hindi, or Hinglish:\n• 'Add ₹200 transport expense today'\n• 'Kal subah 8 baje college reminder lagao'\n• 'Rahul se ₹500 lene hain next Friday'\n• 'Show my pending tasks'\n• 'Summarize my spending this month'",
                isUser = false
            )
        )
    )
    val aiMessages: StateFlow<List<AiMessage>> = _aiMessages.asStateFlow()

    fun sendAiPrompt(prompt: String) {
        val userMsg = AiMessage(text = prompt, isUser = true)
        _aiMessages.value = _aiMessages.value + userMsg

        val parsed = LifeAiEngine.parseUserCommand(prompt)
        when (parsed) {
            is AiParsedResult.SummaryQuery -> {
                handleAiSummaryQuery(parsed.queryType)
            }
            is AiParsedResult.Unknown -> {
                val botMsg = AiMessage(text = parsed.message, isUser = false)
                _aiMessages.value = _aiMessages.value + botMsg
            }
            else -> {
                val previewText = when (parsed) {
                    is AiParsedResult.TaskAction -> "Parsed Task: \"${parsed.title}\" on ${parsed.date} at ${parsed.time} (${parsed.category}, ${parsed.priority}).\nReady to save to your schedule."
                    is AiParsedResult.TransactionAction -> "Parsed Transaction: ${parsed.type} of ₹${parsed.amount} for ${parsed.category} (${parsed.description}).\nReady to add to Money Manager."
                    is AiParsedResult.DebtAction -> "Parsed Udhar record: ${parsed.type} ₹${parsed.amount} with ${parsed.person} (Due: ${parsed.dueDate}).\nReady to save."
                    is AiParsedResult.StudySessionAction -> "Parsed Study Session: 60m of ${parsed.subject} for ${parsed.date}.\nReady to log."
                    is AiParsedResult.ShoppingItemAction -> "Parsed Shopping Item: ${parsed.name} (Qty: ${parsed.quantity}).\nReady to add."
                    else -> "Action parsed successfully."
                }
                val botMsg = AiMessage(
                    text = previewText,
                    isUser = false,
                    proposedAction = parsed
                )
                _aiMessages.value = _aiMessages.value + botMsg
            }
        }
    }

    private fun handleAiSummaryQuery(queryType: String) {
        if (queryType == "pending_tasks") {
            val pending = allTasks.value.filter { !it.isCompleted }
            val text = if (pending.isEmpty()) {
                "You have no pending tasks! You are all caught up. 🎉"
            } else {
                "Here are your pending tasks (${pending.size}):\n" +
                        pending.take(5).joinToString("\n") { "• [${it.priority}] ${it.title} (${it.date})" } +
                        if (pending.size > 5) "\n...and ${pending.size - 5} more." else ""
            }
            _aiMessages.value = _aiMessages.value + AiMessage(text = text, isUser = false)
        } else if (queryType == "spending_summary") {
            val thisMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"))
            val monthTx = allTransactions.value.filter { it.date.startsWith(thisMonth) }
            val expenses = monthTx.filter { it.type == "Expense" }.sumOf { it.amount }
            val income = monthTx.filter { it.type == "Income" }.sumOf { it.amount }
            val text = "📊 Monthly Spending Summary ($thisMonth):\n• Total Income: ₹${"%,.2f".format(income)}\n• Total Spent: ₹${"%,.2f".format(expenses)}\n• Net Balance Change: ₹${"%,.2f".format(income - expenses)}"
            _aiMessages.value = _aiMessages.value + AiMessage(text = text, isUser = false)
        }
    }

    fun confirmAiAction(messageId: String, action: AiParsedResult) {
        viewModelScope.launch {
            when (action) {
                is AiParsedResult.TaskAction -> {
                    repository.insertTask(
                        TaskEntity(
                            title = action.title,
                            date = action.date,
                            startTime = action.time,
                            category = action.category,
                            priority = action.priority,
                            description = "Added via LIFE AI Assistant"
                        )
                    )
                }
                is AiParsedResult.TransactionAction -> {
                    val defaultAccount = allAccounts.value.firstOrNull()?.id ?: 1L
                    repository.insertTransaction(
                        TransactionEntity(
                            type = action.type,
                            amount = action.amount,
                            date = action.date,
                            category = action.category,
                            accountId = defaultAccount,
                            description = action.description
                        )
                    )
                }
                is AiParsedResult.DebtAction -> {
                    repository.insertDebt(
                        DebtEntity(
                            personName = action.person,
                            type = action.type,
                            originalAmount = action.amount,
                            outstandingAmount = action.amount,
                            transactionDate = LocalDate.now().toString(),
                            dueDate = action.dueDate,
                            description = action.description
                        )
                    )
                }
                is AiParsedResult.StudySessionAction -> {
                    val subject = allSubjects.value.firstOrNull { it.name.contains(action.subject, ignoreCase = true) }
                    repository.logStudySession(
                        StudySessionEntity(
                            subjectId = subject?.id ?: 0L,
                            subjectName = action.subject,
                            date = action.date,
                            plannedMinutes = action.plannedMinutes,
                            actualMinutes = action.plannedMinutes,
                            notes = "Scheduled by LIFE AI"
                        )
                    )
                }
                is AiParsedResult.ShoppingItemAction -> {
                    repository.insertShoppingItem(
                        ShoppingItemEntity(
                            name = action.name,
                            estimatedPrice = action.estimatedPrice,
                            quantity = action.quantity
                        )
                    )
                }
                else -> {}
            }

            // Mark message as executed
            _aiMessages.value = _aiMessages.value.map { msg ->
                if (msg.id == messageId) msg.copy(isActionExecuted = true) else msg
            } + AiMessage(text = "✅ Confirmed & successfully saved to your system records!", isUser = false)
        }
    }
}
