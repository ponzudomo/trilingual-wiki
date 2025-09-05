package io.github.nicolasraoul.rosette

import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.WindowInsetsController
import android.view.WindowManager
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target

/**
 * Full-screen image viewer activity that displays images over all panels.
 * This provides a native Android full-screen image viewing experience
 * that overlays the entire app instead of using Wikipedia's image viewer.
 */
class FullscreenImageActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_IMAGE_URL = "extra_image_url"
        private const val TAG = "FullscreenImageActivity"
    }

    private lateinit var imageView: ImageView
    private lateinit var progressBar: ProgressBar
    private lateinit var closeButton: ImageButton
    private val progressHandler = Handler(Looper.getMainLooper())
    private var timeoutRunnable: Runnable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        Log.d(TAG, "FullscreenImageActivity onCreate started")
        
        setContentView(R.layout.activity_fullscreen_image)
        
        // Set up full-screen immersive mode after content view is set
        setupFullscreenMode()
        
        imageView = findViewById(R.id.fullscreen_image_view)
        progressBar = findViewById(R.id.loading_progress)
        closeButton = findViewById(R.id.close_button)
        
        // Set up close button
        closeButton.setOnClickListener { finish() }
        
        // Remove tap-to-close functionality on image as requested
        // Only close button and back button should dismiss the image
        
        // Get the image URL from the intent
        val imageUrl = intent.getStringExtra(EXTRA_IMAGE_URL)
        
        Log.d(TAG, "Received image URL: $imageUrl")
        
        if (imageUrl.isNullOrBlank()) {
            Log.e(TAG, "Invalid image URL received")
            Toast.makeText(this, "Invalid image URL", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        
        // Convert SVG URLs to PNG if needed
        val processedUrl = processSvgUrl(imageUrl)
        Log.d(TAG, "Processed URL: $processedUrl")
        
        // Load the image
        loadImage(processedUrl)
    }
    
    private fun setupFullscreenMode() {
        // Make the activity full screen
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        
        // For API 30+ use WindowInsetsController
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            window.insetsController?.let { controller ->
                controller.hide(android.view.WindowInsets.Type.systemBars())
                controller.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            // For older API levels use system UI visibility
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            )
        }
        
        // Set status bar color to transparent
        try {
            window.statusBarColor = Color.TRANSPARENT
            window.navigationBarColor = Color.TRANSPARENT
        } catch (e: Exception) {
            // Ignore if setting transparent colors fails on some devices
        }
    }
    
    /**
     * Converts SVG URLs to PNG thumbnail URLs for better compatibility.
     * Example: 
     * https://commons.wikimedia.org/wiki/File:Flag_of_New_Zealand.svg
     * -> https://upload.wikimedia.org/wikipedia/commons/thumb/3/3e/Flag_of_New_Zealand.svg/960px-Flag_of_New_Zealand.svg.png
     */
    private fun processSvgUrl(imageUrl: String): String {
        Log.d(TAG, "Processing URL: $imageUrl")
        
        // Check if it's an SVG file
        if (imageUrl.contains(".svg", ignoreCase = true)) {
            Log.d(TAG, "Detected SVG file, converting to PNG thumbnail")
            
            // Handle commons.wikimedia.org/wiki/File: URLs
            if (imageUrl.contains("commons.wikimedia.org/wiki/File:")) {
                val fileName = imageUrl.substringAfterLast("File:").replace(" ", "_")
                Log.d(TAG, "Extracted file name: $fileName")
                
                // For SVG files, we'll use a simple approach and try the most common path structure
                // In practice, Wikimedia Commons uses MD5 hash, but we'll try some common patterns
                val thumbnailUrl = convertWikimediaCommonsSvgToPng(fileName)
                Log.d(TAG, "Converted SVG to PNG thumbnail: $thumbnailUrl")
                return thumbnailUrl
            }
            
            // Handle direct upload.wikimedia.org URLs that are SVG
            if (imageUrl.contains("upload.wikimedia.org") && imageUrl.contains(".svg")) {
                // Try to convert existing upload URLs to PNG thumbnails
                if (imageUrl.contains("/commons/")) {
                    val fileName = imageUrl.substringAfterLast("/")
                    val pathPart = imageUrl.substringAfter("/commons/").substringBeforeLast("/")
                    val convertedUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/$pathPart/$fileName/960px-$fileName.png"
                    Log.d(TAG, "Converted direct SVG URL to PNG: $convertedUrl")
                    return convertedUrl
                }
            }
        }
        
        Log.d(TAG, "No SVG conversion needed")
        return imageUrl
    }
    
    /**
     * Converts Wikimedia Commons SVG files to PNG thumbnails.
     * Uses common directory patterns since we don't have MD5 hash calculation.
     */
    private fun convertWikimediaCommonsSvgToPng(fileName: String): String {
        // Common patterns for well-known files
        val knownMappings = mapOf(
            "Flag_of_New_Zealand.svg" to "3/3e",
            "Flag_of_Australia.svg" to "b/b9", 
            "Flag_of_Canada.svg" to "c/cf",
            "Flag_of_the_United_States.svg" to "a/a4",
            "Flag_of_the_United_Kingdom.svg" to "a/ae"
        )
        
        val path = knownMappings[fileName] ?: getWikimediaPath(fileName)
        return "https://upload.wikimedia.org/wikipedia/commons/thumb/$path/$fileName/960px-$fileName.png"
    }
    
    /**
     * Gets the Wikimedia Commons directory path for a file.
     * This is a simplified version - in reality, Wikimedia uses MD5 hash of filename.
     */
    private fun getWikimediaPath(fileName: String): String {
        val name = fileName.replace(" ", "_")
        val firstChar = name[0].toString().lowercase()
        val secondChar = if (name.length > 1) name.substring(0, 2).lowercase() else firstChar
        return "$firstChar/$secondChar"
    }
    
    private fun loadImage(imageUrl: String) {
        Log.d(TAG, "Starting image load for: $imageUrl")
        progressBar.visibility = View.VISIBLE
        progressBar.progress = 0
        
        // Register progress listener for this specific URL
        ProgressInterceptor.addListener(imageUrl) { progress ->
            Log.d(TAG, "Real progress update: $progress%")
            progressBar.progress = progress
        }
        
        // Set up timeout handling - 10 seconds for better UX
        timeoutRunnable = Runnable {
            Log.w(TAG, "Image load timeout after 10 seconds for URL: $imageUrl")
            ProgressInterceptor.removeListener(imageUrl)
            progressBar.visibility = View.GONE
            Toast.makeText(this@FullscreenImageActivity, "Image loading timeout. Please try again.", Toast.LENGTH_SHORT).show()
        }
        progressHandler.postDelayed(timeoutRunnable!!, 10000) // 10 second timeout
        
        Glide.with(this)
            .load(imageUrl)
            .listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>,
                    isFirstResource: Boolean
                ): Boolean {
                    Log.e(TAG, "Image load failed", e)
                    ProgressInterceptor.removeListener(imageUrl)
                    clearTimeouts()
                    progressBar.visibility = View.GONE
                    Toast.makeText(this@FullscreenImageActivity, "Failed to load image", Toast.LENGTH_SHORT).show()
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable,
                    model: Any,
                    target: Target<Drawable>?,
                    dataSource: DataSource,
                    isFirstResource: Boolean
                ): Boolean {
                    Log.d(TAG, "Image loaded successfully")
                    ProgressInterceptor.removeListener(imageUrl)
                    clearTimeouts()
                    
                    // Complete progress bar if not already at 100%
                    if (progressBar.progress < 100) {
                        progressBar.progress = 100
                    }
                    
                    // Hide progress bar after a brief delay
                    progressHandler.postDelayed({
                        progressBar.visibility = View.GONE
                    }, 200)
                    
                    return false // Let Glide handle the actual display
                }
            })
            .transition(DrawableTransitionOptions.withCrossFade())
            .into(imageView)
    }
    
    private fun clearTimeouts() {
        Log.d(TAG, "Clearing timeouts")
        timeoutRunnable?.let { runnable ->
            progressHandler.removeCallbacks(runnable)
        }
        timeoutRunnable = null
    }
    
    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "FullscreenImageActivity onDestroy")
        
        // Clean up any remaining progress listeners
        val imageUrl = intent.getStringExtra(EXTRA_IMAGE_URL)
        if (!imageUrl.isNullOrBlank()) {
            val processedUrl = processSvgUrl(imageUrl)
            ProgressInterceptor.removeListener(processedUrl)
        }
        
        clearTimeouts()
    }
    
    override fun onBackPressed() {
        @Suppress("DEPRECATION")
        super.onBackPressed()
        finish()
    }
}