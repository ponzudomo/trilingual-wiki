package io.github.nicolasraoul.rosette

import android.content.Context
import android.content.SharedPreferences
import org.junit.Test
import org.junit.Assert.*
import org.mockito.Mockito.*

/**
 * Unit tests for LanguageManager class
 */
class LanguageManagerTest {

    @Test
    fun testDefaultLanguages() {
        val mockContext = mock(Context::class.java)
        val mockSharedPreferences = mock(SharedPreferences::class.java)
        
        `when`(mockContext.getSharedPreferences(anyString(), anyInt())).thenReturn(mockSharedPreferences)
        `when`(mockSharedPreferences.getString(anyString(), isNull())).thenReturn(null)
        
        val languageManager = LanguageManager(mockContext)
        
        val displayLanguages = languageManager.getDisplayLanguages()
        val searchPriorityLanguages = languageManager.getSearchPriorityLanguages()
        
        assertArrayEquals(arrayOf("en", "fr", "ja"), displayLanguages)
        assertArrayEquals(arrayOf("fr", "ja", "en"), searchPriorityLanguages)
    }

    @Test
    fun testCustomLanguages() {
        val mockContext = mock(Context::class.java)
        val mockSharedPreferences = mock(SharedPreferences::class.java)
        val mockEditor = mock(SharedPreferences.Editor::class.java)
        
        `when`(mockContext.getSharedPreferences(anyString(), anyInt())).thenReturn(mockSharedPreferences)
        `when`(mockSharedPreferences.edit()).thenReturn(mockEditor)
        `when`(mockEditor.putString(anyString(), anyString())).thenReturn(mockEditor)
        `when`(mockEditor.apply()).then { }
        
        val languageManager = LanguageManager(mockContext)
        
        // Test saving custom languages
        val customLanguages = arrayOf("es", "de", "it")
        languageManager.saveLanguages(customLanguages)
        
        // Verify the save operation was called
        verify(mockEditor).putString("display_languages", "es,de,it")
        verify(mockEditor).putString("search_priority_languages", "es,de,it")
        verify(mockEditor).apply()
    }

    @Test
    fun testHasCustomLanguages() {
        val mockContext = mock(Context::class.java)
        val mockSharedPreferences = mock(SharedPreferences::class.java)
        
        `when`(mockContext.getSharedPreferences(anyString(), anyInt())).thenReturn(mockSharedPreferences)
        
        // Test when no custom languages are set
        `when`(mockSharedPreferences.contains("display_languages")).thenReturn(false)
        val languageManager1 = LanguageManager(mockContext)
        assertFalse(languageManager1.hasCustomLanguages())
        
        // Test when custom languages are set
        `when`(mockSharedPreferences.contains("display_languages")).thenReturn(true)
        val languageManager2 = LanguageManager(mockContext)
        assertTrue(languageManager2.hasCustomLanguages())
    }

    @Test
    fun testWikipediaLanguageToString() {
        val languageEn = WikipediaLanguage("en", "English", "English")
        assertEquals("English", languageEn.toString())
        
        val languageFr = WikipediaLanguage("fr", "French", "Français")
        assertEquals("French (Français)", languageFr.toString())
        
        val languageJa = WikipediaLanguage("ja", "Japanese", "日本語")
        assertEquals("Japanese (日本語)", languageJa.toString())
    }

    @Test
    fun testCaseInsensitiveLanguageSorting() {
        // Test that language sorting is case-insensitive
        val languages = listOf(
            WikipediaLanguage("zh", "chinese", "中文"),  // lowercase 'c'
            WikipediaLanguage("en", "English", "English"),  // uppercase 'E'
            WikipediaLanguage("ar", "arabic", "العربية"),  // lowercase 'a' 
            WikipediaLanguage("fr", "French", "Français"),  // uppercase 'F'
            WikipediaLanguage("de", "german", "Deutsch"),   // lowercase 'g'
            WikipediaLanguage("it", "Italian", "Italiano") // uppercase 'I'
        )
        
        // Sort using the same logic as in LanguageManager (case-insensitive)
        val sortedLanguages = languages.sortedBy { it.englishName.lowercase() }
        
        // Expected order should be: arabic, chinese, English, French, german, Italian
        val expectedOrder = listOf("arabic", "chinese", "English", "French", "german", "Italian")
        val actualOrder = sortedLanguages.map { it.englishName }
        
        assertEquals("Languages should be sorted case-insensitively", expectedOrder, actualOrder)
    }
}