package com.github.awxkee.avifcoil.decoder.animation

import android.graphics.Bitmap
import android.util.Size
import com.radzivon.bartoshyk.avif.coder.AvifAnimatedDecoder
import com.radzivon.bartoshyk.avif.coder.PreferredColorConfig
import com.radzivon.bartoshyk.avif.coder.ScaleMode
import com.radzivon.bartoshyk.avif.coder.ScalingQuality
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

public class AvifAnimatedStore(
    private val avifAnimatedDecoder: AvifAnimatedDecoder,
    private val scaleMode: ScaleMode,
    private val preferredColorConfig: PreferredColorConfig,
    private val targetWidth: Int = 0,
    private val targetHeight: Int = 0,
) : AnimatedFrameStore {

    private val cachedOriginalWidth: Int = avifAnimatedDecoder.getImageSize().width
    private val cachedOriginalHeight: Int = avifAnimatedDecoder.getImageSize().height

    private val dstSize: Size = if (targetWidth > 0 && targetHeight > 0) {
        val xf = targetWidth.toFloat() / cachedOriginalWidth.toFloat()
        val yf = targetHeight.toFloat() / cachedOriginalHeight.toFloat()
        val factor: Float = if (scaleMode == ScaleMode.FILL) {
            max(xf, yf)
        } else {
            min(xf, yf)
        }
        val newSize = Size(
            (cachedOriginalWidth * factor).roundToInt(),
            (cachedOriginalHeight * factor).roundToInt()
        )
        newSize
    } else {
        Size(0, 0)
    }

    override val width: Int
        get() = if (targetWidth > 0 && targetHeight > 0) dstSize.width else cachedOriginalWidth
    override val height: Int
        get() = if (targetWidth > 0 && targetHeight > 0) dstSize.height else cachedOriginalHeight

    override fun getFrame(frame: Int): Bitmap {
        return avifAnimatedDecoder.getScaledFrame(
            frame = frame,
            scaledWidth = targetWidth,
            scaledHeight = targetHeight,
            scaleQuality = ScalingQuality.FASTEST,
            scaleMode = scaleMode,
            preferredColorConfig = preferredColorConfig
        )
    }

    override fun getFrameDuration(frame: Int): Int {
        return avifAnimatedDecoder.getFrameDuration(frame)
    }

    private var storedFramesCount: Int = -1

    override val framesCount: Int
        get() = if (storedFramesCount == -1) {
            storedFramesCount = avifAnimatedDecoder.getFramesCount()
            storedFramesCount
        } else {
            storedFramesCount
        }
}