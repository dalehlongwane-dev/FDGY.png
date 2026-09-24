package com.example.data.model

data class Habit(
  val id: String,
  val name: String,
  val type: String, // "good" or "bad"
  val difficulty: Int, // 5 (Easy), 10 (Medium), 15 (Hard)
  val streak: Int = 0,
  val archived: Boolean = false,
  val archivedAt: String? = null,
  val createdAt: Long = System.currentTimeMillis()
)

data class HabitMeta(
  val type: String,
  val difficulty: Int
)

data class DayRecord(
  val date: String, // YYYY-MM-DD
  val actions: Map<String, String> = emptyMap(), // habitId -> "did" or "missed"
  val meta: Map<String, HabitMeta> = emptyMap(), // habitId -> {type, difficulty} snapshot
  val score: Double? = null,
  val completedGood: Int = 0,
  val completedBad: Int = 0
)

data class PlayerProfile(
  val name: String = "Habit Adventurer",
  val level: Int = 1,
  val hp: Int = 100,
  val xp: Int = 0,
  val coins: Int = 100,
  val streak: Int = 0,
  val bestStreak: Int = 0,
  val shields: Int = 0,
  val avatar: String = "🧙",
  val hardModeUnlocked: Boolean = false,
  val bossClaimed: Boolean = false,
  val weekClaimed: Boolean = false,
  val lastWeekStart: String = "",
  val notificationsEnabled: Boolean = false,
  val themeMode: String = "dark",
  val lastSyncedAt: Long = 0L
) {
  fun levelTitle(): String = when {
    level >= 50 -> "Legend"
    level >= 35 -> "Mythic Hero"
    level >= 25 -> "Master"
    level >= 15 -> "Champion"
    level >= 10 -> "Veteran"
    level >= 5 -> "Warrior"
    else -> "Novice"
  }
}

data class ShopItem(
  val id: String,
  val icon: String,
  val name: String,
  val cost: Int,
  val description: String
)

data class Achievement(
  val id: String,
  val title: String,
  val description: String,
  val icon: String,
  val isUnlocked: Boolean
)
