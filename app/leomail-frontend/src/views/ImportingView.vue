<!-- src/views/ImportingView.vue -->

<template>
  <div v-if="importing" class="importing-view">
    <Spinner />
    <p>Kontakte werden importiert ... Bitte warte einen Moment</p>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue';
import { useRouter } from 'vue-router';
import Spinner from '@/components/Spinner.vue';
import { useImportStatusStore } from '@/stores/import-status.store';
import { Service } from '@/services/service';

const router = useRouter();
const importStatusStore = useImportStatusStore();

const importing = ref(true);

let socket: WebSocket | null = null;

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

/**
 * Verbindet sich mit dem WebSocket-Server und empfängt Importstatus-Updates.
 */
const connectWebSocket = () => {
  const protocol = window.location.protocol === 'https:' ? 'wss' : 'ws';
  const host = window.location.host;
  const wsUrl = `${protocol}://${host}/api/ws/import-status`;

  socket = new WebSocket(wsUrl);

  socket.onopen = () => {
    console.log('WebSocket-Verbindung hergestellt.');
  };

  socket.onmessage = (event) => {
    try {
      const data = JSON.parse(event.data);
      importStatusStore.importing = data.importing;
      console.log('Importstatus aktualisiert:', data.importing);
    } catch (error) {
      console.error('Fehler beim Parsen der WebSocket-Nachricht:', error);
    }
  };

  socket.onclose = () => {
    console.log('WebSocket-Verbindung geschlossen.');
    // Optional: Wiederverbinden versuchen
  };

  socket.onerror = (error) => {
    console.error('WebSocket-Fehler:', error);
  };
};

/**
 * Beobachtet Änderungen im Importing-Status und navigiert bei Abschluss.
 */
watch(
    () => importStatusStore.importing,
    (newVal) => {
      if (!newVal) {
        checkOutlookAuthorization();
      }
    }
);

onMounted(() => {
  connectWebSocket();
});
</script>

<style scoped>
.importing-view {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100vh;
}
</style>