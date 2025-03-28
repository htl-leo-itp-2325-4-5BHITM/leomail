<script setup lang="ts">
import ProfileComponent from "@/components/profile/ProfileComponent.vue";
import HeaderComponent from "@/components/header/HeaderComponent.vue";
</script>

<template>
  <header-component></header-component>
  <div id="mail-mainBox">
    <profile-component></profile-component>
  </div>
</template>

<style scoped>
#mail-mainBox{
  display: flex;
  justify-content: center;
  align-items: center;
}
</style>