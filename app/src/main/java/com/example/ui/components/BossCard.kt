package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.SportsKabaddi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.BossPurple
import com.example.ui.theme.BossPurpleContainer
import com.example.ui.theme.GoldAccent

@Composable
fun BossCard(
  bossHp: Int,
  bossClaimed: Boolean,
  onClaimReward: () -> Unit,
  modifier: Modifier = Modifier
) {
  val animatedProgress by animateFloatAsState(
    targetValue = (bossHp / 500f).coerceIn(0f, 1f),
    label = "Boss HP Progress"
  )

  Surface(
    modifier = modifier.fillMaxWidth(),
    shape = RoundedCornerShape(20.dp),
    color = MaterialTheme.colorScheme.surface,
    border = BorderStroke(1.dp, BossPurple.copy(alpha = 0.35f)),
    shadowElevation = 3.dp
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .background(
          Brush.verticalGradient(
            colors = listOf(
              BossPurple.copy(alpha = 0.08f),
              MaterialTheme.colorScheme.surface
            )
          )
        )
        .padding(16.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Boss Art thumbnail
        Box(
          modifier = Modifier
            .size(56.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(BossPurple.copy(alpha = 0.2f))
        ) {
          Image(
            painter = painterResource(id = R.drawable.boss_procrastination),
            contentDescription = "Boss Procrastination",
            contentScale = ContentScale.Crop,
            modifier = Modifier.matchParentSize()
          )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.SportsKabaddi,
              contentDescription = null,
              tint = BossPurple,
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "Boss Battle",
              style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold,
                color = BossPurple
              )
            )
          }
          Text(
            text = "Procrastination Shadow",
            style = MaterialTheme.typography.titleMedium.copy(
              fontWeight = FontWeight.Bold,
              fontSize = 17.sp
            ),
            color = MaterialTheme.colorScheme.onSurface
          )
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // HP Bar row
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = if (bossHp == 0) "DEFEATED" else "Boss HP",
          style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
          color = if (bossHp == 0) GoldAccent else MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
          text = "$bossHp / 500",
          style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
          color = MaterialTheme.colorScheme.onSurface
        )
      }

      Spacer(modifier = Modifier.height(6.dp))

      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(8.dp)
          .clip(CircleShape)
          .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
      ) {
        Box(
          modifier = Modifier
            .fillMaxWidth(animatedProgress)
            .height(8.dp)
            .clip(CircleShape)
            .background(
              Brush.horizontalGradient(
                colors = listOf(BossPurple, Color(0xFFC084FC))
              )
            )
        )
      }

      Spacer(modifier = Modifier.height(10.dp))

      Text(
        text = "Every completed good habit deals 5 damage. Defeat the boss at 0 HP and earn 500 gold.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )

      if (bossHp == 0) {
        Spacer(modifier = Modifier.height(12.dp))
        if (!bossClaimed) {
          Button(
            onClick = onClaimReward,
            modifier = Modifier
              .fillMaxWidth()
              .testTag("claim_boss_button"),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
              containerColor = GoldAccent,
              contentColor = Color.Black
            )
          ) {
            Icon(
              imageVector = Icons.Default.EmojiEvents,
              contentDescription = null,
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "Claim Boss Reward (500 Gold)",
              style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
            )
          }
        } else {
          Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            color = GoldAccent.copy(alpha = 0.15f)
          ) {
            Row(
              modifier = Modifier.padding(10.dp),
              horizontalArrangement = Arrangement.Center,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(
                imageVector = Icons.Default.EmojiEvents,
                contentDescription = null,
                tint = GoldAccent,
                modifier = Modifier.size(16.dp)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = "Boss Defeated! +500 Gold Claimed",
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                color = GoldAccent
              )
            }
          }
        }
      }
    }
  }
}
