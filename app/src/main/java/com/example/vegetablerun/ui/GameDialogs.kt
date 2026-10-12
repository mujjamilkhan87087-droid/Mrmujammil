package com.example.vegetablerun.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeMute
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.vegetablerun.audio.SoundManager
import com.example.vegetablerun.engine.GameSnapshot
import com.example.vegetablerun.storage.GameStorage
import kotlinx.coroutines.delay

@Composable
fun GameOverDialog(
    snapshot: GameSnapshot,
    storage: GameStorage,
    onRevive: () -> Unit,
    onRestart: () -> Unit,
    onHome: () -> Unit
) {
    val isNewHighScore = snapshot.score >= storage.highScore && snapshot.score > 0

    Dialog(onDismissRequest = { /* Must choose an option */ }) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "💥 Run Ended!",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFD32F2F)
                )

                if (isNewHighScore) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "🏆 NEW HIGH SCORE! 🏆",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFE65100),
                        fontSize = 15.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Stats Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ScoreStatRow(label = "Final Score", value = "${snapshot.score}")
                        ScoreStatRow(label = "Best Score", value = "${storage.highScore}")
                        ScoreStatRow(label = "Distance", value = "${snapshot.distanceMeters} m")
                        ScoreStatRow(label = "Coins Collected", value = "+${snapshot.runCoins} 🪙")
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Revive with Test Rewarded Ad (if available)
                if (snapshot.canRevive) {
                    Button(
                        onClick = onRevive,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("revive_ad_button")
                    ) {
                        Text("📺 Watch Ad to Revive (1/1)", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                // Play Again
                Button(
                    onClick = onRestart,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFB8500)),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("play_again_button")
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Play Again", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Back to Main Menu
                OutlinedButton(
                    onClick = onHome,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .testTag("menu_button")
                ) {
                    Icon(Icons.Default.Home, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Main Menu", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
fun PauseDialog(
    onResume: () -> Unit,
    onRestart: () -> Unit,
    onHome: () -> Unit,
    storage: GameStorage,
    soundManager: SoundManager
) {
    var sfx by remember { mutableStateOf(storage.isSfxEnabled) }
    var music by remember { mutableStateOf(storage.isMusicEnabled) }

    Dialog(onDismissRequest = onResume) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "⏸️ Game Paused",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFE65100)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Audio quick toggles
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    IconButton(
                        onClick = {
                            sfx = !sfx
                            storage.isSfxEnabled = sfx
                            soundManager.isSfxEnabled = sfx
                        },
                        modifier = Modifier
                            .background(if (sfx) Color(0xFFFFF3E0) else Color(0xFFEEEEEE), CircleShape)
                            .testTag("toggle_sfx_pause")
                    ) {
                        Icon(
                            if (sfx) Icons.AutoMirrored.Filled.VolumeUp else Icons.AutoMirrored.Filled.VolumeMute,
                            contentDescription = "SFX",
                            tint = if (sfx) Color(0xFFFB8500) else Color.Gray
                        )
                    }

                    IconButton(
                        onClick = {
                            music = !music
                            storage.isMusicEnabled = music
                            soundManager.isMusicEnabled = music
                        },
                        modifier = Modifier
                            .background(if (music) Color(0xFFFFF3E0) else Color(0xFFEEEEEE), CircleShape)
                            .testTag("toggle_music_pause")
                    ) {
                        Icon(
                            if (music) Icons.AutoMirrored.Filled.VolumeUp else Icons.AutoMirrored.Filled.VolumeMute,
                            contentDescription = "Music",
                            tint = if (music) Color(0xFFFB8500) else Color.Gray
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = onResume,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("resume_button")
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Resume Run", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = onRestart,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFB8500)),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("restart_pause_button")
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Restart", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedButton(
                    onClick = onHome,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .testTag("quit_pause_button")
                ) {
                    Icon(Icons.Default.Home, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Quit to Menu", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
fun TestAdOverlay(
    isRewarded: Boolean,
    onAdCompleted: (Boolean) -> Unit
) {
    var countdown by remember { mutableIntStateOf(if (isRewarded) 5 else 3) }

    LaunchedEffect(Unit) {
        while (countdown > 0) {
            delay(1000L)
            countdown--
        }
    }

    Dialog(onDismissRequest = {
        if (countdown <= 0) onAdCompleted(true)
    }) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color(0xFF212121),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Google AdMob (Test Ad)",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )

                    if (countdown <= 0) {
                        IconButton(
                            onClick = { onAdCompleted(true) },
                            modifier = Modifier.testTag("close_test_ad_button")
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                        }
                    } else {
                        Text(
                            text = "Reward in: ${countdown}s",
                            color = Color(0xFFFFD54F),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .background(Color(0xFF333333), RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (isRewarded) "🎬 Rewarded Video Test Ad" else "📢 Interstitial Test Ad",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Ad Unit ID:\nca-app-pub-3940256099942544/${if (isRewarded) "5224354917" else "1033173712"}",
                            color = Color.LightGray,
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (countdown > 0) "Simulating test video playback..." else "✅ Test Ad Finished!",
                            color = Color(0xFF81C784),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                if (countdown <= 0) {
                    Button(
                        onClick = { onAdCompleted(true) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF43A047)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .testTag("claim_ad_reward_button")
                    ) {
                        Text("Claim Reward & Continue", fontWeight = FontWeight.Bold)
                    }
                } else {
                    Text(
                        text = "Please watch until the timer finishes...",
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun ScoreStatRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
        Text(text = value, fontWeight = FontWeight.Bold, fontSize = 16.sp)
    }
}
