<script setup lang="ts">

import MailDetailComponent from "@/components/mail/MailDetailComponent.vue";
import HeaderComponent from "@/components/header/HeaderComponent.vue";
import NavComponent from "@/components/navigation/NavComponent.vue";
import {ref} from "vue";

const mailId = ref('1');
const projectId = ref('123e4567-e89b-12d3-a456-426614174000');
</script>

<template>
  <header-component></header-component>
  <div id="mail-mainBox">
    <nav-component></nav-component>
    <MailDetailComponent :id="mailId" :project-id="projectId"></MailDetailComponent>
  </div>
</template>

<style scoped>
#mail-mainBox{
  display: flex;
  flex-direction: row;
  width: 100%;
}
</style>
