<script setup>
import { onMounted, onUnmounted, ref } from 'vue'
import { RouterLink } from 'vue-router'
import { Icon } from '@iconify/vue'
import ProductCard from '@/components/storefront/ProductCard.vue'
import HorizontalScroll from '@/components/storefront/HorizontalScroll.vue'
import { categoryIcon } from '@/utils/categoryIcon'
import { fetchAllProducts, fetchDanhMucList, fetchThuongHieuList } from '@/api/storefrontApi'

const loading = ref(true)
const featured = ref([])
const suggestions = ref([])
const categories = ref([])
const brands = ref([])
const heroIndex = ref(0)
let heroTimer

const heroSlides = [
  {
    eyebrow: 'Bộ sưu tập Soleil',
    title: 'Bảo vệ làn da dưới ánh nắng Việt Nam',
    desc: 'Kem chống nắng kết cấu nhẹ, finish sang, phù hợp khí hậu nhiệt đới.',
    cta: 'Khám phá ngay',
    to: '/san-pham',
  },
  {
    eyebrow: 'Chống nắng cao cấp',
    title: 'SPF đa tầng, cảm giác thoáng cả ngày',
    desc: 'Lựa chọn từ vật lý, hóa học đến lai — tìm sản phẩm chân ái cho làn da bạn.',
    cta: 'Xem sản phẩm',
    to: '/san-pham',
  },
]

const sideBanners = [
  { title: 'Sản phẩm nổi bật', desc: 'Được chọn lọc bởi SUNOVA', to: '/san-pham?noiBat=1' },
  { title: 'Chăm sóc da', desc: 'Dưỡng ẩm & phục hồi', to: '/san-pham' },
]

const promises = [
  { icon: 'solar:verified-check-linear', title: 'Chính hãng', desc: 'Nguồn gốc rõ ràng' },
  { icon: 'solar:delivery-linear', title: 'Giao nhanh', desc: 'Toàn quốc 2–5 ngày' },
  { icon: 'solar:refresh-linear', title: 'Đổi trả', desc: 'Trong 7 ngày' },
]

function nextHero() {
  heroIndex.value = (heroIndex.value + 1) % heroSlides.length
}

onMounted(async () => {
  heroTimer = setInterval(nextHero, 6000)
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

onUnmounted(() => {
  clearInterval(heroTimer)
})
</script>

<template>
  <div class="sf-home">
    <!-- Hero carousel + banner phụ -->
    <section class="sf-hero-split sf-container">
      <div class="sf-hero-carousel">
        <div
          v-for="(slide, idx) in heroSlides"
          :key="idx"
          class="sf-hero-carousel__slide"
          :class="{ active: heroIndex === idx }"
        >
          <p class="sf-eyebrow sf-eyebrow--light">{{ slide.eyebrow }}</p>
          <h1 class="sf-hero-carousel__title">{{ slide.title }}</h1>
          <p class="sf-hero-carousel__desc">{{ slide.desc }}</p>
          <RouterLink :to="slide.to" class="btn-soleil btn-soleil--espresso"><span>{{ slide.cta }}</span></RouterLink>
        </div>
        <div class="sf-hero-carousel__dots">
          <button
            v-for="(_, idx) in heroSlides"
            :key="idx"
            type="button"
            :class="{ active: heroIndex === idx }"
            :aria-label="`Slide ${idx + 1}`"
            @click="heroIndex = idx"
          />
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
        <HorizontalScroll v-else aria-label="Danh mục sản phẩm">
          <RouterLink
            v-for="cat in categories"
            :key="cat.id"
            :to="`/san-pham?danhMuc=${cat.id}`"
            class="sf-circle-item"
          >
            <span class="sf-circle-item__ring">
              <Icon :icon="categoryIcon(cat.ten, cat.ma)" width="28" />
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
        <HorizontalScroll v-else aria-label="Thương hiệu">
          <RouterLink
            v-for="brand in brands"
            :key="brand.id"
            :to="`/san-pham?thuongHieu=${brand.id}`"
            class="sf-circle-item"
          >
            <span class="sf-circle-item__ring sf-circle-item__ring--brand">
              <span class="sf-circle-item__brand-text">{{ brand.ten }}</span>
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

    <!-- Quiz CTA placeholder -->
    <section class="sf-section">
      <div class="sf-container">
        <div class="sf-quiz-banner sf-quiz-banner--contrast">
          <div>
            <p class="sf-quiz-banner__eyebrow">Sắp ra mắt</p>
            <h2 class="sf-quiz-banner__title">Quiz phân tích loại da</h2>
            <p class="sf-quiz-banner__desc">Tính năng đang phát triển — khám phá sản phẩm phù hợp với làn da bạn.</p>
          </div>
          <RouterLink to="/quiz" class="sf-quiz-banner__btn">Tìm hiểu thêm</RouterLink>
        </div>
      </div>
    </section>
  </div>
</template>
