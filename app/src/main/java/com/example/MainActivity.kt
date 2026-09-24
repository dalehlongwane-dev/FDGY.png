package com.example

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.Habit
import com.example.ui.HabitQuestUiState
import com.example.ui.HabitQuestViewModel
import com.example.ui.components.AccountSyncDialog
import com.example.ui.components.AddEditHabitDialog
import com.example.ui.components.RpgHud
import com.example.ui.components.SettingsDialog
import com.example.ui.screens.HeroScreen
import com.example.ui.screens.QuestScreen
import com.example.ui.screens.RewardsScreen
import com.example.ui.screens.StatsScreen
import com.example.ui.theme.MyApplicationTheme
import org.json.JSONArray
import org.json.JSONObject

enum class ScreenTab(val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
  QUEST("Quest", Icons.Default.TrackChanges),
  STATS("Stats", Icons.Default.ShowChart),
  REWARDS("Rewards", Icons.Default.EmojiEvents),
  HERO("Hero", Icons.Default.Person)
}

class MainActivity : ComponentActivity() {

  private val viewModel: HabitQuestViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      val uiState by viewModel.uiState.collectAsStateWithLifecycle()
      val systemDark = isSystemInDarkTheme()
      val isDarkTheme = when (uiState.themeMode) {
        "light" -> false
        "dark" -> true
        else -> systemDark
      }

      MyApplicationTheme(darkTheme = isDarkTheme) {
        val context = LocalContext.current

        LaunchedEffect(uiState.toastMessage) {
          uiState.toastMessage?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            viewModel.clearToast()
          }
        }

        HabitQuestApp(
          uiState = uiState,
          viewModel = viewModel,
          isDarkTheme = isDarkTheme,
          onExportData = {
            exportJson(uiState)
          }
        )
      }
    }
  }

  private fun exportJson(uiState: HabitQuestUiState) {
    try {
      val root = JSONObject()
      val profileObj = JSONObject().apply {
        put("name", uiState.playerProfile.name)
        put("level", uiState.playerProfile.level)
        put("hp", uiState.playerProfile.hp)
        put("xp", uiState.playerProfile.xp)
        put("coins", uiState.playerProfile.coins)
        put("streak", uiState.playerProfile.streak)
        put("bestStreak", uiState.playerProfile.bestStreak)
        put("shields", uiState.playerProfile.shields)
        put("avatar", uiState.playerProfile.avatar)
        put("themeMode", uiState.themeMode)
      }
      root.put("player", profileObj)

      val habitsArr = JSONArray()
      uiState.habits.forEach { h ->
        habitsArr.put(JSONObject().apply {
          put("id", h.id)
          put("name", h.name)
          put("type", h.type)
          put("difficulty", h.difficulty)
          put("streak", h.streak)
          put("archived", h.archived)
        })
      }
      root.put("habits", habitsArr)

      val daysArr = JSONArray()
      uiState.dayRecords.forEach { d ->
        daysArr.put(JSONObject().apply {
          put("date", d.date)
          put("completedGood", d.completedGood)
          put("completedBad", d.completedBad)
          put("score", d.score ?: 0.0)
        })
      }
      root.put("days", daysArr)

      val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, root.toString(2))
        type = "application/json"
      }
      val shareIntent = Intent.createChooser(sendIntent, "Export HabitQuest Data")
      startActivity(shareIntent)
    } catch (e: Exception) {
      Toast.makeText(this, "Export failed: ${e.message}", Toast.LENGTH_SHORT).show()
    }
  }
}

@Composable
fun HabitQuestApp(
  uiState: HabitQuestUiState,
  viewModel: HabitQuestViewModel,
  isDarkTheme: Boolean,
  onExportData: () -> Unit
) {
  var currentTab by remember { mutableStateOf(ScreenTab.QUEST) }
  var showAddDialog by remember { mutableStateOf(false) }
  var habitToEdit by remember { mutableStateOf<Habit?>(null) }
  var showSettingsDialog by remember { mutableStateOf(false) }
  var showAccountSyncDialog by remember { mutableStateOf(false) }

  Scaffold(
    modifier = Modifier.fillMaxSize(),
    contentWindowInsets = WindowInsets.safeDrawing,
    topBar = {
      RpgHud(
        profile = uiState.playerProfile,
        dailyScore = uiState.todayScore,
        authState = uiState.authState,
        syncState = uiState.syncState,
        isDarkTheme = isDarkTheme,
        onToggleTheme = { viewModel.toggleTheme() },
        onOpenSettings = { showSettingsDialog = true },
        onOpenAccountSync = { showAccountSyncDialog = true }
      )
    },
    bottomBar = {
      NavigationBar(
        tonalElevation = 4.dp
      ) {
        ScreenTab.entries.forEach { tab ->
          val isSelected = currentTab == tab
          NavigationBarItem(
            selected = isSelected,
            onClick = { currentTab = tab },
            icon = {
              Icon(
                imageVector = tab.icon,
                contentDescription = tab.title
              )
            },
            label = {
              Text(
                text = tab.title,
                style = MaterialTheme.typography.labelSmall
              )
            },
            modifier = Modifier.testTag("nav_tab_${tab.name.lowercase()}")
          )
        }
      }
    },
    floatingActionButton = {
      if (currentTab == ScreenTab.QUEST) {
        FloatingActionButton(
          onClick = {
            habitToEdit = null
            showAddDialog = true
          },
          containerColor = MaterialTheme.colorScheme.primary,
          contentColor = MaterialTheme.colorScheme.onPrimary,
          modifier = Modifier.testTag("fab_add_habit")
        ) {
          Icon(imageVector = Icons.Default.Add, contentDescription = "Add Habit")
        }
      }
    }
  ) { paddingValues ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
    ) {
      when (currentTab) {
        ScreenTab.QUEST -> {
          QuestScreen(
            uiState = uiState,
            onSwitchTab = { viewModel.switchTab(it) },
            onHabitAction = { id, answer -> viewModel.recordHabitAction(id, answer) },
            onEditHabit = { habit ->
              habitToEdit = habit
              showAddDialog = true
            },
            onOpenAddHabit = {
              habitToEdit = null
              showAddDialog = true
            },
            onRestoreHabit = { id -> viewModel.restoreHabit(id) },
            onClaimBossReward = { viewModel.claimBossReward() }
          )
        }
        ScreenTab.STATS -> {
          StatsScreen(
            uiState = uiState,
            onSelectDate = { viewModel.selectCalendarDate(it) }
          )
        }
        ScreenTab.REWARDS -> {
          RewardsScreen(
            uiState = uiState,
            onBuyItem = { viewModel.buyShopItem(it) },
            onClaimWeekly = { viewModel.claimWeeklyReward() }
          )
        }
        ScreenTab.HERO -> {
          HeroScreen(
            uiState = uiState,
            onOpenCloudSync = { showAccountSyncDialog = true },
            onOpenSettings = { showSettingsDialog = true }
          )
        }
      }
    }

    // Dialogs
    if (showAddDialog) {
      AddEditHabitDialog(
        habitToEdit = habitToEdit,
        onDismiss = {
          showAddDialog = false
          habitToEdit = null
        },
        onSave = { name, type, diff ->
          if (habitToEdit == null) {
            viewModel.addHabit(name, type, diff)
          } else {
            viewModel.editHabit(habitToEdit!!.id, name, type, diff)
          }
        },
        onArchive = habitToEdit?.let { h ->
          { viewModel.archiveHabit(h.id) }
        }
      )
    }

    if (showSettingsDialog) {
      SettingsDialog(
        profile = uiState.playerProfile,
        authState = uiState.authState,
        themeMode = uiState.themeMode,
        onDismiss = { showSettingsDialog = false },
        onUpdateHeroName = { viewModel.updateHeroName(it) },
        onSelectThemeMode = { viewModel.setThemeMode(it) },
        onOpenCloudSync = { showAccountSyncDialog = true },
        onExportData = onExportData,
        onResetData = { viewModel.resetAllData() }
      )
    }

    if (showAccountSyncDialog) {
      AccountSyncDialog(
        authState = uiState.authState,
        syncState = uiState.syncState,
        onDismiss = { showAccountSyncDialog = false },
        onLogin = { email, pass, cb -> viewModel.loginFirebase(email, pass, cb) },
        onRegister = { email, pass, cb -> viewModel.registerFirebase(email, pass, cb) },
        onSignOut = { viewModel.signOutFirebase() },
        onSyncNow = { viewModel.syncNow() }
      )
    }
  }
}
