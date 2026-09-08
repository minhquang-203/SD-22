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
import { rankProductsByQuiz, resolveQuizProfile } from '@/utils/quizRecommend'

const route = useRoute()
const router = useRouter()

const loading = ref(true)
const enriching = ref(false)
const allProducts = ref([])
const detailMap = ref({})
const quizProfile = ref(null)

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
const isGoiYPage = computed(() => route.path === '/san-pham/goi-y' || route.meta.goiY === true)
const sortBy = ref('newest')
const priceAsc = ref(true)
const viewMode = ref('grid')
const page = ref(1)
const pageSize = 12
const openAcc = ref({
  price: true,
  brand: true,
  cat: false,
  type: false,
  use: false,
  skin: false,
  spf: false,
})

const PRICE_PRESETS = [
  { key: '0-300000', label: 'Dưới 300k', min: '', max: '300000' },
  { key: '300000-500000', label: '300–500k', min: '300000', max: '500000' },
  { key: '500000+', label: 'Trên 500k', min: '500000', max: '' },
  { key: '', label: 'Mọi mức giá', min: '', max: '' },
]

let searchTimer
let priceTimer

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

function listBasePath() {
  if (isGoiYPage.value) return '/san-pham/goi-y'
  if (isKhuyenMaiPage.value) return '/san-pham/khuyen-mai'
  return '/san-pham'
}

async function loadProducts() {
  loading.value = true
  try {
    if (isGoiYPage.value) {
      sortBy.value = 'relevance'
      const profile = await resolveQuizProfile()
      quizProfile.value = profile
      if (!profile) {
        allProducts.value = []
        return
      }
      const res = await fetchAllProducts({ excludeKhuyenMai: true })
      allProducts.value = rankProductsByQuiz(activeProducts(res.data || []), {
        scoreMap: profile.scoreMap,
        filters: profile.filters,
      })
      return
    }

    quizProfile.value = null
    if (sortBy.value === 'relevance') sortBy.value = 'newest'

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
  if (isGoiYPage.value) {
    if (quizProfile.value?.tenLoaiDa) {
      return `Sản phẩm gợi ý — ${quizProfile.value.tenLoaiDa}`
    }
    return 'Sản phẩm gợi ý'
  }
  if (isKhuyenMaiPage.value) {
    return 'Sản phẩm khuyến mãi'
  }
  if (keywordFromRoute.value) {
    return `Kết quả tìm kiếm từ khóa "${keywordFromRoute.value}"`
  }
  return 'Sản phẩm chống nắng'
})

const breadcrumbLabel = computed(() => {
  if (isGoiYPage.value) return 'Sản phẩm gợi ý'
  if (isKhuyenMaiPage.value) return 'Khuyến mãi'
  return 'Sản phẩm'
})

const filtered = computed(() => {
  let list = [...allProducts.value]

  if (isGoiYPage.value && searchQuery.value.trim()) {
    const q = searchQuery.value.trim().toLowerCase()
    list = list.filter((p) => (p.ten || '').toLowerCase().includes(q))
  }

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
      const fromDetail = d?.idLoaiDas?.some((id) => ids.includes(id))
      const fromList = p.idLoaiDas?.some((id) => ids.includes(id))
      return fromDetail || fromList
    })
  }

  switch (sortBy.value) {
    case 'relevance':
      list.sort((a, b) => Number(b.matchScore || 0) - Number(a.matchScore || 0))
      break
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

function schedulePriceApply() {
  clearTimeout(priceTimer)
  priceTimer = setTimeout(applyPrice, 250)
}

function applyPricePreset(preset) {
  priceMinInput.value = preset.min
  priceMaxInput.value = preset.max
  applyPrice()
}

function toggleAcc(key) {
  openAcc.value[key] = !openAcc.value[key]
}

const activePricePreset = computed(() => {
  const min = String(appliedPriceMin.value || '')
  const max = String(appliedPriceMax.value || '')
  const match = PRICE_PRESETS.find((p) => p.min === min && p.max === max)
  return match ? match.key : null
})

const hasActiveFilters = computed(() => (
  selectedDanhMuc.value.length
  || selectedThuongHieu.value.length
  || selectedLoaiCN.value.length
  || selectedCongDung.value.length
  || selectedLoaiDa.value.length
  || selectedSpf.value.length
  || !!appliedPriceMin.value
  || !!appliedPriceMax.value
  || filterNoiBat.value
  || !!searchQuery.value.trim()
))

const activeTags = computed(() => {
  const tags = []
  selectedThuongHieu.value.forEach((id) => {
    const item = thuongHieuList.value.find((t) => String(t.id) === String(id))
    if (item) tags.push({ group: 'brand', id, label: item.ten })
  })
  selectedDanhMuc.value.forEach((id) => {
    const item = danhMucList.value.find((d) => String(d.id) === String(id))
    if (item) tags.push({ group: 'cat', id, label: item.ten })
  })
  selectedLoaiCN.value.forEach((id) => {
    tags.push({ group: 'type', id, label: LOAI_CHONG_NANG_LABELS[id] || id })
  })
  selectedCongDung.value.forEach((id) => {
    const item = congDungList.value.find((c) => String(c.id) === String(id))
    if (item) tags.push({ group: 'use', id, label: item.ten })
  })
  selectedLoaiDa.value.forEach((id) => {
    const item = LOAI_DA_OPTIONS.find((d) => String(d.id) === String(id))
    if (item) tags.push({ group: 'skin', id, label: item.ten })
  })
  selectedSpf.value.forEach((id) => {
    tags.push({ group: 'spf', id, label: `SPF ${id}` })
  })
  if (appliedPriceMin.value || appliedPriceMax.value) {
    const min = appliedPriceMin.value ? Number(appliedPriceMin.value).toLocaleString('vi-VN') : null
    const max = appliedPriceMax.value ? Number(appliedPriceMax.value).toLocaleString('vi-VN') : null
    const label = min && max ? `${min}–${max}₫` : min ? `Từ ${min}₫` : `Đến ${max}₫`
    tags.push({ group: 'price', id: 'price', label })
  }
  if (filterNoiBat.value) tags.push({ group: 'hot', id: '1', label: 'Nổi bật' })
  return tags
})

function removeTag(tag) {
  if (tag.group === 'hot') filterNoiBat.value = false
  else if (tag.group === 'price') {
    priceMinInput.value = ''
    priceMaxInput.value = ''
    applyPrice()
  } else if (tag.group === 'brand') toggleId(selectedThuongHieu.value, tag.id)
  else if (tag.group === 'cat') toggleId(selectedDanhMuc.value, tag.id)
  else if (tag.group === 'type') toggleId(selectedLoaiCN.value, tag.id)
  else if (tag.group === 'use') toggleId(selectedCongDung.value, tag.id)
  else if (tag.group === 'skin') toggleId(selectedLoaiDa.value, tag.id)
  else if (tag.group === 'spf') toggleId(selectedSpf.value, tag.id)
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
  if (isGoiYPage.value) sortBy.value = 'relevance'
  router.replace({ path: listBasePath() })
}

watch(filterNoiBat, () => {
  page.value = 1
})

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
  if (isKhuyenMaiPage.value || isGoiYPage.value) {
    page.value = 1
    return
  }
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
          <span>{{ breadcrumbLabel }}</span>
        </nav>
        <h1 class="sf-plp__title">{{ pageTitle }}</h1>
        <p v-if="isGoiYPage && quizProfile" class="sf-plp__goi-y-hint">
          Sắp xếp mặc định theo độ phù hợp từ kết quả quiz (cao → thấp). Bạn vẫn có thể lọc và sắp xếp như trang sản phẩm thường.
        </p>
        <div class="sf-plp__result-tabs">
          <button type="button" class="active">Sản phẩm ({{ filtered.length }})</button>
          <button type="button" class="sf-plp__tab--disabled" disabled title="Sắp ra mắt">Bài viết</button>
        </div>
      </div>
    </div>

    <div v-if="isGoiYPage && !loading && !quizProfile" class="sf-container sf-plp__quiz-empty">
      <h2>Bạn chưa có kết quả quiz</h2>
      <p>Làm quiz da ngắn để SUNOVA phân tích làn da và gợi ý sản phẩm chống nắng phù hợp nhất với bạn.</p>
      <RouterLink to="/quiz" class="btn-soleil">Làm Quiz ngay</RouterLink>
    </div>

    <div v-else class="sf-container sf-plp__layout">
      <aside class="sf-filters" aria-label="Bộ lọc">
        <div class="sf-filters__head">
          <h2 class="sf-filters__title">Bộ lọc</h2>
          <button v-if="hasActiveFilters" type="button" class="sf-filters__clear" @click="resetFilters">
            Xóa hết
          </button>
        </div>

        <div v-if="activeTags.length" class="sf-active-row">
          <span v-for="tag in activeTags" :key="`${tag.group}-${tag.id}`" class="sf-active-tag">
            {{ tag.label }}
            <button type="button" aria-label="Bỏ lọc" @click="removeTag(tag)">×</button>
          </span>
        </div>

        <div class="sf-fsearch">
          <input v-model="searchQuery" type="search" class="sf-filter-input" placeholder="Tìm tên sản phẩm..." />
        </div>

        <div class="sf-facc" :class="{ 'is-open': openAcc.price }">
          <button type="button" class="sf-facc__btn" @click="toggleAcc('price')">
            <span class="sf-facc__label">Giá</span>
            <i class="sf-facc__count" :class="{ 'is-on': appliedPriceMin || appliedPriceMax }">1</i>
            <span class="sf-facc__chev" aria-hidden="true">▾</span>
          </button>
          <div class="sf-facc__body">
            <div class="sf-fprice-presets">
              <button
                v-for="preset in PRICE_PRESETS"
                :key="preset.key || 'all'"
                type="button"
                class="sf-fpill"
                :class="{ 'is-on': activePricePreset === preset.key }"
                @click="applyPricePreset(preset)"
              >
                {{ preset.label }}
              </button>
            </div>
            <div class="sf-frow">
              <input
                v-model="priceMinInput"
                type="number"
                class="sf-filter-input"
                placeholder="Từ"
                min="0"
                @input="schedulePriceApply"
              />
              <span>–</span>
              <input
                v-model="priceMaxInput"
                type="number"
                class="sf-filter-input"
                placeholder="Đến"
                min="0"
                @input="schedulePriceApply"
              />
            </div>
          </div>
        </div>

        <div class="sf-facc" :class="{ 'is-open': openAcc.brand }">
          <button type="button" class="sf-facc__btn" @click="toggleAcc('brand')">
            <span class="sf-facc__label">Thương hiệu</span>
            <i class="sf-facc__count" :class="{ 'is-on': selectedThuongHieu.length }">{{ selectedThuongHieu.length }}</i>
            <span class="sf-facc__chev" aria-hidden="true">▾</span>
          </button>
          <div class="sf-facc__body">
            <div class="sf-fpills">
              <button
                v-for="t in thuongHieuList"
                :key="t.id"
                type="button"
                class="sf-fpill"
                :class="{ 'is-on': isChecked(selectedThuongHieu, t.id) }"
                @click="toggleId(selectedThuongHieu, t.id)"
              >
                {{ t.ten }}
              </button>
            </div>
          </div>
        </div>

        <div class="sf-facc" :class="{ 'is-open': openAcc.cat }">
          <button type="button" class="sf-facc__btn" @click="toggleAcc('cat')">
            <span class="sf-facc__label">Danh mục</span>
            <i class="sf-facc__count" :class="{ 'is-on': selectedDanhMuc.length }">{{ selectedDanhMuc.length }}</i>
            <span class="sf-facc__chev" aria-hidden="true">▾</span>
          </button>
          <div class="sf-facc__body">
            <div class="sf-fpills">
              <button
                v-for="d in danhMucList"
                :key="d.id"
                type="button"
                class="sf-fpill"
                :class="{ 'is-on': isChecked(selectedDanhMuc, d.id) }"
                @click="toggleId(selectedDanhMuc, d.id)"
              >
                {{ d.ten }}
              </button>
            </div>
          </div>
        </div>

        <div class="sf-facc" :class="{ 'is-open': openAcc.type }">
          <button type="button" class="sf-facc__btn" @click="toggleAcc('type')">
            <span class="sf-facc__label">Loại chống nắng</span>
            <i class="sf-facc__count" :class="{ 'is-on': selectedLoaiCN.length }">{{ selectedLoaiCN.length }}</i>
            <span class="sf-facc__chev" aria-hidden="true">▾</span>
          </button>
          <div class="sf-facc__body">
            <div class="sf-fpills">
              <button
                v-for="(label, key) in LOAI_CHONG_NANG_LABELS"
                :key="key"
                type="button"
                class="sf-fpill"
                :class="{ 'is-on': isChecked(selectedLoaiCN, key) }"
                @click="toggleId(selectedLoaiCN, key)"
              >
                {{ label }}
              </button>
            </div>
          </div>
        </div>

        <div class="sf-facc" :class="{ 'is-open': openAcc.use }">
          <button type="button" class="sf-facc__btn" @click="toggleAcc('use')">
            <span class="sf-facc__label">Công dụng</span>
            <i class="sf-facc__count" :class="{ 'is-on': selectedCongDung.length }">{{ selectedCongDung.length }}</i>
            <span class="sf-facc__chev" aria-hidden="true">▾</span>
          </button>
          <div class="sf-facc__body">
            <div class="sf-fpills">
              <button
                v-for="c in congDungList"
                :key="c.id"
                type="button"
                class="sf-fpill"
                :class="{ 'is-on': isChecked(selectedCongDung, c.id) }"
                @click="toggleId(selectedCongDung, c.id)"
              >
                {{ c.ten }}
              </button>
            </div>
          </div>
        </div>

        <div class="sf-facc" :class="{ 'is-open': openAcc.skin }">
          <button type="button" class="sf-facc__btn" @click="toggleAcc('skin')">
            <span class="sf-facc__label">Loại da</span>
            <i class="sf-facc__count" :class="{ 'is-on': selectedLoaiDa.length }">{{ selectedLoaiDa.length }}</i>
            <span class="sf-facc__chev" aria-hidden="true">▾</span>
          </button>
          <div class="sf-facc__body">
            <div class="sf-fpills">
              <button
                v-for="ld in LOAI_DA_OPTIONS"
                :key="ld.id"
                type="button"
                class="sf-fpill"
                :class="{ 'is-on': isChecked(selectedLoaiDa, ld.id) }"
                @click="toggleId(selectedLoaiDa, ld.id)"
              >
                {{ ld.ten }}
              </button>
            </div>
          </div>
        </div>

        <div v-if="spfOptions.length" class="sf-facc" :class="{ 'is-open': openAcc.spf }">
          <button type="button" class="sf-facc__btn" @click="toggleAcc('spf')">
            <span class="sf-facc__label">SPF</span>
            <i class="sf-facc__count" :class="{ 'is-on': selectedSpf.length }">{{ selectedSpf.length }}</i>
            <span class="sf-facc__chev" aria-hidden="true">▾</span>
          </button>
          <div class="sf-facc__body">
            <div class="sf-fpills">
              <button
                v-for="s in spfOptions"
                :key="s"
                type="button"
                class="sf-fpill"
                :class="{ 'is-on': isChecked(selectedSpf, s) }"
                @click="toggleId(selectedSpf, s)"
              >
                SPF {{ s }}
              </button>
            </div>
          </div>
        </div>

        <label class="sf-ftoggle">
          Chỉ nổi bật
          <span class="sf-switch">
            <input v-model="filterNoiBat" type="checkbox" />
            <span class="sf-switch__ui" />
          </span>
        </label>
      </aside>

      <div class="sf-plp__main">
        <div class="sf-plp__toolbar">
          <div class="sf-sort-tabs">
            <span class="sf-sort-tabs__label">Sắp xếp</span>
            <button
              v-if="isGoiYPage"
              type="button"
              :class="{ active: sortBy === 'relevance' }"
              @click="setSort('relevance')"
            >
              Phù hợp
            </button>
            <button type="button" :class="{ active: sortBy === 'popular' }" @click="setSort('popular')">Phổ biến</button>
            <button type="button" :class="{ active: sortBy === 'newest' }" @click="setSort('newest')">Mới nhất</button>
            <button type="button" :class="{ active: sortBy === 'bestseller' }" @click="setSort('bestseller')">Bán chạy</button>
            <button type="button" :class="{ active: sortBy === 'price' }" @click="setSort('price')">
              Giá {{ sortBy === 'price' ? (priceAsc ? '↑' : '↓') : '↑↓' }}
            </button>
          </div>
          <div class="sf-plp__toolbar-end">
            <div class="sf-plp__count"><strong>{{ filtered.length }}</strong> sản phẩm</div>
            <div class="sf-view-toggle">
              <button type="button" :class="{ active: viewMode === 'grid' }" title="Lưới" @click="viewMode = 'grid'">
                <Icon icon="solar:widget-4-linear" width="18" />
              </button>
              <button type="button" :class="{ active: viewMode === 'list' }" title="Danh sách" @click="viewMode = 'list'">
                <Icon icon="solar:list-linear" width="18" />
              </button>
            </div>
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
          <p>
            {{
              isKhuyenMaiPage
                ? 'Hiện chưa có sản phẩm trong đợt khuyến mãi đang hoạt động.'
                : isGoiYPage
                  ? 'Không tìm thấy sản phẩm phù hợp với bộ lọc hiện tại.'
                  : 'Không tìm thấy sản phẩm phù hợp.'
            }}
          </p>
          <button type="button" class="btn-soleil-outline" @click="resetFilters">Xóa bộ lọc</button>
          <RouterLink v-if="isGoiYPage" to="/quiz" class="btn-soleil-outline" style="margin-left: 8px">
            Làm lại Quiz
          </RouterLink>
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

<style scoped>
.sf-plp__goi-y-hint {
  margin: 8px 0 0;
  max-width: 640px;
  font-size: 14px;
  line-height: 1.5;
  color: rgba(255, 255, 255, 0.82);
}

.sf-plp__quiz-empty {
  padding: 64px 16px 80px;
  text-align: center;
  max-width: 520px;
  margin: 0 auto;
}

.sf-plp__quiz-empty h2 {
  margin: 0 0 12px;
  font-size: 24px;
  font-weight: 600;
  color: #1a1412;
}

.sf-plp__quiz-empty p {
  margin: 0 0 28px;
  font-size: 15px;
  line-height: 1.6;
  color: #64748b;
}
</style>
