package dev.sajidali.onplayer.core

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.TextureView
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet

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
    var videoSize: MediaPlayer.VideoSize = MediaPlayer.VideoSize(1920, 1080, 1.0f)
        set(value) {
            field = value
            setInternalAspectRatio()
        }

    private var onAspectRatioChanged: AspectRatio.() -> Unit = {}

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

    fun onAspectRatioChanged(block: AspectRatio.() -> Unit) {
        onAspectRatioChanged = block
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

                val bestRatio =
                    if (videoSize.width > 0 && videoSize.height > 0)
                        (width / videoSize.width).coerceAtMost(
                            height / videoSize.height
                        )
                    else width / height
                set.setDimensionRatio(
                    surfaceView.id,
                    "H,${videoSize.width * bestRatio}:${videoSize.height * bestRatio}"
                )
                set.applyTo(this)
            }
        }
        onAspectRatioChanged(aspectRatio)
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        keepScreenOn = true
        updateVideo()
    }

    private fun updateVideo() {
        val index = indexOfChild(surfaceView)
        removeView(surfaceView)
        addView(surfaceView, index)
    }

}