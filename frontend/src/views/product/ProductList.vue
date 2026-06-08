<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import ProductFilter from '@/components/products/ProductFilter.vue'
import ProductTable from '@/components/products/ProductTable.vue'
import ProductFormModal from '@/components/products/ProductFormModal.vue'
import {
  addProduct,
  getProductDetail,
  getProducts,
  updateProductStatus,
  searchProducts,
  updateProduct,
} from '@/api/sanPhamApi'
import { formToFormData } from '@/utils/productForm'
import {
  getCongDungList,
  getDanhMucList,
  getDangSanPhamList,
  getMauSacList,
  getThanhPhanList,
  getThuongHieuList,
} from '@/api/danhMucApi'
const router = useRouter()

const loading = ref(false)
const saving = ref(false)
const allProducts = ref([])
const message = ref('')
const messageType = ref('success')

const filters = ref({
  keyword: '',
  idDanhMuc: null,
  idThuongHieu: null,
  chiSoSpf: '',
  chiSoPa: '',
  trangThai: null,
})

const page = ref(1)
const pageSize = ref(10)

const danhMucOptions = ref([])
const thuongHieuOptions = ref([])
const dangSanPhamOptions = ref([])
const congDungOptions = ref([])
const thanhPhanOptions = ref([])
const mauSacOptions = ref([])

const modalOpen = ref(false)
const modalMode = ref('create')
const editingId = ref(null)
const editingDetail = ref(null)

let searchTimer = null

const spfOptions = computed(() =>
  [...new Set(allProducts.value.map((p) => p.chiSoSpf).filter(Boolean))].sort(),
)
const paOptions = computed(() =>
  [...new Set(allProducts.value.map((p) => p.chiSoPa).filter(Boolean))].sort(),
)

const filteredProducts = computed(() => {
  let list = [...allProducts.value]

  if (filters.value.idDanhMuc) {
    list = list.filter((p) => {
      const dm = danhMucOptions.value.find((d) => d.id === filters.value.idDanhMuc)
      return dm && p.tenDanhMuc === dm.ten
    })
  }
  if (filters.value.idThuongHieu) {
    list = list.filter((p) => {
      const th = thuongHieuOptions.value.find((d) => d.id === filters.value.idThuongHieu)
      return th && p.tenThuongHieu === th.ten
    })
  }
  if (filters.value.chiSoSpf) {
    list = list.filter((p) => p.chiSoSpf === filters.value.chiSoSpf)
  }
  if (filters.value.chiSoPa) {
    list = list.filter((p) => p.chiSoPa === filters.value.chiSoPa)
  }
  if (filters.value.trangThai !== null) {
    list = list.filter((p) => (p.trangThai !== false) === filters.value.trangThai)
  }

  return list
})

const totalPages = computed(() =>
  Math.max(1, Math.ceil(filteredProducts.value.length / pageSize.value)),
)

const pagedProducts = computed(() => {
  const start = (page.value - 1) * pageSize.value
  return filteredProducts.value.slice(start, start + pageSize.value)
})

function showMessage(text, type = 'success') {
  message.value = text
  messageType.value = type
  setTimeout(() => {
    message.value = ''
  }, 3500)
}

async function loadLookups() {
  const [dm, th, dang, cd, tp, ms] = await Promise.all([
    getDanhMucList(),
    getThuongHieuList(),
    getDangSanPhamList(),
    getCongDungList(),
    getThanhPhanList(),
    getMauSacList(),
  ])
  const onlyActive = (list) => (list || []).filter((item) => item.trangThai !== false)
  danhMucOptions.value = onlyActive(dm.data)
  thuongHieuOptions.value = onlyActive(th.data)
  dangSanPhamOptions.value = onlyActive(dang.data)
  congDungOptions.value = onlyActive(cd.data)
  thanhPhanOptions.value = onlyActive(tp.data)
  mauSacOptions.value = onlyActive(ms.data)
}

async function loadProducts() {
  loading.value = true
  try {
    const keyword = filters.value.keyword?.trim()
    const res = keyword
      ? await searchProducts(keyword)
      : await getProducts()
    allProducts.value = res.data || []
    page.value = 1
  } catch (err) {
    showMessage(String(err), 'error')
  } finally {
    loading.value = false
  }
}

function resetFilters() {
  filters.value = {
    keyword: '',
    idDanhMuc: null,
    idThuongHieu: null,
    chiSoSpf: '',
    chiSoPa: '',
    trangThai: null,
  }
  loadProducts()
}

function openCreateModal() {
  modalMode.value = 'create'
  editingId.value = null
  editingDetail.value = null
  modalOpen.value = true
}

async function openEditModal(product) {
  modalMode.value = 'edit'
  editingId.value = product.id
  saving.value = true
  try {
    const res = await getProductDetail(product.id)
    editingDetail.value = res.data
    modalOpen.value = true
  } catch (err) {
    showMessage(String(err), 'error')
  } finally {
    saving.value = false
  }
}

function handleManage(product) {
  router.push({
    path: '/admin/products/variants',
    query: { productId: product.id },
  })
}

async function handleToggleStatus(product) {
  const isActive = product.trangThai !== false
  const action = isActive ? 'ngưng hoạt động' : 'kích hoạt lại'
  if (!confirm(`Bạn có chắc muốn ${action} sản phẩm "${product.ten}"?`)) return
  try {
    await updateProductStatus(product.id, !isActive)
    showMessage(isActive ? 'Đã chuyển sang ngưng hoạt động' : 'Đã kích hoạt lại sản phẩm')
    await loadProducts()
  } catch (err) {
    showMessage(String(err), 'error')
  }
}

async function handleSubmit(form) {
  saving.value = true
  try {
    const formData = formToFormData(form)
    if (modalMode.value === 'create') {
      await addProduct(formData)
      showMessage('Thêm sản phẩm thành công')
    } else {
      await updateProduct(editingId.value, formData)
      showMessage('Cập nhật sản phẩm thành công')
    }
    modalOpen.value = false
    await loadProducts()
  } catch (err) {
    showMessage(String(err), 'error')
  } finally {
    saving.value = false
  }
}

watch(
  () => filters.value.keyword,
  () => {
    clearTimeout(searchTimer)
    searchTimer = setTimeout(() => loadProducts(), 400)
  },
)

watch(filteredProducts, () => {
  if (page.value > totalPages.value) {
    page.value = totalPages.value
  }
})

onMounted(async () => {
  try {
    await loadLookups()
    await loadProducts()
  } catch (err) {
    showMessage(String(err), 'error')
  }
})
</script>

<template>
  <div class="space-y-4">
    <div class="admin-page-header flex-col sm:flex-row sm:items-center">
      <div class="admin-page-header__icon">☀️</div>
      <div class="flex-1 min-w-0">
        <h1 class="admin-page-title">Quản lý sản phẩm</h1>
        <p class="admin-page-subtitle">
          SUNOVA — sản phẩm chống nắng ({{ filteredProducts.length }} sản phẩm)
        </p>
      </div>
      <button type="button" class="admin-fab-btn admin-fab-btn--primary group shrink-0" @click="openCreateModal">
        <span>＋</span>
        <span class="admin-fab-btn__label">Thêm sản phẩm</span>
      </button>
    </div>

    <div
      v-if="message"
      class="admin-alert rounded-lg px-4 py-3 text-sm"
      :class="messageType === 'error' ? 'admin-alert-error' : 'admin-alert-success'"
    >
      {{ message }}
    </div>

    <ProductFilter
      v-model="filters"
      :danh-muc-options="danhMucOptions"
      :thuong-hieu-options="thuongHieuOptions"
      :spf-options="spfOptions"
      :pa-options="paOptions"
      @reset="resetFilters"
    />

    <div class="admin-card admin-card--rounded">
      <div class="admin-card-header">
        <h3>Danh sách sản phẩm</h3>
        <div class="flex items-center gap-2">
          <span class="text-sm text-gray-500 hidden sm:inline">Trang {{ page }} / {{ totalPages }}</span>
          <button type="button" class="admin-fab-btn admin-fab-btn--info group" @click="loadProducts">
            <span>⟳</span>
            <span class="admin-fab-btn__label">Tải lại</span>
          </button>
        </div>
      </div>

      <ProductTable
        :products="pagedProducts"
        :loading="loading"
        :page="page"
        :page-size="pageSize"
        @edit="openEditModal"
        @toggle-status="handleToggleStatus"
        @manage="handleManage"
      />

      <div
        class="px-4 py-3 border-t flex items-center justify-between gap-3"
        style="border-color: var(--admin-border)"
      >
        <div class="text-sm text-[var(--admin-muted)]">
          Hiển thị {{ pagedProducts.length }} / {{ filteredProducts.length }} sản phẩm
        </div>
        <div class="flex items-center gap-2">
          <button
            type="button"
            class="admin-btn admin-btn-default"
            :disabled="page <= 1"
            @click="page--"
          >
            ‹ Trước
          </button>
          <button
            type="button"
            class="admin-btn admin-btn-default"
            :disabled="page >= totalPages"
            @click="page++"
          >
            Sau ›
          </button>
        </div>
      </div>
    </div>

    <ProductFormModal
      :open="modalOpen"
      :mode="modalMode"
      :loading="saving"
      :product-id="editingId"
      :initial-detail="editingDetail"
      :danh-muc-options="danhMucOptions"
      :thuong-hieu-options="thuongHieuOptions"
      :dang-san-pham-options="dangSanPhamOptions"
      :cong-dung-options="congDungOptions"
      :thanh-phan-options="thanhPhanOptions"
      :mau-sac-options="mauSacOptions"
      @close="modalOpen = false"
      @submit="handleSubmit"
    />
  </div>
</template>
