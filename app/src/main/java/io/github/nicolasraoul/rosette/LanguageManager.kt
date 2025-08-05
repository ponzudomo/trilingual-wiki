package io.github.nicolasraoul.rosette

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import kotlinx.coroutines.CancellationException

class LanguageManager(private val context: Context) {
    private val preferences: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val wikipediaApiService = RetrofitClient.wikipediaApiService

    companion object {
        private const val TAG = "LanguageManager"
        private const val PREFS_NAME = "language_preferences"
        private const val KEY_DISPLAY_LANGUAGES = "display_languages"
        private const val KEY_SEARCH_PRIORITY_LANGUAGES = "search_priority_languages"
        
        // Default languages - same as original hardcoded values
        private val DEFAULT_DISPLAY_LANGUAGES = arrayOf("en", "fr", "ja")
        private val DEFAULT_SEARCH_PRIORITY_LANGUAGES = arrayOf("fr", "ja", "en")
    }

    /**
     * Get the current display languages (for the 3 WebViews)
     */
    fun getDisplayLanguages(): Array<String> {
        val savedLanguages = preferences.getString(KEY_DISPLAY_LANGUAGES, null)
        return if (savedLanguages != null) {
            savedLanguages.split(",").toTypedArray()
        } else {
            DEFAULT_DISPLAY_LANGUAGES
        }
    }

    /**
     * Get the current search priority languages (order to search for articles)
     */
    fun getSearchPriorityLanguages(): Array<String> {
        val savedLanguages = preferences.getString(KEY_SEARCH_PRIORITY_LANGUAGES, null)
        return if (savedLanguages != null) {
            savedLanguages.split(",").toTypedArray()
        } else {
            DEFAULT_SEARCH_PRIORITY_LANGUAGES
        }
    }

    /**
     * Save selected languages
     */
    fun saveLanguages(displayLanguages: Array<String>) {
        // For simplicity, use the same order for both display and search priority
        // This can be enhanced later if users need different search order
        preferences.edit()
            .putString(KEY_DISPLAY_LANGUAGES, displayLanguages.joinToString(","))
            .putString(KEY_SEARCH_PRIORITY_LANGUAGES, displayLanguages.joinToString(","))
            .apply()
        Log.d(TAG, "Saved languages: ${displayLanguages.joinToString(", ")}")
    }

    /**
     * Get list of available Wikipedia languages from the API
     */
    suspend fun getAvailableWikipediaLanguages(): List<WikipediaLanguage> {
        try {
            Log.d(TAG, "Making API request to Wikipedia sitematrix...")
            val response = wikipediaApiService.getWikipediaLanguages()
            Log.d(TAG, "API response received: ${response.code()}")
            
            if (response.isSuccessful) {
                val body = response.body()
                Log.d(TAG, "Response body: ${body != null}")
                
                val languages = mutableListOf<WikipediaLanguage>()
                
                body?.sitematrix?.forEach { (key, site) ->
                    // Skip special entries like "count" and "specials"
                    if (key != "count" && key != "specials" && site.code != null && site.name != null) {
                        languages.add(WikipediaLanguage(site.code, site.name, site.localname ?: site.name))
                    }
                }
                
                Log.d(TAG, "Parsed ${languages.size} languages from sitematrix")
                // Sort by English name for better UX
                return languages.sortedBy { it.englishName }
            } else {
                Log.e(TAG, "API request failed with code: ${response.code()}, message: ${response.message()}")
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.e(TAG, "Failed to fetch Wikipedia languages: ${e.message}", e)
        }
        
        // Return empty list if API fails
        Log.w(TAG, "Returning empty language list due to API failure")
        return emptyList()
    }

    /**
     * Check if user has configured custom languages
     */
    fun hasCustomLanguages(): Boolean {
        return preferences.contains(KEY_DISPLAY_LANGUAGES)
    }
}

/**
 * Data class representing a Wikipedia language
 */
data class WikipediaLanguage(
    val code: String,           // Language code (e.g., "en", "fr", "ja")
    val englishName: String,    // English name (e.g., "English", "French", "Japanese")
    val localName: String       // Local name (e.g., "English", "Français", "日本語")
) {
    override fun toString(): String {
        return if (localName != englishName) {
            "$englishName ($localName)"
        } else {
            englishName
        }
    }
}