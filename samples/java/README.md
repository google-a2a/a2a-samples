# A2A Java Implementation Samples

This project is a Java implementation example of the Agent-to-Agent (A2A) protocol, providing complete client and server SDKs, along with an AI-powered translation service demonstration application.

## 🏗️ Architecture Overview

```
samples/java/
├── model/                          # A2A Protocol Models & Data Types
├── client-core/                    # A2A Client Core Library  
├── client-example/                 # A2A Client Usage Examples
├── server-core/                    # A2A Server Core Library
├── server-starter/                 # A2A Server Spring Boot Starter
├── server-example-spring-ai/       # Translation Server using Spring AI
├── server-example-google-adk/      # Translation Server using Google ADK
└── pom.xml                        # Parent Maven Configuration
```

## 🚀 Available Demos

### 1. **Spring AI Translation Server** (`server-example-spring-ai`)
- **Framework**: Spring AI with OpenAI/Gemini integration
- **Features**: Multilingual translation with streaming support
- **Languages**: English ↔ Spanish ↔ French ↔ German ↔ Italian
- **Architecture**: Spring Boot + Spring AI ChatClient

### 2. **Google ADK Translation Server** (`server-example-google-adk`)
- **Framework**: Google Agent Development Kit (ADK)
- **Features**: Advanced agent-based translation with LLM integration
- **Languages**: Same multilingual chain
- **Architecture**: Spring Boot + Google ADK LlmAgent

### 3. **A2A Client Examples** (`client-example`)
- **Purpose**: Demonstrates how to interact with A2A servers
- **Features**: Agent discovery, message sending, streaming responses
- **Testing**: Both server implementations

---

## 📋 Prerequisites

### Required Software
- **Java 17+**
- **Maven 3.6+**
- **AI Service Access**: One of the following:
  - OpenAI API Key
  - Google AI Studio API Key  
  - Google Cloud Project (for Vertex AI)

### Environment Setup

#### For Spring AI Demo (OpenAI)
```bash
export OPENAI_API_KEY="your-openai-api-key"
export OPENAI_BASE_URL="https://api.openai.com"  # Optional
export OPENAI_CHAT_MODEL="gpt-4o"              # Optional
```

#### For Google ADK Demo
```bash
# Option 1: Google AI Studio
export GOOGLE_API_KEY="your-google-ai-studio-key"
export GOOGLE_GENAI_USE_VERTEXAI=FALSE

# Option 2: Vertex AI
export GOOGLE_CLOUD_PROJECT="your-gcp-project"
export GOOGLE_CLOUD_LOCATION="us-central1"
export GOOGLE_GENAI_USE_VERTEXAI=TRUE
export GOOGLE_APPLICATION_CREDENTIALS="/path/to/service-account.json"
```

---

## 🎯 Quick Start

### 1. Run AI Translation Server

You can use one of the following frameworks

#### 1.1 Using Spring AI Framework
```bash
cd server-example-spring-ai
mvn spring-boot:run
```
Server will start on `http://localhost:8080`

#### 1.2 Using Google ADK  Framework
```bash
cd server-example-google-adk
mvn spring-boot:run
```
Server will start on `http://localhost:8080`

### 2. Test with A2A Client
```bash
# In a new terminal
cd client-example
mvn compile exec:java
```

