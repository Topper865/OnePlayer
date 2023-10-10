package dev.sajidali.onplayer.core

/**
 * Created by Sajid Ali on 10/10/2023.
 */
data class MediaSource(
    var uri: String? = null,
    var useMediaCodec: Boolean = false,
    var overlayFormat: Int = -1,
    var userAgent: String = "OnePlayer",
    var dropFrames: Boolean = true,
    var bufferSize: Long = 1500,
    var retryOnError: Boolean = true,
    var retryInterval: Long = 5000,
    var isLooping: Boolean = false,
    var volume: Float = 100.0f
)