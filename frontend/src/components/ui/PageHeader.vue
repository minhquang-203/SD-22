<script setup>
defineProps({
  title: { type: String, required: true },
  description: { type: String, default: '' },
})
</script>

<template>
  <div class="soleil-page-header">
    <div class="soleil-page-header__text">
      <h1 class="soleil-page-header__title">{{ title }}</h1>
      <p v-if="description" class="soleil-page-header__desc">{{ description }}</p>
    </div>
    <div v-if="$slots.actions" class="soleil-page-header__actions">
      <slot name="actions" />
    </div>
  </div>
</template>
