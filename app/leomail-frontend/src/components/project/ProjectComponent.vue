<script setup lang="ts">
import { computed, ref, watch } from 'vue';
import { useRouter } from 'vue-router';
import axios from 'axios';
import { Service } from "@/services/service";
import axiosInstance from "@/axiosInstance";

const router = useRouter();

export interface User {
  id: number;
  firstName: string;
  lastName: string;
  mailAddress: string;
  type: string
}

const selectedUsers = ref<User[]>([]);
const searchTerm = ref('');
const users = ref<User[]>([]);
const loading = ref(false);
const isChecking = ref(false);  // Neue Variable zum Verfolgen des Ladezustands
const isEmailVerified = ref(false);  // Neue Variable für Verifizierungsstatus

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

const filteredUsers = computed(() => users.value);

// Benutzer auswählen
const selectUser = (user: User) => {
  if (!selectedUsers.value.find(u => u.id === user.id)) {
    user = {
      id: user.id,
      firstName: user.firstName,
      lastName: user.lastName,
      mailAddress: user.mailAddress,
      type: 'natural'
    }
    selectedUsers.value.push(user);
  }
  searchTerm.value = '';
  users.value = [];
};

// Benutzer entfernen
const removeUser = (user: User) => {
  selectedUsers.value = selectedUsers.value.filter(u => u.id !== user.id);
};

// Formulardaten
const formState = ref({
  name: '',
  description: '',
  mailAddress: '',
  password: '',
  members: computed(() => selectedUsers.value)
});

// Fehlerobjekt zur Validierung
const errors = ref({
  name: '',
  description: '',
  email: '',
  password: '',
  selectedUsers: ''
});

// Formvalidierung
const validateForm = () => {
  let valid = true;

  if (!formState.value.name) {
    errors.value.name = 'Name ist erforderlich';
    valid = false;
  } else if (formState.value.name.length > 40) {
    errors.value.name = 'Name darf maximal 40 Zeichen lang sein';
    valid = false;
  } else {
    errors.value.name = '';
  }

  if (!formState.value.description) {
    errors.value.description = 'Kurzbeschreibung ist erforderlich';
    valid = false;
  } else if (formState.value.description.length > 240) {
    errors.value.description = 'Kurzbeschreibung darf maximal 240 Zeichen lang sein';
    valid = false;
  } else {
    errors.value.description = '';
  }

  const emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  if (!formState.value.mailAddress) {
    errors.value.email = 'E-Mail-Adresse ist erforderlich';
    valid = false;
  } else if (!emailPattern.test(formState.value.mailAddress)) {
    errors.value.email = 'Ungültige E-Mail-Adresse';
    valid = false;
  } else {
    errors.value.email = '';
  }

  if (!formState.value.password) {
    errors.value.password = 'Passwort ist erforderlich';
    valid = false;
  } else {
    errors.value.password = '';
  }

  errors.value.selectedUsers = ''; // Fehlertext zurücksetzen, falls vorhanden

  if (!isEmailVerified.value) {
    errors.value.email = 'E-Mail muss verifiziert sein, um fortzufahren';
    valid = false;
  }

  return valid;
};

// Zurück navigieren
const handleBack = () => {
  router.push({ name: 'projects' });
};

const checkAccount = async () => {
  if (!formState.value.mailAddress || !formState.value.password) {
    alert("Bitte füllen Sie alle Felder aus.");
    return;
  }

  isChecking.value = true;

  try {
    const isOutlookValid = await Service.getInstance().checkOutlookPassword(formState.value.mailAddress, formState.value.password);

    if (isOutlookValid) {
      alert("Account und Outlook-Passwort sind gültig!");
      isEmailVerified.value = true;  // Setzt die Verifizierung auf true
    } else {
      alert("E-Mail oder Passwort sind ungültig.");
      isEmailVerified.value = false; // Setzt die Verifizierung auf false
    }
  } catch (error) {
    console.error("Fehler bei der Überprüfung:", error);
    alert("Es gab ein Problem bei der Überprüfung des Accounts.");
    isEmailVerified.value = false;  // Setzt die Verifizierung auf false bei Fehler
  } finally {
    isChecking.value = false;  // Laden beendet
  }
};

const handleSubmit = () => {
  if (validateForm()) {
    if (!isEmailVerified.value) {
      alert("Die E-Mail muss verifiziert werden, bevor das Projekt erstellt werden kann.");
      return;
    }

    Service.getInstance().addProject(formState.value, selectedUsers.value).then(() => {
      router.push({ name: 'projects' });
      formState.value.name = '';
      formState.value.description = '';
      formState.value.mailAddress = '';
      formState.value.password = '';
      selectedUsers.value = [];
    }).catch((error) => {
      console.error('Error adding project:', error);
      alert('Ein Fehler ist aufgetreten. Bitte versuchen Sie es erneut.');
    });
  }
};

</script>

<template>
  <div id="bigBox">
    <div id="formHeaderContainer">
      <img id="arrow" src="../../assets/icons/pfeil-links-big.png" @click="handleBack">
      <h3 id="headline">Neues Projekt</h3>
    </div>
    <div id="contentBox">
      <form @submit.prevent="handleSubmit">

        <!-- Projektname -->
        <div class="form-flex-group">
          <div class="form-flex-item">
            <div class="boxLabel">
              <label for="name" class="project-label">Name des Projekts (max. 40 Zeichen)</label><br>
            </div>
            <input
                type="text"
                id="name"
                class="projectForm"
                placeholder="z.Bsp. Maturaball-2025"
                v-model="formState.name"
            >
            <span class="error">{{ errors.name }}</span>
          </div>

        </div>

        <!-- Kurzbeschreibung -->
        <div class="boxLabel">
          <label for="description" class="project-label">Kurzbeschreibung (max. 240 Zeichen)</label><br>
        </div>
        <textarea
            id="description"
            class="projectForm"
            v-model="formState.description"
            maxlength="240"
        ></textarea>
        <span class="error">{{ errors.description }}</span>

        <!-- Mail & Passwort -->
        <div class="form-flex-group">
          <div class="form-flex-item">
            <div class="boxLabel">
              <label for="mail" class="project-label">Mail-Adresse</label><br>
            </div>
            <input
                type="email"
                id="mail"
                class="projectForm"
                placeholder="z.Bsp. maturaball-2025@gmail.com"
                v-model="formState.mailAddress"
            >
            <span class="error">{{ errors.email }}</span>
            <span v-if="!isEmailVerified && formState.mailAddress" class="error">E-Mail muss verifiziert sein, um fortzufahren.</span>
          </div>

          <div class="form-flex-item" style="margin-left: 7%">
            <div class="boxLabel">
              <label for="password" class="project-label">Passwort</label><br>
            </div>
            <input
                type="password"
                id="password"
                class="projectForm"
                v-model="formState.password"
            >
            <span class="error">{{ errors.password }}</span>
          </div>
        </div>

        <!-- Button zur Account-Überprüfung -->
        <div class="form-flex-item">
          <button
              type="button"
              id="checkAccountButton"
              @click="checkAccount"
              :disabled="isChecking"
          >
            {{ isChecking ? 'Überprüfe...' : 'Account Überprüfung' }}
            <span v-if="isEmailVerified" class="checkmark">&#10003;</span>
          </button>
          <span v-if="isChecking" class="loader"></span> <!-- Ladezeichen -->
        </div>

        <!-- Benutzer hinzufügen -->
        <div class="boxLabel">
          <label for="recipients" class="project-label">Benutzer hinzufügen</label>
          <div class="multiselect">
            <input
                type="text"
                v-model="searchTerm"
                class="projectForm"
                placeholder="Geben Sie hier den Namen eines Benutzers ein..."
            />
            <!-- Autocomplete Dropdown -->
            <ul v-if="searchTerm.length >= 3 && filteredUsers.length" class="autocomplete">
              <li v-for="user in filteredUsers" :key="user.id" @click="selectUser(user)">
                <div class="user-info">
                  <span>{{ user.firstName }} {{ user.lastName }}</span>
                  <small>{{ user.mailAddress }}</small>
                </div>
              </li>
              <li v-if="loading" class="loading">Laden...</li>
            </ul>
          </div>
          <span class="error">{{ errors.selectedUsers }}</span>
        </div>

        <!-- Ausgewählte Benutzer anzeigen -->
        <div id="selectedUsersList">
          <div id="selectedRecipients">
            <div class="tag" v-for="user in selectedUsers" :key="user.id">
              {{ user.firstName }} {{ user.lastName }}
              <span @click="removeUser(user)" class="tag-remove">✕</span>
            </div>
          </div>
        </div>

        <!-- Button zum Speichern -->
        <div id="buttonBox">
          <button
              type="submit"
              id="submitButton"
              :disabled="!isEmailVerified || !validateForm()"
          >
            Erstellen
          </button>
        </div>
      </form>
    </div>
  </div>
</template>


<style scoped>
#checkAccountButton{
  all: unset;
  font-size:0.6em;
  color: #78A6FF;
  border-bottom: solid 1px #78A6FF;
}

#checkAccountButton:hover {
  font-weight: bold;
}
/* Spinner (Loader) */
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



/* Tags für ausgewählte Benutzer/Gruppen */
#selectedUsersList {
  width: 80%;
  height: 15vh;
  border: 1px solid #ccc;
  margin-top: 1vh;
  border-radius: 5px;
  padding: 0.3%;
}

#selectedRecipients {
  display: flex;
  flex-wrap: wrap;
  gap: 0.2%;
  margin-bottom: 20px;
}

.tag {
  background-color: rgba(0, 123, 255, 0.45);
  color: white;
  border-radius: 10px;
  padding: 6px 12px;
  font-size: 0.6em;
  display: inline-flex;
  align-items: center;
}

.tag-remove {
  margin-left: 8px;
  cursor: pointer;
}
/* header */
#formHeaderContainer {
  display: flex;
  flex-direction: row;
  align-items: center;
  margin-top: 4vh;
}

#arrow {
  width: 1.2vw;
  margin-left: 2%;
}

#headline {
  align-items: center;
  margin-left: 6%;
  font-weight: bold;
}

/* content */
#contentBox {
  margin-top: 2vh;
}

#bigBox {
  width: 100%;
}

/* form */
form {
  display: flex;
  flex-direction: column;
  margin-left: 10%;
  width: 80%;
}

.form-flex-group {
  display: flex;
  flex-direction: row;
  width: 50vw;
}

.form-flex-item{
   width: 25vw;
}

.project-label {
  color: #5A5A5A;
  font-size: 0.9em;
}

.boxLabel{
  margin-top: 1vh;
}

.projectForm::placeholder {
  color: #B3B3B3;
}

.projectForm {
  display: block;
  all: unset;
  border: solid 1px #BEBEBE;
  border-radius: 5px;
  padding: 0.6vw;
  width: 50vw;
  font-size: 0.8em;
  background-color: #F6F6F6;
}

.form-flex-item input{
  width: 100%;
}

.projectForm:focus {
  box-shadow: 0 2px 2px 0 rgba(0, 0, 0, 0.2);
}

.error {
  color: red;
  font-size: 0.7em;
}

#buttonBox {
  position: absolute;
  top: 80vh;
  right: 15vw;
  width: 12vw;
  margin-top: 0;
}

/* Multiselect-Bereich */
.multiselect {
  display: block;
  border: solid 1px #D1D1D1;
  border-radius: 8px;
  padding: 0.1rem 0;
  width: 50%;
  font-size: 0.8em; /* Originale Schriftgröße */
  position: relative;
  margin-top: 0.5rem;
  transition: border-color 0.2s ease;
}

.selected {
  display: inline-flex;
  align-items: center;
  background-color: #78A6FF;
  color: white;
  border-radius: 3px;
  padding: 2px 5px;
  margin-right: 5px;
  margin-bottom: 3px;
  font-size: 0.5em;
}

.remove {
  cursor: pointer;
  margin-left: 5px;
}

.remove:after {
  content: '×';
  color: white;
  font-weight: bold;
}

.multiselect input[type="text"] {
  all: unset;
  background-color: #F6F6F6;
  width: 100%;
  padding: 0.6vw;
  box-sizing: border-box;
  border-radius: 5px;
}

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
  overflow-y: auto; /* Scrollbar bei zu vielen Ergebnissen */
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
  background-color: #78A6FF; /* Hintergrund beim Hover */
  color: white;
  transition: background-color 0.2s ease;
}

li:hover .user-info small {
  color: white; /* Kleiner Text in weiß beim Hover */
}

/* Benutzer-Info in der Liste (Name + E-Mail) */
.user-info {
  display: flex;
  flex-direction: column;
}

.user-info small {
  color: #B3B3B3; /* Leicht graue Schrift für E-Mail */
}


.error {
  color: red;
  font-size: 0.7em;
}

/* Button-Container */
#buttonBox {
  display: flex;
  flex-wrap: wrap;
  justify-content: flex-end; /* Align items to the right */
}

/* Erstellen Button */
#submitButton {
  all: unset;
  padding: 1vh 3vh;
  border-radius: 12px;
  background-color: #78A6FF;
  color: white;
  border: #78A6FF solid 1px;
  font-size: 0.8rem;
  display: flex;
  justify-content: flex-end;
  transition: background-color 0.2s ease, box-shadow 0.2s ease;
}

#submitButton:hover {
  background-color: #3a6ad4; /* Dunkleres Blau beim Hover */
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.15); /* Leichter Schatten beim Hover */
}

#submitButton:disabled {
  background-color: lightgray; /* Deaktivierter Zustand */
  border-color: lightgray;
}

#submitButton:disabled:hover {
  box-shadow: none;
}

</style>
