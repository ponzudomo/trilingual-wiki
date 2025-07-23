# Trilingual Wikipedia Viewer

## 1. Overview

The **Trilingual Wikipedia Viewer** is an Android app designed to display Wikipedia articles side-by-side in three languages: Japanese, French, and English. Users can search for an article title, and the tool will intelligently find the article on one of the wikis and then load its corresponding translations in the other two panels. This provides a powerful way to compare information and language across different versions of Wikipedia.

---

## 2. User Guide

Using the tool is straightforward:

1.  **Enter a Search Term**: Type the title of any Wikipedia article into the search bar at the top of the app.
2.  Press the "Search" button or hit the `Enter` key.
3.  **View the Results**: The application will perform the following search sequence:
    * It first looks for the article on **French Wikipedia**.
    * If not found, it tries **Japanese Wikipedia**.
    * If still not found, it tries **English Wikipedia**.
4.  **Displaying Articles**:
    * Once a source article is found, it is loaded in its corresponding language panel.
    * The application then fetches the official language links for that article and loads the corresponding pages in the other two panels.
    * If a translation for a specific language doesn't exist, a "Not Found" message is displayed in that panel.

A status message below the search bar provides real-time feedback on the search process.


---

## Wikipedia API Calls in the Trilingual Viewer

The application's core functionality relies on two distinct API calls made to the public MediaWiki API. Each call serves a specific purpose in the search and retrieval process.

### 1\. Finding the Article (and Handling Redirects)

This is the first and most critical API call. Its job is to verify that an article for the user's search term exists on a specific language's Wikipedia and to find its *true* title, even if the user's term is a redirect.

**Purpose:** To confirm a page exists and get its canonical title.

**Function in Code:** `getFinalArticleTitle(lang, title)`

**Example API Call URL:**
(Searching for "Eiffel Tower" on the French Wikipedia)

```
https://fr.wikipedia.org/w/api.php?action=query&titles=Eiffel_Tower&format=json&origin=*&redirects=1
```

#### Parameter Breakdown:

| Parameter | Value | Purpose |
| :--- | :--- | :--- |
| `action` | `query` | The fundamental action we are performing. We are querying the Wikipedia database for information. |
| `titles` | `Eiffel_Tower` | The subject of our query. This is the article title we are searching for. The JavaScript code replaces spaces with underscores to match URL formatting. |
| `format` | `json` | Specifies that the API response should be in JSON format, which is easily parsed by JavaScript. |
| `origin` | `*` | This is a required parameter for Cross-Origin Resource Sharing (CORS). It tells the Wikipedia server that it's safe to serve the request to a script running on any domain (like the one hosting our tool). |
| `redirects` | `1` | **This is a crucial parameter.** It instructs the API to automatically follow any redirects. For example, searching for "Eiffel Tower" on French Wikipedia actually redirects to the page "Tour Eiffel". This parameter ensures the API returns the information for the final page, "Tour Eiffel", not the redirect page. |

#### Example Response (Simplified):

If the article **is found**, the API returns a `pages` object with a numeric page ID. The title under this ID is the final, canonical title.

```json
{
  "query": {
    "pages": {
      "57924": {
        "pageid": 57924,
        "title": "Tour Eiffel"
      }
    }
  }
}
```

If the article **is not found**, the page ID will be `-1`.

```json
{
  "query": {
    "pages": {
      "-1": {
        "missing": ""
      }
    }
  }
}
```

-----

### 2\. Fetching Language Links

Once a valid source article has been found, this second API call is made. Its sole purpose is to retrieve the list of officially registered translations for that specific article.

**Purpose:** To get a list of all translations for a confirmed article.

**Function in Code:** `getLangLinksForTitle(lang, title)`

**Example API Call URL:**
(Using the confirmed title "Tour Eiffel" from the previous call)

```
https://fr.wikipedia.org/w/api.php?action=query&prop=langlinks&titles=Tour_Eiffel&lllimit=500&format=json&origin=*
```

#### Parameter Breakdown:

| Parameter | Value | Purpose |
| :--- | :--- | :--- |
| `action` | `query` | Same as before, we are querying the database. |
| `prop` | `langlinks` | This is the key parameter. Instead of asking for general page info, we are requesting a specific **property** of the page: its inter-language links. |
| `titles` | `Tour_Eiffel` | The subject of our query. It is essential to use the **exact final title** returned from the first API call to ensure accuracy. |
| `lllimit` | `500` | The `ll` stands for "langlinks". This parameter sets the maximum number of language links to return. `500` is a safe, high number to ensure we get all available translations for any given article. |
| `format` | `json` | We want the response in JSON format. |
| `origin` | `*` | The required CORS parameter. |

#### Example Response (Simplified):

The API returns the page object, which now contains a `langlinks` array. Each object in the array represents one translation.

```json
{
  "query": {
    "pages": {
      "57924": {
        "pageid": 57924,
        "title": "Tour Eiffel",
        "langlinks": [
          {
            "lang": "en",
            "*": "Eiffel Tower"
          },
          {
            "lang": "ja",
            "*": "エッフェル塔"
          },
          {
            "lang": "es",
            "*": "Torre Eiffel"
          }
        ]
      }
    }
  }
}
```

The application code then iterates through this array, looks for the entries where `lang` is `en` and `ja`, and uses the corresponding title from the `*` property to construct the URL for the other panels.
