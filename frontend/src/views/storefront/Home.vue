<script setup>
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { RouterLink } from 'vue-router'
import { Icon } from '@iconify/vue'
import axios from 'axios'
import ProductCard from '@/components/storefront/ProductCard.vue'
import { fetchAllProducts, fetchDanhMucList } from '@/api/storefrontApi'
import { fetchActiveBanners } from '@/api/bannerApi'
import { productImageUrl } from '@/utils/productImage'
import { rankProductsByQuiz, resolveQuizProfile } from '@/utils/quizRecommend'

const DEFAULT_HERO = {
  id: 'fallback-hero',
  tieuDe: '',
  tieuDeChinh: 'Sunova',
  moTa: 'Mang đến trải nghiệm chống nắng cá nhân hóa theo loại da của bạn',
  nutText: 'Xem sản phẩm',
  linkUrl: '/san-pham',
  anhUrl: '/hero-banner.png',
  isFallback: true,
}

const loading = ref(true)
const featured = ref([])
const suggestions = ref([])
const quizSuggestions = ref([])
const quizSkinName = ref('')
const categories = ref([])
const homeBanners = ref([DEFAULT_HERO])

const BANNER_INTERVAL_MS = 5000
const currentBannerIndex = ref(0)
let bannerTimer = null

const activeBanner = computed(() => homeBanners.value[currentBannerIndex.value] || DEFAULT_HERO)
const hasMultipleBanners = computed(() => homeBanners.value.length > 1)
const isFallbackHero = computed(() => !!activeBanner.value?.isFallback)

function isExternalLink(url) {
  return /^https?:\/\//i.test(String(url || ''))
}

function bannerImageSrc(banner) {
  const url = banner?.anhUrl
  if (!url || url === '/hero-banner.png') return '/hero-banner.png'
  return productImageUrl(url)
}

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

const weather = ref(null)
const isHighUvAlert = ref(false)
const weatherLoading = ref(true)

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
  return { level, spf, pa }
})

const weatherDesc = computed(() => {
  if (!weather.value) return ''
  const t = weather.value.temp
  if (t >= 34) return 'Nắng gắt'
  if (t >= 28) return 'Trời nắng đẹp'
  if (t >= 22) return 'Dịu mát'
  return 'Se lạnh'
})

const showUvChip = computed(() => {
  if (!uvInfo.value) return false
  return isHighUvAlert.value || (weather.value?.uvIndex ?? 0) >= 6
})

const trustItems = [
  { title: 'Chính hãng 100%', desc: 'Nguồn gốc rõ ràng, hóa đơn đầy đủ' },
  { title: 'Giao 2–5 ngày', desc: 'Toàn quốc, theo dõi đơn realtime' },
  { title: 'Đổi trả 7 ngày', desc: 'Sản phẩm còn nguyên seal' },
  { title: 'Tư vấn theo da', desc: 'Quiz + chat hỗ trợ khi cần' },
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
    const [prodRes, dmRes, quizProfile, bannerRes] = await Promise.all([
      fetchAllProducts(),
      fetchDanhMucList(),
      resolveQuizProfile(),
      fetchActiveBanners().catch(() => ({ data: [] })),
    ])
    const active = (prodRes.data || []).filter((p) => p.trangThai !== false)
    featured.value = active.filter((p) => p.noiBat).slice(0, 8)
    if (!featured.value.length) featured.value = active.slice(0, 8)
    suggestions.value = active.slice(0, 8)
    categories.value = (dmRes.data || []).filter((d) => d.trangThai !== false)
    const fromApi = Array.isArray(bannerRes.data) ? bannerRes.data : []
    homeBanners.value = fromApi.length ? fromApi : [DEFAULT_HERO]

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
    <section
      class="sf-home-hero"
      aria-label="Giới thiệu"
      @mouseenter="stopAutoplay"
      @mouseleave="startAutoplay"
    >
      <div class="sf-home-hero__media" aria-hidden="true">
        <transition name="sf-home-hero-fade" mode="out-in">
          <img
            :key="activeBanner.id"
            :src="bannerImageSrc(activeBanner)"
            alt=""
          />
        </transition>
      </div>
      <div class="sf-container sf-home-hero__inner">
        <div class="sf-home-hero__content">
          <template v-if="isFallbackHero">
            <h1 class="sf-home-hero__brand">Sunova</h1>
            <p class="sf-home-hero__line">{{ activeBanner.moTa }}</p>
            <div class="sf-home-hero__actions">
              <RouterLink to="/san-pham" class="sf-home-btn sf-home-btn--solid">Xem sản phẩm</RouterLink>
              <RouterLink to="/quiz" class="sf-home-btn sf-home-btn--ghost">Làm quiz 2 phút</RouterLink>
            </div>
          </template>
          <template v-else>
            <h2 class="sf-home-hero__title">{{ activeBanner.tieuDeChinh }}</h2>
            <p v-if="activeBanner.tieuDe" class="sf-home-hero__line">{{ activeBanner.tieuDe }}</p>
            <p v-if="activeBanner.moTa" class="sf-home-hero__line">{{ activeBanner.moTa }}</p>
            <div class="sf-home-hero__actions">
              <component
                :is="isExternalLink(activeBanner.linkUrl) ? 'a' : RouterLink"
                class="sf-home-btn sf-home-btn--solid"
                :href="isExternalLink(activeBanner.linkUrl) ? activeBanner.linkUrl : undefined"
                :target="isExternalLink(activeBanner.linkUrl) ? '_blank' : undefined"
                :rel="isExternalLink(activeBanner.linkUrl) ? 'noopener noreferrer' : undefined"
                :to="isExternalLink(activeBanner.linkUrl) ? undefined : (activeBanner.linkUrl || '/')"
              >
                {{ activeBanner.nutText || 'Xem ngay' }}
              </component>
              <RouterLink to="/quiz" class="sf-home-btn sf-home-btn--ghost">Làm quiz 2 phút</RouterLink>
            </div>
          </template>
        </div>
      </div>

      <template v-if="hasMultipleBanners">
        <button type="button" class="sf-home-hero__nav sf-home-hero__nav--prev" aria-label="Banner trước" @click="prevBanner">
          <Icon icon="solar:alt-arrow-left-linear" width="22" />
        </button>
        <button type="button" class="sf-home-hero__nav sf-home-hero__nav--next" aria-label="Banner sau" @click="nextBanner">
          <Icon icon="solar:alt-arrow-right-linear" width="22" />
        </button>
        <div class="sf-home-hero__dots">
          <button
            v-for="(banner, i) in homeBanners"
            :key="banner.id"
            type="button"
            class="sf-home-hero__dot"
            :class="{ 'is-active': i === currentBannerIndex }"
            :aria-label="`Chuyển tới banner ${i + 1}`"
            @click="goToBanner(i)"
          />
        </div>
      </template>
    </section>

    <div class="sf-uvbar">
      <div class="sf-container sf-uvbar__row">
        <div class="sf-uvbar__left">
          <div class="sf-uvbar__city">
            <Icon icon="solar:map-point-bold" width="14" />
            Hà Nội hôm nay
          </div>
          <template v-if="weatherLoading">
            <span class="sf-uvbar__skel sf-uvbar__skel--temp" />
            <span class="sf-uvbar__skel sf-uvbar__skel--meta" />
          </template>
          <template v-else-if="weather && uvInfo">
            <div class="sf-uvbar__temp">{{ Math.round(weather.temp) }}°</div>
            <div class="sf-uvbar__meta">{{ weatherDesc }} · UV {{ weather.uvIndex }}</div>
            <div v-if="showUvChip" class="sf-uvbar__chip">
              <span />
              Nên dùng {{ uvInfo.spf }}{{ uvInfo.pa ? ` ${uvInfo.pa}` : '' }}
            </div>
          </template>
          <div v-else class="sf-uvbar__meta">Không tải được thời tiết</div>
        </div>
        <RouterLink to="/san-pham" class="sf-home-more">Xem gợi ý theo UV →</RouterLink>
      </div>
    </div>

    <section v-if="loading || categories.length" class="sf-cats" aria-label="Danh mục">
      <div v-if="loading" class="sf-container sf-cats__skel" />
      <div v-else class="sf-container sf-cats__row">
        <RouterLink
          v-for="cat in categories"
          :key="cat.id"
          :to="`/san-pham?danhMuc=${cat.id}`"
          class="sf-cat"
        >
          <span class="sf-cat__label">{{ cat.ten }}</span>
          <span v-if="cat.moTa" class="sf-cat__sub">{{ cat.moTa }}</span>
        </RouterLink>
      </div>
    </section>

    <section class="sf-section">
      <div class="sf-container">
        <div class="sf-section-head sf-section-head--row">
          <div>
            <h2 class="sf-section-title">Sản phẩm nổi bật</h2>
            <p class="sf-home-sub">Chính hãng · Có SPF / PA rõ ràng trên từng sản phẩm</p>
          </div>
          <RouterLink to="/san-pham?noiBat=1" class="sf-home-more">Xem tất cả →</RouterLink>
        </div>
        <div v-if="loading" class="sf-skeleton-grid" />
        <div v-else-if="featured.length" class="sf-product-grid">
          <ProductCard v-for="p in featured" :key="p.id" :product="p" />
        </div>
        <p v-else class="sf-empty-hint">Chưa có sản phẩm nổi bật.</p>
      </div>
    </section>

    <section v-if="!loading && quizSuggestions.length" class="sf-section">
      <div class="sf-container">
        <div class="sf-section-head sf-section-head--row">
          <div>
            <h2 class="sf-section-title">
              Sản phẩm gợi ý{{ quizSkinName ? ` — ${quizSkinName}` : '' }}
            </h2>
            <p class="sf-home-sub">Theo kết quả quiz da của bạn</p>
          </div>
          <RouterLink to="/san-pham/goi-y" class="sf-home-more">Xem tất cả →</RouterLink>
        </div>
        <div class="sf-product-grid">
          <ProductCard v-for="p in quizSuggestions" :key="`q-${p.id}`" :product="p" />
        </div>
      </div>
    </section>

    <section class="sf-section sf-section--muted">
      <div class="sf-container">
        <div class="sf-section-head sf-section-head--row">
          <div>
            <h2 class="sf-section-title">Có thể bạn sẽ thích</h2>
            <p class="sf-home-sub">Gợi ý thêm từ danh mục đang có</p>
          </div>
          <RouterLink to="/san-pham" class="sf-home-more">Xem tất cả →</RouterLink>
        </div>
        <div v-if="loading" class="sf-skeleton-grid" />
        <div v-else-if="suggestions.length" class="sf-product-grid">
          <ProductCard v-for="p in suggestions" :key="`s-${p.id}`" :product="p" />
        </div>
      </div>
    </section>

    <div class="sf-trust">
      <div class="sf-container sf-trust__row">
        <div v-for="item in trustItems" :key="item.title" class="sf-trust__item">
          <strong>{{ item.title }}</strong>
          <span>{{ item.desc }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.sf-home {
  --home-teal: #0b6e75;
  --home-teal-deep: #08545a;
  --home-line: #e3e9ef;
  --home-muted: #5a6a78;
}

.sf-home-hero {
  position: relative;
  min-height: min(72vh, 620px);
  display: grid;
  align-items: end;
  overflow: hidden;
  background: #e8f1f4;
}

.sf-home-hero__media {
  position: absolute;
  inset: 0;
}

.sf-home-hero__media img {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
  object-position: center 45%;
}

.sf-home-hero-fade-enter-active,
.sf-home-hero-fade-leave-active {
  transition: opacity 0.45s ease;
}

.sf-home-hero-fade-enter-from,
.sf-home-hero-fade-leave-to {
  opacity: 0;
}

.sf-home-hero__inner {
  position: relative;
  z-index: 1;
  width: 100%;
  padding-top: 4rem;
  padding-bottom: 3.5rem;
}

.sf-home-hero__content {
  max-width: 34rem;
}

.sf-home-hero__brand,
.sf-home-hero__title {
  margin: 0 0 1rem;
  font-family: var(--sf-font-display, 'Playfair Display', serif);
  font-weight: 700;
  color: var(--home-teal-deep);
}

.sf-home-hero__brand {
  font-size: clamp(3rem, 7vw, 5rem);
  letter-spacing: 0.08em;
  line-height: 0.92;
  text-transform: uppercase;
}

.sf-home-hero__title {
  font-size: clamp(1.7rem, 3.4vw, 2.6rem);
  letter-spacing: -0.02em;
  line-height: 1.15;
}

.sf-home-hero__line {
  margin: 0 0 1.6rem;
  font-size: clamp(1.05rem, 2vw, 1.25rem);
  font-weight: 400;
  line-height: 1.45;
  color: var(--home-teal-deep);
  max-width: 28ch;
}

.sf-home-hero__actions {
  display: flex;
  flex-wrap: wrap;
  gap: 0.75rem;
}

.sf-home-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-height: 48px;
  padding: 0 1.35rem;
  font-size: 14px;
  font-weight: 600;
  text-decoration: none;
  border-radius: 2px;
  transition: background 0.2s, color 0.2s, border-color 0.2s;
}

.sf-home-btn--solid {
  background: var(--home-teal);
  color: #fff;
}

.sf-home-btn--solid:hover {
  background: var(--home-teal-deep);
  color: #fff;
}

.sf-home-btn--ghost {
  border: 1px solid rgba(8, 84, 90, 0.35);
  color: var(--home-teal-deep);
  background: rgba(255, 255, 255, 0.55);
}

.sf-home-btn--ghost:hover {
  border-color: var(--home-teal);
  background: #fff;
  color: var(--home-teal);
}

.sf-home-hero__nav {
  position: absolute;
  top: 50%;
  z-index: 2;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 2.5rem;
  height: 2.5rem;
  border: 0;
  border-radius: 50%;
  cursor: pointer;
  color: var(--home-teal-deep);
  background: rgba(255, 255, 255, 0.88);
  box-shadow: 0 2px 8px rgba(8, 84, 90, 0.12);
  transform: translateY(-50%);
}

.sf-home-hero__nav:hover {
  background: #fff;
}

.sf-home-hero__nav--prev { left: 1rem; }
.sf-home-hero__nav--next { right: 1rem; }

.sf-home-hero__dots {
  position: absolute;
  left: 50%;
  bottom: 1.1rem;
  z-index: 2;
  display: flex;
  gap: 0.45rem;
  transform: translateX(-50%);
}

.sf-home-hero__dot {
  width: 9px;
  height: 9px;
  padding: 0;
  border: 0;
  border-radius: 50%;
  cursor: pointer;
  background: rgba(8, 84, 90, 0.28);
}

.sf-home-hero__dot.is-active {
  background: var(--home-teal);
  transform: scale(1.2);
}

.sf-uvbar {
  background: #fff;
  border-bottom: 1px solid var(--home-line);
}

.sf-uvbar__row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 1.5rem;
  padding: 0.9rem 1.5rem;
  flex-wrap: wrap;
}

.sf-uvbar__left {
  display: flex;
  align-items: center;
  gap: 1rem;
  flex-wrap: wrap;
}

.sf-uvbar__city {
  font-size: 13px;
  font-weight: 600;
  display: inline-flex;
  align-items: center;
  gap: 0.4rem;
  color: var(--home-teal-deep);
}

.sf-uvbar__temp {
  font-family: var(--sf-font-display, 'Playfair Display', serif);
  font-size: 1.35rem;
  font-weight: 700;
  letter-spacing: -0.02em;
  color: var(--home-teal-deep);
}

.sf-uvbar__meta {
  font-size: 13px;
  color: var(--home-muted);
}

.sf-uvbar__chip {
  display: inline-flex;
  align-items: center;
  gap: 0.4rem;
  padding: 0.35rem 0.7rem;
  background: #fff7ed;
  color: #9a3412;
  font-size: 12px;
  font-weight: 600;
  border-radius: 999px;
}

.sf-uvbar__chip span {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: #d97706;
}

.sf-uvbar__skel {
  display: block;
  border-radius: 6px;
  background: #eef2f5;
}

.sf-uvbar__skel--temp {
  width: 2.4rem;
  height: 1.35rem;
}

.sf-uvbar__skel--meta {
  width: 8.5rem;
  height: 0.85rem;
}

.sf-home-more {
  font-size: 13px;
  font-weight: 600;
  color: var(--home-teal);
  white-space: nowrap;
  text-decoration: none;
}

.sf-home-more:hover {
  text-decoration: underline;
  text-underline-offset: 3px;
}

.sf-home-sub {
  margin: 0.35rem 0 0;
  color: var(--home-muted);
  font-size: 14px;
}

.sf-cats {
  background: #fff;
  border-bottom: 1px solid var(--home-line);
  padding: 0;
}

.sf-cats__row {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
}

.sf-cat {
  padding: 1.35rem 1rem;
  text-align: center;
  text-decoration: none;
  color: inherit;
  border: 1px solid var(--home-line);
  margin: -1px 0 0 -1px;
  transition: background 0.2s;
}

.sf-cat:hover {
  background: #e6f3f4;
}

.sf-cat__label {
  display: block;
  font-size: 13px;
  font-weight: 600;
  color: var(--home-teal-deep);
}

.sf-cat__sub {
  display: block;
  margin-top: 0.2rem;
  font-size: 11px;
  color: var(--home-muted);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.sf-cats__skel {
  height: 72px;
  background: #f7f9fb;
}

.sf-trust {
  background: #fff;
  border-block: 1px solid var(--home-line);
  padding: 1.75rem 0;
}

.sf-trust__row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 1.25rem;
}

.sf-trust__item strong {
  display: block;
  font-size: 14px;
  margin-bottom: 0.2rem;
  color: var(--home-teal-deep);
}

.sf-trust__item span {
  font-size: 13px;
  color: var(--home-muted);
}

@media (max-width: 980px) {
  .sf-home-hero {
    min-height: 58vh;
  }
  .sf-home-hero__nav {
    width: 2.1rem;
    height: 2.1rem;
  }
  .sf-cats__row {
    grid-template-columns: repeat(3, 1fr);
  }
  .sf-trust__row {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 640px) {
  .sf-home-hero__inner {
    padding-top: 3rem;
    padding-bottom: 2.5rem;
  }
  .sf-cats__row {
    grid-template-columns: repeat(2, 1fr);
  }
  .sf-trust__row {
    grid-template-columns: 1fr;
  }
}
</style>
