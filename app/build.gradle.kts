import org.gradle.api.tasks.bundling.Zip

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

val releaseVersionCode = providers.gradleProperty("RELEASE_VERSION_CODE")
    .orElse(providers.environmentVariable("RELEASE_VERSION_CODE"))
    .map(String::toInt)
    .orElse(
        providers.gradleProperty("appVersionCode")
            .map(String::toInt)
            .orElse(1)
    )

val releaseVersionName = providers.gradleProperty("RELEASE_VERSION_NAME")
    .orElse(providers.environmentVariable("RELEASE_VERSION_NAME"))
    .orElse(providers.gradleProperty("appVersionName"))
    .orElse("1.0")

val releaseKeystorePath = providers.gradleProperty("ANDROID_KEYSTORE_PATH")
    .orElse(providers.environmentVariable("ANDROID_KEYSTORE_PATH"))
    .orNull

android {
    namespace = "me.asteroidus.swissgrades"
    compileSdk = 36

    defaultConfig {
        applicationId = "me.asteroidus.swissgrades"
        minSdk = 24
        targetSdk = 36
        versionCode = releaseVersionCode.get()
        versionName = releaseVersionName.get()

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        if (!releaseKeystorePath.isNullOrBlank()) {
            create("release") {
                storeFile = file(releaseKeystorePath)
                storePassword = providers.gradleProperty("ANDROID_KEYSTORE_PASSWORD")
                    .orElse(providers.environmentVariable("ANDROID_KEYSTORE_PASSWORD"))
                    .orNull
                keyAlias = providers.gradleProperty("ANDROID_KEY_ALIAS")
                    .orElse(providers.environmentVariable("ANDROID_KEY_ALIAS"))
                    .orNull
                keyPassword = providers.gradleProperty("ANDROID_KEY_PASSWORD")
                    .orElse(providers.environmentVariable("ANDROID_KEY_PASSWORD"))
                    .orNull
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            ndk {
                debugSymbolLevel = "SYMBOL_TABLE"
            }
            if (!releaseKeystorePath.isNullOrBlank()) {
                signingConfig = signingConfigs.getByName("release")
            }
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }

    testOptions {
        animationsDisabled = true

        managedDevices {
            localDevices {
                create("pixel2Api36") {
                    device = "Pixel 2"
                    apiLevel = 36
                    systemImageSource = "google"
                    testedAbi = "x86_64"
                }
            }
        }
    }
}

val nativeSymbolsSourceDir = layout.buildDirectory.dir(
    "intermediates/merged_native_libs/release/mergeReleaseNativeLibs/out/lib"
)

tasks.register<Zip>("zipReleaseNativeDebugSymbols") {
    group = "build"
    description = "Packages native debug symbols for Play Console uploads."
    dependsOn("mergeReleaseNativeLibs")
    from(nativeSymbolsSourceDir)
    destinationDirectory.set(layout.buildDirectory.dir("outputs/native-debug-symbols/release"))
    archiveFileName.set("native-debug-symbols.zip")
}

tasks.matching { it.name == "bundleRelease" }.configureEach {
    finalizedBy("zipReleaseNativeDebugSymbols")
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.coil.compose)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
