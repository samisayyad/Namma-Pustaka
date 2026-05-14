package com.nammapustaka.data.repository

import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.nammapustaka.BuildConfig
import com.nammapustaka.data.model.Book
import com.nammapustaka.utils.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AiRepository @Inject constructor() {

    private val model = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = BuildConfig.GEMINI_API_KEY
    )

    fun getBookSummary(book: Book, inKannada: Boolean = false): Flow<Resource<String>> = flow {
        emit(Resource.Loading())
        try {
            val language = if (inKannada) "Kannada (ಕನ್ನಡ)" else "English"
            val prompt = """
                You are a helpful librarian assistant for rural school students in Karnataka, India.
                
                Generate a concise, engaging book summary in $language for the following book:
                Title: "${book.title}"
                Author: ${book.author}
                Genre: ${book.genre}
                Description: ${book.description}
                
                The summary should be:
                - 3-4 sentences long
                - Age-appropriate for school students
                - Highlight what makes this book interesting
                - End with why a student should read it
                ${if (inKannada) "- Write entirely in Kannada script" else ""}
            """.trimIndent()

            val response = model.generateContent(content { text(prompt) })
            val summary = response.text ?: "Summary unavailable"
            emit(Resource.Success(summary))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "AI summary failed"))
        }
    }

    fun chat(
        userMessage: String,
        chatHistory: List<Pair<String, Boolean>>,
        schoolContext: String = ""
    ): Flow<Resource<String>> = flow {
        emit(Resource.Loading())
        try {
            val chat = model.startChat(history = chatHistory.map { (msg, isUser) ->
                content(role = if (isUser) "user" else "model") { text(msg) }
            })

            val systemContext = """
                You are Pustaka, an AI librarian assistant for Namma Pustaka - a smart library app for rural schools in Karnataka, India.
                You help students discover books, get summaries, and improve their reading habits.
                You can respond in both English and Kannada based on what the student prefers.
                School context: $schoolContext
                Be friendly, encouraging, and educational.
            """.trimIndent()

            val fullMessage = if (chatHistory.isEmpty()) "$systemContext\n\nStudent: $userMessage" else userMessage
            val response = chat.sendMessage(fullMessage)
            val reply = response.text ?: "I'm having trouble responding right now."
            emit(Resource.Success(reply))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "AI response failed"))
        }
    }

    fun getRecommendations(
        userInterests: List<String>,
        recentlyRead: List<String>,
        grade: String
    ): Flow<Resource<String>> = flow {
        emit(Resource.Loading())
        try {
            val prompt = """
                You are a librarian at a rural school in Karnataka, India.
                
                Recommend 5 books for a Class $grade student with these interests: ${userInterests.joinToString(", ")}
                Recently read: ${recentlyRead.joinToString(", ")}
                
                For each recommendation, provide:
                1. Book title and author
                2. One sentence why it's perfect for this student
                3. Genre
                
                Format as a clean numbered list. Focus on books available in Indian school libraries.
            """.trimIndent()

            val response = model.generateContent(content { text(prompt) })
            emit(Resource.Success(response.text ?: "No recommendations available"))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to get recommendations"))
        }
    }
}
