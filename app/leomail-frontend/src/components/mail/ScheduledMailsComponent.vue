<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue';
import { Service } from '@/services/service';
import { parseISO, format, isToday } from 'date-fns';
import { useRouter, useRoute } from 'vue-router';
import { useAppStore } from '@/stores/app.store';

const route = useRoute();
const router = useRouter();
const headline = ref('');
const isMailSent = ref(false);

interface MailMeta {
  templateName: string;
  mailHeadline: string;
  mailContent: string;
  greetingId: number;
}

interface MailKeyDates {
  created: string;
  sentOn: string;
  scheduledAt: string | null;
}

interface MailAccountInformation {
  createdBy: string;
  sentBy: string;
}

interface Contact {
  id: number;
  firstName: string;
  lastName: string;
  mailAddress: string;
}

interface MailDetails {
  contact: Contact;
  content: string;
}

interface Mail {
  id: number;
  meta: MailMeta;
  keyDates: MailKeyDates;
  accountInformation: MailAccountInformation;
  mails: MailDetails[];
  visible: boolean;
}

const fetchedMails = ref<Mail[]>([]);
const totalMails = ref(0);
const limit = ref(10);
const startIndex = ref(1);
const endIndex = ref(limit.value);
const checkAllMails = ref(false);
const appStore = useAppStore();

const getMails = async () => {
  try {
    const response = await Service.getInstance().getUsedTemplates(true, appStore.$state.project);
    fetchedMails.value = response.data.map((mail: any) => {
      const scheduledAt = parseISO(mail.keyDates.scheduledAt);
      const formattedScheduledAt = isToday(scheduledAt)
          ? format(scheduledAt, 'HH:mm')
          : format(scheduledAt, 'dd.MM.yyyy, HH:mm');

      return {
        id: mail.id,
        meta: {
          templateName: mail.meta.templateName,
          mailHeadline: mail.meta.mailHeadline,
          mailContent: mail.meta.mailContent,
          greetingId: mail.meta.greetingId,
        },
        keyDates: {
          created: mail.keyDates.created,
          sentOn: mail.keyDates.sentOn,
          scheduledAt: formattedScheduledAt,
        },
        accountInformation: {
          createdBy: mail.accountInformation.createdBy,
          sentBy: mail.accountInformation.sentBy,
        },
        mails: mail.mails.map((mailDetail: any) => ({
          contact: {
            id: mailDetail.contact.id,
            firstName: mailDetail.contact.firstName,
            lastName: mailDetail.contact.lastName,
            mailAddress: mailDetail.contact.mailAddress,
          },
          content: mailDetail.content,
        }))
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

const decrement = () => {
  if (startIndex.value - limit.value >= 1) {
    startIndex.value -= limit.value;
    endIndex.value = startIndex.value + limit.value - 1;
  } else {
    startIndex.value = 1;
    endIndex.value = limit.value;
  }
};

const increment = () => {
  if (endIndex.value + limit.value <= totalMails.value) {
    startIndex.value += limit.value;
    endIndex.value = startIndex.value + limit.value - 1;
  } else {
    startIndex.value = Math.max(totalMails.value - limit.value + 1, 1);
    endIndex.value = totalMails.value;
  }
};

const handleEmailClick = (mailId: number) => {
  router.push({ name: 'MailDetail', params: { id: mailId, projectId: appStore.$state.project } });
};

onMounted(() => {
  getMails();
});

watch(() => route.query.mailsend, (newValue) => {
  if (newValue === 'true') {
    isMailSent.value = true;
    setTimeout(() => {
      isMailSent.value = false;
      const query = { ...route.query };
      delete query.mailsend;
      router.replace({ path: route.path, query });
    }, 5000);
  }
});
</script>

<template>
  <div id="bigVGContainer">
    <div id="VGHeaderBox">
      <h1 id="vgHeading">Geplante Emails</h1>
    </div>

    <div id="search-container">
      <input type="text" id="search" placeholder="suche" />
      <div id="searchIconBox">
        <img src="../../assets/icons/search.png" alt="Suche" id="search-icon" width="auto" height="10" />
      </div>
    </div>

    <div id="bigFeaturesContainer">
      <div id="mailFeaturesContainer">
        <div>
          <input type="checkbox" v-model="checkAllMails" id="checkbox" />
        </div>
        <div>
          <img src="../../assets/trash.png" alt="Trash" id="trash-icon" width="auto" height="12" />
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
          <button @click="decrement" :disabled="startIndex === 1" class="icon-button">
            <img src="../../assets/icons/pfeil-links.png" alt="Decrement" class="icon" />
          </button>
          <button @click="increment" :disabled="endIndex === totalMails" class="icon-button">
            <img src="../../assets/icons/pfeil-rechts.png" alt="Increment" class="icon" />
          </button>
        </div>
      </div>-->
    </div>

    <div id="mailsBox">
      <div id="mailContentBox">
        <div v-for="email in paginatedMails" :key="email.id" @click="handleEmailClick(email.id)" class="emailElement">
          <div id="checkboxContainer"><input type="checkbox" id="checkbox" /></div>
          <div class="contactName">
            <p class="allContacts">
              <span v-for="(mailDetail, index) in email.mails" :key="mailDetail.contact.id">
                {{ mailDetail.contact.lastName }} {{ mailDetail.contact.firstName }}<span v-if="index < email.mails.length - 1">,</span>
              </span>
            </p>
          </div>
          <p id="mailHeadline">{{ email.meta.mailHeadline }}</p>
          <p id="sentOnMail">{{ email.keyDates.scheduledAt }}</p>
        </div>
        <transition name="fade">
          <div v-if="isMailSent" class="notification-box">
            <p>E-Mail wurde erfolgreich gesendet!</p>
          </div>
        </transition>
      </div>
    </div>
  </div>
</template>

<style scoped>
#sentOnMail{
  width: 15%;
  margin: 0 auto; /* zentriert das Element */
  text-align: center;
}
#mailHeadline{
  width: 55%;
}
#checkboxContainer{
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
  background-color: lightgreen;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 20vw;
  border-radius: 10px;
  position: absolute;
  top: 85vh;
  left: 75vw;
  transform: translate(-50%, -50%);
}

.fade-enter-active, .fade-leave-active {
  transition: opacity 2s; /* Dauer der Einblendung/Ausblendung */
}

.fade-enter, .fade-leave-to /* .fade-leave-active in <2.1.8 */
{
  opacity: 0;
}

.notification-box p {
  padding: 0.6vw;
}

.emailElement {
  display: flex;
  flex-direction: row;
  border-bottom: #DBDBDB solid 2px;
  height: 4vh;
  justify-content: center;
  align-items: center;
}

.emailElement:hover {
  background-color: #f5f5f5;
}

#form {
  width: 86vw;
  margin-top: 4vh;
  margin-left: 1.5%;
  display: flex;
  flex-direction: column;
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
  background-color: #ECECEC;
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
  background-color: #ECECEC;
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
  background-color: #E8E8E8;
  height: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 7px;
  margin-left: 80%;
  margin-top: 1.8%;
  box-shadow: 0 2px 4px 0 rgba(0, 0, 0, 0.2), 0 6px 20px 0 rgba(0, 0, 0, 0.12);
  border: #8a8a8a solid 2px;
}

#neueMail:hover {
  background-color: #a2a2a2;
  color: white;
  border-color: #8a8a8a;
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
</style>