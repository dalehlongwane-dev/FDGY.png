package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.data.model.Habit
import com.example.ui.theme.CrimsonHp
import com.example.ui.theme.EmeraldGood

@Composable
fun AddEditHabitDialog(
  habitToEdit: Habit?,
  onDismiss: () -> Unit,
  onSave: (name: String, type: String, difficulty: Int) -> Unit,
  onArchive: (() -> Unit)? = null
) {
  var name by remember { mutableStateOf(habitToEdit?.name ?: "") }
  var type by remember { mutableStateOf(habitToEdit?.type ?: "good") }
  var difficulty by remember { mutableIntStateOf(habitToEdit?.difficulty ?: 10) }

  Dialog(onDismissRequest = onDismiss) {
    Surface(
      shape = RoundedCornerShape(24.dp),
      color = MaterialTheme.colorScheme.surface,
      tonalElevation = 6.dp,
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(20.dp)
      ) {
        // Header
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = if (habitToEdit == null) "Add Quest Habit" else "Edit Habit",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
          )
          IconButton(onClick = onDismiss) {
            Icon(
              imageVector = Icons.Default.Close,
              contentDescription = "Close",
              tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Habit Name Input
        OutlinedTextField(
          value = name,
          onValueChange = { if (it.length <= 60) name = it },
          label = { Text("Habit name") },
          placeholder = { Text("e.g., Morning Meditation") },
          singleLine = true,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("habit_name_input"),
          shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Type Selector: Good vs Bad
        Text(
          text = "Habit Type",
          style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          FilterChip(
            selected = type == "good",
            onClick = { type = "good" },
            label = { Text("Good Habit") },
            modifier = Modifier.weight(1f),
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = EmeraldGood.copy(alpha = 0.2f),
              selectedLabelColor = EmeraldGood
            )
          )
          FilterChip(
            selected = type == "bad",
            onClick = { type = "bad" },
            label = { Text("Bad Habit") },
            modifier = Modifier.weight(1f),
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = CrimsonHp.copy(alpha = 0.2f),
              selectedLabelColor = CrimsonHp
            )
          )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Difficulty Selector
        Text(
          text = "Difficulty & Rewards",
          style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          listOf(
            Triple(5, "Easy", "5 HP/XP"),
            Triple(10, "Medium", "10 HP/XP"),
            Triple(15, "Hard", "15 HP/XP")
          ).forEach { (diff, label, rewards) ->
            FilterChip(
              selected = difficulty == diff,
              onClick = { difficulty = diff },
              label = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                  Text(label, fontWeight = FontWeight.SemiBold)
                  Text(rewards, style = MaterialTheme.typography.labelSmall)
                }
              },
              modifier = Modifier.weight(1f)
            )
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
          text = "Changes apply to future answers only. Past results, scores, and stats remain preserved.",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(18.dp))

        // Actions: Save / Add button
        Button(
          onClick = {
            if (name.isNotBlank()) {
              onSave(name.trim(), type, difficulty)
              onDismiss()
            }
          },
          enabled = name.isNotBlank(),
          modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .testTag("save_habit_button"),
          shape = RoundedCornerShape(12.dp)
        ) {
          Text(
            text = if (habitToEdit == null) "Add to Quest Log" else "Save Changes",
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
          )
        }

        if (habitToEdit != null && onArchive != null) {
          Spacer(modifier = Modifier.height(10.dp))
          OutlinedButton(
            onClick = {
              onArchive()
              onDismiss()
            },
            modifier = Modifier
              .fillMaxWidth()
              .height(44.dp)
              .testTag("archive_habit_button"),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
          ) {
            Icon(
              imageVector = Icons.Default.Archive,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "Archive Habit",
              style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }
    }
  }
}
