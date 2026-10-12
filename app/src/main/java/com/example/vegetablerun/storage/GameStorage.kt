package com.example.vegetablerun.storage

import android.content.Context
import android.content.SharedPreferences
import com.example.vegetablerun.model.CharacterId
import com.example.vegetablerun.model.Mission

class GameStorage(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("vegetable_run_prefs", Context.MODE_PRIVATE)

    var coins: Int
        get() = prefs.getInt(KEY_COINS, 50) // 50 welcome coins!
        set(value) = prefs.edit().putInt(KEY_COINS, value.coerceAtLeast(0)).apply()

    var highScore: Int
        get() = prefs.getInt(KEY_HIGH_SCORE, 0)
        set(value) = prefs.edit().putInt(KEY_HIGH_SCORE, maxOf(highScore, value)).apply()

    var bestDistanceMeters: Int
        get() = prefs.getInt(KEY_BEST_DISTANCE, 0)
        set(value) = prefs.edit().putInt(KEY_BEST_DISTANCE, maxOf(bestDistanceMeters, value)).apply()

    var selectedCharacter: CharacterId
        get() = CharacterId.fromId(prefs.getString(KEY_SELECTED_CHARACTER, CharacterId.ALOO.name))
        set(value) = prefs.edit().putString(KEY_SELECTED_CHARACTER, value.name).apply()

    fun isCharacterUnlocked(characterId: CharacterId): Boolean {
        if (characterId == CharacterId.ALOO) return true
        val unlocked = prefs.getStringSet(KEY_UNLOCKED_CHARACTERS, emptySet()) ?: emptySet()
        return unlocked.contains(characterId.name)
    }

    fun unlockCharacter(characterId: CharacterId) {
        val current = (prefs.getStringSet(KEY_UNLOCKED_CHARACTERS, emptySet()) ?: emptySet()).toMutableSet()
        current.add(characterId.name)
        prefs.edit().putStringSet(KEY_UNLOCKED_CHARACTERS, current).apply()
    }

    var isSfxEnabled: Boolean
        get() = prefs.getBoolean(KEY_SFX_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_SFX_ENABLED, value).apply()

    var isMusicEnabled: Boolean
        get() = prefs.getBoolean(KEY_MUSIC_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_MUSIC_ENABLED, value).apply()

    var isTouchButtonsEnabled: Boolean
        get() = prefs.getBoolean(KEY_TOUCH_BUTTONS, true) // Enabled by default for easy low-end phone thumb gameplay
        set(value) = prefs.edit().putBoolean(KEY_TOUCH_BUTTONS, value).apply()

    var isHapticEnabled: Boolean
        get() = prefs.getBoolean(KEY_HAPTIC, true)
        set(value) = prefs.edit().putBoolean(KEY_HAPTIC, value).apply()

    var hasCompletedTutorial: Boolean
        get() = prefs.getBoolean(KEY_TUTORIAL_COMPLETED, false)
        set(value) = prefs.edit().putBoolean(KEY_TUTORIAL_COMPLETED, value).apply()

    // Upgrades: 1 to 5
    fun getUpgradeLevel(key: String): Int = prefs.getInt("upgrade_$key", 1)
    fun setUpgradeLevel(key: String, level: Int) = prefs.edit().putInt("upgrade_$key", level).apply()

    // Daily streak logic
    fun checkDailyRewardAvailable(): Pair<Boolean, Int> {
        val lastClaimTime = prefs.getLong(KEY_LAST_CLAIM_TIME, 0L)
        val currentTime = System.currentTimeMillis()
        val streak = prefs.getInt(KEY_DAILY_STREAK, 1)

        val oneDayMillis = 24 * 60 * 60 * 1000L
        val diff = currentTime - lastClaimTime

        if (lastClaimTime == 0L || diff >= oneDayMillis) {
            val newStreak = if (diff >= 2 * oneDayMillis && lastClaimTime != 0L) 1 else streak
            return Pair(true, newStreak)
        }
        return Pair(false, streak)
    }

    fun claimDailyReward(): Int {
        val (available, streak) = checkDailyRewardAvailable()
        if (!available) return 0
        val rewardAmount = 50 + (streak - 1) * 25 // 50, 75, 100, 125, etc.
        coins += rewardAmount
        val nextStreak = if (streak >= 7) 1 else streak + 1
        prefs.edit()
            .putLong(KEY_LAST_CLAIM_TIME, System.currentTimeMillis())
            .putInt(KEY_DAILY_STREAK, nextStreak)
            .apply()
        return rewardAmount
    }

    // Missions
    fun getMissions(): List<Mission> {
        val m1Cur = prefs.getInt("mission_coins_collected", 0)
        val m1Claimed = prefs.getBoolean("mission_coins_claimed", false)

        val m2Cur = prefs.getInt("mission_slide_count", 0)
        val m2Claimed = prefs.getBoolean("mission_slide_claimed", false)

        val m3Cur = prefs.getInt("mission_dist_run", 0)
        val m3Claimed = prefs.getBoolean("mission_dist_claimed", false)

        return listOf(
            Mission(
                id = "coins_40",
                title = "Coin Collector",
                description = "Collect 40 coins in games",
                target = 40,
                current = m1Cur,
                rewardCoins = 100,
                isClaimed = m1Claimed
            ),
            Mission(
                id = "slide_5",
                title = "Smooth Slider",
                description = "Slide under obstacles 5 times",
                target = 5,
                current = m2Cur,
                rewardCoins = 120,
                isClaimed = m2Claimed
            ),
            Mission(
                id = "dist_500",
                title = "Village Champion",
                description = "Reach 500 meters in a run",
                target = 500,
                current = m3Cur,
                rewardCoins = 150,
                isClaimed = m3Claimed
            )
        )
    }

    fun recordRunStats(runCoins: Int, runSlides: Int, runDistance: Int) {
        val m1Cur = prefs.getInt("mission_coins_collected", 0) + runCoins
        val m2Cur = prefs.getInt("mission_slide_count", 0) + runSlides
        val m3Cur = maxOf(prefs.getInt("mission_dist_run", 0), runDistance)
        prefs.edit()
            .putInt("mission_coins_collected", m1Cur)
            .putInt("mission_slide_count", m2Cur)
            .putInt("mission_dist_run", m3Cur)
            .apply()
    }

    fun claimMissionReward(missionId: String): Boolean {
        return when (missionId) {
            "coins_40" -> {
                if (prefs.getInt("mission_coins_collected", 0) >= 40 && !prefs.getBoolean("mission_coins_claimed", false)) {
                    coins += 100
                    prefs.edit().putBoolean("mission_coins_claimed", true).apply()
                    true
                } else false
            }
            "slide_5" -> {
                if (prefs.getInt("mission_slide_count", 0) >= 5 && !prefs.getBoolean("mission_slide_claimed", false)) {
                    coins += 120
                    prefs.edit().putBoolean("mission_slide_claimed", true).apply()
                    true
                } else false
            }
            "dist_500" -> {
                if (prefs.getInt("mission_dist_run", 0) >= 500 && !prefs.getBoolean("mission_dist_claimed", false)) {
                    coins += 150
                    prefs.edit().putBoolean("mission_dist_claimed", true).apply()
                    true
                } else false
            }
            else -> false
        }
    }

    companion object {
        private const val KEY_COINS = "player_coins"
        private const val KEY_HIGH_SCORE = "high_score"
        private const val KEY_BEST_DISTANCE = "best_distance"
        private const val KEY_SELECTED_CHARACTER = "selected_character"
        private const val KEY_UNLOCKED_CHARACTERS = "unlocked_characters"
        private const val KEY_SFX_ENABLED = "sfx_enabled"
        private const val KEY_MUSIC_ENABLED = "music_enabled"
        private const val KEY_TOUCH_BUTTONS = "touch_buttons_enabled"
        private const val KEY_HAPTIC = "haptic_enabled"
        private const val KEY_TUTORIAL_COMPLETED = "tutorial_completed"
        private const val KEY_LAST_CLAIM_TIME = "last_daily_claim_time"
        private const val KEY_DAILY_STREAK = "daily_login_streak"
    }
}
