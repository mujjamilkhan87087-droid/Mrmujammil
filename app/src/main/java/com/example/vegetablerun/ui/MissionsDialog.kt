package com.example.vegetablerun.ui

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.vegetablerun.storage.GameStorage

@Composable
fun MissionsDialog(
    storage: GameStorage,
    onDismiss: () -> Unit,
    onCoinsUpdated: () -> Unit
) {
    var missions by remember { mutableStateOf(storage.getMissions()) }
    val (dailyAvailable, streak) = storage.checkDailyRewardAvailable()
    var dailyRewardAvailable by remember { mutableStateOf(dailyAvailable) }
    var currentStreak by remember { mutableIntStateOf(streak) }
    var rewardClaimedMsg by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🎯 Daily Challenges",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFE65100)
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_missions_button")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close Missions")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Daily Login Streak Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "☀️ Daily Login Streak: Day $currentStreak",
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFE65100),
                                    fontSize = 15.sp
                                )
                                Text(
                                    text = "Log in every day for bonus coins!",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        if (dailyRewardAvailable) {
                            Button(
                                onClick = {
                                    val amount = storage.claimDailyReward()
                                    dailyRewardAvailable = false
                                    rewardClaimedMsg = "🎉 Claimed +$amount Coins!"
                                    onCoinsUpdated()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF43A047)),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("claim_daily_button")
                            ) {
                                Text("Claim +${50 + (currentStreak - 1) * 25} Coins!", fontWeight = FontWeight.Bold)
                            }
                        } else {
                            Text(
                                text = "✅ Today's daily reward claimed. Come back tomorrow!",
                                fontSize = 12.sp,
                                color = Color(0xFF2E7D32),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                rewardClaimedMsg?.let { msg ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = msg,
                        color = Color(0xFF2E7D32),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Active Missions",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    modifier = Modifier.align(Alignment.Start)
                )

                Spacer(modifier = Modifier.height(8.dp))

                missions.forEach { mission ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.03f)),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 5.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = mission.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text(
                                    text = "🪙 +${mission.rewardCoins}",
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFE65100),
                                    fontSize = 13.sp
                                )
                            }

                            Text(
                                text = mission.description,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            val progress = (mission.current.toFloat() / mission.target).coerceIn(0f, 1f)
                            LinearProgressIndicator(
                                progress = { progress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp),
                                color = Color(0xFFFB8500),
                                trackColor = Color(0xFFFFE0B2)
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${mission.current.coerceAtMost(mission.target)} / ${mission.target}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                when {
                                    mission.isClaimed -> {
                                        Text(
                                            text = "Claimed ✓",
                                            color = Color(0xFF2E7D32),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                    }
                                    mission.current >= mission.target -> {
                                        Button(
                                            onClick = {
                                                if (storage.claimMissionReward(mission.id)) {
                                                    missions = storage.getMissions()
                                                    rewardClaimedMsg = "🎉 Claimed +${mission.rewardCoins} Coins!"
                                                    onCoinsUpdated()
                                                }
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFB8500)),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier
                                                .height(34.dp)
                                                .testTag("claim_mission_${mission.id}")
                                        ) {
                                            Text("Claim", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                    else -> {
                                        Text(
                                            text = "In Progress",
                                            fontSize = 11.sp,
                                            color = Color(0xFF757575)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFB8500)),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("done_missions_button")
                ) {
                    Text("Close", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    }
}
