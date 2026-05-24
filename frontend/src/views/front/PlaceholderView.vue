<script setup>
defineProps({
  title: {
    type: String,
    required: true,
  },
  description: {
    type: String,
    required: true,
  },
})
</script>

<template>
  <section class="placeholder-page">
    <h1 class="placeholder-page__title">{{ title }}</h1>
    <p class="placeholder-page__description">{{ description }}</p>
  </section>
</template>
