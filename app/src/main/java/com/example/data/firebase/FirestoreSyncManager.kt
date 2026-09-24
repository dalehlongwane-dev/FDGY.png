package com.example.data.firebase

import com.example.data.model.DayRecord
import com.example.data.model.Habit
import com.example.data.model.HabitMeta
import com.example.data.model.PlayerProfile
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await

enum class SyncStatus {
  IDLE,
  SYNCING,
  SUCCESS,
  ERROR
}

data class CloudSyncState(
  val status: SyncStatus = SyncStatus.IDLE,
  val message: String = "Ready",
  val lastSyncedTime: Long = 0L
)

class FirestoreSyncManager {

  private val firestore: FirebaseFirestore? by lazy {
    try {
      if (FirebaseApp.getApps(FirebaseApp.getInstance().applicationContext).isNotEmpty()) {
        FirebaseFirestore.getInstance()
      } else {
        null
      }
    } catch (_: Exception) {
      null
    }
  }

  private val _syncState = MutableStateFlow(CloudSyncState())
  val syncState: StateFlow<CloudSyncState> = _syncState.asStateFlow()

  suspend fun pushDataToCloud(
    uid: String,
    profile: PlayerProfile,
    habits: List<Habit>,
    dayRecords: List<DayRecord>
  ): Result<Unit> {
    val db = firestore ?: return Result.failure(IllegalStateException("Firestore is not available."))
    _syncState.value = CloudSyncState(status = SyncStatus.SYNCING, message = "Uploading quest data to cloud...")

    return try {
      val userDoc = db.collection("users").document(uid).collection("quest_data").document("state")

      val profileMap = hashMapOf<String, Any>(
        "name" to profile.name,
        "level" to profile.level,
        "hp" to profile.hp,
        "xp" to profile.xp,
        "coins" to profile.coins,
        "streak" to profile.streak,
        "bestStreak" to profile.bestStreak,
        "shields" to profile.shields,
        "avatar" to profile.avatar,
        "hardModeUnlocked" to profile.hardModeUnlocked,
        "bossClaimed" to profile.bossClaimed,
        "weekClaimed" to profile.weekClaimed,
        "lastWeekStart" to profile.lastWeekStart,
        "notificationsEnabled" to profile.notificationsEnabled,
        "themeMode" to profile.themeMode,
        "lastUpdated" to System.currentTimeMillis()
      )

      val habitsList = habits.map { h ->
        hashMapOf<String, Any?>(
          "id" to h.id,
          "name" to h.name,
          "type" to h.type,
          "difficulty" to h.difficulty,
          "streak" to h.streak,
          "archived" to h.archived,
          "archivedAt" to h.archivedAt,
          "createdAt" to h.createdAt
        )
      }

      val daysList = dayRecords.map { d ->
        val actionsMap = HashMap<String, String>(d.actions)
        val metaMap = HashMap<String, Map<String, Any>>()
        d.meta.forEach { (k, v) ->
          metaMap[k] = mapOf("t" to v.type, "d" to v.difficulty)
        }

        hashMapOf<String, Any?>(
          "date" to d.date,
          "actions" to actionsMap,
          "meta" to metaMap,
          "score" to (d.score ?: -1.0),
          "completedGood" to d.completedGood,
          "completedBad" to d.completedBad
        )
      }

      val payload = hashMapOf<String, Any>(
        "profile" to profileMap,
        "habits" to habitsList,
        "days" to daysList,
        "syncedAt" to System.currentTimeMillis()
      )

      userDoc.set(payload, SetOptions.merge()).await()
      val now = System.currentTimeMillis()
      _syncState.value = CloudSyncState(
        status = SyncStatus.SUCCESS,
        message = "Successfully synced with cloud",
        lastSyncedTime = now
      )
      Result.success(Unit)
    } catch (e: Exception) {
      _syncState.value = CloudSyncState(
        status = SyncStatus.ERROR,
        message = e.localizedMessage ?: "Sync error",
        lastSyncedTime = System.currentTimeMillis()
      )
      Result.failure(e)
    }
  }

  suspend fun pullDataFromCloud(uid: String): Result<Triple<PlayerProfile, List<Habit>, List<DayRecord>>?> {
    val db = firestore ?: return Result.failure(IllegalStateException("Firestore is not available."))
    _syncState.value = CloudSyncState(status = SyncStatus.SYNCING, message = "Downloading quest data...")

    return try {
      val doc = db.collection("users").document(uid).collection("quest_data").document("state").get().await()
      if (!doc.exists()) {
        _syncState.value = CloudSyncState(status = SyncStatus.IDLE, message = "No remote cloud data found yet.")
        return Result.success(null)
      }

      val profileRaw = doc.get("profile") as? Map<*, *>
      val habitsRaw = doc.get("habits") as? List<*>
      val daysRaw = doc.get("days") as? List<*>

      val profile = if (profileRaw != null) {
        PlayerProfile(
          name = profileRaw["name"] as? String ?: "Habit Adventurer",
          level = (profileRaw["level"] as? Number)?.toInt() ?: 1,
          hp = (profileRaw["hp"] as? Number)?.toInt() ?: 100,
          xp = (profileRaw["xp"] as? Number)?.toInt() ?: 0,
          coins = (profileRaw["coins"] as? Number)?.toInt() ?: 100,
          streak = (profileRaw["streak"] as? Number)?.toInt() ?: 0,
          bestStreak = (profileRaw["bestStreak"] as? Number)?.toInt() ?: 0,
          shields = (profileRaw["shields"] as? Number)?.toInt() ?: 0,
          avatar = profileRaw["avatar"] as? String ?: "🧙",
          hardModeUnlocked = profileRaw["hardModeUnlocked"] as? Boolean ?: false,
          bossClaimed = profileRaw["bossClaimed"] as? Boolean ?: false,
          weekClaimed = profileRaw["weekClaimed"] as? Boolean ?: false,
          lastWeekStart = profileRaw["lastWeekStart"] as? String ?: "",
          notificationsEnabled = profileRaw["notificationsEnabled"] as? Boolean ?: false,
          themeMode = profileRaw["themeMode"] as? String ?: "dark",
          lastSyncedAt = System.currentTimeMillis()
        )
      } else {
        PlayerProfile()
      }

      val habits = mutableListOf<Habit>()
      habitsRaw?.forEach { item ->
        if (item is Map<*, *>) {
          habits.add(
            Habit(
              id = item["id"] as? String ?: java.util.UUID.randomUUID().toString(),
              name = item["name"] as? String ?: "",
              type = item["type"] as? String ?: "good",
              difficulty = (item["difficulty"] as? Number)?.toInt() ?: 10,
              streak = (item["streak"] as? Number)?.toInt() ?: 0,
              archived = item["archived"] as? Boolean ?: false,
              archivedAt = item["archivedAt"] as? String,
              createdAt = (item["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis()
            )
          )
        }
      }

      val dayRecords = mutableListOf<DayRecord>()
      daysRaw?.forEach { item ->
        if (item is Map<*, *>) {
          val actionsRaw = item["actions"] as? Map<*, *>
          val actions = mutableMapOf<String, String>()
          actionsRaw?.forEach { (k, v) ->
            if (k != null && v != null) actions[k.toString()] = v.toString()
          }

          val metaRaw = item["meta"] as? Map<*, *>
          val meta = mutableMapOf<String, HabitMeta>()
          metaRaw?.forEach { (k, v) ->
            if (k != null && v is Map<*, *>) {
              val t = v["t"] as? String ?: "good"
              val d = (v["d"] as? Number)?.toInt() ?: 10
              meta[k.toString()] = HabitMeta(t, d)
            }
          }

          val rawScore = (item["score"] as? Number)?.toDouble()
          val score = if (rawScore != null && rawScore >= 0.0) rawScore else null

          dayRecords.add(
            DayRecord(
              date = item["date"] as? String ?: "",
              actions = actions,
              meta = meta,
              score = score,
              completedGood = (item["completedGood"] as? Number)?.toInt() ?: 0,
              completedBad = (item["completedBad"] as? Number)?.toInt() ?: 0
            )
          )
        }
      }

      val now = System.currentTimeMillis()
      _syncState.value = CloudSyncState(
        status = SyncStatus.SUCCESS,
        message = "Loaded latest progress from cloud",
        lastSyncedTime = now
      )
      Result.success(Triple(profile, habits, dayRecords))
    } catch (e: Exception) {
      _syncState.value = CloudSyncState(
        status = SyncStatus.ERROR,
        message = e.localizedMessage ?: "Failed to pull cloud data",
        lastSyncedTime = System.currentTimeMillis()
      )
      Result.failure(e)
    }
  }
}
