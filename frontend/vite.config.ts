import { defineConfig, loadEnv } from 'vite';
import react from '@vitejs/plugin-react';
import { resolve } from 'node:path';

/**
 * Le développement passe par un proxy : le front appelle `/api/...` sur son propre origine, et Vite
 * renvoie vers le backend sur le port 8080. Aucune URL d'API n'est écrite en dur dans le code, et
 * aucun CORS n'est nécessaire.
 *
 * Configuration locale : le `.env` est unique et vit à la racine du dépôt (cf. `.env.example`), pour
 * que Spring Boot, Vite et Docker Compose ne lisent pas trois fichiers différents qui finiraient par
 * se contredire. Vite, lui, cherche par défaut un `.env` dans `frontend/` : on lui indique donc
 * explicitement la racine.
 *
 * La priorité reste celle des variables d'environnement, pour la ligne de commande :
 *   VITE_API_TARGET=http://localhost:8081 npm run dev
 */
const racine = resolve(import.meta.dirname, '..');

export default defineConfig(({ mode }) => {
  const depuisLeEnv = loadEnv(mode, racine, 'VITE_');
  const cibleApi =
    process.env.VITE_API_TARGET ?? depuisLeEnv.VITE_API_TARGET ?? 'http://localhost:8080';

  return {
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
  };
});
