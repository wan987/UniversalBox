plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("kotlin-kapt")
}

android {
    namespace = "com.universalbox.app"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.universalbox.app"
        minSdk = 27
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation("androidx.compose.material:material-icons-extended")
    implementation(libs.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // Room 数据库 (必选)
    val roomVersion = "2.6.1" // 变量名改了

    implementation("androidx.room:room-runtime:$roomVersion") // 这里也要改引用名
    implementation("androidx.room:room-ktx:$roomVersion")
    kapt("androidx.room:room-compiler:$roomVersion")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.1")
    
    // 导航库 (Jetpack Compose Navigation)
    implementation("androidx.navigation:navigation-compose:2.7.6")
    
    // 常用工具
    implementation("com.google.code.gson:gson:2.10.1")     // 后面存复杂数据用
    
    // 网页解析库 (抓取网页标题、描述、图片)
    implementation("org.jsoup:jsoup:1.17.2")
    
    // 图片加载库 (Coil - Compose专用)
    implementation("io.coil-kt:coil-compose:2.6.0")
    
    // Chrome Custom Tabs (用于打开网页链接)
    implementation("androidx.browser:browser:1.7.0")
    
    // Lottie 动画库 (Compose版本)
    implementation("com.airbnb.android:lottie-compose:6.3.0")
    
    // 二维码库 (ZXing)
    implementation("com.google.zxing:core:3.5.3")
    implementation("com.journeyapps:zxing-android-embedded:4.3.0")
    
    // Google ML Kit 文字识别
    implementation("com.google.mlkit:text-recognition:16.0.0")
}