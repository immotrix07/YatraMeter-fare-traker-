plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.project.yatrameter"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.project.yatrameter"
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {
    implementation(libs.preference)
    // --- Room components (cleaned up, using direct string literals only) ---
    val room_version = "2.6.1" // Let's revert to 2.6.1 for stability if 2.9.1 caused issues
    // or use the latest stable version Android Studio suggests.
    // As of June 2025, 2.6.1 is generally stable.

    implementation("androidx.room:room-runtime:$room_version")
    annotationProcessor("androidx.room:room-compiler:$room_version") // For Java projects
    // If you need Room Paging or Testing, uncomment these:
    // implementation("androidx.room:room-paging:$room_version")
    // testImplementation("androidx.room:room-testing:$room_version")

    // --- Lifecycle components (using direct string literals) ---
    // Ensure you have these for LiveData/ViewModel which are good with Room
    val lifecycle_version = "2.7.0" // Use latest stable version here
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:$lifecycle_version")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:$lifecycle_version")

    // RecyclerView
    implementation("androidx.recyclerview:recyclerview:1.3.2") // Or the latest stable version suggested

    // CardView (if not already present for item_ride.xml)
    implementation("androidx.cardview:cardview:1.0.0")
    // --- Google Play Services (Maps and Location) ---
    implementation("com.google.android.gms:play-services-maps:18.2.0")    // Keep this version, or latest stable
    implementation("com.google.android.gms:play-services-location:21.0.1") // Keep this version, or latest stable


    // --- Material Design Library ---
    implementation("com.google.android.material:material:1.12.0") // Keep this version, or latest stable


    // --- Core AndroidX UI Libraries (from version catalog) ---
    // These should continue to work as they are aliases to androidx libraries
    implementation(libs.appcompat)
    implementation(libs.activity)
    implementation(libs.constraintlayout)


    // --- Testing Libraries (from version catalog) ---
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}