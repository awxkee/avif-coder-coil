package com.awxkee.avif.coil

import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import coil.ImageLoader
import coil.load
import com.awxkee.avif.coil.databinding.ActivityMainBinding
import com.github.awxkee.avifcoil.HeifDecoder

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            binding.imageView.load("https://wh.aimuse.online/preset/federico-beccari.avif",
                imageLoader = ImageLoader.Builder(this)
                    .components {
                        add(HeifDecoder.Factory())
                    }
                    .bitmapConfig(Bitmap.Config.HARDWARE)
                    .build())
        }
    }
}