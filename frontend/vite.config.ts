import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

/**
 * Le développement passe par un proxy : le front appelle `/api/...` sur son propre origine, et Vite
 * renvoie vers le backend sur le port 8080. Aucune URL d'API n'est écrite en dur dans le code, et
 * aucun CORS n'est nécessaire.
 */
export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },
});
