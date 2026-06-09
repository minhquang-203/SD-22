<script setup>
import { Icon } from '@iconify/vue'

defineProps({
  label: { type: String, required: true },
  variant: {
    type: String,
    default: 'default',
    validator: (v) => ['percent', 'fixed', 'ship', 'default'].includes(v),
  },
  icon: { type: String, default: '' },
})
</script>

<template>
  <span class="soleil-pill-badge" :class="`soleil-pill-badge--${variant}`">
    <Icon v-if="icon" :icon="icon" class="soleil-pill-badge__icon" />
    {{ label }}
  </span>
</template>
