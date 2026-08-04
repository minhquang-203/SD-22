<script setup>
import { computed, ref, watch } from 'vue'
import ProductSkuTable from './ProductSkuTable.vue'
import ProductImageManager from './ProductImageManager.vue'
import AttributeChipGroup from '@/components/ui/AttributeChipGroup.vue'
import SearchableSelect from '@/components/ui/SearchableSelect.vue'
import { LOAI_DA_OPTIONS } from '@/constants/loaiDa'
import {
  createEmptyProductForm,
  detailToForm,
  PA_OPTIONS,
  sanitizeSpfSuffix,
  validateProductForm,
} from '@/utils/productForm'
import { getMaTiepTheo } from '@/api/sanPhamApi'

function mapSelectOptions(list) {
  return (list || []).map((item) => ({ value: item.id, label: item.ten }))
}

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

const thuongHieuSelectOptions = computed(() => mapSelectOptions(props.thuongHieuOptions))
const danhMucSelectOptions = computed(() => mapSelectOptions(props.danhMucOptions))
const dangSanPhamSelectOptions = computed(() => mapSelectOptions(props.dangSanPhamOptions))

/** Màu từ biến thể đang có + màu đã gán trên ảnh (khi sửa) */
const imageMauOptions = computed(() => {
  const ids = new Set(
    (form.value.chiTiets || [])
      .map((ct) => ct.idMauSac)
      .filter((id) => id != null),
  )
  ;(form.value.anhs || []).forEach((img) => {
    if (img.idMauSac != null) ids.add(img.idMauSac)
  })
  return (props.mauSacOptions || []).filter((m) => ids.has(m.id))
})

const tabs = [
  { key: 'basic', label: 'Thông tin cơ bản' },
  { key: 'attributes', label: 'Thuộc tính' },
  { key: 'sku', label: 'SKU / Giá / Tồn' },
  { key: 'images', label: 'Ảnh sản phẩm' },
]

watch(
  () => [props.open, props.initialDetail, props.mode],
  async () => {
    if (!props.open) return
    activeTab.value = 'basic'
    error.value = ''
    if (props.initialDetail) {
      form.value = detailToForm(props.initialDetail, props.mauSacOptions)
    } else {
      form.value = createEmptyProductForm()
      await loadPreviewMaSanPham()
    }
  },
  { immediate: true },
)

async function loadPreviewMaSanPham() {
  if (props.mode !== 'create') return
  form.value.maSanPham = '...'
  try {
    const res = await getMaTiepTheo()
    form.value.maSanPham = res.data?.ma || ''
  } catch {
    form.value.maSanPham = ''
  }
}

function onSpfInput(event) {
  form.value.chiSoSpf = sanitizeSpfSuffix(event.target.value)
}

function handleSubmit() {
  form.value.chiSoSpf = sanitizeSpfSuffix(form.value.chiSoSpf)
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
  <div v-if="open" class="modal-overlay" @click.self="emit('close')">
    <div class="modal-panel relative w-full max-w-5xl max-h-[92vh] overflow-hidden admin-card flex flex-col">
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
            <label class="admin-label">Mã sản phẩm</label>
            <input
              v-model="form.maSanPham"
              class="admin-input bg-[var(--mist)] text-[var(--admin-muted)] cursor-not-allowed font-mono"
              readonly
              placeholder="Đang tải mã..."
            />
            <p v-if="mode === 'create'" class="text-xs text-[var(--admin-muted)] mt-1">
              Mã dự kiến — backend tự sinh khi lưu
            </p>
          </div>
          <div>
            <label class="admin-label">Tên sản phẩm *</label>
            <input v-model="form.ten" class="admin-input" placeholder="Tên sản phẩm chống nắng" />
          </div>
          <div>
            <label class="admin-label">Thương hiệu *</label>
            <SearchableSelect
              v-model="form.idThuongHieu"
              :options="thuongHieuSelectOptions"
              placeholder="Chọn thương hiệu"
            />
          </div>
          <div>
            <label class="admin-label">Danh mục *</label>
            <SearchableSelect
              v-model="form.idDanhMuc"
              :options="danhMucSelectOptions"
              placeholder="Chọn danh mục"
            />
          </div>
          <div>
            <label class="admin-label">Dạng sản phẩm *</label>
            <SearchableSelect
              v-model="form.idDangSanPham"
              :options="dangSanPhamSelectOptions"
              placeholder="Chọn dạng"
            />
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
            <div class="spf-input-group">
              <span class="spf-input-group__prefix" aria-hidden="true">SPF</span>
              <input
                :value="form.chiSoSpf"
                class="admin-input spf-input-group__field"
                placeholder="VD: 50+ hoặc 30"
                inputmode="text"
                autocomplete="off"
                @input="onSpfInput"
              />
            </div>
          </div>
          <div>
            <label class="admin-label">Chỉ số PA</label>
            <select v-model="form.chiSoPa" class="admin-select">
              <option value="">— Chọn PA —</option>
              <option v-for="opt in PA_OPTIONS" :key="opt" :value="opt">{{ opt }}</option>
            </select>
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

        <div v-show="activeTab === 'attributes'" class="space-y-6">
          <AttributeChipGroup
            v-model="form.idLoaiDas"
            title="Loại da phù hợp"
            :options="LOAI_DA_OPTIONS"
          />
          <AttributeChipGroup
            v-model="form.idCongDungs"
            title="Công dụng"
            :options="congDungOptions"
          />
          <AttributeChipGroup
            v-model="form.idThanhPhans"
            title="Thành phần"
            :options="thanhPhanOptions"
          />
        </div>

        <div v-show="activeTab === 'sku'">
          <ProductSkuTable
            v-model="form.chiTiets"
            :mau-sac-options="mauSacOptions"
            :ma-san-pham="form.maSanPham"
          />
        </div>

        <div v-show="activeTab === 'images'">
          <ProductImageManager v-model="form.anhs" :mau-options="imageMauOptions" />
          <p
            v-if="!imageMauOptions.length"
            class="text-xs text-[var(--admin-muted)] mt-2"
          >
            Chưa có màu ở tab SKU — ảnh mặc định «Dùng chung». Thêm biến thể + màu để gán ảnh riêng.
          </p>
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

<style scoped>
.spf-input-group {
  display: flex;
  align-items: stretch;
  width: 100%;
  border: 1px solid var(--admin-border);
  border-radius: 8px;
  overflow: hidden;
  background: #fff;
  transition: border-color 0.2s, box-shadow 0.2s;
}
.spf-input-group:focus-within {
  border-color: var(--admin-primary);
  box-shadow: 0 0 0 2px rgba(201, 169, 110, 0.12);
}
.spf-input-group__prefix {
  display: inline-flex;
  align-items: center;
  padding: 0 12px;
  background: rgba(201, 169, 110, 0.12);
  color: var(--ink, #1e1510);
  font-weight: 600;
  font-size: 14px;
  letter-spacing: 0.02em;
  border-right: 1px solid var(--admin-border);
  user-select: none;
  flex-shrink: 0;
}
.spf-input-group__field {
  border: none !important;
  border-radius: 0 !important;
  box-shadow: none !important;
  flex: 1;
  min-width: 0;
}
</style>
