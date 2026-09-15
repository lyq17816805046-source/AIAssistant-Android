package com.ai.client.api.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// ==================== OpenAI (原生) ====================
@Serializable
data class OpenAIChatRequest(
    val model: String,
    val messages: List<Message>,
    val temperature: Double? = null,
    val max_tokens: Int? = null,
    val stream: Boolean = true
)

@Serializable
data class Message(
    val role: String,
    val content: String
)

@Serializable
data class OpenAIChatResponse(
    val id: String,
    val `object`: String,
    val created: Long,
    val model: String,
    val choices: List<Choice>,
    val usage: Usage? = null
)

@Serializable
data class Choice(
    val index: Int,
    val message: AssistantMessage,
    val finish_reason: String?
)

@Serializable
data class AssistantMessage(
    val role: String,
    val content: String
)

@Serializable
data class Usage(
    val prompt_tokens: Int,
    val completion_tokens: Int,
    val total_tokens: Int
)

// ==================== 阿里云百炼 DashScope (原生 API，非兼容层) ====================
// 百炼原生 API 端点: POST https://dashscope.aliyuncs.com/api/v1/services/aigc/text-generation/generation
// 请求体格式完全不同！

@Serializable
data class DashScopeNativeRequest(
    val model: String,
    val input: Input,
    val parameters: Parameters? = null
)

@Serializable
data class Input(
    val messages: List<DashScopeInputMessage>
)

@Serializable
data class DashScopeInputMessage(
    val role: String,
    val content: String // 支持 string 或 array[TextContent]
)

@Serializable
data class TextContent(
    val text: String,
    val type: String // "text"
)

@Serializable
data class DashScopeNativeParameters(
    val result_format: String? = "message",
    val temperature: Double? = null,
    val top_p: Double? = null,
    val max_tokens: Int? = null,
    val seed: Int? = null,
    val stop: List<String>? = null,
    val repetition_penalty: Double? = null,
    val top_k: Int? = null
)

@Serializable
data class DashScopeNativeResponse(
    val output: Output,
    val usage: DashScopeUsage,
    val request_id: String,
    @SerialName("code") val code: String? = null,
    @SerialName("message") val msg: String? = null
)

@Serializable
data class DashScopeOutput(
    val text: String? = null,
    val messages: List<DashScopeOutputMessage>? = null,
    @SerialName("finish_reason") val finishReason: String? = null
)

@Serializable
data class DashScopeOutputMessage(
    val role: String,
    val content: String
)

@Serializable
data class DashScopeUsage(
    val input_tokens: Int,
    val output_tokens: Int,
    val total_tokens: Int
)

// ==================== 智谱 GLM (原生 API) ====================
// 智谱原生 API 端点: POST https://open.bigmodel.cn/api/paas/v4/chat/completions
// 注意：智谱除了标准消息格式，还支持 extra_messages 等特有字段

@Serializable
data class ZhipuNativeRequest(
    val model: String,
    val messages: List<ZhipuMessage>,
    val temperature: Double? = null,
    val top_p: Double? = null,
    val stream: Boolean = true,
    val max_tokens: Int? = null,
    val response_format: ResponseFormat? = null
)

@Serializable
data class ZhipuMessage(
    val role: String,
    val content: String
)

@Serializable
data class ResponseFormat(
    val type: String // "text"
)

@Serializable
data class ZhipuNativeResponse(
    val code: Int,
    val msg: String,
    val data: ZhipuData
)

@Serializable
data class ZhipuData(
    val id: String,
    val `object`: String,
    val created: Long,
    val model: String,
    val choices: List<ZhipuChoice>,
    val usage: ZhipuUsage
)

@Serializable
data class ZhipuChoice(
    val index: Int,
    val message: ZhipuAssistantMessage,
    val finish_reason: String?
)

@Serializable
data class ZhipuAssistantMessage(
    val role: String,
    val content: String
)

@Serializable
data class ZhipuUsage(
    val prompt_tokens: Int,
    val completion_tokens: Int,
    val total_tokens: Int
)

// ==================== Provider Type ====================
sealed class ProviderType {
    object OpenAI : ProviderType()
    object DashScope : ProviderType()
    object Zhipu : ProviderType()
}
