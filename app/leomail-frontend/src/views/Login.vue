<template>
  <HeaderLoginComponent />
  <div id="login-container">
    <h2 id="headline">Benutzer-Login</h2>
    <form @submit.prevent="handleLogin">
      <div class="form-group">
        <label for="username" class="login-label">Benutzername:</label><br>
        <input
            type="text"
            id="username"
            class="formLogin"
            v-model="username"
            required
        />
      </div>
      <div class="form-group">
        <label for="password" class="login-label">Passwort:</label><br>
        <input
            type="password"
            id="password"
            class="formLogin"
            v-model="password"
            required
        />
      </div>
      <button type="submit" id="loginButton">Login</button>
      <p v-if="errorMessage" id="errorMessage">{{ errorMessage }}</p>
    </form>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import HeaderLoginComponent from '@/components/header/HeaderLoginComponent.vue';
import {login} from "@/services/auth.service";

const username = ref('');
const password = ref('');
const errorMessage = ref('');

const router = useRouter();

const handleLogin = async () => {
  try {
    const loginSuccess = await login(username.value, password.value);

    if (loginSuccess) {
      // Redirect to 'post-login' to handle Outlook authorization
      router.replace({ name: 'post-login' });
    } else {
      errorMessage.value = 'Ungültige Anmeldedaten.';
    }
  } catch (error: any) {
    errorMessage.value = error.message || 'Ein Fehler ist aufgetreten.';
  }
};
</script>

<style scoped>
/* Existing styles... */
#errorMessage {
  color: red;
}
#headline {
  font-weight: bold;
  text-align: center;
}

#login-container {
  width: 35vw;
  height: 55vh;
  padding: 2%;
  margin: auto;
  margin-top: 15vh;
  display: flex;
  flex-direction: column;
  box-shadow: 0 4px 4px 0 rgba(0, 0, 0, 0.2),
  0 6px 20px 0 rgba(0, 0, 0, 0.12);
}

.form-group {
  margin-bottom: 3vh;
}

form {
  margin-top: 5vh;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.login-label {
  color: #5a5a5a;
  font-size: 0.8em;
}

#loginButton {
  all: unset;
  width: 30vw;
  border-radius: 5px;
  padding: 0.6vw;
  border: solid 1px #0086d4;
  background-color: #0086d4;
  color: white;
  text-align: center;
  margin-top: 2vh;
}

#loginButton:hover {
  box-shadow: 0 2px 2px 0 rgba(0, 0, 0, 0.2);
}

#loginButton:active {
  background-color: #01379a;
}

.formLogin::placeholder {
  color: #b3b3b3;
}

.formLogin {
  all: unset;
  border: solid 1px #bebebe;
  border-radius: 5px;
  padding: 0.9vw 0.6vw;
  width: 30vw;
  font-size: 1em;
}

.formLogin:focus {
  box-shadow: 0 2px 2px 0 rgba(0, 0, 0, 0.2);
}
</style>