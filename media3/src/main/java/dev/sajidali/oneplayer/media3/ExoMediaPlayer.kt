package dev.sajidali.oneplayer.media3

import android.content.Context
import android.util.Log
import android.view.SurfaceView
import android.view.TextureView
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Metadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.TrackSelectionOverride
import androidx.media3.common.VideoSize
import androidx.media3.common.text.Cue
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.decoder.ffmpeg.FfmpegLibrary
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.SeekParameters
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import dev.sajidali.onplayer.core.MediaPlayer
import dev.sajidali.onplayer.core.MediaSource
import dev.sajidali.onplayer.core.MediaTrack
import dev.sajidali.onplayer.core.VideoView
import kotlinx.coroutines.CoroutineScope
import kotlin.concurrent.thread

@UnstableApi
class ExoMediaPlayer(
    private val context: Context, override var scope: CoroutineScope?
) : MediaPlayer, Player.Listener {

    private var mediaPlayer: ExoPlayer = getPlayer()

    private var surfaceView: SurfaceView? = null
    private var textureView: TextureView? = null
    private var videoView: VideoView? = null
    private var seekDuration: Long = 0
    override var isLooping: Boolean = false
    private var retryThread: Thread? = null
    private var isReleased = false

    init {
        Log.d("ExoMediaPlayer", "FFMPeg available " + FfmpegLibrary.isAvailable())
    }

    private fun getPlayer() = ExoPlayer.Builder(
        context,
        DefaultRenderersFactory(context)
            .setEnableDecoderFallback(true)
            .setExtensionRendererMode(
                if (FfmpegLibrary.isAvailable()) DefaultRenderersFactory.EXTENSION_RENDERER_MODE_PREFER
                else DefaultRenderersFactory.EXTENSION_RENDERER_MODE_OFF
            )
    ).setSeekParameters(SeekParameters.EXACT).build().also {
        isReleased = false
        it.setForegroundMode(true)
        it.addListener(this)
    }

    override fun onInfo(block: MediaPlayer.Info.() -> Unit) {
        onInfo = block
    }

    override fun onBufferingUpdate(block: (progress: Int) -> Unit) {
        onBufferingUpdate = block
    }

    override fun onVideoSizeChanged(block: MediaPlayer.VideoSize.() -> Unit) {
        onVideoSizeChanged = block
    }

    override fun onStateChanged(block: MediaPlayer.State.() -> Unit) {
        onStateChanged = block
    }

    override fun onError(block: MediaPlayer.Info.() -> Unit) {
        onError = block
    }

    override fun onSubtitleTextUpdated(block: (subtitle: String) -> Unit) {
        onSubtitleTextUpdated = block
    }

    override var videoWidth: Int = 0
    override var videoHeight: Int = 0
    override var mediaSource: MediaSource? = null

    private var onInfo: MediaPlayer.Info.() -> Unit = {}
    private var onBufferingUpdate: (progress: Int) -> Unit = {}
    private var onVideoSizeChanged: MediaPlayer.VideoSize.() -> Unit = {}
    private var onStateChanged: MediaPlayer.State.() -> Unit = {}
    private var onError: MediaPlayer.Info.() -> Unit = {}
    private var onSubtitleTextUpdated: (subtitle: String) -> Unit = {}

    override val audioTracks: List<MediaTrack>
        get() {
            return buildList {
                mediaPlayer.currentTracks.groups.filter { it.mediaTrackGroup.type == C.TRACK_TYPE_AUDIO }
                    .forEach {
                        add(
                            MediaTrack(
                                it.mediaTrackGroup.id,
                                it.mediaTrackGroup.getFormat(0).language ?: "",
                                MediaTrack.TrackType.AUDIO,
                                it.isSelected
                            )
                        )

                    }
            }
        }
    override val subtitleTracks: List<MediaTrack>
        get() {
            return buildList {
                mediaPlayer.currentTracks.groups.filter { it.mediaTrackGroup.type == C.TRACK_TYPE_TEXT }
                    .forEach {
                        add(
                            MediaTrack(
                                it.mediaTrackGroup.id,
                                it.mediaTrackGroup.getFormat(0).language ?: "",
                                MediaTrack.TrackType.SUBTITLE,
                                it.isSelected
                            )
                        )
                    }
            }
        }
    override var selectedAudioTrack: String?
        get() {
            return mediaPlayer.currentTracks.groups
                .filter { it.type == C.TRACK_TYPE_AUDIO }
                .find { it.isSelected }
                ?.mediaTrackGroup
                ?.id
        }
        set(value) {
            if (value == null) {
                mediaPlayer.trackSelectionParameters = mediaPlayer.trackSelectionParameters
                    .buildUpon()
                    .setTrackTypeDisabled(C.TRACK_TYPE_AUDIO, true)
                    .build()
                return
            }
            mediaPlayer.currentTracks
                .groups.filter { it.mediaTrackGroup.type == C.TRACK_TYPE_AUDIO }
                .find { it.mediaTrackGroup.id == value }
                ?.let {
                    mediaPlayer.trackSelectionParameters = mediaPlayer.trackSelectionParameters
                        .buildUpon()
                        .setTrackTypeDisabled(C.TRACK_TYPE_AUDIO, false)
                        .setOverrideForType(TrackSelectionOverride(it.mediaTrackGroup, 0))
                        .build()
                }
        }
    override var selectedSubtitleTrack: String?
        get() = mediaPlayer.currentTracks.groups
            .filter { it.type == C.TRACK_TYPE_TEXT }
            .find { it.isSelected }
            ?.mediaTrackGroup
            ?.id
        set(value) {
            if (value == null) {
                mediaPlayer.trackSelectionParameters = mediaPlayer.trackSelectionParameters
                    .buildUpon()
                    .setTrackTypeDisabled(C.TRACK_TYPE_TEXT, true)
                    .build()
                return
            }
            mediaPlayer.currentTracks
                .groups.filter { it.mediaTrackGroup.type == C.TRACK_TYPE_TEXT }
                .find { it.mediaTrackGroup.id == value }
                ?.let {
                    mediaPlayer.trackSelectionParameters = mediaPlayer.trackSelectionParameters
                        .buildUpon()
                        .setTrackTypeDisabled(C.TRACK_TYPE_TEXT, false)
                        .setOverrideForType(TrackSelectionOverride(it.mediaTrackGroup, 0))
                        .build()
                }
        }


    override val position: Long
        get() {
            if (isReleased) return 0
            return if (seekDuration > 0) seekDuration else mediaPlayer.currentPosition
        }
    override val duration: Long
        get() {
            if (isReleased) return 0
            return mediaPlayer.duration
        }
    override var volume: Float
        get() = mediaPlayer.volume
        set(value) {
            if (isReleased) return
            mediaPlayer.volume = value
        }
    override val playerState: MediaPlayer.State
        get() = MediaPlayer.State.IDLE
    override val isPlaying: Boolean
        get() = mediaPlayer.isPlaying

    private fun setInternalVideoView() {
        mediaPlayer.clearVideoSurface()
        if (surfaceView != null) {
            mediaPlayer.setVideoSurfaceView(surfaceView)
        } else if (textureView != null) {
            mediaPlayer.setVideoTextureView(textureView)
        } else if (videoView != null) {
            if (videoView?.surface is SurfaceView) {
                (videoView?.surface as? SurfaceView)?.let(mediaPlayer::setVideoSurfaceView)
            } else if (videoView?.surface is TextureView) {
                (videoView?.surface as? TextureView)?.let(mediaPlayer::setVideoTextureView)
            }
        }
    }

    override fun onPlaybackStateChanged(playbackState: Int) {
        when (playbackState) {
            Player.STATE_BUFFERING -> {
                onInfo(MediaPlayer.Info(MediaPlayer.INFO_BUFFERING_STARTED, 0))
                onStateChanged(MediaPlayer.State.BUFFERING)
            }

            Player.STATE_READY -> {
                if (seekDuration > 0) {
                    mediaPlayer.seekTo(seekDuration)
                    seekDuration = 0
                }
            }

            Player.STATE_ENDED -> {
                onInfo(MediaPlayer.Info(MediaPlayer.INFO_PLAYBACK_COMPLETE, 0))
                onStateChanged(MediaPlayer.State.COMPLETED)
            }
        }
    }

    override fun onCues(cues: MutableList<Cue>) {
        val subtitle = cues.map { it.text }
            .joinToString("\n")
        onSubtitleTextUpdated(subtitle)
    }

    override fun onIsPlayingChanged(isPlaying: Boolean) {
        if (isPlaying) {
            if (mediaPlayer.playbackState == Player.STATE_BUFFERING) {
                onInfo(MediaPlayer.Info(MediaPlayer.INFO_BUFFERING_COMPLETED, 0))
            }
            onStateChanged(MediaPlayer.State.PLAYING)
        } else {
            onStateChanged(MediaPlayer.State.PAUSED)
        }

    }

    override fun onPlayerError(error: PlaybackException) {
        error.printStackTrace()
        onInfo(MediaPlayer.Info(error.errorCode, 0))
    }

    override fun onEvents(player: Player, events: Player.Events) {

    }

    override fun onVideoSizeChanged(videoSize: VideoSize) {
        videoWidth = (videoSize.width * videoSize.pixelWidthHeightRatio).toInt()
        videoHeight = (videoSize.height * videoSize.pixelWidthHeightRatio).toInt()
        videoView?.videoSize = MediaPlayer.VideoSize(
            videoWidth,
            videoHeight,
            videoSize.pixelWidthHeightRatio
        )
        onVideoSizeChanged(
            MediaPlayer.VideoSize(
                videoWidth,
                videoHeight,
                videoSize.pixelWidthHeightRatio
            )
        )
    }

    override fun onMetadata(metadata: Metadata) {

    }

    override fun play(initialSeek: Long) {
        Log.d("playMedia", "ExoMediaPlayer:play")

        seekDuration = initialSeek
        if (isReleased) {
            mediaPlayer = getPlayer()
            setInternalVideoView()
        }
        stop()
        mediaPlayer.playWhenReady = true
        mediaSource?.let { media ->
            val factory = if (media.uri?.startsWith("file://") == true) {
                DefaultDataSource.Factory(context)
            } else {
                DefaultHttpDataSource.Factory().apply {
                    setUserAgent(media.userAgent)
                    setAllowCrossProtocolRedirects(true)
                    setKeepPostFor302Redirects(true)
                }
            }
            val source = DefaultMediaSourceFactory(context)
                .setDataSourceFactory(factory)
                .createMediaSource(MediaItem.fromUri(media.uri!!))
            mediaPlayer.setMediaSource(source, initialSeek)
            mediaPlayer.prepare()
        }
    }

    override fun pause() {
        if (isPlaying) {
            mediaPlayer.pause()
        }
    }

    override fun stop() {
        if (isPlaying) {
            mediaPlayer.stop()
        }
    }

    override fun seekTo(msec: Long) {
        if (isReleased) return

        seekDuration = if (isPlaying) {
            mediaPlayer.seekTo(msec)
            0
        } else {
            msec
        }
    }

    private fun retry() {
        try {
            retryThread?.interrupt()
            retryThread = thread {
                try {
                    release(MediaPlayer.State.PLAYING)
                    Thread.sleep(5000)
                    play()
                } catch (e: Throwable) {
                    e.printStackTrace()
                }
            }
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    override fun seek(msec: Long) {
        Log.d("ExoPlayer", "seek: $msec")
        val cp = position
        seekTo(cp + msec)
    }

    override fun resume() {
        mediaPlayer.run {
            if (!mediaPlayer.playWhenReady) {
                Log.d("playMedia", "ExoMediaPlayer:resume")
                play()
            }
        }
    }

    override fun release(targetState: MediaPlayer.State) {
        if (mediaPlayer.isPlaying)
            mediaPlayer.stop()
        mediaPlayer.clearVideoSurface()
        mediaPlayer.release()
        mediaPlayer.setForegroundMode(false)
        isReleased = true
        retryThread?.interrupt()
    }

    override fun setVideoView(surfaceView: SurfaceView?) {
        this.surfaceView = surfaceView
        setInternalVideoView()
    }

    override fun setVideoView(textureView: TextureView?) {
        this.textureView = textureView
        setInternalVideoView()
    }

    override fun setVideoView(videoView: VideoView?) {
        this.videoView = videoView
        setInternalVideoView()
    }

}