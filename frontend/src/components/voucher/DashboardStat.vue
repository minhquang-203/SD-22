<script setup>
import { ref, computed, onMounted, watch } from 'vue';
import VoucherStats from './VoucherStats.vue';
import { getVoucherStats } from '@/api/voucherApi.js';

const props = defineProps({
  refreshKey: {
    type: Number,
    default: 0,
  },
});

const loading = ref(false);
const rawStats = ref({
  active: 0,
  usageCount: 0,
  totalSavings: 0,
  expiringSoon: 0,
});

function formatCount(num) {
  if (num >= 1_000_000) return `${(num / 1_000_000).toFixed(1)}M`;
  if (num >= 1_000) return `${(num / 1_000).toFixed(1)}k`;
  return String(num);
}

function formatCurrency(num) {
  const value = Number(num) || 0;
  if (value >= 1_000_000_000) return `₫${(value / 1_000_000_000).toFixed(1)}B`;
  if (value >= 1_000_000) return `₫${(value / 1_000_000).toFixed(0)}M`;
  if (value >= 1_000) return `₫${(value / 1_000).toFixed(0)}k`;
  return `₫${value.toLocaleString('vi-VN')}`;
}

const stats = computed(() => [
  {
    title: 'Đang hoạt động',
    value: loading.value ? '...' : rawStats.value.active,
    change: 'Đang áp dụng',
    trend: 'neutral',
    icon: 'mdi:ticket-percent',
    iconClass: 'ic-primary',
  },
  {
    title: 'Lượt sử dụng',
    value: loading.value ? '...' : formatCount(rawStats.value.usageCount),
    change: 'Từ đơn hàng',
    trend: 'neutral',
    icon: 'mdi:check-circle-outline',
    iconClass: 'ic-success',
  },
  {
    title: 'Tiết kiệm',
    value: loading.value ? '...' : formatCurrency(rawStats.value.totalSavings),
    change: 'Tổng giảm giá',
    trend: 'neutral',
    icon: 'mdi:cash-minus',
    iconClass: 'ic-warning',
  },
  {
    title: 'Sắp hết hạn',
    value: loading.value ? '...' : rawStats.value.expiringSoon,
    change: 'Trong 7 ngày',
    trend: 'neutral',
    icon: 'mdi:clock-outline',
    iconClass: 'ic-info',
  },
]);

async function loadStats() {
  loading.value = true;
  try {
    const res = await getVoucherStats();
    rawStats.value = {
      active: res.data?.active ?? 0,
      usageCount: res.data?.usageCount ?? 0,
      totalSavings: res.data?.totalSavings ?? 0,
      expiringSoon: res.data?.expiringSoon ?? 0,
    };
  } catch (error) {
    console.error(error);
  } finally {
    loading.value = false;
  }
}

onMounted(loadStats);
watch(() => props.refreshKey, loadStats);
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
