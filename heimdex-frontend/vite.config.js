import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
 
// IMPORTANTE: Troque 'heimdex' pelo nome EXATO do seu repositório no GitHub
// Exemplo: se o repo for github.com/joao/meu-sistema, coloque '/meu-sistema/'
const REPO_NAME = '/heimdex-frontend/'
 
export default defineConfig({
  plugins: [react()],
  base: REPO_NAME,
  server: {
    host: true,
    port: 5173,
    strictPort: true,
  },
  build: {
    outDir: 'dist',
  }
})
