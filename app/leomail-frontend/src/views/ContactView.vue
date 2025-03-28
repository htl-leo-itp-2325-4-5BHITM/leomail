<script setup lang="ts">
import HeaderComponent from "@/components/header/HeaderComponent.vue";
import PersonenFormComponent from "@/components/contact/ContactFormComponent.vue";
import { Service } from "@/services/service";
import { computed, onMounted, ref, watch } from "vue";


interface BaseContact {
  id: string | null;
  mailAddress: string;
  contactType: 'NATURAL' | 'COMPANY';
}

interface NaturalContact extends BaseContact {
  firstName: string;
  lastName: string;
  gender?: string | null;
  positionAtCompany?: string | null;
  company?: string | null;
  prefixTitle?: string | null;
  suffixTitle?: string | null;
}

interface CompanyContact extends BaseContact {
  companyName: string;
}

type Contact = NaturalContact | CompanyContact;

// Reactive variables

const selectedContact = ref<Contact | null>(null);
const contactData = ref<Contact[]>([]);
const searchQuery = ref('');
const filteredContacts = ref<Contact[]>([]);
const selectedContactIndex = ref<number | null>(null);

// Fetch all contacts

const getContacts = async () => {
  try {
    const response = await Service.getInstance().searchAllContacts("");
    const data = response.data.map((item: any) => {
      if ('firstName' in item && 'lastName' in item) {
        item.contactType = 'NATURAL';
      } else if ('companyName' in item) {
        item.contactType = 'COMPANY';
      } else {
        item.contactType = 'UNKNOWN';
      }
      return item;
    });
    contactData.value = data;
    filteredContacts.value = data;
  } catch (error) {
    console.error('Error fetching contacts:', error);
  }
};


// Handle clicking on a contact

const handleClickedContact = async (item: Contact, index: number) => {
  selectedContactIndex.value = index;

  try {
    const response = await Service.getInstance().getContact(item.id!);
    selectedContact.value = response.data;
  } catch (error) {
    console.error('Error fetching contact details:', error);
  }
};

// Search contacts based on query

const searchContacts = async (query: string) => {
  if (query === '') {
    filteredContacts.value = contactData.value;
  } else {
    try {
      const response = await Service.getInstance().searchAllContacts(query);
      filteredContacts.value = response.data;
    } catch (error) {
      console.error('Error searching contacts:', error);
    }
  }
};

// Handle contact deletion

const handleDeleteContact = async () => {
  if (selectedContact.value) {
    try {
      await Service.getInstance().deleteContact(selectedContact.value.id!);
      await getContacts();
      selectedContact.value = null;
      selectedContactIndex.value = null;
    } catch (error) {
      console.error('Error deleting contact:', error);
    }
  }
};

// Watch the search query and update results

watch(searchQuery, () => {
  searchContacts(searchQuery.value);
});

// Handle creating a new contact

const handleNewContact = () => {
  selectedContact.value = null;
  selectedContactIndex.value = null;
};

// Handle contact deleted event

const handleContactDeleted = () => {
  getContacts();
  selectedContact.value = null;
  selectedContactIndex.value = null;
};

// Handle contact updated event

const handleContactUpdated = async () => {
  await getContacts();
  selectedContact.value = null;
  selectedContactIndex.value = null;
};

// Handle contact added event

const handleAddedContact = () => {
  getContacts();
  selectedContact.value = null;
  selectedContactIndex.value = null;
};

// Fetch contacts on component mount

onMounted(() => {
  getContacts();
});
</script>

<template>
  <header-component></header-component>
  <div id="bigContainer">
    <div id="listContainer">

      <div id="flexHeadline">
        <div>
          <h3 id="headline">Kontakte</h3>
        </div>
        <div>
          <div @click="handleNewContact" id="newContact">
            <img src="../assets/icons/newMail-white.png">
          </div>
        </div>
      </div>

      <div id="search-container">
        <div id="searchIconBox">
          <img src="../assets/icons/search.png" alt="Suche" id="search-icon" width="auto" height="10">
        </div>
        <input type="text" id="search" placeholder="Suche" v-model="searchQuery">
      </div>

      <div id="contactsBoxContainer">
        <a
            v-for="(item, index) in filteredContacts"
            :key="index"
            @click="handleClickedContact(item, index)"
            class="contactItems"
            :id="`contact-${index}`"
            :class="{ highlighted: selectedContactIndex === index, 'font-bold': selectedContactIndex === index }"
        >
          <template v-if="item.firstName && item.lastName">
            {{ item.firstName }} {{ item.lastName }}<br>
          </template>
          <template v-else-if="item.companyName">
            {{ item.companyName }}<br>
          </template>
          <template v-else>
            Unknown Contact<br>
          </template>
        </a>
      </div>

    </div>

    <div id="contentContainer">
      <personen-form-component
          :selectedContact="selectedContact"
          @contact-deleted="handleContactDeleted"
          @contact-updated="handleContactUpdated"
          @contact-added="handleAddedContact"
      ></personen-form-component>
    </div>
  </div>
</template>

<style scoped>
#flexHeadline {
  display: flex;
  flex-direction: row;
}

#flexHeadline * {
  width: 50%;
}

#newContact {
  background-color: #0086D4;
  border-radius: 25%;
  border-color: #0086D4;
  display: flex;
  justify-content: center;
  align-items: center;
  width: 18%;
  margin-left: 70%;
  margin-top: 2vh;
}

#newContact:focus {
  box-shadow: 0 2px 2px 0 rgba(0, 0, 0, 0.2);
}

#newContact:hover {
  box-shadow: 0 2px 2px 0 rgba(0, 0, 0, 0.2);
}

#newContact img {
  width: 100%;
  height: auto;
  padding: 10%;
}

.contactItems.highlighted {
  font-weight: bold;
}

#contactsBoxContainer a:hover {
  cursor: pointer;
}

#contactsBoxContainer {
  margin-left: 10%;
  margin-top: 5%;
}

#headline {
  font-weight: var(--font-weight-medium);
  padding: 2vh 0 1vh 1.5vw;
}

#searchIconBox {
  display: flex;
  align-items: center;
  justify-content: center;
}

#search-container {
  display: flex;
  flex-direction: row;
  width: 90%;
  margin: auto;
  border-radius: 5px;
  padding: 2% 4%;
  background-color: #ECECEC;
}

#search {
  all: unset;
  width: 90%;
  margin-left: 3%;
}

#bigContainer {
  width: 80%;
  height: 80vh;
  display: flex;
  flex-direction: row;
  margin: auto;
  margin-top: 4vh;
  box-shadow: 0 4px 8px 0 rgba(0, 0, 0, 0.2),
  0 6px 20px 0 rgba(0, 0, 0, 0.12);
}

#listContainer {
  width: 25%;
  border-right: rgba(0, 0, 0, 0.20) solid 2px;
}

#contentContainer {
  width: 75%;
}
</style>
