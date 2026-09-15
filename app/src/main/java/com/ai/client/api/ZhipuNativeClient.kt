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

object ZhipuNativeClient {
    // 智谱**真正原生** API 端点
    private const val BASE_URL = "https://open.bigmodel.cn"
    
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
            }
        }
    }
    
    /**
     * 获取智谱 GLM 支持的模型列表
     */
    suspend fun listModels(client: HttpClient): List<AppModelInfo> {
        try {
            // 智谱模型列表 API
            val response = client.get("/api/paas/v4/models").body<ZhipuModelListResponse>()
            
            val models = mutableListOf<AppModelInfo>()
            response.data?.forEach { model ->
                models.add(
                    AppModelInfo(
                        id = model.model,
                        name = model.model,
                        provider = ProviderType.Zhipu,
                        category = ModelCategory.Chat::class.java.simpleName,
                        supportsStream = true
                    )
                )
            }
            
            return models
        } catch (e: Exception) {
            // 如果失败返回空列表
            return emptyList()
        }
    }
    
    /**
     * 调用智谱 GLM 原生 API
     * 端点：POST /api/paas/v4/chat/completions
     */
    suspend fun chat(
        client: HttpClient,
        model: String,
        messages: List<ZhipuMessage>,
        temperature: Double?,
        topP: Double?,
        maxTokens: Int?
    ): ZhipuNativeResponse {
        val request = ZhipuNativeRequest(
            model = model,
            messages = messages,
            temperature = temperature,
            top_p = topP,
            stream = false,
            max_tokens = maxTokens
        )
        
        return client.post("/api/paas/v4/chat/completions") {
            setBody(request)
        }.body()
    }
    
    /**
     * 智谱流式 API
     * 返回 SSE 格式的流
     */
    fun chatStream(
        client: HttpClient,
        model: String,
        messages: List<ZhipuMessage>,
        temperature: Double?,
        topP: Double?,
        maxTokens: Int?
    ): Flow<String> = flow {
        val request = ZhipuNativeRequest(
            model = model,
            messages = messages,
            temperature = temperature,
            top_p = topP,
            stream = true,
            max_tokens = maxTokens
        )
        
        val response = client.submitFormUrlEncoded("/api/paas/v4/chat/completions") {
            setBody(request)
        }
        
        // 解析智谱原生 SSE 格式
        // 格式: data: {"code":0,"msg":"success","data":{"choices":[{"delta":{"content":"..."}}]}}\n\n
        response.bodyAsText().split("\n\n").forEach { chunk ->
            if (chunk.startsWith("data: ")) {
                val data = chunk.substring(6).trim()
                if (data.isNotEmpty()) {
                    try {
                        val zhipuResp = json.decodeFromString<ZhipuNativeResponse>(data)
                        zhipuResp.data.choices?.firstOrNull()?.message?.content?.let { content ->
                            emit(content)
                        }
                    } catch (e: Exception) {
                        // 忽略部分块解析错误
                    }
                }
            }
        }
    }
}

// 智谱模型列表响应
@Serializable
data class ZhipuModelListResponse(
    val code: Int,
    val msg: String,
    val data: List<ZhipuModelItem>? = null
)
