import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import tailwindcss from '@tailwindcss/vite'
import { fileURLToPath } from 'node:url' // 1. 引入插件

// https://vite.dev/config/
export default defineConfig({
  plugins: [
    vue(),
    tailwindcss(), // 2. 注册插件
  ],
  resolve: {
    // 👈 2. 核心：配置路径别名
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url)) // 将 @ 指向 src 目录
    }
  },
  // 💡 核心：配置本地开发服务器的代理规则 [1.1.2]
  server: {
    proxy: {
      // 只要请求路径是以 /api 开头的，就触发代理 [1.1.2]
      '/api': {
        target: 'http://localhost:8081', //  Java 后端运行端口
        changeOrigin: true,            // 允许跨域
        /* 关键：如果后端 Controller 没有写 /api 这个前缀（后端直接是 /ai ），代理在转发给 8081 时，需要把开头的 /api 用replace方法重写替换为空字符串，反之则不用管
        rewrite: (path) => path.replace(/^\/api/, '')  */
      }
    }
  }
})
