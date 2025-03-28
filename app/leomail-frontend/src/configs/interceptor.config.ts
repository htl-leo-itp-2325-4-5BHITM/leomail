// src/setupAxiosInterceptors.ts

import axiosInstance from '@/axiosInstance';
import { useAuthStore } from '@/stores/auth.store';
import { refreshToken } from '@/services/auth.service';
import {pinia} from "@/main";

const setupAxiosInterceptors = () => {
    axiosInstance.interceptors.request.use(
        (config) => {
            const authStore = useAuthStore(pinia); 
            const token = authStore.$state.accessToken;

            if (token && !config.headers['Authorization']) {
                config.headers['Authorization'] = `Bearer ${token}`;
            }

            return config;
        },
        (error) => Promise.reject(error)
    );

    axiosInstance.interceptors.response.use(
        (response) => response,
        async (error) => {
            const originalRequest = error.config;
            const authStore = useAuthStore(pinia);

            if (
                error.response &&
                error.response.status === 401 &&
                !originalRequest._retry &&
                !originalRequest.url.includes('/auth/refresh')
            ) {
                originalRequest._retry = true;
                try {
                    const newToken = await refreshToken();
                    axiosInstance.defaults.headers.common['Authorization'] = `Bearer ${newToken.access_token}`;
                    originalRequest.headers['Authorization'] = `Bearer ${newToken.access_token}`;
                    return axiosInstance(originalRequest);
                } catch (err) {
                    authStore.logout();
                    return Promise.reject(err);
                }
            }

            return Promise.reject(error);
        }
    );
};

export default setupAxiosInterceptors;
