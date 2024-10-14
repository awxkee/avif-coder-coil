package com.awxkee.avif.coil

import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import coil.imageLoader
import coil.load
import coil.util.DebugLogger
import com.awxkee.avif.coil.databinding.ActivityMainBinding
import com.github.awxkee.avifcoil.decoder.animation.AnimatedAvifDecoder

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            binding.imageView.load(
                data = "file:///android_asset/output.avif".toUri(),
                imageLoader = imageLoader
                    .newBuilder()
                    .logger(DebugLogger())
                    .components {
                        add(AnimatedAvifDecoder.Factory(preheatFrames = 2))
                    }
                    .bitmapConfig(Bitmap.Config.RGBA_1010102)
                    .build()
            )
        }
    }
}