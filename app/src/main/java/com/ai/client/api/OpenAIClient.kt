package com.ai.client.api

import com.ai.client.api.models.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json

object OpenAIClient {
    private val json = Json { ignoreUnknownKeys = true }
    
    fun createClient(apiKey: String, baseUrl: String): HttpClient {
        return HttpClient(OkHttp) {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                })
            }
            
            defaultRequest {
                url(baseUrl)
                header("Authorization", "Bearer $apiKey")
                header("Content-Type", "application/json")
            }
        }
    }
    
    /**
     * 获取 OpenAI 支持的模型列表
     */
    suspend fun listModels(client: HttpClient): List<AppModelInfo> {
        try {
            val response = client.get("/v1/models").body<OpenAIModelListResponse>()
            
            return response.data.map { model ->
                AppModelInfo(
                    id = model.id,
                    name = model.id,
                    provider = ProviderType.OpenAI,
                    category = ModelCategory.Chat::class.java.simpleName,
                    supportsStream = true
                )
            }
        } catch (e: Exception) {
            // 如果失败返回空列表
            return emptyList()
        }
    }
    
    suspend fun chat(
        client: HttpClient,
        model: String,
        messages: List<Message>,
        temperature: Double?,
        maxTokens: Int?
    ): OpenAIChatResponse {
        val request = OpenAIChatRequest(
            model = model,
            messages = messages,
            temperature = temperature,
            max_tokens = maxTokens,
            stream = false
        )
        
        return client.post("/v1/chat/completions") {
            setBody(request)
        }.body()
    }
    
    fun chatStream(
        client: HttpClient,
        model: String,
        messages: List<Message>,
        temperature: Double?,
        maxTokens: Int?
    ): Flow<String> = flow {
        val request = OpenAIChatRequest(
            model = model,
            messages = messages,
            temperature = temperature,
            max_tokens = maxTokens,
            stream = true
        )
        
        val response = client.submitFormUrlEncoded("/v1/chat/completions") {
            setBody(request)
        }
        
        // SSE 解析
        response.bodyAsText().split("\n\n").forEach { chunk ->
            if (chunk.startsWith("data: ")) {
                val data = chunk.substring(6)
                if (data != "[DONE]") {
                    try {
                        val choice = json.decodeFromString<OpenAIChoiceChunk>(data).choices[0].delta.content ?: ""
                        emit(choice)
                    } catch (e: Exception) {
                        // 忽略解析错误
                    }
                }
            }
        }
    }
}

// OpenAI 模型列表响应（使用 @SerialName 处理 object 字段名冲突）
@Serializable
data class OpenAIModelListResponse(
    val data: List<OpenAIModelItem>,
    @kotlinx.serialization.SerialName("object")
    val `object`: String
)

// ==================== SSE Chunk 模型（用于流式）====================
@Serializable
data class OpenAIChoiceChunk(
    val id: String,
    @kotlinx.serialization.SerialName("object")
    val `object`: String,
    val created: Long,
    val model: String,
    val choices: List<DeltaChoice>
)

@Serializable
data class DeltaChoice(
    val index: Int,
    val delta: DeltaMessage,
    val finish_reason: String?
)

@Serializable
data class DeltaMessage(
    val role: String?,
    val content: String?
)
