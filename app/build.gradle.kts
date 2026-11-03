plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.kotlinAndroid)
    alias(libs.plugins.kotlinCompose)
    alias(libs.plugins.kotlinSerialization)
}

android { namespace = "io.github.takano536.kobako"; compileSdk = 36; buildToolsVersion = "36.0.0"
    defaultConfig { applicationId = "io.github.takano536.kobako"; minSdk = 26; targetSdk = 36; versionCode = 1; versionName = "0.1.0" }
    buildTypes { getByName("debug") { manifestPlaceholders["usesCleartextTraffic"] = "true" }; getByName("release") { manifestPlaceholders["usesCleartextTraffic"] = "false" } }
    buildFeatures { compose = true; buildConfig = true }
    compileOptions { sourceCompatibility = JavaVersion.VERSION_21; targetCompatibility = JavaVersion.VERSION_21 }
    kotlinOptions { jvmTarget = "21" }
    buildTypes { release { isMinifyEnabled = false } }
}

dependencies {
    implementation(libs.androidx.core)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime)
    implementation(libs.androidx.lifecycle.viewmodel)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.work)
    implementation(libs.androidx.datastore)
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.foundation)
    implementation(libs.compose.material3)
    implementation(libs.retrofit)
    implementation(libs.retrofit.serialization)
    implementation(libs.okhttp)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.coroutines)
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines)
}
