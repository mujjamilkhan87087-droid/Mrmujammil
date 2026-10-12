package com.example.vegetablerun.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.vegetablerun.audio.SoundManager
import com.example.vegetablerun.storage.GameStorage

@Composable
fun SettingsDialog(
    storage: GameStorage,
    soundManager: SoundManager,
    onDismiss: () -> Unit
) {
    var sfx by remember { mutableStateOf(storage.isSfxEnabled) }
    var music by remember { mutableStateOf(storage.isMusicEnabled) }
    var touchButtons by remember { mutableStateOf(storage.isTouchButtonsEnabled) }
    var haptic by remember { mutableStateOf(storage.isHapticEnabled) }

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
                        text = "⚙️ Game Settings",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFE65100)
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_settings_button")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close Settings")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                SettingToggleRow(
                    icon = Icons.AutoMirrored.Filled.VolumeUp,
                    title = "Sound Effects (SFX)",
                    subtitle = "Jumps, slides, coin dings and crashes",
                    checked = sfx,
                    onCheckedChange = {
                        sfx = it
                        storage.isSfxEnabled = it
                        soundManager.isSfxEnabled = it
                    }
                )

                Spacer(modifier = Modifier.height(10.dp))

                SettingToggleRow(
                    icon = Icons.AutoMirrored.Filled.VolumeUp,
                    title = "Desi Folk Music",
                    subtitle = "Catchy upbeat background melody",
                    checked = music,
                    onCheckedChange = {
                        music = it
                        storage.isMusicEnabled = it
                        soundManager.isMusicEnabled = it
                    }
                )

                Spacer(modifier = Modifier.height(10.dp))

                SettingToggleRow(
                    icon = Icons.Default.TouchApp,
                    title = "On-Screen Touch Buttons",
                    subtitle = "Tactile buttons for left, right, jump, slide",
                    checked = touchButtons,
                    onCheckedChange = {
                        touchButtons = it
                        storage.isTouchButtonsEnabled = it
                    }
                )

                Spacer(modifier = Modifier.height(10.dp))

                SettingToggleRow(
                    icon = Icons.Default.Vibration,
                    title = "Haptic Vibration",
                    subtitle = "Gentle vibration upon action and impacts",
                    checked = haptic,
                    onCheckedChange = {
                        haptic = it
                        storage.isHapticEnabled = it
                        soundManager.isHapticEnabled = it
                    }
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Mobile Optimization Note (Vivo Y16 / Low-end device)
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.PhoneAndroid,
                            contentDescription = "Device info",
                            tint = Color(0xFF2E7D32)
                        )
                        Column(modifier = Modifier.padding(start = 10.dp)) {
                            Text(
                                text = "Device Performance: Smooth 60 FPS",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1B5E20),
                                fontSize = 13.sp
                            )
                            Text(
                                text = "Optimized lightweight canvas engine for Vivo Y16 and all Android devices.",
                                color = Color(0xFF2E7D32),
                                fontSize = 11.sp
                            )
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
                        .testTag("save_settings_button")
                ) {
                    Text("Save & Return", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    }
}

@Composable
private fun SettingToggleRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Black.copy(alpha = 0.03f), RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = Color(0xFFE65100),
                modifier = Modifier.padding(end = 10.dp)
            )
            Column {
                Text(text = title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color(0xFFFB8500),
                checkedTrackColor = Color(0xFFFFCC80)
            )
        )
    }
}
