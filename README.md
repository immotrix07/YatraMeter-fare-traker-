# YatraMeter: Your Smart Ride Fare Tracker 🚕📊

**YatraMeter is a comprehensive Android application designed to provide transparent and accurate ride fare estimations based on live GPS tracking and customizable fare structures. No more guessing your ride cost!**

This app allows users to set their own fare parameters (base fare, per-kilometer, per-minute, waiting charges, and multipliers) and receive real-time updates on their ride cost. It also features detailed ride history, public fare rate charts, and extensive customization options.

## ✨ Features

-   **Live Fare Calculation:** Get instant, real-time fare updates as you travel, based on distance, time, and waiting.
-   **Dynamic Map Tracking:** Visualize your journey on an interactive map. See your live location, track your route with a drawn polyline, mark start/end points, and view live traffic.
-   **Customizable Fare Settings:** Easily configure your own fare rates directly within the app, allowing for personalized cost estimation.
-   **Comprehensive Ride History:** All your past rides are automatically saved. View a detailed log with summaries, breakdowns, and statistics for each journey.
-   **PDF Export:** Generate and save detailed ride summaries as PDF documents directly from the app.
-   **Smart App Settings:**
    -   **Default Fare Presets:** Save and load your most-used fare structures.
    -   **Unit Preferences:** Switch between Kilometers (km) and Miles (mi).
    -   **Theme Selection:** Choose between light and dark themes.
    -   **Fare Lock PIN:** Protect sensitive settings with a secure PIN.
    -   **Bilingual Support:** Seamlessly switch between English and Hindi.
-   **Public Fare Charts:** Access and compare official public transport fare rates for various cities.
-   **Offline Functionality:** Core fare calculation works without an internet connection.

## 📸 Screenshots



![Welcome Screen]([https://raw.githubusercontent.com/immotrix07/YatraMeter-fare-traker-/main/screenshots/welcome_screen.png](https://github.com/immotrix07/YatraMeter-fare-traker-/blob/master/screenshots/additional_settings.jpg)
![Set Fare Details Screen](https://raw.githubusercontent.com/immotrix07/YatraMeter-fare-traker-/main/screenshots/set_fare_details_screen.png)
![Live Ride Screen](https://raw.githubusercontent.com/immotrix07/YatraMeter-fare-traker-/main/screenshots/live_ride_screen.png)
![Ride Finish Screen](https://raw.githubusercontent.com/immotrix07/YatraMeter-fare-traker-/main/screenshots/ridde_finish_screen.png)
![Ride History Screen](https://raw.githubusercontent.com/immotrix07/YatraMeter-fare-traker-/main/screenshots/ride_history_screen.png)
![Settings Screen](https://raw.githubusercontent.com/immotrix07/YatraMeter-fare-traker-/main/screenshots/settings_screen.png)
![Additional Settings](https://raw.githubusercontent.com/immotrix07/YatraMeter-fare-traker-/main/screenshots/additional_settings.png)
![Select Language Dialog](https://raw.githubusercontent.com/immotrix07/YatraMeter-fare-traker-/main/screenshots/select_language.png)


## 🛠️ Technologies Used

-   **Language:** Java
-   **IDE:** Android Studio
-   **UI Toolkit:** Android View System
-   **Database:** Room Persistence Library (SQLite)
-   **Location Services:** Google Play Services Fused Location Provider
-   **Mapping:** Google Maps SDK for Android
-   **Dependency Management:** Gradle Kotlin DSL
-   **Persistence:** SharedPreferences

## 🚀 Getting Started

To get a local copy up and running, follow these simple steps.

### Prerequisites

-   Android Studio (Bumblebee 2021.1.1 or newer recommended)
-   Android SDK Platform 31 (or higher, as per `compileSdk`)
-   A physical Android device or emulator with Google Play Services.
-   **Google Maps API Key:** You will need to obtain your own API key and enable "Maps SDK for Android" in Google Cloud Console. Follow the instructions [here](https://developers.google.com/maps/documentation/android-sdk/start).

### Installation

1.  **Clone the repository:**
    ```bash
    git clone [https://github.com/immotrix07/YatraMeter-fare-traker-.git](https://github.com/immotrix07/YatraMeter-fare-traker-.git)
    ```
2.  **Open in Android Studio:**
    Open the cloned project in Android Studio.
3.  **Add your Google Maps API Key:**
    Open `app/src/main/AndroidManifest.xml` and replace `"YOUR_API_KEY_HERE"` with your actual API key:
    ```xml
    <meta-data
        android:name="com.google.android.geo.API_KEY"
        android:value="YOUR_API_KEY_HERE" />
    ```
4.  **Sync Gradle:**
    Click `Sync Project with Gradle Files` in Android Studio.
5.  **Run the App:**
    Build and run the app on an emulator or a physical device.

## 🤝 Contributing

Contributions are what make the open-source community such an amazing place to learn, inspire, and create. Any contributions you make are **greatly appreciated**.

1.  Fork the Project
2.  Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3.  Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4.  Push to the Branch (`git push origin feature/AmazingFeature`)
5.  Open a Pull Request

## 📄 License

Distributed under the MIT License. See `LICENSE` for more information.

## 📞 Contact

 Name -  devxsachin
linked-in -  https://rb.gy/gph4v8
Project Link: [https://github.com/immotrix07/YatraMeter-fare-traker-](https://github.com/immotrix07/YatraMeter-fare-traker-)
