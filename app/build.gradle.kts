plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}
android {
    namespace = "com.jamiewardle.auralift"
    compileSdk = 36
    defaultConfig {
        applicationId = "com.jamiewardle.auralift"
        minSdk = 26
        targetSdk = 36
        versionCode = 5
        versionName = "0.5.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    flavorDimensions += "distribution"
    productFlavors {
        create("owner") {
            dimension = "distribution"
            applicationIdSuffix = ".owner"
            versionNameSuffix = "-owner"
            resValue("string", "app_name", "Auralift Owner")
        }
        create("play") {
            dimension = "distribution"
            resValue("string", "app_name", "Auralift")
            buildConfigField("String", "PLAY_PUBLIC_KEY", "\"${providers.gradleProperty("auralift.playPublicKey").orElse("").get()}\"")
            buildConfigField("boolean", "STORE_LIVE", providers.gradleProperty("auralift.storeLive").orElse("false").get())
            buildConfigField("String", "ADMOB_APP_ID", "\"${providers.gradleProperty("auralift.admobAppId").orElse("").get()}\"")
            buildConfigField("String", "REWARDED_AD_ID", "\"${providers.gradleProperty("auralift.rewardedAdId").orElse("").get()}\"")
            manifestPlaceholders["admobAppId"] = providers.gradleProperty("auralift.admobAppId").orElse("ca-app-pub-3940256099942544~3347511713").get()
        }
    }
    buildTypes {
        debug {
            // Always Google's test inventory in debug, regardless of local production properties.
            manifestPlaceholders["admobAppId"] = "ca-app-pub-3940256099942544~3347511713"
        }
        release { isMinifyEnabled = true; proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro") }
    }
    compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
    buildFeatures { compose = true; buildConfig = true }
    // Language selection must work offline even when Play delivers split APKs.
    bundle { language { enableSplit = false } }
    testOptions {
        unitTests.isIncludeAndroidResources = true
        unitTests.all {
            // Run SDK 26 and 35 in separate JVM invocations. Robolectric 4.16's
            // shared native font ZIP cannot reliably be reopened across SDK sandboxes.
            it.systemProperty("robolectric.enabledSdks", providers.gradleProperty("auralift.testSdk").orElse("35").get())
            it.forkEvery = 1
            it.maxHeapSize = "1536m"
        }
    }
}
kotlin { compilerOptions { jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17) } }
dependencies {
    implementation(platform("androidx.compose:compose-bom:2025.08.01"))
    implementation("androidx.activity:activity-compose:1.10.1")
    implementation("androidx.appcompat:appcompat:1.7.1")
    implementation("androidx.core:core-ktx:1.16.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.9.2")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    // Explicit upgrade of Compose's transitive native path library for current page-size support.
    implementation("androidx.graphics:graphics-path:1.1.0")
    "playImplementation"("com.android.billingclient:billing:9.1.0")
    "playImplementation"("com.google.android.libraries.ads.mobile.sdk:ads-mobile-sdk:1.4.0")
    debugImplementation("androidx.compose.ui:ui-tooling")
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.robolectric:robolectric:4.16")
    testImplementation("androidx.test:core:1.7.0")
    testImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
