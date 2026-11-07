import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";

// Entwicklung (npm run dev): /api wird an das lokal laufende Backend weitergereicht.
// Im Docker-Betrieb uebernimmt das nginx (siehe nginx.conf).
export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    proxy: {
      "/api": "http://localhost:8080",
    },
  },
});
