package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
  entities = [HabitEntity::class, DayRecordEntity::class, PlayerProfileEntity::class],
  version = 2,
  exportSchema = false
)
abstract class HabitQuestDatabase : RoomDatabase() {
  abstract fun habitDao(): HabitDao
  abstract fun dayRecordDao(): DayRecordDao
  abstract fun playerDao(): PlayerDao

  companion object {
    @Volatile
    private var INSTANCE: HabitQuestDatabase? = null

    fun getInstance(context: Context): HabitQuestDatabase {
      return INSTANCE ?: synchronized(this) {
        val instance = Room.databaseBuilder(
          context.applicationContext,
          HabitQuestDatabase::class.java,
          "habitquest_database"
        ).fallbackToDestructiveMigration().build()
        INSTANCE = instance
        instance
      }
    }
  }
}
