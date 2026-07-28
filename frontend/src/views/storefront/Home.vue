<script setup>
import { computed, onMounted, ref } from 'vue'
import { RouterLink } from 'vue-router'
import { Icon } from '@iconify/vue'
import axios from 'axios'
import ProductCard from '@/components/storefront/ProductCard.vue'
import HorizontalScroll from '@/components/storefront/HorizontalScroll.vue'
import { categoryIcon, brandDisplayName } from '@/utils/categoryIcon'
import { fetchAllProducts, fetchDanhMucList, fetchThuongHieuList } from '@/api/storefrontApi'
import { productImageUrl } from '@/utils/productImage'

const loading = ref(true)
const featured = ref([])
const suggestions = ref([])
const categories = ref([])
const brands = ref([])

// --- Thời tiết & UV (cố định Hà Nội) ---
const weather = ref(null)
const isHighUvAlert = ref(false)
const weatherLoading = ref(true)

// Mức UV -> SPF khuyến nghị (đồng bộ với logic backend /weather/suggest)
const uvInfo = computed(() => {
  if (!weather.value) return null
  const uv = weather.value.uvIndex
  let level, spf, pa
  if (uv <= 2) {
    level = 'Thấp'
    spf = 'SPF 15+'
    pa = ''
  } else if (uv <= 5) {
    level = 'Trung bình'
    spf = 'SPF 30+'
    pa = ''
  } else if (uv <= 7) {
    level = 'Cao'
    spf = 'SPF 50'
    pa = 'PA+++'
  } else {
    level = 'Rất cao'
    spf = 'SPF 50+'
    pa = 'PA++++'
  }
  const percent = Math.max(0, Math.min(100, Math.round((uv / 12) * 100)))
  return { level, spf, pa, percent }
})

const weatherDesc = computed(() => {
  if (!weather.value) return ''
  const t = weather.value.temp
  if (t >= 34) return 'Nắng gắt'
  if (t >= 28) return 'Trời nắng đẹp'
  if (t >= 22) return 'Dịu mát'
  return 'Se lạnh'
})

const now = new Date()
const timeLabel = now.toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' })
const dateLabel = now.toLocaleDateString('vi-VN', { day: '2-digit', month: 'short', year: 'numeric' })

const sideBanners = [
  { title: 'Sản phẩm nổi bật', desc: 'Được chọn lọc bởi SUNOVA', to: '/san-pham?noiBat=1' },
  { title: 'Khuyến mãi', desc: 'Đợt giảm giá đang diễn ra', to: '/san-pham/khuyen-mai' },
]

const promises = [
  { icon: 'solar:verified-check-linear', title: 'Chính hãng', desc: 'Nguồn gốc rõ ràng' },
  { icon: 'solar:delivery-linear', title: 'Giao nhanh', desc: 'Toàn quốc 2–5 ngày' },
  { icon: 'solar:refresh-linear', title: 'Đổi trả', desc: 'Trong 7 ngày' },
]

async function loadWeather() {
  weatherLoading.value = true
  try {
    const res = await axios.get('http://localhost:8080/api/v1/weather/current', {
      params: { city: 'Hà Nội' },
    })
    weather.value = res.data.weather
    isHighUvAlert.value = res.data.isHighAlert
  } catch (e) {
    console.error('Lỗi tải thời tiết:', e)
  } finally {
    weatherLoading.value = false
  }
}

onMounted(async () => {
  loadWeather()
  try {
    const [prodRes, dmRes, thRes] = await Promise.all([
      fetchAllProducts(),
      fetchDanhMucList(),
      fetchThuongHieuList(),
    ])
    const active = (prodRes.data || []).filter((p) => p.trangThai !== false)
    featured.value = active.filter((p) => p.noiBat).slice(0, 8)
    if (!featured.value.length) featured.value = active.slice(0, 8)
    suggestions.value = active.slice(0, 8)
    categories.value = (dmRes.data || []).filter((d) => d.trangThai !== false)
    brands.value = (thRes.data || []).filter((b) => b.trangThai !== false).slice(0, 12)
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <div class="sf-home">
    <!-- Hero carousel + banner phụ -->
    <section class="sf-hero-split sf-container">
      <div class="sf-hero-carousel">
        <div class="sf-weather-card" v-if="!weatherLoading && weather">
          <div class="sf-weather-card__sun" aria-hidden="true" />

          <div class="sf-weather-card__top">
            <span class="sf-weather-card__location">
              <Icon icon="solar:map-point-linear" width="16" /> Hà Nội
            </span>
          </div>

          <div class="sf-weather-card__main">
            <span class="sf-weather-card__temp">{{ Math.round(weather.temp) }}°</span>
            <div class="sf-weather-card__main-meta">
              <span class="sf-weather-card__desc">{{ weatherDesc }}</span>
              <span class="sf-weather-card__time">{{ timeLabel }}</span>
              <span class="sf-weather-card__date">{{ dateLabel }}</span>
            </div>
          </div>

          <div class="sf-weather-card__stats">
            <div class="sf-weather-stat">
              <div class="sf-weather-stat__head">
                <span>Chỉ số UV</span>
                <Icon icon="solar:sun-linear" width="18" />
              </div>
              <div class="sf-weather-stat__value">{{ weather.uvIndex }}</div>
              <div class="sf-weather-stat__label">{{ uvInfo?.level }}</div>
              <div class="sf-weather-stat__bar">
                <div class="sf-weather-stat__bar-track" />
                <div class="sf-weather-stat__bar-dot" :style="{ left: uvInfo?.percent + '%' }" />
              </div>
            </div>

            <div class="sf-weather-stat">
              <div class="sf-weather-stat__head">
                <span>SPF khuyến nghị</span>
                <Icon icon="solar:shield-check-linear" width="18" />
              </div>
              <div class="sf-weather-stat__value sf-weather-stat__value--sm">{{ uvInfo?.spf }}</div>
              <div class="sf-weather-stat__label">{{ uvInfo?.pa || 'Dùng hàng ngày' }}</div>
            </div>
          </div>
        </div>

        <div class="sf-weather-card sf-weather-card--loading" v-else-if="weatherLoading">
          <span>Đang tải thời tiết Hà Nội…</span>
        </div>
      </div>
      <div class="sf-hero-side">
        <RouterLink
          v-for="banner in sideBanners"
          :key="banner.title"
          :to="banner.to"
          class="sf-hero-side__card"
        >
          <p class="sf-eyebrow sf-eyebrow--light">{{ banner.desc }}</p>
          <h2>{{ banner.title }}</h2>
        </RouterLink>
      </div>
    </section>

    <!-- Vòng tròn danh mục -->
    <section class="sf-section sf-section--tight">
      <div class="sf-container">
        <div class="sf-section-head sf-section-head--row">
          <h2 class="sf-section-title">Danh mục</h2>
          <RouterLink to="/san-pham" class="sf-link-more">Xem tất cả →</RouterLink>
        </div>
        <div v-if="loading" class="sf-circle-skeleton" />
        <HorizontalScroll v-else aria-label="Danh mục sản phẩm" :item-count="categories.length">
          <RouterLink
            v-for="cat in categories"
            :key="cat.id"
            :to="`/san-pham?danhMuc=${cat.id}`"
            class="sf-circle-item"
          >
            <span class="sf-circle-item__ring">
              <img
                v-if="cat.anhUrl || cat.urlAnh"
                :src="productImageUrl(cat.anhUrl || cat.urlAnh)"
                :alt="cat.ten"
                class="sf-circle-item__img"
              />
              <Icon v-else :icon="categoryIcon(cat.ten, cat.ma, cat.id)" width="32" />
            </span>
            <span class="sf-circle-item__label">{{ cat.ten }}</span>
          </RouterLink>
        </HorizontalScroll>
      </div>
    </section>

    <!-- Vòng tròn thương hiệu -->
    <section id="sf-brands" class="sf-section sf-section--tight sf-section--muted">
      <div class="sf-container">
        <div class="sf-section-head sf-section-head--row">
          <h2 class="sf-section-title">Thương hiệu</h2>
          <RouterLink to="/san-pham" class="sf-link-more">Xem tất cả →</RouterLink>
        </div>
        <div v-if="loading" class="sf-circle-skeleton" />
        <HorizontalScroll v-else aria-label="Thương hiệu" :item-count="brands.length">
          <RouterLink
            v-for="brand in brands"
            :key="brand.id"
            :to="`/san-pham?thuongHieu=${brand.id}`"
            class="sf-circle-item"
          >
            <span class="sf-circle-item__ring sf-circle-item__ring--brand">
              <img
                v-if="brand.logoUrl || brand.anhUrl"
                :src="productImageUrl(brand.logoUrl || brand.anhUrl)"
                :alt="brand.ten"
                class="sf-circle-item__logo"
              />
              <span v-else class="sf-circle-item__brand-text">{{ brandDisplayName(brand.ten) }}</span>
            </span>
            <span class="sf-circle-item__label">{{ brand.ten }}</span>
          </RouterLink>
        </HorizontalScroll>
      </div>
    </section>

    <!-- Sản phẩm nổi bật -->
    <section class="sf-section">
      <div class="sf-container">
        <div class="sf-section-head sf-section-head--row">
          <div>
            <p class="sf-eyebrow">Nổi bật</p>
            <h2 class="sf-section-title">Sản phẩm nổi bật</h2>
          </div>
          <RouterLink to="/san-pham?noiBat=1" class="sf-link-more">Xem tất cả →</RouterLink>
        </div>
        <div v-if="loading" class="sf-skeleton-grid" />
        <div v-else-if="featured.length" class="sf-product-grid">
          <ProductCard v-for="p in featured" :key="p.id" :product="p" />
        </div>
        <p v-else class="sf-empty-hint">Chưa có sản phẩm nổi bật.</p>
      </div>
    </section>

    <!-- Gợi ý (không có API giảm giá công khai) -->
    <section class="sf-section sf-section--muted">
      <div class="sf-container">
        <div class="sf-section-head sf-section-head--row">
          <div>
            <p class="sf-eyebrow">Gợi ý</p>
            <h2 class="sf-section-title">Có thể bạn sẽ thích</h2>
          </div>
          <RouterLink to="/san-pham" class="sf-link-more">Xem tất cả →</RouterLink>
        </div>
        <div v-if="loading" class="sf-skeleton-grid" />
        <div v-else-if="suggestions.length" class="sf-product-grid">
          <ProductCard v-for="p in suggestions" :key="`s-${p.id}`" :product="p" />
        </div>
      </div>
    </section>

    <!-- Cam kết -->
    <section class="sf-section sf-promise-strip">
      <div class="sf-container sf-promise-grid">
        <div v-for="item in promises" :key="item.title" class="sf-promise-item">
          <Icon :icon="item.icon" width="28" class="sf-promise-icon" />
          <div>
            <strong>{{ item.title }}</strong>
            <span>{{ item.desc }}</span>
          </div>
        </div>
      </div>
    </section>

    <!-- Quiz CTA -->
    <section class="sf-section">
      <div class="sf-container">
        <div class="sf-quiz-banner sf-quiz-banner--contrast">
          <div>
            <p class="sf-quiz-banner__eyebrow">Trắc nghiệm da</p>
            <h2 class="sf-quiz-banner__title">Tìm sản phẩm chống nắng phù hợp với bạn</h2>
            <p class="sf-quiz-banner__desc">Trả lời vài câu hỏi ngắn — hệ thống SUNOVA sẽ phân tích làn da và gợi ý sản phẩm hoàn hảo dành riêng cho bạn.</p>
          </div>
          <RouterLink to="/quiz" class="sf-quiz-banner__btn">Làm Quiz Ngay</RouterLink>
        </div>
      </div>
    </section>
  </div>
</template>

<style scoped>
/* --- Thẻ thời tiết + UV, lấp đầy chiều rộng khối hero --- */
.sf-hero-carousel {
  display: flex;
}

.sf-weather-card {
  position: relative;
  overflow: hidden;
  width: 100%;
  border-radius: 24px;
  padding: 28px 28px 24px;
  background: linear-gradient(135deg, #2b1a12 0%, #3d2417 55%, #5a3520 100%);
  color: #f4f1ea;
}

.sf-weather-card--loading {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 320px;
  color: rgba(244, 241, 234, 0.75);
  font-style: italic;
  background: linear-gradient(135deg, #2b1a12 0%, #4a2c1b 100%);
}

.sf-weather-card__sun {
  position: absolute;
  top: -30px;
  left: -20px;
  width: 150px;
  height: 150px;
  border-radius: 50%;
  background: radial-gradient(circle at 40% 40%, #f2b459, #d9822b 70%, rgba(217, 130, 43, 0) 100%);
  filter: blur(1px);
  z-index: 0;
}

.sf-weather-card__top,
.sf-weather-card__main,
.sf-weather-card__stats {
  position: relative;
  z-index: 1;
}

.sf-weather-card__top {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  font-size: 14px;
}

.sf-weather-card__location {
  display: flex;
  align-items: center;
  gap: 6px;
  font-weight: 600;
}

.sf-weather-card__main {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 14px;
  margin: 28px 0 30px;
}

.sf-weather-card__temp {
  font-size: 72px;
  font-weight: 300;
  line-height: 1;
}

.sf-weather-card__main-meta {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 4px;
  text-align: right;
}

.sf-weather-card__desc {
  font-size: 16px;
  font-weight: 600;
}

.sf-weather-card__time {
  font-size: 13px;
  color: rgba(244, 241, 234, 0.85);
}

.sf-weather-card__date {
  font-size: 12px;
  color: rgba(244, 241, 234, 0.6);
}

.sf-weather-card__stats {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

.sf-weather-stat {
  background: rgba(244, 241, 234, 0.94);
  color: #2b1a12;
  border-radius: 18px;
  padding: 16px 18px;
}

.sf-weather-stat__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 13px;
  color: #8a7259;
}

.sf-weather-stat__value {
  font-size: 28px;
  font-weight: 700;
  margin-top: 8px;
}

.sf-weather-stat__value--sm {
  font-size: 20px;
}

.sf-weather-stat__label {
  font-size: 13px;
  color: #8a7259;
  margin-top: 2px;
}

.sf-weather-stat__bar {
  position: relative;
  margin-top: 12px;
  height: 6px;
}

.sf-weather-stat__bar-track {
  position: absolute;
  inset: 0;
  border-radius: 999px;
  background: linear-gradient(90deg, #22c55e, #eab308, #f97316, #dc2626);
}

.sf-weather-stat__bar-dot {
  position: absolute;
  top: 50%;
  width: 14px;
  height: 14px;
  border-radius: 50%;
  background: #f4f1ea;
  border: 2px solid #2b1a12;
  transform: translate(-50%, -50%);
}

@media (max-width: 640px) {
  .sf-weather-card__temp {
    font-size: 56px;
  }

  .sf-weather-card__stats {
    grid-template-columns: 1fr;
  }
}
</style>