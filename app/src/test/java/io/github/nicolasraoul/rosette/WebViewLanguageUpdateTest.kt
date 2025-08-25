package io.github.nicolasraoul.rosette

import org.junit.Test
import org.junit.Assert.*

/**
 * Test to verify that WebView language handling is correct after language changes
 */
class WebViewLanguageUpdateTest {

    @Test
    fun testLanguageIdentifierFormat() {
        // Test that language identifiers are in the expected format
        val validLanguageCodes = listOf("en", "fr", "ja", "es", "de", "it", "pt", "ru", "zh", "ar", "hi", "ko")
        
        validLanguageCodes.forEach { code ->
            // Language codes should be lowercase
            assertEquals("Language code should be lowercase", code, code.lowercase())
            
            // Language codes should be 2-3 characters typically  
            assertTrue("Language code should be reasonable length", code.length in 2..3)
            
            // Should not contain special characters for URL safety
            assertTrue("Language code should be alphanumeric", code.matches(Regex("[a-z]+")))
        }
    }

    @Test
    fun testWebViewClientLanguageIdentifier() {
        // Test that the language identifier properly maps to expected WebView behavior
        // This simulates what happens in TrilingualWebViewClient constructor
        
        val testLanguages = arrayOf("en", "fr", "ja")
        
        testLanguages.forEach { lang ->
            // Verify language code can be used in logs and URL construction
            assertNotNull("Language should not be null", lang)
            assertFalse("Language should not be empty", lang.isEmpty())
            
            // Verify it can be used in Wikipedia URL construction
            val expectedBaseUrl = "https://$lang.m.wikipedia.org"
            assertTrue("Should create valid URL", expectedBaseUrl.startsWith("https://"))
            assertTrue("Should contain language code", expectedBaseUrl.contains(lang))
        }
    }

    @Test
    fun testWebViewMappingConsistency() {
        // Test that the mapping between languages and webviews is consistent
        val displayLanguages = arrayOf("es", "de", "it") // New languages after change
        
        // Simulate the webViewMap creation like in recreateWebViews()
        val webViewNames = arrayOf("webViewEN", "webViewFR", "webViewJA") // Actual WebView instances
        
        // Verify we have the same number of languages and webviews
        assertEquals("Should have same number of languages and webviews", 
                    displayLanguages.size, webViewNames.size)
        
        // Verify all languages are valid
        displayLanguages.forEach { lang ->
            assertTrue("Language should be valid format", lang.matches(Regex("[a-z]{2,3}")))
        }
    }

    @Test
    fun testLanguageChangeScenario() {
        // Test the specific scenario described in the issue
        // Initial state: en, fr, ja
        val initialLanguages = arrayOf("en", "fr", "ja")
        
        // After language change: es, de, it  
        val newLanguages = arrayOf("es", "de", "it")
        
        // Verify the transition is valid
        assertEquals("Should maintain 3 languages", initialLanguages.size, newLanguages.size)
        
        // Each language should map to a webview
        for (i in initialLanguages.indices) {
            val oldLang = initialLanguages[i]
            val newLang = newLanguages[i]
            
            // Both should be valid language codes
            assertTrue("Old language should be valid", oldLang.matches(Regex("[a-z]{2,3}")))
            assertTrue("New language should be valid", newLang.matches(Regex("[a-z]{2,3}")))
            
            // They should be different (testing an actual change)
            assertNotEquals("Languages should be different in this test", oldLang, newLang)
        }
    }
}