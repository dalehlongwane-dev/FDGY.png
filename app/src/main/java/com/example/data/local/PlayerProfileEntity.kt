package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.PlayerProfile

@Entity(tableName = "player_profile")
data class PlayerProfileEntity(
  @PrimaryKey val id: String = "current_player",
  val name: String,
  val level: Int,
  val hp: Int,
  val xp: Int,
  val coins: Int,
  val streak: Int,
  val bestStreak: Int,
  val shields: Int,
  val avatar: String,
  val hardModeUnlocked: Boolean,
  val bossClaimed: Boolean,
  val weekClaimed: Boolean,
  val lastWeekStart: String,
  val notificationsEnabled: Boolean,
  val themeMode: String = "dark",
  val lastSyncedAt: Long
) {
  fun toDomain(): PlayerProfile = PlayerProfile(
    name = name,
    level = level,
    hp = hp,
    xp = xp,
    coins = coins,
    streak = streak,
    bestStreak = bestStreak,
    shields = shields,
    avatar = avatar,
    hardModeUnlocked = hardModeUnlocked,
    bossClaimed = bossClaimed,
    weekClaimed = weekClaimed,
    lastWeekStart = lastWeekStart,
    notificationsEnabled = notificationsEnabled,
    themeMode = themeMode,
    lastSyncedAt = lastSyncedAt
  )

  companion object {
    fun fromDomain(p: PlayerProfile): PlayerProfileEntity = PlayerProfileEntity(
      id = "current_player",
      name = p.name,
      level = p.level,
      hp = p.hp,
      xp = p.xp,
      coins = p.coins,
      streak = p.streak,
      bestStreak = p.bestStreak,
      shields = p.shields,
      avatar = p.avatar,
      hardModeUnlocked = p.hardModeUnlocked,
      bossClaimed = p.bossClaimed,
      weekClaimed = p.weekClaimed,
      lastWeekStart = p.lastWeekStart,
      notificationsEnabled = p.notificationsEnabled,
      themeMode = p.themeMode,
      lastSyncedAt = p.lastSyncedAt
    )
  }
}
