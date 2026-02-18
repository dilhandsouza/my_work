# Location App

An Android application that displays your current GPS location with coordinates and address.

## Features

- **Real-time Location Tracking**: Uses Google Play Services FusedLocationProviderClient for accurate location
- **Permissions Handling**: Implements runtime permission requests for Android 6.0+ (API 21+)
- **Address Display**: Converts GPS coordinates to readable addresses using Geocoder
- **Simple UI**: Clean interface with latitude, longitude, and address display

## Requirements

- Android SDK 21 (Android 5.0) or higher
- Android Studio
- Google Play Services

## Permissions

The app requires the following permissions:
- `ACCESS_FINE_LOCATION` - For precise GPS location
- `ACCESS_COARSE_LOCATION` - For approximate location via network
- `INTERNET` - For geocoding services

## How to Build

1. Open the project in Android Studio
2. Sync Gradle files
3. Connect an Android device or start an emulator
4. Click Run or press Shift+F10

## How to Use

1. Launch the app
2. Tap the "Get Current Location" button
3. Grant location permissions when prompted
4. Your current latitude, longitude, and address will be displayed

## Architecture

- **MainActivity.kt**: Main activity handling location retrieval and UI updates
- **activity_main.xml**: Layout file with UI components
- **build.gradle**: Project dependencies including Google Play Services

## Dependencies

- Android AppCompat Library
- Google Play Services Location
- Material Design Components

## Location Sources

The app uses:
- **FusedLocationProviderClient**: High-accuracy location provider
- **Geocoder**: Converts latitude/longitude to addresses

## Troubleshooting

- **"Permission denied"**: Make sure to grant location permissions when the app asks
- **Location not updating**: Enable location services on your device
- **Address shows "--"**: Geocoding may not be available in your region or network is unavailable
