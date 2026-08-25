package com.example.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin

object SoundManager {
    var isMuted: Boolean = false
    private val scope = CoroutineScope(Dispatchers.Default)
    private const val SAMPLE_RATE = 22050

    private fun playTone(freqs: List<Double>, durationsMs: List<Int>, type: String = "sine", volume: Float = 0.5f) {
        if (isMuted) return
        scope.launch {
            try {
                var totalSamples = 0
                for (d in durationsMs) {
                    totalSamples += (SAMPLE_RATE * d / 1000)
                }
                val buffer = ShortArray(totalSamples)
                var currentSample = 0

                for (i in freqs.indices) {
                    val freq = freqs[i]
                    val durationMs = durationsMs.getOrElse(i) { durationsMs.last() }
                    val samples = (SAMPLE_RATE * durationMs / 1000)
                    for (s in 0 until samples) {
                        val t = s.toDouble() / SAMPLE_RATE
                        val fadeFactor = when {
                            s < samples * 0.1 -> s / (samples * 0.1)
                            s > samples * 0.7 -> (samples - s) / (samples * 0.3)
                            else -> 1.0
                        }
                        val sampleVal = when (type) {
                            "square" -> if (sin(2 * PI * freq * t) > 0) 1.0 else -1.0
                            "triangle" -> (2.0 / PI) * Math.asin(sin(2 * PI * freq * t))
                            else -> sin(2 * PI * freq * t)
                        }
                        buffer[currentSample++] = (sampleVal * fadeFactor * volume * Short.MAX_VALUE).toInt().toShort()
                    }
                }

                val audioTrack = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_GAME)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(SAMPLE_RATE)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(buffer.size * 2)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                audioTrack.write(buffer, 0, buffer.size)
                audioTrack.play()
                // Auto release after sound finishes
                scope.launch {
                    val totalDuration = durationsMs.sum() + 50L
                    kotlinx.coroutines.delay(totalDuration)
                    try {
                        audioTrack.stop()
                        audioTrack.release()
                    } catch (_: Exception) {}
                }
            } catch (_: Exception) {
                // Ignore audio init exceptions on test/headless environments
            }
        }
    }

    fun playDiceRoll() {
        // Quick rattling clicking sound
        playTone(
            freqs = listOf(420.0, 310.0, 520.0, 380.0, 480.0, 600.0),
            durationsMs = listOf(35, 35, 35, 35, 35, 45),
            type = "triangle",
            volume = 0.4f
        )
    }

    fun playStep() {
        // Pleasant bubble pop with short duration
        playTone(
            freqs = listOf(580.0, 720.0),
            durationsMs = listOf(20, 35),
            type = "sine",
            volume = 0.45f
        )
    }

    fun playCapture() {
        // Dramatic energetic slide down
        playTone(
            freqs = listOf(650.0, 450.0, 300.0, 180.0),
            durationsMs = listOf(40, 50, 70, 100),
            type = "square",
            volume = 0.5f
        )
    }

    fun playHomeEntry() {
        // Triumph chime
        playTone(
            freqs = listOf(523.25, 659.25, 783.99, 1046.5),
            durationsMs = listOf(60, 60, 80, 150),
            type = "sine",
            volume = 0.6f
        )
    }

    fun playSixRolled() {
        // Cheerful double bell
        playTone(
            freqs = listOf(880.0, 1174.66),
            durationsMs = listOf(70, 120),
            type = "sine",
            volume = 0.55f
        )
    }

    fun playWin() {
        // Victory fanfare
        playTone(
            freqs = listOf(523.25, 659.25, 783.99, 1046.50, 783.99, 1046.50),
            durationsMs = listOf(100, 100, 100, 150, 100, 300),
            type = "triangle",
            volume = 0.65f
        )
    }

    fun playButton() {
        playTone(
            freqs = listOf(440.0),
            durationsMs = listOf(30),
            type = "sine",
            volume = 0.3f
        )
    }
}
