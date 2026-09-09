<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { Icon } from '@iconify/vue'
import ProductFilter from '@/components/products/ProductFilter.vue'
import ProductTable from '@/components/products/ProductTable.vue'
import ProductFormModal from '@/components/products/ProductFormModal.vue'
import PageHeader from '@/components/ui/PageHeader.vue'
import '@/styles/productAdmin.css'
import {
  addProduct,
  getProductDetail,
  getProducts,
  updateProductStatus,
  updateProductNoiBat,
  searchProducts,
  updateProduct,
} from '@/api/sanPhamApi'
import { formToFormData } from '@/utils/productForm'
import { confirm } from '@/composables/useConfirm'
import { useAdminAuth } from '@/composables/useAdminAuth'
import { isManagerOrOwner } from '@/utils/adminAuth'
import {
  getCongDungList,
  getDanhMucList,
  getDangSanPhamList,
  getMauSacList,
  getThanhPhanList,
  getThuongHieuList,
} from '@/api/danhMucApi'

const router = useRouter()
const { vaiTro } = useAdminAuth()
/** Chỉ CHU / QUAN_LY được thêm·sửa·đổi trạng thái sản phẩm */
const canWriteProduct = computed(() => isManagerOrOwner(vaiTro.value))

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
  /** null | 'sapHetHang' | 'canHan' — lọc nhanh cảnh báo */
  canhBao: null,
})

const LOW_STOCK_THRESHOLD = 10

const page = ref(1)
const pageSize = ref(12)

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
  if (filters.value.canhBao === 'sapHetHang') {
    list = list.filter((p) => Number(p.tongTon ?? 0) <= LOW_STOCK_THRESHOLD)
  } else if (filters.value.canhBao === 'canHan') {
    list = list.filter((p) => p.coLoCanHan === true)
  }

  return list
})

const countSapHetHang = computed(
  () => allProducts.value.filter((p) => Number(p.tongTon ?? 0) <= LOW_STOCK_THRESHOLD).length,
)

const countCanHan = computed(
  () => allProducts.value.filter((p) => p.coLoCanHan === true).length,
)

function formatWarnBadge(n) {
  if (!n || n <= 0) return '0'
  return n > 99 ? '99+' : String(n)
}

function toggleCanhBao(type) {
  filters.value.canhBao = filters.value.canhBao === type ? null : type
  page.value = 1
}

const totalPages = computed(() =>
  Math.max(1, Math.ceil(filteredProducts.value.length / pageSize.value)),
)

const pagedProducts = computed(() => {
  const start = (page.value - 1) * pageSize.value
  return filteredProducts.value.slice(start, start + pageSize.value)
})

const statTotal = computed(() => allProducts.value.length)

const statActive = computed(
  () => allProducts.value.filter((p) => p.trangThai !== false).length,
)

const statInactive = computed(
  () => allProducts.value.filter((p) => p.trangThai === false).length,
)

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
    const res = keyword ? await searchProducts(keyword) : await getProducts()
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
    canhBao: null,
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
  router.push(`/admin/san-pham/${product.id}/bien-the`)
}

async function handleToggleStatus(product) {
  const isActive = product.trangThai !== false
  const action = isActive ? 'ẩn' : 'hiện'
  const ok = await confirm({
    title: isActive ? 'Ẩn sản phẩm' : 'Hiện sản phẩm',
    message: `Bạn có muốn ${action} sản phẩm "${product.ten}" không?`,
    confirmText: isActive ? 'Ẩn' : 'Hiện',
    danger: isActive,
  })
  if (!ok) return
  try {
    await updateProductStatus(product.id, !isActive)
    showMessage(isActive ? 'Đã chuyển sang ngưng hoạt động' : 'Đã kích hoạt lại sản phẩm')
    await loadProducts()
  } catch (err) {
    showMessage(String(err), 'error')
  }
}

async function handleToggleNoiBat(product) {
  const isFeatured = !!product.noiBat
  const ok = await confirm({
    title: isFeatured ? 'Bỏ nổi bật' : 'Đánh dấu nổi bật',
    message: `Bạn có chắc muốn ${isFeatured ? 'bỏ đánh dấu nổi bật' : 'đánh dấu nổi bật'} sản phẩm "${product.ten}"?`,
    confirmText: 'Xác nhận',
  })
  if (!ok) return
  try {
    await updateProductNoiBat(product.id, !isFeatured)
    showMessage(isFeatured ? 'Đã bỏ nổi bật' : 'Đã đánh dấu nổi bật')
    await loadProducts()
  } catch (err) {
    showMessage(String(err), 'error')
  }
}

async function handleSubmit(form) {
  const isCreate = modalMode.value === 'create'
  const ok = await confirm({
    title: isCreate ? 'Tạo sản phẩm' : 'Cập nhật sản phẩm',
    message: isCreate
      ? 'Lưu sản phẩm mới vào danh sách?'
      : 'Cập nhật thông tin sản phẩm này?',
    confirmText: isCreate ? 'Tạo' : 'Cập nhật',
  })
  if (!ok) return
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
    editingDetail.value = null
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
  <div class="product-admin">
    <PageHeader
      title="Sản phẩm"
      :description="`${filteredProducts.length} kết quả · chống nắng & chăm sóc da`"
    >
      <template v-if="canWriteProduct" #actions>
        <button type="button" class="soleil-btn-primary" @click="openCreateModal">
          <Icon icon="icon-park-outline:plus" />
          Thêm sản phẩm
        </button>
      </template>
    </PageHeader>

    <div
      v-if="message"
      class="admin-alert rounded-lg px-4 py-3 text-sm"
      :class="messageType === 'error' ? 'admin-alert-error' : 'admin-alert-success'"
    >
      {{ message }}
    </div>

    <div class="pa-strip">
      <div class="pa-strip__item">
        <span class="pa-strip__label">Tổng SP</span>
        <span class="pa-strip__n">{{ statTotal }}</span>
      </div>
      <div class="pa-strip__item">
        <span class="pa-strip__label">Đang bán</span>
        <span class="pa-strip__n">{{ statActive }}</span>
      </div>
      <div class="pa-strip__item">
        <span class="pa-strip__label">Ngưng</span>
        <span class="pa-strip__n">{{ statInactive }}</span>
      </div>
      <div class="pa-strip__item pa-strip__item--warn">
        <span class="pa-strip__label">Cảnh báo kho</span>
        <span class="pa-strip__n">{{ countSapHetHang + countCanHan }}</span>
      </div>
    </div>

    <div class="pa-alerts">
      <button
        type="button"
        class="pa-chip"
        :class="{ 'pa-chip--active': filters.canhBao === 'sapHetHang' }"
        @click="toggleCanhBao('sapHetHang')"
      >
        Sắp hết hàng
        <span class="pa-chip__badge">{{ formatWarnBadge(countSapHetHang) }}</span>
      </button>
      <button
        type="button"
        class="pa-chip"
        :class="{ 'pa-chip--active': filters.canhBao === 'canHan' }"
        @click="toggleCanhBao('canHan')"
      >
        Cận hạn
        <span class="pa-chip__badge">{{ formatWarnBadge(countCanHan) }}</span>
      </button>
    </div>

    <div class="soleil-table-card">
      <div class="soleil-table-card__head">
        <ProductFilter
          compact
          v-model="filters"
          :danh-muc-options="danhMucOptions"
          :thuong-hieu-options="thuongHieuOptions"
          :spf-options="spfOptions"
          :pa-options="paOptions"
          @reset="resetFilters"
        />
      </div>

      <ProductTable
        :products="pagedProducts"
        :loading="loading"
        :page="page"
        :page-size="pageSize"
        :can-write="canWriteProduct"
        @edit="openEditModal"
        @toggle-status="handleToggleStatus"
        @toggle-noi-bat="handleToggleNoiBat"
        @manage="handleManage"
      />

      <div class="soleil-pagination">
        <span class="soleil-pagination__info">
          Hiển thị {{ pagedProducts.length }} / {{ filteredProducts.length }} sản phẩm
          · trang {{ page }} / {{ totalPages }}
        </span>
        <div class="soleil-pagination__btns">
          <button
            type="button"
            class="soleil-page-btn"
            :disabled="page <= 1"
            @click="page--"
          >
            <Icon icon="icon-park-outline:left" />
          </button>
          <button type="button" class="soleil-page-btn soleil-page-btn--active">{{ page }}</button>
          <button
            type="button"
            class="soleil-page-btn"
            :disabled="page >= totalPages"
            @click="page++"
          >
            <Icon icon="icon-park-outline:right" />
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
