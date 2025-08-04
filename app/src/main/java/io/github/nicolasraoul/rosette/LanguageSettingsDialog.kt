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
        lifecycleScope.launch {
            try {
                availableLanguages.clear()
                availableLanguages.addAll(languageManager.getAvailableWikipediaLanguages())
                loadingIndicator.visibility = View.GONE
                availableLanguagesAdapter.notifyDataSetChanged()
                updateAvailableLanguages()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load languages", e)
                loadingIndicator.visibility = View.GONE
                errorText.visibility = View.VISIBLE
                errorText.text = "Failed to load languages. Using fallback list."
            }
        }

        return AlertDialog.Builder(requireContext())
            .setTitle("Select Languages")
            .setView(view)
            .setPositiveButton("Save") { _, _ ->
                saveLanguages()
            }
            .setNegativeButton("Cancel", null)
            .create()
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
            Toast.makeText(context, "You can select up to $MAX_SELECTED_LANGUAGES languages", Toast.LENGTH_SHORT).show()
            return
        }
        
        if (selectedLanguages.any { it.code == language.code }) {
            Toast.makeText(context, "Language already selected", Toast.LENGTH_SHORT).show()
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
            Toast.makeText(context, "Please select exactly $MAX_SELECTED_LANGUAGES languages", Toast.LENGTH_SHORT).show()
            return
        }
        
        val languageCodes = selectedLanguages.map { it.code }.toTypedArray()
        languageManager.saveLanguages(languageCodes)
        onLanguagesChanged?.invoke()
        Toast.makeText(context, "Languages saved successfully", Toast.LENGTH_SHORT).show()
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