<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter, RouterLink } from 'vue-router'
import { Icon } from '@iconify/vue'
import ProductCard from '@/components/storefront/ProductCard.vue'
import { LOAI_DA_OPTIONS } from '@/constants/loaiDa'
import { loaiChongNangLabel } from '@/constants/loaiChongNang'
import {
  fetchAllProducts,
  fetchCongDungList,
  fetchProductDetail,
  fetchProductReviews,
  fetchThanhPhanList,
} from '@/api/storefrontApi'
import { useCart } from '@/composables/useCart'
import { formatVND } from '@/utils/formatVND'
import { productImageUrl } from '@/utils/productImage'

const route = useRoute()
const router = useRouter()
const { addItem } = useCart()

const loading = ref(true)
const notFound = ref(false)
const product = ref(null)
const reviews = ref([])
const related = ref([])

const congDungList = ref([])
const thanhPhanList = ref([])

const selectedVariantId = ref(null)
const quantity = ref(1)
const activeTab = ref('moTa')
const activeImage = ref('')
const toast = ref('')

const variants = computed(() => product.value?.chiTiets?.filter((v) => v.trangThai !== false) || [])

const selectedVariant = computed(() =>
  variants.value.find((v) => v.id === selectedVariantId.value) || variants.value[0] || null,
)

const galleryImages = computed(() => {
  const urls = (product.value?.anhs || []).map((a) => a.url).filter(Boolean)
  if (!urls.length && product.value?.anhChinhUrl) urls.push(product.value.anhChinhUrl)
  return urls.length ? urls : [null]
})

const colors = computed(() => {
  const map = new Map()
  variants.value.forEach((v) => {
    if (v.tenMauSac && !map.has(v.tenMauSac)) map.set(v.tenMauSac, v)
  })
  return [...map.entries()]
})

const volumesForColor = computed(() => {
  if (!selectedVariant.value?.tenMauSac) return variants.value
  return variants.value.filter((v) => v.tenMauSac === selectedVariant.value.tenMauSac)
})

const loaiDaNames = computed(() => {
  const ids = product.value?.idLoaiDas || []
  return LOAI_DA_OPTIONS.filter((ld) => ids.includes(ld.id)).map((ld) => ld.ten)
})

const congDungNames = computed(() => {
  const ids = product.value?.idCongDungs || []
  return congDungList.value.filter((c) => ids.includes(c.id)).map((c) => c.ten)
})

const thanhPhanNames = computed(() => {
  const ids = product.value?.idThanhPhans || []
  return thanhPhanList.value.filter((t) => ids.includes(t.id)).map((t) => t.ten)
})

const avgRating = computed(() => product.value?.diemTrungBinh ?? 0)

function showToast(msg) {
  toast.value = msg
  setTimeout(() => { toast.value = '' }, 2500)
}

function selectVariant(v) {
  selectedVariantId.value = v.id
}

async function addToCart() {
  const v = selectedVariant.value
  if (!v) {
    showToast('Vui lòng chọn biến thể')
    return false
  }
  if (!v.soLuongTon || v.soLuongTon <= 0) {
    showToast('Biến thể đã hết hàng')
    return false
  }
  try {
    await addItem({
      idChiTietSanPham: v.id,
      idSanPham: product.value.id,
      tenSanPham: product.value.ten,
      tenThuongHieu: product.value.tenThuongHieu || '',
      sku: v.sku,
      giaBan: Number(v.giaBan),
      giaGoc: product.value.giaGoc ? Number(product.value.giaGoc) : null,
      soLuongTon: Number(v.soLuongTon) || 0,
      soLuong: quantity.value,
      anhUrl: activeImage.value || product.value.anhChinhUrl,
      tenMauSac: v.tenMauSac,
      dungTichMl: v.dungTichMl,
    })
    showToast('Đã thêm vào giỏ hàng')
    return true
  } catch (error) {
    showToast(typeof error === 'string' ? error : 'Không thêm được vào giỏ hàng')
    return false
  }
}

async function buyNow() {
  if (await addToCart()) {
    router.push('/gio-hang')
  }
}

function formatReviewDate(d) {
  if (!d) return ''
  return new Date(d).toLocaleDateString('vi-VN')
}

async function loadProduct(id) {
  loading.value = true
  notFound.value = false
  product.value = null
  try {
    const [detailRes, revRes, cdRes, tpRes] = await Promise.all([
      fetchProductDetail(id),
      fetchProductReviews(id),
      fetchCongDungList(),
      fetchThanhPhanList(),
    ])
    product.value = detailRes.data
    reviews.value = revRes.data || []
    congDungList.value = cdRes.data || []
    thanhPhanList.value = tpRes.data || []
    if (product.value?.chiTiets?.length) {
      selectedVariantId.value = product.value.chiTiets[0].id
    }
    activeImage.value = galleryImages.value[0] ? productImageUrl(galleryImages.value[0]) : ''

    const allRes = await fetchAllProducts()
    const sameCat = (allRes.data || []).filter(
      (p) =>
        p.id !== product.value.id &&
        p.trangThai !== false &&
        p.tenDanhMuc === product.value.tenDanhMuc,
    )
    related.value = sameCat.slice(0, 4)
  } catch {
    notFound.value = true
  } finally {
    loading.value = false
  }
}

watch(
  () => route.params.id,
  (id) => {
    if (id) loadProduct(id)
  },
)

onMounted(() => {
  loadProduct(route.params.id)
})
</script>

<template>
  <div class="sf-pdp">
    <div v-if="loading" class="sf-container sf-pdp-loading">
      <div class="sf-skeleton-pdp" />
    </div>

    <div v-else-if="notFound" class="sf-container sf-empty-state">
      <h1>Không tìm thấy sản phẩm</h1>
      <RouterLink to="/san-pham" class="btn-soleil"><span>Quay lại danh sách</span></RouterLink>
    </div>

    <template v-else-if="product">
      <div class="sf-container">
        <nav class="sf-breadcrumb">
          <RouterLink to="/">Trang chủ</RouterLink>
          <span>/</span>
          <RouterLink to="/san-pham">Sản phẩm</RouterLink>
          <span>/</span>
          <span>{{ product.ten }}</span>
        </nav>
      </div>

      <div class="sf-container sf-pdp__grid">
        <div class="sf-pdp__gallery">
          <div class="sf-pdp__main-img">
            <img :src="activeImage || productImageUrl(product.anhChinhUrl)" :alt="product.ten" />
          </div>
          <div v-if="galleryImages.length > 1" class="sf-pdp__thumbs">
            <button
              v-for="(url, idx) in galleryImages"
              :key="idx"
              type="button"
              class="sf-pdp__thumb"
              :class="{ active: activeImage === productImageUrl(url) }"
              @click="activeImage = productImageUrl(url)"
            >
              <img :src="productImageUrl(url)" :alt="`Ảnh ${idx + 1}`" />
            </button>
          </div>
        </div>

        <div class="sf-pdp__info">
          <p v-if="product.tenThuongHieu" class="sf-pdp__brand">{{ product.tenThuongHieu }}</p>
          <h1 class="sf-pdp__title">{{ product.ten }}</h1>

          <div class="sf-pdp__rating" v-if="product.soLuongDanhGia">
            <Icon v-for="i in 5" :key="i" icon="solar:star-bold" :class="{ dim: i > Math.round(avgRating) }" width="16" />
            <span>{{ avgRating.toFixed(1) }} ({{ product.soLuongDanhGia }} đánh giá)</span>
          </div>

          <p class="sf-pdp__price">{{ formatVND(selectedVariant?.giaBan) }}</p>

          <div v-if="colors.length" class="sf-pdp__option">
            <span class="sf-pdp__option-label">Màu sắc</span>
            <div class="sf-pdp__chips">
              <button
                v-for="[name, v] in colors"
                :key="name"
                type="button"
                class="sf-chip"
                :class="{ active: selectedVariant?.tenMauSac === name }"
                @click="selectVariant(v)"
              >{{ name }}</button>
            </div>
          </div>

          <div v-if="volumesForColor.length" class="sf-pdp__option">
            <span class="sf-pdp__option-label">Dung tích</span>
            <div class="sf-pdp__chips">
              <button
                v-for="v in volumesForColor"
                :key="v.id"
                type="button"
                class="sf-chip"
                :class="{ active: selectedVariantId === v.id }"
                @click="selectVariant(v)"
              >{{ v.dungTichMl }} ml</button>
            </div>
          </div>

          <p class="sf-pdp__stock">
            <template v-if="selectedVariant?.soLuongTon > 0">Còn {{ selectedVariant.soLuongTon }} sản phẩm</template>
            <template v-else class="out">Hết hàng</template>
          </p>

          <div class="sf-pdp__qty">
            <span class="sf-pdp__option-label">Số lượng</span>
            <div class="sf-qty-control">
              <button type="button" @click="quantity = Math.max(1, quantity - 1)">−</button>
              <span>{{ quantity }}</span>
              <button type="button" @click="quantity++">+</button>
            </div>
          </div>

          <div class="sf-pdp__actions">
            <button type="button" class="btn-soleil btn-soleil--block" @click="addToCart">
              <span>Thêm vào giỏ</span>
            </button>
            <button type="button" class="btn-soleil-outline btn-soleil--block" @click="buyNow">Mua ngay</button>
          </div>

          <table class="sf-spec-table">
            <tbody>
              <tr v-if="product.chiSoSpf"><td>SPF</td><td>{{ product.chiSoSpf }}</td></tr>
              <tr v-if="product.chiSoPa"><td>PA</td><td>{{ product.chiSoPa }}</td></tr>
              <tr v-if="product.loaiChongNang"><td>Loại chống nắng</td><td>{{ loaiChongNangLabel(product.loaiChongNang) }}</td></tr>
              <tr><td>Kháng nước</td><td>{{ product.khangNuoc ? 'Có' : 'Không' }}</td></tr>
              <tr v-if="product.tenDangSanPham"><td>Dạng sản phẩm</td><td>{{ product.tenDangSanPham }}</td></tr>
            </tbody>
          </table>
        </div>
      </div>

      <div class="sf-container sf-pdp__tabs">
        <div class="sf-tabs-nav">
          <button type="button" :class="{ active: activeTab === 'moTa' }" @click="activeTab = 'moTa'">Mô tả</button>
          <button type="button" :class="{ active: activeTab === 'thanhPhan' }" @click="activeTab = 'thanhPhan'">Thành phần</button>
          <button type="button" :class="{ active: activeTab === 'congDung' }" @click="activeTab = 'congDung'">Công dụng</button>
          <button type="button" :class="{ active: activeTab === 'loaiDa' }" @click="activeTab = 'loaiDa'">Loại da</button>
          <button type="button" :class="{ active: activeTab === 'danhGia' }" @click="activeTab = 'danhGia'">Đánh giá</button>
        </div>
        <div class="sf-tabs-body">
          <div v-show="activeTab === 'moTa'" class="sf-tab-panel">
            <p>{{ product.moTa || 'Chưa có mô tả chi tiết.' }}</p>
          </div>
          <div v-show="activeTab === 'thanhPhan'" class="sf-tab-panel">
            <ul v-if="thanhPhanNames.length" class="sf-tag-list">
              <li v-for="t in thanhPhanNames" :key="t">{{ t }}</li>
            </ul>
            <p v-else>Chưa có thông tin thành phần.</p>
          </div>
          <div v-show="activeTab === 'congDung'" class="sf-tab-panel">
            <ul v-if="congDungNames.length" class="sf-tag-list">
              <li v-for="c in congDungNames" :key="c">{{ c }}</li>
            </ul>
            <p v-else>Chưa có thông tin công dụng.</p>
          </div>
          <div v-show="activeTab === 'loaiDa'" class="sf-tab-panel">
            <ul v-if="loaiDaNames.length" class="sf-tag-list">
              <li v-for="ld in loaiDaNames" :key="ld">{{ ld }}</li>
            </ul>
            <p v-else>Phù hợp mọi loại da.</p>
          </div>
          <div v-show="activeTab === 'danhGia'" class="sf-tab-panel">
            <div v-if="reviews.length" class="sf-reviews">
              <div v-for="rv in reviews" :key="rv.id" class="sf-review-item">
                <div class="sf-review-stars">
                  <Icon v-for="i in 5" :key="i" icon="solar:star-bold" :class="{ dim: i > rv.soSao }" width="14" />
                </div>
                <p class="sf-review-text">{{ rv.noiDung }}</p>
                <span class="sf-review-date">{{ formatReviewDate(rv.ngayTao) }}</span>
              </div>
            </div>
            <p v-else>Chưa có đánh giá nào.</p>
          </div>
        </div>
      </div>

      <section v-if="related.length" class="sf-section sf-section--muted">
        <div class="sf-container">
          <h2 class="sf-section-title">Sản phẩm liên quan</h2>
          <div class="sf-product-grid">
            <ProductCard v-for="p in related" :key="p.id" :product="p" :show-quick-add="false" />
          </div>
        </div>
      </section>

      <div v-if="toast" class="sf-toast">{{ toast }}</div>
    </template>
  </div>
</template>
