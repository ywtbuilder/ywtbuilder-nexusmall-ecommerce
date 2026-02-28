import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path'

export default defineConfig({
  plugins: [vue()],
  resolve: {
    extensions: ['.ts', '.tsx', '.vue', '.mjs', '.js', '.jsx', '.json'],
    alias: {
      '@': path.resolve(__dirname, 'src'),
    },
  },
  server: {
    port: 8090,
    proxy: {
      '/admin-api': {
        target: 'http://localhost:18081',
        changeOrigin: true,
        rewrite: (p) => p.replace(/^\/admin-api/, ''),
      },
    },
  },
})
