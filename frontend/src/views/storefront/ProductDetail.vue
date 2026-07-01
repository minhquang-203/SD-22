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
import { formatDiscountPercent, formatVND } from '@/utils/formatVND'
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

const hasVariantSale = computed(() => selectedVariant.value?.giaSauGiam != null)

const variantSaleLabel = computed(() => formatDiscountPercent(selectedVariant.value?.phanTramGiam))

const variantOriginalPrice = computed(() => {
  const v = selectedVariant.value
  if (!v) return ''
  return formatVND(v.giaGoc ?? v.giaBan)
})

const variantSalePrice = computed(() => formatVND(selectedVariant.value?.giaSauGiam))

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
    const originalPrice = Number(v.giaGoc ?? v.giaBan)
    const sellingPrice = v.giaSauGiam != null ? Number(v.giaSauGiam) : originalPrice
    await addItem({
      idChiTietSanPham: v.id,
      idSanPham: product.value.id,
      tenSanPham: product.value.ten,
      tenThuongHieu: product.value.tenThuongHieu || '',
      sku: v.sku,
      giaBan: sellingPrice,
      giaGoc: v.giaSauGiam != null ? originalPrice : null,
      phanTramGiam: v.phanTramGiam ?? null,
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

function maskCustomerName(name) {
  if (!name) return 'Khách hàng'
  if (name.startsWith('khachhang_')) return name
  const n = name.trim()
  if (n.length <= 1) return n + '***'
  const first = n.charAt(0)
  const last = n.charAt(n.length - 1)
  return `${first}***${last}`
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

let intervalId = null;

onMounted(() => {
  loadProduct(route.params.id)
  intervalId = setInterval(async () => {
    if (route.params.id) {
      const res = await fetchProductReviews(route.params.id)
      reviews.value = res.data || []
    }
  }, 10000)
})

import { onUnmounted } from 'vue'
onUnmounted(() => {
  if (intervalId) clearInterval(intervalId)
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

          <div v-if="product.soLuongDanhGia" class="sf-pdp__rating">
            <Icon v-for="i in 5" :key="i" icon="solar:star-bold" :class="{ dim: i > Math.round(avgRating) }" width="16" />
            <span>{{ avgRating.toFixed(1) }} ({{ product.soLuongDanhGia }} đánh giá)</span>
          </div>

          <div v-if="hasVariantSale" class="sf-pdp__price-wrap">
            <span v-if="variantSaleLabel" class="sf-pdp__sale-badge">{{ variantSaleLabel }}</span>
            <div class="sf-pdp__prices">
              <p class="sf-pdp__price sf-pdp__price--sale">{{ variantSalePrice }}</p>
              <p class="sf-pdp__price sf-pdp__price--original">{{ variantOriginalPrice }}</p>
            </div>
          </div>
          <p v-else class="sf-pdp__price">{{ formatVND(selectedVariant?.giaBan) }}</p>

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
              <div v-for="rv in reviews" :key="rv.id" class="sf-review-card">
                <div class="sf-review-avatar">
                  <Icon icon="solar:user-circle-bold" width="36" />
                </div>
                <div class="sf-review-main">
                  <div class="sf-review-name">{{ maskCustomerName(rv.tenKhachHang || ('khachhang_' + rv.idKhachHang)) }}</div>
                  <div class="sf-review-stars">
                    <Icon v-for="i in 5" :key="i" icon="solar:star-bold" :class="{ dim: i > rv.soSao }" width="14" />
                  </div>
                  <div class="sf-review-date-variant">
                    <span>{{ formatReviewDate(rv.ngayTao) }}</span>
                    <span v-if="rv.tenPhanLoaiHang" class="divider">|</span>
                    <span v-if="rv.tenPhanLoaiHang">Phân loại hàng: {{ rv.tenPhanLoaiHang }}</span>
                  </div>
                  <p class="sf-review-text">{{ rv.noiDung }}</p>
                  
                  <div class="sf-review-media" v-if="rv.hinhAnhVideo">
                    <img :src="rv.hinhAnhVideo" alt="Hình ảnh đánh giá" onerror="this.style.display='none'" />
                  </div>
                  
                  <div class="sf-shop-reply" v-if="rv.phanHoiCuaShop">
                    <div class="reply-header">Phản hồi của Người Bán</div>
                    <div class="reply-content">{{ rv.phanHoiCuaShop }}</div>
                  </div>
                  
                  <div class="sf-review-actions">
                    <button class="sf-btn-like">
                      <Icon icon="solar:like-bold" width="16" /> Hữu ích ({{ rv.soLuotThich || 0 }})
                    </button>
                  </div>
                </div>
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

<style scoped>
.sf-pdp { padding-bottom: 60px; }
.sf-pdp-loading { text-align: center; padding: 100px 20px; color: #64748b; font-size: 1.1rem; }
.sf-pdp__grid { display: grid; grid-template-columns: 1fr; gap: 32px; padding-top: 32px; padding-bottom: 48px; }
@media (min-width: 768px) { .sf-pdp__grid { grid-template-columns: 1fr 1fr; gap: 48px; } }
.sf-pdp__main-img { width: 100%; aspect-ratio: 4/5; background: #f8fafc; border-radius: 8px; overflow: hidden; }
.sf-pdp__main-img img { width: 100%; height: 100%; object-fit: cover; }
.sf-pdp__thumbs { display: flex; gap: 12px; margin-top: 16px; overflow-x: auto; padding-bottom: 4px; }
.sf-pdp__thumb { width: 80px; height: 80px; border-radius: 6px; overflow: hidden; border: 2px solid transparent; cursor: pointer; flex-shrink: 0; }
.sf-pdp__thumb.active { border-color: #0f172a; }
.sf-pdp__thumb img { width: 100%; height: 100%; object-fit: cover; }
.sf-pdp__brand { font-size: 13px; text-transform: uppercase; letter-spacing: 1px; color: #64748b; margin-bottom: 8px; }
.sf-pdp__title { font-size: 24px; font-weight: 400; color: #0f172a; margin-bottom: 16px; line-height: 1.3; }
.sf-pdp__rating { display: flex; align-items: center; gap: 4px; color: #eab308; margin-bottom: 24px; font-size: 14px; }
.sf-pdp__rating span { color: #64748b; margin-left: 8px; }
.sf-pdp__rating .dim { color: #e2e8f0; }
.sf-pdp__price { font-size: 28px; font-weight: 500; color: #0f172a; margin-bottom: 32px; }
.sf-pdp__option { margin-bottom: 24px; }
.sf-pdp__option-label { display: block; font-size: 14px; font-weight: 500; margin-bottom: 12px; color: #334155; }
.sf-pdp__chips { display: flex; flex-wrap: wrap; gap: 12px; }
.sf-chip { padding: 8px 16px; border: 1px solid #cbd5e1; border-radius: 4px; background: white; color: #334155; font-size: 14px; cursor: pointer; transition: all 0.2s; }
.sf-chip:hover { border-color: #94a3b8; }
.sf-chip.active { border-color: #0f172a; background: #0f172a; color: white; }
.sf-pdp__stock { font-size: 14px; color: #059669; margin-bottom: 32px; }
.sf-pdp__stock.out { color: #ef4444; }
.sf-pdp__actions { display: flex; flex-direction: column; gap: 16px; }
.sf-pdp__qty { display: flex; align-items: center; border: 1px solid #cbd5e1; border-radius: 4px; width: max-content; }
.sf-pdp__qty button { width: 40px; height: 48px; background: transparent; border: none; font-size: 18px; cursor: pointer; }
.sf-pdp__qty input { width: 50px; height: 48px; border: none; text-align: center; font-size: 16px; }
.sf-pdp__qty input:focus { outline: none; }
.sf-pdp__buttons { display: grid; grid-template-columns: 1fr; gap: 16px; }
@media (min-width: 640px) { .sf-pdp__buttons { grid-template-columns: 1fr 1fr; } }
.sf-pdp__meta { margin-top: 48px; padding-top: 32px; border-top: 1px solid #f1f5f9; }
.sf-pdp__meta-row { display: grid; grid-template-columns: 140px 1fr; padding: 12px 0; border-bottom: 1px solid #f8fafc; font-size: 14px; }
.sf-pdp__meta-label { color: #64748b; }
.sf-pdp__meta-val { color: #0f172a; }
.sf-tabs-nav { display: flex; gap: 32px; border-bottom: 1px solid #e2e8f0; margin-bottom: 32px; overflow-x: auto; }
.sf-tabs-nav button { background: transparent; border: none; padding: 16px 0; font-size: 15px; font-weight: 500; color: #64748b; cursor: pointer; position: relative; white-space: nowrap; }
.sf-tabs-nav button.active { color: #0f172a; }
.sf-tabs-nav button.active::after { content: ''; position: absolute; bottom: -1px; left: 0; right: 0; height: 2px; background: #c9a96e; }
.sf-tab-panel { font-size: 15px; line-height: 1.6; color: #334155; animation: fadeIn 0.3s ease; }
.sf-tag-list { list-style: none; padding: 0; display: flex; flex-wrap: wrap; gap: 12px; }
.sf-tag-list li { background: #f1f5f9; padding: 6px 16px; border-radius: 20px; font-size: 14px; }

/* CUSTOMER REVIEWS STYLING */
.sf-review-card { display: flex; gap: 16px; padding: 24px 0; border-bottom: 1px solid #f1f5f9; }
.sf-review-avatar { color: #cbd5e1; }
.sf-review-main { flex: 1; }
.sf-review-name { font-weight: 600; font-size: 14px; color: #0f172a; margin-bottom: 4px; }
.sf-review-stars { color: #eab308; margin-bottom: 8px; }
.sf-review-stars .dim { color: #e2e8f0; }
.sf-review-date-variant { font-size: 12px; color: #94a3b8; margin-bottom: 12px; }
.sf-review-date-variant .divider { margin: 0 8px; }
.sf-review-text { font-size: 14px; line-height: 1.6; color: #334155; margin-bottom: 16px; }
.sf-review-media img { width: 120px; height: 120px; object-fit: cover; border-radius: 8px; border: 1px solid #f1f5f9; margin-bottom: 16px; }
.sf-shop-reply { background: #f8fafc; padding: 16px; border-radius: 8px; margin-bottom: 16px; }
.sf-shop-reply .reply-header { font-weight: 600; font-size: 13px; color: #475569; margin-bottom: 8px; }
.sf-shop-reply .reply-content { font-size: 14px; color: #334155; }
.sf-btn-like { background: transparent; border: none; display: inline-flex; align-items: center; gap: 8px; color: #64748b; font-size: 13px; cursor: pointer; padding: 0; }
.sf-btn-like:hover { color: #0f172a; }

@keyframes fadeIn { from { opacity: 0; transform: translateY(5px); } to { opacity: 1; transform: translateY(0); } }
.sf-section-title { text-align: center; font-size: 28px; font-weight: 400; color: #0f172a; margin-bottom: 48px; }
.sf-product-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 20px; }
@media (min-width: 768px) { .sf-product-grid { grid-template-columns: repeat(4, 1fr); gap: 32px; } }
</style>
