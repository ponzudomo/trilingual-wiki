# Wikipedia Sitematrix API Example

## API Endpoint
```
https://en.wikipedia.org/w/api.php?action=sitematrix&format=json&smtype=language
```

## Sample Response Structure
```json
{
  "sitematrix": {
    "0": {
      "code": "aa",
      "name": "Afar",
      "localname": "Afar"
    },
    "1": {
      "code": "ab",
      "name": "Abkhazian",
      "localname": "Аҧсуа"
    },
    "2": {
      "code": "ace",
      "name": "Acehnese",
      "localname": "Acèh"
    },
    "3": {
      "code": "af",
      "name": "Afrikaans",
      "localname": "Afrikaans"
    },
    "4": {
      "code": "ak",
      "name": "Akan",
      "localname": "Akan"
    },
    "5": {
      "code": "als",
      "name": "Alemannisch",
      "localname": "Alemannisch"
    },
    "6": {
      "code": "am",
      "name": "Amharic",
      "localname": "አማርኛ"
    },
    "7": {
      "code": "an",
      "name": "Aragonese",
      "localname": "Aragonés"
    },
    "8": {
      "code": "ang",
      "name": "Anglo-Saxon",
      "localname": "Ænglisc"
    },
    "9": {
      "code": "ar",
      "name": "Arabic",
      "localname": "العربية"
    },
    "10": {
      "code": "arc",
      "name": "Aramaic",
      "localname": "ܐܪܡܝܐ"
    }
  }
}
```

## Language List Implementation

The LanguageManager class processes this response to extract:
- `code`: Language code (e.g., "en", "fr", "ja")
- `name`: English name (e.g., "English", "French", "Japanese")  
- `localname`: Native name (e.g., "English", "Français", "日本語")

This provides users with a comprehensive list of all Wikipedia language editions to choose from, ensuring they can select languages that actually have Wikipedia content available.

## Fallback Languages

If the API is unavailable, the app provides a curated list of 15 common languages:
- English, French, Japanese (current defaults)
- Spanish, German, Italian, Portuguese, Russian
- Chinese, Arabic, Korean, Hindi, Turkish, Polish, Dutch

This ensures the app remains functional even with network issues while providing good language coverage.