<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { RouterLink, useRoute } from 'vue-router'
import BlogIconSprite from '@/components/storefront/blog/BlogIconSprite.vue'
import TinTucCard from '@/components/storefront/blog/TinTucCard.vue'
import ProductCard from '@/components/storefront/ProductCard.vue'
import { fetchAllProducts } from '@/api/storefrontApi'
import {
  getTinTucBySlug,
  getBaiLienQuan,
  formatTinTucDate,
} from '@/constants/tinTuc'

const route = useRoute()
const suggested = ref([])
const loadSuggestError = ref('')

const bai = computed(() => getTinTucBySlug(String(route.params.slug || '')))
const related = computed(() => (bai.value ? getBaiLienQuan(bai.value.slug, 3) : []))

const tone = computed(() => {
  const m = bai.value?.danhMuc || ''
  if (m.includes('SUNOVA')) return 'gold'
  if (m.includes('Hướng dẫn')) return 'sand'
  return 'sky'
})

async function loadSuggestedProducts() {
  loadSuggestError.value = ''
  try {
    const res = await fetchAllProducts()
    const list = Array.isArray(res?.data) ? res.data : []
    const active = list.filter((p) => p && p.trangThai !== false)
    const spf50 = active.filter((p) => Number(p.chiSoSpf) >= 50)
    const pool = spf50.length ? spf50 : active
    suggested.value = pool.slice(0, 4)
  } catch {
    loadSuggestError.value = 'Không tải được gợi ý sản phẩm.'
    suggested.value = []
  }
}

onMounted(loadSuggestedProducts)
watch(
  () => route.params.slug,
  () => {
    window.scrollTo({ top: 0, behavior: 'smooth' })
  },
)
</script>

<template>
  <div class="tt-detail">
    <BlogIconSprite />

    <template v-if="bai">
      <nav class="tt-detail__breadcrumb" aria-label="Breadcrumb">
        <RouterLink to="/">Trang chủ</RouterLink>
        <span aria-hidden="true">/</span>
        <RouterLink to="/tin-tuc">Tin tức</RouterLink>
        <span aria-hidden="true">/</span>
        <span class="is-current">{{ bai.tieuDe }}</span>
      </nav>

      <header class="tt-detail__header">
        <span class="tt-detail__cat">{{ bai.danhMuc }}</span>
        <h1 class="tt-detail__title">{{ bai.tieuDe }}</h1>
        <time class="tt-detail__date" :datetime="bai.ngay">{{ formatTinTucDate(bai.ngay) }}</time>
      </header>

      <div class="tt-detail__cover" :class="`tt-detail__cover--${tone}`">
        <img
          v-if="bai.anhBia"
          :src="bai.anhBia"
          :alt="bai.tieuDe"
          class="tt-detail__cover-img"
        />
        <svg v-else viewBox="0 0 48 48" fill="none" class="tt-detail__cover-icon" aria-hidden="true">
          <use :href="`#blog-icon-${bai.icon || 'sun'}`" />
        </svg>
      </div>

      <article class="tt-detail__content">
        <p
          v-for="(para, idx) in bai.noiDung"
          :key="idx"
          class="tt-detail__p"
        >
          {{ para }}
        </p>

        <div class="tt-detail__actions">
          <a
            v-if="bai.nguon?.url"
            class="tt-detail__btn tt-detail__btn--outline"
            :href="bai.nguon.url"
            target="_blank"
            rel="noopener noreferrer"
          >
            Đọc bài gốc tại {{ bai.nguon.ten }} ↗
          </a>
          <RouterLink
            v-if="bai.ctaNoiBo?.to"
            :to="bai.ctaNoiBo.to"
            class="tt-detail__btn tt-detail__btn--solid"
          >
            {{ bai.ctaNoiBo.label }}
          </RouterLink>
        </div>

        <aside class="tt-detail__source" v-if="bai.nguon">
          <p>
            Nội dung tham khảo từ:
            <a :href="bai.nguon.url" target="_blank" rel="noopener noreferrer">
              {{ bai.nguon.ten }}
            </a>
          </p>
        </aside>

        <p class="tt-detail__disclaimer">
          Thông tin mang tính tham khảo, không thay thế tư vấn của bác sĩ da liễu.
        </p>
      </article>

      <section v-if="suggested.length" class="tt-detail__products">
        <div class="tt-detail__section-head">
          <h2>Sản phẩm gợi ý</h2>
          <RouterLink to="/san-pham" class="tt-detail__section-link">Xem tất cả</RouterLink>
        </div>
        <p class="tt-detail__section-desc">Gợi ý kem chống nắng SPF 50+ từ cửa hàng SUNOVA.</p>
        <div class="tt-detail__product-grid">
          <ProductCard v-for="p in suggested" :key="p.id" :product="p" />
        </div>
      </section>
      <p v-else-if="loadSuggestError" class="tt-detail__hint">{{ loadSuggestError }}</p>

      <section v-if="related.length" class="tt-detail__related">
        <h2>Bài viết liên quan</h2>
        <div class="tt-detail__related-grid">
          <TinTucCard v-for="r in related" :key="r.id" :bai="r" />
        </div>
      </section>

      <RouterLink to="/tin-tuc" class="tt-detail__back">← Về danh sách tin tức</RouterLink>
    </template>

    <div v-else class="tt-detail__not-found">
      <h1>Không tìm thấy bài viết</h1>
      <p>Đường dẫn không hợp lệ hoặc bài đã được gỡ.</p>
      <RouterLink to="/tin-tuc" class="tt-detail__btn tt-detail__btn--solid">
        Về trang Tin tức
      </RouterLink>
    </div>
  </div>
</template>

<style scoped>
.tt-detail {
  max-width: 1120px;
  margin: 0 auto;
  padding: 28px 20px 72px;
  font-family: var(--sf-font-body, 'Be Vietnam Pro', system-ui, sans-serif);
  color: var(--sf-espresso, #3e2c1c);
}

.tt-detail__breadcrumb {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
  margin-bottom: 22px;
  font-size: 0.88rem;
  color: var(--sf-mid, #5a5248);
}

.tt-detail__breadcrumb a {
  color: var(--sf-gold-dark, #9e7340);
  text-decoration: none;
}

.tt-detail__breadcrumb a:hover {
  text-decoration: underline;
}

.tt-detail__breadcrumb .is-current {
  max-width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  color: var(--sf-espresso, #3e2c1c);
  font-weight: 600;
}

.tt-detail__header {
  max-width: 720px;
  margin-bottom: 20px;
}

.tt-detail__cat {
  display: inline-block;
  margin-bottom: 10px;
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: var(--sf-gold-dark, #9e7340);
}

.tt-detail__title {
  margin: 0 0 10px;
  font-family: var(--sf-font-display, 'Playfair Display', Georgia, serif);
  font-size: clamp(1.6rem, 3vw, 2.2rem);
  font-weight: 600;
  line-height: 1.25;
}

.tt-detail__date {
  font-size: 0.9rem;
  color: var(--sf-mid, #5a5248);
}

.tt-detail__cover {
  display: grid;
  place-items: center;
  width: 100%;
  max-width: 720px;
  aspect-ratio: 16 / 8;
  margin-bottom: 28px;
  border-radius: 18px;
  overflow: hidden;
}

.tt-detail__cover--gold {
  background: linear-gradient(145deg, #f3e4c8, #e8d3a8 55%, #d4b87a);
}
.tt-detail__cover--sky {
  background: linear-gradient(145deg, #f7efe3, #efe0c8 50%, #e2cba0);
}
.tt-detail__cover--sand {
  background: linear-gradient(145deg, #f5ebe0, #ead9c4 55%, #dcc4a0);
}

.tt-detail__cover-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.tt-detail__cover-icon {
  width: 72px;
  height: 72px;
  color: var(--sf-espresso, #3e2c1c);
  opacity: 0.7;
}

.tt-detail__content {
  max-width: 720px;
}

.tt-detail__p {
  margin: 0 0 1.1em;
  font-size: 1.02rem;
  line-height: 1.75;
  color: var(--sf-espresso, #3e2c1c);
}

.tt-detail__actions {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin: 24px 0 18px;
}

.tt-detail__btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 10px 16px;
  border-radius: 999px;
  font-weight: 600;
  font-size: 0.92rem;
  text-decoration: none;
  transition: background 0.15s ease, color 0.15s ease, border-color 0.15s ease;
}

.tt-detail__btn--outline {
  border: 1px solid rgba(158, 115, 64, 0.4);
  color: var(--sf-gold-dark, #9e7340);
  background: transparent;
}

.tt-detail__btn--outline:hover {
  border-color: var(--sf-gold-dark, #9e7340);
  background: rgba(158, 115, 64, 0.08);
}

.tt-detail__btn--solid {
  border: 1px solid var(--sf-espresso, #3e2c1c);
  background: var(--sf-espresso, #3e2c1c);
  color: #fffaf4;
}

.tt-detail__btn--solid:hover {
  background: #2c1f14;
}

.tt-detail__source {
  margin-bottom: 10px;
  font-size: 0.88rem;
  color: var(--sf-mid, #5a5248);
}

.tt-detail__source a {
  color: var(--sf-gold-dark, #9e7340);
}

.tt-detail__disclaimer {
  margin: 0 0 40px;
  padding: 12px 14px;
  border-radius: 12px;
  background: rgba(158, 115, 64, 0.08);
  font-size: 0.88rem;
  line-height: 1.5;
  color: var(--sf-mid, #5a5248);
}

.tt-detail__products,
.tt-detail__related {
  margin-top: 8px;
  padding-top: 28px;
  border-top: 1px solid rgba(158, 115, 64, 0.16);
}

.tt-detail__section-head {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 6px;
}

.tt-detail__section-head h2,
.tt-detail__related h2 {
  margin: 0 0 8px;
  font-family: var(--sf-font-display, 'Playfair Display', Georgia, serif);
  font-size: 1.4rem;
  font-weight: 600;
}

.tt-detail__section-link {
  font-size: 0.9rem;
  font-weight: 600;
  color: var(--sf-gold-dark, #9e7340);
  text-decoration: none;
}

.tt-detail__section-desc,
.tt-detail__hint {
  margin: 0 0 16px;
  color: var(--sf-mid, #5a5248);
  font-size: 0.92rem;
}

.tt-detail__product-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 16px;
}

.tt-detail__related-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 18px;
  margin-top: 12px;
}

.tt-detail__back {
  display: inline-block;
  margin-top: 32px;
  font-weight: 600;
  color: var(--sf-gold-dark, #9e7340);
  text-decoration: none;
}

.tt-detail__back:hover {
  text-decoration: underline;
}

.tt-detail__not-found {
  text-align: center;
  padding: 72px 16px;
}

.tt-detail__not-found h1 {
  margin: 0 0 10px;
  font-family: var(--sf-font-display, 'Playfair Display', Georgia, serif);
}

.tt-detail__not-found p {
  margin: 0 0 20px;
  color: var(--sf-mid, #5a5248);
}

@media (max-width: 900px) {
  .tt-detail__product-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
  .tt-detail__related-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 560px) {
  .tt-detail__product-grid,
  .tt-detail__related-grid {
    grid-template-columns: 1fr;
  }
}
</style>
