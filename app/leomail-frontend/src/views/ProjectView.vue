<script setup lang="ts">
import HeaderComponent from "@/components/header/HeaderComponent.vue";
import {useRouter} from 'vue-router';
import {onMounted, type Ref, ref} from 'vue';
import {Service} from "@/services/service";
import {useAppStore} from "@/stores/app.store";

const router = useRouter();
const appStore = useAppStore();
const projects = ref([]) as Ref<ProjectOverview[]>;

const clickedNewProject = () => {
  router.push({name: 'newProjects'});
}

const clickedProject = (project: ProjectOverview) => {
  appStore.project = project.id;
  router.push(`/mail?pid=${project.id}`).then(() => {
  });
}

interface ProjectOverview {
  id: number;
  name: string;
}

onMounted(() => {
  Service.getInstance().getPersonalProjects().then((response) => {
    projects.value = response.data;
  });
});
</script>

<template>
  <header-component></header-component>
  <div id="bigContainer">
    <div id="listContainer">
      <div id="flexHeadline">
        <div>
          <h3 id="headline">Projekte</h3>
        </div>
        <div>
          <div @click="clickedNewProject" id="newMailButton">
            <img src="../assets/icons/newMail-white.png">
          </div>
        </div>
      </div>

      <div id="search-container">
        <div id="searchIconBox">
          <img src="../assets/icons/search.png" alt="Suche" id="search-icon" width="auto" height="15">
        </div>
        <input type="text" id="search" placeholder="suche">
      </div>

      <div id="project-list">
        <div v-for="project in projects" :key="project.id" class="project-item" @click="clickedProject(project)">
          {{ project.name }}
        </div>
      </div>
    </div>

    <div id="contentContainer">
      <div id="infoLeoMail">
        <h3 id="headlineInfo">LeoMail</h3>
        <p>LeoMail ist eine innovative Softwarelösung, die es Ihnen ermöglicht, personalisierte E-Mails an Gruppen zu
          versenden und Projekte effizient zu verwalten.<br><br>

          Mit LeoMail können Sie ganz einfach individuelle E-Mails erstellen, die auf die Bedürfnisse und Interessen
          Ihrer Empfänger zugeschnitten sind.<br><br>

          Die benutzerfreundliche Oberfläche erlaubt es, verschiedene Gruppen zu definieren und spezifische Nachrichten
          an jede Gruppe zu senden, was die Kommunikation erheblich verbessert.</p>
      </div>
    </div>
  </div>
</template>

<style scoped>
#flexHeadline{
  display: flex;
  flex-direction: row;
}

#flexHeadline *{
  width: 50%;
}
#newMailButton {
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

#newMailButton:focus{
  box-shadow: 0 2px 2px 0 rgba(0, 0, 0, 0.2);
}

#newMailButton:hover{
  box-shadow: 0 2px 2px 0 rgba(0, 0, 0, 0.2);
}

#newMailButton img {
  width: 100%;
  height: auto;
  padding: 10%;
}

#headlineInfo {
  font-weight: var(--font-weight-medium);
}

#infoLeoMail p {
  font-size: 0.8rem;
}

#infoLeoMail {
  width: 90%;
  height: auto;
  padding: 4%;
  background-color: #A9C5DF;
  border-radius: 10px;
  display: flex;
  flex-direction: column;
  margin: auto;
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
  margin-top: 3%;
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
  box-shadow: 0 4px 8px 0 rgba(0, 0, 0, 0.2), 0 6px 20px 0 rgba(0, 0, 0, 0.12);
}

#listContainer {
  width: 25%;
  border-right: rgba(0, 0, 0, 0.20) solid 2px;
}

#contentContainer {
  width: 75%;
}

#project-list {
  margin-top: 20px;
}

.project-item {
  padding: 10px;
  border-bottom: 1px solid #ECECEC;
  cursor: pointer;
}

.project-item:hover {
  background-color: #f4f4f4;
}
</style>