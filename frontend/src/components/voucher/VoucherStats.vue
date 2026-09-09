<script setup>
defineProps({
  title: String,
  value: [String, Number],
  change: String,
  accent: {
    type: String,
    default: 'c-blue',
  },
});
</script>

<template>
  <div class="stat-card" :class="accent">
    <div class="stat-label">{{ title }}</div>
    <div class="stat-value">{{ value }}</div>
    <div class="stat-foot">
      <span class="dot"></span>
      {{ change }}
    </div>
  </div>
</template>
