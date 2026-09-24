package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Habit
import com.example.ui.HabitQuestUiState
import com.example.ui.components.BossCard
import com.example.ui.components.HabitCard
import com.example.ui.theme.CrimsonHp
import com.example.ui.theme.EmeraldGood

@Composable
fun QuestScreen(
  uiState: HabitQuestUiState,
  onSwitchTab: (String) -> Unit,
  onHabitAction: (habitId: String, answer: String) -> Unit,
  onEditHabit: (Habit) -> Unit,
  onOpenAddHabit: () -> Unit,
  onRestoreHabit: (String) -> Unit,
  onClaimBossReward: () -> Unit,
  modifier: Modifier = Modifier
) {
  var isArchivedExpanded by remember { mutableStateOf(false) }

  val activeHabits = uiState.habits.filter { !it.archived && it.type == uiState.activeTab }
  val archivedHabits = uiState.habits.filter { it.archived }

  // Daily quest progress computation
  val totalActiveGood = uiState.habits.count { !it.archived && it.type == "good" }
  val totalActiveBad = uiState.habits.count { !it.archived && it.type == "bad" }
  val answeredTodayCount = uiState.todayRecord.actions.size
  val totalHabitsCount = totalActiveGood + totalActiveBad

  LazyColumn(
    modifier = modifier.fillMaxSize(),
    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    // Segmented Tabs: Good Habits vs Bad Habits
    item {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(14.dp))
          .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        val isGoodTab = uiState.activeTab == "good"
        FilterChip(
          selected = isGoodTab,
          onClick = { onSwitchTab("good") },
          label = {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text("Good Habits (${uiState.habits.count { !it.archived && it.type == "good" }})")
            }
          },
          modifier = Modifier
            .weight(1f)
            .testTag("tab_good_habits"),
          colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = EmeraldGood.copy(alpha = 0.2f),
            selectedLabelColor = EmeraldGood
          )
        )

        FilterChip(
          selected = !isGoodTab,
          onClick = { onSwitchTab("bad") },
          label = {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.Close,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text("Bad Habits (${uiState.habits.count { !it.archived && it.type == "bad" }})")
            }
          },
          modifier = Modifier
            .weight(1f)
            .testTag("tab_bad_habits"),
          colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = CrimsonHp.copy(alpha = 0.2f),
            selectedLabelColor = CrimsonHp
          )
        )
      }
    }

    // Active Habits List
    if (activeHabits.isEmpty()) {
      item {
        Surface(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(16.dp),
          color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Text(
              text = "No ${uiState.activeTab} habits yet.",
              style = MaterialTheme.typography.bodyLarge,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(
              onClick = onOpenAddHabit,
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier.testTag("empty_add_habit_button")
            ) {
              Icon(imageVector = Icons.Default.Add, contentDescription = null)
              Spacer(modifier = Modifier.width(6.dp))
              Text("Add your first ${uiState.activeTab} habit")
            }
          }
        }
      }
    } else {
      items(activeHabits, key = { it.id }) { habit ->
        HabitCard(
          habit = habit,
          todayRecord = uiState.todayRecord,
          onAction = { answer -> onHabitAction(habit.id, answer) },
          onEdit = { onEditHabit(habit) }
        )
      }
    }

    // "Add Habit" quick card
    item {
      ElevatedCard(
        onClick = onOpenAddHabit,
        modifier = Modifier
          .fillMaxWidth()
          .testTag("add_habit_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.Center
        ) {
          Icon(
            imageVector = Icons.Default.Add,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "Add Habit to Quest Log",
            style = MaterialTheme.typography.labelLarge.copy(
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.primary
            )
          )
        }
      }
    }

    // Daily Quest Card
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
          containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.TrackChanges,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "Daily Quest",
              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
              color = MaterialTheme.colorScheme.onSurface
            )
          }

          Spacer(modifier = Modifier.height(6.dp))

          Text(
            text = "$answeredTodayCount / $totalHabitsCount habits answered today",
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurface
          )

          Spacer(modifier = Modifier.height(4.dp))

          val badPenalty = if (totalActiveBad > 0) Math.round(100f / totalActiveBad) else 0
          Text(
            text = "Complete all good habits for 100%. Each bad habit done removes $badPenalty points.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }
    }

    // Boss Battle Card
    item {
      BossCard(
        bossHp = uiState.bossHp,
        bossClaimed = uiState.bossClaimed,
        onClaimReward = onClaimBossReward
      )
    }

    // Archived Habits Collapsible
    if (archivedHabits.isNotEmpty()) {
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(18.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .clickable { isArchivedExpanded = !isArchivedExpanded },
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  imageVector = Icons.Default.Archive,
                  contentDescription = null,
                  tint = MaterialTheme.colorScheme.onSurfaceVariant,
                  modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = "Archived Habits",
                  style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Surface(
                  shape = RoundedCornerShape(99.dp),
                  color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                  Text(
                    text = "${archivedHabits.size}",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                  )
                }
              }

              Icon(
                imageVector = if (isArchivedExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }

            AnimatedVisibility(visible = isArchivedExpanded) {
              Column(
                modifier = Modifier.padding(top = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                archivedHabits.forEach { habit ->
                  Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                  ) {
                    Row(
                      modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                      horizontalArrangement = Arrangement.SpaceBetween,
                      verticalAlignment = Alignment.CenterVertically
                    ) {
                      Column(modifier = Modifier.weight(1f)) {
                        Text(
                          text = habit.name,
                          style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                        )
                        Text(
                          text = "${if (habit.type == "good") "Good" else "Bad"} · ${habit.difficulty} HP/XP",
                          style = MaterialTheme.typography.bodySmall,
                          color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                      }

                      OutlinedButton(
                        onClick = { onRestoreHabit(habit.id) },
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                      ) {
                        Icon(
                          imageVector = Icons.Default.Undo,
                          contentDescription = "Restore",
                          modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Restore", fontSize = 12.sp)
                      }
                    }
                  }
                }

                Text(
                  text = "Archived habits preserve past calendar days and analytics history.",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                  modifier = Modifier.padding(top = 4.dp)
                )
              }
            }
          }
        }
      }
    }
  }
}
