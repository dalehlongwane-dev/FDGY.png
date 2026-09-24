package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.firebase.AuthUserState
import com.example.data.firebase.CloudSyncState
import com.example.data.firebase.SyncStatus
import com.example.ui.theme.EmeraldGood
import com.example.ui.theme.IndigoPrimary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AccountSyncDialog(
  authState: AuthUserState,
  syncState: CloudSyncState,
  onDismiss: () -> Unit,
  onLogin: (email: String, pass: String, (Boolean, String) -> Unit) -> Unit,
  onRegister: (email: String, pass: String, (Boolean, String) -> Unit) -> Unit,
  onSignOut: () -> Unit,
  onSyncNow: () -> Unit
) {
  var isRegisterTab by remember { mutableStateOf(false) }
  var email by remember { mutableStateOf(authState.email ?: "dale.hlongwane@gmail.com") }
  var password by remember { mutableStateOf("") }
  var isPasswordVisible by remember { mutableStateOf(false) }
  var isLoading by remember { mutableStateOf(false) }
  var statusMessage by remember { mutableStateOf<String?>(null) }
  var isError by remember { mutableStateOf(false) }

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
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(IndigoPrimary.copy(alpha = 0.15f)),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.CloudSync,
                contentDescription = null,
                tint = IndigoPrimary,
                modifier = Modifier.size(20.dp)
              )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Text(
                text = "Cloud Sync & Account",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
              )
              Text(
                text = "Secure Firebase Authentication",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }

          IconButton(onClick = onDismiss) {
            Icon(
              imageVector = Icons.Default.Close,
              contentDescription = "Close",
              tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (authState.isAuthenticated) {
          // Logged in UI state
          Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = EmeraldGood.copy(alpha = 0.1f),
            border = BorderStroke(1.dp, EmeraldGood.copy(alpha = 0.3f))
          ) {
            Row(
              modifier = Modifier.padding(14.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = EmeraldGood,
                modifier = Modifier.size(24.dp)
              )
              Spacer(modifier = Modifier.width(12.dp))
              Column {
                Text(
                  text = "Signed In",
                  style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                  color = EmeraldGood
                )
                Text(
                  text = authState.email ?: "Firebase Account",
                  style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                  color = MaterialTheme.colorScheme.onSurface
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          // Sync status info
          Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
          ) {
            Column(modifier = Modifier.padding(14.dp)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(
                  text = "Sync Status",
                  style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                  text = syncState.status.name,
                  style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                  color = if (syncState.status == SyncStatus.SUCCESS) EmeraldGood else IndigoPrimary
                )
              }

              Spacer(modifier = Modifier.height(4.dp))

              Text(
                text = syncState.message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
              )

              if (syncState.lastSyncedTime > 0) {
                val formatted = SimpleDateFormat("MMM d, yyyy · HH:mm", Locale.getDefault()).format(Date(syncState.lastSyncedTime))
                Text(
                  text = "Last synced: $formatted",
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(16.dp))

          // Sync Now button
          Button(
            onClick = onSyncNow,
            modifier = Modifier
              .fillMaxWidth()
              .height(48.dp)
              .testTag("sync_now_button"),
            shape = RoundedCornerShape(12.dp),
            enabled = syncState.status != SyncStatus.SYNCING
          ) {
            if (syncState.status == SyncStatus.SYNCING) {
              CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = Color.White,
                strokeWidth = 2.dp
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text("Syncing...")
            } else {
              Icon(
                imageVector = Icons.Default.Sync,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text("Sync Now to All Devices", fontWeight = FontWeight.Bold)
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          // Sign Out button
          OutlinedButton(
            onClick = onSignOut,
            modifier = Modifier
              .fillMaxWidth()
              .height(44.dp)
              .testTag("sign_out_button"),
            shape = RoundedCornerShape(12.dp)
          ) {
            Text("Sign Out", color = MaterialTheme.colorScheme.error)
          }

        } else {
          // Not signed in: Show Tabs for Log In / Register
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            FilterChip(
              selected = !isRegisterTab,
              onClick = { isRegisterTab = false },
              label = { Text("Log In") },
              modifier = Modifier.weight(1f)
            )
            FilterChip(
              selected = isRegisterTab,
              onClick = { isRegisterTab = true },
              label = { Text("Create Account") },
              modifier = Modifier.weight(1f)
            )
          }

          Spacer(modifier = Modifier.height(14.dp))

          // Email field
          OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email Address") },
            leadingIcon = {
              Icon(imageVector = Icons.Default.Mail, contentDescription = null)
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
              keyboardType = KeyboardType.Email,
              imeAction = ImeAction.Next
            ),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("auth_email_input"),
            shape = RoundedCornerShape(12.dp)
          )

          Spacer(modifier = Modifier.height(10.dp))

          // Password field
          OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            leadingIcon = {
              Icon(imageVector = Icons.Default.Lock, contentDescription = null)
            },
            trailingIcon = {
              IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                Icon(
                  imageVector = if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                  contentDescription = "Toggle password visibility"
                )
              }
            },
            visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
              keyboardType = KeyboardType.Password,
              imeAction = ImeAction.Done
            ),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("auth_password_input"),
            shape = RoundedCornerShape(12.dp)
          )

          AnimatedVisibility(visible = statusMessage != null) {
            statusMessage?.let { msg ->
              Spacer(modifier = Modifier.height(10.dp))
              Text(
                text = msg,
                style = MaterialTheme.typography.bodySmall,
                color = if (isError) MaterialTheme.colorScheme.error else EmeraldGood
              )
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          // Info banner
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(12.dp))
              .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
              .padding(10.dp),
            verticalAlignment = Alignment.Top
          ) {
            Icon(
              imageVector = Icons.Default.Info,
              contentDescription = null,
              tint = IndigoPrimary,
              modifier = Modifier
                .size(16.dp)
                .padding(top = 2.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "Log in to securely sync your habits, streaks, level, and rewards across all your Android devices via Firebase.",
              style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }

          Spacer(modifier = Modifier.height(16.dp))

          Button(
            onClick = {
              if (email.isNotBlank() && password.isNotBlank()) {
                isLoading = true
                statusMessage = null
                if (isRegisterTab) {
                  onRegister(email, password) { success, msg ->
                    isLoading = false
                    statusMessage = msg
                    isError = !success
                  }
                } else {
                  onLogin(email, password) { success, msg ->
                    isLoading = false
                    statusMessage = msg
                    isError = !success
                  }
                }
              } else {
                statusMessage = "Please enter email and password"
                isError = true
              }
            },
            enabled = !isLoading && email.isNotBlank() && password.isNotBlank(),
            modifier = Modifier
              .fillMaxWidth()
              .height(48.dp)
              .testTag("auth_submit_button"),
            shape = RoundedCornerShape(12.dp)
          ) {
            if (isLoading) {
              CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = Color.White,
                strokeWidth = 2.dp
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(if (isRegisterTab) "Creating Account..." else "Signing In...")
            } else {
              Text(
                text = if (isRegisterTab) "Create Firebase Account" else "Log In to Sync",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
              )
            }
          }
        }
      }
    }
  }
}
