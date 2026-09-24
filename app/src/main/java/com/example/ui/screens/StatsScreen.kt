package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DayRecord
import com.example.ui.HabitQuestUiState
import com.example.ui.theme.CrimsonHp
import com.example.ui.theme.EmeraldGood
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.IndigoPrimary
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun StatsScreen(
  uiState: HabitQuestUiState,
  onSelectDate: (String) -> Unit,
  modifier: Modifier = Modifier
) {
  var chartMode by remember { mutableStateOf("daily") } // "daily", "weekly", "monthly"

  val recordsWithActions = uiState.dayRecords.filter { it.actions.isNotEmpty() }
  val scores = recordsWithActions.mapNotNull { it.score }
  val avgScore = if (scores.isNotEmpty()) Math.round(scores.average()) else 0L
  val bestScore = if (scores.isNotEmpty()) Math.round(scores.maxOrNull() ?: 0.0) else 0L
  val totalActions = uiState.dayRecords.sumOf { it.actions.size }
  val daysTracked = recordsWithActions.size

  LazyColumn(
    modifier = modifier.fillMaxSize(),
    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    // Calendar Card
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.CalendarMonth,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "Quest Calendar",
              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
              color = MaterialTheme.colorScheme.onSurface
            )
          }

          Spacer(modifier = Modifier.height(12.dp))

          CalendarGrid(
            dayRecords = uiState.dayRecords,
            todayDate = uiState.todayRecord.date,
            selectedDate = uiState.selectedCalendarDate,
            onSelectDate = onSelectDate
          )
        }
      }
    }

    // Selected Day Summary
    val selectedRec = uiState.dayRecords.find { it.date == uiState.selectedCalendarDate }
      ?: if (uiState.selectedCalendarDate == uiState.todayRecord.date) uiState.todayRecord else null

    if (selectedRec != null && selectedRec.actions.isNotEmpty()) {
      item {
        Surface(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(16.dp),
          color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
        ) {
          Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(
                text = "Day: ${selectedRec.date}",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
              )
              Text(
                text = "${selectedRec.completedGood} good habits done · ${selectedRec.completedBad} bad habits",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }

            Surface(
              shape = RoundedCornerShape(12.dp),
              color = EmeraldGood.copy(alpha = 0.15f)
            ) {
              Text(
                text = "${Math.round(selectedRec.score ?: 0.0)}%",
                style = MaterialTheme.typography.titleMedium.copy(
                  fontWeight = FontWeight.Bold,
                  color = EmeraldGood
                ),
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
              )
            }
          }
        }
      }
    }

    // Analytics Chart Card
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.TrendingUp,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = "Score Analytics",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
              )
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          // Segmented chart mode buttons
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            listOf(
              "daily" to "Daily",
              "weekly" to "Weekly",
              "monthly" to "Monthly"
            ).forEach { (mode, label) ->
              FilterChip(
                selected = chartMode == mode,
                onClick = { chartMode = mode },
                label = { Text(label) },
                modifier = Modifier.weight(1f)
              )
            }
          }

          Spacer(modifier = Modifier.height(16.dp))

          // Chart Canvas
          val chartData = remember(chartMode, uiState.dayRecords) {
            computeChartData(chartMode, uiState.dayRecords)
          }

          AnalyticsChartCanvas(
            data = chartData,
            modifier = Modifier
              .fillMaxWidth()
              .height(200.dp)
          )

          Spacer(modifier = Modifier.height(16.dp))

          // Average & Best Row
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
          ) {
            MetricBox(
              title = "Average Score",
              value = "$avgScore%",
              color = IndigoPrimary,
              modifier = Modifier.weight(1f)
            )
            MetricBox(
              title = "Best Score",
              value = "$bestScore%",
              color = EmeraldGood,
              modifier = Modifier.weight(1f)
            )
          }
        }
      }
    }

    // Days Tracked & Habit Actions Row
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        MetricBox(
          title = "Days Tracked",
          value = "$daysTracked",
          color = MaterialTheme.colorScheme.onSurface,
          modifier = Modifier.weight(1f)
        )
        MetricBox(
          title = "Habit Actions",
          value = "$totalActions",
          color = GoldAccent,
          modifier = Modifier.weight(1f)
        )
      }
    }
  }
}

@Composable
private fun MetricBox(
  title: String,
  value: String,
  color: Color,
  modifier: Modifier = Modifier
) {
  Surface(
    modifier = modifier,
    shape = RoundedCornerShape(16.dp),
    color = MaterialTheme.colorScheme.surface,
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
    shadowElevation = 1.dp
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
      Text(
        text = value,
        style = MaterialTheme.typography.headlineMedium.copy(
          fontWeight = FontWeight.Bold,
          color = color
        )
      )
      Spacer(modifier = Modifier.height(2.dp))
      Text(
        text = title,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
    }
  }
}

@Composable
private fun CalendarGrid(
  dayRecords: List<DayRecord>,
  todayDate: String,
  selectedDate: String,
  onSelectDate: (String) -> Unit
) {
  val cal = Calendar.getInstance()
  val year = cal.get(Calendar.YEAR)
  val month = cal.get(Calendar.MONTH)

  val monthName = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(cal.time)

  cal.set(Calendar.DAY_OF_MONTH, 1)
  val firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK) - 1 // 0-based Sunday=0
  val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)

  val recordMap = remember(dayRecords) {
    dayRecords.associateBy { it.date }
  }

  Column {
    Text(
      text = monthName,
      style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
      color = MaterialTheme.colorScheme.primary,
      modifier = Modifier.padding(bottom = 8.dp)
    )

    // Day of week headers
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      listOf("S", "M", "T", "W", "T", "F", "S").forEach { dow ->
        Box(
          modifier = Modifier
            .weight(1f)
            .padding(vertical = 4.dp),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = dow,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(4.dp))

    // Grid of days
    val totalSlots = firstDayOfWeek + daysInMonth
    val rows = (totalSlots + 6) / 7

    for (row in 0 until rows) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
      ) {
        for (col in 0 until 7) {
          val slot = row * 7 + col
          val dayNum = slot - firstDayOfWeek + 1

          if (slot in firstDayOfWeek until totalSlots) {
            val dateStr = String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, dayNum)
            val rec = recordMap[dateStr]
            val score = rec?.score

            val isToday = dateStr == todayDate
            val isSelected = dateStr == selectedDate

            val cellBg = when {
              score != null && score >= 80.0 -> EmeraldGood.copy(alpha = 0.25f)
              score != null && score >= 40.0 -> GoldAccent.copy(alpha = 0.25f)
              score != null && score > 0.0 -> CrimsonHp.copy(alpha = 0.2f)
              else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            }

            val textColor = when {
              score != null && score >= 80.0 -> EmeraldGood
              score != null && score >= 40.0 -> GoldAccent
              score != null && score > 0.0 -> CrimsonHp
              else -> MaterialTheme.colorScheme.onSurfaceVariant
            }

            Box(
              modifier = Modifier
                .weight(1f)
                .aspectRatio(1f)
                .clip(RoundedCornerShape(8.dp))
                .background(cellBg)
                .clickable { onSelectDate(dateStr) }
                .then(
                  if (isSelected || isToday) {
                    Modifier.background(
                      color = Color.Transparent,
                      shape = RoundedCornerShape(8.dp)
                    )
                  } else Modifier
                ),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = "$dayNum",
                style = MaterialTheme.typography.labelSmall.copy(
                  fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.Medium
                ),
                color = textColor
              )
            }
          } else {
            Spacer(modifier = Modifier.weight(1f))
          }
        }
      }
      Spacer(modifier = Modifier.height(4.dp))
    }
  }
}

data class ChartPoint(val label: String, val value: Float)

private fun computeChartData(mode: String, dayRecords: List<DayRecord>): List<ChartPoint> {
  val recordMap = dayRecords.associateBy { it.date }
  val cal = Calendar.getInstance()
  val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
  val list = mutableListOf<ChartPoint>()

  when (mode) {
    "daily" -> {
      // Past 14 days
      for (i in 13 downTo 0) {
        val c = Calendar.getInstance()
        c.add(Calendar.DAY_OF_YEAR, -i)
        val k = dateFormat.format(c.time)
        val label = SimpleDateFormat("dd", Locale.US).format(c.time)
        val score = recordMap[k]?.score?.toFloat() ?: 0f
        list.add(ChartPoint(label, score))
      }
    }
    "weekly" -> {
      // Past 8 weeks
      for (i in 7 downTo 0) {
        val c = Calendar.getInstance()
        c.add(Calendar.WEEK_OF_YEAR, -i)
        var sum = 0f
        var count = 0
        for (d in 0..6) {
          val dayCal = Calendar.getInstance().apply {
            time = c.time
            add(Calendar.DAY_OF_YEAR, -d)
          }
          val k = dateFormat.format(dayCal.time)
          recordMap[k]?.score?.let {
            sum += it.toFloat()
            count++
          }
        }
        val avg = if (count > 0) sum / count else 0f
        list.add(ChartPoint("W${8 - i}", avg))
      }
    }
    "monthly" -> {
      // Past 6 months
      for (i in 5 downTo 0) {
        val c = Calendar.getInstance()
        c.add(Calendar.MONTH, -i)
        val monthLabel = SimpleDateFormat("MMM", Locale.US).format(c.time)
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)

        var sum = 0f
        var count = 0
        dayRecords.forEach { rec ->
          try {
            val d = dateFormat.parse(rec.date)
            if (d != null) {
              val itemCal = Calendar.getInstance().apply { time = d }
              if (itemCal.get(Calendar.YEAR) == year && itemCal.get(Calendar.MONTH) == month) {
                rec.score?.let {
                  sum += it.toFloat()
                  count++
                }
              }
            }
          } catch (_: Exception) {}
        }
        val avg = if (count > 0) sum / count else 0f
        list.add(ChartPoint(monthLabel, avg))
      }
    }
  }
  return list
}

@Composable
private fun AnalyticsChartCanvas(
  data: List<ChartPoint>,
  modifier: Modifier = Modifier
) {
  val outlineColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
  val textColor = MaterialTheme.colorScheme.onSurfaceVariant

  Canvas(modifier = modifier) {
    if (data.isEmpty()) return@Canvas

    val paddingLeft = 32.dp.toPx()
    val paddingRight = 16.dp.toPx()
    val paddingTop = 16.dp.toPx()
    val paddingBottom = 28.dp.toPx()

    val chartWidth = size.width - paddingLeft - paddingRight
    val chartHeight = size.height - paddingTop - paddingBottom

    // Draw horizontal grid lines at 0, 50, 100
    listOf(0f, 50f, 100f).forEach { value ->
      val y = paddingTop + chartHeight - (value / 100f) * chartHeight
      drawLine(
        color = outlineColor,
        start = Offset(paddingLeft, y),
        end = Offset(size.width - paddingRight, y),
        strokeWidth = 1.dp.toPx()
      )

      drawContext.canvas.nativeCanvas.drawText(
        "${value.toInt()}%",
        paddingLeft - 8.dp.toPx(),
        y + 4.dp.toPx(),
        android.graphics.Paint().apply {
          this.color = android.graphics.Color.GRAY
          textSize = 10.sp.toPx()
          textAlign = android.graphics.Paint.Align.RIGHT
        }
      )
    }

    val stepX = if (data.size > 1) chartWidth / (data.size - 1) else chartWidth

    val path = Path()
    val fillPath = Path()

    data.forEachIndexed { i, p ->
      val x = paddingLeft + i * stepX
      val y = paddingTop + chartHeight - (p.value.coerceIn(0f, 100f) / 100f) * chartHeight

      if (i == 0) {
        path.moveTo(x, y)
        fillPath.moveTo(x, paddingTop + chartHeight)
        fillPath.lineTo(x, y)
      } else {
        path.lineTo(x, y)
        fillPath.lineTo(x, y)
      }

      if (i == data.size - 1) {
        fillPath.lineTo(x, paddingTop + chartHeight)
        fillPath.close()
      }
    }

    // Draw area fill
    drawPath(
      path = fillPath,
      brush = Brush.verticalGradient(
        colors = listOf(IndigoPrimary.copy(alpha = 0.25f), Color.Transparent),
        startY = paddingTop,
        endY = paddingTop + chartHeight
      )
    )

    // Draw line
    drawPath(
      path = path,
      color = IndigoPrimary,
      style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
    )

    // Draw points and labels
    val labelStep = if (data.size > 8) 2 else 1
    data.forEachIndexed { i, p ->
      val x = paddingLeft + i * stepX
      val y = paddingTop + chartHeight - (p.value.coerceIn(0f, 100f) / 100f) * chartHeight

      drawCircle(
        color = Color.White,
        radius = 4.dp.toPx(),
        center = Offset(x, y)
      )
      drawCircle(
        color = IndigoPrimary,
        radius = 2.5.dp.toPx(),
        center = Offset(x, y)
      )

      if (i % labelStep == 0) {
        drawContext.canvas.nativeCanvas.drawText(
          p.label,
          x,
          size.height - 6.dp.toPx(),
          android.graphics.Paint().apply {
            this.color = android.graphics.Color.GRAY
            textSize = 10.sp.toPx()
            textAlign = android.graphics.Paint.Align.CENTER
          }
        )
      }
    }
  }
}
