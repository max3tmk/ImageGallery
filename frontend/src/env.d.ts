/// <reference types="vite/client" />

interface ImportMetaEnv {
    readonly VITE_API_URL: string;
    readonly VITE_API_BASE_URL: string;
    readonly VITE_SERVER_NAME: string;
    readonly VITE_NGINX_PORT: string;
    readonly VITE_GATEWAY_SERVICE: string;
}

interface ImportMeta {
    readonly env: ImportMetaEnv;
}