// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    // Define the Android Application Plugin version here consistently
    id("com.android.application") version "8.5.2" apply false // Set to 8.5.2
    // Define the Android Library Plugin version here if needed for libraries
    id("com.android.library") version "8.5.2" apply false      // Set to 8.5.2

    // Keep Kotlin plugin version consistent if you use Kotlin
    id("org.jetbrains.kotlin.android") version "1.9.0" apply false // Example Kotlin version
}