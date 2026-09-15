# 🤖 AI 助手 Android 客户端

一个**纯原生 Kotlin + Jetpack Compose** 的 AI 聊天应用，支持三大 AI 提供商：

## ✨ 特性

- 🔑 **OpenAI** - 使用 OpenAI 原生 `/v1/chat/completions` API
- 🔑 **阿里云百炼** - 使用 DashScope 原生 HTTP API（非兼容层）
- 🔑 **智谱 GLM** - 使用智谱原生端点 `open.bigmodel.cn/api/paas/v4`
- 🎨 现代化深色 UI，基于 Material Design 3
- 💬 实时流式输出
- ⚙️ 灵活切换不同 AI 提供商

## 📋 架构

```
app/src/main/java/com/ai/client/
├── api/
│   ├── models/          # 各厂商原生数据模型
│   │   └── OpenAIModels.kt
│   ├── OpenAIClient.kt  # OpenAI 原生客户端
│   ├── DashScopeNativeClient.kt  # 百炼原生客户端
│   └── ZhipuNativeClient.kt     # 智谱原生客户端
├── ui/
│   ├── chat/            # 聊天界面
│   └── theme/           # 主题配置
└── MainActivity.kt      # 主入口
```

## 🛠️ 技术栈

- **语言**: Kotlin
- **UI**: Jetpack Compose
- **网络**: Ktor Client (OkHttp)
- **序列化**: kotlinx.serialization
- **协程**: Kotlin Coroutines
- **架构**: MVVM + Flow

## 📦 编译

### 本地编译
```bash
./gradlew assembleDebug
```

### GitHub Actions 云编译
推送代码到 GitHub 后，GitHub Actions 会自动编译并生成 APK。

## 🔧 配置 API Key

在应用设置中配置：

| 提供商 | Base URL | 端点 |
|--------|----------|------|
| OpenAI | `https://api.openai.com` | `/v1/chat/completions` |
| 阿里云百炼 | `https://dashscope.aliyuncs.com` | `/api/v1/services/aigc/text-generation/generation` |
| 智谱 | `https://open.bigmodel.cn` | `/api/paas/v4/chat/completions` |

## 📱 运行要求

- Android 8.0+ (API 26+)
- Kotlin 17
- JDK 17

## 🚀 快速开始

1. 克隆仓库
2. 配置 API Keys
3. 运行或编译 APK

```bash
bash setup-github.sh <github_username> <repo_name>
```

## 📄 License

MIT
