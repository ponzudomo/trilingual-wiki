package io.github.nicolasraoul.rosette

import android.content.Context
import android.util.Log
import com.google.ai.edge.aicore.GenerativeModel
import com.google.ai.edge.aicore.generationConfig

class LlmManager(context: Context) {

    private var generativeModel: GenerativeModel? = null

    init {
        try {
            generativeModel = GenerativeModel(
                generationConfig = generationConfig {
                    temperature = 0.7f
                    topK = 40
                }
            )
            Log.d(TAG, "GenerativeModel initialized successfully.")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize generative model", e)
        }
    }

    suspend fun answerQuestion(question: String, articleText: String): Result<String> {
        Log.d(TAG, "Answering question: $question")
        Log.d(TAG, "Article text length: ${articleText.length}")
        if (generativeModel == null) {
            Log.e(TAG, "Generative model is not initialized.")
            return Result.failure(IllegalStateException("AI model is not available"))
        }

        val prompt = "Article:\n$articleText\n\nQuestion:\n$question\n\nAnswer:"
        Log.d(TAG, "Prompt: $prompt")

        return try {
            val response = generativeModel!!.generateContent(prompt)
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
