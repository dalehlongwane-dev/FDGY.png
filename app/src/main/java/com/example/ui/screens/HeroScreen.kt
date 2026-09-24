package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CompassCalibration
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.HabitQuestUiState
import com.example.ui.theme.EmeraldGood
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.IndigoPrimary

@Composable
fun HeroScreen(
  uiState: HabitQuestUiState,
  onOpenCloudSync: () -> Unit,
  onOpenSettings: () -> Unit,
  modifier: Modifier = Modifier
) {
  val profile = uiState.playerProfile

  LazyColumn(
    modifier = modifier.fillMaxSize(),
    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    // Hero Banner & Profile Card
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
      ) {
        Column {
          // Banner Image
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .height(130.dp)
          ) {
            Image(
              painter = painterResource(id = R.drawable.hero_quest_banner),
              contentDescription = "Hero Quest Banner",
              contentScale = ContentScale.Crop,
              modifier = Modifier.matchParentSize()
            )
            Box(
              modifier = Modifier
                .matchParentSize()
                .background(
                  Brush.verticalGradient(
                    colors = listOf(Color.Transparent, MaterialTheme.colorScheme.surface.copy(alpha = 0.8f))
                  )
                )
            )
          }

          // Hero Info Row
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            // Avatar
            Box(
              modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(IndigoPrimary.copy(alpha = 0.15f))
                .border(2.dp, IndigoPrimary, CircleShape),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = profile.avatar,
                fontSize = 32.sp
              )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = profile.name,
                style = MaterialTheme.typography.titleLarge.copy(
                  fontWeight = FontWeight.Bold,
                  fontSize = 20.sp
                ),
                color = MaterialTheme.colorScheme.onSurface
              )
              Spacer(modifier = Modifier.height(2.dp))
              Text(
                text = "Level ${profile.level} · ${profile.levelTitle()}",
                style = MaterialTheme.typography.bodyMedium.copy(
                  fontWeight = FontWeight.SemiBold,
                  color = IndigoPrimary
                )
              )
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = "${profile.coins} Gold · ${profile.shields} Shields · ${profile.bestStreak} Best Streak",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
        }
      }
    }

    // Cloud Sync Promo / Status Card
    item {
      Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f))
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(14.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = Icons.Default.Cloud,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Text(
                text = if (uiState.authState.isAuthenticated) "Cloud Sync Active" else "Multi-Device Sync",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
              )
              Text(
                text = if (uiState.authState.isAuthenticated) {
                  uiState.authState.email ?: "Firebase Account"
                } else {
                  "Log in to sync progress across all your devices"
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }

          Button(
            onClick = onOpenCloudSync,
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.testTag("hero_cloud_sync_button")
          ) {
            Text(if (uiState.authState.isAuthenticated) "Sync" else "Log In")
          }
        }
      }
    }

    // Achievements Section
    item {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          imageVector = Icons.Default.EmojiEvents,
          contentDescription = null,
          tint = GoldAccent,
          modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = "Hero Achievements",
          style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
        )
      }
    }

    items(uiState.achievements, key = { it.id }) { ach ->
      Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(
          1.dp,
          if (ach.isUnlocked) GoldAccent.copy(alpha = 0.35f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
        ),
        shadowElevation = if (ach.isUnlocked) 2.dp else 0.dp
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(14.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(
                  if (ach.isUnlocked) GoldAccent.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant
                ),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = if (ach.isUnlocked) Icons.Default.MilitaryTech else Icons.Default.Lock,
                contentDescription = null,
                tint = if (ach.isUnlocked) GoldAccent else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(22.dp)
              )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
              Text(
                text = ach.title,
                style = MaterialTheme.typography.titleSmall.copy(
                  fontWeight = FontWeight.Bold,
                  color = if (ach.isUnlocked) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
              )
              Text(
                text = ach.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }

          if (ach.isUnlocked) {
            Icon(
              imageVector = Icons.Default.Check,
              contentDescription = "Unlocked",
              tint = EmeraldGood,
              modifier = Modifier.size(20.dp)
            )
          } else {
            Icon(
              imageVector = Icons.Default.Lock,
              contentDescription = "Locked",
              tint = MaterialTheme.colorScheme.outline,
              modifier = Modifier.size(18.dp)
            )
          }
        }
      }
    }

    // Epic Missions Section
    item {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          imageVector = Icons.Default.CompassCalibration,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.primary,
          modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = "Missions",
          style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
        )
      }
    }

    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
      ) {
        Column(
          modifier = Modifier.padding(14.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text("30-Day Habit Mission", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
              Text("Build daily consistency for 30 active days", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text("In Progress", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = IndigoPrimary))
          }

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text("Master a Hard Habit", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
              Text("Build a 10-day streak on any Hard (15 HP/XP) habit", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(if (profile.hardModeUnlocked) "Completed" else "In Progress", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = if (profile.hardModeUnlocked) EmeraldGood else GoldAccent))
          }
        }
      }
    }
  }
}
