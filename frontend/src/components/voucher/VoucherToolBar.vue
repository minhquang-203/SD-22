<script setup>
defineProps({
  search: String,
  status: String,
  type: String,
});

const emit = defineEmits([
  "update:search",
  "update:status",
  "update:type",
  "sort",
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

    <button class="btn-outline-sol ms-auto" @click="emit('sort')">
      <i class="bi bi-arrow-down-up"></i> Sắp xếp
    </button>

    <button class="btn-outline-sol" @click="emit('export')">
      <i class="bi bi-download"></i> Xuất CSV
    </button>
  </div>
</template>
