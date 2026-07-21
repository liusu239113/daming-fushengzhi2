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
        minSdk = 23
        targetSdk = 35
        versionCode = 61
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

    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("**/*.aar"))))

    implementation("com.taptap.sdk:tap-core:4.10.3")
    implementation("com.taptap.sdk:tap-login:4.10.3")
    implementation("com.taptap.sdk:tap-compliance:4.10.3")

    testImplementation("junit:junit:4.13.2")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
