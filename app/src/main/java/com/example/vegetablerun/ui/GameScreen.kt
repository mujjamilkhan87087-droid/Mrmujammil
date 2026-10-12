package com.example.vegetablerun.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vegetablerun.ads.AdManager
import com.example.vegetablerun.audio.SoundManager
import com.example.vegetablerun.engine.GameEngine
import com.example.vegetablerun.engine.GameRenderer
import com.example.vegetablerun.engine.GameState
import com.example.vegetablerun.storage.GameStorage
import kotlinx.coroutines.isActive
import kotlin.math.abs

@Composable
fun GameScreen(
    engine: GameEngine,
    storage: GameStorage,
    soundManager: SoundManager,
    adManager: AdManager,
    onNavigateHome: () -> Unit
) {
    val snapshot by engine.snapshot.collectAsState()
    val isShowingRewardedAd by adManager.isShowingRewardedAd.collectAsState()
    val isShowingInterstitialAd by adManager.isShowingInterstitialAd.collectAsState()

    // 60 FPS update loop
    LaunchedEffect(engine) {
        var lastTime = withFrameNanos { it }
        while (isActive) {
            withFrameNanos { now ->
                val delta = (now - lastTime) / 1_000_000_000f
                lastTime = now
                engine.update(delta)
            }
        }
    }

    // Animation progress for spinning coins & pulsing power-ups
    val infiniteTransition = rememberInfiniteTransition(label = "game_anim")
    val animProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing)
        ),
        label = "anim_progress"
    )

    // Touch gesture drag state
    var totalDragX by remember { mutableFloatStateOf(0f) }
    var totalDragY by remember { mutableFloatStateOf(0f) }
    var hasHandledSwipe by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // 3D Canvas Game View
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .testTag("game_canvas")
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = {
                            totalDragX = 0f
                            totalDragY = 0f
                            hasHandledSwipe = false
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            if (!hasHandledSwipe) {
                                totalDragX += dragAmount.x
                                totalDragY += dragAmount.y

                                val threshold = 28.dp.toPx()
                                if (abs(totalDragX) > threshold || abs(totalDragY) > threshold) {
                                    hasHandledSwipe = true
                                    if (abs(totalDragX) > abs(totalDragY)) {
                                        // Horizontal swipe
                                        if (totalDragX < 0) engine.moveLeft() else engine.moveRight()
                                    } else {
                                        // Vertical swipe
                                        if (totalDragY < 0) engine.jump() else engine.slide()
                                    }
                                }
                            }
                        },
                        onDragEnd = {
                            hasHandledSwipe = false
                        },
                        onDragCancel = {
                            hasHandledSwipe = false
                        }
                    )
                }
        ) {
            GameRenderer.drawGame(this, snapshot, animProgress)
        }

        // Top HUD Overlay
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Score & Theme Badges
                Row(verticalAlignment = Alignment.CenterVertically) {
                    HudPill(
                        text = "🏆 ${snapshot.score}",
                        color = Color(0xFFFB8500)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    HudPill(
                        text = "📍 ${snapshot.distanceMeters}m",
                        color = Color(0xFF023E8A)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    HudPill(
                        text = "🪙 ${snapshot.runCoins}",
                        color = Color(0xFF2E7D32)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = { engine.pauseGame() },
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = Color.Black.copy(alpha = 0.5f),
                            contentColor = Color.White
                        ),
                        modifier = Modifier
                            .size(38.dp)
                            .testTag("pause_button")
                    ) {
                        Icon(Icons.Default.Pause, contentDescription = "Pause", modifier = Modifier.size(20.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Current Area and Active Power-Ups
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Environment Theme Tag
                Text(
                    text = "${snapshot.currentTheme.hindiTitle} (${snapshot.currentTheme.title})",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    modifier = Modifier
                        .background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )

                // Active Power-Up Timers
                Row {
                    if (snapshot.magnetRemaining > 0f) {
                        PowerUpBadge(icon = "🧲", label = "${snapshot.magnetRemaining.toInt()}s", color = Color(0xFF00E676))
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    if (snapshot.shieldRemaining > 0f) {
                        PowerUpBadge(icon = "🛡️", label = "${snapshot.shieldRemaining.toInt()}s", color = Color(0xFF00B0FF))
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    if (snapshot.multiplierRemaining > 0f) {
                        PowerUpBadge(icon = "⚡2X", label = "${snapshot.multiplierRemaining.toInt()}s", color = Color(0xFFFFD600))
                    }
                }
            }
        }

        // On-Screen Touch Buttons (Dual Control Mode for low-end / easy thumb gameplay)
        if (storage.isTouchButtonsEnabled && snapshot.state == GameState.RUNNING) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 60.dp, start = 16.dp, end = 16.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    // Left / Right thumb controls
                    Row {
                        ThumbControlButton(
                            icon = Icons.AutoMirrored.Filled.ArrowBack,
                            description = "Move Left",
                            onClick = { engine.moveLeft() },
                            tag = "touch_left"
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        ThumbControlButton(
                            icon = Icons.AutoMirrored.Filled.ArrowForward,
                            description = "Move Right",
                            onClick = { engine.moveRight() },
                            tag = "touch_right"
                        )
                    }

                    // Jump / Slide thumb controls
                    Row {
                        ThumbControlButton(
                            icon = Icons.Default.ArrowUpward,
                            description = "Jump",
                            onClick = { engine.jump() },
                            tag = "touch_jump"
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        ThumbControlButton(
                            icon = Icons.Default.ArrowDownward,
                            description = "Slide",
                            onClick = { engine.slide() },
                            tag = "touch_slide"
                        )
                    }
                }
            }
        }

        // Bottom Banner Ad Bar (AdMob Test Banner)
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
                .background(Color(0xFF212121))
                .padding(vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "AdMob Test Banner (320x50) • Test ID: ca-app-pub-3940256099942544/6300978111",
                color = Color.LightGray,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium
            )
        }

        // Modals & Overlays
        when (snapshot.state) {
            GameState.PAUSED -> {
                PauseDialog(
                    onResume = { engine.resumeGame() },
                    onRestart = { engine.startRun() },
                    onHome = {
                        soundManager.stopBackgroundMusic()
                        onNavigateHome()
                    },
                    storage = storage,
                    soundManager = soundManager
                )
            }
            GameState.GAME_OVER -> {
                GameOverDialog(
                    snapshot = snapshot,
                    storage = storage,
                    onRevive = {
                        adManager.showRewardedAd {
                            engine.revive()
                        }
                    },
                    onRestart = {
                        adManager.onGameFinished {
                            engine.startRun()
                        }
                    },
                    onHome = {
                        adManager.onGameFinished {
                            soundManager.stopBackgroundMusic()
                            onNavigateHome()
                        }
                    }
                )
            }
            else -> {}
        }

        // Test Ad Displays
        if (isShowingRewardedAd) {
            TestAdOverlay(
                isRewarded = true,
                onAdCompleted = { earned ->
                    adManager.completeRewardedAd(earned)
                }
            )
        } else if (isShowingInterstitialAd) {
            TestAdOverlay(
                isRewarded = false,
                onAdCompleted = {
                    adManager.closeInterstitialAd()
                }
            )
        }
    }
}

@Composable
private fun HudPill(text: String, color: Color) {
    Box(
        modifier = Modifier
            .background(color.copy(alpha = 0.9f), RoundedCornerShape(12.dp))
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Text(
            text = text,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp
        )
    }
}

@Composable
private fun PowerUpBadge(icon: String, label: String, color: Color) {
    Row(
        modifier = Modifier
            .background(color, RoundedCornerShape(8.dp))
            .padding(horizontal = 6.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = icon, fontSize = 11.sp)
        Spacer(modifier = Modifier.width(3.dp))
        Text(text = label, color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
    }
}

@Composable
private fun ThumbControlButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    description: String,
    onClick: () -> Unit,
    tag: String
) {
    IconButton(
        onClick = onClick,
        colors = IconButtonDefaults.iconButtonColors(
            containerColor = Color.Black.copy(alpha = 0.45f),
            contentColor = Color.White
        ),
        modifier = Modifier
            .size(54.dp)
            .testTag(tag)
    ) {
        Icon(icon, contentDescription = description, modifier = Modifier.size(28.dp))
    }
}
