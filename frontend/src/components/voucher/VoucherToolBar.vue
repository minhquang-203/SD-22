<script setup>
import SortDropdown from "@/components/common/SortDropdown.vue";

defineProps({
  search: String,
  status: String,
  type: String,
  sort: { type: String, default: "" },
  sortOptions: { type: Array, default: () => [] },
});

const emit = defineEmits([
  "update:search",
  "update:status",
  "update:type",
  "update:sort",
  "export",
]);
</script>

<template>
  <div class="toolbar">
    <div class="search-wrap">
      <i class="bi bi-search"></i>
      <input
        :value="search"
        class="search-input"
        type="text"
        placeholder="Tìm mã phiếu, tên chương trình..."
        @input="emit('update:search', $event.target.value)"
      />
    </div>

    <select
      :value="status"
      class="filter-select"
      @change="emit('update:status', $event.target.value)"
    >
      <option value="">Tất cả trạng thái</option>
      <option value="active">Đang hoạt động</option>
      <option value="upcoming">Sắp diễn ra</option>
      <option value="expired">Đã hết hạn</option>
      <option value="inactive">Ngừng áp dụng</option>
    </select>

    <select
      :value="type"
      class="filter-select"
      @change="emit('update:type', $event.target.value)"
    >
      <option value="">Tất cả loại</option>
      <option value="PHAN_TRAM">Phần trăm (%)</option>
      <option value="TIEN_MAT">Số tiền cố định</option>
      <option value="FREE_SHIP">Miễn phí ship</option>
    </select>

    <SortDropdown
      class="ms-auto"
      :model-value="sort"
      :options="sortOptions"
      @update:model-value="emit('update:sort', $event)"
    />

    <button class="btn-outline-sol" @click="emit('export')">
      <i class="bi bi-download"></i> Xuất CSV
    </button>
  </div>
</template>
