<script setup>
import AppHeader from './components/AppHeader.vue'
</script>

<template>
  <div class="app-shell">
    <AppHeader />

    <main class="page-shell">
      <RouterView />
    </main>
  </div>
</template>
