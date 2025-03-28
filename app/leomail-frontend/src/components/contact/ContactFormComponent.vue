<script setup lang="ts">
import { computed, ref, watch } from "vue";
import { Service } from "@/services/service";

interface BaseContact {
  id: string | null;
  mailAddress: string;
  contactType: 'NATURAL' | 'COMPANY';
  kcUser?: boolean;
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

const picked = ref<string | null>(null); // Gender
const pickedEntity = ref<'NATURAL' | 'COMPANY'>('NATURAL'); // Contact Type
const firstname = ref('');
const lastname = ref('');
const email = ref('');
const positionAtCompany = ref('');
const company = ref('');
const prefixTitle = ref('');
const suffixTitle = ref('');
const companyName = ref('');
const isKcUser = ref(false);
const propsSelected = defineProps<{ selectedContact: Contact | null }>();
const emit = defineEmits(['contact-deleted', 'contact-updated', 'contact-added']);

watch(
    () => propsSelected.selectedContact,
    (newContact) => {
      clearForm();
      if (newContact) {
        pickedEntity.value = newContact.firstName === undefined ? 'COMPANY' : 'NATURAL';
        email.value = newContact.mailAddress;
        isKcUser.value = newContact.kcUser || false;

        if (pickedEntity.value === 'COMPANY') {
          // CompanyContact
          const companyContact = newContact as CompanyContact;
          companyName.value = companyContact.companyName || '';
        } else {
          const naturalContact = newContact as NaturalContact;
          firstname.value = naturalContact.firstName || '';
          lastname.value = naturalContact.lastName || '';
          picked.value = naturalContact.gender || null;
          positionAtCompany.value = naturalContact.positionAtCompany || '';
          company.value = naturalContact.company || '';
          prefixTitle.value = naturalContact.prefixTitle || '';
          suffixTitle.value = naturalContact.suffixTitle || '';
        }
      }
    }
);

const clearForm = () => {
  firstname.value = '';
  lastname.value = '';
  email.value = '';
  picked.value = null;
  positionAtCompany.value = '';
  company.value = '';
  prefixTitle.value = '';
  suffixTitle.value = '';
  companyName.value = '';
  isKcUser.value = false;
};

const saveOrUpdateContact = async () => {
  if (isKcUser.value) {
    return;
  }
  try {
    let contactForm: any = {
      id: propsSelected.selectedContact?.id || null,
      mailAddress: email.value,
    };

    if (pickedEntity.value === 'NATURAL') {
      contactForm = {
        ...contactForm,
        firstName: firstname.value,
        lastName: lastname.value,
        prefixTitle: prefixTitle.value || null,
        suffixTitle: suffixTitle.value || null,
        company: company.value || null,
        positionAtCompany: positionAtCompany.value || null,
        gender: picked.value,
      };

      if (propsSelected.selectedContact) {
        await Service.getInstance().updateNaturalContact(contactForm);
        emit('contact-updated');
      } else {
        await Service.getInstance().addNaturalContact(contactForm);
        emit('contact-added');
      }
    } else if (pickedEntity.value === 'COMPANY') {
      // CompanyContact
      contactForm = {
        ...contactForm,
        companyName: companyName.value,
      };

      if (propsSelected.selectedContact) {
        await Service.getInstance().updateCompanyContact(contactForm);
        emit('contact-updated');
      } else {
        await Service.getInstance().addCompanyContact(contactForm);
        emit('contact-added');
      }
    }

    clearForm();
  } catch (error) {
    console.error('Error saving data:', error);
  }
};

const deleteContact = async () => {
  if (propsSelected.selectedContact) {
    const confirmed = confirm('Möchten Sie diesen Kontakt wirklich löschen?');
    if (confirmed) {
      try {
        await Service.getInstance().deleteContact(propsSelected.selectedContact.id!);
        clearForm();
        emit('contact-deleted');
      } catch (error) {
        console.error('Fehler beim Löschen des Kontakts:', error);
      }
    }
  }
};

const contactName = computed(() => {
  if (pickedEntity.value === 'COMPANY' && companyName.value) {
    return companyName.value;
  } else if (pickedEntity.value === 'NATURAL' && (firstname.value || lastname.value)) {
    return `${firstname.value} ${lastname.value}`;
  } else {
    return 'Neuer Kontakt';
  }
});
</script>

<template>
  <form @submit.prevent="saveOrUpdateContact">
    <div id="contentContainer">
      <h3 id="headline"> {{ contactName }}</h3>
      <div id="formular">

        <div class="personContainer">
          <div class="personBox">
            <label for="radioNatural">natürliche Person</label>
            <input
                type="radio"
                class="radio"
                id="radioNatural"
                value="NATURAL"
                v-model="pickedEntity"
                :disabled="isKcUser"
            />
          </div>

          <div class="personBox">
            <label for="radioCompany">juristische Person</label>
            <input
                type="radio"
                class="radio"
                id="radioCompany"
                value="COMPANY"
                v-model="pickedEntity"
                :disabled="isKcUser"
            />
          </div>
        </div>

        <div v-if="pickedEntity === 'NATURAL'">
          <!-- NaturalContact form fields -->
          <div id="checkBoxGenderContainer">
            <label class="personen-label">Geschlecht</label><br>
            <div id="checkBox">
              <div id="boxMale">
                <label for="checkboxMale">männlich</label>
                <input
                    type="radio"
                    class="radio"
                    id="checkboxMale"
                    value="M"
                    v-model="picked"
                    :disabled="isKcUser"
                    required
                />
              </div>
              <div id="boxFemale">
                <label for="checkboxFemale">weiblich</label>
                <input
                    type="radio"
                    class="radio"
                    id="checkboxFemale"
                    value="W"
                    v-model="picked"
                    :disabled="isKcUser"
                    required
                />
              </div>
            </div>
          </div>

          <div id="titleBox" class="inputBox">
            <div>
              <label for="titelPrefix" class="personen-label">Titel prefix</label><br>
              <input
                  type="text"
                  id="titelPrefix"
                  class="formPerson"
                  placeholder="z.Bsp. Dr."
                  v-model="prefixTitle"
                  :disabled="isKcUser"
              >
            </div>
            <div>
              <label for="titelSuffix" class="personen-label">Titel suffix</label><br>
              <input
                  type="text"
                  id="titelSuffix"
                  class="formPerson"
                  placeholder="z.Bsp. PhD"
                  v-model="suffixTitle"
                  :disabled="isKcUser"
              >
            </div>
          </div>

          <div id="nameBox" class="inputBox">
            <div>
              <label for="firstName" class="personen-label">Vorname</label><br>
              <input
                  type="text"
                  id="firstName"
                  class="formPerson"
                  v-model="firstname"
                  :disabled="isKcUser"
                  required
              >
            </div>
            <div>
              <label for="lastName" class="personen-label">Nachname</label><br>
              <input
                  type="text"
                  id="lastName"
                  class="formPerson"
                  v-model="lastname"
                  :disabled="isKcUser"
                  required
              >
            </div>
          </div>

          <div class="inputBox">
            <label for="email" class="personen-label">Email</label><br>
            <input
                type="email"
                id="email"
                class="formPerson"
                placeholder="z.Bsp. max.muster@gmail.com"
                v-model="email"
                :disabled="isKcUser"
                required
            >
          </div>
          <div>
            <div class="inputBox">
              <label for="company" class="personen-label">Firma (optional)</label><br>
              <input
                  type="text"
                  id="company"
                  class="formPerson"
                  v-model="company"
                  :disabled="isKcUser"
              >
            </div>
            <div class="inputBox">
              <label for="position" class="personen-label">Position (optional)</label><br>
              <input
                  type="text"
                  id="position"
                  class="formPerson"
                  v-model="positionAtCompany"
                  :disabled="isKcUser"
              >
            </div>
          </div>
        </div>

        <div v-if="pickedEntity === 'COMPANY'">
          <!-- CompanyContact form fields -->
          <div>
            <label for="companyName" class="personen-label">Unternehmensbezeichnung</label><br>
            <input
                type="text"
                id="companyName"
                class="formPerson"
                v-model="companyName"
                :disabled="isKcUser"
                required
            >
          </div>
          <div>
            <label for="emailCompany" class="personen-label">E-Mail</label><br>
            <input
                type="email"
                id="emailCompany"
                class="formPerson"
                v-model="email"
                :disabled="isKcUser"
                required
            >
          </div>
        </div>

        <div id="buttonBox">
          <button type="button" id="deleteButton" @click="deleteContact" v-if="propsSelected.selectedContact && !isKcUser">Kontakt löschen</button>
          <button type="submit" id="submitButton" v-if="!isKcUser">
            {{ propsSelected.selectedContact ? 'Speichern' : 'Kontakt erstellen' }}
          </button>
        </div>
      </div>
    </div>
  </form>
</template>

<style scoped>
.inputBox {
  margin-top: 1vh;
}

#titleBox {
  display: flex;
  flex-direction: row;
  width: 100%;
}

#titleBox div {
  width: 15%;
}

#titelPrefix, #titelSuffix {
  width: 60%;
}

#email, #company, #position {
  width: 28%;
}

.personBox {
  display: flex;
  flex-direction: row;
  width: 20%;
}

.personBox label {
  margin-right: 5%;
}

.personContainer {
  display: flex;
  flex-direction: row;
  width: 100%;
}

#nameBox {
  display: flex;
  flex-direction: row;
  width: 100%;
}

#nameBox div {
  width: 30%;
}

#firstName, #lastName {
  width: 80%;
}

.personen-label {
  color: #5A5A5A;
  font-size: 0.8em;
}

.formPerson::placeholder {
  color: #B3B3B3;
}

.formPerson {
  display: block;
  all: unset;
  border: solid 1px #D3D3D3;
  border-radius: 5px;
  padding: 0.6vw;
  font-size: 0.8em;
  background-color: #F6F6F6;
}

.formPerson:focus {
  box-shadow: 0 2px 2px 0 rgba(0, 0, 0, 0.2);
}

#checkBox {
  display: flex;
  flex-direction: row;
}

#checkBox div {
  width: 15%;
}

#checkBox div label {
  margin-right: 5%;
}

#checkBoxGenderContainer {
  margin-top: 2%;
}

#formular {
  margin-top: 5%;
  height: 60vh;
}

#headline {
  font-weight: bold;
}

#contentContainer {
  padding: 3% 5%;
}

#buttonBox {
  position: absolute; /* Added */
  top: 80vh; /* Adjust as needed */
  right: 15vw; /* Adjust as needed */
  width: 12vw;
  margin-top: 0; /* Removed margin-top */
}

#submitButton {
  all: unset;
  border-radius: 12px;
  padding: 1vh 0;
  background-color: #78A6FF;
  color: white;
  width: 60%;
  border: #78A6FF solid 1px;
  font-size: 0.8rem;
  text-align: center;
  margin-left: 50%;
}

#submitButton:hover {
  background-color: rgba(75, 129, 253, 0.86);
  box-shadow: 0 2px 2px 0 rgba(0, 0, 0, 0.2);
}

#submitButton:disabled {
  background-color: lightgray;
  border-color: lightgray;;
}

#submitButton:disabled:hover {
  border-color: lightgray;
  box-shadow: none;
}

#deleteButton{
  all: unset;
  border-radius: 12px;
  padding: 1vh 0;
  background-color: #f5151c;
  color: white;
  width: 100%;
  border: #ff393f solid 1px;
  font-size: 0.8rem;
  text-align: center;
}

#deleteButton:hover{
  background-color: rgba(253, 75, 96, 0.86);
  box-shadow: 0 2px 2px 0 rgba(0, 0, 0, 0.2);
}

#buttonBox{
  display: flex;
  flex-direction: row;
  width: 18%;
}
</style>
