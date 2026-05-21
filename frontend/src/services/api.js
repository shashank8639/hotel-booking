/**
 * Shared Axios instance for the Hotel Booking SPA.
 * Base URL points at Spring Boot context-path `/api` (see vite proxy + .env).
 */
import axios from 'axios';
import { tokenStorage } from '../auth/tokenStorage';

const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 15000,
  headers: {
    'Content-Type': 'application/json',
    Accept: 'application/json',
  },
});

let refreshPromise = null;
let onUnauthorized = null;

/**
 * AuthContext registers a callback so interceptors can clear React state on forced logout.
 */
export function registerUnauthorizedHandler(handler) {
  onUnauthorized = handler;
}

api.interceptors.request.use(
  (config) => {
    const token = tokenStorage.getAccessToken();
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

api.interceptors.response.use(
  (response) => response,
  async (error) => {
    const original = error.config;
    const status = error.response?.status;

    if (status !== 401 || !original || original._retry) {
      return Promise.reject(normalizeApiError(error));
    }

    // Do not try to refresh the refresh/login calls themselves
    const url = original.url || '';
    if (url.includes('/auth/login') || url.includes('/auth/register') || url.includes('/auth/refresh')) {
      return Promise.reject(normalizeApiError(error));
    }

    const refreshToken = tokenStorage.getRefreshToken();
    if (!refreshToken) {
      tokenStorage.clear();
      onUnauthorized?.();
      return Promise.reject(normalizeApiError(error));
    }

    original._retry = true;

    try {
      // Single-flight refresh so parallel 401s share one refresh call
      if (!refreshPromise) {
        refreshPromise = axios
          .post(
            `${api.defaults.baseURL}/auth/refresh`,
            { refreshToken },
            { headers: { 'Content-Type': 'application/json' } }
          )
          .then((res) => {
            const data = res.data;
            tokenStorage.saveSession(
              {
                accessToken: data.accessToken,
                refreshToken: data.refreshToken,
                user: data.user || tokenStorage.getUser(),
              },
              tokenStorage.isRememberMe()
            );
            return data.accessToken;
          })
          .finally(() => {
            refreshPromise = null;
          });
      }

      const newAccessToken = await refreshPromise;
      original.headers.Authorization = `Bearer ${newAccessToken}`;
      return api(original);
    } catch (refreshError) {
      tokenStorage.clear();
      onUnauthorized?.();
      return Promise.reject(normalizeApiError(refreshError));
    }
  }
);

export function normalizeApiError(error) {
  const message =
    error.response?.data?.message ||
    error.response?.data?.error ||
    error.message ||
    'Request failed';
  const normalized = new Error(message);
  normalized.status = error.response?.status;
  normalized.details = error.response?.data;
  return normalized;
}

export default api;
