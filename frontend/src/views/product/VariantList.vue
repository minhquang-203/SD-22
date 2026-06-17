<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import AdminSwitch from '@/components/admin/AdminSwitch.vue'
import VariantModal from '@/components/products/VariantModal.vue'
import LoHangListModal from '@/components/products/LoHangListModal.vue'
import LoHangLotModal from '@/components/products/LoHangLotModal.vue'
import { getMauSacList } from '@/api/danhMucApi'
import { getProductDetail } from '@/api/sanPhamApi'
import { addChiTiet, hideChiTiet, updateChiTiet } from '@/api/sanPhamApi'
import { getLoHangByChiTiet, nhapLoHang } from '@/api/loHangApi'
import { formatCurrency, formatDate } from '@/utils/format'
import { confirm } from '@/composables/useConfirm'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const saving = ref(false)
const lotLoading = ref(false)
const message = ref('')
const messageType = ref('success')

const mauSacOptions = ref([])
const productDetail = ref(null)

const productId = computed(() => {
  const id = Number(route.params.id)
  return Number.isNaN(id) ? null : id
})

const filter = reactive({
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

const lotListOpen = ref(false)
const lotFormOpen = ref(false)
const activeVariant = ref(null)
const activeLots = ref([])

const productInfo = computed(() => ({
  id: productDetail.value?.id,
  maSanPham: productDetail.value?.maSanPham,
  ten: productDetail.value?.ten,
}))

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

async function loadMauSac() {
  const res = await getMauSacList()
  mauSacOptions.value = (res.data || []).filter((m) => m.trangThai !== false)
}

async function loadProductDetail() {
  if (!productId.value) {
    productDetail.value = null
    return
  }
  loading.value = true
  try {
    const res = await getProductDetail(productId.value)
    productDetail.value = res.data
    page.value = 1
  } catch (err) {
    notify(String(err), 'error')
  } finally {
    loading.value = false
  }
}

function resetFilter() {
  filter.idMauSac = null
  filter.dungTichMl = null
  filter.priceMin = null
  filter.priceMax = null
}

function goBack() {
  router.push('/admin/products')
}

function openAdd() {
  if (!productId.value) return
  modalMode.value = 'add'
  editingVariant.value = null
  modalOpen.value = true
}

function openEdit(row) {
  modalMode.value = 'edit'
  editingVariant.value = row
  modalOpen.value = true
}

async function openLots(row) {
  activeVariant.value = row
  lotListOpen.value = true
  await loadLots(row.id)
}

async function loadLots(idChiTiet) {
  lotLoading.value = true
  try {
    const res = await getLoHangByChiTiet(idChiTiet)
    activeLots.value = res.data || []
  } catch (err) {
    notify(String(err), 'error')
  } finally {
    lotLoading.value = false
  }
}

function openNhapLo() {
  lotFormOpen.value = true
}

async function handleNhapLo(payload) {
  saving.value = true
  try {
    await nhapLoHang(payload)
    notify('Nhập lô thành công')
    lotFormOpen.value = false
    await Promise.all([loadLots(activeVariant.value.id), loadProductDetail()])
  } catch (err) {
    notify(String(err), 'error')
  } finally {
    saving.value = false
  }
}

async function handleSubmit(payload) {
  const isCreate = modalMode.value === 'add'
  const ok = await confirm({
    title: isCreate ? 'Thêm biến thể' : 'Cập nhật biến thể',
    message: isCreate ? 'Thêm biến thể mới cho sản phẩm?' : 'Cập nhật biến thể này?',
    confirmText: isCreate ? 'Thêm' : 'Cập nhật',
  })
  if (!ok) return
  saving.value = true
  try {
    if (isCreate) {
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
  const ok = await confirm({
    title: isActive ? 'Ngưng biến thể' : 'Kích hoạt biến thể',
    message: `Bạn có chắc muốn ${isActive ? 'ngưng' : 'kích hoạt'} biến thể "${row.sku}"?`,
    confirmText: isActive ? 'Ngưng' : 'Kích hoạt',
    danger: isActive,
  })
  if (!ok) return
  try {
    if (isActive) {
      await hideChiTiet(row.id)
      notify('Đã chuyển biến thể sang ngưng hoạt động')
    } else {
      await updateChiTiet(row.id, {
        idSanPham: productId.value,
        sku: row.sku,
        idMauSac: row.idMauSac,
        dungTichMl: row.dungTichMl,
        giaBan: row.giaBan,
        trangThai: true,
      })
      notify('Đã kích hoạt lại biến thể')
    }
    await loadProductDetail()
  } catch (err) {
    notify(String(err), 'error')
  }
}

watch(productId, () => {
  filter.dungTichMl = null
  loadProductDetail()
})

onMounted(async () => {
  if (!productId.value) {
    notify('Không tìm thấy sản phẩm', 'error')
    goBack()
    return
  }
  try {
    await loadMauSac()
    await loadProductDetail()
  } catch (err) {
    notify(String(err), 'error')
  }
})
</script>

<template>
  <div class="space-y-4">
    <div class="admin-page-header">
      <button type="button" class="admin-btn admin-btn-default shrink-0" @click="goBack">← Danh sách SP</button>
      <div class="admin-page-header__icon">📦</div>
      <div class="flex-1 min-w-0">
        <h1 class="admin-page-title truncate">Biến thể sản phẩm</h1>
        <p
          v-if="productInfo.maSanPham"
          class="admin-page-subtitle truncate text-[var(--admin-primary)] font-medium"
          :title="`${productInfo.maSanPham} - ${productInfo.ten}`"
        >
          {{ productInfo.maSanPham }} — {{ productInfo.ten }}
        </p>
        <p class="admin-page-subtitle">Quản lý SKU, giá bán, tồn kho theo lô (FEFO khi bán)</p>
      </div>
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
      <div class="p-4 md:p-5 grid grid-cols-1 md:grid-cols-3 gap-4">
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
        <div class="grid grid-cols-2 gap-3">
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
    </div>

    <div class="admin-card admin-card--rounded">
      <div class="admin-card-header flex-col items-stretch sm:flex-row sm:items-center gap-2">
        <div>
          <h3>Danh sách biến thể</h3>
          <p class="text-xs text-[var(--admin-muted)] mt-1 mb-0">
            Tồn kho = tổng các lô. HSD hiển thị theo lô gần nhất.
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
              <th>HSD gần nhất</th>
              <th class="text-center">Trạng thái</th>
              <th class="text-center">Thao tác</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="loading">
              <td colspan="9" class="text-center py-10 text-gray-400">Đang tải...</td>
            </tr>
            <tr v-else-if="pagedVariants.length === 0">
              <td colspan="9" class="text-center py-10 text-[var(--admin-muted)] px-6">
                Sản phẩm này chưa có biến thể. Nhấn <strong>+ Thêm biến thể</strong> để tạo.
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
              <td>
                <span>{{ row.hanSuDungGanNhat ? formatDate(row.hanSuDungGanNhat) : '—' }}</span>
                <span
                  v-if="row.sapHetHan"
                  class="ml-1 inline-block text-[10px] px-2 py-0.5 rounded-full bg-amber-100 text-amber-800"
                >
                  Sắp HH
                </span>
              </td>
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
                <div class="flex items-center justify-center gap-1">
                  <button type="button" class="admin-btn admin-btn-default text-xs !px-2" @click="openLots(row)">
                    Lô hàng
                  </button>
                  <button type="button" class="admin-icon-btn admin-icon-btn--warning" @click="openEdit(row)">✏️</button>
                </div>
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
      :product-id="productId"
      :ma-san-pham="productInfo.maSanPham"
      :initial="editingVariant"
      :mau-sac-options="mauSacOptions"
      @close="modalOpen = false"
      @submit="handleSubmit"
    />

    <LoHangListModal
      :open="lotListOpen"
      :loading="lotLoading"
      :variant="activeVariant"
      :lots="activeLots"
      @close="lotListOpen = false"
      @open-nhap="openNhapLo"
    />

    <LoHangLotModal
      :open="lotFormOpen"
      :loading="saving"
      :variant="activeVariant"
      @close="lotFormOpen = false"
      @submit="handleNhapLo"
    />
  </div>
</template>
