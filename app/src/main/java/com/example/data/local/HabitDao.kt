package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitDao {
  @Query("SELECT * FROM habits ORDER BY createdAt ASC")
  fun getAllHabits(): Flow<List<HabitEntity>>

  @Query("SELECT * FROM habits WHERE id = :id LIMIT 1")
  suspend fun getHabitById(id: String): HabitEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertHabit(habit: HabitEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertAll(habits: List<HabitEntity>)

  @Update
  suspend fun updateHabit(habit: HabitEntity)

  @Query("DELETE FROM habits WHERE id = :id")
  suspend fun deleteHabit(id: String)

  @Query("DELETE FROM habits")
  suspend fun deleteAll()
}
