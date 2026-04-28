# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

```sh
npm install          # Install dependencies
npm run dev          # Start dev server on port 5173 with HMR
npm run build        # Production build to dist/
npm run preview      # Preview production build locally
```

No test runner, linter, or type-checker is configured. There is no CI pipeline.

## Architecture

**campus-market** is a campus second-hand trading SPA — Vue 3 + Vite 7 + Element Plus 2.x, with a Java backend at `api.guying.xyz`.

### Stack

- **Vue 3** (Composition API, `<script setup>`)
- **Vue Router 4** with `createWebHistory` — routes: `/login`, `/market`, `/user`, root redirects to `/market`
- **Element Plus 2.x** with auto-import via `unplugin-auto-import` + `unplugin-vue-components` — components and icons are available globally without manual imports
- **Axios** for HTTP, wrapped in `src/utils/request.js`
- **Vite 7** with `@` alias pointing to `src/`

### Routing & Auth

`src/router/index.js` has a global `beforeEach` guard: if `access_token` exists in localStorage, visiting `/login` redirects to `/market`; if no token, all routes except `/login` redirect to `/login`.

### Request Layer (`src/utils/request.js`)

Axios instance with:
- `baseURL`: `/api` in dev (proxied to `https://api.guying.xyz` via Vite server proxy), direct `https://api.guying.xyz` in prod
- Request interceptor: attaches `Authorization` header from `localStorage.getItem('access_token')`
- Response interceptor: on 401/403, attempts token refresh via `/user/refresh-token` using `refresh_token`. Queues concurrent requests during refresh. If refresh fails, clears localStorage and redirects to `/login`.
- All API calls go through this instance. Import as `import request from '@/utils/request'`.

### Views

- **Login.vue** — Login/register page. On success stores `access_token`, `refresh_token`, and `user_info` (partial user object minus tokens) in localStorage, then redirects to `/market`.
- **Market.vue** — Main product listing with keyword search, category radio buttons, price/latest sorting, price range filter, paginated grid. Product detail dialog loads description statically and price/view count dynamically. Purchase flow: create order → prompt payment. Also houses an **AI chat assistant** (floating button → chat window with sidebar history, SSE streaming via native `fetch`, manual token refresh delegation).
- **UserCenter.vue** — Profile display/edit (avatar upload, nickname, phone, email, address). Three tabs: "交易中心" (buy/sell orders with status filters and actions), "我的发布" (product CRUD with status management). Avatar and product image uploads use a custom `http-request` wrapper to route through the shared Axios instance.

### API Endpoints

Products: `/product/list`, `/product/detail/:id`, `/product/dynamic/:id` (real-time), `/product/my-list`, `/product/save`, `/product/delete/:id`

Orders: `/order/create`, `/order/pay/:id`, `/order/list`, `/order/sold-list`, `/order/pending-order`, `/order/detail/:id`, `/order/cancel/:id`, `/order/confirm/:id`, `/order/ship/:id`

User: `/user/login`, `/user/register`, `/user/info`, `/user/update`, `/user/logout`, `/user/refresh-token`

Files: `/file/upload` (multipart, field name `file`)

AI: `/ai/history`, `/ai/history/:chatId`, `/ai/service?prompt=...&chatId=...` (SSE GET, uses native fetch not axios)

### Key Conventions

- Images stored as comma-separated URL strings; `getMainImage(url)` takes the first URL.
- Order status codes: 0=待付款, 1=待发货, 2=待收货, 3=已完成, 4=已取消.
- Product status codes: 0=出售中, 1=交易中, 2=已下架, 3=交易完成.
- User ID is decoded from the JWT `access_token` payload (field `id`/`userId`/`sub`/`uid`).
- Backend API prefix is stripped: dev proxy rewrites `/api` → `/`, production baseURL appends no prefix.
- The API contract uses underscored field names (e.g., `access_token`, `refresh_token`, `category_id`).
