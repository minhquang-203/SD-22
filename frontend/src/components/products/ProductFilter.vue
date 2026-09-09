<script setup>
import { Icon } from '@iconify/vue'

const model = defineModel({ type: Object, required: true })

defineProps({
  danhMucOptions: { type: Array, default: () => [] },
  thuongHieuOptions: { type: Array, default: () => [] },
  spfOptions: { type: Array, default: () => [] },
  paOptions: { type: Array, default: () => [] },
  compact: { type: Boolean, default: false },
})

const emit = defineEmits(['reset'])
</script>

<template>
  <div class="soleil-toolbar soleil-toolbar--filter" :class="{ 'pa-filter': compact }">
    <div class="soleil-toolbar__field soleil-toolbar__field--wide">
      <label v-if="!compact" class="soleil-toolbar__label">Tìm kiếm</label>
      <div class="soleil-toolbar__search">
        <Icon icon="icon-park-outline:search" class="soleil-toolbar__search-icon" />
        <input
          v-model="model.keyword"
          class="soleil-toolbar__input"
          type="text"
          :placeholder="compact ? 'Tên SP, mã...' : 'Tên sản phẩm, mã sản phẩm...'"
        />
      </div>
    </div>

    <div
      class="soleil-toolbar__field"
      :class="{ 'pa-field--short pa-field--dm': compact }"
    >
      <label v-if="!compact" class="soleil-toolbar__label">Danh mục</label>
      <select v-model="model.idDanhMuc" class="soleil-toolbar__select">
        <option :value="null">{{ compact ? 'Danh mục' : 'Tất cả' }}</option>
        <option v-for="item in danhMucOptions" :key="item.id" :value="item.id">
          {{ item.ten }}
        </option>
      </select>
    </div>

    <div
      class="soleil-toolbar__field"
      :class="{ 'pa-field--short pa-field--th': compact }"
    >
      <label v-if="!compact" class="soleil-toolbar__label">Thương hiệu</label>
      <select v-model="model.idThuongHieu" class="soleil-toolbar__select">
        <option :value="null">{{ compact ? 'Thương hiệu' : 'Tất cả' }}</option>
        <option v-for="item in thuongHieuOptions" :key="item.id" :value="item.id">
          {{ item.ten }}
        </option>
      </select>
    </div>

    <div class="soleil-toolbar__field">
      <label v-if="!compact" class="soleil-toolbar__label">SPF</label>
      <select v-model="model.chiSoSpf" class="soleil-toolbar__select">
        <option value="">{{ compact ? 'SPF' : 'Tất cả' }}</option>
        <option v-for="spf in spfOptions" :key="spf" :value="spf">{{ spf }}</option>
      </select>
    </div>

    <div class="soleil-toolbar__field">
      <label v-if="!compact" class="soleil-toolbar__label">PA</label>
      <select v-model="model.chiSoPa" class="soleil-toolbar__select">
        <option value="">{{ compact ? 'PA' : 'Tất cả' }}</option>
        <option v-for="pa in paOptions" :key="pa" :value="pa">{{ pa }}</option>
      </select>
    </div>

    <div
      class="soleil-toolbar__field"
      :class="{ 'pa-field--short pa-field--tt': compact }"
    >
      <label v-if="!compact" class="soleil-toolbar__label">Trạng thái</label>
      <select v-model="model.trangThai" class="soleil-toolbar__select">
        <option :value="null">{{ compact ? 'Trạng thái' : 'Tất cả' }}</option>
        <option :value="true">Đang hiển thị</option>
        <option :value="false">Ngưng hoạt động</option>
      </select>
    </div>

    <button type="button" class="soleil-btn-outline" @click="emit('reset')">
      <Icon icon="icon-park-outline:refresh" />
      Làm mới
    </button>
  </div>
</template>
