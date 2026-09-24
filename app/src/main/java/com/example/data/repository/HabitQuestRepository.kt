package com.example.data.repository

import com.example.data.firebase.FirebaseAuthManager
import com.example.data.firebase.FirestoreSyncManager
import com.example.data.local.DayRecordDao
import com.example.data.local.DayRecordEntity
import com.example.data.local.HabitDao
import com.example.data.local.HabitEntity
import com.example.data.local.PlayerDao
import com.example.data.local.PlayerProfileEntity
import com.example.data.model.DayRecord
import com.example.data.model.Habit
import com.example.data.model.HabitMeta
import com.example.data.model.PlayerProfile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID

class HabitQuestRepository(
  private val habitDao: HabitDao,
  private val dayRecordDao: DayRecordDao,
  private val playerDao: PlayerDao,
  val authManager: FirebaseAuthManager = FirebaseAuthManager(),
  val syncManager: FirestoreSyncManager = FirestoreSyncManager(),
  private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {

  private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

  fun getTodayDate(): String = dateFormat.format(Date())

  val habitsFlow: Flow<List<Habit>> = habitDao.getAllHabits().map { list ->
    list.map { it.toDomain() }
  }

  val dayRecordsFlow: Flow<List<DayRecord>> = dayRecordDao.getAllDayRecords().map { list ->
    list.map { it.toDomain() }
  }

  val playerProfileFlow: Flow<PlayerProfile> = playerDao.getPlayerProfile().map { entity ->
    entity?.toDomain() ?: PlayerProfile()
  }

  suspend fun initializeDefaultsIfNeeded() {
    val currentHabits = habitDao.getAllHabits().first()
    if (currentHabits.isEmpty()) {
      val defaultHabits = listOf(
        Habit(id = UUID.randomUUID().toString(), name = "Jump Rope & Calisthenics", type = "good", difficulty = 15, streak = 4),
        Habit(id = UUID.randomUUID().toString(), name = "Acoustic Guitar Practice", type = "good", difficulty = 10, streak = 2),
        Habit(id = UUID.randomUUID().toString(), name = "Late Night Screen Time", type = "bad", difficulty = 15, streak = 1),
        Habit(id = UUID.randomUUID().toString(), name = "Unnecessary Snacking", type = "bad", difficulty = 10, streak = 0),
        Habit(id = UUID.randomUUID().toString(), name = "Skipping Water", type = "bad", difficulty = 5, streak = 0)
      )
      habitDao.insertAll(defaultHabits.map { HabitEntity.fromDomain(it) })
    }

    val currentProfile = playerDao.getPlayerProfileSync()
    if (currentProfile == null) {
      playerDao.insertOrUpdate(PlayerProfileEntity.fromDomain(PlayerProfile(streak = 4, bestStreak = 4)))
    }
  }

  suspend fun addHabit(name: String, type: String, difficulty: Int): Habit {
    val habit = Habit(
      id = UUID.randomUUID().toString(),
      name = name.trim(),
      type = type,
      difficulty = difficulty,
      streak = 0
    )
    habitDao.insertHabit(HabitEntity.fromDomain(habit))
    triggerCloudSync()
    return habit
  }

  suspend fun editHabit(id: String, name: String, type: String, difficulty: Int) {
    val existing = habitDao.getHabitById(id) ?: return
    val updated = existing.copy(
      name = name.trim(),
      type = type,
      difficulty = difficulty
    )
    habitDao.updateHabit(updated)
    triggerCloudSync()
  }

  suspend fun archiveHabit(id: String) {
    val existing = habitDao.getHabitById(id) ?: return
    val today = getTodayDate()
    habitDao.updateHabit(existing.copy(archived = true, archivedAt = today))
    triggerCloudSync()
  }

  suspend fun restoreHabit(id: String) {
    val existing = habitDao.getHabitById(id) ?: return
    habitDao.updateHabit(existing.copy(archived = false, archivedAt = null))
    triggerCloudSync()
  }

  suspend fun recordHabitAction(habitId: String, answer: String): String {
    val today = getTodayDate()
    val habits = habitDao.getAllHabits().first().map { it.toDomain() }
    val habit = habits.find { it.id == habitId } ?: return "Habit not found"

    var day = dayRecordDao.getDayRecordByDate(today)?.toDomain()
      ?: DayRecord(date = today)

    if (day.actions.containsKey(habitId)) {
      return "Already answered today"
    }

    var profile = playerDao.getPlayerProfileSync()?.toDomain() ?: PlayerProfile()

    // Latch hard mode if difficulty was 15 and streak >= 10
    var hardModeUnlocked = profile.hardModeUnlocked
    if (habits.any { it.difficulty == 15 && it.streak >= 10 }) {
      hardModeUnlocked = true
    }

    val isGood = habit.type == "good"
    val did = answer == "did"

    val newActions = day.actions.toMutableMap()
    newActions[habitId] = answer

    val newMeta = day.meta.toMutableMap()
    newMeta[habitId] = HabitMeta(type = habit.type, difficulty = habit.difficulty)

    var hp = profile.hp
    var xp = profile.xp
    var level = profile.level
    var coins = profile.coins
    var streak = profile.streak
    var shields = profile.shields
    var completedGood = day.completedGood
    var completedBad = day.completedBad
    var habitStreak = habit.streak

    if (isGood && did) {
      hp = (hp + habit.difficulty).coerceIn(0, 100)
      xp += habit.difficulty
      coins += habit.difficulty * 2
      habitStreak++
      completedGood++
      streak++
    } else if (isGood && !did) {
      if (shields > 0) {
        shields--
      } else {
        hp = (hp - habit.difficulty).coerceIn(0, 100)
        xp = (xp - habit.difficulty).coerceAtLeast(0)
        habitStreak = 0
        streak = 0
      }
    } else if (!isGood && did) {
      // Bad habit done
      if (shields > 0) {
        shields--
      } else {
        hp = (hp - habit.difficulty).coerceIn(0, 100)
        xp = (xp - habit.difficulty).coerceAtLeast(0)
        habitStreak = 0
        streak = 0
      }
      completedBad++
    } else {
      // Bad habit avoided (didn't do it)
      hp = (hp + habit.difficulty).coerceIn(0, 100)
      habitStreak++
      streak++
    }

    // Level up check
    while (xp >= 100) {
      xp -= 100
      level++
      coins += 100
    }

    if (hp == 100) {
      coins += 5 // bonus for full health
    }

    val bestStreak = maxOf(profile.bestStreak, streak)

    // Save updated habit
    habitDao.updateHabit(
      HabitEntity.fromDomain(
        habit.copy(streak = habitStreak)
      )
    )

    // Calculate score
    val updatedDayPreScore = day.copy(
      actions = newActions,
      meta = newMeta,
      completedGood = completedGood,
      completedBad = completedBad
    )

    val score = calculateScore(updatedDayPreScore, habits)
    val finalDay = updatedDayPreScore.copy(score = score)

    dayRecordDao.insertDayRecord(DayRecordEntity.fromDomain(finalDay))

    val updatedProfile = profile.copy(
      hp = hp,
      xp = xp,
      level = level,
      coins = coins,
      streak = streak,
      bestStreak = bestStreak,
      shields = shields,
      hardModeUnlocked = hardModeUnlocked
    )
    playerDao.insertOrUpdate(PlayerProfileEntity.fromDomain(updatedProfile))

    triggerCloudSync()
    return "Recorded quest!"
  }

  fun calculateScore(day: DayRecord, allHabits: List<Habit>): Double {
    var goodCount = 0
    var badCount = 0

    allHabits.forEach { h ->
      val answered = day.actions.containsKey(h.id)
      if (h.archived && !answered) return@forEach
      val type = if (answered && day.meta.containsKey(h.id)) {
        day.meta[h.id]!!.type
      } else {
        h.type
      }
      if (type == "good") goodCount++ else badCount++
    }

    if (goodCount == 0) return 0.0
    val goodPart = (day.completedGood.toDouble() / goodCount.toDouble()) * 100.0
    val badPart = if (badCount > 0) (day.completedBad.toDouble() / badCount.toDouble()) * 100.0 else 0.0
    return (goodPart - badPart).coerceIn(0.0, 100.0)
  }

  suspend fun buyShopItem(itemIndex: Int): Pair<Boolean, String> {
    val profile = playerDao.getPlayerProfileSync()?.toDomain() ?: return Pair(false, "Profile error")
    val costs = listOf(500, 350, 250, 750)
    val names = listOf("Streak Shield", "XP Boost", "Full Heal", "Hero Costume")
    if (itemIndex !in costs.indices) return Pair(false, "Invalid item")

    val cost = costs[itemIndex]
    if (profile.coins < cost) {
      return Pair(false, "Not enough gold! Need $cost gold.")
    }

    var hp = profile.hp
    var xp = profile.xp
    var level = profile.level
    var coins = profile.coins - cost
    var shields = profile.shields
    var avatar = profile.avatar

    when (itemIndex) {
      0 -> shields++
      1 -> {
        xp += 100
        while (xp >= 100) {
          xp -= 100
          level++
          coins += 100
        }
      }
      2 -> hp = 100
      3 -> avatar = "🦸"
    }

    val updated = profile.copy(
      hp = hp,
      xp = xp,
      level = level,
      coins = coins,
      shields = shields,
      avatar = avatar
    )
    playerDao.insertOrUpdate(PlayerProfileEntity.fromDomain(updated))
    triggerCloudSync()
    return Pair(true, "Purchased ${names[itemIndex]}!")
  }

  suspend fun claimBossReward(): Pair<Boolean, String> {
    val profile = playerDao.getPlayerProfileSync()?.toDomain() ?: return Pair(false, "Error")
    if (profile.bossClaimed) return Pair(false, "Boss reward already claimed!")
    val allDays = dayRecordDao.getAllDayRecords().first().map { it.toDomain() }
    val totalGood = allDays.sumOf { it.completedGood }
    val bossHp = (500 - totalGood * 5).coerceAtLeast(0)
    if (bossHp > 0) return Pair(false, "Boss is still alive with $bossHp HP!")

    val updated = profile.copy(
      bossClaimed = true,
      coins = profile.coins + 500
    )
    playerDao.insertOrUpdate(PlayerProfileEntity.fromDomain(updated))
    triggerCloudSync()
    return Pair(true, "Boss defeated! Claimed 500 Gold!")
  }

  suspend fun claimWeeklyReward(): Pair<Boolean, String> {
    val profile = playerDao.getPlayerProfileSync()?.toDomain() ?: return Pair(false, "Error")
    if (profile.weekClaimed) return Pair(false, "Weekly reward already claimed!")

    val allDays = dayRecordDao.getAllDayRecords().first().map { it.toDomain() }
    val cal = Calendar.getInstance()
    cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
    cal.set(Calendar.HOUR_OF_DAY, 0)
    cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MILLISECOND, 0)
    val weekStartStr = dateFormat.format(cal.time)

    val weekGood = allDays.filter { it.date >= weekStartStr }.sumOf { it.completedGood }
    if (weekGood < 10) return Pair(false, "Only $weekGood/10 completed this week!")

    val updated = profile.copy(
      weekClaimed = true,
      coins = profile.coins + 300
    )
    playerDao.insertOrUpdate(PlayerProfileEntity.fromDomain(updated))
    triggerCloudSync()
    return Pair(true, "Weekly challenge complete! Claimed 300 Gold!")
  }

  suspend fun updateHeroName(newName: String) {
    val profile = playerDao.getPlayerProfileSync()?.toDomain() ?: return
    playerDao.insertOrUpdate(PlayerProfileEntity.fromDomain(profile.copy(name = newName.trim())))
    triggerCloudSync()
  }

  suspend fun updateThemeMode(themeMode: String) {
    val profile = playerDao.getPlayerProfileSync()?.toDomain() ?: return
    playerDao.insertOrUpdate(PlayerProfileEntity.fromDomain(profile.copy(themeMode = themeMode)))
    triggerCloudSync()
  }

  suspend fun resetAllData() {
    habitDao.deleteAll()
    dayRecordDao.deleteAll()
    playerDao.deleteAll()
    initializeDefaultsIfNeeded()
    triggerCloudSync()
  }

  fun triggerCloudSync() {
    scope.launch {
      val currentUser = authManager.getCurrentUser() ?: return@launch
      val profile = playerDao.getPlayerProfileSync()?.toDomain() ?: return@launch
      val habits = habitDao.getAllHabits().first().map { it.toDomain() }
      val days = dayRecordDao.getAllDayRecords().first().map { it.toDomain() }
      syncManager.pushDataToCloud(currentUser.uid, profile, habits, days)
    }
  }

  suspend fun syncWithCloudNow(): Result<Unit> {
    val currentUser = authManager.getCurrentUser()
      ?: return Result.failure(IllegalStateException("Please log in with Firebase to sync."))

    val cloudResult = syncManager.pullDataFromCloud(currentUser.uid)
    if (cloudResult.isSuccess) {
      val cloudData = cloudResult.getOrNull()
      if (cloudData != null) {
        val (cloudProfile, cloudHabits, cloudDays) = cloudData
        if (cloudHabits.isNotEmpty()) {
          habitDao.deleteAll()
          habitDao.insertAll(cloudHabits.map { HabitEntity.fromDomain(it) })
        }
        if (cloudDays.isNotEmpty()) {
          dayRecordDao.deleteAll()
          dayRecordDao.insertAll(cloudDays.map { DayRecordEntity.fromDomain(it) })
        }
        playerDao.insertOrUpdate(PlayerProfileEntity.fromDomain(cloudProfile.copy(lastSyncedAt = System.currentTimeMillis())))
        return Result.success(Unit)
      }
    }

    // If cloud had no data or was empty, push local state up!
    val profile = playerDao.getPlayerProfileSync()?.toDomain() ?: PlayerProfile()
    val habits = habitDao.getAllHabits().first().map { it.toDomain() }
    val days = dayRecordDao.getAllDayRecords().first().map { it.toDomain() }
    return syncManager.pushDataToCloud(currentUser.uid, profile, habits, days)
  }
}
