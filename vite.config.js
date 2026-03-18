import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { fileURLToPath, URL } from 'node:url'
// 下面这些是你可能原有的插件，千万别动
import AutoImport from 'unplugin-auto-import/vite'
import Components from 'unplugin-vue-components/vite'
import { ElementPlusResolver } from 'unplugin-vue-components/resolvers'

export default defineConfig({
  plugins: [
    vue(),
    // ...这里是你原有的插件配置，保留不动...
    AutoImport({ resolvers: [ElementPlusResolver()] }),
    Components({ resolvers: [ElementPlusResolver()] }),
  ],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    }
  },
  
  // ▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼
  // 【只需要补上这一段 server 配置】
  server: {
    port: 5173, // 端口号
    proxy: {
      '/api': {
        target: 'https://api.guying.xyz', 
        changeOrigin: true, // 这个必须为 true，意思是把请求头里的 Host 改成目标域名，完美骗过跨域校验！
        rewrite: (path) => path.replace(/^\/api/, '')
      }
    }
  }
  // ▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲
})