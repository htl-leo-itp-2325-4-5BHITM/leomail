<script setup lang="ts">
import { ref, onMounted, watch, computed } from 'vue';
import { Service } from "@/services/service";
import { useAppStore } from "@/stores/app.store";

interface NaturalUser {
  type: 'natural';
  id: string;
  firstName: string;
  lastName: string;
  mailAddress: string;
}

interface CompanyUser {
  type: 'company';
  id: string;
  companyName: string;
  mailAddress: string;
}

type ContactMember = NaturalUser | CompanyUser;

interface Group {
  id: string;
  name: string;
  description: string;
  members: ContactMember[];
}

const appStore = useAppStore();
const emitEvents = defineEmits(['group-added', 'group-removed', 'group-saved']);
const props = defineProps<{ selectedTemplate: Group | null }>();

const groupName = ref('');
const groupDescription = ref('');
const selectedMembers = ref<ContactMember[]>([]);
const users = ref<ContactMember[]>([]);
const searchTerm = ref('');
const loading = ref(false);

const clearForm = () => {
  groupName.value = '';
  groupDescription.value = '';
  selectedMembers.value = [];
};

defineExpose({ clearForm });

const fetchUsers = async (query: string) => {
  loading.value = true;
  try {
    const response = await Service.getInstance().searchContacts(query);
    // Process the response to include 'type' based on presence of 'companyName' or 'firstName'
    const processedUsers: ContactMember[] = response.data.map((user: any) => {
      if (user.companyName) {
        return {
          type: 'company',
          id: user.id,
          companyName: user.companyName,
          mailAddress: user.mailAddress
        };
      } else if (user.firstName && user.lastName) {
        return {
          type: 'natural',
          id: user.id,
          firstName: user.firstName,
          lastName: user.lastName,
          mailAddress: user.mailAddress
        };
      } else {
        console.warn('Unknown user type for user:', user);
        return {
          type: 'natural', // Defaulting to 'natural', adjust as needed
          id: user.id,
          firstName: user.firstName || '',
          lastName: user.lastName || '',
          mailAddress: user.mailAddress || ''
        };
      }
    });
    users.value = processedUsers;
  } catch (error) {
    console.error('Error fetching users:', error);
    users.value = [];
  } finally {
    loading.value = false;
  }
};

watch(searchTerm, (newTerm) => {
  if (newTerm.length > 2) {
    fetchUsers(newTerm);
  } else {
    users.value = [];
  }
});

const filteredUsers = computed(() => users.value);

const selectUser = (user: ContactMember) => {
  if (!selectedMembers.value.find(u => u.id === user.id)) {
    selectedMembers.value.push(user);
  }
  searchTerm.value = '';
  users.value = [];
};


const removeUser = (user: ContactMember) => {
  selectedMembers.value = selectedMembers.value.filter(u => u.id !== user.id);
};

const addGroup = async () => {
  try {
    // Validate that all members have the 'type' property
    for (const member of selectedMembers.value) {
      if (!member.type) {
        throw new Error(`Member with ID ${member.id} is missing the 'type' property.`);
      }
    }

    const formData = {
      name: groupName.value,
      description: groupDescription.value,
      members: selectedMembers.value
    };
    const response = await Service.getInstance().addGroup(appStore.$state.project, formData);
    emitEvents('group-added', response.data);
    clearForm();
  } catch (error: any) {
    alert(`Error adding group: ${error.response?.data?.error || error.message}`);
  }
};

const updateGroup = async () => {
  try {
    if (!props.selectedTemplate) return;
    // Validate that all members have the 'type' property
    for (const member of selectedMembers.value) {
      if (!member.type) {
        throw new Error(`Member with ID ${member.id} is missing the 'type' property.`);
      }
    }

    const formData = {
      id: props.selectedTemplate.id,
      name: groupName.value,
      description: groupDescription.value,
      members: selectedMembers.value
    };
    const response = await Service.getInstance().updateGroup(appStore.$state.project, formData);
    emitEvents('group-saved', response.data);
    clearForm();
    alert('Group successfully updated!');
  } catch (error: any) {
    alert(`Error updating group: ${error.response?.data?.error || error.message}`);
  }
};

const removeGroup = async () => {
  try {
    if (!props.selectedTemplate) return;
    await Service.getInstance().deleteGroup(appStore.$state.project, props.selectedTemplate.id);
    emitEvents('group-removed', props.selectedTemplate);
    clearForm();
    alert('Group successfully removed!');
  } catch (error: any) {
    console.error('Error removing group:', error);
    alert(`Error removing group: ${error.response?.data?.error || error.message}`);
  }
};

watch(() => props.selectedTemplate, (newTemplate) => {
  if (newTemplate) {
    groupName.value = newTemplate.name;
    groupDescription.value = newTemplate.description;

    const uniqueMembers = newTemplate.members.filter(newMember =>
        !selectedMembers.value.find(existingMember => existingMember.id === newMember.id)
    );
    selectedMembers.value.push(...uniqueMembers);
  } else {
    clearForm();
  }
});

onMounted(() => {
  if (props.selectedTemplate) {
    groupName.value = props.selectedTemplate.name;
    groupDescription.value = props.selectedTemplate.description;
    selectedMembers.value = [...props.selectedTemplate.members];
  }
});


const errorMessage = ref<string | null>(null);

const validateForm = () => {
  if (!groupName.value || !groupDescription.value || selectedMembers.value.length === 0) {
    errorMessage.value = 'Alle Felder müssen ausgefüllt sein (Gruppenname, Beschreibung, Mitglieder)';
    return false;
  }
  errorMessage.value = null;
  return true;
};

</script>

<template>
  <div id="bigBox">
    <form @submit.prevent="props.selectedTemplate ? updateGroup() : addGroup()">
      <div class="dataBox">
        <div class="boxLabel">
          <label for="groupName" class="group-label">Gruppenname</label><br>
        </div>
        <input
            type="text"
            id="groupName"
            class="formGroup"
            placeholder="Geben Sie einen Gruppennamen ein..."
            v-model="groupName"
            required
        >
      </div>

      <div class="dataBox">
        <div class="boxLabel">
          <label for="groupDescription" class="group-label">Beschreibung</label><br>
        </div>
        <textarea
            id="groupDescription"
            class="formGroup"
            placeholder="Geben Sie eine kurze Beschreibung zur Gruppe ein..."
            v-model="groupDescription"
            required
        ></textarea>
      </div>

      <div class="dataBox">
        <div class="boxLabel">
          <label for="members" class="group-label">Mitglieder hinzufügen</label><br>
        </div>
          <input
              type="text"
              v-model="searchTerm"
              class="formGroup"
              placeholder="Benutzer suchen"
          />
          <ul v-if="searchTerm.length > 0 && filteredUsers.length">
            <li
                v-for="user in filteredUsers"
                :key="user.id"
                @click="selectUser(user)"
            >
              <div class="user-info">
                <span v-if="user.type === 'natural'">{{ user.firstName }} {{ user.lastName }}</span>
                <span v-else-if="user.type === 'company'">{{ user.companyName }}</span>
                <small>{{ user.mailAddress }}</small>
              </div>
            </li>
            <li v-if="loading">Laden...</li>
          </ul>
        </div>

      <div id="selectedUsersList">
        <div id="selectedRecipients">
          <div class="tag" v-for="user in selectedMembers" :key="user.id">
            {{ user.firstName }} {{ user.lastName }} <span class="remove" @click="removeUser(user)">✕</span>
          </div>
        </div>
      </div>

      <!-- Fehleranzeige -->
      <div v-if="errorMessage" class="error">{{ errorMessage }}</div>

      <div id="buttonBox">
        <button type="button" @click="removeGroup" v-if="props.selectedTemplate" id="deleteButton">Löschen</button>
        <button type="submit" id="submitButton" :disabled="!groupName || !groupDescription || selectedMembers.length === 0">
          {{ props.selectedTemplate ? 'Speichern' : 'Erstellen' }}
        </button>
      </div>
    </form>
  </div>
</template>

<style scoped>
/* Fehleranzeige */
.error {
  color: red;
  font-size: 0.7em; /* Kleinere Schriftgröße für Fehlernachricht */
  margin-top: 10px;
  padding: 0.1em;
}

/* Tags für ausgewählte Benutzer/Gruppen */
#selectedUsersList {
  width: 100%;
  height: 15vh;
  border-radius: 10px;
  border: 1px solid #ccc;
  padding: 0.2rem;
}

#selectedRecipients {
  display: flex;
  flex-wrap: wrap;
  gap: 0.2%;
  margin-bottom: 20px;
}

.tag {
  background-color: lightblue;
  color: white;
  border-radius: 10px;
  padding: 6px 12px;
  font-size: 0.7rem;
  display: inline-flex;
  align-items: center;
}

.remove {
  margin-left: 8px;
  cursor: pointer;
}

.dataBox {
  margin-bottom: 1.5rem; /* Abstand zwischen den Eingabefeldern */
}

form {
  padding: 2% 3%; /* Innenabstand des Formulars */
}

#bigBox {
  width: 100%; /* Vollständige Breite des Formulars */
}

/* Label für Gruppenname, Beschreibung etc. */
.group-label {
  color: #5A5A5A;
  font-size: 0.8em; /* Beibehaltung der originalen Schriftgröße */
}

/* Platzhalter-Text in den Eingabefeldern */
.formGroup::placeholder {
  color: #B3B3B3;
}

/* Stil der Eingabefelder */
.formGroup {
  display: block;
  border: solid 1px #D1D1D1;
  border-radius: 8px;
  padding: 0.8rem;
  width: 100%;
  font-size: 0.8em; /* Beibehaltung der originalen Schriftgröße */
  margin-top: 0.1rem;
  transition: border-color 0.2s ease;
}

.formGroup:focus {
  border-color: #4B81FD; /* Blaues Highlight beim Fokus */
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1); /* Leichter Schatten bei Fokus */
}

/* Multiselect-Bereich */
.multiselect {
  display: block;
  border: solid 1px #D1D1D1;
  border-radius: 8px;
  padding: 0.1rem 0;
  width: 50%;
  font-size: 0.8em; /* Originale Schriftgröße */
  margin-bottom: 1.5rem;
  position: relative;
  margin-top: 0.5rem;
  transition: border-color 0.2s ease;
}

/* Ausgewählte Mitglieder-Tag */
.selected {
  display: inline-flex;
  align-items: center;
  background-color: #4B81FD; /* Blaue Hintergrundfarbe */
  color: white;
  border-radius: 15px; /* Abgerundete Ecken */
  padding: 0.4rem 0.8rem;
  margin-right: 0.5rem;
  margin-bottom: 0.5rem;
  font-size: 0.5em; /* Originale Schriftgröße */
  transition: background-color 0.2s ease;
}

.selected:hover {
  background-color: #3a6ad4; /* Etwas dunkleres Blau beim Hover */
}

.remove {
  cursor: pointer;
  margin-left: 8px;
  font-size: 0.8rem; /* Größe des "×"-Symbols */
}

/* Textfeld für Benutzersuche im Multiselect */
.multiselect input[type="text"] {
  all: unset;
  width: 100%;
  box-sizing: border-box;
  border-radius: 8px;
  margin: 0.8rem 0 0.8em 1em;
}

/* Dropdown-Liste der Suchergebnisse */
ul {
  list-style-type: none;
  padding: 0;
  margin: 0;
  background-color: white;
  border: solid 1px #D1D1D1;
  border-top: none;
  position: absolute;
  width: 70%;
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
  background-color: #4B81FD; /* Hintergrund beim Hover */
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

/* Fehleranzeige */
.error {
  color: red;
  font-size: 0.7em; /* Kleine Schriftgröße für Fehlernachricht */
}

/* Button-Container */
#buttonBox {
  display: flex;
  flex-wrap: wrap;
  margin-top: 2%;
  justify-content: flex-end; /* Align items to the right */
}

/* Erstellen/Speichern Button */
#submitButton {
  all: unset;
  padding: 1vh 3vh;
  border-radius: 12px;
  background-color: #78A6FF;
  color: white;
  border: #78A6FF solid 1px;
  font-size: 0.8rem;
  transition: background-color 0.2s ease, box-shadow 0.2s ease;
}

/* Löschen Button */
#deleteButton {
  all: unset;
  border-radius: 12px;
  margin-right: 0.5rem;
  padding: 1vh 3vh;
  color: red;
  font-size: 0.8rem; /* Originale Schriftgröße */
  transition: background-color 0.2s ease, box-shadow 0.2s ease;
}

#submitButton:hover {
  background-color: #3a6ad4; /* Dunkleres Blau beim Hover */
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.15); /* Leichter Schatten beim Hover */
}

#deleteButton:hover {
  font-weight: bold;
}

#submitButton:disabled {
  background-color: lightgray; /* Deaktivierter Zustand */
  border-color: lightgray;
}

#submitButton:disabled:hover {
  box-shadow: none;
}
</style>