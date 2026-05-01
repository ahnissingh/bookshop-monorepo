import axios from 'axios';

const api = axios.create({
    baseURL: import.meta.env.VITE_API_BASE_URL,
    withCredentials: true,
    headers: {
        'Content-Type': 'application/json',
    }
});

api.interceptors.response.use(
    (response) => response,
    async (error) => {
        const originalRequest = error.config;

        const authEndpoints = [
            '/auth/login',
            '/auth/refresh',
            '/auth/register/vendor',
            '/auth/register/client'
        ];

        const isAuthEndpoint = authEndpoints.some(endpoint => originalRequest.url.includes(endpoint));

        if (error.response?.status === 401 && !originalRequest._retry && !isAuthEndpoint) {
            originalRequest._retry = true;
            try {
                // Only now do we attempt a silent refresh
                await api.post('/auth/refresh');
                return api(originalRequest);
            } catch (refreshError) {
                return Promise.reject(refreshError);
            }
        }

        return Promise.reject(error);
    }
);

export default api;