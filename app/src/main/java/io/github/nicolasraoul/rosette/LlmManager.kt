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
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize generative model", e)
        }
    }

    suspend fun answerQuestion(question: String, articleText: String): Result<String> {
        if (generativeModel == null) {
            return Result.failure(IllegalStateException("AI model is not available"))
        }

        val prompt = "Article:\n$articleText\n\nQuestion:\n$question\n\nAnswer:"

        return try {
            val response = generativeModel!!.generateContent(prompt)
            val answer = response.text
            if (answer.isNullOrBlank()) {
                Result.failure(Exception("Failed to get answer"))
            } else {
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
