package io.github.nicolasraoul.rosette

import android.content.Context
import android.util.Log
import com.google.ai.edge.aicore.GenerativeModel
import com.google.ai.edge.aicore.generationConfig

class LlmManager(private val context: Context) {

    private val generativeModel: GenerativeModel by lazy {
        Log.d(TAG, "Initializing GenerativeModel...")
        GenerativeModel(
            generationConfig = generationConfig {
                this.context = this@LlmManager.context.applicationContext
                temperature = 0.2f
                topK = 16
                maxOutputTokens = 256
            }
        )
    }

    fun isModelAvailable(): Boolean {
        return try {
            // Accessing the property will trigger the lazy initialization.
            // If it throws an exception, we'll catch it and return false.
            generativeModel
            Log.d(TAG, "GenerativeModel is available.")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Model is not available", e)
            false
        }
    }

    suspend fun answerQuestion(question: String, articleText: String): Result<String> {
        Log.d(TAG, "Answering question: $question")
        Log.d(TAG, "Article text length: ${articleText.length}")

        return try {
            val model = generativeModel
            val prompt = "Article:\n$articleText\n\nQuestion:\n$question\n\nAnswer:"
            Log.d(TAG, "Prompt: $prompt")

            val response = model.generateContent(prompt)
            val answer = response.text
            if (answer.isNullOrBlank()) {
                Log.e(TAG, "Failed to get answer from model. Response was null or blank.")
                Result.failure(Exception("Failed to get answer"))
            } else {
                Log.d(TAG, "Got answer: $answer")
                Result.success(answer)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to generate content", e)
            Result.failure(e)
        }
    }

    companion object {
        private const val TAG = "LlmManager"
    }
}
