package com.example.audio

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
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin

class AudioFeedbackEngine(
    private val context: Context
) {
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    var isSoundEnabled: Boolean = true
    var isHapticsEnabled: Boolean = true

    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        manager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    fun playMarkNumber() {
        if (isSoundEnabled) {
            scope.launch {
                // Short bright chime
                val samples = generateToneSequence(
                    listOf(
                        Tone(frequency = 784.0, durationMs = 45, volume = 0.6f),
                        Tone(frequency = 1046.5, durationMs = 70, volume = 0.8f)
                    )
                )
                playPcmSamples(samples)
            }
        }
        performHaptic(40, 120)
    }

    fun playLineStrike() {
        if (isSoundEnabled) {
            scope.launch {
                // Ascending harmonic triad (C-E-G-C)
                val samples = generateToneSequence(
                    listOf(
                        Tone(frequency = 523.25, durationMs = 70, volume = 0.7f),
                        Tone(frequency = 659.25, durationMs = 70, volume = 0.75f),
                        Tone(frequency = 783.99, durationMs = 70, volume = 0.8f),
                        Tone(frequency = 1046.50, durationMs = 180, volume = 0.9f)
                    )
                )
                playPcmSamples(samples)
            }
        }
        performHaptic(100, 200)
    }

    fun playLetterUnlock() {
        if (isSoundEnabled) {
            scope.launch {
                // Bouncy energetic pop (440Hz -> 880Hz)
                val samples = generateToneSequence(
                    listOf(
                        Tone(frequency = 440.0, durationMs = 40, volume = 0.6f),
                        Tone(frequency = 880.0, durationMs = 90, volume = 0.85f)
                    )
                )
                playPcmSamples(samples)
            }
        }
        performHaptic(60, 160)
    }

    fun playYourTurn() {
        if (isSoundEnabled) {
            scope.launch {
                // Subtle attention chime
                val samples = generateToneSequence(
                    listOf(
                        Tone(frequency = 587.33, durationMs = 60, volume = 0.5f),
                        Tone(frequency = 880.00, durationMs = 120, volume = 0.7f)
                    )
                )
                playPcmSamples(samples)
            }
        }
        performHaptic(30, 80)
    }

    fun playVictory() {
        if (isSoundEnabled) {
            scope.launch {
                // Triumphant Fanfare
                val samples = generateToneSequence(
                    listOf(
                        Tone(frequency = 523.25, durationMs = 100, volume = 0.7f),
                        Tone(frequency = 523.25, durationMs = 100, volume = 0.7f),
                        Tone(frequency = 523.25, durationMs = 100, volume = 0.7f),
                        Tone(frequency = 659.25, durationMs = 250, volume = 0.85f),
                        Tone(frequency = 587.33, durationMs = 100, volume = 0.7f),
                        Tone(frequency = 659.25, durationMs = 100, volume = 0.8f),
                        Tone(frequency = 783.99, durationMs = 120, volume = 0.85f),
                        Tone(frequency = 1046.50, durationMs = 400, volume = 1.0f)
                    )
                )
                playPcmSamples(samples)
            }
        }
        performHaptic(250, 255)
    }

    private data class Tone(val frequency: Double, val durationMs: Int, val volume: Float = 0.8f)

    private fun generateToneSequence(tones: List<Tone>, sampleRate: Int = 22050): ShortArray {
        val totalSamples = tones.sumOf { (it.durationMs * sampleRate) / 1000 }
        val output = ShortArray(totalSamples)
        var offset = 0

        for (tone in tones) {
            val numSamples = (tone.durationMs * sampleRate) / 1000
            for (i in 0 until numSamples) {
                val t = i.toDouble() / sampleRate
                val rawSine = sin(2.0 * PI * tone.frequency * t)
                // Linear decay envelope to prevent audio clicking
                val envelope = when {
                    i < 100 -> i / 100f
                    i > numSamples - 200 -> (numSamples - i) / 200f
                    else -> 1f
                }
                val sampleValue = (rawSine * Short.MAX_VALUE * tone.volume * envelope).toInt()
                output[offset + i] = sampleValue.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
            }
            offset += numSamples
        }
        return output
    }

    private fun playPcmSamples(samples: ShortArray, sampleRate: Int = 22050) {
        try {
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            val audioFormat = AudioFormat.Builder()
                .setSampleRate(sampleRate)
                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                .build()

            val bufferSize = samples.size * 2
            val track = AudioTrack(
                audioAttributes,
                audioFormat,
                bufferSize,
                AudioTrack.MODE_STATIC,
                0
            )

            track.write(samples, 0, samples.size)
            track.play()
            track.setNotificationMarkerPosition(samples.size)
            track.setPlaybackPositionUpdateListener(object : AudioTrack.OnPlaybackPositionUpdateListener {
                override fun onMarkerReached(t: AudioTrack?) {
                    try {
                        t?.stop()
                        t?.release()
                    } catch (_: Exception) {}
                }
                override fun onPeriodicNotification(track: AudioTrack?) {}
            })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun performHaptic(durationMs: Long, amplitude: Int = VibrationEffect.DEFAULT_AMPLITUDE) {
        if (!isHapticsEnabled) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(
                    VibrationEffect.createOneShot(
                        durationMs,
                        amplitude.coerceIn(1, 255)
                    )
                )
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(durationMs)
            }
        } catch (_: Exception) {}
    }
}
