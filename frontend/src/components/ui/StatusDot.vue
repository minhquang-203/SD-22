<script setup>
defineProps({
  status: {
    type: String,
    default: 'active',
    validator: (v) => ['active', 'expired', 'upcoming', 'paused'].includes(v),
  },
  label: { type: String, required: true },
})
</script>

<template>
  <span class="soleil-status-dot" :class="`soleil-status-dot--${status}`">
    {{ label }}
  </span>
</template>
