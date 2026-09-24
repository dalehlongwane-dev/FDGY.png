package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.HabitQuestUiState
import com.example.ui.theme.CrimsonHp
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.IndigoPrimary

data class ShopItemInfo(
  val id: Int,
  val icon: ImageVector,
  val name: String,
  val cost: Int,
  val description: String,
  val tint: Color
)

@Composable
fun RewardsScreen(
  uiState: HabitQuestUiState,
  onBuyItem: (Int) -> Unit,
  onClaimWeekly: () -> Unit,
  modifier: Modifier = Modifier
) {
  val shopItems = listOf(
    ShopItemInfo(
      id = 0,
      icon = Icons.Default.Shield,
      name = "Streak Shield",
      cost = 500,
      description = "Protects one future streak from breaking upon a missed habit. Owned: ${uiState.playerProfile.shields}",
      tint = IndigoPrimary
    ),
    ShopItemInfo(
      id = 1,
      icon = Icons.Default.Bolt,
      name = "XP Boost",
      cost = 350,
      description = "Instantly gain +100 XP to level up your hero and unlock rewards.",
      tint = GoldAccent
    ),
    ShopItemInfo(
      id = 2,
      icon = Icons.Default.Favorite,
      name = "Full Heal",
      cost = 250,
      description = "Restores your Hero HP back to 100 instantly.",
      tint = CrimsonHp
    ),
    ShopItemInfo(
      id = 3,
      icon = Icons.Default.AutoAwesome,
      name = "Hero Costume",
      cost = 750,
      description = "Unlock the legendary Champion Super Hero cosmetic avatar.",
      tint = Color(0xFFA855F7)
    )
  )

  LazyColumn(
    modifier = modifier.fillMaxSize(),
    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    // Balances Header: Gold & Best Streak
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        Surface(
          modifier = Modifier.weight(1f),
          shape = RoundedCornerShape(18.dp),
          color = MaterialTheme.colorScheme.surface,
          border = BorderStroke(1.dp, GoldAccent.copy(alpha = 0.35f)),
          shadowElevation = 2.dp
        ) {
          Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(GoldAccent.copy(alpha = 0.15f)),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.MonetizationOn,
                contentDescription = null,
                tint = GoldAccent,
                modifier = Modifier.size(24.dp)
              )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Text(
                text = "${uiState.playerProfile.coins}",
                style = MaterialTheme.typography.titleLarge.copy(
                  fontWeight = FontWeight.Bold,
                  color = GoldAccent
                )
              )
              Text(
                text = "Gold Coins",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
        }

        Surface(
          modifier = Modifier.weight(1f),
          shape = RoundedCornerShape(18.dp),
          color = MaterialTheme.colorScheme.surface,
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
          shadowElevation = 2.dp
        ) {
          Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(GoldAccent.copy(alpha = 0.15f)),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.LocalFireDepartment,
                contentDescription = null,
                tint = Color(0xFFF97316),
                modifier = Modifier.size(24.dp)
              )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Text(
                text = "${uiState.playerProfile.bestStreak}",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
              )
              Text(
                text = "Best Streak",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
        }
      }
    }

    // Weekly Challenge Card
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
              imageVector = Icons.Default.Flag,
              contentDescription = null,
              tint = IndigoPrimary,
              modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "Weekly Challenge",
              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
              color = MaterialTheme.colorScheme.onSurface
            )
          }

          Spacer(modifier = Modifier.height(8.dp))

          Text(
            text = "Complete 10 good habits this week",
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
          )

          Spacer(modifier = Modifier.height(10.dp))

          val progressFraction = (uiState.weeklyProgress.toFloat() / uiState.weeklyGoal).coerceIn(0f, 1f)

          Box(
            modifier = Modifier
              .fillMaxWidth()
              .height(8.dp)
              .clip(CircleShape)
              .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
          ) {
            Box(
              modifier = Modifier
                .fillMaxWidth(progressFraction)
                .height(8.dp)
                .clip(CircleShape)
                .background(IndigoPrimary)
            )
          }

          Spacer(modifier = Modifier.height(8.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text(
              text = "${uiState.weeklyProgress} / ${uiState.weeklyGoal} completed",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
              text = "Reward: 300 Gold",
              style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.Bold,
                color = GoldAccent
              )
            )
          }

          if (uiState.weeklyProgress >= uiState.weeklyGoal) {
            Spacer(modifier = Modifier.height(12.dp))
            if (!uiState.weeklyClaimed) {
              Button(
                onClick = onClaimWeekly,
                modifier = Modifier
                  .fillMaxWidth()
                  .testTag("claim_weekly_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GoldAccent, contentColor = Color.Black)
              ) {
                Icon(imageVector = Icons.Default.EmojiEvents, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Claim 300 Gold Reward", fontWeight = FontWeight.Bold)
              }
            } else {
              Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                color = GoldAccent.copy(alpha = 0.15f)
              ) {
                Text(
                  text = "Challenge Complete! +300 Gold Claimed",
                  style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = GoldAccent),
                  modifier = Modifier.padding(10.dp)
                )
              }
            }
          }
        }
      }
    }

    // Reward Shop Section
    item {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          imageVector = Icons.Default.ShoppingBag,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.primary,
          modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = "Hero Reward Shop",
          style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
        )
      }
    }

    items(shopItems.size) { index ->
      val item = shopItems[index]
      val canAfford = uiState.playerProfile.coins >= item.cost

      Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
        shadowElevation = 2.dp
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
                .size(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(item.tint.copy(alpha = 0.15f)),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = item.icon,
                contentDescription = item.name,
                tint = item.tint,
                modifier = Modifier.size(22.dp)
              )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
              Text(
                text = item.name,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
              )
              Spacer(modifier = Modifier.height(2.dp))
              Text(
                text = item.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }

          Spacer(modifier = Modifier.width(10.dp))

          OutlinedButton(
            onClick = { onBuyItem(item.id) },
            enabled = canAfford,
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, if (canAfford) GoldAccent else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
            modifier = Modifier.testTag("buy_shop_item_${item.id}")
          ) {
            Icon(
              imageVector = Icons.Default.MonetizationOn,
              contentDescription = null,
              tint = if (canAfford) GoldAccent else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = "${item.cost}",
              fontWeight = FontWeight.Bold,
              color = if (canAfford) GoldAccent else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
            )
          }
        }
      }
    }
  }
}
