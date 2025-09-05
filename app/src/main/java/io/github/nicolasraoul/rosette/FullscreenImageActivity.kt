package io.github.nicolasraoul.rosette

import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
    }

    private lateinit var imageView: ImageView
    private lateinit var progressBar: ProgressBar
    private lateinit var closeButton: ImageButton
    private val progressHandler = Handler(Looper.getMainLooper())
    private var progressRunnable: Runnable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
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
        
        if (imageUrl.isNullOrBlank()) {
            Toast.makeText(this, "Invalid image URL", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        
        // Load the image
        loadImage(imageUrl)
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
    
    private fun loadImage(imageUrl: String) {
        progressBar.visibility = View.VISIBLE
        progressBar.progress = 0
        
        // Start simulating realistic download progress
        startProgressSimulation()
        
        Glide.with(this)
            .load(imageUrl)
            .listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>?,
                    isFirstResource: Boolean
                ): Boolean {
                    stopProgressSimulation()
                    progressBar.visibility = View.GONE
                    Toast.makeText(this@FullscreenImageActivity, "Failed to load image", Toast.LENGTH_SHORT).show()
                    return false
                }
                
                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any?,
                    target: Target<Drawable>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    stopProgressSimulation()
                    // Complete the progress bar and then hide it
                    progressBar.progress = 100
                    progressHandler.postDelayed({
                        progressBar.visibility = View.GONE
                    }, 200)
                    return false
                }
            })
            .transition(DrawableTransitionOptions.withCrossFade())
            .into(imageView)
    }
    
    private fun startProgressSimulation() {
        var currentProgress = 0
        val progressIncrement = 2
        val updateInterval = 50L // milliseconds
        
        progressRunnable = object : Runnable {
            override fun run() {
                if (currentProgress < 90) { // Don't complete until image is actually loaded
                    currentProgress += progressIncrement
                    progressBar.progress = currentProgress
                    progressHandler.postDelayed(this, updateInterval)
                }
            }
        }
        progressHandler.post(progressRunnable!!)
    }
    
    private fun stopProgressSimulation() {
        progressRunnable?.let { runnable ->
            progressHandler.removeCallbacks(runnable)
        }
        progressRunnable = null
    }
    
    override fun onDestroy() {
        super.onDestroy()
        stopProgressSimulation()
    }
    
    override fun onBackPressed() {
        @Suppress("DEPRECATION")
        super.onBackPressed()
        finish()
    }
}