<script setup lang="ts">

import HeaderComponent from "@/components/header/HeaderComponent.vue";
import NavComponent from "@/components/navigation/NavComponent.vue";
import MailFormComponent from "@/components/mail/MailFormComponent.vue";
</script>

<template>
  <header-component>
  </header-component>
  <div id="mail-mainBox">
    <nav-component></nav-component>
    <mail-form-component></mail-form-component>
  </div>
</template>

<style scoped>
#mail-mainBox{
  display: flex;
  flex-direction: row;
  width: 100%;
}
</style>