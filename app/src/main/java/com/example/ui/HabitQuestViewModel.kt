package com.example.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.firebase.AuthUserState
import com.example.data.firebase.CloudSyncState
import com.example.data.local.HabitQuestDatabase
import com.example.data.model.Achievement
import com.example.data.model.DayRecord
import com.example.data.model.Habit
import com.example.data.model.PlayerProfile
import com.example.data.repository.HabitQuestRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

data class HabitQuestUiState(
  val activeTab: String = "good",
  val habits: List<Habit> = emptyList(),
  val dayRecords: List<DayRecord> = emptyList(),
  val todayRecord: DayRecord = DayRecord(date = ""),
  val todayScore: Double = 0.0,
  val playerProfile: PlayerProfile = PlayerProfile(),
  val bossHp: Int = 500,
  val bossClaimed: Boolean = false,
  val weeklyProgress: Int = 0,
  val weeklyGoal: Int = 10,
  val weeklyClaimed: Boolean = false,
  val achievements: List<Achievement> = emptyList(),
  val authState: AuthUserState = AuthUserState(),
  val syncState: CloudSyncState = CloudSyncState(),
  val toastMessage: String? = null,
  val selectedCalendarDate: String = "",
  val themeMode: String = "dark" // "dark", "light", "system"
)

private data class CoreData(
  val habits: List<Habit>,
  val days: List<DayRecord>,
  val profile: PlayerProfile
)

private data class ViewControls(
  val tab: String,
  val selectedDate: String,
  val toast: String?,
  val themeMode: String
)

class HabitQuestViewModel(application: Application) : AndroidViewModel(application) {

  private val database = HabitQuestDatabase.getInstance(application)
  val repository = HabitQuestRepository(
    habitDao = database.habitDao(),
    dayRecordDao = database.dayRecordDao(),
    playerDao = database.playerDao()
  )

  private val prefs = application.getSharedPreferences("habitquest_prefs", Context.MODE_PRIVATE)

  private val _activeTab = MutableStateFlow("good")
  private val _selectedDate = MutableStateFlow(repository.getTodayDate())
  private val _toast = MutableStateFlow<String?>(null)
  private val _themeMode = MutableStateFlow(prefs.getString("theme_mode", "dark") ?: "dark")

  init {
    viewModelScope.launch {
      repository.initializeDefaultsIfNeeded()
    }
  }

  private val coreDataFlow = combine(
    repository.habitsFlow,
    repository.dayRecordsFlow,
    repository.playerProfileFlow
  ) { habits, days, profile ->
    CoreData(habits, days, profile)
  }

  private val controlsFlow = combine(
    _activeTab,
    _selectedDate,
    _toast,
    _themeMode
  ) { tab, date, toast, theme ->
    ViewControls(tab, date, toast, theme)
  }

  val uiState: StateFlow<HabitQuestUiState> = combine(
    coreDataFlow,
    repository.authManager.userStateFlow(),
    repository.syncManager.syncState,
    controlsFlow
  ) { core, auth, sync, controls ->
    val habits = core.habits
    val days = core.days
    val profile = core.profile

    val today = repository.getTodayDate()
    val todayRec = days.find { it.date == today } ?: DayRecord(date = today)
    val todayScore = todayRec.score ?: repository.calculateScore(todayRec, habits)

    val totalGoodAllTime = days.sumOf { it.completedGood }
    val bossHp = (500 - totalGoodAllTime * 5).coerceAtLeast(0)

    val cal = Calendar.getInstance()
    cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
    cal.set(Calendar.HOUR_OF_DAY, 0)
    cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MILLISECOND, 0)
    val weekStartStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(cal.time)
    val weeklyProgress = days.filter { it.date >= weekStartStr }.sumOf { it.completedGood }

    val totalActions = days.sumOf { it.actions.size }

    val achList = listOf(
      Achievement(
        id = "7_day_warrior",
        title = "7-Day Warrior",
        description = "Reach a 7-day streak",
        icon = "flame",
        isUnlocked = profile.bestStreak >= 7
      ),
      Achievement(
        id = "perfect_day",
        title = "Perfect Day",
        description = "Achieve 100% daily score",
        icon = "target",
        isUnlocked = days.any { (it.score ?: 0.0) >= 100.0 } || todayScore >= 100.0
      ),
      Achievement(
        id = "centurion",
        title = "Centurion",
        description = "Complete 100 habit actions",
        icon = "sword",
        isUnlocked = totalActions >= 100
      ),
      Achievement(
        id = "legend",
        title = "Legend",
        description = "Reach Player Level 50",
        icon = "crown",
        isUnlocked = profile.level >= 50
      ),
      Achievement(
        id = "hard_mode",
        title = "Hard Mode",
        description = "Build a 10-streak on a Hard habit",
        icon = "award",
        isUnlocked = profile.hardModeUnlocked || habits.any { it.difficulty == 15 && it.streak >= 10 }
      ),
      Achievement(
        id = "prepared",
        title = "Prepared",
        description = "Own at least one Streak Shield",
        icon = "shield",
        isUnlocked = profile.shields > 0
      )
    )

    HabitQuestUiState(
      activeTab = controls.tab,
      habits = habits,
      dayRecords = days,
      todayRecord = todayRec,
      todayScore = todayScore,
      playerProfile = profile,
      bossHp = bossHp,
      bossClaimed = profile.bossClaimed,
      weeklyProgress = weeklyProgress,
      weeklyGoal = 10,
      weeklyClaimed = profile.weekClaimed,
      achievements = achList,
      authState = auth,
      syncState = sync,
      toastMessage = controls.toast,
      selectedCalendarDate = controls.selectedDate,
      themeMode = controls.themeMode
    )
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5000),
    initialValue = HabitQuestUiState(todayRecord = DayRecord(date = repository.getTodayDate()))
  )

  fun switchTab(tab: String) {
    _activeTab.value = tab
  }

  fun selectCalendarDate(date: String) {
    _selectedDate.value = date
  }

  fun setThemeMode(mode: String) {
    _themeMode.value = mode
    prefs.edit().putString("theme_mode", mode).apply()
    viewModelScope.launch {
      repository.updateThemeMode(mode)
    }
  }

  fun toggleTheme() {
    val current = _themeMode.value
    val next = if (current == "dark") "light" else "dark"
    setThemeMode(next)
    showToast(if (next == "dark") "Dark Mode enabled" else "Light Mode enabled")
  }

  fun showToast(msg: String) {
    _toast.value = msg
  }

  fun clearToast() {
    _toast.value = null
  }

  fun recordHabitAction(habitId: String, answer: String) {
    viewModelScope.launch {
      val res = repository.recordHabitAction(habitId, answer)
      showToast(res)
    }
  }

  fun addHabit(name: String, type: String, difficulty: Int) {
    viewModelScope.launch {
      if (name.isBlank()) {
        showToast("Please enter a habit name")
        return@launch
      }
      repository.addHabit(name, type, difficulty)
      showToast("Habit added to quest log!")
    }
  }

  fun editHabit(id: String, name: String, type: String, difficulty: Int) {
    viewModelScope.launch {
      if (name.isBlank()) {
        showToast("Please enter a habit name")
        return@launch
      }
      repository.editHabit(id, name, type, difficulty)
      showToast("Habit updated!")
    }
  }

  fun archiveHabit(id: String) {
    viewModelScope.launch {
      repository.archiveHabit(id)
      showToast("Habit archived")
    }
  }

  fun restoreHabit(id: String) {
    viewModelScope.launch {
      repository.restoreHabit(id)
      showToast("Habit restored to quest log!")
    }
  }

  fun buyShopItem(itemIndex: Int) {
    viewModelScope.launch {
      val (ok, msg) = repository.buyShopItem(itemIndex)
      showToast(msg)
    }
  }

  fun claimBossReward() {
    viewModelScope.launch {
      val (ok, msg) = repository.claimBossReward()
      showToast(msg)
    }
  }

  fun claimWeeklyReward() {
    viewModelScope.launch {
      val (ok, msg) = repository.claimWeeklyReward()
      showToast(msg)
    }
  }

  fun updateHeroName(newName: String) {
    viewModelScope.launch {
      if (newName.isNotBlank()) {
        repository.updateHeroName(newName)
        showToast("Hero name updated!")
      }
    }
  }

  fun resetAllData() {
    viewModelScope.launch {
      repository.resetAllData()
      showToast("Progress reset.")
    }
  }

  fun loginFirebase(email: String, pass: String, onComplete: (Boolean, String) -> Unit) {
    viewModelScope.launch {
      val res = repository.authManager.signInWithEmail(email, pass)
      if (res.isSuccess) {
        showToast("Welcome back! Syncing...")
        repository.syncWithCloudNow()
        onComplete(true, "Signed in successfully!")
      } else {
        val err = res.exceptionOrNull()?.localizedMessage ?: "Sign in failed"
        showToast(err)
        onComplete(false, err)
      }
    }
  }

  fun registerFirebase(email: String, pass: String, onComplete: (Boolean, String) -> Unit) {
    viewModelScope.launch {
      val res = repository.authManager.signUpWithEmail(email, pass)
      if (res.isSuccess) {
        showToast("Account created! Uploading initial progress...")
        repository.triggerCloudSync()
        onComplete(true, "Account created!")
      } else {
        val err = res.exceptionOrNull()?.localizedMessage ?: "Registration failed"
        showToast(err)
        onComplete(false, err)
      }
    }
  }

  fun signOutFirebase() {
    repository.authManager.signOut()
    showToast("Signed out.")
  }

  fun syncNow() {
    viewModelScope.launch {
      val res = repository.syncWithCloudNow()
      if (res.isSuccess) {
        showToast("Synced with cloud!")
      } else {
        showToast(res.exceptionOrNull()?.localizedMessage ?: "Sync error")
      }
    }
  }
}
