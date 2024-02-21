@Suppress("DSL_SCOPE_VIOLATION") // TODO: Remove once KTIJ-19369 is fixed
plugins {
    id("com.android.application")
    alias(libs.plugins.org.jetbrains.kotlin.android)
    alias(libs.plugins.google.ksp.plugin)
    alias(libs.plugins.hilt)
    id("kotlin-kapt")
    alias(libs.plugins.breadboard)
}
android {
    namespace = "io.xnchung.breadboard.sample"
    compileSdk = 33

    defaultConfig {
        applicationId = "io.xnchung.breadboard.sample"
        minSdk = 21
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.getByName("debug")
        }
        create("breadboard") {
            initWith(buildTypes.getByName("debug"))
        }
    }

    flavorDimensions.add("feature")
    productFlavors {
        create("pro") {
            dimension = "feature"
        }
        create("free") {
            dimension = "feature"
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.composeCompiler.get()
    }
//    packaging {
//        resources {
//            excludes += "/META-INF/{AL2.0,LGPL2.1}"
//        }
//    }
}

dependencies {
    implementation(libs.androidx.core)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui.ui.graphics)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material.material3)
    implementation(project(":sample:bizs:bizhome"))
    implementation(libs.material)
    implementation(libs.constraintlayout)

    implementation(libs.hilt.android)
    kapt(project(":framework:processor"))
    kapt(libs.hilt.compiler)

    debugImplementation(project(":framework:launcher"))
    debugImplementation(project(":framework:base"))
    implementation(project(":framework:annotations"))
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.junit)
    androidTestImplementation(libs.androidx.test.espresso.core)
    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.compose.ui.test.junit4)
    debugImplementation(libs.compose.ui.tooling)
    debugImplementation(libs.compose.ui.test.manifest)
}