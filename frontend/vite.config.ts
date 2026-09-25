import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

/**
 * Le développement passe par un proxy : le front appelle `/api/...` sur son propre origine, et Vite
 * renvoie vers le backend sur le port 8080. Aucune URL d'API n'est écrite en dur dans le code, et
 * aucun CORS n'est nécessaire.
 */
// Cible surchargeable : si le port 8080 est déjà occupé par un autre service, on démarre l'API
// ailleurs (`SERVER_PORT=8081 ./mvnw spring-boot:run`) et on fait suivre le frontend sans toucher au
// code : `VITE_API_TARGET=http://localhost:8081 npm run dev`.
const cibleApi = process.env.VITE_API_TARGET ?? 'http://localhost:8080';

export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: cibleApi,
        changeOrigin: true,
      },
    },
  },
});
