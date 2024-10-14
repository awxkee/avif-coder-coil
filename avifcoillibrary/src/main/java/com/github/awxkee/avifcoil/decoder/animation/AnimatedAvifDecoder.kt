/*
 * MIT License
 *
 * Copyright (c) 2024 Radzivon Bartoshyk
 * jxl-coder [https://github.com/awxkee/jxl-coder]
 *
 * Created by Radzivon Bartoshyk on 9/3/2024
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package com.github.awxkee.avifcoil.decoder.animation

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
        ).apply {
            setBounds(0, 0, dstWidth, dstHeight)
        }
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

}