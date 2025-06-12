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
    // Preference library for PreferenceManager and SharedPreferences utilities
    implementation("androidx.preference:preference-ktx:1.2.1") // Or the latest stable version

    // --- Room components ---
    val room_version = "2.6.1"
    implementation("androidx.room:room-runtime:$room_version")
    annotationProcessor("androidx.room:room-compiler:$room_version")
    // optional: implementation("androidx.room:room-paging:$room_version")
    // optional: testImplementation("androidx.room:room-testing:$room_version")

    // --- Lifecycle components ---
    val lifecycle_version = "2.7.0"
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:$lifecycle_version")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:$lifecycle_version")

    // RecyclerView
    implementation("androidx.recyclerview:recyclerview:1.3.2")

    // CardView (if not already present for item_ride.xml)
    implementation("androidx.cardview:cardview:1.0.0")
    // --- Google Play Services (Maps and Location) ---
    implementation("com.google.android.gms:play-services-maps:18.2.0")
    implementation("com.google.android.gms:play-services-location:21.0.1")


    // --- Material Design Library ---
    implementation("com.google.android.material:material:1.12.0")


    // --- Core AndroidX UI Libraries (from version catalog) ---
    implementation(libs.appcompat)
    implementation(libs.activity)
    implementation(libs.constraintlayout)


    // --- Testing Libraries (from version catalog) ---
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}