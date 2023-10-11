package dev.sajidali.onplayer.core

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.TextureView
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class VideoView : ConstraintLayout {

    enum class AspectRatio {

        AR_MATCH_PARENT,
        AR_16_9_FIT_PARENT,
        AR_4_3_FIT_PARENT,
        AR_BEST_FIT

    }

    private var surfaceView: TextureView = TextureView(context)
    var aspectRatio: AspectRatio = AspectRatio.AR_BEST_FIT
        set(value) {
            field = value
            setInternalAspectRatio()
        }
    var videoSize: MediaPlayer.VideoSize = MediaPlayer.VideoSize(0, 0, 1.0f)
        set(value) {
            field = value
            setInternalAspectRatio()
        }

    var onTouchListener: ((event: MotionEvent) -> Boolean) = { false }

    var videoController: VideoController? = null
    private var visibilityJob: Job? = null

    fun updateSubtitles(subtitle: String) {
        videoController?.setSubTitles(subtitle)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        setVisibilityTimer()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        visibilityJob?.cancel()
    }

    val surface: View
        get() = surfaceView

    constructor(ctx: Context) : super(ctx)
    constructor(ctx: Context, attrs: AttributeSet) : super(ctx, attrs)
    constructor(ctx: Context, attrs: AttributeSet, defStyleRes: Int) : super(
        ctx,
        attrs,
        defStyleRes
    )

    init {
        surfaceView.id = View.generateViewId()
        addView(
            surfaceView,
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        setBackgroundColor(Color.BLACK)
        setInternalAspectRatio()
    }

    private fun setInternalAspectRatio() {
        val set = ConstraintSet()
        when (aspectRatio) {
            AspectRatio.AR_MATCH_PARENT -> {
                set.clear(surfaceView.id)
                set.connect(
                    surfaceView.id,
                    ConstraintSet.LEFT,
                    ConstraintSet.PARENT_ID,
                    ConstraintSet.LEFT
                )
                set.connect(
                    surfaceView.id,
                    ConstraintSet.RIGHT,
                    ConstraintSet.PARENT_ID,
                    ConstraintSet.RIGHT
                )
                set.connect(
                    surfaceView.id,
                    ConstraintSet.TOP,
                    ConstraintSet.PARENT_ID,
                    ConstraintSet.TOP
                )
                set.connect(
                    surfaceView.id,
                    ConstraintSet.BOTTOM,
                    ConstraintSet.PARENT_ID,
                    ConstraintSet.BOTTOM
                )
                set.applyTo(this)
                videoController?.showInfo("Fit Screen")
            }

            AspectRatio.AR_16_9_FIT_PARENT -> {
                set.clear(surfaceView.id)
                set.connect(
                    surfaceView.id,
                    ConstraintSet.LEFT,
                    ConstraintSet.PARENT_ID,
                    ConstraintSet.LEFT
                )
                set.connect(
                    surfaceView.id,
                    ConstraintSet.RIGHT,
                    ConstraintSet.PARENT_ID,
                    ConstraintSet.RIGHT
                )
                set.connect(
                    surfaceView.id,
                    ConstraintSet.TOP,
                    ConstraintSet.PARENT_ID,
                    ConstraintSet.TOP
                )
                set.connect(
                    surfaceView.id,
                    ConstraintSet.BOTTOM,
                    ConstraintSet.PARENT_ID,
                    ConstraintSet.BOTTOM
                )
                set.setDimensionRatio(surfaceView.id, "16:10")
                set.applyTo(this)
                videoController?.showInfo("16:10")
            }

            AspectRatio.AR_4_3_FIT_PARENT -> {
                set.clear(surfaceView.id)
                set.connect(
                    surfaceView.id,
                    ConstraintSet.LEFT,
                    ConstraintSet.PARENT_ID,
                    ConstraintSet.LEFT
                )
                set.connect(
                    surfaceView.id,
                    ConstraintSet.RIGHT,
                    ConstraintSet.PARENT_ID,
                    ConstraintSet.RIGHT
                )
                set.connect(
                    surfaceView.id,
                    ConstraintSet.TOP,
                    ConstraintSet.PARENT_ID,
                    ConstraintSet.TOP
                )
                set.connect(
                    surfaceView.id,
                    ConstraintSet.BOTTOM,
                    ConstraintSet.PARENT_ID,
                    ConstraintSet.BOTTOM
                )
                set.setDimensionRatio(surfaceView.id, "4:3")
                set.applyTo(this)
                videoController?.showInfo("4:3")
            }

            AspectRatio.AR_BEST_FIT -> {
                set.clear(surfaceView.id)
                set.connect(
                    surfaceView.id,
                    ConstraintSet.LEFT,
                    ConstraintSet.PARENT_ID,
                    ConstraintSet.LEFT
                )
                set.connect(
                    surfaceView.id,
                    ConstraintSet.RIGHT,
                    ConstraintSet.PARENT_ID,
                    ConstraintSet.RIGHT
                )
                set.connect(
                    surfaceView.id,
                    ConstraintSet.TOP,
                    ConstraintSet.PARENT_ID,
                    ConstraintSet.TOP
                )
                set.connect(
                    surfaceView.id,
                    ConstraintSet.BOTTOM,
                    ConstraintSet.PARENT_ID,
                    ConstraintSet.BOTTOM
                )
                set.setDimensionRatio(
                    surfaceView.id,
                    "H,${videoSize.width * videoSize.ratio}:${videoSize.height * videoSize.ratio}"
                )
                set.applyTo(this)
                videoController?.showInfo("Best Fit")
            }
        }
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        keepScreenOn = true
        updateVideo()
        setVisibilityTimer()
    }

    private fun updateVideo() {
        val index = indexOfChild(surfaceView)
        removeView(surfaceView)
        addView(surfaceView, index)
    }

    fun setVisibilityTimer(showButtons: Boolean = false) {
        videoController?.show(true)
        if (visibilityJob?.isActive == true) {
            visibilityJob?.cancel()
        }

        visibilityJob = GlobalScope.launch {
            delay(10000L)
            post {
                videoController?.hide()
            }
        }
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        setVisibilityTimer()
        return super.onKeyUp(keyCode, event)
    }

    override fun performClick(): Boolean {
        return super.performClick()
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (videoController?.isControlsVisible() == true) event?.let { onTouchListener(it) }
        setVisibilityTimer(true)
        if (event?.action == MotionEvent.ACTION_UP) {
            performClick()
        }
        return true
    }

}