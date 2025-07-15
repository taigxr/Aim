plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.google.gms.google.services)

    id("org.jetbrains.kotlin.plugin.compose") version "2.0.21" // this version matches your Kotlin version
    kotlin("kapt")
}

android {
    namespace = "com.example.aim"
    compileSdk = 36

    buildFeatures {
        compose = true
        viewBinding = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.6.4"
    }

    kotlinOptions {
        jvmTarget = "11"
    }


    defaultConfig {
        applicationId = "com.example.aim"
        minSdk = 36
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
}

dependencies {
    // Firebase BoM first
    implementation(platform("com.google.firebase:firebase-bom:32.7.0"))

    // Firebase libraries without explicit versions (managed by BoM)
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-firestore-ktx")
    implementation("com.google.firebase:firebase-storage-ktx")

    // Your other libraries (keep these as is)
    implementation("com.github.bumptech.glide:glide:4.15.1")
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.firebase.auth)
    implementation(libs.androidx.runtime.android)
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.cardview)
    implementation(libs.androidx.foundation.android)
    implementation(libs.androidx.material3)
    implementation(libs.firebase.firestore.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.firebase.database.ktx)
    implementation(libs.androidx.media3.common.ktx)
    implementation(libs.firebase.storage)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation("androidx.compose.ui:ui:1.6.4")
    implementation("androidx.compose.material3:material3:1.2.1")
    implementation("androidx.compose.ui:ui-tooling-preview:1.6.4")
    debugImplementation("androidx.compose.ui:ui-tooling:1.6.4")

    //CameraDependencies
    implementation("androidx.camera:camera-core:1.2.3")
    implementation("androidx.compose.material:material-icons-extended:1.4.3")
    implementation("androidx.camera:camera-camera2:1.2.3")
    implementation("androidx.camera:camera-lifecycle:1.2.3")
    implementation("androidx.camera:camera-view:1.2.3")
    implementation("androidx.compose.ui:ui:1.4.3")
    implementation("androidx.compose.runtime:runtime:1.4.3")
    implementation("androidx.compose.ui:ui-tooling-preview:1.4.3")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.1")

    //CalendarDependencies
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")

    implementation("androidx.compose.ui:ui:1.6.7")
    implementation("androidx.compose.material:material:1.6.7")
    implementation("androidx.compose.ui:ui-tooling-preview:1.6.7")
    debugImplementation("androidx.compose.ui:ui-tooling:1.6.7")

    implementation("io.github.vanpra.compose-material-dialogs:core:0.9.0")
    implementation("io.github.vanpra.compose-material-dialogs:datetime:0.9.0")

    implementation("androidx.compose.material3:material3:1.2.1")

    //PictureUploadDependencies
    implementation("androidx.core:core-ktx:1.16.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.9.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("com.google.firebase:firebase-storage-ktx:20.2.1")
    implementation("com.github.bumptech.glide:glide:4.16.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    implementation(libs.glide)
    kapt(libs.compiler)
}
