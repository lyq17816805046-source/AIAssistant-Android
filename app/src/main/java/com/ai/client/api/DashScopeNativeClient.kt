package com.ai.client.api

import com.ai.client.api.models.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.client.request.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json

object DashScopeNativeClient {
    // 百炼**真正原生** API 端点（非 OpenAI 兼容层）
    private const val BASE_URL = "https://dashscope.aliyuncs.com"
    
    // 百炼支持流式输出，使用 SSE 格式
    fun createClient(apiKey: String): HttpClient {
        return HttpClient(OkHttp) {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                })
            }
            
            defaultRequest {
                url(BASE_URL)
                header("Authorization", "Bearer $apiKey")
                header("Content-Type", "application/json")
                header("X-DashScope-SSE", "enable") // 启用 SSE 流式
            }
        }
    }
    
    /**
     * 调用百炼原生文本生成 API
     * 端点：POST /api/v1/services/aigc/text-generation/generation
     */
    suspend fun chat(
        client: HttpClient,
        model: String,
        messages: List<DashScopeInputMessage>,
        temperature: Double?,
        maxTokens: Int?
    ): DashScopeNativeResponse {
        val request = DashScopeNativeRequest(
            model = model,
            input = Input(messages = messages),
            parameters = DashScopeNativeParameters(
                result_format = "message",
                temperature = temperature,
                max_tokens = maxTokens
            )
        )
        
        return client.post("/api/v1/services/aigc/text-generation/generation") {
            setBody(request)
        }.body()
    }
    
    /**
     * 百炼原生流式 API
     * 返回 SSE 格式的流
     */
    fun chatStream(
        client: HttpClient,
        model: String,
        messages: List<DashScopeInputMessage>,
        temperature: Double?,
        maxTokens: Int?
    ): Flow<String> = flow {
        val request = DashScopeNativeRequest(
            model = model,
            input = Input(messages = messages),
            parameters = DashScopeNativeParameters(
                result_format = "message",
                temperature = temperature,
                max_tokens = maxTokens
            )
        )
        
        val response = client.submitFormUrlEncoded("/api/v1/services/aigc/text-generation/generation") {
            setBody(request)
        }
        
        // 解析百炼原生 SSE 格式
        // 格式: data: {"output":{"text":"..."},"usage":{},"request_id":"..."}\n\n
        response.bodyAsText().split("\n\n").forEach { chunk ->
            if (chunk.startsWith("data: ")) {
                val data = chunk.substring(6).trim()
                if (data.isNotEmpty()) {
                    try {
                        val dashScopeResp = json.decodeFromString<DashScopeNativeResponse>(data)
                        dashScopeResp.output?.text?.let { text ->
                            emit(text)
                        }
                    } catch (e: Exception) {
                        // 忽略部分块解析错误
                    }
                }
            }
        }
    }
}
