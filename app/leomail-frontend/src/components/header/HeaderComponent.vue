<script setup lang="ts">
import {logout} from "@/services/auth.service";
import {Service} from "@/services/service";
import {onMounted, ref} from "vue";
import {useAppStore} from "@/stores/app.store";
import {useRoute} from "vue-router";

const profileData = ref();
const name = ref();
const projectName = ref();

const route = useRoute();
const dropdownVisible = ref(false);

onMounted(() => {
  getProfile();

  const pid = route.query.pid;
  if (pid) {
    getProjectName();
  }
});

const toggleDropdown = () => {
  dropdownVisible.value = !dropdownVisible.value;
}


const getProfile = async () => {
  const response = await Service.getInstance().getProfile();
  profileData.value = response.data;
  name.value = profileData.value.firstName + " " + profileData.value.lastName;
};

const getProjectName = async () => {
  const response = await Service.getInstance().getProjectName(useAppStore().$state.project);
  projectName.value = response.data;
};
</script>

<template>
  <div id="headerNav">
    <RouterLink to="/"><img alt="LeoMail-Logo" class="logo" src="../../assets/LeoMail.png" width="auto" height="45">
    </RouterLink>
    <div id="HeaderLinkBox">
      <div class="routeBox">
        <RouterLink to="/" activeClass="highlight" class="headerLink">Projekte</RouterLink>
      </div>
      <div class="routeBox">
        <RouterLink to="/contacts" activeClass="highlight" class="headerLink">Personen</RouterLink>
      </div>
    </div>
    <div id="projectName" v-if="projectName">
      <p>{{ projectName }}</p>
    </div>
    <!-- ProfilBox nach rechts -->
    <div id="profilBox" @click="toggleDropdown">
      <span>{{ name }}</span>
      <!-- Dropdown Menu mit Transition -->
      <transition name="dropdown">
        <div v-if="dropdownVisible" class="dropdownMenu">
          <RouterLink to="/profile">Mein Profil</RouterLink>
          <a href="#" @click="logout()">Logout</a>
        </div>
      </transition>
    </div>
  </div>
</template>

<style scoped>
/* project name */
#projectName{
  color: #78A6FF;
  border-bottom: #78A6FF solid 1px;
  padding: 0.5% 0.8%;
}
#projectName p{
  font-family: "Adelle Sans Devanagari",sans-serif;
  font-weight: bold;
}
/* Dropdown */
#profilBox {
  position: relative;
  display: inline-block;
  cursor: pointer;
}

.dropdownMenu {
  position: absolute;
  right: 0;
  top: 100%;
  background-color: white;
  border: 1px solid #ccc;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.1);
  z-index: 1000;
  width: 180px;
  display: flex;
  flex-direction: column;
  border-radius: 8px; /* Runde Ecken */
  overflow: hidden;
  transition: all 0.3s ease-in-out;
}

.dropdownMenu a,
.dropdownMenu RouterLink {
  padding: 12px 15px;
  text-decoration: none;
  color: #333;
  text-align: left;
  font-size: 14px;
  font-family: "Adelle Sans Devanagari", sans-serif;
  transition: background-color 0.2s, color 0.2s;
}

.dropdownMenu a:hover,
.dropdownMenu RouterLink:hover {
  background-color: #f0f4ff;
  color: #1b73e8;
}

/* Transition für das Dropdown-Menu */
.dropdown-enter-active, .dropdown-leave-active {
  transition: opacity 0.3s ease, transform 0.3s ease;
}

.dropdown-enter-from, .dropdown-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}

.dropdown-enter-to, .dropdown-leave-from {
  opacity: 1;
  transform: translateY(0);
}

#profilBox {
  margin-left: auto;
  margin-right: 2%;
}

body {
  margin: 0;
  padding: 0;
}

#headerNav {
  background-color: white;
  display: flex;
  align-items: center;
  height: 10vh;
  padding-left: 2.5%;
  padding-right: 2.5%; /* Etwas Platz von rechts */
  box-shadow: 0 4px 8px 0 rgba(0, 0, 0, 0.2), 0 6px 20px 0 rgba(0, 0, 0, 0.12);
}

.headerLink {
  /*border-left: black solid 1px;
  border-right: black solid 1px;*/
  padding: 10%;
  text-decoration: none;
  color: #1B1B1B;
  margin-left: 5%;
}

#HeaderLinkBox a {
  font-size: 0.9em;
}

#HeaderLinkBox {
  margin-left: 2%;
  padding-top: 10px;
  height: 100%;
  display: flex;
  align-items: center;
  width: 40%;
}

.routeBox {
  border-right: black solid 1px;
  width: 30%;
  text-align: center;
}

.highlight {
  font-weight: var(--font-weight-bold);
  border-bottom: lightgray solid 1px;
}

.headerLink:hover {
  font-weight: var(--font-weight-bold);
}
</style>