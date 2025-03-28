<!-- src/components/mail/MailForm.vue -->


<template>
  <div id="mailFormContainer">
    <div id="formHeader">
      <h1>Neue E-Mail</h1>
    </div>

    <div id="formContent">
      <form @submit.prevent="handlePreview" id="mailForm">

        <!-- Absender Auswahl -->
        <div class="form-group">
          <label for="senderMail" class="form-label">Senden als: </label>
          <div class="input-with-icon">
            <select v-model="selectedSender" class="form-input">
              <option :value="personalMail">{{profileData.firstName}} {{profileData.lastName}} <{{ personalMail }}></option>
              <option :value="projectMail.id">{{ projectMail.displayName}} <{{ projectMail.mailAddress }}></option>
            </select>
            <!--<i class="fas fa-chevron-down"></i>-->
          </div>
        </div>

        <!-- Empfänger Auswahl -->
        <div class="form-group">
          <label for="recipients" class="form-label">Empfänger:</label>
          <div class="input-container-recipients">
            <input
                type="text"
                v-model="searchTerm"
                class="form-input search-input"
                placeholder="Geben Sie den Namen oder Gruppen ein..."
                @focus="fetchUsersAndGroups(searchTerm)"
                @input="onSearchInput"
            />
            <ul
                v-if="searchTerm.length > 0 && (filteredUsers.length || filteredGroups.length)"
                class="autocomplete"
            >
              <li v-for="user in filteredUsers" :key="user.id" @click="selectUser(user)">
                {{ user.firstName }} {{ user.lastName }} - {{ user.mailAddress }}
              </li>

              <li v-for="group in filteredGroups" :key="group.id" @click="selectGroup(group)">
                Gruppe: {{ group.name }}
              </li>
              <li v-if="loading">Laden...</li>
            </ul>
          </div>
        </div>

        <!-- Ausgewählte Empfänger anzeigen -->
        <div id="selectedUsersList">
          <div id="selectedRecipients">
            <div class="tag" v-for="user in selectedUsers" :key="user.id">
              {{ user.firstName }} {{ user.lastName }}
              <span class="tag-remove" @click="removeUser(user)">✕</span>
            </div>
            <div class="tag" v-for="group in selectedGroups" :key="group.id">
              Gruppe: {{ group.name }}
              <span class="tag-remove" @click="removeGroup(group)">✕</span>
            </div>
          </div>
        </div>


        <!-- Sendezeit auswählen -->
        <div class="form-group">
          <label for="sendLater" class="form-label">Senden am:</label>
          <div id="flexSendLaterContainer">
            <div class="input-container flex">
              <input
                  type="checkbox"
                  id="sendLater"
                  v-model="checked"
                  class="form-checkbox"
              />
              <label for="sendLater" class="form-checkbox-label">später senden</label>
            </div>
            <div v-if="checked" class="datetime-picker">
              <VueDatePicker v-if="checked"
                             locale="de-AT"
                             v-model="date"
                             class="datepicker"
                             id="datepicker"
                             now-button-label="Current"
                             format="dd.MM.yyyy"
                             :enable-time-picker="false"
                             placeholder='Datum auswählen'
                             :min-date="new Date()">
              </VueDatePicker>
              <VueDatePicker
                  v-model="time"
                  class="timepicker"
                  time-picker
                  placeholder="Uhrzeit auswählen"
              />
            </div>
          </div>
        </div>

        <!-- Vorlage auswählen -->
        <div class="form-group">
          <label for="template" class="form-label">Vorlage:</label>
          <div id="mailFlexBox">
            <div id="mailTemplateInput" class="input-container">
              <input
                  type="text"
                  v-model="filter"
                  class="form-input search-input"
                  placeholder="Vorlage suchen..."
                  @input="onTemplateSearch"
              />
              <ul v-if="dropdownVisible" class="autocomplete">
                <li
                    v-for="template in fetchedTemplates"
                    :key="template.id"
                    v-show="template.visible"
                    @click="selectTemplate(template)"
                >
                  {{ template.name }}
                </li>
              </ul>
            </div>
          </div>
        </div>

        <!-- Dateien anhängen -->
        <div v-if="selectedTemplate?.filesRequired" class="form-group">
          <label for="attachments" class="form-label">Dateien anhängen:</label>
          <input type="file" id="attachments" multiple required @change="handleFileUpload"/>
          <ul>
            <li v-for="(file, index) in selectedFiles" :key="index">
              {{ file.name }} ({{ formatFileSize(file.size) }})
              <button type="button" @click="removeFile(index)">✕</button>
            </li>
          </ul>
        </div>


        <!-- Vorschau anzeigen Button -->
        <div class="form-actions">
          <button type="button" @click="handlePreview" :disabled="!canPreview" class="btn">
            Weiter
          </button>
        </div>
      </form>

      <!-- Mail Preview Component -->
      <MailPreviewComponent
          v-if="showPreview"
          :selectedTemplate="selectedTemplate"
          :selectedUsers="previewRecipients"
          :personalized="personalized"
          :visible="showPreview"
          :scheduledAt="isScheduled()"
          :attachments="selectedFiles"
          @close="closePreview"
          @send-mail="sendMail"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import {computed, onMounted, ref} from 'vue';
import {Service} from '@/services/service';
import {useAppStore} from '@/stores/app.store';
import {useRouter} from 'vue-router';
import MailPreviewComponent from '@/components/mail/MailPreviewComponent.vue';

interface Template {
  id: number;
  name: string;
  headline: string;
  greeting: string;
  content: string;
  visible: boolean;
  filesRequired: boolean;
}

interface User {
  id: string;
  firstName: string;
  lastName: string;
  mailAddress: string;
  displayName: string;
}

interface Group {
  id: string;
  name: string;
}

const router = useRouter();
const appStore = useAppStore();
const filter = ref('');
const dropdownVisible = ref(false);
const selectedTemplate = ref<Template | null>(null);
const receiverInput = ref('');
const checked = ref(false);
const fetchedTemplates = ref<Template[]>([]);

const selectedUsers = ref<User[]>([]);
const selectedGroups = ref<Group[]>([]);
const searchTerm = ref('');
const users = ref<User[]>([]);
const groups = ref<Group[]>([]);
const loading = ref(false);
const personalized = ref(true);
const showPreview = ref(false);

// Neuer Ref für die Vorschau-Empfänger
const previewRecipients = ref<User[]>([]);

// Neue Referenzen für Absender-Mails
const profileData = ref({
  firstName: '',
  lastName: '',
  mailAddress: '',
  schoolClass: '',
  departement: ''
});
const personalMail = ref<string>('');
const projectMail = ref<{ id: string; mailAddress: string; displayName: string }>({
  id: '',
  mailAddress: '',
  displayName: ''
});
const selectedSender = ref<string>('');

// Neue Referenz für ausgewählte Dateien
const selectedFiles = ref<File[]>([]);

// Fetch Profile Data
const getProfile = async () => {
  try {
    const response = await Service.getInstance().getProfile();
    profileData.value = response.data;
    personalMail.value = profileData.value.mailAddress;
    selectedSender.value = profileData.value.mailAddress;
  } catch (error) {
    console.error('Fehler beim Abrufen des Profils:', error);
  }
};

// Fetch Project Mail Data
const fetchProjectMail = async () => {
  try {
    const response = await Service.getInstance().fetchProjectMailData(appStore.$state.project);
    projectMail.value = response.data;
  } catch (error) {
    console.error('Fehler beim Abrufen der Projekt-Mail-Daten:', error);
  }
};

// Computed Property for Preview Button
//const canPreview = computed(() => (selectedUsers.value.length > 0 || selectedGroups.value.length > 0) && selectedTemplate.value && selectedFiles.value.length > 0);

const canPreview = computed(() => {
  const hasFiles = selectedTemplate?.value?.filesRequired ? selectedFiles.value.length > 0 : true;
  return (selectedUsers.value.length > 0 || selectedGroups.value.length > 0) && selectedTemplate.value && hasFiles;
});

// Handle Preview
const handlePreview = async () => {
  // Überprüfe, ob eine Vorlage ausgewählt wurde und mindestens ein Empfänger vorhanden ist
  if (selectedUsers.value.length === 0 && selectedGroups.value.length === 0) {
    alert('Es sind keine Empfänger für die Vorschau vorhanden.');
    return;
  }

  if (!selectedTemplate.value) {
    alert('Bitte wählen Sie eine Vorlage aus.');
    return;
  }

  if (!validateAttachments()) return;

  try {
    // Erstelle eine Kopie der individuell ausgewählten Nutzer
    let combinedUsers: User[] = [...selectedUsers.value];

    // Wenn Gruppen ausgewählt sind, lade ihre Mitglieder
    if (selectedGroups.value.length > 0) {
      const groupUsersPromises = selectedGroups.value.map(group =>
          Service.getInstance().getUsersInGroup(group.id, appStore.$state.project)
      );

      const groupsResponses = await Promise.all(groupUsersPromises);

      groupsResponses.forEach(response => {
        combinedUsers = combinedUsers.concat(response.data);
      });
    }

    // Entferne doppelte Nutzer basierend auf der Nutzer-ID
    const uniqueUsersMap = new Map<string, User>();
    combinedUsers.forEach(user => {
      uniqueUsersMap.set(user.id, user);
    });
    combinedUsers = Array.from(uniqueUsersMap.values());

    // Setze die Vorschau-Empfänger
    previewRecipients.value = combinedUsers;

    if (previewRecipients.value.length > 0 && selectedTemplate.value) {
      showPreview.value = true;
    } else {
      alert('Es sind keine Empfänger für die Vorschau vorhanden.');
    }
  } catch (error) {
    console.error('Fehler beim Laden der Gruppenmitglieder:', error);
    alert('Fehler beim Laden der Gruppenmitglieder.');
  }
};

// Close Preview
const closePreview = () => {
  showPreview.value = false;
};

/* Date Picker */
const date = ref<Date | null>(null);

const time = ref({
  hours: new Date().getHours().toString().padStart(2, "0"),
  minutes: new Date().getMinutes().toString().padStart(2, "0")
});

const parseDate = () => {
  let d;

  // Wenn kein Datum ausgewählt wurde, aktuelles Datum verwenden
  if (!date.value) {
    d = new Date(); // Heutiges Datum
  } else {
    d = date.value;
  }

  const day = String(d.getDate()).padStart(2, '0');
  const month = String(d.getMonth() + 1).padStart(2, '0');
  const year = d.getFullYear();

  const hours = String(time.value.hours).padStart(2, '0');
  const minutes = String(time.value.minutes).padStart(2, '0');

  console.log(`${year}-${month}-${day}T${hours}:${minutes}:00.000Z`);
  return `${year}-${month}-${day}T${hours}:${minutes}:00.000Z`;
};

const isScheduled = () => {
  if (checked.value == true) {
    return parseDate();
  } else {
    return null;
  }
};

const validateAttachments = () => {
  if (selectedTemplate.value?.filesRequired && selectedFiles.value.length === 0) {
    alert('Bitte laden Sie die erforderlichen Dateien hoch.');
    return false;
  }
  return true;
};

/* Lifecycle Hooks */
onMounted(() => {
  getProfile();
  getTemplates();
  fetchProjectMail();

  document.addEventListener('click', handleClickOutside);
});

/* Click Outside Handler */
const handleClickOutside = (event: MouseEvent) => {
  const target = event.target as HTMLElement;
  const mailTemplateInput = document.getElementById('mailTemplateInput');
  if (mailTemplateInput && !mailTemplateInput.contains(target)) {
    closeDropdown();
  }
};

/* Search Input Handler */
const onSearchInput = () => {
  if (searchTerm.value.length > 0) {
    fetchUsersAndGroups(searchTerm.value);
  } else {
    users.value = [];
    groups.value = [];
  }
};

/* Template Search Handler */
const onTemplateSearch = () => {
  filterFunction();
  dropdownVisible.value = filter.value.length > 0;
};

/* Filter Function for Templates */
const filterFunction = () => {
  const filterValue = filter.value.trim().toLowerCase();
  fetchedTemplates.value.forEach(item => {
    item.visible = item.name.toLowerCase().includes(filterValue);
  });
};

/* Dropdown Visibility Handlers */
const showDropdown = () => {
  dropdownVisible.value = filter.value.length > 0;
};

const closeDropdown = () => {
  dropdownVisible.value = false;
};

/* Template Selection */
const selectTemplate = (template: Template) => {
  selectedTemplate.value = template;
  filter.value = template.name;
  setTimeout(() => {
    closeDropdown();
  }, 0);
};

/* Fetch Templates */
const getTemplates = async () => {
  try {
    const response = await Service.getInstance().getTemplates(appStore.$state.project);
    fetchedTemplates.value = response.data.map((template: any) => ({
      ...template,
      visible: true
    }));
  } catch (error) {
    console.error('Error fetching templates:', error);
  }
};

/* Sort Selected Users */
const sortSelectedUsers = (selectedUsers: User[]): string[] => {
  return selectedUsers.map(user => user.id).sort();
};

/* Methoden für Datei-Handling */

/**
 * Handle File Upload
 * @param event Input Event
 */
const handleFileUpload = (event: Event) => {
  const target = event.target as HTMLInputElement;
  if (target.files) {
    for (let i = 0; i < target.files.length; i++) {
      selectedFiles.value.push(target.files[i]);
    }
  }
};

/**
 * Remove Selected File
 * @param index Index des zu entfernenden Files
 */
const removeFile = (index: number) => {
  selectedFiles.value.splice(index, 1);
};

/**
 * Format File Size
 * @param size Dateigröße in Bytes
 * @returns Formatierte Dateigröße
 */
const formatFileSize = (size: number): string => {
  if (size < 1024) return `${size} B`;
  else if (size < 1048576) return `${(size / 1024).toFixed(2)} KB`;
  else if (size < 1073741824) return `${(size / 1048576).toFixed(2)} MB`;
  else return `${(size / 1073741824).toFixed(2)} GB`;
};

/**
 * Send Mail with Attachments
 */
const sendMail = async () => {
  if (!validateAttachments()) return;

  try {
    const formData = new FormData();
    formData.append('projectId', appStore.$state.project);
    formData.append('smtpInformation', JSON.stringify({
      receiver: {
        groups: selectedGroups.value.map(g => g.id),
        contacts: selectedUsers.value.map(u => u.id)
      },
      templateId: selectedTemplate.value?.id,
      personalized: personalized.value,
      scheduledAt: isScheduled(),
      from: {
        mailType: selectedSender.value === personalMail.value ? 'PERSONAL' : 'PROJECT',
        id: selectedSender.value === personalMail.value ? '' : appStore.$state.project
      }
    }));

    // Fügen Sie Anhänge und deren Metadaten hinzu
    selectedFiles.value.forEach((file, index) => {
      formData.append('attachments', file); // Schlüsselname muss 'attachments' sein
      formData.append('fileName', file.name);
      formData.append('contentType', file.type || 'application/octet-stream');
      formData.append('size', file.size.toString());
    });

    // Debugging: FormData-Inhalte anzeigen
    for (let pair of formData.entries()) {
      console.log(`${pair[0]}: ${pair[1]}`);
    }

    const response = await Service.getInstance().sendMailWithAttachments(formData, appStore.$state.project);

    selectedUsers.value = [];
    selectedGroups.value = [];
    selectedFiles.value = [];

    router.push({ name: 'mail', query: { mailsend: 'true' } });
  } catch (error) {
    console.error('Fehler beim Senden der E-Mail:', error.response?.data || error.message);
    alert('Fehler beim Senden der E-Mail: ' + (error.response?.data || error.message));
  }
};

/* Fetch Users and Groups based on Search Term */
const fetchUsersAndGroups = async (query: string) => {
  loading.value = true;
  try {
    const usersResponse = await Service.getInstance().searchContacts(query);
    users.value = usersResponse.data;

    const groupsResponse = await Service.getInstance().searchGroups(appStore.$state.project, query);
    groups.value = groupsResponse.data;
  } catch (error) {
    console.error('Fehler beim Abrufen von Benutzern oder Gruppen:', error);
    // Optional: Setze eine Fehlermeldung, falls erforderlich
  } finally {
    loading.value = false;
  }
};

const filteredUsers = computed(() => users.value);
const filteredGroups = computed(() => groups.value);

/* Select Group */
const selectGroup = (group: Group) => {
  if (!selectedGroups.value.find(g => g.id === group.id)) {
    selectedGroups.value.push(group);
  }

  searchTerm.value = '';
  users.value = [];
  groups.value = [];
};

/* Select User */
const selectUser = (user: User) => {
  if (!selectedUsers.value.find(u => u.id === user.id)) {
    selectedUsers.value.push(user);
  }
  searchTerm.value = '';
  users.value = [];
  groups.value = [];
};

/* Remove User */
const removeUser = (user: User) => {
  selectedUsers.value = selectedUsers.value.filter(u => u.id !== user.id);
};

/* Remove Group */
const removeGroup = (group: Group) => {
  selectedGroups.value = selectedGroups.value.filter(g => g.id !== group.id);
};
</script>

<style scoped>

.input-with-icon {
  position: relative;
}

.fas.fa-chevron-down {
  position: absolute;
  right: 10px;
  top: 50%;
  transform: translateY(-50%);
  color: #BEBEBE;
}

#flexSendLaterContainer {
  display: flex;
  flex-direction: row;
}

/* Container */
#mailFormContainer {
  width: 86.5%;
  margin-top: 2%;
  margin-left: 1.5%;
  display: flex;
  flex-direction: column;
}

#mailFlexBox {
  display: flex;
  flex-direction: row;
  width: 100%;
}

#mailTemplateInput {
  width: 32vw;
}

#personalizedBox {
  padding-left: 1%;
  width: 100%;
}

/* Header */
#formContent {
  height: 75vh;
  overflow-y: scroll;
  margin-top: 1%;
  padding-bottom: 3%;
  background-color: white;
  box-shadow: 5px 5px 10px lightgray;
}

#mailForm {
  padding: 0 3%;
}

#formHeader {
  display: flex;
  flex-direction: row;
  background-color: white;
  box-shadow: 0 4px 4px 0 rgba(0, 0, 0, 0.2), 0 6px 20px 0 rgba(0, 0, 0, 0.12);
}

#formHeader h1 {
  margin-left: 2%;
  margin-top: 1%;
  margin-bottom: 1%;
  font-size: 1.1em;
}

.form-group {
  margin-bottom: 1%;
  margin-top: 2%;
}

.form-label {
  color: #555;
  display: block;
}

.input-container {
  position: relative;
  width: 15%;
}

.form-input {
  all: unset;
  border: solid 1px #BEBEBE;
  border-radius: 5px;
  padding: 0.6vw;
  width: 100%;
  font-size: 0.8em;
}

.form-input:focus {
  border-color: #007bff;
}

/* Tags für ausgewählte Benutzer/Gruppen */
#selectedUsersList {
  width: 100%;
  height: 15vh;
  border: 1px solid #ccc;
  padding: 10px;
  border-radius: 10px;
  overflow-y: scroll;
}

#selectedRecipients {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5%;
  margin-bottom: 20px;
}

.tag {
  background-color: rgba(0, 123, 255, 0.45);
  color: white;
  border-radius: 10px;
  padding: 6px 12px;
  font-size: 0.8rem;
  display: inline-flex;
  align-items: center;
}

.tag-remove {
  margin-left: 8px;
  cursor: pointer;
}

/* Checkbox */
.flex {
  display: flex;
  align-items: center;
  gap: 10px;
}

.form-checkbox {
  transform: scale(1.3);
}

.form-checkbox-label {
  font-size: 1rem;
  color: #333;
}

.datetime-picker {
  display: flex;
  gap: 20px;
  margin-top: 10px;
}

.datepicker,
.timepicker {
  width: 38%;
}

/* Anpassung des Autocomplete-Containers */
.input-container-recipients {
  width: 30vw; /* Hier definierst du die Breite des Input-Felds */
  position: relative; /* Um sicherzustellen, dass das Autocomplete innerhalb des Containers positioniert wird */
}

.autocomplete {
  list-style-type: none;
  padding: 0;
  margin: 0;
  background-color: white;
  border: solid 1px #D1D1D1;
  border-top: none;
  position: absolute;
  top: 100%; /* Platzierung direkt unter dem Input-Feld */
  left: 0;
  width: 100%; /* Die Breite der Autocomplete-Liste an das Input-Feld anpassen */
  z-index: 1000;
  max-height: 200px;
  overflow-y: auto; /* Scrollbar bei zu vielen Ergebnissen */
  border-radius: 0 0 8px 8px;
}

.autocomplete li {
  padding: 12px;
  cursor: pointer;
  transition: background-color 0.2s ease-in-out;
}

.autocomplete li:hover {
  background-color: rgba(0, 123, 255, 0.36);
  color: white;
}

/* Form Actions */
.form-actions {
  display: flex;
  margin-top: 2%;
  justify-content: flex-end;
}

.btn {
  background-color: #78A6FF;
  color: white;
  padding: 1% 4%;
  border: none;
  border-radius: 5px;
  font-size: 1em;
  cursor: pointer;
  align-self: center;
}

.btn:hover {
  background-color: rgba(75, 129, 253, 0.86);
  box-shadow: 0 2px 2px 0 rgba(0, 0, 0, 0.2);
}

.btn:disabled {
  background-color: #cccccc;
  cursor: not-allowed;
}

/* Tags für ausgewählte Dateien */
.form-group ul {
  list-style-type: none;
  padding: 0;
}

.form-group li {
  display: flex;
  align-items: center;
  margin-top: 0.5em;
}

.form-group li button {
  margin-left: 1em;
  background: none;
  border: none;
  color: red;
  cursor: pointer;
}


/* Weitere bestehende Stile */
.editor-wrapper {
  max-width: 80vw;
  max-height: 35vh;
  overflow-y: auto;
}

#editor {
  width: auto;
  height: 25vh;
}
</style>
