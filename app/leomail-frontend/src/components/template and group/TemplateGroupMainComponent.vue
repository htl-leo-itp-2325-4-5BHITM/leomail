<script setup lang="ts">
import NavComponent from "@/components/navigation/NavComponent.vue";
import { onMounted, ref, shallowRef, watch, computed } from "vue";
import { Service } from "@/services/service";
import { useRoute } from "vue-router";
import GroupComponent from "@/components/template and group/group/GroupComponent.vue";
import TemplateComponent from "@/components/template and group/template/TemplateComponent.vue";
import { useAppStore } from "@/stores/app.store";

const route = useRoute();
const appStore = useAppStore();

const fetchedData = ref([]);
const selectedTemplate = ref(null);
let headlineVG = ref('');
let newVGButton = ref('');
let formVG = shallowRef(null);

const searchQuery = ref('');

const filteredTemplates = computed(() => {
  if (!searchQuery.value) return fetchedData.value;
  return fetchedData.value.filter(template =>
      template.name.toLowerCase().includes(searchQuery.value.toLowerCase())
  );
});

const getData = async () => {
  let response;
  try {
    if (route.path.includes('template')) {
      response = await Service.getInstance().getTemplates(appStore.$state.project);
    } else if (route.path.includes('groups')) {
      response = await Service.getInstance().getPersonalGroups(appStore.$state.project);
    }
    fetchedData.value = response.data;
  } catch (error) {
    console.error('Fehler beim Abrufen der Daten:', error);
  }
};

const handleCreate = () => {
  if (route.path.includes('template')) {
    formVG.value = TemplateComponent;
  } else if (route.path.includes('groups')) {
    formVG.value = GroupComponent;
  }
};

const handleClick = async (item) => {
  try {
    let response;
    if (route.path.includes('groups')) {
      response = await Service.getInstance().getGroupDetails(appStore.$state.project, item.id);
    } else if (route.path.includes('template')) {
      response = await Service.getInstance().getTemplateById(item.id);
    }
    console.log("Clicked item:", item, "Full response:", response);
    // Falls response.data undefined ist, wird der Response selbst verwendet
    selectedTemplate.value = response.data || response;
  } catch (error) {
    console.error('Fehler beim Laden:', error);
  }
};


const handleNewAddedObject = (newObject) => {
  fetchedData.value.push(newObject);
  getData();
};

const handleRemovedObject = (removedObject) => {
  selectedTemplate.value = null;
  fetchedData.value = fetchedData.value.filter((obj) => removedObject.id !== obj.id);
  getData();
};

const handleSavedObject = (savedObject = {}) => {
  if (!savedObject || Object.keys(savedObject).length === 0) {
    console.error('Saved object is invalid or undefined!');
    return;
  }

  const index = fetchedData.value.findIndex(obj => obj.id === savedObject.id);
  if (index !== -1) {
    fetchedData.value[index] = savedObject;
  } else {
    fetchedData.value.push(savedObject);
  }

  // Mehrfache Aufrufe von getData() können hier zu unerwünschten Effekten führen!
  getData();
  getData();
};

onMounted(() => {
  getData();
});

watch(
    () => route.path,
    (newPath) => {
      if (newPath.toLowerCase().includes('template')) {
        headlineVG.value = 'Vorlagen';
        newVGButton.value = 'Neue Vorlage';
        formVG.value = TemplateComponent;
      } else if (newPath.toLowerCase().includes('groups')) {
        headlineVG.value = 'Gruppen';
        newVGButton.value = 'Neue Gruppe';
        formVG.value = GroupComponent;
      }
      getData();
    },
    { immediate: true }
);
</script>

<template>
  <div id="VGMainContainer">
    <nav-component></nav-component>

    <div id="bigVGContainer">
      <div id="dataVGContainer">
        <h3 id="headlineDataVG">{{ headlineVG }}</h3>

        <div id="getVGBox">
          <div id="search-container">
            <div id="searchIconBox">
              <img src="../../assets/icons/search.png" alt="Suche" id="search-icon" width="auto" height="10">
            </div>
            <input v-model="searchQuery" type="text" id="search" placeholder="suche">
          </div>

          <ul id="templateGroupItemsContainer">
            <li
                v-for="(item, index) in filteredTemplates"
                :key="index"
                :id="String('template-' + index)"
                @click="handleClick(item)"
                class="templateGroupItems"
                :class="{ highlighted: selectedTemplate && selectedTemplate.id === item.id }"
            >
              {{ item.name }}
            </li>
          </ul>
        </div>

        <div id="newVGBox">
          <button id="newVGButton" @click="handleCreate()">
            <img src="../../assets/icons/newMail.png" width="auto" height="15">
            {{ newVGButton }}
          </button>
        </div>
      </div>

      <div id="contentVGContainer">
        <div id="VGHeaderBox">
          <h1 id="vgHeading">{{ headlineVG }}</h1>
        </div>

        <div id="VGFormBox">
          <router-view>
            <component
                :is="formVG"
                @group-added="handleNewAddedObject"
                @group-removed="handleRemovedObject"
                @group-saved="handleSavedObject"
                :selected-template="selectedTemplate"
            ></component>
            </router-view>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
#VGMainContainer {
  display: flex;
  flex-direction: row;
}

#bigVGContainer {
  width: 92%;
  margin-top: 2%;
  padding-left: 0.3%;
  display: flex;
  flex-direction: row;
}

#newVGButton {
  background-color: unset;
  border: none;
  color: #A3A3A3;
  margin-top: 1vh;
  width: 100%;
  display: flex;
  align-items: center;
  border-radius: 7px;
  padding: 0.5vh 0.5vw;
  transition: all 0.3s ease-in-out;
  font-size: 1em;
}

#newVGButton:hover {
  box-shadow: 0 4px 4px 0 rgba(0, 0, 0, 0.2);
}

#newVGButton:active {
  padding: 1vh 1vw;
  box-shadow: 0 6px 6px 0 rgba(0, 0, 0, 0.3);
}

#newVGButton img {
  margin-right: 0.5vw;
}

#dataVGContainer {
  width: 15vw;
  padding: 0 1%;
  display: flex;
  flex-direction: column;
}

#headlineDataVG {
  margin-top: 5%;
  margin-left: 5%;
  font-size: 1.1rem;
  text-align: left;
}

#newVGBox {
  height: 40%;
  border-top: #A3A3A3 solid 2px;
  width: 100%;
}

#contentVGContainer {
  width: 80vw;
  height: 83vh;
  padding-left: 1%;
  padding-right: 2%;
}

input[type="text"] {
  all: unset;
  width: 60%;
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
  margin-left: 1%;
  margin-right: 1%;
  border-radius: 5px;
  padding: 2% 4%;
  background-color: #ECECEC;
}

#search {
  width: 90%;
  margin-left: 3%;
  font-size: 0.8rem;
}

#search-container:focus {
  box-shadow: 0 2px 2px 0 rgba(0, 0, 0, 0.2);
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
  font-size: 1.1rem;
}

#VGFormBox {
  height: 85%;
  margin-top: 2%;
  background-color: white;
  box-shadow: 5px 5px 10px lightgray;
}

#getVGBox {
  display: flex;
  flex-direction: column;
  height: 60%;
}

#templateGroupItemsContainer {
  display: flex;
  flex-direction: column;
  list-style-type: none;
  padding: 0;
  margin: 0;
  box-sizing: border-box;
  word-wrap: break-word;
  height: 100%;
  overflow-y: auto;
  padding-right: 10px;
}

#templateGroupItemsContainer::-webkit-scrollbar {
  width: 8px;
}

#templateGroupItemsContainer::-webkit-scrollbar-track {
  background: #f1f1f1;
}

#templateGroupItemsContainer::-webkit-scrollbar-thumb {
  background: #888;
  border-radius: 4px;
}

#templateGroupItemsContainer::-webkit-scrollbar-thumb:hover {
  background: #555;
}

.templateGroupItems {
  border-bottom: 1px solid #ddd;
  cursor: pointer;
  font-size: 0.8rem;
  padding: 8px;
}

.templateGroupItems:hover {
  color: #9f9f9f;
}

.highlighted {
  font-weight: var(--font-weight-bold);
}
</style>