import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path'

export default defineConfig({
  plugins: [
    vue(),
  ],
  resolve: {
    extensions: ['.ts', '.tsx', '.vue', '.mjs', '.js', '.jsx', '.json'],
    alias: {
      '@': path.resolve(__dirname, 'src'),
    },
  },
  // ─── 依赖预编译：Vite 启动时提前打包所有常用依赖，消除首次访问的按需编译延迟 ───
  // 不加此配置时，每个 import 在浏览器首次请求时才编译，导致"首屏卡分钟"。
  optimizeDeps: {
    include: [
      'vue',
      'vue-router',
      'pinia',
      'axios',
      'vant',
      'vant/lib/index.css',
    ],
    // 排除本地 workspace 包（它们已经是 ESM，无需转换）
    exclude: ['@mall/api-sdk'],
  },
  server: {
    port: 8091,
    // 预热关键 Vue  组件：Vite 启动后立即编译，首次访问无需等待按需编译
    warmup: {
      clientFiles: [
        './src/App.vue',
        './src/views/HomeView.vue',
        './src/views/ProductDetailView.vue',
        './src/views/SearchView.vue',
        './src/router/index.ts',
        './src/views/LoginView.vue',
        './src/stores/user.ts',
      ],
    },
    proxy: {
      '/api': {
        target: 'http://localhost:18080',
        changeOrigin: true,
        rewrite: (p) => p.replace(/^\/api/, ''),
      },
      '/admin-api': {
        target: 'http://localhost:18081',
        changeOrigin: true,
        rewrite: (p) => p.replace(/^\/admin-api/, ''),
      },
    },
  },
})
