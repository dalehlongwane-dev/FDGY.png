package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.DayRecord
import com.example.data.model.HabitMeta
import org.json.JSONObject

@Entity(tableName = "day_records")
data class DayRecordEntity(
  @PrimaryKey val date: String,
  val actionsJson: String,
  val metaJson: String,
  val score: Double?,
  val completedGood: Int,
  val completedBad: Int
) {
  fun toDomain(): DayRecord {
    val actionsMap = mutableMapOf<String, String>()
    if (actionsJson.isNotEmpty()) {
      try {
        val obj = JSONObject(actionsJson)
        val keys = obj.keys()
        while (keys.hasNext()) {
          val k = keys.next()
          actionsMap[k] = obj.optString(k, "")
        }
      } catch (_: Exception) {}
    }

    val metaMap = mutableMapOf<String, HabitMeta>()
    if (metaJson.isNotEmpty()) {
      try {
        val obj = JSONObject(metaJson)
        val keys = obj.keys()
        while (keys.hasNext()) {
          val k = keys.next()
          val sub = obj.optJSONObject(k)
          if (sub != null) {
            metaMap[k] = HabitMeta(
              type = sub.optString("t", "good"),
              difficulty = sub.optInt("d", 10)
            )
          }
        }
      } catch (_: Exception) {}
    }

    return DayRecord(
      date = date,
      actions = actionsMap,
      meta = metaMap,
      score = score,
      completedGood = completedGood,
      completedBad = completedBad
    )
  }

  companion object {
    fun fromDomain(d: DayRecord): DayRecordEntity {
      val actionsObj = JSONObject()
      d.actions.forEach { (k, v) -> actionsObj.put(k, v) }

      val metaObj = JSONObject()
      d.meta.forEach { (k, v) ->
        val sub = JSONObject()
        sub.put("t", v.type)
        sub.put("d", v.difficulty)
        metaObj.put(k, sub)
      }

      return DayRecordEntity(
        date = d.date,
        actionsJson = actionsObj.toString(),
        metaJson = metaObj.toString(),
        score = d.score,
        completedGood = d.completedGood,
        completedBad = d.completedBad
      )
    }
  }
}
