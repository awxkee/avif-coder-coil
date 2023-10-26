# AVIF/HEIF coil plugin for Android 24+

Library eliminates many bugs from standart AVIF decoder, supports HDR and ICC profiles.
Correctly handles ICC, and color profiles and HDR images.
Fully supports HDR images, 10, 12 bit. Preprocess image in tile to increase speed.
Extremly fast in decoding large HDR images or just large images.
The most features AVIF, HEIF library in android.
Supported decoding in all necessary pixel formats in Android and avoids android decoding bugs.

# Usage example

Just add to image loader heif decoder factory and use it as image loader in coil

```kotlin
val imageLoader = ImageLoader.Builder(context)
    .components {
        add(HeifDecoder.Factory(context))
    }
    .build()
```

# Add Jitpack repository

```groovy
repositories {
    maven { url "https://jitpack.io" }
}
```

```groovy
implementation 'com.github.awxkee:avif-coder-coil:1.5.3' // or any version above picker from release tags
```

# Disclaimer

## AVIF

AVIF is the next step in image optimization based on the AV1 video codec. It is standardized by the
Alliance for Open Media. AVIF offers more compression gains than other image formats such as JPEG
and WebP. Our and other studies have shown that, depending on the content, encoding settings, and
quality target, you can save up to 50% versus JPEG or 20% versus WebP images. The advanced image
encoding feature of AVIF brings codec and container support for HDR and wide color gamut images,
film grain synthesis, and progressive decoding. AVIF support has improved significantly since Chrome
M85 implemented AVIF support last summer.
