package dev.sajidali.onplayer.core

import android.view.SurfaceView
import android.view.TextureView
import kotlinx.coroutines.CoroutineScope

/**
 * Created by Sajid Ali on 10/10/2023.
 */
interface MediaPlayer {

    companion object {
        const val INFO_STOPPED = 1000
        const val INFO_BUFFERING_STARTED = 1001
        const val INFO_BUFFERING_COMPLETED = 1002
        const val INFO_PLAYING_STARTED = 1003
        const val INFO_SEEK_COMPLETE = 1004
        const val INFO_PLAYBACK_COMPLETE = 1005
    }


    enum class State {
        IDLE, BUFFERING, PLAYING, PAUSED, STOPPED, RELEASED, SEEKING, COMPLETED
    }

    fun onInfo(block: Info.() -> Unit)
    fun onBufferingUpdate(block: (progress: Int) -> Unit)
    fun onVideoSizeChanged(block: VideoSize.() -> Unit)
    fun onStateChanged(block: State.() -> Unit)
    fun onError(block: Info.() -> Unit)
    fun onSubtitleTextUpdated(block: (subtitle: String) -> Unit)

    val videoWidth: Int
    val videoHeight: Int

    var mediaSource: MediaSource?

    val audioTracks: List<MediaTrack>
    val subtitleTracks: List<MediaTrack>

    /**
     * Selected audio track id
     */
    var selectedAudioTrack: String?

    /**
     * Selected subtitle track id
     */
    var selectedSubtitleTrack: String?

    /**
     * Current position of the player
     */
    val position: Long

    /**
     * Duration of the media
     */
    val duration: Long

    /**
     * Is player looping
     */
    var isLooping: Boolean

    /**
     * Volume of the player
     */
    var volume: Float

    /**
     * Current state of the player
     */
    val playerState: State

    /**
     * Is player playing
     */
    val isPlaying: Boolean

    fun play(initialSeek: Long = 0)
    fun pause()
    fun stop()

    /**
     * Seek to an exact position
     * @param msec Position to seek
     */
    fun seekTo(msec: Long)

    /**
     * Seek to a relative position
     * @param msec Duration to add in current position
     */
    fun seek(msec: Long)

    fun resume()
    fun release(targetState: State = State.RELEASED)
    fun setVideoView(surfaceView: SurfaceView?)
    fun setVideoView(textureView: TextureView?)
    fun setVideoView(videoView: VideoView?)


    data class Info(val what: Int, val extra: Int)
    data class VideoSize(val width: Int, val height: Int, val ratio: Float)

}