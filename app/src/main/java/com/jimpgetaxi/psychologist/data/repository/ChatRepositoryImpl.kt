package com.jimpgetaxi.psychologist.data.repository

import android.util.Log
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.BlockThreshold
import com.google.ai.client.generativeai.type.HarmCategory
import com.google.ai.client.generativeai.type.SafetySetting
import com.google.ai.client.generativeai.type.content
import com.google.ai.client.generativeai.type.generationConfig
import com.jimpgetaxi.psychologist.BuildConfig
import com.jimpgetaxi.psychologist.data.local.JournalDao
import com.jimpgetaxi.psychologist.data.local.MessageDao
import com.jimpgetaxi.psychologist.data.local.MessageEntity
import com.jimpgetaxi.psychologist.data.local.MoodDao
import com.jimpgetaxi.psychologist.data.local.SessionEntity
import com.jimpgetaxi.psychologist.data.local.UserPreferencesRepository
import com.jimpgetaxi.psychologist.domain.model.Sender
import com.jimpgetaxi.psychologist.domain.model.SystemPrompts
import com.jimpgetaxi.psychologist.domain.repository.ChatRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onCompletion
import javax.inject.Inject
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ChatRepositoryImpl @Inject constructor(
    private val messageDao: MessageDao,
    private val moodDao: MoodDao,
    private val journalDao: JournalDao,
    private val insightDao: com.jimpgetaxi.psychologist.data.local.InsightDao,
    private val userPreferencesRepository: UserPreferencesRepository
) : ChatRepository {

    override fun getMessages(sessionId: Long): Flow<List<MessageEntity>> {
        return messageDao.getMessagesForSession(sessionId)
    }

    override fun getSessions(): Flow<List<SessionEntity>> {
        return messageDao.getAllSessions()
    }

    override suspend fun createNewSession(): Long {
        val session = SessionEntity(
            startTime = System.currentTimeMillis(),
            title = "New Session"
        )
        return messageDao.insertSession(session)
    }

    private suspend fun getGenerativeModel(): GenerativeModel {
        val profile = userPreferencesRepository.userProfile.first()
        val currentModelName = userPreferencesRepository.selectedModel.first()
        val recentMoods = moodDao.getRecentMoods(5)
        val recentJournalEntries = journalDao.getRecentEntries(3)
        val longTermInsights = insightDao.getRecentInsights()

        val moodContext = if (recentMoods.isNotEmpty()) {
            "\nRECENT MOOD HISTORY (1=Sad, 5=Happy):\n" + 
            recentMoods.joinToString("\n") { "- Value: ${it.moodValue} (at ${java.util.Date(it.timestamp)})" }
        } else ""

        val journalContext = if (recentJournalEntries.isNotEmpty()) {
            "\nRECENT JOURNAL ENTRIES (User's private thoughts):\n" +
            recentJournalEntries.joinToString("\n\n") { 
                "Title: ${it.title}\nDate: ${java.util.Date(it.timestamp)}\nContent: ${it.content}"
            }
        } else ""

        val memoryContext = if (longTermInsights.isNotEmpty()) {
            "\nLONG-TERM MEMORY (Important facts from past sessions):\n" +
            longTermInsights.joinToString("\n") { "- ${it.content}" }
        } else ""

        val systemInstructionText = """
                ${SystemPrompts.DEFAULT_PSYCHOLOGIST}
                
                USER PROFILE CONTEXT:
                Name: ${profile.name}
                Age: ${profile.age}
                Main Concern: ${profile.mainConcern}
                $moodContext
                $journalContext
                $memoryContext
            """.trimIndent()

        val config = generationConfig {
            temperature = 0.7f
        }
        val safetySettings = listOf(
            SafetySetting(HarmCategory.HARASSMENT, BlockThreshold.NONE),
            SafetySetting(HarmCategory.HATE_SPEECH, BlockThreshold.NONE),
            SafetySetting(HarmCategory.SEXUALLY_EXPLICIT, BlockThreshold.NONE),
            SafetySetting(HarmCategory.DANGEROUS_CONTENT, BlockThreshold.NONE)
        )

        return GenerativeModel(
            modelName = currentModelName,
            apiKey = BuildConfig.GEMINI_API_KEY,
            generationConfig = config,
            safetySettings = safetySettings,
            systemInstruction = content { text(systemInstructionText) }
        )
    }

    private suspend fun extractAndSaveInsight(sessionId: Long) {
        try {
            val messages = messageDao.getMessagesForSession(sessionId).first().takeLast(6)
            if (messages.size < 2) return

            val conversationText = messages.joinToString("\n") { "${it.sender}: ${it.content}" }
            
            val extractionModel = GenerativeModel(
                modelName = "gemini-3-flash-preview",
                apiKey = BuildConfig.GEMINI_API_KEY,
                systemInstruction = content { 
                    text("You are a memory module. Analyze the conversation and extract ONE important fact or insight about the user that should be remembered long-term (e.g., 'User's father passed away 2 years ago' or 'User is afraid of flying'). If no new important fact is found, reply only with 'NONE'. Keep it very brief.") 
                }
            )

            val response = extractionModel.generateContent("Analyze this conversation:\n$conversationText")
            val insightText = response.text?.trim() ?: "NONE"

            if (insightText != "NONE" && insightText.length > 5) {
                insightDao.insertInsight(com.jimpgetaxi.psychologist.data.local.InsightEntity(content = insightText))
                Log.d("ChatRepo", "Saved new insight: $insightText")
            }
        } catch (e: Exception) {
            Log.e("ChatRepo", "Error extracting insight", e)
        }
    }

    override suspend fun sendMessage(sessionId: Long, text: String): Result<Unit> {
        return try {
            Log.d("ChatRepo", "Sending message: $text")
            val generativeModel = getGenerativeModel()

            // Fetch history BEFORE inserting the new message
            val history = messageDao.getMessagesForSession(sessionId).first()
                .map { msg ->
                    content(role = if (msg.sender == Sender.USER) "user" else "model") {
                        text(msg.content)
                    }
                }

            // Save user message to DB
            val userMessage = MessageEntity(
                sessionId = sessionId,
                content = text,
                timestamp = System.currentTimeMillis(),
                sender = Sender.USER
            )
            messageDao.insertMessage(userMessage)

            // Setup chat with history and send message
            val chat = generativeModel.startChat(history)
            val response = chat.sendMessage(text)
            
            val responseText = response.text
            if (responseText == null) {
                throw Exception("Response blocked or empty")
            }

            // Save AI message to DB
            val aiMessage = MessageEntity(
                sessionId = sessionId,
                content = responseText,
                timestamp = System.currentTimeMillis(),
                sender = Sender.AI
            )
            messageDao.insertMessage(aiMessage)

            // Extract insight asynchronously (or simple call for now)
            extractAndSaveInsight(sessionId)

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("ChatRepo", "Error sending message", e)
            Result.failure(e)
        }
    }

    override fun sendMessageStream(sessionId: Long, text: String): Flow<String> = flow {
        val generativeModel = getGenerativeModel()
        
        // 1. Fetch history
        val history = messageDao.getMessagesForSession(sessionId).first()
            .map { msg ->
                content(role = if (msg.sender == Sender.USER) "user" else "model") {
                    text(msg.content)
                }
            }

        // 2. Save user message to DB
        val userMessage = MessageEntity(
            sessionId = sessionId,
            content = text,
            timestamp = System.currentTimeMillis(),
            sender = Sender.USER
        )
        messageDao.insertMessage(userMessage)

        // 3. Start streaming
        val chat = generativeModel.startChat(history)
        var fullResponse = ""
        
        chat.sendMessageStream(text).collect { response ->
            val chunk = response.text ?: ""
            fullResponse += chunk
            emit(fullResponse)
        }

        // 4. Save full response to DB when done
        val aiMessage = MessageEntity(
            sessionId = sessionId,
            content = fullResponse,
            timestamp = System.currentTimeMillis(),
            sender = Sender.AI
        )
        messageDao.insertMessage(aiMessage)
        
        // 5. Extract insight
        extractAndSaveInsight(sessionId)
    }

    override suspend fun deleteSession(sessionId: Long) {
        messageDao.deleteMessagesForSession(sessionId)
        val session = messageDao.getSessionById(sessionId)
        if (session != null) {
            messageDao.deleteSession(session)
        }
    }

    // ... (existing imports)

    override suspend fun updateSessionTitle(sessionId: Long, newTitle: String) {
        val session = messageDao.getSessionById(sessionId)
        if (session != null) {
            val updatedSession = session.copy(title = newTitle)
            messageDao.insertSession(updatedSession)
        }
    }

    override suspend fun getAvailableModels(): List<String> = withContext(Dispatchers.IO) {
        try {
            val apiKey = BuildConfig.GEMINI_API_KEY
            val url = URL("https://generativelanguage.googleapis.com/v1beta/models?key=$apiKey")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"

            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val reader = BufferedReader(InputStreamReader(connection.inputStream))
                val response = StringBuilder()
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    response.append(line)
                }
                reader.close()

                val jsonResponse = JSONObject(response.toString())
                val modelsArray = jsonResponse.getJSONArray("models")
                val modelList = mutableListOf<String>()

                for (i in 0 until modelsArray.length()) {
                    val modelObj = modelsArray.getJSONObject(i)
                    val name = modelObj.getString("name").removePrefix("models/")
                    // Filter for Gemini models that support content generation
                    if (name.contains("gemini") && modelObj.optJSONArray("supportedGenerationMethods")?.toString()?.contains("generateContent") == true) {
                        modelList.add(name)
                    }
                }
                // Sort: put "flash" models first, then "pro", then others
                modelList.sortedBy { 
                    when {
                        it.contains("flash") -> 1
                        it.contains("pro") -> 2
                        else -> 3
                    }
                }
            } else {
                Log.e("ChatRepo", "Error fetching models: $responseCode")
                emptyList()
            }
        } catch (e: Exception) {
            Log.e("ChatRepo", "Exception fetching models", e)
            emptyList()
        }
    }
}
