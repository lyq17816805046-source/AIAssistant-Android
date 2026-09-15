package com.ai.client.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ai.client.api.*
import com.ai.client.api.models.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class ChatMessage(
    val role: String,
    val content: String
)

class ChatViewModel : ViewModel() {
    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _currentProvider = MutableStateFlow<ProviderType>(ProviderType.OpenAI)
    val currentProvider: StateFlow<ProviderType> = _currentProvider.asStateFlow()
    
    // 模型列表相关状态
    private val _availableModels = MutableStateFlow<Map<ProviderType, List<AppModelInfo>>>(emptyMap())
    val availableModels: StateFlow<Map<ProviderType, List<AppModelInfo>>> = _availableModels.asStateFlow()
    
    var showSettings = false
    
    private val conversationHistory = mutableListOf<ChatMessage>()
    
    init {
        // 初始化时加载各提供商的模型列表
        loadAllModels()
    }
    
    fun setProvider(provider: ProviderType) {
        _currentProvider.value = provider
    }
    
    /**
     * 加载所有可用模型
     */
    private fun loadAllModels() {
        viewModelScope.launch {
            try {
                val models = mutableMapOf<ProviderType, List<AppModelInfo>>()
                
                // 并行加载各提供商的模型列表
                val openAiDeferred = async { 
                    ApiManager.getOpenAIClient()?.let { OpenAIClient.listModels(it) } ?: emptyList()
                }
                
                val dashScopeDeferred = async { 
                    ApiManager.getDashScopeClient()?.let { DashScopeNativeClient.listModels(it) } ?: emptyList()
                }
                
                val zhipuDeferred = async { 
                    ApiManager.getZhipuClient()?.let { ZhipuNativeClient.listModels(it) } ?: emptyList()
                }
                
                models[ProviderType.OpenAI] = openAiDeferred.await()
                models[ProviderType.DashScope] = dashScopeDeferred.await()
                models[ProviderType.Zhipu] = zhipuDeferred.await()
                
                _availableModels.value = models
            } catch (e: Exception) {
                // 忽略错误，保持空列表
            }
        }
    }
    
    /**
     * 获取当前提供商的模型列表
     */
    fun getCurrentProviderModels(): List<AppModelInfo> {
        return _availableModels.value[_currentProvider.value] ?: emptyList()
    }
    
    /**
     * 重新加载模型列表
     */
    fun reloadModels() {
        loadAllModels()
    }
    
    fun sendMessage(text: String) {
        viewModelScope.launch {
            _isLoading.value = true
            
            // 添加用户消息
            val userMsg = ChatMessage("user", text)
            conversationHistory.add(userMsg)
            _messages.value = _messages.value + userMsg
            
            try {
                when (val provider = _currentProvider.value) {
                    is ProviderType.OpenAI -> handleOpenAI(text)
                    is ProviderType.DashScope -> handleDashScope(text)
                    is ProviderType.Zhipu -> handleZhipu(text)
                }
            } catch (e: Exception) {
                _messages.value = _messages.value + ChatMessage(
                    "assistant", 
                    "❌ 错误：${e.message}"
                )
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    private suspend fun handleOpenAI(text: String) {
        val client = ApiManager.getOpenAIClient() ?: run {
            _messages.value = _messages.value + ChatMessage(
                "assistant",
                "⚠️ 请先在设置中配置 OpenAI API Key"
            )
            return
        }
        
        val config = getProviderConfig(ProviderType.OpenAI)
        val openAiMessages = conversationHistory.map { msg ->
            com.ai.client.api.models.Message(msg.role, msg.content)
        }
        
        // 流式输出
        var fullResponse = ""
        OpenAIClient.chatStream(client, config.defaultModel, openAiMessages, null, null).collect { chunk ->
            fullResponse += chunk
            _messages.value = _messages.value + ChatMessage("assistant", fullResponse)
        }
        
        conversationHistory.add(ChatMessage("assistant", fullResponse))
    }
    
    private suspend fun handleDashScope(text: String) {
        val client = ApiManager.getDashScopeClient() ?: run {
            _messages.value = _messages.value + ChatMessage(
                "assistant",
                "⚠️ 请先在设置中配置阿里云百炼 API Key"
            )
            return
        }
        
        val config = getProviderConfig(ProviderType.DashScope)
        val dashScopeMessages = conversationHistory.map { msg ->
            DashScopeInputMessage(msg.role, msg.content)
        }
        
        var fullResponse = ""
        DashScopeNativeClient.chatStream(client, config.defaultModel, dashScopeMessages, null, null).collect { chunk ->
            fullResponse += chunk
            _messages.value = _messages.value + ChatMessage("assistant", fullResponse)
        }
        
        conversationHistory.add(ChatMessage("assistant", fullResponse))
    }
    
    private suspend fun handleZhipu(text: String) {
        val client = ApiManager.getZhipuClient() ?: run {
            _messages.value = _messages.value + ChatMessage(
                "assistant",
                "⚠️ 请先在设置中配置智谱 API Key"
            )
            return
        }
        
        val config = getProviderConfig(ProviderType.Zhipu)
        val zhipuMessages = conversationHistory.map { msg ->
            ZhipuMessage(msg.role, msg.content)
        }
        
        var fullResponse = ""
        ZhipuNativeClient.chatStream(client, config.defaultModel, zhipuMessages, null, null, null).collect { chunk ->
            fullResponse += chunk
            _messages.value = _messages.value + ChatMessage("assistant", fullResponse)
        }
        
        conversationHistory.add(ChatMessage("assistant", fullResponse))
    }
    
    private fun getProviderConfig(type: ProviderType): ProviderConfig {
        // TODO: 从 SharedPreferences 读取配置
        return when (type) {
            is ProviderType.OpenAI -> ProviderConfig(
                type, "", "https://api.openai.com/v1", "gpt-4o"
            )
            is ProviderType.DashScope -> ProviderConfig(
                type, "", "https://dashscope.aliyuncs.com", "qwen-plus"
            )
            is ProviderType.Zhipu -> ProviderConfig(
                type, "", "https://open.bigmodel.cn/api/paas/v4", "glm-4"
            )
        }
    }
    
    fun clearHistory() {
        conversationHistory.clear()
        _messages.value = emptyList()
    }
}
