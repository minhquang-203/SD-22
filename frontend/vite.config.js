import { fileURLToPath, URL } from 'node:url'

import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueDevTools from 'vite-plugin-vue-devtools'
import tailwindcss from '@tailwindcss/vite'

// https://vite.dev/config/
export default defineConfig({
  plugins: [
    vue(),
    vueDevTools(),
    tailwindcss(),
  ],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    },
  },
  server: {
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        secure: false,
        timeout: 180000,
        configure: (proxy) => {
          // Giữ UTF-8 khi proxy JSON (địa chỉ / GHN tiếng Việt)
          proxy.on('proxyRes', (proxyRes) => {
            const ct = proxyRes.headers['content-type']
            if (ct && ct.includes('application/json') && !ct.includes('charset')) {
              proxyRes.headers['content-type'] = 'application/json;charset=UTF-8'
            }
          })
        },
      },
      '/ws': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        secure: false,
        ws: true,
      },
      '/uploads': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        secure: false,
        timeout: 180000,
      },
    },
  },
  define: {
    global: 'globalThis',
  },
})
