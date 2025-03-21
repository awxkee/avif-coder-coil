package com.awxkee.avif.coil

import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import coil3.imageLoader
import coil3.load
import coil3.request.bitmapConfig
import coil3.util.DebugLogger
import com.awxkee.avif.coil.databinding.ActivityMainBinding
import com.github.awxkee.avifcoil.decoder.HeifDecoder

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            binding.imageView.load(
                data = "file:///android_asset/test_alpha.avif".toUri(),
                imageLoader = imageLoader
                    .newBuilder()
                    .logger(DebugLogger())
                    .components {
                        add(HeifDecoder.Factory())
                    }
                    .bitmapConfig(Bitmap.Config.RGBA_1010102)
                    .build()
            )
        }
    }
}