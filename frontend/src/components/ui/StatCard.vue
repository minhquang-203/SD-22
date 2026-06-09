<script setup>
import { Icon } from '@iconify/vue'

defineProps({
  label: { type: String, required: true },
  value: { type: [String, Number], required: true },
  trend: { type: String, default: '' },
  trendType: {
    type: String,
    default: 'up',
    validator: (v) => ['up', 'down', 'neutral'].includes(v),
  },
  icon: { type: String, default: '' },
  iconTone: {
    type: String,
    default: 'gold',
    validator: (v) => ['gold', 'sage', 'coral', 'sky'].includes(v),
  },
})
</script>

<template>
  <div class="soleil-stat-card">
    <div class="soleil-stat-card__inner">
      <div class="soleil-stat-card__content">
        <div class="soleil-stat-card__label">{{ label }}</div>
        <div class="soleil-stat-card__value">{{ value }}</div>
        <span
          v-if="trend"
          class="soleil-stat-card__trend"
          :class="`soleil-stat-card__trend--${trendType}`"
        >
          <Icon
            v-if="trendType === 'up'"
            icon="icon-park-outline:arrow-up"
            class="soleil-stat-card__trend-icon"
          />
          <Icon
            v-else-if="trendType === 'down'"
            icon="icon-park-outline:arrow-down"
            class="soleil-stat-card__trend-icon"
          />
          <Icon
            v-else
            icon="icon-park-outline:time"
            class="soleil-stat-card__trend-icon"
          />
          {{ trend }}
        </span>
      </div>
      <div
        v-if="icon"
        class="soleil-stat-card__icon"
        :class="`soleil-stat-card__icon--${iconTone}`"
      >
        <Icon :icon="icon" />
      </div>
    </div>
  </div>
</template>
