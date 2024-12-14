plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.example.gotoesig"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.gotoesig"
        minSdk = 24
        targetSdk = 34
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
}

dependencies {
    // Firebase BOM to manage versions automatically
    implementation(platform("com.google.firebase:firebase-bom:32.0.0"))

    // Firebase dependencies (let BOM manage versions)
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-storage")

    // Other dependencies
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation("androidx.activity:activity:1.8.0")
    implementation(libs.constraintlayout)

    // Glide dependency
    implementation("com.github.bumptech.glide:glide:4.15.1")
    implementation(libs.play.services.location)


    annotationProcessor("com.github.bumptech.glide:compiler:4.15.1") // Usar annotationProcessor

    // Activity Result API dependency
    implementation("androidx.activity:activity:1.7.2")

    // Testing dependencies
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    implementation("com.squareup.okhttp3:okhttp:4.10.0")
    implementation("com.google.code.gson:gson:2.8.8")
    implementation("org.osmdroid:osmdroid-android:6.1.14")
    implementation("androidx.recyclerview:recyclerview:1.2.1")
}

apply(plugin = "com.google.gms.google-services")
