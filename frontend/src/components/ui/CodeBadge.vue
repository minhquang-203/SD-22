<script setup>
defineProps({
  code: { type: String, required: true },
})
</script>

<template>
  <span class="soleil-code-badge">{{ code }}</span>
</template>
