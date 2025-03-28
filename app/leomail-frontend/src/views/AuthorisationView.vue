<script setup lang="ts">

import HeaderLoginComponent from "@/components/header/HeaderLoginComponent.vue";
import AuthorisationFormComponent from "@/components/AuthorisationFormComponent.vue";

</script>

<template>
<header-login-component></header-login-component>
  <div id="auth-container">
    <h2 id="headline">Office-Account Authorisation</h2>
    <authorisation-form-component></authorisation-form-component>
  </div>
</template>

<style scoped>
#headline {
  font-weight: bold;
  text-align: center;
}
#auth-container {
  width: 35vw;
  height: 55vh;
  padding: 2%;
  margin: auto;
  margin-top: 15vh;
  display: flex;
  flex-direction: column;
  box-shadow: 0 4px 4px 0 rgba(0, 0, 0, 0.2), 0 6px 20px 0 rgba(0, 0, 0, 0.12);
}

</style>