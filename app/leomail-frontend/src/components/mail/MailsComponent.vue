<template>
  <div id="bigVGContainer">
    <div id="VGHeaderBox">
      <h1 id="vgHeading">Mails</h1>

      <button id="neueMail" @click="clickedEmailForm" aria-label="Neue Email">
        <p>Neue Email</p>
      </button>
    </div>

    <div id="search-container">
      <input
          type="text"
          id="search"
          v-model="searchQuery"
          @input="searchUsedTemplates"
          placeholder="suche"
          aria-label="E-Mail Suche"
      />
      <div id="searchIconBox">
        <img
            src="../../assets/icons/search.png"
            alt="Suchsymbol"
            id="search-icon"
            width="auto"
            height="10"
        />
      </div>
    </div>

    <div id="bigFeaturesContainer">
      <div id="mailFeaturesContainer">
        <div>
          <input
              type="checkbox"
              v-model="checkAllMails"
              id="checkbox"
              aria-label="Alle E-Mails auswählen"
              @change="selectAllMails"
          />
        </div>
        <div>
          <img
              src="../../assets/trash.png"
              alt="Trash"
              id="trash-icon"
              width="auto"
              height="12"
              @click="deleteSelectedMails"
              style="cursor: pointer"
              class="trash-icon"
          />
        </div>
      </div>


<!--
      <div id="pages">
        <div id="pagesNummern">
          <p>{{ startIndex }}</p>
          <p>-</p>
          <p>{{ endIndex }}</p>
          <p>von</p>
          <div id="totalMailsBox">
            <p>{{ totalMails }}</p>
          </div>
        </div>
        <div id="pagesButtonBox">
          <button
              @click="decrement"
              :disabled="startIndex === 1"
              class="icon-button"
              aria-label="Vorherige Seite"
          >
            <img src="../../assets/icons/pfeil-links.png" alt="Decrement" class="icon" />
          </button>
          <button
              @click="increment"
              :disabled="endIndex === totalMails"
              class="icon-button"
              aria-label="Nächste Seite"
          >
            <img src="../../assets/icons/pfeil-rechts.png" alt="Increment" class="icon" />
          </button>
        </div>
      </div>-->
    </div>

    <div id="mailsBox">
      <div id="mailContentBox">
        <div
            v-for="email in paginatedMails"
            :key="email.id"
            @click="handleEmailClick(email.id)"
            class="emailElement"
        >
          <div id="checkboxContainer">
            <input
                type="checkbox"
                v-model="selectedMailIds"
                :value="email.id"
                aria-label="E-Mail auswählen"
                @click.stop
            />
          </div>
          <div class="contactName">
            <p class="allContacts">
              <span v-for="(mailDetail, index) in email.mails" :key="mailDetail.contact?.id || index">
                {{ mailDetail.contact ? `${mailDetail.contact.lastName} ${mailDetail.contact.firstName}` : 'Unbekannter Kontakt' }}
                <span v-if="index < email.mails.length - 1">, </span>
              </span>
            </p>
          </div>
          <p id="mailHeadline">{{ email.meta.mailHeadline }}</p>
          <p id="sentOnMail">{{ email.sentOnDate }}</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref, computed, watch } from 'vue';
import { Service } from '@/services/service';
import { useRouter, useRoute } from 'vue-router';
import { useAppStore } from '@/stores/app.store';
import { parseISO, format } from 'date-fns';

const router = useRouter();
const route = useRoute();
const appStore = useAppStore();
const projectId = appStore.$state.project;

const isMailSent = ref(false);
const fetchedMails = ref<Mail[]>([]);
const totalMails = ref(0);
const limit = ref(10);
const startIndex = ref(1);
const endIndex = ref(limit.value);
const checkAllMails = ref(false);
const selectedMailIds = ref<number[]>([]);

const getMails = async () => {
  try {
    const response = await Service.getInstance().getUsedTemplates(false, appStore.$state.project);

    fetchedMails.value = response.data.map((mail: any) => {
      const sentOnDate = parseISO(mail.keyDates.sentOn);
      let formatted = format(sentOnDate, 'dd.MM.yyyy, HH:mm');

      const mappedMails: MailDetails[] = mail.mails
          .filter((mailDetail: any) => mailDetail.contact != null)
          .map((mailDetail: any) => ({
            contact: {
              id: mailDetail.contact.id,
              firstName: mailDetail.contact.firstName,
              lastName: mailDetail.contact.lastName,
              mailAddress: mailDetail.contact.mailAddress,
            },
            content: mailDetail.content,
          }));

      return {
        ...mail,
        mails: mappedMails,
        visible: true,
        sentOnDate: formatted,
      };
    });

    totalMails.value = fetchedMails.value.length;
    updatePaginationIndices();
  } catch (error) {
    console.error('Error fetching mails:', error);
  }
};

const updatePaginationIndices = () => {
  endIndex.value = Math.min(startIndex.value + limit.value - 1, totalMails.value);
};

const paginatedMails = computed(() => {
  const start = startIndex.value - 1;
  const end = endIndex.value;
  return fetchedMails.value.slice(start, end);
});

const selectAllMails = () => {
  if (checkAllMails.value) {
    selectedMailIds.value = fetchedMails.value.map(mail => mail.id);
  } else {
    selectedMailIds.value = [];
  }
};

const clickedEmailForm = () => {
  router.push({ name: 'newMail' });
};

const handleEmailClick = (mailId: number) => {
  router.push({
    name: 'MailDetail',
    params: {
      id: mailId,
      projectId: appStore.$state.project
    }
  });
};

const deleteSelectedMails = async () => {
  if (selectedMailIds.value.length === 0) {
    alert('Bitte wählen Sie mindestens eine E-Mail zum Löschen aus.');
    return;
  }

  if (!confirm('Sind Sie sicher, dass Sie die ausgewählten E-Mails löschen möchten?')) {
    return;
  }

  try {
    await Service.getInstance().deleteSentMails(selectedMailIds.value, appStore.$state.project).then(() => {
      alert('Ausgewählte E-Mails wurden gelöscht.');
      fetchedMails.value = fetchedMails.value.filter(
          (mail) => !selectedMailIds.value.includes(mail.id)
      );
      selectedMailIds.value = [];
      checkAllMails.value = false;
    });
    await getMails();
  } catch (error) {
    console.error('Fehler beim Löschen der E-Mails:', error);
    alert('Fehler beim Löschen der E-Mails.');
  }
};

const searchQuery = ref("");

const searchUsedTemplates = async () => {
  if (!searchQuery.value.trim()) {
    await getMails();
    return;
  }

  try {
    const response = await Service.getInstance().searchUsedTemplates(appStore.$state.project, searchQuery.value);

    fetchedMails.value = response.data.map((mail: any) => {
      const sentOnDate = parseISO(mail.keyDates.sentOn);
      let formatted = format(sentOnDate, 'dd.MM.yyyy, HH:mm');

      const mappedMails: MailDetails[] = mail.mails
          .filter((mailDetail: any) => mailDetail.contact != null)
          .map((mailDetail: any) => ({
            contact: {
              id: mailDetail.contact.id,
              firstName: mailDetail.contact.firstName,
              lastName: mailDetail.contact.lastName,
              mailAddress: mailDetail.contact.mailAddress,
            },
            content: mailDetail.content,
          }));

      return {
        ...mail,
        mails: mappedMails,
        visible: true,
        sentOnDate: formatted,
      };
    });

    totalMails.value = fetchedMails.value.length;
    updatePaginationIndices();
  } catch (error) {
    console.error('Error searching templates:', error);
  }
};

watch(searchQuery, async (newQuery) => {
  if (newQuery.trim() === "") {
    await getMails(); // Fallback zur Standardmethode
  } else {
    await searchUsedTemplates();
  }
});

onMounted(() => {
  getMails();
});

</script>


<style scoped>
#sentOnMail {
  width: 15%;
  margin: 0 auto; /* zentriert das Element */
  text-align: center;
}
#mailHeadline {
  width: 60%;
  font-weight: bold;
}
#checkboxContainer {
  width: 3%;
  display: flex;
  justify-content: center;
  align-items: center;
}
.contactName {
  display: flex;
  flex-wrap: nowrap; /* Verhindert den Umbruch der Kontakte */
  width: 27%; /* Maximale Breite des Containers */
  overflow: hidden; /* Verhindert Überlauf */
}

.allContacts {
  white-space: nowrap; /* Der gesamte Text bleibt in einer Zeile */
  overflow: hidden; /* Verhindert Überlauf */
  text-overflow: ellipsis; /* Zeigt '...' am Ende des Containers, wenn der Text zu lang ist */
}

.notification-box {
  background-color: green;
  color: white; /* Ensure text is readable on green background */
  display: flex;
  align-items: center;
  justify-content: center;
  width: 20vw;
  border-radius: 10px;
  position: absolute;
  top: 85vh;
  left: 75vw;
}

.notification-box p {
  padding: 0.6vw;
}

.emailElement {
  display: flex;
  flex-direction: row;
  border-bottom: #dbdbdb solid 2px;
  height: 5vh;
  justify-content: center;
  align-items: center;
  cursor: pointer;
}

.emailElement:hover {
  background-color: #f5f5f5;
}

#pagesNummern {
  display: flex;
  flex-direction: row;
  width: 70%;
  align-items: center;
  padding-left: 1%;
}

#pagesNummern p {
  font-size: 0.8rem;
  width: 12%;
  text-align: center;
}

#totalMailsBox {
  background-color: #ececec;
  border-radius: 8px;
  width: 35%;
  display: flex;
  justify-content: center;
  align-items: center;
  margin-left: 10%;
}

#totalMailsBox p {
  width: 100%;
}

#bigFeaturesContainer {
  display: flex;
  flex-direction: row;
  width: 100%;
  height: 5%;
}

#pages {
  display: flex;
  flex-direction: row;
  width: 25%;
}

#mailFeaturesContainer {
  display: flex;
  flex-direction: row;
  width: 75%;
  padding-left: 1%;
}

#mailFeaturesContainer div {
  width: 3%;
  height: 2%;
  display: flex;
  align-items: center;
  margin-top: 2%;
}

#searchIconBox {
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #ececec;
  height: 100%;
  width: 20%;
  border-radius: 5px;
}

#search-container {
  display: flex;
  flex-direction: row;
  width: 28%;
  height: 6%;
  border-radius: 5px;
  background-color: white;
  margin: 1.5% 0 1% 3%;
  box-shadow: 0 2px 4px 0 rgba(0, 0, 0, 0.1);
}

#search {
  all: unset;
  width: 80%;
  padding-left: 3%;
  font-size: 0.8rem;
}

#bigVGContainer {
  width: 86.5%;
  margin-top: 2%;
  margin-left: 1.5%;
  display: flex;
  flex-direction: column;
}

#VGHeaderBox {
  display: flex;
  flex-direction: row;
  background-color: white;
  box-shadow: 0 4px 4px 0 rgba(0, 0, 0, 0.2), 0 6px 20px 0 rgba(0, 0, 0, 0.12);
}

#vgHeading {
  margin-left: 4%;
  margin-top: 2%;
  margin-bottom: 2%;
  font-size: 1.1em;
}

#neueMail {
  all: unset;
  background-color: #78A6FF;
  height: 50%;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 7px;
  margin-left: 80%;
  margin-top: 1.8%;
  box-shadow: 0 2px 4px 0 rgba(0, 0, 0, 0.2), 0 6px 20px 0 rgba(0, 0, 0, 0.12);
  transition: background-color 0.2s ease, box-shadow 0.2s ease;
}

#neueMail:hover {
  background-color: rgb(75, 120, 216); /* Dunkleres Blau beim Hover */
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.15); /* Leichter Schatten beim Hover */
}

#neueMail p {
  padding: 0 20px;
  font-size: 1em;
  font-weight: 500;
}

#mailsBox {
  background-color: white;
  height: 75%;
  box-shadow: 0 2px 4px 0 rgba(0, 0, 0, 0.2), 0 6px 20px 0 rgba(0, 0, 0, 0.12);
  overflow-y: scroll;
}

#pagesButtonBox {
  width: 30%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.icon-button {
  background: none;
  border: none;
  cursor: pointer;
  padding: 0;
  margin: 0 2%;
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
  width: 30%;
}

.icon-button:disabled {
  cursor: not-allowed;
  opacity: 0.5;
}

.icon-button:not(:disabled):hover {
  background-color: #f0f0f0;
  border-radius: 5px;
}

.icon-button:active {
  box-shadow: 0 2px 4px 0 rgba(0, 0, 0, 0.2);
}

.icon {
  height: 70%;
  width: auto;
}

<style scoped>
   /* Trash icon styles */
 .trash-icon {
   cursor: pointer;
   transition: transform 0.2s ease, filter 0.2s ease;
 }

.trash-icon:hover {
  transform: scale(1.1);
  filter: brightness(0.9);
}

.trash-icon:active {
  transform: scale(0.95);
}

#checkboxContainer {
  width: 3%;
  display: flex;
  justify-content: center;
  align-items: center;
}

/* Additional styles */
#sentOnMail {
  width: 15%;
  margin: 0 auto; /* zentriert das Element */
  text-align: center;
}

#mailHeadline {
  width: 60%;
}

.contactName {
  display: flex;
  flex-wrap: nowrap; /* Verhindert den Umbruch der Kontakte */
  width: 27%; /* Maximale Breite des Containers */
  overflow: hidden; /* Verhindert Überlauf */
}

.allContacts {
  white-space: nowrap; /* Der gesamte Text bleibt in einer Zeile */
  overflow: hidden; /* Verhindert Überlauf */
  text-overflow: ellipsis; /* Zeigt '...' am Ende des Containers, wenn der Text zu lang ist */
}

.notification-box {
  background-color: green;
  color: white; /* Ensure text is readable on green background */
  display: flex;
  align-items: center;
  justify-content: center;
  width: 20vw;
  border-radius: 10px;
  position: absolute;
  top: 85vh;
  left: 75vw;
}

.notification-box p {
  padding: 0.6vw;
}
</style>
