package com.example.vegetablerun.ui

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Star
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.vegetablerun.ads.AdManager
import com.example.vegetablerun.model.CharacterId
import com.example.vegetablerun.storage.GameStorage

@Composable
fun MainMenuScreen(
    storage: GameStorage,
    adManager: AdManager,
    onStartGame: (CharacterId) -> Unit,
    onOpenShop: () -> Unit,
    onOpenMissions: () -> Unit,
    onOpenTutorial: () -> Unit,
    onOpenSettings: () -> Unit
) {
    var coins by remember { mutableIntStateOf(storage.coins) }
    var selectedChar by remember { mutableStateOf(storage.selectedCharacter) }
    val characterList = remember { CharacterId.entries }
    var charCarouselIndex by remember {
        mutableIntStateOf(characterList.indexOf(selectedChar).coerceAtLeast(0))
    }

    val displayedChar = characterList[charCarouselIndex]
    val isUnlocked = storage.isCharacterUnlocked(displayedChar)
    val isSelected = selectedChar == displayedChar
    val (dailyAvailable, _) = storage.checkDailyRewardAvailable()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFFFFBEA) // Warm ivory desi background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Bar: Coins Pill & Settings
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Coins Pill with Watch Ad bonus button
                Row(
                    modifier = Modifier
                        .background(Color(0xFFFFF3E0), RoundedCornerShape(20.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("🪙", fontSize = 18.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "$coins",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFE65100),
                        fontSize = 15.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .background(Color(0xFF43A047), RoundedCornerShape(10.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "+100 (Ad)",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.testTag("free_coins_pill")
                        )
                    }
                }

                Row {
                    IconButton(
                        onClick = onOpenTutorial,
                        modifier = Modifier.testTag("tutorial_nav_button")
                    ) {
                        Icon(Icons.Default.HelpOutline, contentDescription = "Tutorial", tint = Color(0xFFFB8500))
                    }
                    IconButton(
                        onClick = onOpenSettings,
                        modifier = Modifier.testTag("settings_nav_button")
                    ) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings", tint = Color(0xFF5D4037))
                    }
                }
            }

            // Hero Village Artwork Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(170.dp)
                    .clip(RoundedCornerShape(20.dp))
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_hero_village),
                    contentDescription = "Indian Village Runner Banner",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // Shaded Title Overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.75f)),
                                startY = 60f
                            )
                        )
                        .padding(14.dp),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "VEGETABLE RUN",
                            fontWeight = FontWeight.Black,
                            color = Color(0xFFFFD54F),
                            fontSize = 26.sp,
                            letterSpacing = 1.2.sp
                        )
                        Text(
                            text = "सब्ज़ी रन • Desi Endless Runner",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Player Stats Ribbon
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(14.dp),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("🏆 High Score", fontSize = 11.sp, color = Color.Gray)
                        Text(
                            text = "${storage.highScore}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color(0xFFE65100)
                        )
                    }
                }

                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(14.dp),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("📍 Best Distance", fontSize = 11.sp, color = Color.Gray)
                        Text(
                            text = "${storage.bestDistanceMeters} m",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color(0xFF023E8A)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Character Showcase Carousel
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(3.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                charCarouselIndex = if (charCarouselIndex > 0) charCarouselIndex - 1 else characterList.size - 1
                            },
                            modifier = Modifier.testTag("char_prev_button")
                        ) {
                            Icon(Icons.Default.ChevronLeft, contentDescription = "Previous Character")
                        }

                        // Big character icon with kurta color halo
                        Box(
                            modifier = Modifier
                                .size(76.dp)
                                .background(displayedChar.kurtaColor, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = when (displayedChar) {
                                    CharacterId.ALOO -> "🥔"
                                    CharacterId.BAINGAN -> "🍆"
                                    CharacterId.TAMATAR -> "🍅"
                                },
                                fontSize = 42.sp
                            )
                        }

                        IconButton(
                            onClick = {
                                charCarouselIndex = if (charCarouselIndex < characterList.size - 1) charCarouselIndex + 1 else 0
                            },
                            modifier = Modifier.testTag("char_next_button")
                        ) {
                            Icon(Icons.Default.ChevronRight, contentDescription = "Next Character")
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = displayedChar.displayName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Text(
                        text = displayedChar.hindiTitle,
                        fontSize = 13.sp,
                        color = Color(0xFFFB8500),
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = displayedChar.description,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )

                    // Perk Pill
                    Box(
                        modifier = Modifier
                            .background(Color(0xFFFFF3E0), RoundedCornerShape(10.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "⚡ ${displayedChar.perk}",
                            color = Color(0xFFE65100),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Character Action: Select or Unlock
                    when {
                        isSelected -> {
                            Box(
                                modifier = Modifier
                                    .background(Color(0xFFE8F5E9), RoundedCornerShape(10.dp))
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                Text("✅ Ready to Run!", color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                            }
                        }
                        isUnlocked -> {
                            Button(
                                onClick = {
                                    storage.selectedCharacter = displayedChar
                                    selectedChar = displayedChar
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFB8500)),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.testTag("select_active_runner")
                            ) {
                                Text("Select Runner", fontWeight = FontWeight.Bold)
                            }
                        }
                        else -> {
                            Button(
                                onClick = {
                                    if (coins >= displayedChar.basePrice) {
                                        storage.coins -= displayedChar.basePrice
                                        coins = storage.coins
                                        storage.unlockCharacter(displayedChar)
                                        storage.selectedCharacter = displayedChar
                                        selectedChar = displayedChar
                                    }
                                },
                                enabled = coins >= displayedChar.basePrice,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.testTag("unlock_from_menu")
                            ) {
                                Text("Unlock for ${displayedChar.basePrice} 🪙", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Primary Play Button ("RUN NOW! / दौड़ शुरू करें")
            Button(
                onClick = { onStartGame(selectedChar) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE65100)),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp)
                    .padding(horizontal = 16.dp)
                    .testTag("play_game_button")
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(28.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "RUN NOW! • दौड़ शुरू करें",
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Secondary Quick Actions (Shop, Daily Challenges)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onOpenShop,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFB8500)),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("shop_nav_button")
                ) {
                    Icon(Icons.Default.ShoppingBag, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Bazaar / Shop", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                Button(
                    onClick = onOpenMissions,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (dailyAvailable) Color(0xFF2E7D32) else Color(0xFF023E8A)
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("missions_nav_button")
                ) {
                    Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        if (dailyAvailable) "Daily (Claim!)" else "Missions",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
