<script setup>
import { Icon } from '@iconify/vue'

const model = defineModel({ type: Object, required: true })

defineProps({
  danhMucOptions: { type: Array, default: () => [] },
  thuongHieuOptions: { type: Array, default: () => [] },
  spfOptions: { type: Array, default: () => [] },
  paOptions: { type: Array, default: () => [] },
})

const emit = defineEmits(['reset'])
</script>

<template>
  <div class="soleil-toolbar soleil-toolbar--filter">
    <div class="soleil-toolbar__field soleil-toolbar__field--wide">
      <label class="soleil-toolbar__label">Tìm kiếm</label>
      <div class="soleil-toolbar__search">
        <Icon icon="icon-park-outline:search" class="soleil-toolbar__search-icon" />
        <input
          v-model="model.keyword"
          class="soleil-toolbar__input"
          type="text"
          placeholder="Tên sản phẩm, mã sản phẩm..."
        />
      </div>
    </div>

    <div class="soleil-toolbar__field">
      <label class="soleil-toolbar__label">Danh mục</label>
      <select v-model="model.idDanhMuc" class="soleil-toolbar__select">
        <option :value="null">Tất cả</option>
        <option v-for="item in danhMucOptions" :key="item.id" :value="item.id">
          {{ item.ten }}
        </option>
      </select>
    </div>

    <div class="soleil-toolbar__field">
      <label class="soleil-toolbar__label">Thương hiệu</label>
      <select v-model="model.idThuongHieu" class="soleil-toolbar__select">
        <option :value="null">Tất cả</option>
        <option v-for="item in thuongHieuOptions" :key="item.id" :value="item.id">
          {{ item.ten }}
        </option>
      </select>
    </div>

    <div class="soleil-toolbar__field">
      <label class="soleil-toolbar__label">SPF</label>
      <select v-model="model.chiSoSpf" class="soleil-toolbar__select">
        <option value="">Tất cả</option>
        <option v-for="spf in spfOptions" :key="spf" :value="spf">{{ spf }}</option>
      </select>
    </div>

    <div class="soleil-toolbar__field">
      <label class="soleil-toolbar__label">PA</label>
      <select v-model="model.chiSoPa" class="soleil-toolbar__select">
        <option value="">Tất cả</option>
        <option v-for="pa in paOptions" :key="pa" :value="pa">{{ pa }}</option>
      </select>
    </div>

    <div class="soleil-toolbar__field">
      <label class="soleil-toolbar__label">Trạng thái</label>
      <select v-model="model.trangThai" class="soleil-toolbar__select">
        <option :value="null">Tất cả</option>
        <option :value="true">Đang hiển thị</option>
        <option :value="false">Ngưng hoạt động</option>
      </select>
    </div>

    <button type="button" class="soleil-btn-outline" style="align-self: flex-end" @click="emit('reset')">
      <Icon icon="icon-park-outline:refresh" />
      Làm mới
    </button>
  </div>
</template>

<style scoped>
.soleil-toolbar__field--wide {
    flex: 1;
}

.soleil-toolbar__field:not(.soleil-toolbar__field--wide) {
    flex: 0 0 140px;
}
</style>
