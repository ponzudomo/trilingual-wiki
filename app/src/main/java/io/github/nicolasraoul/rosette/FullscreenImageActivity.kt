package io.github.nicolasraoul.rosette

import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import android.view.WindowInsetsController
import android.view.WindowManager
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Set up full-screen immersive mode
        setupFullscreenMode()
        
        setContentView(R.layout.activity_fullscreen_image)
        
        imageView = findViewById(R.id.fullscreen_image_view)
        progressBar = findViewById(R.id.loading_progress)
        closeButton = findViewById(R.id.close_button)
        
        // Set up close button
        closeButton.setOnClickListener { finish() }
        
        // Set up tap to close functionality
        imageView.setOnClickListener { finish() }
        
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
        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.TRANSPARENT
    }
    
    private fun loadImage(imageUrl: String) {
        progressBar.visibility = View.VISIBLE
        
        Glide.with(this)
            .load(imageUrl)
            .transition(DrawableTransitionOptions.withCrossFade())
            .into(imageView)
        
        // Hide progress bar after a short delay
        imageView.post {
            progressBar.visibility = View.GONE
        }
    }
    
    override fun onBackPressed() {
        @Suppress("DEPRECATION")
        super.onBackPressed()
        finish()
    }
}