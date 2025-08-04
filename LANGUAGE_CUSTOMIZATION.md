# Language Customization Feature Implementation

## Overview
This implementation adds the ability for users to customize the three languages displayed in the trilingual Wikipedia viewer, replacing the hardcoded English, French, and Japanese languages.

## Key Features

### 1. Dynamic Language Selection
- Users can select any 3 languages from the complete list of Wikipedia language editions
- Languages are fetched dynamically from Wikipedia's sitematrix API
- Fallback list provided if API is unavailable

### 2. Settings Dialog
- Accessible via menu option in the top bar
- Clean, intuitive interface for language selection
- Shows both English and local names for languages (e.g., "French (Français)")
- Real-time validation (exactly 3 languages required)

### 3. Persistent Storage
- User preferences stored using SharedPreferences
- Survives app restarts and device reboots
- Backward compatible with existing installations (defaults to EN/FR/JA)

## Technical Implementation

### New Classes

#### LanguageManager
- Central class for language preference management
- Handles API calls to fetch Wikipedia language list
- Manages SharedPreferences storage and retrieval
- Provides fallback functionality if API calls fail

#### LanguageSettingsDialog
- DialogFragment for language selection UI
- Two RecyclerViews: available languages and selected languages
- Real-time updates and validation
- Loading indicators and error handling

#### WikipediaLanguage
- Data class representing a Wikipedia language
- Contains language code, English name, and local name
- Smart toString() method for display purposes

### Modified Classes

#### MainActivity
- Removed hardcoded language arrays
- Added LanguageManager integration
- Added menu support for accessing settings
- Dynamic WebView recreation when languages change

#### WikipediaApiService
- Added Wikipedia sitematrix API endpoint
- New data classes for API response parsing

## API Integration

### Wikipedia Sitematrix API
- Endpoint: `https://en.wikipedia.org/w/api.php?action=sitematrix&format=json&smtype=language`
- Returns comprehensive list of all Wikipedia language editions
- Provides both English and local language names
- Used to populate the language selection dialog

## User Experience

### First Time Users
- App starts with default EN/FR/JA languages
- Menu option clearly visible for customization
- No forced configuration required

### Returning Users
- Previous language selections automatically restored
- Can change languages at any time via settings
- Immediate effect after saving new selection

## Backward Compatibility
- Existing users see no change in behavior initially
- Default languages remain EN/FR/JA
- No data migration required
- Gradual adoption as users discover the feature

## Error Handling
- Graceful fallback if Wikipedia API is unavailable
- Validation prevents invalid language selections
- Clear error messages for users
- Maintains functionality even with network issues

## Testing
- Unit tests for LanguageManager core functionality
- Tests for default language behavior
- Tests for custom language persistence
- Tests for WikipediaLanguage string representation

## Future Enhancements
- Search priority order customization
- Language usage statistics
- Quick language switching
- Import/export of language preferences
- Custom language ordering within the 3 selected languages