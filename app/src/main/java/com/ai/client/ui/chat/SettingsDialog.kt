package com.ai.client.ui.chat

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ai.client.ApiManager
import com.ai.client.api.models.ProviderType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsDialog(viewModel: ChatViewModel) {
    var openAIApiKey by remember { mutableStateOf("") }
    var openAiBaseUrl by remember { mutableStateOf("https://api.openai.com") }
    
    var dashScopeApiKey by remember { mutableStateOf("") }
    
    var zhipuApiKey by remember { mutableStateOf("") }
    var zhipuBaseUrl by remember { mutableStateOf("https://open.bigmodel.cn") }
    
    Dialog(onDismissRequest = { viewModel.showSettings = false }) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("⚙️ API 设置", style = MaterialTheme.typography.headlineSmall)
                    IconButton(onClick = { viewModel.showSettings = false }) {
                        Icon(Icons.Default.Close, contentDescription = "关闭")
                    }
                }
                
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                
                // OpenAI 配置
                Text("🔑 OpenAI", style = MaterialTheme.typography.titleMedium)
                OutlinedTextField(
                    value = openAIApiKey,
                    onValueChange = { openAIApiKey = it },
                    label = { Text("API Key") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = openAiBaseUrl,
                    onValueChange = { openAiBaseUrl = it },
                    label = { Text("Base URL") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // 阿里云百炼配置
                Text("🔑 阿里云百炼 (原生)", style = MaterialTheme.typography.titleMedium)
                OutlinedTextField(
                    value = dashScopeApiKey,
                    onValueChange = { dashScopeApiKey = it },
                    label = { Text("API Key") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // 智谱配置
                Text("🔑 智谱 GLM (原生)", style = MaterialTheme.typography.titleMedium)
                OutlinedTextField(
                    value = zhipuApiKey,
                    onValueChange = { zhipuApiKey = it },
                    label = { Text("API Key") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = zhipuBaseUrl,
                    onValueChange = { zhipuBaseUrl = it },
                    label = { Text("Base URL") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 保存按钮
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = {
                            ApiManager.initOpenAI(openAIApiKey, openAiBaseUrl)
                            ApiManager.initDashScope(dashScopeApiKey)
                            ApiManager.initZhipu(zhipuApiKey, zhipuBaseUrl)
                            
                            // 保存后重新加载模型列表
                            viewModel.reloadModels()
                            
                            viewModel.showSettings = false
                        }
                    ) {
                        Text("💾 保存并刷新模型")
                    }
                }
            }
        }
    }
}
