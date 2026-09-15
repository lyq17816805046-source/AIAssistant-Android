package com.ai.client

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ai.client.api.DashScopeNativeClient
import com.ai.client.api.OpenAIClient
import com.ai.client.api.ZhipuNativeClient
import com.ai.client.ui.chat.ChatScreen
import com.ai.client.ui.chat.ChatViewModel
import com.ai.client.ui.theme.AIAssistantTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AIAssistantTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val viewModel: ChatViewModel = viewModel()
                    ChatScreen(viewModel = viewModel)
                }
            }
        }
    }
}

// ==================== API 管理器 ====================
object ApiManager {
    private var openAIClient: io.ktor.client.HttpClient? = null
    private var dashScopeClient: io.ktor.client.HttpClient? = null
    private var zhipuClient: io.ktor.client.HttpClient? = null
    
    fun initOpenAI(apiKey: String, baseUrl: String = "https://api.openai.com") {
        openAIClient = OpenAIClient.createClient(apiKey, baseUrl)
    }
    
    fun initDashScope(apiKey: String) {
        dashScopeClient = DashScopeNativeClient.createClient(apiKey)
    }
    
    fun initZhipu(apiKey: String, baseUrl: String = "https://open.bigmodel.cn") {
        zhipuClient = ZhipuNativeClient.createClient(apiKey)
    }
    
    fun getOpenAIClient(): io.ktor.client.HttpClient? = openAIClient
    fun getDashScopeClient(): io.ktor.client.HttpClient? = dashScopeClient
    fun getZhipuClient(): io.ktor.client.HttpClient? = zhipuClient
}
