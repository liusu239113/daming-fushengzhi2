import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.kotlin.plugin.serialization")
}

fun localProperty(name: String): String {
    val localProperties = Properties()
    val file = rootProject.file("local.properties")
    if (file.isFile) file.inputStream().use { localProperties.load(it) }
    return localProperties.getProperty(name).orEmpty()
}

android {
    namespace = "com.arktools.daming"
    compileSdk = 35

    signingConfigs {
        create("release") {
            val releaseStore = localProperty("RELEASE_STORE_FILE")
            if (releaseStore.isNotBlank()) storeFile = rootProject.file(releaseStore)
            storePassword = localProperty("RELEASE_STORE_PASSWORD")
            keyAlias = localProperty("RELEASE_KEY_ALIAS")
            keyPassword = localProperty("RELEASE_KEY_PASSWORD")
            storeType = "PKCS12"
        }
    }

    defaultConfig {
        applicationId = "com.arktools.daming"
        minSdk = 26
        targetSdk = 35
        versionCode = 62
        versionName = "1.0.0"

        buildConfigField("long", "AD_APP_ID", "2079155823355506689L")
        buildConfigField("String", "REWARDED_AD_PLACEMENT_ID", "\"2079387068379312129\"")
        buildConfigField("String", "TAPTAP_CLIENT_ID", "\"6hgf5hurytkwwmg4lb\"")
        buildConfigField("String", "TAPTAP_CLIENT_TOKEN", "\"${localProperty("TAPTAP_CLIENT_TOKEN")}\"")
        buildConfigField("String", "PRIVACY_POLICY_URL", "\"${localProperty("PRIVACY_POLICY_URL")}\"")
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(platform("androidx.compose:compose-bom:2024.12.01"))
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("androidx.datastore:datastore-preferences:1.1.1")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")

    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.github.bumptech.glide:glide:4.16.0")
    implementation("com.squareup.retrofit2:adapter-rxjava2:2.2.0")
    implementation("io.reactivex.rxjava2:rxjava:2.2.21")

    implementation(files("libs/tosin-ad-1.1.2.aar"))
    implementation(files("libs/tosin-adx-2.9.65.aar"))
    implementation(files("libs/tosin-gdt-adapter-4.690.1560.aar"))
    implementation(files("libs/tosin-ks-adapter-5.1.20.1.aar"))
    implementation(files("libs/sigmob/tosin-sigmob_common-adapter-1.9.4.aar"))
    implementation(files("libs/sigmob/tosin-sigmob_windsdk-adapter-4.25.11.aar"))
    implementation(files("libs/topon/tosin-anythink_banner-adapter.aar"))
    implementation(files("libs/topon/tosin-anythink_china_core.aar"))
    implementation(files("libs/topon/tosin-anythink_core-adapter.aar"))
    implementation(files("libs/topon/tosin-anythink_interstitial-adapter.aar"))
    implementation(files("libs/topon/tosin-anythink_native-adapter.aar"))
    implementation(files("libs/topon/tosin-anythink_rewardvideo-adapter.aar"))
    implementation(files("libs/topon/tosin-anythink_splash-adapter.aar"))
    implementation(files("libs/topon/tosin-anythink_adx_sdk_kuying_necessary-adapter-6.5.48.aar"))
    implementation(files("libs/topon/tosin-anythink_network_adx_kuying_sdk_necessary-adapter.aar"))
    implementation(files("libs/yout/tosin-adalliance-adapter-4.7.7.aar"))
    implementation(files("libs/adgain/tosin-adgainsdk-adapter-4.2.5.aar"))
    implementation(files("libs/adgain/tosin-adgainbeizi-adapter-4.2.3.5.aar"))
    implementation(files("libs/adgain/tosin-adgaingromore-adapter-4.2.5.aar"))
    implementation(files("libs/adgain/tosin-adgainjiguang-adapter-4.2.2.1.aar"))
    implementation(files("libs/adgain/tosin-adgaintaku-adapter-4.2.3.2.aar"))
    implementation(files("libs/adgain/tosin-adgaintobid-adapter-4.2.5.aar"))
    implementation(files("libs/hx/tosin-hx-sdk-1.6.17.aar"))
    implementation(files("libs/hx/tosin-hx-gromore-adapter.aar"))
    implementation(files("libs/hx/tosin-hx-mediatom-adapter.aar"))
    implementation(files("libs/hx/tosin-hx-taku-adapter.aar"))
    implementation(files("libs/hx/tosin-hx-tobid-adapter.aar"))
    implementation(files("libs/jiatou/tosin-advista-adapter-1.9.2.aar"))
    implementation(files("libs/tosin-csj-adapter-7.6.1.1.aar"))
    implementation(files("libs/tosin-baidu-adapter-9.450.aar"))
    implementation(files("libs/adview/tosin-adview-adapter-5.0.5.aar"))
    implementation(files("libs/beizi/tosin-beizi-adapter-5.3.0.3.aar"))
    implementation(files("libs/dm/tosin-domob-adapter-3.8.2.aar"))
    implementation(files("libs/funlink/tosin-funlink-adapter-2.9.0_77390768.aar"))
    implementation(files("libs/funlink/tosin-funlink_gromore-adapter-2.9.0_77328722.aar"))
    implementation(files("libs/funlink/tosin-funlink_taku-adapter-2.9.0_77328722.aar"))
    implementation(files("libs/funlink/tosin-funlink_tobid-adapter-2.9.0_77328722.aar"))
    implementation(files("libs/jutui/tosin-jutui-adapter-4.2.3.1.aar"))
    implementation(files("libs/ms/tosin-ms-adapter-3.0.4.1.aar"))
    implementation(files("libs/maimeng/tosin-wm-adapter-7.9.19.25.aar"))
    implementation(files("libs/taptap/tosin-taptap-adapter-4.2.4.8.aar"))
    implementation(files("libs/tianxuan/tosin-UBiX-adapter-2.10.1.11.aar"))
    implementation(files("libs/zhongchen/tosin-starsads-adapter-1.3.04.aar"))
    implementation(files("libs/oaid_sdk_1.0.25.aar"))

    implementation("com.taptap.sdk:tap-core:4.10.3")
    implementation("com.taptap.sdk:tap-login:4.10.3")
    implementation("com.taptap.sdk:tap-compliance:4.10.3")

    testImplementation("junit:junit:4.13.2")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
