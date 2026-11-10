# Campus Market (校园二手交易平台)

校园二手交易平台全栈项目（Monorepo），整合了前端 Vue 3 单页应用与后端 Spring Boot 服务。

## 项目结构

```text
campus-market/
├── frontend/          # 前端项目 (Vue 3 + Vite 7 + Element Plus)
├── backend/           # 后端项目 (Java 21 + Spring Boot 4 + MyBatis-Plus + Spring AI)
├── CLAUDE.md          # AI 协作开发指引
└── README.md          # 项目说明文档
```

## 技术栈

### 前端 (`frontend/`)
- **框架**: Vue 3 (`<script setup>` Composition API) + Vite 7
- **UI 组件库**: Element Plus 2.x (自动按需引入)
- **路由与请求**: Vue Router 4 + Axios（含 JWT 双 Token 无感刷新机制）
- **特色功能**: 商品检索与筛选、下单/支付/发货/确认收货交易流、个人中心与商品管理、SSE 流式 AI 客服助手

### 后端 (`backend/`)
- **基础框架**: Java 21 + Spring Boot 4.0.3 + MyBatis-Plus + PageHelper
- **中间件与存储**: MySQL、Redis（缓存与 AI 对话历史存储）、RabbitMQ（订单延迟超时队列）、Elasticsearch（商品搜索）
- **对象存储与认证**: Aliyun OSS（图片上传）、JWT (JJWT) + jBCrypt
- **AI 与实时通信**: Spring AI (OpenAI / DashScope 兼容接口)、WebSocket

## 本地开发指南

### 1. 环境准备
- Node.js `^20.19.0` 或 `>=22.12.0`
- JDK 21 & Maven 3.9+
- 依赖服务：MySQL、Redis、RabbitMQ、Elasticsearch (`localhost:9200`)

### 2. 启动后端 (`backend/`)
1. 在 `backend/src/main/resources/` 下创建 `application.yml`（需配置数据库连接、Redis、RabbitMQ、Aliyun OSS、JWT 密钥 `jwt.secret` 及 Spring AI 模型配置；可参考本地环境配置或代码中的 `@Value` 注入项）。
2. 启动主类 `com.guying.CppApplication` 或在 `backend/` 目录运行：
   ```sh
   cd backend
   mvn spring-boot:run
   ```
   后端默认运行在 `http://localhost:8080`。

### 3. 启动前端 (`frontend/`)
```sh
cd frontend
npm install
npm run dev
```
前端开发服务器默认运行在 `http://localhost:5173`，Vite 会自动将 `/api` 请求代理到 `.env.development` 中配置的 `VITE_API_BASE_URL`（默认 `http://localhost:8080`）。

### 构建前端生产产物
```sh
cd frontend
npm run build
```