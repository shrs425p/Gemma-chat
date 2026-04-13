package com.gemma.chat.data.inference

import com.google.ai.edge.litertlm.Backend
import com.google.ai.edge.litertlm.Conversation
import com.google.ai.edge.litertlm.Engine
import com.google.ai.edge.litertlm.EngineConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

/**
 * GemmaInferenceEngine wraps Google's LiteRT-LM Engine API.
 *
 * Supports:
 *  - Streaming token-by-token generation via Kotlin Flow
 *  - Conversation management (multi-turn context)
 *  - Gemma 4 E2B .litertlm model format
 */
class GemmaInferenceEngine {
    private var engine: Engine? = null
    private var conversation: Conversation? = null
    private var currentModelPath: String? = null
    private var isInitialized = false

    /**
     * Initialize (or re-initialize) the engine with the given model file path.
     * This must be called on a background thread — it can take 30-60 seconds.
     */
    @Throws(Exception::class)
    suspend fun initialize(
        modelPath: String,
        maxTokens: Int = 2048,
        temperature: Float = 0.8f,
        topK: Int = 40,
        topP: Float = 0.95f,
        randomSeed: Int = 42
    ) = withContext(Dispatchers.IO) {
        // Close existing instance if any
        close()

        val config = EngineConfig(
            modelPath = modelPath,
            backend = Backend.CPU()
        )

        val newEngine = Engine(config)
        newEngine.initialize()

        engine = newEngine
        conversation = newEngine.createConversation()
        currentModelPath = modelPath
        isInitialized = true
    }

    /**
     * Generate a response as a streaming Flow of partial text strings.
     * Each emission is a chunk from the LLM.
     */
    fun generateStreamingResponse(prompt: String): Flow<String> {
        val conv = conversation
            ?: throw IllegalStateException("Engine not initialized. Call initialize() first.")

        return conv.sendMessageAsync(prompt)
            .map { message -> message.toString() }
            .flowOn(Dispatchers.IO)
    }

    /**
     * Generate a full (non-streaming) response.
     */
    @Throws(Exception::class)
    suspend fun generateResponse(prompt: String): String = withContext(Dispatchers.IO) {
        val conv = conversation
            ?: throw IllegalStateException("Engine not initialized. Call initialize() first.")
        conv.sendMessage(prompt).toString()
    }

    /**
     * Build a Gemma 4 instruction-tuned prompt from conversation history.
     *
     * Note: With LiteRT-LM's Conversation API, the engine handles multi-turn
     * context automatically. This method is provided for manual prompt building
     * if needed for non-conversation use cases.
     */
    fun buildPrompt(
        systemPrompt: String,
        history: List<Pair<String, String?>>,
        currentInput: String
    ): String {
        val sb = StringBuilder()

        val firstTurnPrefix = if (systemPrompt.isNotBlank()) "$systemPrompt\n\n" else ""

        history.forEachIndexed { index, (userMsg, assistantMsg) ->
            val prefix = if (index == 0) firstTurnPrefix else ""
            sb.append("<start_of_turn>user\n${prefix}${userMsg}<end_of_turn>\n")
            if (assistantMsg != null) {
                sb.append("<start_of_turn>model\n${assistantMsg}<end_of_turn>\n")
            }
        }

        val prefix = if (history.isEmpty()) firstTurnPrefix else ""
        sb.append("<start_of_turn>user\n${prefix}${currentInput}<end_of_turn>\n")
        sb.append("<start_of_turn>model\n")

        return sb.toString()
    }

    /**
     * Start a fresh conversation (clears context).
     */
    fun resetConversation() {
        conversation?.close()
        conversation = engine?.createConversation()
    }

    val isReady: Boolean get() = isInitialized && engine != null
    val modelPath: String? get() = currentModelPath

    fun close() {
        conversation?.close()
        conversation = null
        engine?.close()
        engine = null
        isInitialized = false
        currentModelPath = null
    }
}
