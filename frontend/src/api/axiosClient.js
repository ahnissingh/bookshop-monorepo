import axios from 'axios';
import { store } from '../store/store';
import { logout } from '../store/authSlice';

const api = axios.create({
    baseURL: import.meta.env.VITE_API_BASE_URL,
    withCredentials: true,
    headers: {
        'Content-Type': 'application/json',
    },
});

// ─── Refresh Token Mutex ───────────────────────────────────────────
// Prevents multiple concurrent 401s from each firing their own
// refresh call. The first 401 triggers the refresh; all others
// queue up and resolve/reject together once it completes.
let isRefreshing = false;
let failedQueue = [];

const processQueue = (error, success = false) => {
    failedQueue.forEach(({ resolve, reject }) => {
        if (error) {
            reject(error);
        } else {
            resolve();
        }
    });
    failedQueue = [];
};

// ─── Auth endpoints that should NEVER trigger a refresh ────────────
const AUTH_ENDPOINTS = [
    '/auth/login',
    '/auth/refresh',
    '/auth/register/vendor',
    '/auth/register/client',
];

const isAuthEndpoint = (url) =>
    AUTH_ENDPOINTS.some((endpoint) => url?.includes(endpoint));

// ─── Response Interceptor ──────────────────────────────────────────
api.interceptors.response.use(
    (response) => response,
    async (error) => {
        const originalRequest = error.config;

        // Only intercept 401s that aren't auth endpoints and haven't retried
        if (
            error.response?.status !== 401 ||
            originalRequest._retry ||
            isAuthEndpoint(originalRequest.url)
        ) {
            return Promise.reject(error);
        }

        // If a refresh is already in flight, queue this request
        if (isRefreshing) {
            return new Promise((resolve, reject) => {
                failedQueue.push({ resolve, reject });
            })
                .then(() => {
                    // Refresh succeeded — retry the original request
                    return api(originalRequest);
                })
                .catch((err) => {
                    return Promise.reject(err);
                });
        }

        // This is the first 401 — take the lock and refresh
        originalRequest._retry = true;
        isRefreshing = true;

        try {
            await api.post('/auth/refresh');

            // Refresh succeeded — process all queued requests
            processQueue(null, true);

            // Retry the original request
            return api(originalRequest);
        } catch (refreshError) {
            // Refresh failed — the session is dead
            processQueue(refreshError);

            // Clean up auth state
            store.dispatch(logout());
            localStorage.removeItem('isLoggedIn');

            // Redirect to login (works outside React tree)
            window.location.href = '/login';

            return Promise.reject(refreshError);
        } finally {
            isRefreshing = false;
        }
    }
);

export default api;