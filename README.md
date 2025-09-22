# RecapMe

RecapMe is a powerful Android application that transforms your WhatsApp chat exports into intelligent, AI-generated summaries. Whether you need to quickly catch up on group conversations, archive important discussions, or organize your chat history, RecapMe helps you extract meaningful insights from your WhatsApp conversations with customizable summary styles and smart organization features.

> **Backend Repository**: The API backend for this app is available at [RecapMe-Backend](https://github.com/nicholasjano/RecapMe-Backend)

## Features

### AI-Powered Chat Summarization
- **Multiple Summary Styles**: Choose from concise, detailed, bullet-point, casual, or formal recap formats
- **Intelligent Processing**: Advanced AI analyzes conversation context, participants, and key topics
- **Participant Recognition**: Automatically identifies and lists conversation participants

### Smart Organization
- **Category System**: Organize recaps with customizable categories (Personal, Work, + 3 custom categories)
- **Color-Coded Labels**: Visual organization with customizable category colors
- **Star System**: Mark important recaps as favorites for quick access
- **Search Functionality**: Find specific recaps with powerful search capabilities

### Analytics & Insights
- **Usage Statistics**: Track weekly recap counts, starred items, and total summaries
- **Time-Based Filtering**: View recaps from different time periods
- **Visual Dashboard**: Clean overview of your recap library

### Modern User Experience
- **Material 3 Design**: Contemporary Android design language with dynamic theming
- **Intuitive Navigation**: Bottom navigation with Home, Guide, and Settings screens
- **Responsive UI**: Optimized for various screen sizes and orientations
- **Accessibility Ready**: Built with Android accessibility guidelines in mind

## Architecture

RecapMe follows modern Android development best practices with a clean MVVM architecture:

### Tech Stack
- **Language**: Kotlin
- **UI Framework**: Jetpack Compose with Material 3
- **Architecture**: MVVM (Model-View-ViewModel)
- **Networking**: Retrofit + OkHttp + Gson
- **Data Storage**: DataStore Preferences
- **Navigation**: Compose Navigation
- **Dependency Injection**: Manual DI with repository pattern
- **File Handling**: DocumentFile API

### Project Structure
```
app/src/main/java/com/example/recapme/
├── data/
│   ├── api/              # API service and models
│   ├── models/           # Data classes (Recap, Settings, ChatMessage)
│   ├── repository/       # Repository pattern implementation
│   └── *DataStore.kt     # Data persistence layer
├── navigation/           # Compose navigation setup
├── ui/
│   ├── components/       # Reusable UI components
│   ├── screens/          # Main app screens (Home, Guide, Settings)
│   ├── theme/           # Material 3 theming
│   └── viewmodels/      # ViewModels for business logic
└── MainActivity.kt       # Main activity
```

### Key Components
- **RecapRepository**: Handles API communication with backend services
- **DataStore Classes**: Manage settings, recap data, and category persistence
- **ViewModels**: Business logic and state management for each screen
- **Compose Screens**: Modern declarative UI with Material 3 components

## Setup & Installation

### Prerequisites
- Android Studio Hedgehog (2023.1.1) or later
- Android SDK 24+ (Android 7.0)
- Kotlin 1.9+
- JDK 11+

### Development Setup

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd RecapMe
   ```

2. **Configure API credentials**

   Copy the template file and add your API credentials:
   ```bash
   cp gradle.properties.template gradle.properties
   ```

   Edit `gradle.properties` and replace the placeholder values:
   ```properties
   RECAP_API_KEY=your_actual_api_key_here
   RECAP_API_URL=your_api_endpoint_url
   ```

3. **Build the project**
   ```bash
   ./gradlew assembleDebug
   ```

4. **Run on device/emulator**
   ```bash
   ./gradlew installDebug
   ```

### Release Build

1. **Build release APK**
   ```bash
   ./gradlew assembleRelease
   ```

2. **Build Android App Bundle (for Play Store)**
   ```bash
   ./gradlew bundleRelease
   ```

## Testing

The project includes comprehensive test coverage across multiple layers:

### Unit Tests
```bash
# Run all unit tests
./gradlew testDebugUnitTest

# Run specific test class
./gradlew testDebugUnitTest --tests="RecapRepositoryTest"
```

### Instrumented Tests
```bash
# Run Android instrumented tests
./gradlew connectedAndroidTest
```

### Test Coverage
- **Repository Layer**: API communication and data mapping
- **ViewModels**: Business logic and state management
- **Data Layer**: DataStore operations and data persistence
- **Service Layer**: WhatsApp file processing and validation

## Usage Guide

### Importing WhatsApp Chats

1. **Export from WhatsApp**
   - Open the desired chat in WhatsApp
   - Tap the three dots menu → More → Export chat
   - Select "Without Media" to keep file size manageable
   - Save to Google Drive or your preferred location

2. **Import to RecapMe**
   - Open RecapMe and tap the "+" button on the Home screen
   - Select your exported WhatsApp file (supports .txt and .zip files up to 20MB)
   - RecapMe will automatically process the chat and generate a summary

3. **Organize Your Recaps**
   - Assign categories to organize your recaps
   - Star important conversations for quick access
   - Use the search function to find specific content

### Customizing Summary Styles

Navigate to Settings to customize how RecapMe generates your summaries:
- **Concise**: Brief, to-the-point summaries
- **Detailed**: Comprehensive overviews with context
- **Bullet**: Organized bullet-point format
- **Casual**: Conversational, friendly tone
- **Formal**: Professional, structured summaries

## Privacy & Data Usage

### Data Processing
- **Secure Transmission**: Chat content is securely transmitted to the RecapMe backend API over HTTPS
- **Transient Processing**: Data is processed transiently to generate summaries and is not permanently stored on servers
- **User Control**: Users maintain full control over their data and can delete recaps at any time

### Permissions
- **Read External Storage**: Required for accessing exported WhatsApp files
- **Read Media Documents**: Access to document files on Android 13+
- **Internet**: Communication with the recap generation service

### Security Features
- API key protection with secure header transmission
- Input validation and sanitization
- Network timeouts and error handling
- No logging of sensitive user data

## Troubleshooting

### Common Issues

**File Import Problems**
- Ensure your WhatsApp export is under 20MB
- Use "Without Media" option when exporting from WhatsApp
- Check that the file format is supported (.txt or .zip)

**Network Issues**
- Verify internet connection is stable
- Check if the API service is accessible
- Review API key configuration in gradle.properties

**App Performance**
- Large chat files may take longer to process
- Ensure sufficient device storage is available
- Close other apps if experiencing memory issues

### Error Messages
- **"File too large"**: WhatsApp export exceeds 20MB limit
- **"Network error"**: Check internet connection and API configuration
- **"Invalid file format"**: Ensure you're importing a valid WhatsApp export

## Build Configuration

### Build Types
- **Debug**: Development builds with logging and debugging enabled
- **Release**: Optimized production builds with ProGuard obfuscation

### Dependencies
Key dependencies include:
- Jetpack Compose BOM for UI components
- Retrofit + OkHttp for networking
- DataStore for data persistence
- Material 3 for modern design
- Testing libraries (JUnit, Mockito, Espresso)

### Gradle Commands
```bash
# Clean build
./gradlew clean

# Run lint checks
./gradlew lintDebug

# Generate dependency report
./gradlew dependencies
```

## License

This project is licensed under the Apache License 2.0. See the [LICENSE](LICENSE) file for details.

```
Copyright 2025 Nicholas Jano

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```