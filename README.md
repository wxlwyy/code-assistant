# DeepReason AI

基于 Spring AI 的深度思考型 AI 对话平台，支持 ReAct 多轮工具调用与 SSE 流式传输。

## 核心特性

- **双模式对话**：标准模式（直接回答） / 深度思考模式（ReAct 多步推理 + 工具调用）
- **ReAct Agent 引擎**：Think → Act 循环，模型自主规划下一步行动，最多 20 步推理
- **可扩展工具链**：已接入网络搜索（Serper.dev）、网页抓取、文件读写、PDF 生成、终端执行等 7 种工具
- **SSE 流式传输**：基于 WebFlux 响应式编程，前端打字机队列平滑展示思考过程
- **对话持久化**：基于 PostgreSQL 的 ChatMemory，支持历史会话回放
- **RAG 检索增强**：集成 PgVector 向量数据库，支持自定义文档加载与上下文增强

## 技术栈

| 层级 | 技术 |
|------|------|
| 后端框架 | Spring Boot 3.2、Spring AI 1.1 |
| AI 模型 | 阿里云 DashScope（通义千问） |
| 数据库 | PostgreSQL + PgVector |
| ORM | MyBatis-Plus 3.5 |
| 前端 | Vue 3 + TypeScript + Vite + Pinia + Tailwind CSS |
| 认证 | JWT + BCrypt |

## 项目结构

```
ai-agent/
├── backend/                          # Spring Boot 后端
│   └── src/main/java/com/wxl/agent/
│       ├── agent/                    # Agent 核心（BaseAgent → ReActAgent → ToolCallAgent → Manus）
│       │   └── model/                # AgentState 状态枚举
│       ├── tool/                     # 工具注册与实现（WebSearch/FileOperation/PDF 等）
│       ├── app/                      # LoveApp 恋爱助手
│       ├── controller/               # REST API + SSE 流式接口
│       ├── service/                  # 会话管理服务
│       ├── config/                   # ChatClient / CORS / RAG 配置
│       ├── rag/                      # 自定义 RAG 文档加载与查询增强
│       ├── advisor/                  # 自定义 Spring AI Advisor
│       └── model/                    # DTO / VO / Entity
├── frontend/                         # Vue 3 前端
│   └── src/
│       ├── pages/                    # ChatWorkspace / Login / Register
│       ├── components/               # GlobalHeader / GlobalSidebar
│       ├── stores/                   # Pinia 状态管理（chat / user）
│       ├── utils/                    # SSE 流式请求工具
│       └── api/                      # 自动生成的 API 客户端
└── image-search-mcp-server/          # MCP 服务模块
```

## 快速开始

### 环境要求

- JDK 17+
- Maven 3.8+
- Node.js 18+
- PostgreSQL 14+（需启用 pgvector 扩展）

### 后端启动

1. 配置 `application.yml` 中的数据库连接和 DashScope API Key
2. 执行 SQL 初始化脚本（chat_memory 表 + tb_chat_session 表 + tb_user 表）
3. 启动后端：

```bash
cd backend
mvn spring-boot:run
```

### 前端启动

```bash
cd frontend
npm install
npm run dev
```

## 设计说明

### Agent ReAct 循环

```
用户提问 → Think（模型分析 + 决定调用哪些工具）
         → Act（执行工具调用 + 结果写回上下文）
         → Think（基于工具结果继续分析）
         → ... 循环直到模型输出最终答案或调用终止工具
```

## License

MIT
