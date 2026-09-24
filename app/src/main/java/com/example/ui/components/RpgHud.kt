package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.firebase.AuthUserState
import com.example.data.firebase.CloudSyncState
import com.example.data.firebase.SyncStatus
import com.example.data.model.PlayerProfile
import com.example.ui.theme.CrimsonHp
import com.example.ui.theme.EmeraldGood
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.IndigoPrimary

@Composable
fun RpgHud(
  profile: PlayerProfile,
  dailyScore: Double,
  authState: AuthUserState,
  syncState: CloudSyncState,
  isDarkTheme: Boolean,
  onToggleTheme: () -> Unit,
  onOpenSettings: () -> Unit,
  onOpenAccountSync: () -> Unit,
  modifier: Modifier = Modifier
) {
  Surface(
    modifier = modifier.fillMaxWidth(),
    color = MaterialTheme.colorScheme.surface,
    tonalElevation = 2.dp,
    shadowElevation = 4.dp
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
      // Header: App Brand + Title + Cloud Sync status + Settings
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(38.dp)
              .clip(RoundedCornerShape(12.dp))
              .background(
                Brush.linearGradient(
                  colors = listOf(IndigoPrimary, MaterialTheme.colorScheme.secondary)
                )
              ),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.Shield,
              contentDescription = "HabitQuest Logo",
              tint = Color.White,
              modifier = Modifier.size(22.dp)
            )
          }

          Spacer(modifier = Modifier.width(10.dp))

          Column {
            Text(
              text = "HabitQuest",
              style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.2).sp
              ),
              color = MaterialTheme.colorScheme.onSurface
            )
            Text(
              text = "Level ${profile.level} · ${profile.levelTitle()}",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
          // Cloud Sync Chip Button
          Surface(
            modifier = Modifier
              .clip(RoundedCornerShape(12.dp))
              .clickable(onClick = onOpenAccountSync)
              .testTag("cloud_sync_button"),
            color = if (authState.isAuthenticated) {
              MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
            } else {
              MaterialTheme.colorScheme.surfaceVariant
            },
            shape = RoundedCornerShape(12.dp)
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
              val (icon, tint) = when {
                syncState.status == SyncStatus.SYNCING -> Pair(Icons.Default.Sync, IndigoPrimary)
                authState.isAuthenticated -> Pair(Icons.Default.CloudDone, EmeraldGood)
                else -> Pair(Icons.Default.Cloud, MaterialTheme.colorScheme.onSurfaceVariant)
              }
              Icon(
                imageVector = icon,
                contentDescription = "Cloud Sync",
                tint = tint,
                modifier = Modifier.size(16.dp)
              )
              Text(
                text = if (authState.isAuthenticated) "Synced" else "Cloud",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                color = if (authState.isAuthenticated) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }

          Spacer(modifier = Modifier.width(4.dp))

          IconButton(
            onClick = onToggleTheme,
            modifier = Modifier.testTag("theme_toggle_button")
          ) {
            Icon(
              imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
              contentDescription = if (isDarkTheme) "Switch to Light Mode" else "Switch to Dark Mode",
              tint = if (isDarkTheme) GoldAccent else MaterialTheme.colorScheme.onSurfaceVariant
            )
          }

          IconButton(
            onClick = onOpenSettings,
            modifier = Modifier.testTag("settings_button")
          ) {
            Icon(
              imageVector = Icons.Default.Settings,
              contentDescription = "Settings",
              tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // 3 Stat Gauges: HP, XP, Daily Score
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        StatBox(
          label = "HP",
          value = "${profile.hp}/100",
          progress = profile.hp / 100f,
          color = CrimsonHp,
          icon = Icons.Default.Favorite,
          modifier = Modifier.weight(1f)
        )
        StatBox(
          label = "XP",
          value = "${profile.xp}/100",
          progress = profile.xp / 100f,
          color = IndigoPrimary,
          icon = Icons.Default.Bolt,
          modifier = Modifier.weight(1f)
        )
        StatBox(
          label = "Score",
          value = "${Math.round(dailyScore)}%",
          progress = (dailyScore / 100f).toFloat().coerceIn(0f, 1f),
          color = EmeraldGood,
          icon = Icons.Default.GpsFixed,
          modifier = Modifier.weight(1f)
        )
      }
    }
  }
}

@Composable
private fun StatBox(
  label: String,
  value: String,
  progress: Float,
  color: Color,
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  modifier: Modifier = Modifier
) {
  val animatedProgress by animateFloatAsState(
    targetValue = progress.coerceIn(0f, 1f),
    label = "$label Progress"
  )

  Surface(
    modifier = modifier,
    shape = RoundedCornerShape(12.dp),
    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
  ) {
    Column(
      modifier = Modifier.padding(horizontal = 8.dp, vertical = 7.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(13.dp)
          )
          Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
        Text(
          text = value,
          style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
          color = MaterialTheme.colorScheme.onSurface
        )
      }

      Spacer(modifier = Modifier.height(6.dp))

      // Custom progress bar
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(5.dp)
          .clip(CircleShape)
          .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
      ) {
        Box(
          modifier = Modifier
            .fillMaxWidth(animatedProgress)
            .height(5.dp)
            .clip(CircleShape)
            .background(color)
        )
      }
    }
  }
}
