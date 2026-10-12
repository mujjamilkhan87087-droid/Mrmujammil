package com.example.vegetablerun.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.sin

class SoundManager(private val context: Context) {
    private val scope = CoroutineScope(Dispatchers.Default)
    private var musicJob: Job? = null

    var isSfxEnabled: Boolean = true
    var isMusicEnabled: Boolean = true
        set(value) {
            field = value
            if (value) startBackgroundMusic() else stopBackgroundMusic()
        }

    var isHapticEnabled: Boolean = true

    private val vibrator: Vibrator? by lazy {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vm?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
        } catch (_: Exception) {
            null
        }
    }

    // Play synthesized tone buffer
    private fun playPcm(samples: ShortArray, sampleRate: Int = 22050) {
        if (!isSfxEnabled) return
        scope.launch {
            var track: AudioTrack? = null
            try {
                val bufferSize = samples.size * 2
                track = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .setUsage(AudioAttributes.USAGE_GAME)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(bufferSize)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                track.write(samples, 0, samples.size)
                track.play()
                // Wait for playback to finish
                val durationMs = (samples.size * 1000L) / sampleRate
                delay(durationMs + 30)
            } catch (_: Exception) {
                // Safely ignored
            } finally {
                try {
                    track?.stop()
                    track?.release()
                } catch (_: Exception) {}
            }
        }
    }

    fun playJump() {
        if (!isSfxEnabled) return
        val sampleRate = 22050
        val durationSeconds = 0.14f
        val numSamples = (sampleRate * durationSeconds).toInt()
        val samples = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val progress = i.toFloat() / numSamples
            val freq = 300f + progress * 400f
            val envelope = 1f - progress
            val angle = 2.0 * Math.PI * freq * i / sampleRate
            samples[i] = (sin(angle) * Short.MAX_VALUE * 0.4f * envelope).toInt().toShort()
        }
        playPcm(samples, sampleRate)
        vibrate(15)
    }

    fun playSlide() {
        if (!isSfxEnabled) return
        val sampleRate = 22050
        val durationSeconds = 0.16f
        val numSamples = (sampleRate * durationSeconds).toInt()
        val samples = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val progress = i.toFloat() / numSamples
            val freq = 450f - progress * 220f
            val envelope = (1f - progress) * 0.35f
            val angle = 2.0 * Math.PI * freq * i / sampleRate
            samples[i] = (sin(angle) * Short.MAX_VALUE * envelope).toInt().toShort()
        }
        playPcm(samples, sampleRate)
        vibrate(15)
    }

    fun playCoin() {
        if (!isSfxEnabled) return
        val sampleRate = 22050
        val durationSeconds = 0.10f
        val numSamples = (sampleRate * durationSeconds).toInt()
        val samples = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val progress = i.toFloat() / numSamples
            val freq = if (progress < 0.5f) 987f else 1318f // B5 -> E6
            val envelope = 1f - (progress * progress)
            val angle = 2.0 * Math.PI * freq * i / sampleRate
            samples[i] = (sin(angle) * Short.MAX_VALUE * 0.35f * envelope).toInt().toShort()
        }
        playPcm(samples, sampleRate)
    }

    fun playPowerUp() {
        if (!isSfxEnabled) return
        val sampleRate = 22050
        val durationSeconds = 0.28f
        val numSamples = (sampleRate * durationSeconds).toInt()
        val samples = ShortArray(numSamples)
        val freqs = floatArrayOf(523f, 659f, 784f, 1046f) // C5, E5, G5, C6
        for (i in 0 until numSamples) {
            val seg = (i * 4 / numSamples).coerceIn(0, 3)
            val progress = i.toFloat() / numSamples
            val envelope = 1f - progress * 0.4f
            val angle = 2.0 * Math.PI * freqs[seg] * i / sampleRate
            samples[i] = (sin(angle) * Short.MAX_VALUE * 0.4f * envelope).toInt().toShort()
        }
        playPcm(samples, sampleRate)
        vibrate(40)
    }

    fun playCrash() {
        if (!isSfxEnabled) return
        val sampleRate = 22050
        val durationSeconds = 0.30f
        val numSamples = (sampleRate * durationSeconds).toInt()
        val samples = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val progress = i.toFloat() / numSamples
            val freq = 160f - progress * 100f
            val envelope = (1f - progress) * (1f - progress)
            // Add a little noise for crunch impact
            val noise = (Math.random() * 2.0 - 1.0).toFloat() * 0.4f
            val wave = sin(2.0 * Math.PI * freq * i / sampleRate).toFloat() * 0.6f
            samples[i] = ((wave + noise) * Short.MAX_VALUE * 0.55f * envelope).toInt().toShort()
        }
        playPcm(samples, sampleRate)
        vibrate(80)
    }

    fun playLaneChange() {
        vibrate(10)
    }

    private fun vibrate(durationMs: Long) {
        if (!isHapticEnabled) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(durationMs)
            }
        } catch (_: Exception) {}
    }

    // Upbeat Desi Folk Chiptune Melodic Loop
    fun startBackgroundMusic() {
        if (!isMusicEnabled || musicJob?.isActive == true) return
        musicJob = scope.launch {
            // Pentatonic upbeat notes (Desi Raag Bhoopali / Major Pentatonic):
            // C4 (261.6), D4 (293.7), E4 (329.6), G4 (392.0), A4 (440.0), C5 (523.3)
            val melody = intArrayOf(
                262, 294, 330, 392, 440, 392, 330, 294,
                330, 392, 440, 523, 440, 392, 330, 262
            )
            val beatBass = intArrayOf(131, 196, 131, 196) // Bass drum beat
            val tempoMs = 135L

            var step = 0
            while (isActive && isMusicEnabled) {
                val freqMelody = melody[step % melody.size]
                val freqBass = beatBass[(step / 2) % beatBass.size]
                playShortNote(freqMelody, freqBass, tempoMs.toInt())
                delay(tempoMs)
                step++
            }
        }
    }

    private fun playShortNote(freq: Int, bassFreq: Int, durationMs: Int) {
        if (!isMusicEnabled) return
        val sampleRate = 16000
        val numSamples = (sampleRate * (durationMs / 1000f)).toInt()
        val samples = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val progress = i.toFloat() / numSamples
            val env = 1f - progress * 0.7f
            val melodyWave = sin(2.0 * Math.PI * freq * i / sampleRate) * 0.12
            val bassWave = sin(2.0 * Math.PI * bassFreq * i / sampleRate) * 0.18
            samples[i] = ((melodyWave + bassWave) * Short.MAX_VALUE * env).toInt().toShort()
        }
        playPcm(samples, sampleRate)
    }

    fun stopBackgroundMusic() {
        musicJob?.cancel()
        musicJob = null
    }

    fun release() {
        stopBackgroundMusic()
    }
}
