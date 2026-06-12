<script setup>
import VoucherStats from './VoucherStats.vue';

const stats = [
  {
    title: 'Đang hoạt động',
    value: 24,
    change: '+3',
    trend: 'up',
    icon: 'mdi:ticket-percent', // 👈 đổi
    iconClass: 'ic-primary'
  },
  {
    title: 'Lượt sử dụng',
    value: '1.8k',
    change: '+12.4%',
    trend: 'up',
    icon: 'mdi:check-circle-outline',
    iconClass: 'ic-success'
  },
  {
    title: 'Tiết kiệm',
    value: '47M',
    change: '-2.1%',
    trend: 'down',
    icon: 'mdi:cash-minus',
    iconClass: 'ic-warning'
  },
  {
    title: 'Sắp hết hạn',
    value: 6,
    change: '7 ngày',
    trend: 'neutral',
    icon: 'mdi:clock-outline',
    iconClass: 'ic-info'
  }
]
</script>

<template>
  <div class="stats-grid">
    <VoucherStats
      v-for="item in stats"
      :key="item.title"
      :title="item.title"
      :value="item.value"
      :change="item.change"
      :trend="item.trend"
      :icon="item.icon"
      :icon-class="item.iconClass"
    />
  </div>
</template>

<style scoped>
.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}

@media (max-width: 1200px) {
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .stats-grid {
    grid-template-columns: 1fr;
  }
}
</style>