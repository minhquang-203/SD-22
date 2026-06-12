<script setup>
import { Icon } from '@iconify/vue';
defineProps({
  title: String,
  value: [String, Number],
  change: String,
  trend: String, // up | down | neutral
  icon: String,
  iconClass: String
})

const badgeClassMap = {
  up: 'badge-up',
  down: 'badge-down',
  neutral: 'badge-neu'
}

const arrowIconMap = {
  up: '↗',
  down: '↘',
  neutral: '⏱'
}
</script>

<template>
  <div class="stat-card">
    <div class="stat-content">
      <div>
        <div class="stat-label">{{ title }}</div>

        <div class="stat-value">
          {{ value }}
        </div>

        <span
          class="stat-badge"
          :class="badgeClassMap[trend]"
        >
          {{ arrowIconMap[trend] }}
          {{ change }}
        </span>
      </div>

      <div
        class="stat-icon"
        :class="iconClass"
      >
        <Icon :icon="icon" />
      </div>
    </div>
  </div>
</template>

<style scoped>
.stat-card {
  background: white;
  border-radius: 16px;
  padding: 20px;
  box-shadow: 0 4px 16px rgba(0,0,0,0.06);
}

.stat-content {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.stat-label {
  font-size: 14px;
  color: #64748b;
}

.stat-value {
  font-size: 32px;
  font-weight: 700;
  margin-top: 8px;
}

.stat-badge {
  margin-top: 12px;
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 4px 10px;
  border-radius: 999px;
  font-size: 13px;
}

.badge-up {
  background: #dcfce7;
  color: #15803d;
}

.badge-down {
  background: #fee2e2;
  color: #dc2626;
}

.badge-neu {
  background: #e2e8f0;
  color: #475569;
}

.stat-icon {
  width: 52px;
  height: 52px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
}

.ic-primary {
  background: #dbeafe;
  color: #2563eb;
}

.ic-success {
  background: #dcfce7;
  color: #16a34a;
}

.ic-warning {
  background: #fef3c7;
  color: #d97706;
}

.ic-info {
  background: #cffafe;
  color: #0891b2;
}
</style>