# CitySearch

A modern Android application for searching and exploring cities around the world. The app features a
clean architecture, responsive design, and integration with Google Maps.

<p align="center">
  <img src="https://github.com/user-attachments/assets/2cc3f466-d70e-4cf8-8f1b-efdb5cb18832" width="300"/>
  <img src="https://github.com/user-attachments/assets/e5733f3d-2bf1-4bd8-b454-47d049c316fd" width="300"/>
  <img src="https://github.com/user-attachments/assets/9ab5b544-26be-4705-a1b4-d8e5ae9c3121" height="300"/>
</p>

## Observations

- During the challenge, I misunderstood part of the requirements and initially thought I needed to add more data to the cities. That’s why I implemented a service to fetch their polygons. Later, I realized that this information was intended for the detail screen, but since I had already invested time in that implementation, I decided to keep it rather than refocus on improving the detail view.

- There are several areas that could be optimized, such as using WorkManager to fetch and process cities (see details below), adding an app icon, implementing a strings provider, decoupling features from Google’s SDK, improving dark theme handling, and refining overall design.

## Features

- **City Search**: Search for cities by name with real-time filtering
- **City Details**: View detailed information about each city
- **Interactive Map**: Visualize city locations on Google Maps
- **Adaptive Layout**: Responsive UI that adapts to different screen sizes and orientations
- **Offline Support**: Access previously viewed cities without an internet connection

## Architecture

This application follows Clean Architecture principles with a modular structure:

- **App**: Contains the application entry point and dependency injection setup
- **Core**: Core utilities like json helper for download/parsing
- **Data**: Data sources and repositories
    - **Local**: Room database for local storage
    - **Remote**: Retrofit for network operations
    - **Repository**: Implementation of repository pattern
- **Domain**: Business models
- **Feature**: Feature-specific implementations
    - **Map**: Google Maps integration with polygons
    - **Search**: City search and details
    - **Adaptive**: Responsive layout handling
- **Navigation**: Navigation components and routing
- **UI**: Composable UI components and theming

## Technologies

- Kotlin
- Jetpack Compose
- Coroutines for asynchronous operations
- Hilt for dependency injection
- Room for local database
- Retrofit for network requests
- Google Maps SDK
- Jetpack Navigation
- MVI architecture pattern
- Kotlin Serialization
- Moshi
- JUnit for unit tests
- Roborazzi for UI tests

## Setup

### Prerequisites

- Android Studio Hedgehog or newer
- Minimum SDK 24 (Android 7.0)
- JDK 11

### Google Maps API Key

To run this application, you'll need to obtain a Google Maps API key:

1. Go to the [Google Cloud Console](https://console.cloud.google.com/)
2. Create a new project (or select an existing one)
3. Navigate to APIs & Services > Dashboard
4. Click on "+ ENABLE APIS AND SERVICES"
5. Search for "Maps SDK for Android" and enable it
6. Go to APIs & Services > Credentials
7. Create an API key (restrict it to Android apps for production use)
8. Add the following to your `local.properties` file:
   ```
   MAPS_API_KEY=your_api_key_here
   ```

### Build and Run

1. Clone the repository
2. Add your Google Maps API key to `local.properties`
3. Sync the project with Gradle files
4. Run the app on an emulator or device

## Data Loading Mechanism

The application implements a sophisticated data loading mechanism to handle a large JSON dataset
efficiently while preventing Out-Of-Memory exceptions:

### Streaming Approach

- Data is downloaded as a stream and saved directly to a local file
- The streaming approach avoids loading the entire JSON into memory at once
- JSON is processed incrementally and stored in the database

### Resilience Workflow

The app uses the following workflow to ensure data integrity and handle interruptions:

1. **Check if data exists locally**

- If YES: Verify if it's the same version as remote
    - If SAME VERSION: Verify if properly indexed in database
        - If INDEXED: Complete loading
        - If NOT INDEXED: Reindex data and complete loading
    - If DIFFERENT VERSION: Replace local file, index data, and complete loading
- If NO: Download file, index data, and complete loading

### Concurrent Processing

- Database entries are accessible as they are being saved
- Users can interact with the app while data processing continues in background
- Changes in the remote JSON are detected and handled appropriately

This approach ensures the app remains responsive even when handling large datasets and provides
resilience against interruptions like app closure during data processing.

## Adaptability

The application is designed with adaptability in mind:

- **Screen Sizes**: UI adjusts to different device sizes from phones to tablets
- **Orientation**: Supports both portrait and landscape orientations
- **Adaptive Layout**: Uses different layout strategies based on available screen space
- **Dark/Light Themes**: Supports system theme preferences

## Documentation and sources

- https://medium.com/androiddevelopers/jetnews-for-every-screen-4d8e7927752
- https://github.com/SantiagoFlynnUTN/CoinConverter
- https://github.com/takahirom/roborazzi

## Requirements

[Mobile Challenge - Engineer - v0.8.pdf](https://github.com/user-attachments/files/20824531/Mobile.Challenge.-.Engineer.-.v0.8.pdf)


