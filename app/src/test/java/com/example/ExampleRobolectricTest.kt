package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.vegetablerun.audio.SoundManager
import com.example.vegetablerun.engine.GameEngine
import com.example.vegetablerun.engine.GameState
import com.example.vegetablerun.model.CharacterId
import com.example.vegetablerun.storage.GameStorage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun testAppName() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Vegetable Run", appName)
    }

    @Test
    fun testGameStoragePersistence() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val storage = GameStorage(context)

        // Initial default state
        assertEquals(50, storage.coins)
        assertEquals(0, storage.highScore)
        assertEquals(CharacterId.ALOO, storage.selectedCharacter)
        assertTrue(storage.isCharacterUnlocked(CharacterId.ALOO))
        assertFalse(storage.isCharacterUnlocked(CharacterId.BAINGAN))

        // Modify and check
        storage.coins += 250
        assertEquals(300, storage.coins)

        storage.unlockCharacter(CharacterId.BAINGAN)
        assertTrue(storage.isCharacterUnlocked(CharacterId.BAINGAN))

        storage.highScore = 1200
        assertEquals(1200, storage.highScore)
    }

    @Test
    fun testGameEngineControls() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val storage = GameStorage(context)
        val soundManager = SoundManager(context).apply {
            isSfxEnabled = false
            isMusicEnabled = false
        }
        val engine = GameEngine(storage, soundManager)

        engine.startRun(CharacterId.ALOO)
        assertEquals(GameState.RUNNING, engine.snapshot.value.state)
        assertEquals(0, engine.snapshot.value.targetLane)

        // Move Left
        engine.moveLeft()
        assertEquals(-1, engine.snapshot.value.targetLane)

        // Cannot move past left border
        engine.moveLeft()
        assertEquals(-1, engine.snapshot.value.targetLane)

        // Move Right twice
        engine.moveRight()
        assertEquals(0, engine.snapshot.value.targetLane)
        engine.moveRight()
        assertEquals(1, engine.snapshot.value.targetLane)

        // Cannot move past right border
        engine.moveRight()
        assertEquals(1, engine.snapshot.value.targetLane)

        // Jump & Slide
        engine.jump()
        engine.slide()
        assertTrue(engine.snapshot.value.isSliding)

        // Pause & Resume
        engine.pauseGame()
        assertEquals(GameState.PAUSED, engine.snapshot.value.state)
        engine.resumeGame()
        assertEquals(GameState.RUNNING, engine.snapshot.value.state)
    }
}
