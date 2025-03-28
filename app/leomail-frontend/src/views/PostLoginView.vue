<!-- src/views/PostLoginView.vue -->

<template>
  <div class="post-login">
    <Spinner /> <!-- Optional: Fügen Sie einen Spinner oder Ladeindikator hinzu -->
    <p>Überprüfung der Outlook-Autorisierung...</p>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { Service } from '@/services/service';
import Spinner from '@/components/Spinner.vue';
import {useImportStatusStore} from "@/stores/import-status.store";

const router = useRouter();
const importStatusStore = useImportStatusStore();
const importing = ref(true);

/**
 * Überprüft den Importstatus und aktualisiert den Store.
 */
const checkImportStatus = async () => {
  try {
    await importStatusStore.updateImportStatus();
    importing.value = importStatusStore.importing;
    if (!importing.value) {
      await checkOutlookAuthorization();
    } else {
      // Weiter warten und erneut prüfen nach 2 Sekunden
      setTimeout(checkImportStatus, 2000);
    }
  } catch (error) {
    console.error('Fehler beim Überprüfen des Importstatus:', error);
    setTimeout(checkImportStatus, 5000); // Wiederholen nach 5 Sekunden
  }
};

/**
 * Überprüft die Outlook-Autorisierung und navigiert entsprechend.
 */
const checkOutlookAuthorization = async () => {
  try {
    const isOutlookAuthorized = await Service.getInstance().checkOutlookAuthorization();
    if (isOutlookAuthorized) {
      router.replace({ name: 'projects' });
    } else {
      router.replace({ name: 'authorisation' });
    }
  } catch (error) {
    console.error('Fehler bei der Überprüfung der Outlook-Autorisierung:', error);
    router.replace({ name: 'authorisation' });
  }
};

onMounted(() => {
  checkImportStatus();
});
</script>

<style scoped>
.post-login {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100vh;
}
</style>
