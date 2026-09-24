package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.Habit

@Entity(tableName = "habits")
data class HabitEntity(
  @PrimaryKey val id: String,
  val name: String,
  val type: String,
  val difficulty: Int,
  val streak: Int,
  val archived: Boolean,
  val archivedAt: String?,
  val createdAt: Long
) {
  fun toDomain(): Habit = Habit(
    id = id,
    name = name,
    type = type,
    difficulty = difficulty,
    streak = streak,
    archived = archived,
    archivedAt = archivedAt,
    createdAt = createdAt
  )

  companion object {
    fun fromDomain(h: Habit): HabitEntity = HabitEntity(
      id = h.id,
      name = h.name,
      type = h.type,
      difficulty = h.difficulty,
      streak = h.streak,
      archived = h.archived,
      archivedAt = h.archivedAt,
      createdAt = h.createdAt
    )
  }
}
