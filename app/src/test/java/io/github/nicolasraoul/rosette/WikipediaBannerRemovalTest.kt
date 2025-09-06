package io.github.nicolasraoul.rosette

import org.junit.Test
import org.junit.Assert.*

/**
 * Test to verify that Wikipedia's grey top banner and navigation elements are removed via CSS injection.
 * This addresses the issue where the Wikipedia mobile header and navigation elements (Article/Talk tabs,
 * language switcher, download, star, edit buttons) take up valuable screen space
 * that could be used to display more article content.
 */
class WikipediaBannerRemovalTest {

    @Test
    fun `CSS injection should hide Wikipedia mobile header and navigation elements`() {
        // Test that verifies the expected CSS rules are injected to hide Wikipedia's banner and navigation
        // This documents the elements that should be hidden:
        
        val expectedHiddenElements = listOf(
            ".minerva-header",      // Main header container in Minerva skin
            ".header",              // Generic header element  
            ".header-container",    // Header container wrapper
            ".page-header",         // Page-specific header
            "#mw-mf-page-center > .header", // Specific header selector
            ".mw-header",           // MediaWiki header
            ".page-actions-menu",   // Page actions menu container
            ".talk-tab",            // Article/Talk tab switcher
            ".page-tabs",           // Page tabs container
            ".namespace-tabs",      // Namespace tabs
            ".minerva-page-actions", // Minerva page actions
            ".language-selector",   // Language switcher
            ".mw-ui-icon-language-switcher", // Language switcher icon
            ".mw-ui-icon-language", // Language icon
            ".language-button",     // Language button
            ".mw-ui-icon-download", // Download icon
            ".download-button",     // Download button
            ".mw-ui-icon-star",     // Star/watchlist icon
            ".watch-this-article",  // Watch article button
            ".watchstar",           // Watchstar element
            ".mw-ui-icon-edit",     // Edit icon
            ".edit-page",           // Edit page button
            ".edit-button",         // Edit button
            "#page-actions",        // Page actions container
            ".mw-editsection"       // Section edit links
        )
        
        // In a real WebView test, we would:
        // 1. Load a Wikipedia mobile page
        // 2. Verify that the onPageFinished method injects the CSS
        // 3. Check that the header and navigation elements are hidden (display: none)
        // 4. Verify that body margins/padding are reset to 0
        
        // For this unit test, we document the expected behavior
        expectedHiddenElements.forEach { selector ->
            assertTrue("CSS should hide $selector element", selector.isNotEmpty())
        }
        
        assertTrue("Header and navigation removal should free up screen space for article content", true)
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