<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import AdminSwitch from '@/components/admin/AdminSwitch.vue'
import VariantModal from '@/components/products/VariantModal.vue'
import { getMauSacList } from '@/api/danhMucApi'
import { getProductDetail, getProducts } from '@/api/sanPhamApi'
import { addChiTiet, hideChiTiet, updateChiTiet } from '@/api/sanPhamApi'
import { formatCurrency } from '@/utils/format'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const saving = ref(false)
const message = ref('')
const messageType = ref('success')

const products = ref([])
const mauSacOptions = ref([])
const selectedProductId = ref(null)
const productDetail = ref(null)

const filter = reactive({
  keyword: '',
  idMauSac: null,
  dungTichMl: null,
  priceMin: null,
  priceMax: null,
})

const page = ref(1)
const pageSize = ref(10)

const modalOpen = ref(false)
const modalMode = ref('add')
const editingVariant = ref(null)

const selectedProduct = computed(() =>
  products.value.find((p) => p.id === selectedProductId.value),
)

const variants = computed(() => {
  const list = productDetail.value?.chiTiets || []
  return list.map((ct) => {
    const mau = mauSacOptions.value.find((m) => m.ten === ct.tenMauSac)
    return { ...ct, idMauSac: mau?.id ?? null, maHex: mau?.maHex }
  })
})

const dungTichOptions = computed(() => {
  const values = variants.value
    .map((v) => v.dungTichMl)
    .filter((v) => v != null && v !== '')
    .map((v) => Number(v))
  return [...new Set(values)].sort((a, b) => a - b)
})

const filteredVariants = computed(() => {
  let rows = [...variants.value]
  const kw = filter.keyword.trim().toLowerCase()
  if (kw) {
    rows = rows.filter((r) =>
      [r.sku, r.tenMauSac, r.dungTichMl != null ? `${r.dungTichMl} ml` : '']
        .some((v) => String(v || '').toLowerCase().includes(kw)),
    )
  }
  if (filter.idMauSac) {
    rows = rows.filter((r) => r.idMauSac === filter.idMauSac)
  }
  if (filter.dungTichMl != null && filter.dungTichMl !== '') {
    rows = rows.filter((r) => Number(r.dungTichMl) === Number(filter.dungTichMl))
  }
  if (filter.priceMin != null && filter.priceMin !== '') {
    rows = rows.filter((r) => Number(r.giaBan) >= Number(filter.priceMin))
  }
  if (filter.priceMax != null && filter.priceMax !== '') {
    rows = rows.filter((r) => Number(r.giaBan) <= Number(filter.priceMax))
  }
  return rows
})

const totalPages = computed(() => Math.max(1, Math.ceil(filteredVariants.value.length / pageSize.value)))

const pagedVariants = computed(() => {
  const start = (page.value - 1) * pageSize.value
  return filteredVariants.value.slice(start, start + pageSize.value)
})

function notify(text, type = 'success') {
  message.value = text
  messageType.value = type
  setTimeout(() => { message.value = '' }, 3000)
}

function applyProductIdFromQuery() {
  const raw = route.query.productId
  if (raw == null || raw === '') return false
  const id = Number(raw)
  if (Number.isNaN(id)) return false
  if (products.value.some((p) => p.id === id)) {
    selectedProductId.value = id
    return true
  }
  return false
}

async function loadProducts() {
  const res = await getProducts()
  products.value = res.data || []
  if (!applyProductIdFromQuery() && !selectedProductId.value && products.value.length) {
    selectedProductId.value = products.value[0].id
  }
}

function syncProductIdToUrl(id) {
  const queryId = route.query.productId != null ? Number(route.query.productId) : null
  if (id && id !== queryId) {
    router.replace({ path: route.path, query: { productId: id } })
  } else if (!id && route.query.productId != null) {
    router.replace({ path: route.path, query: {} })
  }
}

async function loadMauSac() {
  const res = await getMauSacList()
  mauSacOptions.value = (res.data || []).filter((m) => m.trangThai !== false)
}

async function loadProductDetail() {
  if (!selectedProductId.value) {
    productDetail.value = null
    return
  }
  loading.value = true
  try {
    const res = await getProductDetail(selectedProductId.value)
    productDetail.value = res.data
    page.value = 1
  } catch (err) {
    notify(String(err), 'error')
  } finally {
    loading.value = false
  }
}

function resetFilter() {
  filter.keyword = ''
  filter.idMauSac = null
  filter.dungTichMl = null
  filter.priceMin = null
  filter.priceMax = null
}

function openAdd() {
  if (!selectedProductId.value) return notify('Vui lòng chọn sản phẩm', 'error')
  modalMode.value = 'add'
  editingVariant.value = null
  modalOpen.value = true
}

function openEdit(row) {
  modalMode.value = 'edit'
  editingVariant.value = row
  modalOpen.value = true
}

async function handleSubmit(payload) {
  saving.value = true
  try {
    if (modalMode.value === 'add') {
      await addChiTiet(payload)
      notify('Thêm biến thể thành công')
    } else {
      await updateChiTiet(editingVariant.value.id, payload)
      notify('Cập nhật biến thể thành công')
    }
    modalOpen.value = false
    await loadProductDetail()
  } catch (err) {
    notify(String(err), 'error')
  } finally {
    saving.value = false
  }
}

async function handleToggleStatus(row) {
  const isActive = row.trangThai !== false
  if (!confirm(`Bạn có chắc muốn ${isActive ? 'ngưng' : 'kích hoạt'} biến thể "${row.sku}"?`)) return
  try {
    if (isActive) {
      await hideChiTiet(row.id)
      notify('Đã chuyển biến thể sang ngưng hoạt động')
    } else {
      await updateChiTiet(row.id, {
        idSanPham: selectedProductId.value,
        sku: row.sku,
        idMauSac: row.idMauSac,
        dungTichMl: row.dungTichMl,
        giaBan: row.giaBan,
        soLuongTon: row.soLuongTon,
        hanSuDung: row.hanSuDung,
        trangThai: true,
      })
      notify('Đã kích hoạt lại biến thể')
    }
    await loadProductDetail()
  } catch (err) {
    notify(String(err), 'error')
  }
}

watch(selectedProductId, (id) => {
  filter.dungTichMl = null
  syncProductIdToUrl(id)
  loadProductDetail()
})

watch(
  () => route.query.productId,
  (raw) => {
    if (raw == null || raw === '') return
    const id = Number(raw)
    if (!Number.isNaN(id) && id !== selectedProductId.value) {
      selectedProductId.value = id
    }
  },
)

onMounted(async () => {
  try {
    await Promise.all([loadProducts(), loadMauSac()])
    await loadProductDetail()
  } catch (err) {
    notify(String(err), 'error')
  }
})
</script>

<template>
  <div class="space-y-4">
    <div class="admin-page-header">
      <div class="admin-page-header__icon">📦</div>
      <div class="flex-1 min-w-0">
        <h1 class="admin-page-title">
          Quản lý biến thể sản phẩm
          <span v-if="selectedProduct" class="text-[var(--admin-primary)]">
            {{ selectedProduct.maSanPham }} - {{ selectedProduct.ten }}
          </span>
        </h1>
        <p class="admin-page-subtitle">Quản lý SKU, giá bán, tồn kho theo từng biến thể (`chi_tiet_san_pham`)</p>
      </div>
      <select v-model="selectedProductId" class="admin-select !w-full md:!w-[320px] shrink-0">
        <option :value="null">Chọn sản phẩm</option>
        <option v-for="p in products" :key="p.id" :value="p.id">
          {{ p.maSanPham }} - {{ p.ten }}
        </option>
      </select>
    </div>

    <div
      v-if="message"
      class="admin-alert rounded-xl px-4 py-3 text-sm"
      :class="messageType === 'error' ? 'admin-alert-error' : 'admin-alert-success'"
    >
      {{ message }}
    </div>

    <div class="admin-card admin-card--rounded">
      <div class="admin-card-header">
        <h3>Bộ lọc</h3>
        <button type="button" class="admin-icon-btn admin-icon-btn--success" title="Làm mới" @click="resetFilter">↺</button>
      </div>
      <div class="p-4 md:p-5 grid grid-cols-1 md:grid-cols-2 xl:grid-cols-4 gap-4">
        <div class="md:col-span-2">
          <label class="admin-label">Tìm kiếm</label>
          <input v-model="filter.keyword" class="admin-input" placeholder="SKU, màu sắc..." />
        </div>
        <div>
          <label class="admin-label">Màu sắc</label>
          <select v-model="filter.idMauSac" class="admin-select">
            <option :value="null">Tất cả</option>
            <option v-for="m in mauSacOptions" :key="m.id" :value="m.id">{{ m.ten }}</option>
          </select>
        </div>
        <div>
          <label class="admin-label">Dung tích</label>
          <select v-model="filter.dungTichMl" class="admin-select">
            <option :value="null">Tất cả</option>
            <option v-for="dt in dungTichOptions" :key="dt" :value="dt">{{ dt }} ml</option>
          </select>
        </div>
        <div>
          <label class="admin-label">Giá từ</label>
          <input v-model.number="filter.priceMin" type="number" class="admin-input" min="0" step="1000" />
        </div>
        <div>
          <label class="admin-label">Giá đến</label>
          <input v-model.number="filter.priceMax" type="number" class="admin-input" min="0" step="1000" />
        </div>
      </div>
    </div>

    <div class="admin-card admin-card--rounded">
      <div class="admin-card-header flex-col items-stretch sm:flex-row sm:items-center gap-2">
        <div>
          <h3>Danh sách biến thể</h3>
          <p class="text-xs text-[var(--admin-muted)] mt-1 mb-0">
            Mỗi biến thể là một phiên bản bán ra (vd: 60ml, 90ml). Mỗi biến thể có SKU, giá, tồn kho riêng.
          </p>
        </div>
        <div class="flex gap-2 shrink-0">
          <button type="button" class="admin-fab-btn admin-fab-btn--primary group" @click="openAdd">
            <span>＋</span>
            <span class="admin-fab-btn__label">Thêm biến thể</span>
          </button>
          <button type="button" class="admin-fab-btn admin-fab-btn--info group" @click="loadProductDetail">
            <span>⟳</span>
            <span class="admin-fab-btn__label">Tải lại</span>
          </button>
        </div>
      </div>

      <div class="overflow-x-auto">
        <table class="admin-table admin-table--striped">
          <thead>
            <tr>
              <th class="w-12 text-center">#</th>
              <th>SKU</th>
              <th>Màu sắc</th>
              <th>Dung tích</th>
              <th>Giá bán</th>
              <th>Tồn kho</th>
              <th>Hạn dùng</th>
              <th class="text-center">Trạng thái</th>
              <th class="text-center">Thao tác</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="loading">
              <td colspan="9" class="text-center py-10 text-gray-400">Đang tải...</td>
            </tr>
            <tr v-else-if="!selectedProductId">
              <td colspan="9" class="text-center py-10 text-[var(--admin-muted)] px-6">
                Hãy chọn một sản phẩm ở ô trên để xem và thêm các biến thể (dung tích, màu, giá) của sản phẩm đó.
              </td>
            </tr>
            <tr v-else-if="pagedVariants.length === 0">
              <td colspan="9" class="text-center py-10 text-[var(--admin-muted)] px-6">
                Sản phẩm này chưa có biến thể nào. Nhấn <strong>+ Thêm biến thể</strong> để tạo phiên bản bán (vd: 60ml, 90ml) — mỗi biến thể cần SKU, giá và tồn kho riêng.
              </td>
            </tr>
            <tr v-for="(row, index) in pagedVariants" :key="row.id">
              <td class="text-center">{{ (page - 1) * pageSize + index + 1 }}</td>
              <td><span class="font-semibold text-[var(--admin-primary)]">{{ row.sku }}</span></td>
              <td>
                <div class="flex items-center gap-2">
                  <span
                    v-if="row.maHex"
                    class="w-5 h-5 rounded border shrink-0"
                    :style="{ backgroundColor: row.maHex }"
                  />
                  <span>{{ row.tenMauSac || '—' }}</span>
                </div>
              </td>
              <td>{{ row.dungTichMl ? `${row.dungTichMl} ml` : '—' }}</td>
              <td>{{ formatCurrency(row.giaBan) }}</td>
              <td>{{ row.soLuongTon ?? 0 }}</td>
              <td>{{ row.hanSuDung || '—' }}</td>
              <td class="text-center">
                <div class="flex flex-col items-center gap-1">
                  <AdminSwitch :model-value="row.trangThai !== false" @update:model-value="handleToggleStatus(row)" />
                  <span
                    class="text-xs"
                    :class="row.trangThai !== false ? 'text-[var(--admin-primary)]' : 'text-gray-400'"
                  >
                    {{ row.trangThai !== false ? 'Hoạt động' : 'Ngưng' }}
                  </span>
                </div>
              </td>
              <td class="text-center">
                <button type="button" class="admin-icon-btn admin-icon-btn--warning" @click="openEdit(row)">✏️</button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <div class="px-4 py-3 border-t flex justify-end gap-2" style="border-color: var(--admin-border)">
        <button type="button" class="admin-btn admin-btn-default" :disabled="page <= 1" @click="page--">‹ Trước</button>
        <span class="text-sm self-center">{{ page }} / {{ totalPages }}</span>
        <button type="button" class="admin-btn admin-btn-default" :disabled="page >= totalPages" @click="page++">Sau ›</button>
      </div>
    </div>

    <VariantModal
      :open="modalOpen"
      :mode="modalMode"
      :loading="saving"
      :product-id="selectedProductId"
      :ma-san-pham="selectedProduct?.maSanPham"
      :initial="editingVariant"
      :mau-sac-options="mauSacOptions"
      @close="modalOpen = false"
      @submit="handleSubmit"
    />
  </div>
</template>
