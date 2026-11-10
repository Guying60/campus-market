# CLAUDE.md

This file provides guidance for working in this monorepo.

## Repository Layout

- `frontend/` — Vue 3 + Vite 7 + Element Plus 2.x SPA (see `frontend/CLAUDE.md` for frontend-specific details).
- `backend/` — Java 21 + Spring Boot 4.0.3 backend (`com.guying.CppApplication`).

## Common Commands

### Frontend (`frontend/`)
```sh
cd frontend
npm install          # Install dependencies
npm run dev          # Start dev server on port 5173 (proxies /api -> VITE_API_BASE_URL)
npm run build        # Production build to frontend/dist/
npm run preview      # Preview production build locally
```

### Backend (`backend/`)
```sh
cd backend
mvn test             # Run unit tests
mvn spring-boot:run  # Start Spring Boot server on port 8080
mvn package          # Package JAR into backend/target/
```

Note: `backend/src/main/resources/application*.yml` is intentionally gitignored to avoid committing secrets (DB credentials, JWT secret, Aliyun OSS keys, AI API keys). Ensure a local `application.yml` exists before running the backend.

## Architecture Overview

- **Frontend ↔ Backend API**: In development, `frontend/vite.config.js` proxies `/api/*` to `http://localhost:8080` (stripping the `/api` prefix). In production, `frontend/.env.production` targets `https://api.guying.xyz`.
- **Auth**: JWT access/refresh tokens handled by `com.guying.interceptor.TokenInterceptor` & `JwtUtils` on the backend, and Axios interceptors in `frontend/src/utils/request.js` on the frontend.
- **Core Backend Modules** (`backend/src/main/java/com/guying/`):
  - `controller/` — REST endpoints for `User`, `Product`, `Order`, `File`, `Ai`, `ChatHistory`
  - `service/` & `mapper/` — MyBatis-Plus business logic & persistence
  - `mq/` & `config/DelayedQueueConfiguration.java` — RabbitMQ order timeout/event handling
  - `config/ElasticsearchConfig.java` — Elasticsearch product indexing/search
  - `repository/` — Redis-backed AI chat memory and history
  - `websocket/` — WebSocket configuration & handler