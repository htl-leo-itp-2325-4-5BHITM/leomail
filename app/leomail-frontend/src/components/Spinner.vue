<template>
  <div class="spinner"></div>
</template>

<script setup lang="ts"></script>

<style scoped>
.spinner {
  border: 8px solid #f3f3f3;
  border-top: 8px solid #0086d4;
  border-radius: 50%;
  width: 60px;
  height: 60px;
  animation: spin 1s linear infinite;
  margin-bottom: 1em;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}
</style>