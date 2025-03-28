<script setup lang="ts">
import {onMounted, ref} from 'vue';
import {logout} from "@/services/auth.service";
import {Service} from "@/services/service";

const profileImage = ref<File | null>(null);
const imageUrl = ref<string | null>(null);
const profileData = ref();
const firstname = ref();
const lastname = ref();
const email = ref();

const onImageSelected = (event: Event) => {
  const file = (event.target as HTMLInputElement).files?.[0];
  if (file) {
    profileImage.value = file;
    imageUrl.value = URL.createObjectURL(file);
  }
};

onMounted(() => {
  getProfile();
});

const getProfile = async () => {
  const response = await Service.getInstance().getProfile();
  profileData.value = response.data;
  firstname.value = profileData.value.firstName;
  lastname.value = profileData.value.lastName;
  email.value = profileData.value.mailAddress;
};
</script>

<template>

  <div id="bigVGContainer">
    <div id="VGHeaderBox">
      <h1 id="vgHeading">Profil</h1>
    </div>

    <div id="profileContentBox">

      <div id="inputBoxen">

        <div id="inputContainer">
          <div class="inputBox">
            <label for="firstName">Vorname</label>
            <input type="text" v-model="firstname" disabled>
          </div>

          <div class="inputBox">
            <label for="lastName">Nachname</label>
            <input type="text" v-model="lastname" disabled>
          </div>
        </div>

        <div class="inputBox" style="margin: 8% 0;width: 50%">
          <label for="email">Email-Adresse</label>
          <input type="email" v-model="email" disabled>
        </div>

        <div id="inputContainer">
          <div class="inputBox">
            <label for="userGroup">Benutzergruppe</label>
            <input type="text" disabled>
          </div>

          <div class="inputBox">
            <label for="lastName">Abteilung</label>
            <input type="text" disabled>
          </div>
        </div>
      </div>

      <button id="logout" @click="logout()">Logout</button>
    </div>
  </div>
</template>

<style scoped>
/* content box */
#bigVGContainer {
  width: 60vw;
  margin-top: 2%;
  height: 80vh;
  display: flex;
  flex-direction: column;
}

#profileContentBox {
  background-color: white;
  height: 85%;
  margin-top: 3%;
  padding: 6% 6%;
  box-shadow: 0 2px 4px 0 rgba(0, 0, 0, 0.2), 0 6px 20px 0 rgba(0, 0, 0, 0.12);
}

/* headline */
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
  font-size: 1.4em;
}

/* input box */
#inputContainer {
  display: flex;
  flex-direction: row;
}

#inputContainer div {
  margin-right: 5%;
}

#inputBoxen{
  display: flex;
  flex-direction: column;
}

.inputBox {
  display: flex;
  flex-direction: column;
  width: 50%;
}

.inputBox label {
  color: #848484;
}

.inputBox input {
  all: unset;
  border-bottom: #D9D9D9 solid 2px;
}

/* button */
#logout{
  all: unset;
  padding: 0.5vh 2.3vw;
  border-radius: 12px;
  background-color: #78A6FF;
  color: white;
  border: #78A6FF solid 1px;
  font-size: 0.8rem;
  margin-top: 5%;
  box-shadow: 0 2px 2px 0 rgba(0, 0, 0, 0.2);
}

#logout:hover {
  background-color: rgba(38, 93, 198, 0.8);
  color: white;
}
</style>