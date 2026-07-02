const rawApiBaseUrl = import.meta.env.VITE_API_BASE_URL;

export const env = {
  apiBaseUrl:
    typeof rawApiBaseUrl === 'string' && rawApiBaseUrl.trim().length > 0
      ? rawApiBaseUrl.replace(/\/$/, '')
      : 'http://localhost:8080',
};
