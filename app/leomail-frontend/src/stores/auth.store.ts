// src/stores/auth.store.js
import { defineStore } from 'pinia';
import axios from 'axios';
import routerConfig from "@/configs/router.config";
import setupAxiosInterceptors from "@/configs/interceptor.config";
import {useAppStore} from "@/stores/app.store";
import axiosInstance from "@/axiosInstance";

export const useAuthStore = defineStore('auth', {
    state: () => ({
        accessToken: '',
        _refreshToken: '',
    }),
    actions: {
        setTokens(accessToken: string, refreshToken: string) {
            this.accessToken = accessToken;
            this._refreshToken = refreshToken;

            setupAxiosInterceptors();
        },
        logout() {
            this.accessToken = '';
            this._refreshToken = '';
            delete axiosInstance.defaults.headers.common['Authorization'];

            const appStore = useAppStore();
            appStore.project = '';
            routerConfig.push('/login').then(() => {});
        }
    },
    persist: {
        enabled: true,
        strategies: [
            {
                key: 'accessToken',
            },
            {
                key: '_refreshToken',
            },
        ],
    }
});