<template>
  <div v-if="visible" class="error-box">
    <p>{{ message }}</p>
    <button @click="closeBox">Schließen</button>
  </div>
</template>

<script setup lang="ts">
import {ref, watch} from 'vue';

const props = defineProps({
  message: String,
});

const visible = ref(true);

watch(() => props.message, (newMessage) => {
  if (newMessage) {
    visible.value = true;
  }
});

const closeBox = () => {
  visible.value = false;
};
</script>

<style scoped>
.error-box {
  position: absolute;
  background-color: #f8d7da;
  color: #721c24;
  border: 1px solid #f5c6cb;
  border-radius: 5px;
  margin-left: 60vw;
  padding: 1%;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

button {
  background: none;
  border: none;
  color: #721c24;
  font-weight: bold;
  cursor: pointer;
}
</style>
