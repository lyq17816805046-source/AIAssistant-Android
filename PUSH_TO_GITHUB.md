# 🚀 一键推送到 GitHub

## 方法一：使用 GitHub CLI（推荐）

```bash
# 安装 gh CLI
# macOS: brew install gh
# Ubuntu: sudo apt install gh

# 登录 GitHub
gh auth login

# 执行一键创建并推送
bash setup-github.sh <你的 GitHub 用户名> <仓库名>
```

## 方法二：手动创建

1. 在 GitHub 上创建新仓库
2. 复制以下命令：

```bash
cd /workspace/ai-client-android

# 设置远程仓库（替换为你的用户名和仓库名）
git remote add origin https://github.com/<username>/<repo>.git

# 推送到 GitHub
git branch -M main
git push -u origin main
```

## 编译 APK

推送后，GitHub Actions 会自动编译。你也可以手动编译：

```bash
./gradlew assembleDebug
```

APK 位置：`app/build/outputs/apk/debug/app-debug.apk`
