import { defineStore } from 'pinia';

export const useAppStore = defineStore('app', {
    state: () => ({
        project: '',
    }),
    persist: {
        enabled: true,
        strategies: [
            {
                key: 'project',
            },
        ],
    }
});