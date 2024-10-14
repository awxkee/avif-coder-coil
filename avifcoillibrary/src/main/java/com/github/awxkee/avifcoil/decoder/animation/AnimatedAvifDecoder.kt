package com.github.awxkee.avifcoil.decoder.animation


import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import coil.ImageLoader
import coil.decode.DecodeResult
import coil.decode.Decoder
import coil.fetch.SourceResult
import coil.request.Options
import coil.size.Scale
import coil.size.Size
import coil.size.pxOrElse
import com.radzivon.bartoshyk.avif.coder.AvifAnimatedDecoder
import com.radzivon.bartoshyk.avif.coder.PreferredColorConfig
import com.radzivon.bartoshyk.avif.coder.ScaleMode
import kotlinx.coroutines.runInterruptible
import okio.ByteString.Companion.encodeUtf8

public class AnimatedAvifDecoder(
    private val source: SourceResult,
    private val options: Options,
    private val preheatFrames: Int,
    private val exceptionLogger: ((Exception) -> Unit)? = null,
) : Decoder {

    override suspend fun decode(): DecodeResult? = runInterruptible {
        try {
            // ColorSpace is preferred to be ignored due to lib is trying to handle all color profile by itself
            val sourceData = source.source.source().readByteArray()

            var mPreferredColorConfig: PreferredColorConfig = when (options.config) {
                Bitmap.Config.ALPHA_8 -> PreferredColorConfig.RGBA_8888
                Bitmap.Config.RGB_565 -> if (options.allowRgb565) PreferredColorConfig.RGB_565 else PreferredColorConfig.DEFAULT
                Bitmap.Config.ARGB_8888 -> PreferredColorConfig.RGBA_8888
                else -> PreferredColorConfig.DEFAULT
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && options.config == Bitmap.Config.RGBA_F16) {
                mPreferredColorConfig = PreferredColorConfig.RGBA_F16
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && options.config == Bitmap.Config.HARDWARE) {
                mPreferredColorConfig = PreferredColorConfig.HARDWARE
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && options.config == Bitmap.Config.RGBA_1010102) {
                mPreferredColorConfig = PreferredColorConfig.RGBA_1010102
            }

            if (options.size == Size.ORIGINAL) {
                val originalImage = AvifAnimatedDecoder(sourceData)
                return@runInterruptible DecodeResult(
                    drawable = originalImage.drawable(
                        colorConfig = mPreferredColorConfig,
                        scaleMode = ScaleMode.FIT,
                    ),
                    isSampled = false
                )
            }

            val dstWidth = options.size.width.pxOrElse { 0 }
            val dstHeight = options.size.height.pxOrElse { 0 }
            val scaleMode = when (options.scale) {
                Scale.FILL -> ScaleMode.FILL
                Scale.FIT -> ScaleMode.FIT
            }

            val originalImage = AvifAnimatedDecoder(sourceData)

            DecodeResult(
                drawable = originalImage.drawable(
                    dstWidth = dstWidth,
                    dstHeight = dstHeight,
                    colorConfig = mPreferredColorConfig,
                    scaleMode = scaleMode
                ),
                isSampled = true
            )
        } catch (e: Exception) {
            exceptionLogger?.invoke(e)
            return@runInterruptible null
        }
    }

    private fun AvifAnimatedDecoder.drawable(
        dstWidth: Int = 0,
        dstHeight: Int = 0,
        colorConfig: PreferredColorConfig,
        scaleMode: ScaleMode
    ): Drawable = if (getFramesCount() > 1) {
        AnimatedDrawable(
            frameStore = AvifAnimatedStore(
                avifAnimatedDecoder = this,
                targetWidth = dstWidth,
                targetHeight = dstHeight,
                scaleMode = scaleMode,
                preferredColorConfig = colorConfig
            ),
            preheatFrames = preheatFrames,
            firstFrameAsPlaceholder = true
        )
    } else {
        BitmapDrawable(
            options.context.resources,
            if (dstWidth == 0 || dstHeight == 0) {
                getFrame(
                    frame = 0,
                    preferredColorConfig = colorConfig
                )
            } else {
                getScaledFrame(
                    frame = 0,
                    scaledWidth = dstWidth,
                    scaledHeight = dstHeight,
                    scaleMode = scaleMode,
                    preferredColorConfig = colorConfig
                )
            }
        )
    }

    public class Factory(
        private val preheatFrames: Int = 6,
        private val exceptionLogger: ((Exception) -> Unit)? = null,
    ) : Decoder.Factory {

        override fun create(
            result: SourceResult,
            options: Options,
            imageLoader: ImageLoader,
        ): Decoder? {
            return if (
                AVAILABLE_BRANDS.any {
                    result.source.source().rangeEquals(4, it)
                }
            ) AnimatedAvifDecoder(
                source = result,
                options = options,
                preheatFrames = preheatFrames,
                exceptionLogger = exceptionLogger,
            )
            else null
        }

        companion object {
            private val AVIF = "ftypavif".encodeUtf8()
            private val AVIS = "ftypavis".encodeUtf8()

            private val AVAILABLE_BRANDS = listOf(AVIF, AVIS)
        }
    }

    @SuppressLint("ObsoleteSdkInt")
    companion object {
        init {
            if (Build.VERSION.SDK_INT >= 24) {
                System.loadLibrary("coder")
            }
        }
    }

}