package com.ai.client.ui.chat

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ai.client.api.models.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    viewModel: ChatViewModel
) {
    val messages by viewModel.messages.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val currentProvider by viewModel.currentProvider.collectAsState()
    
    // 获取当前提供商的可用模型
    val availableModels = remember(currentProvider) {
        viewModel.getCurrentProviderModels()
    }
    
    var selectedModel by remember { mutableStateOf<String?>(null) }
    var inputText by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AI 助手", fontWeight = FontWeight.Bold) },
                actions = {
                    Button(
                        onClick = { viewModel.showSettings = true },
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Icon(Icons.Default.Settings, contentDescription = "设置")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 模型选择区域
            Surface(
                tonalElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // 提供商选择
                    ExposedDropdownMenuBox(
                        expanded = false,
                        onExpandedChange = {}
                    ) {
                        Text(
                            text = when (currentProvider) {
                                is ProviderType.OpenAI -> "OpenAI"
                                is ProviderType.DashScope -> "阿里云百炼"
                                is ProviderType.Zhipu -> "智谱 GLM"
                            },
                            modifier = Modifier.padding(horizontal = 8.dp),
                            fontWeight = FontWeight.Medium
                        )
                        
                        DropdownMenu(
                            expanded = false,
                            onDismissRequest = {}
                        ) {
                            DropdownMenuItem(
                                text = { Text("OpenAI") },
                                onClick = { viewModel.setProvider(ProviderType.OpenAI) }
                            )
                            DropdownMenuItem(
                                text = { Text("阿里云百炼") },
                                onClick = { viewModel.setProvider(ProviderType.DashScope) }
                            )
                            DropdownMenuItem(
                                text = { Text("智谱 GLM") },
                                onClick = { viewModel.setProvider(ProviderType.Zhipu) }
                            )
                        }
                    }
                    
                    // 模型选择
                    if (availableModels.isNotEmpty()) {
                        ExposedDropdownMenuBox(
                            expanded = false,
                            onExpandedChange = {}
                        ) {
                            Text(
                                text = selectedModel ?: "选择模型...",
                                modifier = Modifier.padding(horizontal = 8.dp),
                                style = MaterialTheme.typography.bodyMedium
                            )
                            
                            DropdownMenu(
                                expanded = false,
                                onDismissRequest = {}
                            ) {
                                availableModels.forEach { model ->
                                    DropdownMenuItem(
                                        text = { Text(model.name) },
                                        onClick = { 
                                            selectedModel = model.id
                                            viewModel.setProvider(model.provider)
                                        }
                                    )
                                }
                            }
                        }
                    } else {
                        Text(
                            text = "加载中...",
                            modifier = Modifier.padding(horizontal = 8.dp),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                }
            }
            
            // 消息列表
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(messages.size) { index ->
                    val message = messages[index]
                    MessageBubble(
                        message = message,
                        isUser = message.role == "user"
                    )
                }
                
                if (isLoading) {
                    item {
                        LoadingIndicator()
                    }
                }
            }
            
            // 输入区域
            Surface(
                tonalElevation = 6.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("输入消息...") },
                        maxLines = 4
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Button(
                        onClick = {
                            if (inputText.isNotBlank()) {
                                scope.launch {
                                    viewModel.sendMessage(inputText.trim())
                                    inputText = ""
                                }
                            }
                        },
                        enabled = !isLoading && inputText.isNotBlank()
                    ) {
                        Icon(Icons.Default.Send, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("发送")
                    }
                }
            }
        }
    }
    
    // 设置弹窗
    if (viewModel.showSettings) {
        SettingsDialog(viewModel = viewModel)
    }
}

@Composable
fun MessageBubble(message: ChatMessage, isUser: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 300.dp)
                .padding(8.dp),
            horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
        ) {
            Text(
                text = if (isUser) "你" else "AI",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (isUser) 
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) 
                    else 
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
                )
            ) {
                Text(
                    text = message.content,
                    modifier = Modifier.padding(12.dp),
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
fun LoadingIndicator() {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(24.dp),
            strokeWidth = 2.dp
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text("AI 正在思考...")
    }
}
