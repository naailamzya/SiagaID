import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
}

// Baca local.properties
val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localProperties.load(localPropertiesFile.inputStream())
}

android {
    namespace = "com.siagaid"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.siagaid"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField(
            "String",
            "OPEN_WEATHER_API_KEY",
            "\"${localProperties.getProperty("OPEN_WEATHER_API_KEY", "YOUR_OWM_KEY")}\""
        )

        manifestPlaceholders["GOOGLE_MAPS_API_KEY"] =
            localProperties.getProperty("GOOGLE_MAPS_API_KEY", "YOUR_MAPS_KEY")
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

    buildFeatures {
        buildConfig = true
        viewBinding = true
    }
}

dependencies {
    // Default yang sudah ada
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)

    // SwipeRefreshLayout
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")

    // Navigation Component
    implementation("androidx.navigation:navigation-fragment:2.7.6")
    implementation("androidx.navigation:navigation-ui:2.7.6")

    // RecyclerView & CardView
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("androidx.cardview:cardview:1.0.0")

    // Retrofit + OkHttp + Gson
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation("com.google.code.gson:gson:2.10.1")

    // Glide
    implementation("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")

    // Google Maps SDK
    implementation("com.google.android.gms:play-services-maps:18.2.0")

    // Google Maps Utils (Cluster + Circle)
    implementation("com.google.maps.android:android-maps-utils:3.4.0")

    // Google Location Services
    implementation("com.google.android.gms:play-services-location:21.0.1")

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}