import com.vanniktech.maven.publish.AndroidMultiVariantLibrary

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("maven-publish")
    id("signing")
    id("com.vanniktech.maven.publish") version "0.34.0"
}

mavenPublishing {
    if (System.getenv("PUBLISH_STATE") == "Release") {
        signAllPublications()
    }
}

mavenPublishing {
    configure(
        AndroidMultiVariantLibrary(
            sourcesJar = true,
            publishJavadocJar = true,
        )
    )

    if (System.getenv("PUBLISH_STATE") == "Release") {
        coordinates("io.github.awxkee", "avif-coder-coil", System.getenv("VERSION_NAME") ?: "0.0.10")
    } else {
        coordinates("io.github.awxkee", "avif-coder-coil", "0.0.10")
    }

    pom {
        name.set("AVIF Coder Coil")
        description.set("AVIF encoder/decoder plugin for coil for Android")
        inceptionYear.set("2025")
        url.set("https://github.com/awxkee/avif-coder-coil")
        licenses {
            license {
                name.set("The Apache License, Version 2.0")
                url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                distribution.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
            }
            license {
                name.set("The 3-Clause BSD License")
                url.set("https://opensource.org/license/bsd-3-clause")
                description.set("https://opensource.org/license/bsd-3-clause")
            }
        }
        developers {
            developer {
                id.set("awxkee")
                name.set("Radzivon Bartoshyk")
                url.set("https://github.com/awxkee")
                email.set("radzivon.bartoshyk@proton.me")
            }
        }
        scm {
            url.set("https://github.com/awxkee/avif-coder-coil")
            connection.set("scm:git:git@github.com:awxkee/avif-coder-coil.git")
            developerConnection.set("scm:git:ssh://git@github.com/awxkee/avif-coder-coil.git")
        }
    }
}

task("androidSourcesJar", Jar::class) {
    archiveClassifier.set("sources")
    from(android.sourceSets.getByName("main").java.srcDirs)
}

android {
    publishing {
        singleVariant("release") {
            withSourcesJar()
            withJavadocJar()
        }
    }

    namespace = "com.github.awxkee.avifcodercoil"
    compileSdk = 35

    defaultConfig {
        minSdk = 24
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    api("io.coil-kt.coil3:coil:3.3.0")
    api("com.github.awxkee:avif-coder:2.1.4")
}