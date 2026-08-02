<script setup>
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { RouterLink } from 'vue-router'
import { Icon } from '@iconify/vue'
import axios from 'axios'
import ProductCard from '@/components/storefront/ProductCard.vue'
import HorizontalScroll from '@/components/storefront/HorizontalScroll.vue'
import { categoryIcon, brandDisplayName } from '@/utils/categoryIcon'
import { fetchAllProducts, fetchDanhMucList, fetchThuongHieuList } from '@/api/storefrontApi'
import { fetchActiveBanners } from '@/api/bannerApi'
import { productImageUrl } from '@/utils/productImage'
import { rankProductsByQuiz, resolveQuizProfile } from '@/utils/quizRecommend'

const loading = ref(true)
const featured = ref([])
const suggestions = ref([])
const quizSuggestions = ref([])
const quizSkinName = ref('')
const homeBanners = ref([])
const categories = ref([])
const brands = ref([])

const DEFAULT_QUIZ_BANNER = {
  id: 'fallback-quiz',
  tieuDe: 'Trắc nghiệm da',
  tieuDeChinh: 'Tìm sản phẩm chống nắng phù hợp với bạn',
  moTa: 'Trả lời vài câu hỏi ngắn — hệ thống SUNOVA sẽ phân tích làn da và gợi ý sản phẩm hoàn hảo dành riêng cho bạn.',
  nutText: 'Làm Quiz Ngay',
  linkUrl: '/quiz',
  anhUrl: null,
}

function isExternalLink(url) {
  return /^https?:\/\//i.test(String(url || ''))
}

function bannerImageUrl(url) {
  if (!url) return ''
  return productImageUrl(url)
}

function bannerBgStyle(banner) {
  if (!banner?.anhUrl) return undefined
  return {
    backgroundImage: `linear-gradient(90deg, rgba(36,26,18,0.92) 0%, rgba(36,26,18,0.72) 55%, rgba(36,26,18,0.45) 100%), url(${bannerImageUrl(banner.anhUrl)})`,
    backgroundSize: 'cover',
    backgroundPosition: 'center',
  }
}

// --- Carousel banner ở vùng hero ---
const BANNER_INTERVAL_MS = 5000
const currentBannerIndex = ref(0)
let bannerTimer = null

const activeBanner = computed(() => homeBanners.value[currentBannerIndex.value] || null)
const hasMultipleBanners = computed(() => homeBanners.value.length > 1)

function goToBanner(index) {
  const total = homeBanners.value.length
  if (!total) return
  currentBannerIndex.value = ((index % total) + total) % total
  startAutoplay()
}

function nextBanner() {
  goToBanner(currentBannerIndex.value + 1)
}

function prevBanner() {
  goToBanner(currentBannerIndex.value - 1)
}

function startAutoplay() {
  stopAutoplay()
  if (homeBanners.value.length > 1) {
    bannerTimer = setInterval(() => {
      currentBannerIndex.value = (currentBannerIndex.value + 1) % homeBanners.value.length
    }, BANNER_INTERVAL_MS)
  }
}

function stopAutoplay() {
  if (bannerTimer) {
    clearInterval(bannerTimer)
    bannerTimer = null
  }
}

watch(
  () => homeBanners.value.length,
  () => {
    currentBannerIndex.value = 0
    startAutoplay()
  },
)

onBeforeUnmount(stopAutoplay)

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
    const [prodRes, dmRes, thRes, quizProfile, bannerRes] = await Promise.all([
      fetchAllProducts(),
      fetchDanhMucList(),
      fetchThuongHieuList(),
      resolveQuizProfile(),
      fetchActiveBanners().catch(() => ({ data: [] })),
    ])
    const active = (prodRes.data || []).filter((p) => p.trangThai !== false)
    featured.value = active.filter((p) => p.noiBat).slice(0, 8)
    if (!featured.value.length) featured.value = active.slice(0, 8)
    suggestions.value = active.slice(0, 8)
    categories.value = (dmRes.data || []).filter((d) => d.trangThai !== false)
    brands.value = (thRes.data || []).filter((b) => b.trangThai !== false).slice(0, 12)
    const fromApi = Array.isArray(bannerRes.data) ? bannerRes.data : []
    homeBanners.value = fromApi.length ? fromApi : [DEFAULT_QUIZ_BANNER]

    if (quizProfile) {
      quizSkinName.value = quizProfile.tenLoaiDa || ''
      quizSuggestions.value = rankProductsByQuiz(active, {
        scoreMap: quizProfile.scoreMap,
        filters: quizProfile.filters,
      }).slice(0, 8)
    } else {
      quizSuggestions.value = []
      quizSkinName.value = ''
    }
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
      <div
        v-if="activeBanner"
        class="sf-hero-banner-wrap"
        @mouseenter="stopAutoplay"
        @mouseleave="startAutoplay"
      >
        <transition name="sf-banner-fade" mode="out-in">
          <component
            :is="isExternalLink(activeBanner.linkUrl) ? 'a' : RouterLink"
            :key="activeBanner.id"
            class="sf-hero-banner"
            :href="isExternalLink(activeBanner.linkUrl) ? activeBanner.linkUrl : undefined"
            :target="isExternalLink(activeBanner.linkUrl) ? '_blank' : undefined"
            :rel="isExternalLink(activeBanner.linkUrl) ? 'noopener noreferrer' : undefined"
            :to="isExternalLink(activeBanner.linkUrl) ? undefined : (activeBanner.linkUrl || '/')"
            :style="bannerBgStyle(activeBanner)"
          >
            <div class="sf-hero-banner__content">
              <p v-if="activeBanner.tieuDe" class="sf-hero-banner__eyebrow">{{ activeBanner.tieuDe }}</p>
              <h2 class="sf-hero-banner__title">{{ activeBanner.tieuDeChinh }}</h2>
              <p v-if="activeBanner.moTa" class="sf-hero-banner__desc">{{ activeBanner.moTa }}</p>
              <span class="sf-hero-banner__btn">{{ activeBanner.nutText || 'Xem ngay' }}</span>
            </div>
          </component>
        </transition>

        <template v-if="hasMultipleBanners">
          <button
            type="button"
            class="sf-hero-nav sf-hero-nav--prev"
            aria-label="Banner trước"
            @click="prevBanner"
          >
            <Icon icon="solar:alt-arrow-left-linear" width="22" />
          </button>
          <button
            type="button"
            class="sf-hero-nav sf-hero-nav--next"
            aria-label="Banner sau"
            @click="nextBanner"
          >
            <Icon icon="solar:alt-arrow-right-linear" width="22" />
          </button>
          <div class="sf-hero-dots">
            <button
              v-for="(banner, i) in homeBanners"
              :key="banner.id"
              type="button"
              class="sf-hero-dot"
              :class="{ 'sf-hero-dot--active': i === currentBannerIndex }"
              :aria-label="`Chuyển tới banner ${i + 1}`"
              @click="goToBanner(i)"
            />
          </div>
        </template>
      </div>
      <div v-else class="sf-hero-placeholder" aria-hidden="true" />

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

    <!-- Gợi ý theo kết quả quiz (chỉ hiện khi đã làm quiz) -->
    <section v-if="!loading && quizSuggestions.length" class="sf-section">
      <div class="sf-container">
        <div class="sf-section-head sf-section-head--row">
          <div>
            <p class="sf-eyebrow">Theo quiz da</p>
            <h2 class="sf-section-title">
              Sản phẩm gợi ý{{ quizSkinName ? ` — ${quizSkinName}` : '' }}
            </h2>
          </div>
          <RouterLink to="/san-pham/goi-y" class="sf-link-more">Xem tất cả →</RouterLink>
        </div>
        <div class="sf-product-grid">
          <ProductCard v-for="p in quizSuggestions" :key="`q-${p.id}`" :product="p" />
        </div>
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

  </div>
</template>

<style scoped>
/* Carousel banner ở vùng hero (cột trái) — thay cho ô trống cũ */
.sf-hero-banner-wrap {
  position: relative;
  min-height: 380px;
  height: 100%;
  border-radius: 14px;
}

.sf-banner-fade-enter-active,
.sf-banner-fade-leave-active {
  transition: opacity 0.45s ease;
}

.sf-banner-fade-enter-from,
.sf-banner-fade-leave-to {
  opacity: 0;
}

.sf-hero-banner {
  position: relative;
  display: flex;
  align-items: flex-end;
  min-height: 380px;
  height: 100%;
  padding: 2.25rem;
  border-radius: 14px;
  overflow: hidden;
  text-decoration: none;
  background: linear-gradient(150deg, #241a12 0%, #3a2a1c 55%, #4a3422 100%);
  background-size: cover;
  background-position: center;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.sf-hero-banner:hover {
  transform: translateY(-2px);
  box-shadow: 0 12px 30px rgba(36, 26, 18, 0.18);
}

.sf-hero-banner__content {
  position: relative;
  max-width: 560px;
}

.sf-hero-banner__eyebrow {
  margin: 0 0 0.6rem;
  font-size: 0.7rem;
  font-weight: 600;
  letter-spacing: 0.12em;
  text-transform: uppercase;
  color: var(--sf-gold, #c9a96e);
}

.sf-hero-banner__title {
  margin: 0 0 0.6rem;
  font-family: var(--sf-font-display, 'Playfair Display', serif);
  font-size: clamp(1.6rem, 2.6vw, 2.4rem);
  font-weight: 600;
  line-height: 1.2;
  color: var(--sf-cream, #f9f5f0);
}

.sf-hero-banner__desc {
  margin: 0 0 1.4rem;
  color: rgba(249, 245, 240, 0.8);
  line-height: 1.7;
}

.sf-hero-banner__btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 0.85rem 1.75rem;
  border-radius: 8px;
  background: var(--sf-gold, #c9a96e);
  color: var(--sf-espresso, #241a12);
  font-size: 0.85rem;
  font-weight: 600;
  transition: background 0.2s ease, color 0.2s ease;
}

.sf-hero-banner:hover .sf-hero-banner__btn {
  background: var(--sf-gold-dark, #9e7340);
  color: #fff;
}

/* Nút điều hướng trái/phải */
.sf-hero-nav {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  z-index: 2;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 2.5rem;
  height: 2.5rem;
  border: none;
  border-radius: 50%;
  cursor: pointer;
  color: var(--sf-espresso, #241a12);
  background: rgba(249, 245, 240, 0.85);
  box-shadow: 0 2px 8px rgba(36, 26, 18, 0.18);
  transition: background 0.2s ease, transform 0.2s ease;
}

.sf-hero-nav:hover {
  background: #fff;
  transform: translateY(-50%) scale(1.06);
}

.sf-hero-nav--prev {
  left: 0.9rem;
}

.sf-hero-nav--next {
  right: 0.9rem;
}

/* Chấm điều hướng */
.sf-hero-dots {
  position: absolute;
  left: 50%;
  bottom: 1rem;
  transform: translateX(-50%);
  z-index: 2;
  display: flex;
  gap: 0.5rem;
}

.sf-hero-dot {
  width: 9px;
  height: 9px;
  padding: 0;
  border: none;
  border-radius: 50%;
  cursor: pointer;
  background: rgba(249, 245, 240, 0.5);
  transition: background 0.2s ease, transform 0.2s ease;
}

.sf-hero-dot:hover {
  background: rgba(249, 245, 240, 0.8);
}

.sf-hero-dot--active {
  background: var(--sf-gold, #c9a96e);
  transform: scale(1.25);
}

@media (max-width: 991px) {
  .sf-hero-banner-wrap {
    min-height: 220px;
  }
  .sf-hero-banner {
    min-height: 220px;
    padding: 1.5rem;
  }
  .sf-hero-nav {
    width: 2.1rem;
    height: 2.1rem;
  }
}
</style>
