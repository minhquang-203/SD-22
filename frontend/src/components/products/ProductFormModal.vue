<script setup>
import { computed, ref, watch } from 'vue'
import ProductSkuTable from './ProductSkuTable.vue'
import ProductImageManager from './ProductImageManager.vue'
import { LOAI_DA_OPTIONS } from '@/constants/loaiDa'
import {
  createEmptyProductForm,
  detailToForm,
  validateProductForm,
} from '@/utils/productForm'

const props = defineProps({
  open: { type: Boolean, default: false },
  mode: { type: String, default: 'create' },
  loading: { type: Boolean, default: false },
  productId: { type: Number, default: null },
  initialDetail: { type: Object, default: null },
  danhMucOptions: { type: Array, default: () => [] },
  thuongHieuOptions: { type: Array, default: () => [] },
  dangSanPhamOptions: { type: Array, default: () => [] },
  congDungOptions: { type: Array, default: () => [] },
  thanhPhanOptions: { type: Array, default: () => [] },
  mauSacOptions: { type: Array, default: () => [] },
})

const emit = defineEmits(['close', 'submit'])

const activeTab = ref('basic')
const form = ref(createEmptyProductForm())
const error = ref('')

const title = computed(() =>
  props.mode === 'create' ? 'Thêm sản phẩm mới' : 'Cập nhật sản phẩm',
)

const tabs = [
  { key: 'basic', label: 'Thông tin cơ bản' },
  { key: 'attributes', label: 'Thuộc tính' },
  { key: 'sku', label: 'SKU / Giá / Tồn' },
  { key: 'images', label: 'Ảnh sản phẩm' },
]

watch(
  () => [props.open, props.initialDetail, props.mode],
  () => {
    if (!props.open) return
    activeTab.value = 'basic'
    error.value = ''
    if (props.initialDetail) {
      form.value = detailToForm(props.initialDetail, props.mauSacOptions)
    } else {
      form.value = createEmptyProductForm()
    }
  },
  { immediate: true },
)

function toggleMultiSelect(field, id) {
  const list = form.value[field] || []
  const index = list.indexOf(id)
  if (index >= 0) {
    list.splice(index, 1)
  } else {
    list.push(id)
  }
  form.value[field] = [...list]
}

function isSelected(field, id) {
  return (form.value[field] || []).includes(id)
}

function handleSubmit() {
  const validationError = validateProductForm(form.value)
  if (validationError) {
    error.value = validationError
    return
  }
  error.value = ''
  emit('submit', form.value)
}
</script>

<template>
  <div v-if="open" class="fixed inset-0 z-50 flex items-center justify-center p-4">
    <div class="absolute inset-0 bg-black/45" @click="emit('close')" />

    <div class="relative w-full max-w-5xl max-h-[92vh] overflow-hidden admin-card flex flex-col">
      <div class="px-5 py-4 border-b flex items-center justify-between" style="border-color: var(--admin-border)">
        <div>
          <h2 class="text-lg font-semibold">{{ title }}</h2>
          <p class="text-sm text-[var(--admin-muted)]">
            Điền thông tin sản phẩm chống nắng
          </p>
        </div>
        <button type="button" class="admin-btn admin-btn-default !px-2.5" @click="emit('close')">
          ✕
        </button>
      </div>

      <div class="px-5 pt-4 border-b" style="border-color: var(--admin-border)">
        <div class="flex gap-2 overflow-x-auto pb-3">
          <button
            v-for="tab in tabs"
            :key="tab.key"
            type="button"
            class="admin-btn whitespace-nowrap"
            :class="activeTab === tab.key ? 'admin-btn-primary' : 'admin-btn-default'"
            @click="activeTab = tab.key"
          >
            {{ tab.label }}
          </button>
        </div>
      </div>

      <div class="flex-1 overflow-y-auto p-5 space-y-4">
        <div v-if="error" class="admin-alert admin-alert-error rounded-lg px-4 py-3 text-sm">
          {{ error }}
        </div>

        <div v-show="activeTab === 'basic'" class="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div>
            <label class="admin-label">Mã sản phẩm *</label>
            <input v-model="form.maSanPham" class="admin-input" placeholder="VD: SP001" />
          </div>
          <div>
            <label class="admin-label">Tên sản phẩm *</label>
            <input v-model="form.ten" class="admin-input" placeholder="Tên sản phẩm chống nắng" />
          </div>
          <div>
            <label class="admin-label">Thương hiệu *</label>
            <select v-model="form.idThuongHieu" class="admin-select">
              <option :value="null">Chọn thương hiệu</option>
              <option v-for="item in thuongHieuOptions" :key="item.id" :value="item.id">
                {{ item.ten }}
              </option>
            </select>
          </div>
          <div>
            <label class="admin-label">Danh mục *</label>
            <select v-model="form.idDanhMuc" class="admin-select">
              <option :value="null">Chọn danh mục</option>
              <option v-for="item in danhMucOptions" :key="item.id" :value="item.id">
                {{ item.ten }}
              </option>
            </select>
          </div>
          <div>
            <label class="admin-label">Dạng sản phẩm *</label>
            <select v-model="form.idDangSanPham" class="admin-select">
              <option :value="null">Chọn dạng</option>
              <option v-for="item in dangSanPhamOptions" :key="item.id" :value="item.id">
                {{ item.ten }}
              </option>
            </select>
          </div>
          <div>
            <label class="admin-label">Loại chống nắng</label>
            <select v-model="form.loaiChongNang" class="admin-select">
              <option value="">— Chọn loại —</option>
              <option value="VAT_LY">Vật lý</option>
              <option value="HOA_HOC">Hóa học</option>
              <option value="LAI">Vật lý và Hóa học</option>
            </select>
          </div>
          <div>
            <label class="admin-label">Chỉ số SPF</label>
            <input v-model="form.chiSoSpf" class="admin-input" placeholder="VD: SPF50+" />
          </div>
          <div>
            <label class="admin-label">Chỉ số PA</label>
            <input v-model="form.chiSoPa" class="admin-input" placeholder="VD: PA++++" />
          </div>
          <div class="md:col-span-2">
            <label class="flex items-center gap-2 text-sm">
              <input v-model="form.khangNuoc" type="checkbox" />
              Kháng nước
            </label>
          </div>
          <div class="md:col-span-2">
            <label class="admin-label">Mô tả</label>
            <textarea v-model="form.moTa" class="admin-input min-h-[120px]" placeholder="Mô tả sản phẩm..." />
          </div>
        </div>

        <div v-show="activeTab === 'attributes'" class="space-y-5">
          <div>
            <h4 class="font-semibold text-sm mb-2">Loại da phù hợp</h4>
            <div class="flex flex-wrap gap-2">
              <button
                v-for="item in LOAI_DA_OPTIONS"
                :key="item.id"
                type="button"
                class="admin-btn"
                :class="isSelected('idLoaiDas', item.id) ? 'admin-btn-primary' : 'admin-btn-default'"
                @click="toggleMultiSelect('idLoaiDas', item.id)"
              >
                {{ item.ten }}
              </button>
            </div>
          </div>

          <div>
            <h4 class="font-semibold text-sm mb-2">Công dụng</h4>
            <div class="flex flex-wrap gap-2">
              <button
                v-for="item in congDungOptions"
                :key="item.id"
                type="button"
                class="admin-btn"
                :class="isSelected('idCongDungs', item.id) ? 'admin-btn-primary' : 'admin-btn-default'"
                @click="toggleMultiSelect('idCongDungs', item.id)"
              >
                {{ item.ten }}
              </button>
            </div>
          </div>

          <div>
            <h4 class="font-semibold text-sm mb-2">Thành phần</h4>
            <div class="flex flex-wrap gap-2">
              <button
                v-for="item in thanhPhanOptions"
                :key="item.id"
                type="button"
                class="admin-btn"
                :class="isSelected('idThanhPhans', item.id) ? 'admin-btn-primary' : 'admin-btn-default'"
                @click="toggleMultiSelect('idThanhPhans', item.id)"
              >
                {{ item.ten }}
              </button>
            </div>
          </div>
        </div>

        <div v-show="activeTab === 'sku'">
          <ProductSkuTable
            v-model="form.chiTiets"
            :mau-sac-options="mauSacOptions"
            :ma-san-pham="form.maSanPham"
          />
        </div>

        <div v-show="activeTab === 'images'">
          <ProductImageManager v-model="form.anhs" />
        </div>
      </div>

      <div
        class="px-5 py-4 border-t flex items-center justify-end gap-3"
        style="border-color: var(--admin-border)"
      >
        <button type="button" class="admin-btn admin-btn-default" :disabled="loading" @click="emit('close')">
          Hủy
        </button>
        <button type="button" class="admin-btn admin-btn-primary" :disabled="loading" @click="handleSubmit">
          {{ loading ? 'Đang lưu...' : mode === 'create' ? 'Thêm sản phẩm' : 'Cập nhật' }}
        </button>
      </div>
    </div>
  </div>
</template>
