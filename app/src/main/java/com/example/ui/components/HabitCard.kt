package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DayRecord
import com.example.data.model.Habit
import com.example.ui.theme.CrimsonHp
import com.example.ui.theme.EmeraldGood
import com.example.ui.theme.GoldAccent

@Composable
fun HabitCard(
  habit: Habit,
  todayRecord: DayRecord,
  onAction: (answer: String) -> Unit,
  onEdit: () -> Unit,
  modifier: Modifier = Modifier
) {
  val answered = todayRecord.actions[habit.id]
  val isLocked = answered != null
  val meta = todayRecord.meta[habit.id]
  val diffValue = meta?.difficulty ?: habit.difficulty
  val diffName = when (diffValue) {
    15 -> "Hard"
    10 -> "Medium"
    else -> "Easy"
  }

  Surface(
    modifier = modifier.fillMaxWidth(),
    shape = RoundedCornerShape(20.dp),
    color = MaterialTheme.colorScheme.surface,
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
    shadowElevation = 2.dp
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)
    ) {
      // Header: Habit Title & Edit Button
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = habit.name,
            style = MaterialTheme.typography.titleMedium.copy(
              fontWeight = FontWeight.SemiBold,
              fontSize = 16.sp
            ),
            color = MaterialTheme.colorScheme.onSurface
          )

          Spacer(modifier = Modifier.height(6.dp))

          // Metadata Chips Row
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            // Good/Bad Chip
            val isGood = habit.type == "good"
            val badgeBg = if (isGood) EmeraldGood.copy(alpha = 0.15f) else CrimsonHp.copy(alpha = 0.15f)
            val badgeTextColor = if (isGood) EmeraldGood else CrimsonHp

            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(99.dp))
                .background(badgeBg)
                .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
              Text(
                text = if (isGood) "Good" else "Bad",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = badgeTextColor
              )
            }

            // Difficulty Chip
            Text(
              text = "$diffName · $diffValue HP/XP",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Streak Flame
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
              Icon(
                imageVector = Icons.Default.LocalFireDepartment,
                contentDescription = "Streak",
                tint = GoldAccent,
                modifier = Modifier.size(15.dp)
              )
              Text(
                text = "Streak ${habit.streak}",
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
        }

        // Edit button
        OutlinedButton(
          onClick = onEdit,
          modifier = Modifier
            .padding(start = 8.dp)
            .testTag("edit_habit_${habit.id}"),
          shape = RoundedCornerShape(10.dp),
          contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 6.dp),
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
        ) {
          Icon(
            imageVector = Icons.Default.Edit,
            contentDescription = "Edit habit",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(14.dp)
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = "Edit",
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Action Buttons: "I did it" & "I didn't"
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        OutlinedButton(
          onClick = { onAction("did") },
          enabled = !isLocked,
          modifier = Modifier
            .weight(1f)
            .height(44.dp)
            .testTag("action_did_${habit.id}"),
          shape = RoundedCornerShape(12.dp),
          border = BorderStroke(
            1.dp,
            if (isLocked) MaterialTheme.colorScheme.outline.copy(alpha = 0.2f) else EmeraldGood.copy(alpha = 0.6f)
          ),
          colors = ButtonDefaults.outlinedButtonColors(
            containerColor = if (!isLocked) EmeraldGood.copy(alpha = 0.08f) else Color.Transparent,
            contentColor = EmeraldGood,
            disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
          )
        ) {
          Icon(
            imageVector = Icons.Default.Check,
            contentDescription = null,
            modifier = Modifier.size(16.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "I did it",
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
          )
        }

        OutlinedButton(
          onClick = { onAction("missed") },
          enabled = !isLocked,
          modifier = Modifier
            .weight(1f)
            .height(44.dp)
            .testTag("action_missed_${habit.id}"),
          shape = RoundedCornerShape(12.dp),
          border = BorderStroke(
            1.dp,
            if (isLocked) MaterialTheme.colorScheme.outline.copy(alpha = 0.2f) else CrimsonHp.copy(alpha = 0.6f)
          ),
          colors = ButtonDefaults.outlinedButtonColors(
            containerColor = if (!isLocked) CrimsonHp.copy(alpha = 0.08f) else Color.Transparent,
            contentColor = CrimsonHp,
            disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
          )
        ) {
          Icon(
            imageVector = Icons.Default.Close,
            contentDescription = null,
            modifier = Modifier.size(16.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "I didn't",
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
          )
        }
      }

      // If locked today, display informative status banner
      if (isLocked) {
        Spacer(modifier = Modifier.height(10.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          Icon(
            imageVector = Icons.Default.Lock,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(13.dp)
          )
          val actionText = if (answered == "did") "did it" else "didn't do it"
          Text(
            text = "Today: $actionText · $diffValue HP/XP. Locked until tomorrow.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }
    }
  }
}
