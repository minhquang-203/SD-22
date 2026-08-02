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

const blogBanner = {
  title: 'Chưa biết chọn kem chống nắng?',
  desc: 'Cẩm nang SUNOVA — hiểu SPF, PA và chọn đúng cho da bạn',
  to: '/blog',
}

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
    <!-- Hero: placeholder trái + 2 card phải -->
    <section class="sf-hero-split sf-container">
      <div class="sf-hero-placeholder" aria-hidden="true" />

      <div class="sf-hero-side">
        <RouterLink :to="blogBanner.to" class="sf-hero-side__card sf-hero-side__card--blog">
          <span class="sf-hero-side__blog-icon" aria-hidden="true">
            <Icon icon="solar:notebook-bookmark-bold-duotone" width="22" />
          </span>
          <p class="sf-hero-side__kicker">{{ blogBanner.desc }}</p>
          <h2 class="sf-hero-side__title">{{ blogBanner.title }}</h2>
          <span class="sf-hero-side__cta">
            Đọc blog <span aria-hidden="true">→</span>
          </span>
        </RouterLink>

        <div
          class="sf-hero-side__card sf-hero-side__card--weather"
          aria-live="polite"
          :aria-busy="weatherLoading"
        >
          <div class="sf-hero-wx">
            <div class="sf-hero-wx__top">
              <span class="sf-hero-wx__loc">
                <Icon icon="solar:map-point-bold" width="14" />
                Hà Nội
              </span>
              <span
                class="sf-hero-wx__sun"
                :class="{ 'sf-hero-wx__sun--muted': weatherLoading || !weather }"
                aria-hidden="true"
              >
                <Icon
                  :icon="!weatherLoading && weather ? 'solar:sun-bold' : 'solar:sun-linear'"
                  width="28"
                />
              </span>
            </div>

            <!-- Skeleton: khớp vị trí temp / desc / meta -->
            <div v-if="weatherLoading" class="sf-hero-wx__body sf-hero-wx__body--skel" key="skel">
              <span class="sf-skel sf-skel--temp" />
              <span class="sf-skel sf-skel--desc" />
              <div class="sf-hero-wx__meta sf-hero-wx__meta--skel">
                <span class="sf-skel sf-skel--chip" />
                <span class="sf-skel sf-skel--chip sf-skel--chip-sm" />
              </div>
            </div>

            <!-- Data -->
            <div
              v-else-if="weather && uvInfo"
              class="sf-hero-wx__body sf-hero-wx__body--fade"
              key="data"
            >
              <div class="sf-hero-wx__temp">{{ Math.round(weather.temp) }}°</div>
              <p class="sf-hero-wx__desc">{{ weatherDesc }}</p>
              <div class="sf-hero-wx__meta">
                <span>UV {{ weather.uvIndex }} · {{ uvInfo.level }}</span>
                <span v-if="uvInfo.spf">{{ uvInfo.spf }}{{ uvInfo.pa ? ` ${uvInfo.pa}` : '' }}</span>
              </div>
            </div>

            <!-- Lỗi / không data -->
            <div v-else class="sf-hero-wx__body sf-hero-wx__body--error" key="err">
              <p class="sf-hero-wx__error">Không tải được thời tiết</p>
            </div>
          </div>
        </div>
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
