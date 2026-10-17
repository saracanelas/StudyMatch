import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import tailwindcss from '@tailwindcss/vite'

export default defineConfig({
  plugins: [react(), tailwindcss()],
  server: {
    // Forwards /api requests to the Spring backend, so the browser sees a single origin and no CORS is needed
    proxy: {
      '/api': 'http://localhost:8080',
    },
  },
})
