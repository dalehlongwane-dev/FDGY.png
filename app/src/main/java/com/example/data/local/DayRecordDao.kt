package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DayRecordDao {
  @Query("SELECT * FROM day_records ORDER BY date ASC")
  fun getAllDayRecords(): Flow<List<DayRecordEntity>>

  @Query("SELECT * FROM day_records WHERE date = :date LIMIT 1")
  suspend fun getDayRecordByDate(date: String): DayRecordEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertDayRecord(record: DayRecordEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertAll(records: List<DayRecordEntity>)

  @Query("DELETE FROM day_records")
  suspend fun deleteAll()
}
