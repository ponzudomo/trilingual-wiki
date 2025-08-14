package io.github.nicolasraoul.rosette

import android.content.res.Configuration
import android.view.View
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule

import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Rule

import org.junit.Assert.*

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    
    @get:Rule
    val activityRule = ActivityTestRule(MainActivity::class.java)
    
    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("io.github.nicolasraoul.rosette", appContext.packageName)
    }
    
    @Test
    fun searchDropdownHiddenOnConfigurationChange() {
        val activity = activityRule.activity
        
        // Simulate search suggestions being visible first
        activity.runOnUiThread {
            val suggestionsRecyclerView = activity.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.search_suggestions_recycler_view)
            val searchBar = activity.findViewById<android.widget.EditText>(R.id.search_bar)
            
            // Set up conditions that would normally trigger search dropdown
            searchBar.setText("test search")
            suggestionsRecyclerView.visibility = View.VISIBLE
            
            // Verify it's visible before configuration change
            assertEquals(View.VISIBLE, suggestionsRecyclerView.visibility)
            
            // Simulate configuration change (like screen unfolding)
            val newConfig = Configuration(activity.resources.configuration)
            activity.onConfigurationChanged(newConfig)
            
            // Verify search dropdown is now hidden
            assertEquals(View.GONE, suggestionsRecyclerView.visibility)
            
            // Verify search bar focus is cleared
            assertFalse(searchBar.hasFocus())
            
            // Verify search bar text is cleared to prevent restoration triggers
            assertEquals("", searchBar.text.toString())
        }
        
        // Wait for the configuration change and restoration flags to reset
        Thread.sleep(2100) // Wait for both delays (1500 + 500 + buffer)
        
        // Verify that search functionality works normally after configuration change
        activity.runOnUiThread {
            val searchBar = activity.findViewById<android.widget.EditText>(R.id.search_bar)
            searchBar.setText("") // Clear first
            searchBar.setText("normal search") // This should work normally now
        }
    }
}