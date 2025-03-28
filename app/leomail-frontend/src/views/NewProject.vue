<script setup lang="ts">

import HeaderComponent from "@/components/header/HeaderComponent.vue";
import ProjectFormComponent from "@/components/project/ProjectComponent.vue";
</script>

<template>
  <header-component></header-component>
  <div id="bigContainer">
    <project-form-component></project-form-component>
  </div>
</template>

<style scoped>
#bigContainer{
  width: 80%;
  height: 80vh;
  display: flex;
  flex-direction: row;
  margin: auto;
  margin-top: 4vh;
  box-shadow: 0 4px 8px 0 rgba(0, 0, 0, 0.2), 0 6px 20px 0 rgba(0, 0, 0, 0.12);
}
</style>