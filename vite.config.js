import { defineConfig } from 'vite';

export default defineConfig({
    root: 'Frontend', // Serve from Frontend directory
    server: {
        port: 8080, // Keep port 8080
        proxy: {
            '/v1': {
                target: 'http://arpeggio.up.railway.app',
                changeOrigin: true,
                secure: false,
            },
        },
    },
});
