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
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.firebase.AuthUserState
import com.example.data.model.PlayerProfile

@Composable
fun SettingsDialog(
  profile: PlayerProfile,
  authState: AuthUserState,
  themeMode: String,
  onDismiss: () -> Unit,
  onUpdateHeroName: (String) -> Unit,
  onSelectThemeMode: (String) -> Unit,
  onOpenCloudSync: () -> Unit,
  onExportData: () -> Unit,
  onResetData: () -> Unit
) {
  var isRenaming by remember { mutableStateOf(false) }
  var heroName by remember { mutableStateOf(profile.name) }
  var showResetConfirm by remember { mutableStateOf(false) }

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
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Settings",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
          )
          IconButton(onClick = onDismiss) {
            Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Hero Name Row
        Surface(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(14.dp),
          color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
        ) {
          Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
              )
              Spacer(modifier = Modifier.width(10.dp))
              Column {
                Text(
                  text = "Hero Name",
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                  text = profile.name,
                  style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                )
              }
            }

            OutlinedButton(
              onClick = { isRenaming = true },
              shape = RoundedCornerShape(10.dp)
            ) {
              Text("Edit")
            }
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Theme Mode Row (Dark / Light / System)
        Surface(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(14.dp),
          color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
        ) {
          Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.Palette,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
              )
              Spacer(modifier = Modifier.width(10.dp))
              Column {
                Text(
                  text = "Appearance Theme",
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                  text = when (themeMode) {
                    "light" -> "Light Mode"
                    "dark" -> "Dark Mode"
                    else -> "System Default"
                  },
                  style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                )
              }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              FilterChip(
                selected = themeMode == "dark",
                onClick = { onSelectThemeMode("dark") },
                label = { Text("Dark", fontSize = 12.sp) },
                leadingIcon = {
                  Icon(
                    imageVector = Icons.Default.DarkMode,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp)
                  )
                },
                modifier = Modifier
                  .weight(1f)
                  .testTag("theme_chip_dark")
              )

              FilterChip(
                selected = themeMode == "light",
                onClick = { onSelectThemeMode("light") },
                label = { Text("Light", fontSize = 12.sp) },
                leadingIcon = {
                  Icon(
                    imageVector = Icons.Default.LightMode,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp)
                  )
                },
                modifier = Modifier
                  .weight(1f)
                  .testTag("theme_chip_light")
              )

              FilterChip(
                selected = themeMode == "system",
                onClick = { onSelectThemeMode("system") },
                label = { Text("System", fontSize = 12.sp) },
                leadingIcon = {
                  Icon(
                    imageVector = Icons.Default.BrightnessAuto,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp)
                  )
                },
                modifier = Modifier
                  .weight(1f)
                  .testTag("theme_chip_system")
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Cloud Account & Sync Row
        Surface(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(14.dp),
          color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
        ) {
          Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.Cloud,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
              )
              Spacer(modifier = Modifier.width(10.dp))
              Column {
                Text(
                  text = "Cloud Sync",
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                  text = if (authState.isAuthenticated) authState.email ?: "Logged in" else "Offline / Not logged in",
                  style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                )
              }
            }

            OutlinedButton(
              onClick = {
                onDismiss()
                onOpenCloudSync()
              },
              shape = RoundedCornerShape(10.dp)
            ) {
              Text(if (authState.isAuthenticated) "Manage" else "Log In")
            }
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Export Data Row
        Surface(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(14.dp),
          color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
        ) {
          Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.Share,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
              )
              Spacer(modifier = Modifier.width(10.dp))
              Column {
                Text(
                  text = "Backup & Export",
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                  text = "Save JSON backup",
                  style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                )
              }
            }

            OutlinedButton(
              onClick = onExportData,
              shape = RoundedCornerShape(10.dp)
            ) {
              Text("Export")
            }
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Reset Data Row
        Surface(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(14.dp),
          color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.15f),
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.3f))
        ) {
          Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.DeleteForever,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(20.dp)
              )
              Spacer(modifier = Modifier.width(10.dp))
              Column {
                Text(
                  text = "Reset Progress",
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.error
                )
                Text(
                  text = "Reset all stats & habits",
                  style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                  color = MaterialTheme.colorScheme.error
                )
              }
            }

            Button(
              onClick = { showResetConfirm = true },
              shape = RoundedCornerShape(10.dp),
              colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
              Text("Reset")
            }
          }
        }
      }
    }
  }

  // Rename Dialog
  if (isRenaming) {
    AlertDialog(
      onDismissRequest = { isRenaming = false },
      title = { Text("Rename Hero") },
      text = {
        OutlinedTextField(
          value = heroName,
          onValueChange = { heroName = it },
          label = { Text("Hero Name") },
          singleLine = true
        )
      },
      confirmButton = {
        Button(
          onClick = {
            if (heroName.isNotBlank()) {
              onUpdateHeroName(heroName)
              isRenaming = false
            }
          }
        ) {
          Text("Save")
        }
      },
      dismissButton = {
        TextButton(onClick = { isRenaming = false }) {
          Text("Cancel")
        }
      }
    )
  }

  // Reset confirmation Dialog
  if (showResetConfirm) {
    AlertDialog(
      onDismissRequest = { showResetConfirm = false },
      title = { Text("Reset All Progress?") },
      text = { Text("This will reset all your habits, streaks, level, and stats back to default. This action cannot be undone.") },
      confirmButton = {
        Button(
          onClick = {
            onResetData()
            showResetConfirm = false
            onDismiss()
          },
          colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        ) {
          Text("Reset Everything")
        }
      },
      dismissButton = {
        TextButton(onClick = { showResetConfirm = false }) {
          Text("Cancel")
        }
      }
    )
  }
}
