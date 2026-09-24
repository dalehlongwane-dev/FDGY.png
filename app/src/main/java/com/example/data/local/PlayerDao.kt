package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PlayerDao {
  @Query("SELECT * FROM player_profile WHERE id = 'current_player' LIMIT 1")
  fun getPlayerProfile(): Flow<PlayerProfileEntity?>

  @Query("SELECT * FROM player_profile WHERE id = 'current_player' LIMIT 1")
  suspend fun getPlayerProfileSync(): PlayerProfileEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertOrUpdate(profile: PlayerProfileEntity)

  @Query("DELETE FROM player_profile")
  suspend fun deleteAll()
}
