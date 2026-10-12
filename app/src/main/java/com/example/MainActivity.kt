package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.ui.theme.MyApplicationTheme
import com.example.vegetablerun.ads.AdManager
import com.example.vegetablerun.audio.SoundManager
import com.example.vegetablerun.engine.GameEngine
import com.example.vegetablerun.model.CharacterId
import com.example.vegetablerun.storage.GameStorage
import com.example.vegetablerun.ui.GameScreen
import com.example.vegetablerun.ui.MainMenuScreen
import com.example.vegetablerun.ui.MissionsDialog
import com.example.vegetablerun.ui.SettingsDialog
import com.example.vegetablerun.ui.ShopScreen
import com.example.vegetablerun.ui.TutorialDialog

enum class ScreenState {
    MENU,
    GAME,
    SHOP
}

class MainActivity : ComponentActivity() {

    private lateinit var storage: GameStorage
    private lateinit var soundManager: SoundManager
    private lateinit var adManager: AdManager
    private lateinit var gameEngine: GameEngine

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        storage = GameStorage(this)
        soundManager = SoundManager(this).apply {
            isSfxEnabled = storage.isSfxEnabled
            isMusicEnabled = storage.isMusicEnabled
            isHapticEnabled = storage.isHapticEnabled
        }
        adManager = AdManager()
        gameEngine = GameEngine(storage, soundManager)

        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFFFFFBEA)
                ) {
                    VegetableRunApp(
                        storage = storage,
                        soundManager = soundManager,
                        adManager = adManager,
                        gameEngine = gameEngine
                    )
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        soundManager.stopBackgroundMusic()
    }

    override fun onDestroy() {
        super.onDestroy()
        soundManager.release()
    }
}

@Composable
fun VegetableRunApp(
    storage: GameStorage,
    soundManager: SoundManager,
    adManager: AdManager,
    gameEngine: GameEngine
) {
    var currentScreen by remember { mutableStateOf(ScreenState.MENU) }
    var showMissionsDialog by remember { mutableStateOf(false) }
    var showTutorialDialog by remember {
        mutableStateOf(!storage.hasCompletedTutorial)
    }
    var showSettingsDialog by remember { mutableStateOf(false) }

    // Clean back handling
    BackHandler(enabled = true) {
        when {
            showMissionsDialog -> showMissionsDialog = false
            showTutorialDialog -> showTutorialDialog = false
            showSettingsDialog -> showSettingsDialog = false
            currentScreen == ScreenState.SHOP -> currentScreen = ScreenState.MENU
            currentScreen == ScreenState.GAME -> {
                gameEngine.pauseGame()
            }
        }
    }

    when (currentScreen) {
        ScreenState.MENU -> {
            MainMenuScreen(
                storage = storage,
                adManager = adManager,
                onStartGame = { charId ->
                    gameEngine.startRun(charId)
                    currentScreen = ScreenState.GAME
                },
                onOpenShop = { currentScreen = ScreenState.SHOP },
                onOpenMissions = { showMissionsDialog = true },
                onOpenTutorial = { showTutorialDialog = true },
                onOpenSettings = { showSettingsDialog = true }
            )
        }

        ScreenState.GAME -> {
            GameScreen(
                engine = gameEngine,
                storage = storage,
                soundManager = soundManager,
                adManager = adManager,
                onNavigateHome = {
                    currentScreen = ScreenState.MENU
                }
            )
        }

        ScreenState.SHOP -> {
            ShopScreen(
                storage = storage,
                adManager = adManager,
                onBack = { currentScreen = ScreenState.MENU }
            )
        }
    }

    // Modal Dialogs
    if (showTutorialDialog) {
        TutorialDialog(
            onDismiss = {
                storage.hasCompletedTutorial = true
                showTutorialDialog = false
            }
        )
    }

    if (showMissionsDialog) {
        MissionsDialog(
            storage = storage,
            onDismiss = { showMissionsDialog = false },
            onCoinsUpdated = {
                // UI automatically reflects storage coins
            }
        )
    }

    if (showSettingsDialog) {
        SettingsDialog(
            storage = storage,
            soundManager = soundManager,
            onDismiss = { showSettingsDialog = false }
        )
    }
}
