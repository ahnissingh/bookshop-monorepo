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


let isRefreshing = false;
let failedQueue = [];

const processQueue = (error) => {
    failedQueue.forEach(({ resolve, reject }) => {
        if (error) {
            reject(error);
        } else {
            resolve();
        }
    });
    failedQueue = [];
};

const AUTH_ENDPOINTS = [
    '/auth/login',
    '/auth/refresh',
    '/auth/register/vendor',
    '/auth/register/client',
    '/auth/verify',
    '/auth/resend-verification',
    '/auth/forgot-password',
    '/auth/reset-password',
];

const isAuthEndpoint = (url) =>
    AUTH_ENDPOINTS.some((endpoint) => url?.includes(endpoint));

api.interceptors.response.use(
    (response) => response,
    async (error) => {
        const originalRequest = error.config;

        if (
            error.response?.status !== 401 ||
            originalRequest._retry ||
            isAuthEndpoint(originalRequest.url)
        ) {
            return Promise.reject(error);
        }

        if (isRefreshing) {
            return new Promise((resolve, reject) => {
                failedQueue.push({ resolve, reject });
            })
                .then(() => api(originalRequest))
                .catch((err) => Promise.reject(err));
        }

        originalRequest._retry = true;
        isRefreshing = true;

        try {
            await api.post('/auth/refresh');
            processQueue(null);
            return api(originalRequest);
        } catch (refreshError) {
            processQueue(refreshError);
            store.dispatch(logout());
            localStorage.removeItem('isLoggedIn');
            window.location.href = '/login';
            return Promise.reject(refreshError);
        } finally {
            isRefreshing = false;
        }
    }
);

export default api;