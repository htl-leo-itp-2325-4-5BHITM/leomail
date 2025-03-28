<script setup lang="ts">

import NavComponent from "@/components/navigation/NavComponent.vue";
import HeaderComponent from "@/components/header/HeaderComponent.vue";
import MainComponent from "@/components/mail/MailsComponent.vue";
import SettingsComponent from "@/components/settings/SettingsComponent.vue";
</script>

<template>
  <header-component></header-component>
  <div id="mainBox">
    <nav-component></nav-component>
    <settings-component></settings-component>
  </div>
</template>

<style scoped>
#mainBox{
  display: flex;
  flex-direction: row;
  width: 100%;
}
</style>