package com.example.vegetablerun.ads

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * AdManager handles AdMob advertisement integration points for Vegetable Run.
 *
 * Official Google AdMob Test Ad Unit IDs for Android:
 * - App ID: ca-app-pub-3940256099942544~3347511713
 * - Adaptive Banner: ca-app-pub-3940256099942544/6300978111
 * - Interstitial: ca-app-pub-3940256099942544/1033173712
 * - Rewarded Video: ca-app-pub-3940256099942544/5224354917
 *
 * During development, test ads are simulated safely without crashing on devices
 * that lack Google Play Services. To connect the live Google Mobile Ads SDK:
 * 1. Add `implementation("com.google.android.gms:play-services-ads:23.6.0")` to build.gradle.kts
 * 2. Add `<meta-data android:name="com.google.android.gms.ads.APPLICATION_ID" android:value="YOUR_ADMOB_APP_ID"/>` to AndroidManifest.xml
 * 3. Replace TEST_AD_UNIT_IDs with your real IDs in production.
 */
class AdManager {

    companion object {
        const val TEST_APP_ID = "ca-app-pub-3940256099942544~3347511713"
        const val TEST_BANNER_ID = "ca-app-pub-3940256099942544/6300978111"
        const val TEST_INTERSTITIAL_ID = "ca-app-pub-3940256099942544/1033173712"
        const val TEST_REWARDED_ID = "ca-app-pub-3940256099942544/5224354917"
    }

    private val _isShowingRewardedAd = MutableStateFlow(false)
    val isShowingRewardedAd: StateFlow<Boolean> = _isShowingRewardedAd.asStateFlow()

    private val _isShowingInterstitialAd = MutableStateFlow(false)
    val isShowingInterstitialAd: StateFlow<Boolean> = _isShowingInterstitialAd.asStateFlow()

    private var onRewardEarnedCallback: (() -> Unit)? = null
    private var onInterstitialClosedCallback: (() -> Unit)? = null

    // Run count tracker to trigger interstitial every 3 games
    private var runsCompletedSinceAd = 0

    fun showRewardedAd(onRewardEarned: () -> Unit) {
        onRewardEarnedCallback = onRewardEarned
        _isShowingRewardedAd.value = true
    }

    fun completeRewardedAd(earned: Boolean) {
        _isShowingRewardedAd.value = false
        if (earned) {
            onRewardEarnedCallback?.invoke()
        }
        onRewardEarnedCallback = null
    }

    fun onGameFinished(onComplete: () -> Unit) {
        runsCompletedSinceAd++
        if (runsCompletedSinceAd >= 3) {
            runsCompletedSinceAd = 0
            onInterstitialClosedCallback = onComplete
            _isShowingInterstitialAd.value = true
        } else {
            onComplete()
        }
    }

    fun closeInterstitialAd() {
        _isShowingInterstitialAd.value = false
        onInterstitialClosedCallback?.invoke()
        onInterstitialClosedCallback = null
    }
}
