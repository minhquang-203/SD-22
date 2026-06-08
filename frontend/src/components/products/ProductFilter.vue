<script setup>
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
  <div class="admin-card admin-card--rounded">
    <div class="admin-card-header">
      <h3>Bộ lọc tìm kiếm</h3>
      <button type="button" class="admin-icon-btn admin-icon-btn--primary" title="Làm mới bộ lọc" @click="emit('reset')">
        ↺
      </button>
    </div>
    <div class="p-4 md:p-5">

    <div class="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-4 gap-4">
      <div class="md:col-span-2">
        <label class="admin-label">Tìm kiếm</label>
        <input
          v-model="model.keyword"
          class="admin-input"
          placeholder="Tên sản phẩm, mã sản phẩm..."
        />
      </div>

      <div>
        <label class="admin-label">Danh mục</label>
        <select v-model="model.idDanhMuc" class="admin-select">
          <option :value="null">Tất cả</option>
          <option v-for="item in danhMucOptions" :key="item.id" :value="item.id">
            {{ item.ten }}
          </option>
        </select>
      </div>

      <div>
        <label class="admin-label">Thương hiệu</label>
        <select v-model="model.idThuongHieu" class="admin-select">
          <option :value="null">Tất cả</option>
          <option v-for="item in thuongHieuOptions" :key="item.id" :value="item.id">
            {{ item.ten }}
          </option>
        </select>
      </div>

      <div>
        <label class="admin-label">SPF</label>
        <select v-model="model.chiSoSpf" class="admin-select">
          <option value="">Tất cả</option>
          <option v-for="spf in spfOptions" :key="spf" :value="spf">{{ spf }}</option>
        </select>
      </div>

      <div>
        <label class="admin-label">PA</label>
        <select v-model="model.chiSoPa" class="admin-select">
          <option value="">Tất cả</option>
          <option v-for="pa in paOptions" :key="pa" :value="pa">{{ pa }}</option>
        </select>
      </div>

      <div>
        <label class="admin-label">Trạng thái</label>
        <select v-model="model.trangThai" class="admin-select">
          <option :value="null">Tất cả</option>
          <option :value="true">Đang hiển thị</option>
          <option :value="false">Ngưng hoạt động</option>
        </select>
      </div>
    </div>
    </div>
  </div>
</template>
