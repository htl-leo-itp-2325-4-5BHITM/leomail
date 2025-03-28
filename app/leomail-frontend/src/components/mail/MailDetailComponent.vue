<template>
  <div id="mailDetailContainer">
    <div v-if="mailDetail">
      <div id="mailDetailHeader">
        <h1 id="heading">{{ mailDetail.meta.templateName }} - {{ mailDetail.meta.mailHeadline }}</h1>
      </div>

      <div id="content">
        <div class="info-row">
          <label>Gesendet von:</label>
          <input type="text" :value="senderName" disabled />
        </div>

        <div class="info-row">
          <label>Gesendet am:</label>
          <input type="text" :value="mailDetail.keyDates.sentOn" disabled />
        </div>

        <div class="attachments-section" v-if="attachments.length > 0">
          <h3>Angehängte Dateien:</h3>
          <ul class="attachments-list">
            <li v-for="(attachment, index) in attachments" :key="index" class="attachment-item">
              <i :class="getIconClass(attachment.contentType)" class="file-icon"></i>
              <a href="#" @click.prevent="downloadAttachment(attachment.id)" class="file-link">
                {{ attachment.fileName }}
              </a>
              <span class="file-size">({{ formatFileSize(attachment.size) }})</span>
            </li>
          </ul>
        </div>

        <div v-if="recipients.length > 0" class="recipients-preview">
          <div class="info-row">
            <label>Betreff:</label>
            <input type="text" :value="mailDetail.meta.mailHeadline" disabled />
          </div>

          <div class="info-row">
            <label>An:</label>
            <input type="text" :value="recipientDisplay" disabled />
          </div>

          <div class="email-preview" v-html="filledTemplate"></div>

          <div class="navigation-buttons">
            <span class="arrow prev-arrow" @click="prevRecipient" :class="{ 'disabled': currentRecipientIndex === 0 }"></span>
            <p class="indexing">{{ currentRecipientIndex + 1 }} / {{ recipients.length }}</p>
            <span class="arrow next-arrow" @click="nextRecipient"
                  :class="{ 'disabled': currentRecipientIndex === recipients.length - 1 }"></span>
          </div>
        </div>
      </div>
    </div>

    <div v-else>
      <p>Lade E-Mail...</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { useRoute } from 'vue-router';
import { Service } from '@/services/service';
import { format, parseISO } from 'date-fns';
import {useAppStore} from "@/stores/app.store";

interface Attachment {
  id: string;
  fileName: string;
  contentType: string;
  downloadUrl: string;
  size: number;
}

interface Contact {
  firstName: string;
  lastName: string;
  mailAddress: string;
}

interface MailItem {
  contact: Contact;
  content: string;
}

interface MailDetail {
  meta: {
    templateName: string;
    mailHeadline: string;
  };
  keyDates: {
    sentOn: string;
    scheduledAt?: string;
  };
  attachments: Attachment[];
  mails: MailItem[];
}

// Deklarieren der Props
const props = defineProps({
  id: {
    type: String,
    required: true
  },
  projectId: {
    type: String,
    required: true
  }
});

const route = useRoute();
const mailDetail = ref<MailDetail | null>(null);
const senderName = ref('');
const recipients = ref<Contact[]>([]);
const currentRecipientIndex = ref(0);
const filledTemplate = ref('');

const mailId = ref(route.params.id as string);

const appStore = useAppStore();

// Methode zum Abrufen des Profils
const getProfile = async () => {
  try {
    const response = await Service.getInstance().getProfile();
    senderName.value = `${response.data.firstName} ${response.data.lastName} <${response.data.mailAddress}>`;
  } catch (error) {
    console.error('Fehler beim Abrufen des Profils:', error);
  }
};

// Methode zum Abrufen der Mail-Details
const fetchMailDetail = async () => {
  try {
    const response = await Service.getInstance().getUsedTemplate(mailId.value, appStore.$state.project);
    mailDetail.value = response.data;

    if (mailDetail.value && mailDetail.value.keyDates && mailDetail.value.keyDates.sentOn) {
      const sentOnDate = parseISO(mailDetail.value.keyDates.sentOn);
      mailDetail.value.keyDates.sentOn = format(sentOnDate, 'dd.MM.yyyy, HH:mm');
    }

    if (mailDetail.value && mailDetail.value.mails && mailDetail.value.mails.length > 0) {
      recipients.value = mailDetail.value.mails.map(mailItem => mailItem.contact);
      updateFilledTemplate();
    }
  } catch (error) {
    console.error('Fehler beim Abrufen der Mail-Details:', error);
  }
};

// Navigation zwischen Empfängern
const nextRecipient = () => {
  if (currentRecipientIndex.value < recipients.value.length - 1) {
    currentRecipientIndex.value++;
    updateFilledTemplate();
  }
};

const prevRecipient = () => {
  if (currentRecipientIndex.value > 0) {
    currentRecipientIndex.value--;
    updateFilledTemplate();
  }
};

// Aktualisierung der E-Mail-Vorschau
const updateFilledTemplate = () => {
  const currentMail = mailDetail.value?.mails[currentRecipientIndex.value];

  if (currentMail && currentMail.content) {
    filledTemplate.value = currentMail.content;
  } else {
    filledTemplate.value = '';
  }
};

// Computed Property für den angezeigten Empfänger
const recipientDisplay = computed(() => {
  const recipient = recipients.value[currentRecipientIndex.value];
  return `${recipient.firstName} ${recipient.lastName} <${recipient.mailAddress}>`;
});

// Computed Property für die Anhänge
const attachments = computed(() => {
  return mailDetail.value?.attachments || [];
});

// Methode zur Auswahl des passenden Icons basierend auf dem Content-Type
const getIconClass = (contentType: string): string => {
  const type = contentType.split('/')[1]?.split(';')[0] || 'file';
  switch (type) {
    case 'pdf':
      return 'fas fa-file-pdf';
    case 'vnd.openxmlformats-officedocument.wordprocessingml.document':
    case 'wordprocessingml.document':
      return 'fas fa-file-word';
    case 'vnd.openxmlformats-officedocument.spreadsheetml.sheet':
    case 'spreadsheetml.sheet':
      return 'fas fa-file-excel';
    case 'vnd.openxmlformats-officedocument.presentationml.presentation':
    case 'presentationml.presentation':
      return 'fas fa-file-powerpoint';
    case 'jpeg':
    case 'png':
    case 'gif':
      return 'fas fa-file-image';
    default:
      return 'fas fa-file';
  }
};

// Methode zur Formatierung der Dateigröße
const formatFileSize = (size: number): string => {
  if (size < 1024) return `${size} B`;
  else if (size < 1048576) return `${(size / 1024).toFixed(2)} KB`;
  else if (size < 1073741824) return `${(size / 1048576).toFixed(2)} MB`;
  else return `${(size / 1073741824).toFixed(2)} GB`;
};

// Implementierung der Download-Methode
const downloadAttachment = async (attachmentId: string) => {
  try {
    const response = await Service.getInstance().downloadAttachment(attachmentId);

    // Extrahieren des Dateinamens aus dem Content-Disposition Header
    const disposition = response.headers['content-disposition'];
    let fileName = 'downloaded_file';
    if (disposition && disposition.includes('filename=')) {
      const filenameRegex = /filename[^;=\n]*=((['"]).*?\2|[^;\n]*)/;
      const matches = filenameRegex.exec(disposition);
      if (matches != null && matches[1]) {
        fileName = matches[1].replace(/['"]/g, '');
      }
    }

    // Erstellen eines Blob-Objekts aus der Antwort
    const blob = new Blob([response.data], { type: response.headers['content-type'] });

    // Erstellen einer temporären URL für den Blob
    const url = window.URL.createObjectURL(blob);

    // Erstellen eines temporären <a> Elements zum Initiieren des Downloads
    const link = document.createElement('a');
    link.href = url;
    link.setAttribute('download', fileName); // Setzt den Dateinamen

    // Anhängen des Links zum DOM und Klicken, um den Download zu starten
    document.body.appendChild(link);
    link.click();

    // Entfernen des Links und Freigeben der temporären URL
    link.parentNode?.removeChild(link);
    window.URL.revokeObjectURL(url);
  } catch (error) {
    console.error('Download-Fehler:', error);
    alert('Der Download ist fehlgeschlagen.');
  }
};

onMounted(() => {
  fetchMailDetail();
  getProfile();
});
</script>

<style scoped>
/* Ihre Stile hier */
#mailDetailContainer {
  width: 86.5%;
  margin-top: 2%;
  margin-left: 1.5%;
  display: flex;
  flex-direction: column;
}

#mailDetailHeader {
  display: flex;
  flex-direction: row;
  background-color: white;
  box-shadow: 0 4px 4px 0 rgba(0, 0, 0, 0.2), 0 6px 20px 0 rgba(0, 0, 0, 0.12);
}

#heading {
  margin-left: 2%;
  margin-top: 0.5%;
  margin-bottom: 0.5%;
  font-size: 1.1em;
  font-weight: var(--font-weight-bold);
}

#content {
  margin-top: 1%;
  background-color: white;
  box-shadow: 5px 5px 10px lightgray;
  padding: 2% 3%;
  height: 90%;
}

.info-row {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
}

.info-row label {
  width: 120px;
  font-weight: bold;
  color: #333;
}

.info-row input {
  flex: 1;
  padding: 5px;
  border: 1px solid #ddd;
  border-radius: 4px;
  background-color: #f9f9f9;
  color: #333;
  font-size: 1em;
}

.attachments-section {
  margin-top: 20px;
}

.attachments-list {
  list-style: none;
  padding: 0;
}

.attachment-item {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
}

.file-icon {
  font-size: 24px;
  margin-right: 10px;
  color: #78A6FF; /* Anpassen der Farbe nach Bedarf */
}

.file-link {
  color: #78A6FF;
  text-decoration: none;
  font-weight: bold;
}

.file-link:hover {
  text-decoration: underline;
}

.file-size {
  margin-left: 10px;
  color: #555;
  font-size: 0.9em;
}

.recipients-preview {
  margin-top: 20px;
}

.email-preview {
  margin-top: 20px;
  padding: 20px;
  border: 1px solid #ddd;
  border-radius: 8px;
  background-color: #f9f9f9;
  min-height: 35vh;
  overflow-y: auto;
  color: #333;
}

.navigation-buttons {
  display: flex;
  justify-content: space-between;
  margin-top: 20px;
  margin-bottom: 20px;
}

.arrow {
  display: inline-block;
  width: 10px;
  height: 10px;
  border: solid #78A6FF;
  border-width: 0 3px 3px 0;
  padding: 5px;
  cursor: pointer;
  transform: rotate(45deg);
}

.prev-arrow {
  transform: rotate(135deg);
}

.next-arrow {
  transform: rotate(-45deg);
}

.arrow.disabled {
  border-color: #cccccc;
  cursor: not-allowed;
}
</style>