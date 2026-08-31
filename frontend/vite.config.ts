import react from '@vitejs/plugin-react'
import { defineConfig } from 'vite'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    proxy: {
      '^/(entries|search|suggestions|playlist|oauth2|login|me)': 'http://127.0.0.1:8080',
    }
  }
})
