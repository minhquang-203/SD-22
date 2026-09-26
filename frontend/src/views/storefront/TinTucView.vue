<script setup>
import { computed, ref } from 'vue'
import { RouterLink } from 'vue-router'
import BlogIconSprite from '@/components/storefront/blog/BlogIconSprite.vue'
import TinTucCard from '@/components/storefront/blog/TinTucCard.vue'
import {
  TIN_TUC_DANH_MUC,
  tinTucBaiViet,
  getBaiNoiBat,
  formatTinTucDate,
} from '@/constants/tinTuc'

const activeCat = ref('Tất cả')
const keyword = ref('')

const featured = computed(() => getBaiNoiBat())

const featuredTone = computed(() => {
  const m = featured.value?.danhMuc || ''
  if (m.includes('SUNOVA')) return 'gold'
  if (m.includes('Hướng dẫn')) return 'sand'
  return 'sky'
})

const filtered = computed(() => {
  const q = keyword.value.trim().toLowerCase()
  return tinTucBaiViet.filter((b) => {
    if (activeCat.value !== 'Tất cả' && b.danhMuc !== activeCat.value) return false
    if (!q) return true
    return (
      b.tieuDe.toLowerCase().includes(q) ||
      b.tomTat.toLowerCase().includes(q) ||
      b.danhMuc.toLowerCase().includes(q)
    )
  })
})

const gridPosts = computed(() => {
  const featSlug = featured.value?.slug
  // Khi đang "Tất cả" và không tìm kiếm: ẩn bài nổi bật khỏi lưới để tránh trùng
  if (activeCat.value === 'Tất cả' && !keyword.value.trim() && featSlug) {
    return filtered.value.filter((b) => b.slug !== featSlug)
  }
  return filtered.value
})
</script>

<template>
  <div class="tt-page">
    <BlogIconSprite />

    <nav class="tt-breadcrumb" aria-label="Breadcrumb">
      <RouterLink to="/">Trang chủ</RouterLink>
      <span aria-hidden="true">/</span>
      <span class="is-current">Tin tức</span>
    </nav>

    <header class="tt-hero">
      <h1 class="tt-hero__title">Tin tức &amp; Cẩm nang chống nắng</h1>
      <p class="tt-hero__desc">
        Kiến thức SPF · PA, mẹo chọn kem theo loại da, và cập nhật từ SUNOVA — viết ngắn, dễ đọc,
        để bạn bảo vệ da đúng cách mỗi ngày.
      </p>
      <div class="tt-hero__search">
        <input
          v-model="keyword"
          type="search"
          placeholder="Tìm theo tiêu đề bài viết…"
          aria-label="Tìm bài viết"
        />
      </div>
    </header>

    <section v-if="featured && activeCat === 'Tất cả' && !keyword.trim()" class="tt-featured">
      <RouterLink
        :to="`/tin-tuc/${featured.slug}`"
        class="tt-featured__media"
        :class="`tt-featured__media--${featuredTone}`"
      >
        <span class="tt-featured__glow" aria-hidden="true" />
        <svg viewBox="0 0 48 48" fill="none" class="tt-featured__icon" aria-hidden="true">
          <use :href="`#blog-icon-${featured.icon || 'sun'}`" />
        </svg>
      </RouterLink>
      <div class="tt-featured__content">
        <span class="tt-featured__badge">{{ featured.danhMuc }}</span>
        <h2 class="tt-featured__title">
          <RouterLink :to="`/tin-tuc/${featured.slug}`">{{ featured.tieuDe }}</RouterLink>
        </h2>
        <p class="tt-featured__excerpt">{{ featured.tomTat }}</p>
        <div class="tt-featured__meta">
          <time :datetime="featured.ngay">{{ formatTinTucDate(featured.ngay) }}</time>
        </div>
        <RouterLink :to="`/tin-tuc/${featured.slug}`" class="tt-featured__cta">
          Đọc tiếp →
        </RouterLink>
      </div>
    </section>

    <nav class="tt-filter" aria-label="Lọc theo danh mục">
      <button
        v-for="cat in TIN_TUC_DANH_MUC"
        :key="cat"
        type="button"
        class="tt-filter__pill"
        :class="{ 'is-active': activeCat === cat }"
        @click="activeCat = cat"
      >
        {{ cat }}
      </button>
    </nav>

    <section class="tt-grid-section">
      <div class="tt-grid-section__head">
        <h2>{{ activeCat === 'Tất cả' ? 'Tất cả bài viết' : activeCat }}</h2>
        <span class="tt-grid-section__count">{{ gridPosts.length }} bài</span>
      </div>

      <div v-if="gridPosts.length" class="tt-grid">
        <TinTucCard v-for="bai in gridPosts" :key="bai.id" :bai="bai" />
      </div>
      <div v-else class="tt-empty">
        <p>Không tìm thấy bài viết phù hợp.</p>
        <button type="button" class="tt-empty__btn" @click="keyword = ''; activeCat = 'Tất cả'">
          Xóa bộ lọc
        </button>
      </div>
    </section>
  </div>
</template>

<style scoped>
.tt-page {
  max-width: 1120px;
  margin: 0 auto;
  padding: 28px 20px 72px;
  font-family: var(--sf-font-body, 'Be Vietnam Pro', system-ui, sans-serif);
  color: var(--sf-espresso, #3e2c1c);
}

.tt-breadcrumb {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
  margin-bottom: 20px;
  font-size: 0.88rem;
  color: var(--sf-mid, #5a5248);
}

.tt-breadcrumb a {
  color: var(--sf-gold-dark, #9e7340);
  text-decoration: none;
}

.tt-breadcrumb a:hover {
  text-decoration: underline;
}

.tt-breadcrumb .is-current {
  color: var(--sf-espresso, #3e2c1c);
  font-weight: 600;
}

.tt-hero {
  margin-bottom: 32px;
}

.tt-hero__title {
  margin: 0 0 10px;
  font-family: var(--sf-font-display, 'Playfair Display', Georgia, serif);
  font-size: clamp(1.75rem, 3.5vw, 2.45rem);
  font-weight: 600;
  line-height: 1.2;
  color: var(--sf-espresso, #3e2c1c);
}

.tt-hero__desc {
  margin: 0 0 18px;
  max-width: 42rem;
  font-size: 1rem;
  line-height: 1.65;
  color: var(--sf-mid, #5a5248);
}

.tt-hero__search {
  max-width: 360px;
}

.tt-hero__search input {
  width: 100%;
  border: 1px solid rgba(158, 115, 64, 0.28);
  border-radius: 999px;
  padding: 10px 16px;
  background: var(--sf-warm-white, #fffaf4);
  font: inherit;
  color: inherit;
}

.tt-hero__search input:focus {
  outline: 2px solid rgba(158, 115, 64, 0.35);
  outline-offset: 1px;
}

.tt-featured {
  display: grid;
  grid-template-columns: minmax(0, 1.05fr) minmax(0, 1.2fr);
  gap: 28px;
  align-items: center;
  padding: 20px;
  margin-bottom: 28px;
  background: var(--sf-warm-white, #fffaf4);
  border: 1px solid rgba(158, 115, 64, 0.16);
  border-radius: 20px;
}

.tt-featured__media {
  position: relative;
  display: grid;
  place-items: center;
  min-height: 220px;
  border-radius: 16px;
  text-decoration: none;
  overflow: hidden;
}

.tt-featured__media--gold {
  background: linear-gradient(145deg, #f3e4c8, #e8d3a8 55%, #d4b87a);
}
.tt-featured__media--sky {
  background: linear-gradient(145deg, #f7efe3, #efe0c8 50%, #e2cba0);
}
.tt-featured__media--sand {
  background: linear-gradient(145deg, #f5ebe0, #ead9c4 55%, #dcc4a0);
}

.tt-featured__glow {
  position: absolute;
  inset: 20% 24%;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.4);
  filter: blur(22px);
}

.tt-featured__icon {
  position: relative;
  width: 64px;
  height: 64px;
  color: var(--sf-espresso, #3e2c1c);
  opacity: 0.75;
}

.tt-featured__badge {
  display: inline-block;
  margin-bottom: 10px;
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: var(--sf-gold-dark, #9e7340);
}

.tt-featured__title {
  margin: 0 0 10px;
  font-family: var(--sf-font-display, 'Playfair Display', Georgia, serif);
  font-size: clamp(1.35rem, 2.4vw, 1.85rem);
  font-weight: 600;
  line-height: 1.3;
}

.tt-featured__title a {
  color: inherit;
  text-decoration: none;
}

.tt-featured__title a:hover {
  color: var(--sf-gold-dark, #9e7340);
}

.tt-featured__excerpt {
  margin: 0 0 12px;
  line-height: 1.6;
  color: var(--sf-mid, #5a5248);
}

.tt-featured__meta {
  margin-bottom: 14px;
  font-size: 0.88rem;
  color: var(--sf-mid, #5a5248);
}

.tt-featured__cta {
  display: inline-flex;
  font-weight: 600;
  color: var(--sf-gold-dark, #9e7340);
  text-decoration: none;
}

.tt-featured__cta:hover {
  text-decoration: underline;
}

.tt-filter {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 24px;
}

.tt-filter__pill {
  border: 1px solid rgba(158, 115, 64, 0.28);
  background: transparent;
  color: var(--sf-espresso, #3e2c1c);
  border-radius: 999px;
  padding: 8px 14px;
  font: inherit;
  font-size: 0.88rem;
  cursor: pointer;
  transition: background 0.15s ease, border-color 0.15s ease, color 0.15s ease;
}

.tt-filter__pill:hover {
  border-color: var(--sf-gold-dark, #9e7340);
}

.tt-filter__pill.is-active {
  background: var(--sf-espresso, #3e2c1c);
  border-color: var(--sf-espresso, #3e2c1c);
  color: #fffaf4;
}

.tt-grid-section__head {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 16px;
}

.tt-grid-section__head h2 {
  margin: 0;
  font-family: var(--sf-font-display, 'Playfair Display', Georgia, serif);
  font-size: 1.35rem;
  font-weight: 600;
}

.tt-grid-section__count {
  font-size: 0.88rem;
  color: var(--sf-mid, #5a5248);
}

.tt-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 20px;
}

.tt-empty {
  text-align: center;
  padding: 48px 16px;
  color: var(--sf-mid, #5a5248);
}

.tt-empty__btn {
  margin-top: 12px;
  border: 1px solid rgba(158, 115, 64, 0.35);
  background: transparent;
  border-radius: 999px;
  padding: 8px 16px;
  font: inherit;
  cursor: pointer;
  color: var(--sf-gold-dark, #9e7340);
}

@media (max-width: 900px) {
  .tt-featured {
    grid-template-columns: 1fr;
  }
  .tt-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 560px) {
  .tt-grid {
    grid-template-columns: 1fr;
  }
}
</style>
