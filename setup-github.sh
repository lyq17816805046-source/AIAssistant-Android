#!/bin/bash
# AI 客户端 Android 项目 - GitHub 仓库自动创建脚本
# 用法: bash setup-github.sh <github_username> <repo_name>

set -e

if [ $# -lt 2 ]; then
    echo "❌ 用法: bash setup-github.sh <github_username> <repo_name>"
    exit 1
fi

GITHUB_USER=$1
REPO_NAME=$2

echo "🚀 开始创建 GitHub 仓库..."
echo "   用户名: $GITHUB_USER"
echo "   仓库名: $REPO_NAME"

# 初始化 Git 仓库
cd /workspace/ai-client-android
git init
git add .
git commit -m "Initial commit: AI Assistant Android App with OpenAI, DashScope & Zhipu native APIs"

# 创建远程仓库（使用 GitHub CLI）
echo "📦 正在创建远程仓库..."
gh repo create "$GITHUB_USER/$REPO_NAME" --public --clone || {
    echo "⚠️  GitHub CLI 未配置或认证失败，请手动创建仓库后运行:"
    echo "   git remote add origin https://github.com/$GITHUB_USER/$REPO_NAME.git"
    echo "   git push -u origin main"
}

# 推送到远程仓库
git branch -M main
git remote add origin "https://github.com/$GITHUB_USER/$REPO_NAME.git"
git push -u origin main

echo ""
echo "✅ 完成！仓库地址: https://github.com/$GITHUB_USER/$REPO_NAME"
echo ""
echo "📱 下一步:"
echo "   1. 在 GitHub Actions 中查看编译结果"
echo "   2. 下载 APK 文件"
echo "   3. 在 Android 设备上安装测试"
