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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import com.example.vegetablerun.ads.AdManager
import com.example.vegetablerun.model.CharacterId
import com.example.vegetablerun.storage.GameStorage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShopScreen(
    storage: GameStorage,
    adManager: AdManager,
    onBack: () -> Unit
) {
    var coins by remember { mutableIntStateOf(storage.coins) }
    var selectedTab by remember { mutableIntStateOf(0) }
    var selectedChar by remember { mutableStateOf(storage.selectedCharacter) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Desi Sabzi Bazaar",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("shop_back_button")
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    // Coins Pill
                    Row(
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🪙", fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "$coins",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 15.sp
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFFB8500)
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color(0xFFFFB703),
                contentColor = Color(0xFF370617)
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Vegetable Heroes", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Power Upgrades", fontWeight = FontWeight.Bold) }
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                // Free coins via test rewarded ad
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "🎁 Free Coins Bonus",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2E7D32),
                                fontSize = 15.sp
                            )
                            Text(
                                "Watch a short test ad to get +100 coins",
                                fontSize = 12.sp,
                                color = Color(0xFF388E3C)
                            )
                        }
                        Button(
                            onClick = {
                                adManager.showRewardedAd {
                                    storage.coins += 100
                                    coins = storage.coins
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF43A047)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("watch_test_ad_button")
                        ) {
                            Text("+100 🪙", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (selectedTab == 0) {
                    // Character Shop
                    CharacterId.entries.forEach { charId ->
                        val isUnlocked = storage.isCharacterUnlocked(charId)
                        val isSelected = selectedChar == charId

                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) Color(0xFFFFF3E0) else Color.White
                            ),
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Character Avatar badge
                                Box(
                                    modifier = Modifier
                                        .size(54.dp)
                                        .background(charId.kurtaColor, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = when (charId) {
                                            CharacterId.ALOO -> "🥔"
                                            CharacterId.BAINGAN -> "🍆"
                                            CharacterId.TAMATAR -> "🍅"
                                        },
                                        fontSize = 28.sp
                                    )
                                }

                                Spacer(modifier = Modifier.width(14.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = charId.displayName,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = charId.hindiTitle,
                                            fontSize = 13.sp,
                                            color = Color(0xFFFB8500),
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                    Text(
                                        text = charId.perk,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    when {
                                        isSelected -> {
                                            Text(
                                                "Active Runner ✓",
                                                color = Color(0xFF2E7D32),
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp
                                            )
                                        }
                                        isUnlocked -> {
                                            Button(
                                                onClick = {
                                                    storage.selectedCharacter = charId
                                                    selectedChar = charId
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFB8500)),
                                                shape = RoundedCornerShape(10.dp),
                                                modifier = Modifier.testTag("select_char_${charId.name}")
                                            ) {
                                                Text("Select")
                                            }
                                        }
                                        else -> {
                                            Button(
                                                onClick = {
                                                    if (coins >= charId.basePrice) {
                                                        storage.coins -= charId.basePrice
                                                        coins = storage.coins
                                                        storage.unlockCharacter(charId)
                                                        storage.selectedCharacter = charId
                                                        selectedChar = charId
                                                    }
                                                },
                                                enabled = coins >= charId.basePrice,
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                                                shape = RoundedCornerShape(10.dp),
                                                modifier = Modifier.testTag("unlock_char_${charId.name}")
                                            ) {
                                                Text("Unlock: ${charId.basePrice} 🪙", fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // Power Upgrades
                    UpgradeCard(
                        icon = "🧲",
                        title = "Nimbu-Mirchi Magnet",
                        description = "Increases coin magnet pull duration by +1.5s per level",
                        key = "magnet",
                        storage = storage,
                        coins = coins,
                        onUpgrade = { coins = storage.coins }
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    UpgradeCard(
                        icon = "🛡️",
                        title = "Garam Masala Shield",
                        description = "Increases shield protection duration by +1.5s per level",
                        key = "shield",
                        storage = storage,
                        coins = coins,
                        onUpgrade = { coins = storage.coins }
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    UpgradeCard(
                        icon = "⚡",
                        title = "2X Desi Ghee Boost",
                        description = "Increases 2X score multiplier duration by +1.5s per level",
                        key = "multiplier",
                        storage = storage,
                        coins = coins,
                        onUpgrade = { coins = storage.coins }
                    )
                }
            }
        }
    }
}

@Composable
private fun UpgradeCard(
    icon: String,
    title: String,
    description: String,
    key: String,
    storage: GameStorage,
    coins: Int,
    onUpgrade: () -> Unit
) {
    var level by remember { mutableIntStateOf(storage.getUpgradeLevel(key)) }
    val maxLevel = 5
    val upgradeCost = level * 100

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(icon, fontSize = 28.sp)
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(title, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text(
                        description,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Star level indicator
                Row {
                    for (i in 1..maxLevel) {
                        Text(
                            text = if (i <= level) "⭐" else "☆",
                            fontSize = 16.sp
                        )
                    }
                    Text(" Lvl $level/$maxLevel", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }

                if (level < maxLevel) {
                    Button(
                        onClick = {
                            if (coins >= upgradeCost) {
                                storage.coins -= upgradeCost
                                storage.setUpgradeLevel(key, level + 1)
                                level = storage.getUpgradeLevel(key)
                                onUpgrade()
                            }
                        },
                        enabled = coins >= upgradeCost,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFB8500)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("upgrade_${key}_button")
                    ) {
                        Text("Upgrade: $upgradeCost 🪙", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Text("MAX LEVEL ✓", color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }
    }
}
