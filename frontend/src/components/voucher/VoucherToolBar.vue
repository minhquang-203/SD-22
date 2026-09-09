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
  <section class="toolbar">
    <div class="search-wrap">
      <svg width="15" height="15" viewBox="0 0 15 15" fill="none" aria-hidden="true">
        <circle cx="6.5" cy="6.5" r="4.7" stroke="currentColor" stroke-width="1.4" />
        <line x1="10.1" y1="10.1" x2="13.3" y2="13.3" stroke="currentColor" stroke-width="1.4" stroke-linecap="round" />
      </svg>
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
      <option value="PHAN_TRAM">Phần trăm</option>
      <option value="TIEN_MAT">Số tiền</option>
      <option value="FREE_SHIP">Miễn phí ship</option>
    </select>

    <SortDropdown
      :model-value="sort"
      :options="sortOptions"
      label="Sắp xếp: mặc định"
      @update:model-value="emit('update:sort', $event)"
    />

    <button type="button" class="btn-outline-sol" @click="emit('export')">
      <svg width="14" height="14" viewBox="0 0 14 14" fill="none" aria-hidden="true">
        <path d="M7 1.5v7.4M4 6.3L7 9.3l3-3" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" />
        <path d="M1.7 11v.9c0 .5.4.9.9.9h8.8c.5 0 .9-.4.9-.9V11" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" />
      </svg>
      Xuất CSV
    </button>
  </section>
</template>
