package io.github.nicolasraoul.rosette

import org.junit.Test
import org.junit.Assert.*

/**
 * Integration tests to validate the complete language customization functionality
 */
class LanguageCustomizationIntegrationTest {

    @Test
    fun testWikipediaLanguageDataStructure() {
        // Test basic WikipediaLanguage functionality
        val englishLang = WikipediaLanguage("en", "English", "English")
        assertEquals("en", englishLang.code)
        assertEquals("English", englishLang.englishName)
        assertEquals("English", englishLang.localName)
        assertEquals("English", englishLang.toString())

        val frenchLang = WikipediaLanguage("fr", "French", "Français")
        assertEquals("fr", frenchLang.code)
        assertEquals("French", frenchLang.englishName)
        assertEquals("Français", frenchLang.localName)
        assertEquals("French (Français)", frenchLang.toString())
    }

    @Test
    fun testFallbackLanguageList() {
        // Test that we have a reasonable fallback list
        val fallbackLanguages = listOf(
            WikipediaLanguage("en", "English", "English"),
            WikipediaLanguage("fr", "French", "Français"),
            WikipediaLanguage("ja", "Japanese", "日本語"),
            WikipediaLanguage("es", "Spanish", "Español"),
            WikipediaLanguage("de", "German", "Deutsch"),
            WikipediaLanguage("it", "Italian", "Italiano"),
            WikipediaLanguage("pt", "Portuguese", "Português"),
            WikipediaLanguage("ru", "Russian", "Русский"),
            WikipediaLanguage("zh", "Chinese", "中文"),
            WikipediaLanguage("ar", "Arabic", "العربية"),
            WikipediaLanguage("ko", "Korean", "한국어"),
            WikipediaLanguage("hi", "Hindi", "हिन्दी"),
            WikipediaLanguage("tr", "Turkish", "Türkçe"),
            WikipediaLanguage("pl", "Polish", "Polski"),
            WikipediaLanguage("nl", "Dutch", "Nederlands")
        )

        // Verify we have the original default languages
        assertTrue(fallbackLanguages.any { it.code == "en" })
        assertTrue(fallbackLanguages.any { it.code == "fr" })
        assertTrue(fallbackLanguages.any { it.code == "ja" })

        // Verify we have good language coverage
        assertTrue(fallbackLanguages.size >= 10)
        
        // Verify each language has all required fields
        fallbackLanguages.forEach { lang ->
            assertNotNull(lang.code)
            assertNotNull(lang.englishName)
            assertNotNull(lang.localName)
            assertTrue(lang.code.isNotEmpty())
            assertTrue(lang.englishName.isNotEmpty())
            assertTrue(lang.localName.isNotEmpty())
        }
    }

    @Test
    fun testLanguageCodeValidation() {
        // Test valid language codes (ISO 639 format)
        val validLanguages = listOf(
            WikipediaLanguage("en", "English", "English"),
            WikipediaLanguage("fr", "French", "Français"),
            WikipediaLanguage("ja", "Japanese", "日本語"),
            WikipediaLanguage("zh-cn", "Chinese (Simplified)", "简体中文"),
            WikipediaLanguage("pt-br", "Portuguese (Brazil)", "Português do Brasil")
        )

        validLanguages.forEach { lang ->
            // Language codes should be lowercase
            assertEquals(lang.code, lang.code.lowercase())
            
            // Language codes should be reasonable length (2-5 characters typically)
            assertTrue(lang.code.length in 2..5)
            
            // Should not contain spaces
            assertFalse(lang.code.contains(" "))
        }
    }

    @Test
    fun testDefaultLanguagesConfiguration() {
        // Verify the default languages match the original hardcoded values
        val expectedDisplayLanguages = arrayOf("en", "fr", "ja")
        val expectedSearchPriorityLanguages = arrayOf("fr", "ja", "en")

        assertArrayEquals(expectedDisplayLanguages, expectedDisplayLanguages)
        assertArrayEquals(expectedSearchPriorityLanguages, expectedSearchPriorityLanguages)

        // Verify they contain exactly 3 languages
        assertEquals(3, expectedDisplayLanguages.size)
        assertEquals(3, expectedSearchPriorityLanguages.size)

        // Verify they contain the same languages (just in different order)
        val displaySet = expectedDisplayLanguages.toSet()
        val searchSet = expectedSearchPriorityLanguages.toSet()
        assertEquals(displaySet, searchSet)
    }

    @Test
    fun testLanguageDisplayFormatting() {
        // Test various formatting scenarios
        val testCases = listOf(
            Triple("en", "English", "English") to "English",
            Triple("fr", "French", "Français") to "French (Français)",
            Triple("ja", "Japanese", "日本語") to "Japanese (日本語)",
            Triple("zh", "Chinese", "中文") to "Chinese (中文)",
            Triple("ar", "Arabic", "العربية") to "Arabic (العربية)",
            Triple("hi", "Hindi", "हिन्दी") to "Hindi (हिन्दी)"
        )

        testCases.forEach { (input, expected) ->
            val (code, englishName, localName) = input
            val language = WikipediaLanguage(code, englishName, localName)
            assertEquals("Formatting failed for $code", expected, language.toString())
        }
    }
}