<script setup lang="ts">
import {useAppStore} from "@/stores/app.store";
import {computed, onMounted, ref} from 'vue';

const appStore = useAppStore();
const currentProjectId = computed(() => appStore.project);
</script>

<template>
    <div id="bigNavBox">
      <div id="iconLinks">
        <div class="iconBox">
          <RouterLink :to="{ path: '/mail', query: { pid: currentProjectId } }" activeClass="highlight">
            <img src="../../assets/icons/mail.svg" alt="Mail Icon"/>
          </RouterLink>
        </div>
        <div class="iconBox">
          <RouterLink :to="{ path: '/scheduledMails', query: { pid: currentProjectId } }" activeClass="highlight">
            <img src="../../assets/icons/sceduled-mail.svg" alt="Scheduled Mail Icon"/>
          </RouterLink>
        </div>
        <div class="iconBox">
          <RouterLink :to="{ path: '/groups', query: { pid: currentProjectId } }" activeClass="highlight">
            <img src="../../assets/icons/gruppe.svg" alt="Group Icon"/>
          </RouterLink>
        </div>
        <div class="iconBox">
          <RouterLink :to="{ path: '/template', query: { pid: currentProjectId } }" activeClass="highlight">
            <img src="../../assets/icons/dokument.svg" alt="Vorlagen Icon"/>
          </RouterLink>
        </div>
        <div class="iconBox">
          <RouterLink :to="{ path: '/settings', query: { pid: currentProjectId } }" activeClass="highlight">
            <img src="../../assets/icons/settings.png" alt="Settings Icon"/>
          </RouterLink>
        </div>
      </div>
    </div>
</template>


<style scoped>
#bigNavBox {
  margin-top: 3vh;
  width: 8%;
  border-right: rgba(0, 0, 0, 0.20) solid 2px;
  border-radius: 20px;
  padding-bottom: 1%;
  padding-top: 1%;
  height: 83vh;
  display: flex;
  justify-content: center;
}

#iconLinks {
  display: flex;
  flex-direction: column;
  align-items: center;
  height: 100%;
  width: 80%;
}

.iconBox {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 12%;
}

.iconBox img {
  width: 3.5vh;
  cursor: pointer;
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
}

.highlight {
  background-color: lightgray;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 7vh;
  height: 7vh;
}


</style>