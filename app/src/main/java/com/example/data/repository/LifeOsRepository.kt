package com.example.data.repository

import com.example.data.local.database.AppDatabase
import com.example.data.local.entity.*
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

class LifeOsRepository(private val db: AppDatabase) {

    // Tasks
    val allTasks: Flow<List<TaskEntity>> = db.taskDao().getAllTasks()
    fun getTasksByDate(date: String): Flow<List<TaskEntity>> = db.taskDao().getTasksByDate(date)
    fun getOverdueTasks(todayDate: String): Flow<List<TaskEntity>> = db.taskDao().getOverdueTasks(todayDate)

    suspend fun insertTask(task: TaskEntity): Long = db.taskDao().insertTask(task)
    suspend fun updateTask(task: TaskEntity) = db.taskDao().updateTask(task)
    suspend fun deleteTask(task: TaskEntity) = db.taskDao().deleteTask(task)
    suspend fun setTaskCompleted(taskId: Long, completed: Boolean) = db.taskDao().setTaskCompleted(taskId, completed)
    suspend fun deleteTasksByIds(ids: List<Long>) = db.taskDao().deleteTasksByIds(ids)

    // Events
    val allEvents: Flow<List<EventEntity>> = db.eventDao().getAllEvents()
    fun getEventsByDate(date: String): Flow<List<EventEntity>> = db.eventDao().getEventsByDate(date)
    fun getUpcomingEvents(startDate: String): Flow<List<EventEntity>> = db.eventDao().getUpcomingEvents(startDate)

    suspend fun insertEvent(event: EventEntity): Long = db.eventDao().insertEvent(event)
    suspend fun updateEvent(event: EventEntity) = db.eventDao().updateEvent(event)
    suspend fun deleteEvent(event: EventEntity) = db.eventDao().deleteEvent(event)

    // Finance
    val allAccounts: Flow<List<AccountEntity>> = db.financeDao().getAllAccounts()
    val allTransactions: Flow<List<TransactionEntity>> = db.financeDao().getAllTransactions()
    val allBudgets: Flow<List<BudgetEntity>> = db.financeDao().getAllBudgets()

    fun getTransactionsForMonth(monthPrefix: String): Flow<List<TransactionEntity>> =
        db.financeDao().getTransactionsForMonth(monthPrefix)

    suspend fun insertAccount(account: AccountEntity): Long = db.financeDao().insertAccount(account)
    suspend fun updateAccount(account: AccountEntity) = db.financeDao().updateAccount(account)
    suspend fun deleteAccount(account: AccountEntity) = db.financeDao().deleteAccount(account)

    suspend fun insertTransaction(transaction: TransactionEntity): Long =
        db.financeDao().insertTransaction(transaction)

    suspend fun deleteTransaction(transaction: TransactionEntity) =
        db.financeDao().deleteTransaction(transaction)

    suspend fun insertBudget(budget: BudgetEntity) = db.financeDao().insertBudget(budget)

    // Debts (Udhar)
    val allDebts: Flow<List<DebtEntity>> = db.debtDao().getAllDebts()

    suspend fun insertDebt(debt: DebtEntity): Long = db.debtDao().insertDebt(debt)
    suspend fun updateDebt(debt: DebtEntity) = db.debtDao().updateDebt(debt)
    suspend fun deleteDebt(debt: DebtEntity) = db.debtDao().deleteDebt(debt)

    fun getRepaymentsForDebt(debtId: Long): Flow<List<DebtRepaymentEntity>> =
        db.debtDao().getRepaymentsForDebt(debtId)

    suspend fun recordDebtRepayment(
        debtId: Long,
        repaymentAmount: Double,
        date: String,
        note: String,
        targetAccountId: Long? = null
    ) {
        val debt = db.debtDao().getDebtById(debtId) ?: return
        val newRepaid = debt.repaidAmount + repaymentAmount
        val newOutstanding = (debt.originalAmount - newRepaid).coerceAtLeast(0.0)
        val newStatus = if (newOutstanding <= 0.0) "Settled" else "Partially Repaid"

        db.debtDao().insertRepayment(
            DebtRepaymentEntity(
                debtId = debtId,
                amount = repaymentAmount,
                date = date,
                note = note,
                accountId = targetAccountId
            )
        )

        db.debtDao().updateDebt(
            debt.copy(
                repaidAmount = newRepaid,
                outstandingAmount = newOutstanding,
                status = newStatus
            )
        )

        // Optionally reflect in money manager if account specified
        if (targetAccountId != null) {
            val transType = if (debt.type == "Lent") "Income" else "Expense"
            db.financeDao().insertTransaction(
                TransactionEntity(
                    type = transType,
                    amount = repaymentAmount,
                    date = date,
                    category = if (debt.type == "Lent") "Debt Received" else "Debt Paid",
                    accountId = targetAccountId,
                    description = "Repayment for ${debt.personName} - $note",
                    relatedPerson = debt.personName
                )
            )
        }
    }

    // Study
    val allSubjects: Flow<List<SubjectEntity>> = db.studyDao().getAllSubjects()
    val allTimetableSlots: Flow<List<TimetableSlotEntity>> = db.studyDao().getAllTimetableSlots()
    fun getSlotsForDay(dayOfWeek: Int): Flow<List<TimetableSlotEntity>> = db.studyDao().getSlotsForDay(dayOfWeek)
    val allAssignments: Flow<List<AssignmentEntity>> = db.studyDao().getAllAssignments()
    val allExams: Flow<List<ExamEntity>> = db.studyDao().getAllExams()
    val allStudySessions: Flow<List<StudySessionEntity>> = db.studyDao().getAllStudySessions()

    suspend fun insertSubject(subject: SubjectEntity): Long = db.studyDao().insertSubject(subject)
    suspend fun deleteSubject(subject: SubjectEntity) = db.studyDao().deleteSubject(subject)
    suspend fun insertTimetableSlot(slot: TimetableSlotEntity): Long = db.studyDao().insertTimetableSlot(slot)
    suspend fun deleteTimetableSlot(slot: TimetableSlotEntity) = db.studyDao().deleteTimetableSlot(slot)
    suspend fun insertAssignment(assignment: AssignmentEntity): Long = db.studyDao().insertAssignment(assignment)
    suspend fun updateAssignment(assignment: AssignmentEntity) = db.studyDao().updateAssignment(assignment)
    suspend fun deleteAssignment(assignment: AssignmentEntity) = db.studyDao().deleteAssignment(assignment)
    suspend fun insertExam(exam: ExamEntity): Long = db.studyDao().insertExam(exam)
    suspend fun deleteExam(exam: ExamEntity) = db.studyDao().deleteExam(exam)
    suspend fun logStudySession(session: StudySessionEntity): Long = db.studyDao().insertStudySession(session)

    // Goals & Habits
    val allGoals: Flow<List<GoalEntity>> = db.goalHabitDao().getAllGoals()
    val allHabits: Flow<List<HabitEntity>> = db.goalHabitDao().getAllHabits()

    suspend fun insertGoal(goal: GoalEntity): Long = db.goalHabitDao().insertGoal(goal)
    suspend fun updateGoal(goal: GoalEntity) = db.goalHabitDao().updateGoal(goal)
    suspend fun deleteGoal(goal: GoalEntity) = db.goalHabitDao().deleteGoal(goal)

    suspend fun insertHabit(habit: HabitEntity): Long = db.goalHabitDao().insertHabit(habit)
    suspend fun updateHabit(habit: HabitEntity) = db.goalHabitDao().updateHabit(habit)
    suspend fun deleteHabit(habit: HabitEntity) = db.goalHabitDao().deleteHabit(habit)

    fun getHabitLogsForDate(date: String): Flow<List<HabitLogEntity>> = db.goalHabitDao().getHabitLogsForDate(date)

    suspend fun toggleHabit(habit: HabitEntity, date: String, currentlyDone: Boolean) {
        if (currentlyDone) {
            db.goalHabitDao().deleteHabitLog(habit.id, date)
            val newStreak = (habit.currentStreak - 1).coerceAtLeast(0)
            db.goalHabitDao().updateHabit(habit.copy(currentStreak = newStreak))
        } else {
            db.goalHabitDao().insertHabitLog(HabitLogEntity(habitId = habit.id, date = date, isCompleted = true))
            val newStreak = habit.currentStreak + 1
            val newBest = maxOf(newStreak, habit.bestStreak)
            db.goalHabitDao().updateHabit(habit.copy(currentStreak = newStreak, bestStreak = newBest))
        }
    }

    // Notes & Diary
    val activeNotes: Flow<List<NoteEntity>> = db.noteDao().getActiveNotes()
    val allNotes: Flow<List<NoteEntity>> = db.noteDao().getAllNotes()
    val allDiaryEntries: Flow<List<DiaryEntity>> = db.noteDao().getAllDiaryEntries()

    suspend fun insertNote(note: NoteEntity): Long = db.noteDao().insertNote(note)
    suspend fun updateNote(note: NoteEntity) = db.noteDao().updateNote(note)
    suspend fun deleteNote(note: NoteEntity) = db.noteDao().deleteNote(note)

    suspend fun insertDiaryEntry(entry: DiaryEntity): Long = db.noteDao().insertDiaryEntry(entry)
    suspend fun deleteDiaryEntry(entry: DiaryEntity) = db.noteDao().deleteDiaryEntry(entry)

    // Shopping & Wishlist
    val allShoppingItems: Flow<List<ShoppingItemEntity>> = db.shoppingDao().getAllShoppingItems()
    val allWishlistItems: Flow<List<WishlistItemEntity>> = db.shoppingDao().getAllWishlistItems()

    suspend fun insertShoppingItem(item: ShoppingItemEntity): Long = db.shoppingDao().insertShoppingItem(item)
    suspend fun updateShoppingItem(item: ShoppingItemEntity) = db.shoppingDao().updateShoppingItem(item)
    suspend fun deleteShoppingItem(item: ShoppingItemEntity) = db.shoppingDao().deleteShoppingItem(item)

    suspend fun insertWishlistItem(item: WishlistItemEntity): Long = db.shoppingDao().insertWishlistItem(item)
    suspend fun updateWishlistItem(item: WishlistItemEntity) = db.shoppingDao().updateWishlistItem(item)
    suspend fun deleteWishlistItem(item: WishlistItemEntity) = db.shoppingDao().deleteWishlistItem(item)

    // Contacts, Focus, Preferences
    val allContacts: Flow<List<ContactEntity>> = db.otherDao().getAllContacts()
    val recentFocusLogs: Flow<List<FocusLogEntity>> = db.otherDao().getRecentFocusLogs()
    val allPreferences: Flow<List<UserPreferenceEntity>> = db.otherDao().getAllPreferences()

    suspend fun insertContact(contact: ContactEntity): Long = db.otherDao().insertContact(contact)
    suspend fun updateContact(contact: ContactEntity) = db.otherDao().updateContact(contact)
    suspend fun deleteContact(contact: ContactEntity) = db.otherDao().deleteContact(contact)

    suspend fun insertFocusLog(log: FocusLogEntity): Long = db.otherDao().insertFocusLog(log)

    suspend fun setPreference(key: String, value: String) =
        db.otherDao().setPreference(UserPreferenceEntity(key, value))

    suspend fun getPreference(key: String): String? = db.otherDao().getPreference(key)
}
