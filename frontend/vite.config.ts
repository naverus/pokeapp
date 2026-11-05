/// <reference types="vitest/config" />
import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

const authTarget = process.env.VITE_AUTH_PROXY_TARGET ?? 'http://localhost:8081';
const catalogTarget = process.env.VITE_CATALOG_PROXY_TARGET ?? 'http://localhost:8082';
const collectionTarget = process.env.VITE_COLLECTION_PROXY_TARGET ?? 'http://localhost:8083';

export default defineConfig({
  plugins: [react()],
  build: {
    rollupOptions: {
      output: {
        manualChunks: {
          react: ['react', 'react-dom', 'react-router-dom'],
          mui: ['@mui/material', '@mui/icons-material', '@emotion/react', '@emotion/styled'],
          data: ['@tanstack/react-query', 'axios', 'react-hook-form', 'zod', '@hookform/resolvers'],
        },
      },
    },
  },
  server: {
    port: 5173,
    proxy: {
      '/api/v1/auth': {
        target: authTarget,
        changeOrigin: true,
      },
      '/api/v1/pokemon': {
        target: catalogTarget,
        changeOrigin: true,
      },
      '/api/v1/local-pokemon': {
        target: collectionTarget,
        changeOrigin: true,
      },
      '/api/v1/admin': {
        target: collectionTarget,
        changeOrigin: true,
      },
    },
  },
  test: {
    environment: 'jsdom',
    globals: true,
    setupFiles: ['./src/test/setup.ts'],
    css: false,
    coverage: {
      provider: 'v8',
      include: ['src/**/*.{ts,tsx}'],
      exclude: ['src/main.tsx', 'src/test/**', 'src/**/*.test.{ts,tsx}', 'src/vite-env.d.ts'],
    },
  },
});
