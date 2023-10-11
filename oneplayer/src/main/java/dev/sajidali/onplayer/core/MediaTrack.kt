package dev.sajidali.onplayer.core

/**
 * Created by Sajid Ali on 10/10/2023.
 */

class MediaTrack(
    val id: String,
    val name: String,
    val type: TrackType,
    val isSelected: Boolean = false
) {

    enum class TrackType {
        AUDIO,
        VIDEO,
        SUBTITLE
    }
}