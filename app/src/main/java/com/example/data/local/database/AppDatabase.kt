package com.example.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.dao.*
import com.example.data.local.entity.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate

@Database(
    entities = [
        TaskEntity::class,
        EventEntity::class,
        AccountEntity::class,
        TransactionEntity::class,
        BudgetEntity::class,
        DebtEntity::class,
        DebtRepaymentEntity::class,
        SubjectEntity::class,
        TimetableSlotEntity::class,
        AssignmentEntity::class,
        ExamEntity::class,
        StudySessionEntity::class,
        GoalEntity::class,
        HabitEntity::class,
        HabitLogEntity::class,
        NoteEntity::class,
        DiaryEntity::class,
        ShoppingItemEntity::class,
        WishlistItemEntity::class,
        ContactEntity::class,
        FocusLogEntity::class,
        UserPreferenceEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun eventDao(): EventDao
    abstract fun financeDao(): FinanceDao
    abstract fun debtDao(): DebtDao
    abstract fun studyDao(): StudyDao
    abstract fun goalHabitDao(): GoalHabitDao
    abstract fun noteDao(): NoteDao
    abstract fun shoppingDao(): ShoppingDao
    abstract fun otherDao(): OtherDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "life_os_database"
                )
                    .addCallback(DatabaseSeedCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabaseSeedCallback : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            CoroutineScope(Dispatchers.IO).launch {
                INSTANCE?.let { database ->
                    seedDefaultData(database)
                }
            }
        }

        private suspend fun seedDefaultData(db: AppDatabase) {
            val today = LocalDate.now().toString()
            val tomorrow = LocalDate.now().plusDays(1).toString()

            // Seed initial accounts
            val bankId = db.financeDao().insertAccount(
                AccountEntity(name = "Main Savings Account", type = "Bank", openingBalance = 24500.0, currency = "₹", colorHex = "#8B5CF6", isDefault = true)
            )
            val cashId = db.financeDao().insertAccount(
                AccountEntity(name = "Cash Wallet", type = "Cash", openingBalance = 3200.0, currency = "₹", colorHex = "#10B981")
            )

            // Seed sample transactions
            db.financeDao().insertTransaction(
                TransactionEntity(type = "Expense", amount = 180.0, date = today, category = "Food", accountId = cashId, description = "Cafeteria lunch & coffee", paymentMethod = "Cash")
            )
            db.financeDao().insertTransaction(
                TransactionEntity(type = "Expense", amount = 850.0, date = today, category = "Education", accountId = bankId, description = "Reference textbooks", paymentMethod = "UPI")
            )
            db.financeDao().insertTransaction(
                TransactionEntity(type = "Income", amount = 5000.0, date = today, category = "Freelance", accountId = bankId, description = "UI Design project milestone", paymentMethod = "Bank Transfer")
            )

            // Seed tasks
            db.taskDao().insertTask(
                TaskEntity(title = "Review Operating Systems Chapter 4", description = "Memory management and paging algorithms", date = today, startTime = "11:00", endTime = "12:30", category = "Study", priority = "High", status = "In Progress", estimatedMinutes = 90)
            )
            db.taskDao().insertTask(
                TaskEntity(title = "Submit Cloud Architecture Assignment", description = "PDF upload to college LMS portal", date = today, startTime = "17:00", endTime = "17:30", category = "College", priority = "Urgent", status = "Not Started", estimatedMinutes = 30)
            )
            db.taskDao().insertTask(
                TaskEntity(title = "Evening 5km Jog & Core Workout", description = "Track heart rate and maintain pace", date = today, startTime = "19:00", endTime = "19:45", category = "Health", priority = "Medium", status = "Not Started", estimatedMinutes = 45)
            )
            db.taskDao().insertTask(
                TaskEntity(title = "Buy groceries & protein oats", description = "Milk, peanut butter, brown bread, oats", date = tomorrow, startTime = "10:00", endTime = "11:00", category = "Shopping", priority = "Medium", status = "Not Started", estimatedMinutes = 60)
            )

            // Seed upcoming events
            db.eventDao().insertEvent(
                EventEntity(title = "Advanced Database Systems Lecture", description = "Room 304 - Prof. Sharma", date = today, startTime = "14:00", endTime = "15:30", eventType = "Lecture", colorHex = "#38BDF8")
            )
            db.eventDao().insertEvent(
                EventEntity(title = "Midterm Examination: Computer Networks", description = "Hall B - Bring student ID and blue pens", date = tomorrow, startTime = "09:30", endTime = "12:30", eventType = "Exam", colorHex = "#EF4444")
            )

            // Seed debts (Udhar)
            db.debtDao().insertDebt(
                DebtEntity(personName = "Rahul Verma", contactInfo = "+91 98765 43210", type = "Lent", originalAmount = 1500.0, repaidAmount = 500.0, outstandingAmount = 1000.0, transactionDate = today, dueDate = tomorrow, description = "Concert ticket advance", status = "Partially Repaid")
            )
            db.debtDao().insertDebt(
                DebtEntity(personName = "Priya Patel", contactInfo = "+91 91234 56789", type = "Borrowed", originalAmount = 400.0, repaidAmount = 0.0, outstandingAmount = 400.0, transactionDate = today, dueDate = tomorrow, description = "Split dinner bill", status = "Pending")
            )

            // Seed subjects
            val mathId = db.studyDao().insertSubject(
                SubjectEntity(name = "Discrete Mathematics", code = "CS-201", teacher = "Dr. Raman", classroom = "Room 202", colorHex = "#8B5CF6", targetWeeklyHours = 6.0)
            )
            val osId = db.studyDao().insertSubject(
                SubjectEntity(name = "Operating Systems", code = "CS-204", teacher = "Prof. Mehta", classroom = "Lab 3", colorHex = "#06B6D4", targetWeeklyHours = 8.0)
            )

            // Seed timetable
            db.studyDao().insertTimetableSlot(
                TimetableSlotEntity(subjectId = mathId, subjectName = "Discrete Math", dayOfWeek = LocalDate.now().dayOfWeek.value, startTime = "09:00", endTime = "10:15", room = "Room 202", teacher = "Dr. Raman")
            )
            db.studyDao().insertTimetableSlot(
                TimetableSlotEntity(subjectId = osId, subjectName = "Operating Systems", dayOfWeek = LocalDate.now().dayOfWeek.value, startTime = "11:30", endTime = "13:00", room = "Lab 3", teacher = "Prof. Mehta")
            )

            // Seed habits
            val h1 = db.goalHabitDao().insertHabit(
                HabitEntity(title = "Daily 30-min Reading", frequency = "Daily", targetDaysPerWeek = 7, colorHex = "#8B5CF6", currentStreak = 5, bestStreak = 12)
            )
            val h2 = db.goalHabitDao().insertHabit(
                HabitEntity(title = "Morning Deep Meditation", frequency = "Daily", targetDaysPerWeek = 7, colorHex = "#06B6D4", currentStreak = 3, bestStreak = 8)
            )
            db.goalHabitDao().insertHabitLog(HabitLogEntity(habitId = h1, date = today, isCompleted = true))

            // Seed Goals
            db.goalHabitDao().insertGoal(
                GoalEntity(title = "Build & Ship LIFE OS", description = "Complete all modules and deploy to production", category = "Career", targetDate = tomorrow, progressType = "Percent", targetValue = 100.0, currentValue = 85.0, status = "Active")
            )
            db.goalHabitDao().insertGoal(
                GoalEntity(title = "Save ₹50,000 Emergency Fund", description = "Maintain 3 months of survival expenses", category = "Finance", targetDate = "2026-12-31", progressType = "Numeric", targetValue = 50000.0, currentValue = 27700.0, status = "Active")
            )

            // Seed Notes & Diary
            db.noteDao().insertNote(
                NoteEntity(title = "System Architecture Ideas", content = "Decoupled MVVM with Room offline caching, instant search index, and safe AI intent routing.", category = "Ideas", isPinned = true, isFavorite = true)
            )
            db.noteDao().insertDiaryEntry(
                DiaryEntity(date = today, title = "High Focus Day", content = "Felt super energized organizing tasks and financial projections. Productive study session in the afternoon.", mood = "😊 Great")
            )

            // Seed Shopping & Wishlist
            db.shoppingDao().insertShoppingItem(
                ShoppingItemEntity(name = "Noise Cancelling Earbuds", quantity = "1", estimatedPrice = 2499.0, category = "Electronics", priority = "High", store = "Online")
            )
            db.shoppingDao().insertWishlistItem(
                WishlistItemEntity(name = "Ergonomic Mechanical Keyboard", description = "Split wireless keyboard with tactile switches", expectedPrice = 8500.0, priority = "High", savedAmount = 3000.0)
            )

            // Seed Contacts
            db.otherDao().insertContact(
                ContactEntity(name = "Rahul Verma", phone = "+91 98765 43210", email = "rahul.v@example.com", relationship = "College", birthday = "2002-11-14", notes = "Study group lead")
            )
            db.otherDao().insertContact(
                ContactEntity(name = "Priya Patel", phone = "+91 91234 56789", email = "priya.p@example.com", relationship = "Friend", birthday = "2003-04-22", notes = "Designer teammate")
            )

            // User preferences
            db.otherDao().setPreference(UserPreferenceEntity("user_name", "Alex"))
            db.otherDao().setPreference(UserPreferenceEntity("currency", "₹"))
            db.otherDao().setPreference(UserPreferenceEntity("theme", "Dark"))
        }
    }
}
