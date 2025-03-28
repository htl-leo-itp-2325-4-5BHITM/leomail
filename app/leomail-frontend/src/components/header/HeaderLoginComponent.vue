<script setup lang="ts">

import {logout} from "@/services/auth.service";
</script>

<template>
  <div id="headerNav">
    <img alt="LeoMail-Logo" class="logo" src="../../assets/LeoMail.png" width="auto" height="45">
  </div>
</template>

<style scoped>
body{
  margin: 0;
  padding: 0;
}
#headerNav{
  background-color: white;
  display: flex;
  flex-direction: row;
  align-items: center;
  height: 10vh;
  padding-left: 2.5%;
  box-shadow: 0 4px 8px 0 rgba(0, 0, 0, 0.2), 0 6px 20px 0 rgba(0, 0, 0, 0.12);
}
</style>