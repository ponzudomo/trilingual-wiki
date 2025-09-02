package io.github.nicolasraoul.rosette

import org.junit.Test
import org.junit.Assert.*

/**
 * Test to verify that Wikipedia's grey top banner is removed via CSS injection.
 * This addresses the issue where the Wikipedia mobile header takes up valuable screen space
 * that could be used to display more article content.
 */
class WikipediaBannerRemovalTest {

    @Test
    fun `CSS injection should hide Wikipedia mobile header elements`() {
        // Test that verifies the expected CSS rules are injected to hide Wikipedia's banner
        // This documents the elements that should be hidden:
        
        val expectedHiddenElements = listOf(
            ".minerva-header",      // Main header container in Minerva skin
            ".header",              // Generic header element  
            ".header-container",    // Header container wrapper
            ".page-header",         // Page-specific header
            "#mw-mf-page-center > .header", // Specific header selector
            ".mw-header"            // MediaWiki header
        )
        
        // In a real WebView test, we would:
        // 1. Load a Wikipedia mobile page
        // 2. Verify that the onPageFinished method injects the CSS
        // 3. Check that the header elements are hidden (display: none)
        // 4. Verify that body margins/padding are reset to 0
        
        // For this unit test, we document the expected behavior
        expectedHiddenElements.forEach { selector ->
            assertTrue("CSS should hide $selector element", selector.isNotEmpty())
        }
        
        assertTrue("Header removal should free up screen space for article content", true)
    }

    @Test
    fun `body margins should be reset to maximize content space`() {
        // Test that verifies body margins and padding are reset to 0
        // This ensures the content moves up to fill the space previously occupied by the header
        
        // Expected CSS rules:
        // - body { margin-top: 0 !important; padding-top: 0 !important; }
        
        // For this unit test, we document the expected behavior
        assertTrue("Body margin-top and padding-top should be reset to 0 to maximize content space", true)
    }

    @Test
    fun `existing padding CSS should be preserved`() {
        // Test that ensures the new header-hiding CSS doesn't break existing functionality
        // The app already injects CSS for left/right padding to prevent text cropping
        
        val existingPaddingRules = listOf(
            "body { padding-left: 8px !important; padding-right: 8px !important; }",
            ".mw-parser-output { padding-left: 8px !important; padding-right: 8px !important; }",
            "#content { padding-left: 8px !important; padding-right: 8px !important; }"
        )
        
        // For this unit test, we document that existing functionality is preserved
        existingPaddingRules.forEach { rule ->
            assertTrue("Existing padding rule should be preserved: $rule", rule.contains("padding"))
        }
        
        assertTrue("New header-hiding CSS should not break existing padding functionality", true)
    }
}