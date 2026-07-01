<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { RouterLink, useRoute, useRouter } from 'vue-router'
import { Icon } from '@iconify/vue'
import ProductCard from '@/components/storefront/ProductCard.vue'
import { LOAI_DA_OPTIONS } from '@/constants/loaiDa'
import { LOAI_CHONG_NANG_LABELS } from '@/constants/loaiChongNang'
import {
  fetchAllProducts,
  fetchCongDungList,
  fetchDanhMucList,
  fetchProductDetail,
  fetchSaleProducts,
  fetchThuongHieuList,
  searchProducts,
} from '@/api/storefrontApi'

const route = useRoute()
const router = useRouter()

const loading = ref(true)
const enriching = ref(false)
const allProducts = ref([])
const detailMap = ref({})

const danhMucList = ref([])
const thuongHieuList = ref([])
const congDungList = ref([])

const searchQuery = ref('')
const selectedDanhMuc = ref([])
const selectedThuongHieu = ref([])
const selectedLoaiCN = ref([])
const selectedCongDung = ref([])
const selectedLoaiDa = ref([])
const selectedSpf = ref([])
const priceMinInput = ref('')
const priceMaxInput = ref('')
const appliedPriceMin = ref('')
const appliedPriceMax = ref('')
const filterNoiBat = ref(false)
const isKhuyenMaiPage = computed(() => route.path === '/san-pham/khuyen-mai')
const sortBy = ref('newest')
const priceAsc = ref(true)
const viewMode = ref('grid')
const page = ref(1)
const pageSize = 12

let searchTimer

function activeProducts(list) {
  return list.filter((p) => p.trangThai !== false)
}

function toggleId(list, id) {
  const s = String(id)
  const idx = list.indexOf(s)
  if (idx >= 0) list.splice(idx, 1)
  else list.push(s)
  page.value = 1
}

function isChecked(list, id) {
  return list.includes(String(id))
}

async function loadMeta() {
  const [dm, th, cd] = await Promise.all([
    fetchDanhMucList(),
    fetchThuongHieuList(),
    fetchCongDungList(),
  ])
  danhMucList.value = (dm.data || []).filter((d) => d.trangThai !== false)
  thuongHieuList.value = (th.data || []).filter((d) => d.trangThai !== false)
  congDungList.value = (cd.data || []).filter((d) => d.trangThai !== false)
}

async function loadProducts() {
  loading.value = true
  try {
    if (isKhuyenMaiPage.value) {
      const res = await fetchSaleProducts()
      allProducts.value = activeProducts(res.data || [])
    } else if (searchQuery.value.trim()) {
      const res = await searchProducts(searchQuery.value.trim(), { excludeKhuyenMai: true })
      allProducts.value = activeProducts(res.data || [])
    } else {
      const res = await fetchAllProducts({ excludeKhuyenMai: true })
      allProducts.value = activeProducts(res.data || [])
    }
  } catch (e) {
    console.error(e)
    allProducts.value = []
  } finally {
    loading.value = false
  }
}

async function ensureDetails() {
  if (!selectedCongDung.value.length && !selectedLoaiDa.value.length) return
  const missing = allProducts.value.filter((p) => !detailMap.value[p.id])
  if (!missing.length) return
  enriching.value = true
  try {
    const results = await Promise.all(missing.map((p) => fetchProductDetail(p.id)))
    results.forEach((res, i) => {
      detailMap.value[missing[i].id] = res.data
    })
  } finally {
    enriching.value = false
  }
}

function applyRouteQuery() {
  if (route.query.danhMuc) selectedDanhMuc.value = [String(route.query.danhMuc)]
  if (route.query.thuongHieu) selectedThuongHieu.value = [String(route.query.thuongHieu)]
  if (route.query.q) searchQuery.value = String(route.query.q)
  if (route.query.noiBat === '1') filterNoiBat.value = true
}

const keywordFromRoute = computed(() => (route.query.q ? String(route.query.q) : ''))

const pageTitle = computed(() => {
  if (isKhuyenMaiPage.value) {
    return 'Sản phẩm khuyến mãi'
  }
  if (keywordFromRoute.value) {
    return `Kết quả tìm kiếm từ khóa "${keywordFromRoute.value}"`
  }
  return 'Sản phẩm chống nắng'
})

const filtered = computed(() => {
  let list = [...allProducts.value]

  if (selectedDanhMuc.value.length) {
    const names = selectedDanhMuc.value
      .map((id) => danhMucList.value.find((d) => String(d.id) === id)?.ten)
      .filter(Boolean)
    list = list.filter((p) => names.includes(p.tenDanhMuc))
  }
  if (selectedThuongHieu.value.length) {
    const names = selectedThuongHieu.value
      .map((id) => thuongHieuList.value.find((t) => String(t.id) === id)?.ten)
      .filter(Boolean)
    list = list.filter((p) => names.includes(p.tenThuongHieu))
  }
  if (selectedLoaiCN.value.length) {
    list = list.filter((p) => selectedLoaiCN.value.includes(p.loaiChongNang))
  }
  if (selectedSpf.value.length) {
    list = list.filter((p) => selectedSpf.value.includes(String(p.chiSoSpf || '')))
  }
  if (filterNoiBat.value) {
    list = list.filter((p) => p.noiBat === true)
  }
  if (appliedPriceMin.value) {
    const min = Number(appliedPriceMin.value)
    list = list.filter((p) => Number(p.giaMin || p.giaMax || 0) >= min)
  }
  if (appliedPriceMax.value) {
    const max = Number(appliedPriceMax.value)
    list = list.filter((p) => Number(p.giaMax || p.giaMin || 0) <= max)
  }
  if (selectedCongDung.value.length) {
    const ids = selectedCongDung.value.map(Number)
    list = list.filter((p) => {
      const d = detailMap.value[p.id]
      return d?.idCongDungs?.some((id) => ids.includes(id))
    })
  }
  if (selectedLoaiDa.value.length) {
    const ids = selectedLoaiDa.value.map(Number)
    list = list.filter((p) => {
      const d = detailMap.value[p.id]
      return d?.idLoaiDas?.some((id) => ids.includes(id))
    })
  }

  switch (sortBy.value) {
    case 'price':
      list.sort((a, b) => {
        const diff = Number(a.giaMin || 0) - Number(b.giaMin || 0)
        return priceAsc.value ? diff : -diff
      })
      break
    case 'popular':
    case 'bestseller':
      list.sort((a, b) => Number(b.noiBat) - Number(a.noiBat) || new Date(b.ngayTao || 0) - new Date(a.ngayTao || 0))
      break
    default:
      list.sort((a, b) => new Date(b.ngayTao || 0) - new Date(a.ngayTao || 0))
  }
  return list
})

const totalPages = computed(() => Math.max(1, Math.ceil(filtered.value.length / pageSize)))
const paged = computed(() => {
  const start = (page.value - 1) * pageSize
  return filtered.value.slice(start, start + pageSize)
})

const spfOptions = computed(() => {
  const set = new Set()
  allProducts.value.forEach((p) => {
    if (p.chiSoSpf) set.add(String(p.chiSoSpf))
  })
  return [...set].sort((a, b) => Number(a) - Number(b))
})

function applyPrice() {
  appliedPriceMin.value = priceMinInput.value
  appliedPriceMax.value = priceMaxInput.value
  page.value = 1
}

function setSort(id) {
  if (id === 'price' && sortBy.value === 'price') {
    priceAsc.value = !priceAsc.value
  } else {
    sortBy.value = id
    if (id === 'price') priceAsc.value = true
  }
}

function resetFilters() {
  selectedDanhMuc.value = []
  selectedThuongHieu.value = []
  selectedLoaiCN.value = []
  selectedCongDung.value = []
  selectedLoaiDa.value = []
  selectedSpf.value = []
  priceMinInput.value = ''
  priceMaxInput.value = ''
  appliedPriceMin.value = ''
  appliedPriceMax.value = ''
  filterNoiBat.value = false
  searchQuery.value = ''
  page.value = 1
  router.replace({ path: isKhuyenMaiPage.value ? '/san-pham/khuyen-mai' : '/san-pham' })
}

watch([selectedCongDung, selectedLoaiDa], () => {
  ensureDetails()
}, { deep: true })

watch(
  () => [route.path, route.query],
  () => {
    applyRouteQuery()
    page.value = 1
    loadProducts()
  },
)

watch(searchQuery, () => {
  if (isKhuyenMaiPage.value) return
  clearTimeout(searchTimer)
  searchTimer = setTimeout(() => {
    page.value = 1
    const q = searchQuery.value.trim()
    router.replace({ path: '/san-pham', query: q ? { q } : {} })
    loadProducts()
  }, 350)
})

watch(filtered, () => {
  if (page.value > totalPages.value) page.value = 1
})

onMounted(async () => {
  applyRouteQuery()
  await loadMeta()
  await loadProducts()
  await ensureDetails()
})
</script>

<template>
  <div class="sf-plp">
    <div class="sf-plp__banner">
      <div class="sf-container">
        <nav class="sf-breadcrumb sf-breadcrumb--light">
          <RouterLink to="/">Trang chủ</RouterLink>
          <span>/</span>
          <span>{{ isKhuyenMaiPage ? 'Khuyến mãi' : 'Sản phẩm' }}</span>
        </nav>
        <h1 class="sf-plp__title">{{ pageTitle }}</h1>
        <div class="sf-plp__result-tabs">
          <button type="button" class="active">Sản phẩm ({{ filtered.length }})</button>
          <button type="button" class="sf-plp__tab--disabled" disabled title="Sắp ra mắt">Bài viết</button>
        </div>
      </div>
    </div>

    <div class="sf-container sf-plp__layout">
      <aside class="sf-plp__sidebar">
        <div class="sf-filter-block">
          <label class="sf-filter-label">Tìm kiếm</label>
          <input v-model="searchQuery" type="search" class="sf-filter-input" placeholder="Tên sản phẩm..." />
        </div>

        <div class="sf-filter-block">
          <span class="sf-filter-label">Khoảng giá</span>
          <div class="sf-filter-block--row">
            <input v-model="priceMinInput" type="number" class="sf-filter-input" placeholder="Từ" min="0" />
            <input v-model="priceMaxInput" type="number" class="sf-filter-input" placeholder="Đến" min="0" />
          </div>
          <button type="button" class="sf-filter-apply" @click="applyPrice">Áp dụng</button>
        </div>

        <div class="sf-filter-block">
          <span class="sf-filter-label">Thương hiệu</span>
          <label v-for="t in thuongHieuList" :key="t.id" class="sf-filter-check">
            <input type="checkbox" :checked="isChecked(selectedThuongHieu, t.id)" @change="toggleId(selectedThuongHieu, t.id)" />
            {{ t.ten }}
          </label>
        </div>

        <div class="sf-filter-block">
          <span class="sf-filter-label">Danh mục</span>
          <label v-for="d in danhMucList" :key="d.id" class="sf-filter-check">
            <input type="checkbox" :checked="isChecked(selectedDanhMuc, d.id)" @change="toggleId(selectedDanhMuc, d.id)" />
            {{ d.ten }}
          </label>
        </div>

        <div class="sf-filter-block">
          <span class="sf-filter-label">Loại chống nắng</span>
          <label v-for="(label, key) in LOAI_CHONG_NANG_LABELS" :key="key" class="sf-filter-check">
            <input type="checkbox" :checked="isChecked(selectedLoaiCN, key)" @change="toggleId(selectedLoaiCN, key)" />
            {{ label }}
          </label>
        </div>

        <div class="sf-filter-block">
          <span class="sf-filter-label">Công dụng</span>
          <label v-for="c in congDungList" :key="c.id" class="sf-filter-check">
            <input type="checkbox" :checked="isChecked(selectedCongDung, c.id)" @change="toggleId(selectedCongDung, c.id)" />
            {{ c.ten }}
          </label>
        </div>

        <div class="sf-filter-block">
          <span class="sf-filter-label">Loại da</span>
          <label v-for="ld in LOAI_DA_OPTIONS" :key="ld.id" class="sf-filter-check">
            <input type="checkbox" :checked="isChecked(selectedLoaiDa, ld.id)" @change="toggleId(selectedLoaiDa, ld.id)" />
            {{ ld.ten }}
          </label>
        </div>

        <div v-if="spfOptions.length" class="sf-filter-block">
          <span class="sf-filter-label">SPF</span>
          <label v-for="s in spfOptions" :key="s" class="sf-filter-check">
            <input type="checkbox" :checked="isChecked(selectedSpf, s)" @change="toggleId(selectedSpf, s)" />
            SPF {{ s }}
          </label>
        </div>

        <label class="sf-filter-check">
          <input v-model="filterNoiBat" type="checkbox" />
          Chỉ sản phẩm nổi bật
        </label>

        <button type="button" class="sf-filter-reset" @click="resetFilters">Xóa bộ lọc</button>
      </aside>

      <div class="sf-plp__main">
        <div class="sf-plp__toolbar">
          <div class="sf-sort-tabs">
            <span class="sf-sort-tabs__label">Sắp xếp</span>
            <button type="button" :class="{ active: sortBy === 'popular' }" @click="setSort('popular')">Phổ biến</button>
            <button type="button" :class="{ active: sortBy === 'newest' }" @click="setSort('newest')">Mới nhất</button>
            <button type="button" :class="{ active: sortBy === 'bestseller' }" @click="setSort('bestseller')">Bán chạy</button>
            <button type="button" :class="{ active: sortBy === 'price' }" @click="setSort('price')">
              Giá {{ sortBy === 'price' ? (priceAsc ? '↑' : '↓') : '↑↓' }}
            </button>
          </div>
          <div class="sf-view-toggle">
            <button type="button" :class="{ active: viewMode === 'grid' }" title="Lưới" @click="viewMode = 'grid'">
              <Icon icon="solar:widget-4-linear" width="18" />
            </button>
            <button type="button" :class="{ active: viewMode === 'list' }" title="Danh sách" @click="viewMode = 'list'">
              <Icon icon="solar:list-linear" width="18" />
            </button>
          </div>
        </div>

        <div v-if="loading || enriching" class="sf-skeleton-grid" />
        <div
          v-else-if="paged.length"
          :class="viewMode === 'grid' ? 'sf-product-grid' : 'sf-product-list'"
        >
          <ProductCard
            v-for="p in paged"
            :key="p.id"
            :product="p"
            :layout="viewMode"
            :show-quick-add="false"
          />
        </div>
        <div v-else class="sf-empty-state">
          <p>{{ isKhuyenMaiPage ? 'Hiện chưa có sản phẩm trong đợt khuyến mãi đang hoạt động.' : 'Không tìm thấy sản phẩm phù hợp.' }}</p>
          <button type="button" class="btn-soleil-outline" @click="resetFilters">Xóa bộ lọc</button>
        </div>

        <div v-if="totalPages > 1 && !loading" class="sf-pagination">
          <button type="button" class="sf-page-btn" :disabled="page <= 1" @click="page--">←</button>
          <span class="sf-page-info">Trang {{ page }} / {{ totalPages }}</span>
          <button type="button" class="sf-page-btn" :disabled="page >= totalPages" @click="page++">→</button>
        </div>
      </div>
    </div>
  </div>
</template>
