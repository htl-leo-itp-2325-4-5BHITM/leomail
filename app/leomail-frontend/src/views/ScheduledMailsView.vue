<script setup lang="ts">
import NavComponent from "@/components/navigation/NavComponent.vue";
import MailsComponent from "@/components/mail/MailsComponent.vue";
import HeaderComponent from "@/components/header/HeaderComponent.vue";
import ScheduledMailsComponent from "@/components/mail/ScheduledMailsComponent.vue";
</script>

<template>
  <header-component></header-component>
  <div id="mail-mainBox">
    <NavComponent></NavComponent>
    <ScheduledMailsComponent></ScheduledMailsComponent>
  </div>
</template>

<style>
#mail-mainBox{
  display: flex;
  flex-direction: row;
  width: 100%;
}
</style>