import { defineStore } from 'pinia';
import { ref } from 'vue';
import { Service } from '@/services/service';
import axiosInstance from "@/axiosInstance";

export const useImportStatusStore = defineStore('importStatus', () => {
    const importing = ref(true);

    /**
     * Aktualisiert den Importstatus über die REST-API (falls nötig).
     */
    const updateImportStatus = async () => {
        try {
            const response = await axiosInstance.get('/status/import');
            importing.value = response.data.importing;
            console.log('Importstatus aktualisiert:', importing.value);
        } catch (error) {
            console.error('Fehler beim Aktualisieren des Importstatus:', error);
            importing.value = false;
        }
    };

    return { importing, updateImportStatus };
});