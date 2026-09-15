package com.ai.client.api.models

import kotlinx.serialization.Serializable

// ==================== 通用模型列表请求/响应 ====================

@Serializable
data class ModelListRequest(
    val object_type: String? = null,
    val page: Int? = null,
    val page_size: Int? = null
)

@Serializable
data class ModelItem(
    val id: String,
    val object_type: String = "model", // OpenAI 用 object，其他可能不同
    val created: Long? = null,
    val owned_by: String? = null,
    val permission: List<Permission>? = null
)

@Serializable
data class Permission(
    val id: String,
    val created: Long,
    val allow_create_engine: Boolean,
    val allow_transaction: Boolean,
    val allow_read: Boolean,
    val organization: String,
    val group: String?,
    val is_blocking: Boolean
)

@Serializable
data class ModelListResponse(
    val data: List<ModelItem>,
    val object_type: String = "list"
)

// ==================== 各厂商特有模型字段 ====================

@Serializable
data class OpenAIModelItem(
    val id: String,
    val object_type: String = "model",
    val created: Long,
    val owned_by: String
) {
    @kotlinx.serialization.SerialName("object")
    val `object`: String get() = object_type
}

@Serializable
data class DashScopeModelItem(
    val model_name: String,
    val model_type: String,
    val author: String,
    val available: Boolean,
    val default_endpoint: String
)

@Serializable
data class ZhipuModelItem(
    val model: String,
    val description: String,
    val context_window: Int,
    val input_modalities: List<String>,
    val output_modalities: List<String>,
    val pricing: Map<String, String>? = null
)

// ==================== 统一的模型信息（用于 UI）====================

@Serializable
data class AppModelInfo(
    val id: String,
    val name: String,
    val provider: ProviderType,
    val category: String, // "chat", "image", "embedding" etc.
    val maxTokens: Int? = null,
    val supportsStream: Boolean = true
)

// ==================== 模型类型枚举 ====================
sealed class ModelCategory {
    object Chat : ModelCategory()
    object Image : ModelCategory()
    object Embedding : ModelCategory()
    object Audio : ModelCategory()
}
