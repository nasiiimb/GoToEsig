plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.google.gms.google.services) apply false
}

buildscript {
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
        maven { url = uri("https://api.maplibre.org/maven/") } // Repositorio MapLibre

    }

    dependencies {
        classpath("com.google.gms:google-services:4.3.15") // Make sure to include the correct version
    }
}
