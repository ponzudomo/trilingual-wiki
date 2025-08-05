package io.github.nicolasraoul.rosette

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch

class LanguageSettingsDialog : DialogFragment() {
    
    private lateinit var languageManager: LanguageManager
    private lateinit var availableLanguagesAdapter: AvailableLanguagesAdapter
    private lateinit var selectedLanguagesAdapter: SelectedLanguagesAdapter
    private var availableLanguages = mutableListOf<WikipediaLanguage>()
    private var selectedLanguages = mutableListOf<WikipediaLanguage>()
    private var onLanguagesChanged: (() -> Unit)? = null

    companion object {
        private const val TAG = "LanguageSettingsDialog"
        private const val MAX_SELECTED_LANGUAGES = 3

        fun newInstance(onLanguagesChanged: () -> Unit): LanguageSettingsDialog {
            return LanguageSettingsDialog().apply {
                this.onLanguagesChanged = onLanguagesChanged
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        languageManager = LanguageManager(requireContext())
        
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_language_settings, null)
        
        val availableRecyclerView = view.findViewById<RecyclerView>(R.id.available_languages_recycler)
        val selectedRecyclerView = view.findViewById<RecyclerView>(R.id.selected_languages_recycler)
        val loadingIndicator = view.findViewById<ProgressBar>(R.id.loading_indicator)
        val errorText = view.findViewById<TextView>(R.id.error_text)
        
        setupRecyclerViews(availableRecyclerView, selectedRecyclerView)
        loadCurrentLanguages()
        
        // Show loading and fetch languages
        loadingIndicator.visibility = View.VISIBLE
        errorText.visibility = View.GONE
        lifecycleScope.launch {
            try {
                Log.d(TAG, "Starting to fetch Wikipedia languages...")
                availableLanguages.clear()
                val fetchedLanguages = languageManager.getAvailableWikipediaLanguages()
                Log.d(TAG, "Fetched ${fetchedLanguages.size} languages from API")
                
                if (fetchedLanguages.isEmpty()) {
                    Log.w(TAG, "No languages returned from API - showing error message")
                    loadingIndicator.visibility = View.GONE
                    errorText.visibility = View.VISIBLE
                    errorText.text = "Unable to load language list. There may be an issue with the Wikipedia API or your connection.\n\nTap outside this dialog to close and try reopening the language settings."
                } else {
                    availableLanguages.addAll(fetchedLanguages)
                    loadingIndicator.visibility = View.GONE
                    availableLanguagesAdapter.notifyDataSetChanged()
                    updateAvailableLanguages()
                    Log.d(TAG, "Successfully loaded languages list")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load languages: ${e.javaClass.simpleName}: ${e.message}", e)
                loadingIndicator.visibility = View.GONE
                errorText.visibility = View.VISIBLE
                errorText.text = "Failed to load languages: ${e.javaClass.simpleName}: ${e.message ?: "Unknown error"}.\n\nCheck the logs for more details."
            }
        }

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle(R.string.select_languages_title)
            .setView(view)
            .setPositiveButton(R.string.save) { _, _ ->
                saveLanguages()
            }
            .setNegativeButton(R.string.cancel, null)
            .create()
            
        // Ensure title has proper contrast - use multiple approaches for better compatibility
        dialog.setOnShowListener {
            // Try different ways to find and style the title
            val titleView = dialog.findViewById<TextView>(androidx.appcompat.R.id.alertTitle)
                ?: dialog.findViewById<TextView>(android.R.id.title)
            
            titleView?.apply {
                setTextColor(0xFF000000.toInt()) // Force solid black color
                textSize = 18f
                setPadding(paddingLeft, 24, paddingRight, 16)
                // Ensure background doesn't interfere
                setBackgroundColor(0x00000000) // Transparent background
            }
            
            // Alternative approach - set window background to ensure contrast
            dialog.window?.decorView?.setBackgroundColor(0xFFFFFFFF.toInt()) // White background
        }
        
        return dialog
    }

    private fun setupRecyclerViews(availableRecyclerView: RecyclerView, selectedRecyclerView: RecyclerView) {
        availableLanguagesAdapter = AvailableLanguagesAdapter { language ->
            addLanguage(language)
        }
        selectedLanguagesAdapter = SelectedLanguagesAdapter { language ->
            removeLanguage(language)
        }
        
        availableRecyclerView.layoutManager = LinearLayoutManager(context)
        availableRecyclerView.adapter = availableLanguagesAdapter
        
        selectedRecyclerView.layoutManager = LinearLayoutManager(context)
        selectedRecyclerView.adapter = selectedLanguagesAdapter
    }

    private fun loadCurrentLanguages() {
        val currentLanguageCodes = languageManager.getDisplayLanguages()
        // We'll populate with actual language objects once we load from API
        selectedLanguages.clear()
        // For now, add placeholder objects that will be replaced when API loads
        currentLanguageCodes.forEach { code ->
            selectedLanguages.add(WikipediaLanguage(code, code.uppercase(), code.uppercase()))
        }
        selectedLanguagesAdapter.notifyDataSetChanged()
    }

    private fun addLanguage(language: WikipediaLanguage) {
        if (selectedLanguages.size >= MAX_SELECTED_LANGUAGES) {
            Toast.makeText(context, getString(R.string.max_languages_selected, MAX_SELECTED_LANGUAGES), Toast.LENGTH_SHORT).show()
            return
        }
        
        if (selectedLanguages.any { it.code == language.code }) {
            Toast.makeText(context, getString(R.string.language_already_selected), Toast.LENGTH_SHORT).show()
            return
        }
        
        selectedLanguages.add(language)
        selectedLanguagesAdapter.notifyItemInserted(selectedLanguages.size - 1)
        updateAvailableLanguages()
    }

    private fun removeLanguage(language: WikipediaLanguage) {
        val index = selectedLanguages.indexOfFirst { it.code == language.code }
        if (index >= 0) {
            selectedLanguages.removeAt(index)
            selectedLanguagesAdapter.notifyItemRemoved(index)
            updateAvailableLanguages()
        }
    }

    private fun updateAvailableLanguages() {
        // Update available languages to replace placeholders with real data
        val currentCodes = selectedLanguages.map { it.code }
        selectedLanguages.clear()
        currentCodes.forEach { code ->
            val realLanguage = availableLanguages.find { it.code == code }
            if (realLanguage != null) {
                selectedLanguages.add(realLanguage)
            } else {
                // Keep placeholder if real data not available yet
                selectedLanguages.add(WikipediaLanguage(code, code.uppercase(), code.uppercase()))
            }
        }
        selectedLanguagesAdapter.notifyDataSetChanged()
        availableLanguagesAdapter.notifyDataSetChanged()
    }

    private fun saveLanguages() {
        if (selectedLanguages.size != MAX_SELECTED_LANGUAGES) {
            Toast.makeText(context, getString(R.string.select_exact_languages, MAX_SELECTED_LANGUAGES), Toast.LENGTH_SHORT).show()
            return
        }
        
        val languageCodes = selectedLanguages.map { it.code }.toTypedArray()
        languageManager.saveLanguages(languageCodes)
        onLanguagesChanged?.invoke()
        Toast.makeText(context, getString(R.string.languages_saved), Toast.LENGTH_SHORT).show()
    }

    inner class AvailableLanguagesAdapter(
        private val onLanguageClick: (WikipediaLanguage) -> Unit
    ) : RecyclerView.Adapter<AvailableLanguagesAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_available_language, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val language = availableLanguages[position]
            val isSelected = selectedLanguages.any { it.code == language.code }
            holder.bind(language, isSelected)
        }

        override fun getItemCount(): Int = availableLanguages.size

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val languageNameText: TextView = itemView.findViewById(R.id.language_name)
            private val languageCodeText: TextView = itemView.findViewById(R.id.language_code)

            fun bind(language: WikipediaLanguage, isSelected: Boolean) {
                languageNameText.text = language.toString()
                languageCodeText.text = language.code
                itemView.alpha = if (isSelected) 0.5f else 1.0f
                itemView.setOnClickListener {
                    if (!isSelected) {
                        onLanguageClick(language)
                    }
                }
            }
        }
    }

    inner class SelectedLanguagesAdapter(
        private val onLanguageRemove: (WikipediaLanguage) -> Unit
    ) : RecyclerView.Adapter<SelectedLanguagesAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_selected_language, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(selectedLanguages[position])
        }

        override fun getItemCount(): Int = selectedLanguages.size

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val languageNameText: TextView = itemView.findViewById(R.id.language_name)
            private val removeButton: ImageButton = itemView.findViewById(R.id.remove_button)

            fun bind(language: WikipediaLanguage) {
                languageNameText.text = language.toString()
                removeButton.setOnClickListener {
                    onLanguageRemove(language)
                }
            }
        }
    }
}