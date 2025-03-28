<script setup lang="ts">
import {ref, onMounted, watch, computed} from 'vue';
import {useAppStore} from "@/stores/app.store";
import {Service} from "@/services/service";
import {useRouter} from 'vue-router';
import axios from 'axios';

const router = useRouter();
const appStore = useAppStore();

// Refs für Formularfelder
const projectName = ref('');
const displayName = ref('');
const description = ref('');
const mail = ref('');
const password = ref('');
const recipients = ref([]);  // Hier speichern wir die Benutzer (recipients)

const searchTerm = ref('');  // Holds the current search query
const users = ref<User[]>([]);  // Holds the list of users fetched
const loading = ref(false);  // Loading state for the search request
const selectedUsers = ref<User[]>([]);  // Declare selectedUsers as a ref

const isChecking = ref(false);  // Überprüfungsstatus
const isEmailVerified = ref(false);  // Überprüft, ob die E-Mail validiert wurde
const isEmailChanged = ref(false);  // Überwacht, ob die E-Mail oder das Passwort geändert wurde

// Fehlernachrichten
const errorMessages = ref({
  projectName: '',
  description: '',
  mail: '',
  password: '',
  recipients: '',
  emailVerification: ''
});

export interface User {
  id: number;
  firstName: string;
  lastName: string;
  mailAddress: string;
  type: string;
}

// Funktion, um Benutzer basierend auf der Suchanfrage abzurufen
const fetchUsers = async (query: string) => {
  loading.value = true;
  try {
    const usersResponse = await Service.getInstance().searchContacts(query);
    users.value = usersResponse.data;
  } catch (error) {
    console.error('Error fetching users:', error);
    users.value = [];
  } finally {
    loading.value = false;
  }
};

// Watcher für die searchTerm, um nur bei mindestens 3 Zeichen die Suche auszuführen
watch(searchTerm, (newTerm) => {
  if (newTerm.length >= 3) {
    fetchUsers(newTerm);
  } else {
    users.value = [];
  }
});

const selectUser = (user: User) => {
  if (!selectedUsers.value.find(u => u.id === user.id)) {
    selectedUsers.value.push(user);
    recipients.value.push(user);
  }
  searchTerm.value = '';
  users.value = [];
};

const removeUser = (user: User) => {
  selectedUsers.value = selectedUsers.value.filter(u => u.id !== user.id);
  recipients.value = recipients.value.filter(u => u.id !== user.id);
};

// Funktion, um Projektdaten zu laden
const loadProjectData = async () => {
  try {
    const response = await Service.getInstance().getProject(appStore.$state.project);
    const projectData = response.data;

    projectName.value = projectData.name;
    displayName.value = projectData.displayName;
    description.value = projectData.description;
    mail.value = projectData.mailInformation.mailAddress;
    password.value = projectData.mailInformation.password;
    recipients.value = projectData.members || [];
    selectedUsers.value = [...recipients.value];
  } catch (error) {
    console.error('Fehler beim Laden der Projektdaten:', error);
  }
};

onMounted(() => {
  loadProjectData();
});

// E-Mail und Passwort prüfen
const checkAccount = async () => {
  if (!mail.value || !password.value) {
    errorMessages.value.mail = 'E-Mail und Passwort dürfen nicht leer sein.';
    return;
  }

  isChecking.value = true;
  errorMessages.value.mail = '';  // Zurücksetzen der Fehlermeldung, falls vorhanden

  try {
    const isOutlookValid = await Service.getInstance().checkOutlookPassword(mail.value, password.value);

    if (isOutlookValid) {
      isEmailVerified.value = true;
      isEmailChanged.value = false;  // Setze den Status zurück, weil die E-Mail und das Passwort nun bestätigt wurden
      alert("Account und Outlook-Passwort sind gültig!");  // Alert bei erfolgreicher Überprüfung
    } else {
      isEmailVerified.value = false;
      isEmailChanged.value = true;  // Setze den Status auf "E-Mail geändert"
      errorMessages.value.emailVerification = 'E-Mail oder Passwort sind ungültig.';
    }
  } catch (error) {
    console.error("Fehler bei der Überprüfung:", error);
    errorMessages.value.emailVerification = 'Es gab ein Problem bei der Überprüfung des Accounts.';
  } finally {
    isChecking.value = false;
  }
};

// Überwache E-Mail und Passwortänderungen, um das Häkchen zurückzusetzen
watch([mail, password], () => {
  if (isEmailVerified.value) {
    isEmailVerified.value = false;  // E-Mail muss wieder validiert werden
    isEmailChanged.value = true;  // Status auf geändert setzen
  }
});

// Formularvalidierung
const validateForm = () => {
  let valid = true;

  if (!projectName.value) {
    errorMessages.value.projectName = 'Projektname ist erforderlich';
    valid = false;
  } else {
    errorMessages.value.projectName = '';
  }

  if (!description.value) {
    errorMessages.value.description = 'Kurzbeschreibung ist erforderlich';
    valid = false;
  } else {
    errorMessages.value.description = '';
  }

  const emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  if (!mail.value || !emailPattern.test(mail.value)) {
    errorMessages.value.mail = 'Gültige E-Mail-Adresse ist erforderlich';
    valid = false;
  } else {
    errorMessages.value.mail = '';
  }

  if (!isEmailVerified.value && isEmailChanged.value) {
    errorMessages.value.emailVerification = 'E-Mail muss verifiziert sein, bevor das Projekt erstellt werden kann.';
    valid = false;
  } else {
    errorMessages.value.emailVerification = '';
  }

  if (selectedUsers.value.length === 0) {
    errorMessages.value.recipients = 'Mindestens ein Benutzer muss hinzugefügt werden.';
    valid = false;
  } else {
    errorMessages.value.recipients = '';
  }

  return valid;
};

// Submit-Handler für das Formular
const handleSubmit = async () => {
  if (validateForm()) {
    try {
      // Hier rufen wir die Methode aus der Service-Klasse auf, um das Projekt zu aktualisieren
      const projectData = {
        id: appStore.$state.project,
        name: projectName.value,
        description: description.value,
        mailInformation: {
          mailAddress: isEmailChanged.value ? mail.value : undefined,
          password: isEmailChanged.value ? password.value : undefined,
        },
        members: selectedUsers.value,
      };

      await Service.getInstance().updateProject(projectData);

      alert('Projekt erfolgreich aktualisiert');
      router.push({name: 'projects'}); // Zurück zur Projektliste
    } catch (error) {
      console.error('Fehler bei der Projekterstellung:', error);
    }
  }
};


// Computed Property, um den Status des "Überprüfen"-Buttons zu steuern
const isButtonDisabled = computed(() => {
  return !mail.value || !password.value || isChecking.value;
});

</script>

<template>
  <div id="bigContentBox">
    <form @submit.prevent="handleSubmit">
      <div class="inputBox">
        <label for="projectname">Projektname</label>
        <input
            type="text"
            id="projectname"
            class="project-form"
            v-model="projectName"
            placeholder="Projektname eingeben"
        />
      </div>


      <!-- Kurzbeschreibung -->
      <div class="inputBox">
        <label for="description">Kurzbeschreibung</label>
        <textarea
            id="description"
            class="project-form"
            v-model="description"
            placeholder="Kurzbeschreibung eingeben"
        ></textarea>
      </div>

      <!-- Mail-Adresse und Passwort -->
      <div class="inputBox">
        <label for="mail">Mail-Adresse</label>
        <div class="inputRow">
          <input
              type="email"
              id="mail"
              class="project-form"
              v-model="mail"
              placeholder="Mail-Adresse eingeben"
          />

          <input
              type="password"
              id="password"
              class="project-form"
              v-model="password"
              placeholder="Passwort eingeben"
          />
        </div>

        <!-- Button zur Account-Überprüfung -->
        <div class="form-flex-item">
          <button
              type="button"
              id="checkAccountButton"
              @click="checkAccount"
              :disabled="isButtonDisabled"
          >
            {{ isChecking ? 'Überprüfe...' : 'Überprüfen' }}
            <span v-if="isButtonDisabled" class="loader"></span> <!-- Ladezeichen -->
            <span v-if="isEmailVerified" class="checkmark">&#10003;</span>
          </button>
          <p v-if="errorMessages.emailVerification" class="error">{{ errorMessages.emailVerification }}</p>
        </div>
      </div>

      <!-- Benutzer (recipients) -->
      <div class="inputBox">
        <label for="recipients">Benutzer</label>

        <div class="multiselect">
          <input
              type="text"
              v-model="searchTerm"
              class="projectForm"
              placeholder="Search for a user..."
          />
          <ul v-if="searchTerm.length >= 3 && users.length">
            <li v-for="user in users" :key="user.id" @click="selectUser(user)">
              <div class="user-info">
                <span>{{ user.firstName }} {{ user.lastName }}</span>
                <span>{{ user.mailAddress }}</span>
              </div>
            </li>
          </ul>
        </div>


        <div class="selectedUsersList">
          <div class="selectedRecipients">
            <div v-for="user in selectedUsers" :key="user.id" class="tag">
              <span>{{ user.firstName }} {{ user.lastName }}</span>
              <span @click="removeUser(user)" class="tag-remove">X</span>
            </div>
          </div>
        </div>
      </div>

      <!-- Submit Button -->
      <div class="buttonBox">
        <button
            type="submit"
            class="save"
            :disabled="isChecking"
        >
          Speichern
        </button>
      </div>
    </form>
  </div>
</template>


<style scoped>
.checkmark {
  color: green;
  font-size: 1.5em;
  display: inline-block;
  margin-left: 5px;
  vertical-align: middle;
}

#checkAccountButton {
  all: unset;
  font-size: 0.6em;
  color: #78A6FF;
  border-bottom: solid 1px #78A6FF;
}

#checkAccountButton:hover {
  font-weight: bold;
}

#checkAccountButton:disabled {
  color: #ccc; /* Graue Farbe für den Button */
  border-bottom: solid 1px #ccc; /* Graue Unterstreichung */
  cursor: not-allowed; /* Ändert den Cursor zu 'not-allowed' */
}

.loader {
  display: inline-block;
  width: 10px;
  height: 10px;
  border-radius: 50%;
  border: 2px solid #f3f3f3;
  border-top: 2px solid #3498db;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  0% {
    transform: rotate(0deg);
  }
  100% {
    transform: rotate(360deg);
  }
}

/* Allgemeine Container */
#bigContentBox {
  width: 80vw;
  margin: 2% auto;
  padding: 3vh 4vw;
  background-color: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  display: flex;
  flex-direction: column;
}

/* Flexbox für Eingabepaare */
.inputRow {
  display: flex;
  justify-content: space-between;
  gap: 2rem;
}

/* Flex-Container für Eingabeboxen */
.inputBox {
  flex: 1;
  display: flex;
  flex-direction: column;
  margin-bottom: 1.5em;
}

/* Labels */
.inputBox label {
  margin-bottom: 0.5rem;
  font-size: 1rem;
  color: #333;
}

/* Eingabefelder und Textarea */
.project-form {
  display: block;
  width: 100%;
  padding: 0.75rem;
  border: 1px solid #ccc;
  border-radius: 6px;
  font-size: 1rem;
  background-color: #fff;
  color: #333;
  transition: border-color 0.3s ease;
}

/* Platzhalter */
.project-form::placeholder {
  color: #999;
}

/* Fokuszustand für die Eingabefelder */
.project-form:focus {
  border-color: #78a6ff;
  outline: none;
  background-color: #fff;
}

/* Textarea-spezifisch */
textarea.project-form {
  min-height: 100px;
  resize: vertical;
}

/* Multiselect für die Benutzerauswahl */
.multiselect {
  display: block;
  border: solid 1px #D1D1D1;
  border-radius: 8px;
  padding: 0.1rem 0;
  width: 50%;
  font-size: 0.8em;
  position: relative;
  margin-top: 0.5rem;
  transition: border-color 0.2s ease;
}

.multiselect input[type="text"] {
  all: unset;
  background-color: #F6F6F6;
  width: 100%;
  padding: 0.6vw;
  box-sizing: border-box;
  border-radius: 5px;
}

/* Dropdown-Liste für die Autocomplete */
ul {
  list-style-type: none;
  padding: 0;
  margin: 0;
  background-color: white;
  border: solid 1px #D1D1D1;
  border-top: none;
  position: absolute;
  width: 100%;
  z-index: 1000;
  max-height: 200px;
  overflow-y: auto;
  border-radius: 0 0 8px 8px;
}

li {
  padding: 12px;
  cursor: pointer;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

li:hover {
  background-color: #78A6FF;
  color: white;
}

li:hover .user-info small {
  color: white;
}

/* Benutzer-Info in der Liste */
.user-info {
  display: flex;
  flex-direction: column;
}

.user-info small {
  color: #B3B3B3;
}

/* Container für die ausgewählten Benutzer */
.selectedUsersList {
  width: 80%;
  height: 15vh;
  border: 1px solid #ccc;
  margin-top: 1vh;
  border-radius: 5px;
  padding: 0.3%;
}

.selectedRecipients {
  display: flex;
  flex-wrap: wrap;
  gap: 0.2%;
}

.tag {
  background-color: rgba(0, 123, 255, 0.45);
  color: white;
  border-radius: 10px;
  padding: 6px 12px;
  font-size: 0.6em;
  display: inline-flex;
  align-items: center;
  margin-top: 0.5%;
}

.tag-remove {
  margin-left: 8px;
  cursor: pointer;
}

/* Button-Container */
.buttonBox {
  display: flex;
  justify-content: flex-end;
  margin-top: 2rem;
}

.save {
  padding: 0.75rem 2rem;
  background-color: #78A6FF;
  color: white;
  font-size: 1rem;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  transition: background-color 0.3s ease, box-shadow 0.3s ease;
}

.save:hover {
  background-color: #4b81fd;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
}

.save:disabled {
  background-color: #cccccc;
  cursor: not-allowed;
}

/*spinner*/
.loader {
  border: 2px solid #f3f3f3; /* Hintergrund des Kreises */
  border-top: 2px solid #78A6FF; /* Farbe des sich drehenden Teils */
  border-radius: 50%;
  width: 18px;  /* kleinere Größe */
  height: 18px; /* kleinere Größe */
  animation: spin 1s linear infinite;
  display: inline-block;
  vertical-align: middle; /* Vertikal ausrichten */
  margin-left: 10px; /* Abstand zum Text im Button */
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}


/* Fehleranzeige */
.error {
  color: red;
  font-size: 0.7em;
}

/* Neue Fehlernachricht-Stile */
.error-message {
  color: red;
  font-size: 0.9rem;
  margin-top: 5px;
}

@media (max-width: 600px) {
  .inputRow {
    flex-direction: column;
  }

  .project-form {
    font-size: 0.9rem;
    padding: 0.65rem;
  }

  .save {
    font-size: 0.9rem;
    padding: 0.65rem 1.5rem;
  }
}
</style>
