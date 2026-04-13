# Gemma Chat — Offline AI for Android

> **100% offline ChatGPT-like experience powered by Gemma 4 E2B Q4 on your Android phone**

## Features

| Feature | Details |
|---|---|
| 🔒 **Fully Offline** | Zero internet required after model setup |
| ⚡ **Streaming Output** | Tokens appear word-by-word in real time |
| 💬 **Multi-Session** | Unlimited saved conversations with auto-titles |
| 📝 **Markdown Rendering** | Bold, italic, code blocks, headings |
| ⚙️ **Full Settings** | Temperature, Top-K, Top-P, Max tokens, context window |
| 🎨 **Material You** | Dynamic color, dark/light theme |
| 📋 **Copy / Regenerate** | Long-press any message for actions |
| 🗑️ **Swipe to Delete** | Swipe conversations to remove |
| 🖊️ **Custom System Prompt** | Tune Gemma's persona |

## Setup Guide

### Step 1: Get the Model File

Download the Gemma 4 E2B model in `.litertlm` format from HuggingFace:

```
https://huggingface.co/litert-community/gemma-4-E2B-it-litert-lm
```

1. Click the **"Files and versions"** tab
2. Download: **`gemma-4-E2B-it.litertlm`** (~2.58 GB)

> You need a free HuggingFace account and must accept the Apache 2.0 license.

### Step 2: Transfer to Phone

- USB transfer (Android File Transfer)
- Google Drive / any cloud storage
- ADB: `adb push gemma-4-E2B-it.litertlm /sdcard/Download/`

### Step 3: Build & Install the App

**Requirements:**
- Android Studio Hedgehog or newer
- Android SDK 35
- Java 17

**Build:**
```bash
cd GemmaChat
./gradlew assembleDebug
adb install app/build/outputs/apk/debug/app-debug.apk
```

### Step 4: Launch & Connect Model

1. Open **Gemma Chat** on your phone
2. Follow the setup wizard
3. Tap **Select Model File** → navigate to your `.litertlm` file
4. Wait ~30–60 seconds for initialization
5. **Start chatting!** 🎉

## Architecture

```
GemmaChat/
├── data/
│   ├── db/          Room database (sessions + messages)
│   ├── inference/   GemmaInferenceEngine (MediaPipe wrapper)
│   └── repository/  ChatRepository, SettingsRepository
├── di/              Hilt modules
├── navigation/      NavGraph + Screen routes
└── ui/
    ├── setup/       First-launch model picker
    ├── chat/        Main chat interface + components
    ├── history/     Conversation list
    ├── settings/    All app settings
    └── theme/       Material You color + typography
```

## Tech Stack

- **Kotlin + Jetpack Compose** (Material 3)
- **MediaPipe LLM Inference API** (`tasks-genai:0.10.27`)
- **Room** for persistent chat history
- **DataStore Preferences** for settings
- **Hilt** for dependency injection
- **Navigation Compose** for screen routing

## Device Requirements

- Android 8.0+ (API 26+)
- 4 GB RAM minimum (8 GB recommended)
- ~2 GB free storage for model

## License

Apache 2.0 — Gemma 4 model is © Google, Apache 2.0 license.
