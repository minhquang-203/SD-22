<template>
  <div class="blog-page">
    <BlogIconSprite />

    <section class="blog-hero">
      <p class="blog-hero__eyebrow">SUNOVA Blog</p>
      <h1 class="blog-hero__title">
        Kiến thức chống nắng dễ hiểu, dễ áp dụng
      </h1>
      <p class="blog-hero__subtitle">
        Hiểu UV, thời tiết bốn mùa và cách chọn kem chống nắng phù hợp khí hậu Việt Nam —
        ngắn gọn, thực tế.
      </p>

      <div class="blog-hero__search">
        <Icon icon="solar:magnifer-linear" width="18" />
        <input
          v-model="searchTerm"
          type="text"
          placeholder="Tìm bài viết về SPF, UV, thời tiết..."
        />
      </div>
    </section>

    <section class="blog-intro" aria-label="Dẫn nhập nhanh về chống nắng">
      <h2 class="blog-intro__title">Trước khi đọc, nắm nhanh vài điều</h2>
      <p class="blog-intro__lead">
        Chống nắng không chỉ cho ngày hè hay lúc ra biển. Ở Việt Nam nắng mạnh gần như quanh năm,
        nên hiểu đúng vài điều cơ bản sẽ giúp bạn chọn và dùng kem chống nắng hiệu quả hơn.
      </p>

      <div class="blog-intro__grid">
        <article class="blog-intro__card">
          <span class="blog-intro__num" aria-hidden="true">1</span>
          <h3 class="blog-intro__card-title">Nắng quanh năm</h3>
          <p class="blog-intro__card-text">
            Tia UV ở Việt Nam thường cao cả ngày thường lẫn ngày râm mát, không chỉ khi trời nắng gắt.
          </p>
        </article>
        <article class="blog-intro__card">
          <span class="blog-intro__num" aria-hidden="true">2</span>
          <h3 class="blog-intro__card-title">Mây không cản được UV</h3>
          <p class="blog-intro__card-text">
            Tới 80% tia UV vẫn xuyên qua mây, nên trời mưa hay âm u vẫn cần chống nắng.
          </p>
        </article>
        <article class="blog-intro__card">
          <span class="blog-intro__num" aria-hidden="true">3</span>
          <h3 class="blog-intro__card-title">SPF và PA khác nhau</h3>
          <p class="blog-intro__card-text">
            SPF chống tia UVB (gây bỏng, đen da), PA chống tia UVA (gây lão hóa). Cần cả hai.
          </p>
        </article>
        <article class="blog-intro__card">
          <span class="blog-intro__num" aria-hidden="true">4</span>
          <h3 class="blog-intro__card-title">Bôi đủ và thoa lại</h3>
          <p class="blog-intro__card-text">
            Bôi đủ lượng và thoa lại sau mỗi 2–3 giờ khi ra ngoài thì kem mới thật sự bảo vệ.
          </p>
        </article>
      </div>

      <p class="blog-intro__outro">Muốn hiểu sâu hơn? Cùng đọc các bài viết bên dưới.</p>
    </section>

    <nav class="blog-filter" aria-label="Lọc bài viết theo chủ đề">
      <button
        v-for="cat in blogCategories"
        :key="cat.slug"
        type="button"
        class="blog-filter__pill"
        :class="{ 'is-active': activeCategory === cat.slug }"
        @click="activeCategory = cat.slug"
      >
        {{ cat.label }}
      </button>
    </nav>

    <section v-if="featuredPost" class="blog-featured">
      <RouterLink
        :to="`/blog/${featuredPost.slug}`"
        class="blog-featured__media"
        :class="`blog-featured__media--${featuredTone}`"
      >
        <span class="blog-featured__glow" aria-hidden="true" />
        <svg viewBox="0 0 48 48" fill="none" class="blog-featured__icon">
          <use :href="`#blog-icon-${featuredPost.icon}`" />
        </svg>
      </RouterLink>
      <div class="blog-featured__content">
        <span class="blog-featured__badge">{{ featuredPost.tag }}</span>
        <h2 class="blog-featured__title">
          <RouterLink :to="`/blog/${featuredPost.slug}`">{{ featuredPost.title }}</RouterLink>
        </h2>
        <p class="blog-featured__excerpt">{{ featuredPost.excerpt }}</p>
        <div class="blog-featured__meta">
          <span>{{ formatDate(featuredPost.publishDate) }}</span>
          <span>·</span>
          <span>{{ featuredPost.readTime }}</span>
        </div>
        <RouterLink :to="`/blog/${featuredPost.slug}`" class="blog-featured__cta">
          Đọc bài viết
          <Icon icon="solar:arrow-right-linear" width="16" />
        </RouterLink>
      </div>
    </section>

    <section class="blog-grid-section">
      <div class="blog-grid-section__head">
        <h2>Tất cả bài viết</h2>
        <span class="blog-grid-section__count">{{ filteredPosts.length }} bài</span>
      </div>

      <div v-if="filteredPosts.length" class="blog-grid">
        <BlogCard v-for="post in filteredPosts" :key="post.id" :post="post" />
      </div>

      <div v-else class="blog-empty">
        <p>Không tìm thấy bài viết phù hợp. Thử từ khoá hoặc chủ đề khác nhé.</p>
      </div>
    </section>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import { Icon } from '@iconify/vue'
import { blogCategories, blogPosts } from '@/constants/blogPosts'
import BlogCard from '@/components/storefront/blog/BlogCard.vue'
import BlogIconSprite from '@/components/storefront/blog/BlogIconSprite.vue'

const activeCategory = ref('tat-ca')
const searchTerm = ref('')

const CAT_TONE = {
  'kien-thuc-spf': 'gold',
  'thoi-tiet-da': 'sky',
  'huong-dan-chon': 'sage',
  'mua-mua': 'teal',
  'cham-soc-da': 'coral',
}

const featuredPost = computed(() => blogPosts[0])
const featuredTone = computed(() => CAT_TONE[featuredPost.value?.category] || 'gold')

const filteredPosts = computed(() => {
  const term = searchTerm.value.trim().toLowerCase()

  return blogPosts.filter((post) => {
    const matchesCategory =
      activeCategory.value === 'tat-ca' || post.category === activeCategory.value

    const matchesSearch =
      !term ||
      post.title.toLowerCase().includes(term) ||
      post.excerpt.toLowerCase().includes(term)

    return matchesCategory && matchesSearch
  })
})

function formatDate(dateStr) {
  const date = new Date(dateStr)
  return date.toLocaleDateString('vi-VN', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
  })
}
</script>

<style scoped>
.blog-page {
  max-width: 1120px;
  margin: 0 auto;
  padding: 2.5rem 1.25rem 5rem;
  color: var(--sf-charcoal, #1a1814);
  font-family: var(--sf-font-body, 'Be Vietnam Pro', sans-serif);
}

.blog-hero {
  max-width: 720px;
  margin-bottom: 2rem;
}

.blog-hero__eyebrow {
  font-size: 0.75rem;
  letter-spacing: 0.16em;
  text-transform: uppercase;
  color: var(--sf-gold-dark, #9e7340);
  font-weight: 600;
  margin: 0 0 0.75rem;
}

.blog-hero__title {
  font-family: var(--sf-font-display, 'Playfair Display', serif);
  font-size: clamp(1.85rem, 4vw, 2.65rem);
  line-height: 1.2;
  font-weight: 600;
  margin: 0 0 0.85rem;
  color: var(--sf-espresso, #241a12);
}

.blog-hero__subtitle {
  font-size: 1rem;
  line-height: 1.7;
  color: var(--sf-mid, #5a5248);
  margin: 0 0 1.5rem;
}

.blog-hero__search {
  display: flex;
  align-items: center;
  gap: 0.65rem;
  max-width: 440px;
  padding: 0.75rem 1rem;
  background: var(--sf-warm-white, #fffdfa);
  border: 1px solid var(--sf-hairline, #ede5d8);
  border-radius: 999px;
  color: var(--sf-light-mid, #8a8278);
}

.blog-hero__search input {
  flex: 1;
  background: transparent;
  border: none;
  outline: none;
  color: var(--sf-charcoal, #1a1814);
  font-family: inherit;
  font-size: 0.9rem;
}

.blog-intro {
  margin: 0 0 2rem;
  padding: 1.5rem 0 0.25rem;
  border-top: 1px solid var(--sf-hairline, #ede5d8);
}

.blog-intro__title {
  font-family: var(--sf-font-display, 'Playfair Display', serif);
  font-size: 1.2rem;
  font-weight: 600;
  color: var(--sf-espresso, #241a12);
  margin: 0 0 0.65rem;
}

.blog-intro__lead {
  margin: 0 0 1.25rem;
  max-width: 52rem;
  font-size: 0.95rem;
  line-height: 1.7;
  color: var(--sf-mid, #5a5248);
}

.blog-intro__grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 0.85rem;
}

.blog-intro__card {
  display: flex;
  flex-direction: column;
  gap: 0.4rem;
  padding: 1rem 1.05rem 1.1rem;
  border-radius: 12px;
  border: 1px solid var(--sf-hairline, #ede5d8);
  background: linear-gradient(160deg, #fffdfa 0%, var(--sf-cream, #f9f5f0) 55%, #f3e8d6 100%);
  transition: transform 0.2s ease, box-shadow 0.2s ease, border-color 0.2s ease;
}

.blog-intro__card:hover {
  transform: translateY(-3px);
  border-color: var(--sf-gold, #c9a96e);
  box-shadow: 0 8px 20px rgba(36, 26, 18, 0.07);
}

.blog-intro__num {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 1.65rem;
  height: 1.65rem;
  border-radius: 999px;
  font-size: 0.75rem;
  font-weight: 700;
  color: var(--sf-espresso, #241a12);
  background: rgba(201, 169, 110, 0.4);
  margin-bottom: 0.15rem;
}

.blog-intro__card-title {
  margin: 0;
  font-size: 0.9375rem;
  font-weight: 700;
  line-height: 1.35;
  color: var(--sf-espresso, #241a12);
}

.blog-intro__card-text {
  margin: 0;
  font-size: 0.8125rem;
  line-height: 1.55;
  color: var(--sf-mid, #5a5248);
}

.blog-intro__outro {
  margin: 1.15rem 0 0;
  font-size: 0.9rem;
  font-weight: 600;
  color: var(--sf-gold-dark, #9e7340);
}

.blog-filter {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
  margin-bottom: 2rem;
}

.blog-filter__pill {
  padding: 0.5rem 1rem;
  border-radius: 999px;
  border: 1px solid var(--sf-hairline, #ede5d8);
  background: var(--sf-warm-white, #fffdfa);
  color: var(--sf-mid, #5a5248);
  font-family: inherit;
  font-size: 0.8125rem;
  cursor: pointer;
  transition: all 0.2s ease;
}

.blog-filter__pill:hover {
  border-color: var(--sf-gold, #c9a96e);
  color: var(--sf-espresso, #241a12);
}

.blog-filter__pill.is-active {
  background: var(--sf-gold, #c9a96e);
  border-color: var(--sf-gold, #c9a96e);
  color: var(--sf-espresso, #241a12);
  font-weight: 600;
}

.blog-featured {
  display: grid;
  grid-template-columns: minmax(0, 0.95fr) minmax(0, 1.15fr);
  gap: 0;
  background: var(--sf-warm-white, #fffdfa);
  border: 1px solid var(--sf-hairline, #ede5d8);
  border-radius: 16px;
  overflow: hidden;
  margin-bottom: 3rem;
  box-shadow: 0 8px 28px rgba(36, 26, 18, 0.05);
}

.blog-featured__media {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 300px;
  text-decoration: none;
}

.blog-featured__glow {
  position: absolute;
  inset: -10%;
  background: radial-gradient(circle at 35% 30%, rgba(255, 255, 255, 0.65), transparent 55%);
  pointer-events: none;
}

.blog-featured__media--gold {
  background: linear-gradient(145deg, #f6ead4, #e2c892);
}
.blog-featured__media--sky {
  background: linear-gradient(145deg, #e4f0f7, #9fc0d8);
}
.blog-featured__media--sage {
  background: linear-gradient(145deg, #eaf1e4, #b3c9a4);
}
.blog-featured__media--teal {
  background: linear-gradient(145deg, #e2f1f0, #8fc4c1);
}
.blog-featured__media--coral {
  background: linear-gradient(145deg, #f8e8e1, #d9a694);
}

.blog-featured__icon {
  position: relative;
  z-index: 1;
  width: 96px;
  height: 96px;
  color: var(--sf-espresso, #241a12);
}

.blog-featured__content {
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 0.85rem;
  padding: 2rem 2.25rem;
}

.blog-featured__badge {
  display: inline-flex;
  width: fit-content;
  padding: 0.3rem 0.75rem;
  border-radius: 999px;
  background: rgba(201, 169, 110, 0.18);
  color: var(--sf-gold-dark, #9e7340);
  font-size: 0.6875rem;
  font-weight: 700;
  letter-spacing: 0.06em;
  text-transform: uppercase;
}

.blog-featured__title {
  font-family: var(--sf-font-display, 'Playfair Display', serif);
  font-size: clamp(1.4rem, 2.5vw, 1.85rem);
  line-height: 1.3;
  font-weight: 600;
  margin: 0;
}

.blog-featured__title a {
  color: var(--sf-espresso, #241a12);
  text-decoration: none;
}

.blog-featured__title a:hover {
  color: var(--sf-gold-dark, #9e7340);
}

.blog-featured__excerpt {
  margin: 0;
  font-size: 0.95rem;
  line-height: 1.7;
  color: var(--sf-mid, #5a5248);
}

.blog-featured__meta {
  display: flex;
  flex-wrap: wrap;
  gap: 0.4rem;
  font-size: 0.8125rem;
  color: var(--sf-light-mid, #8a8278);
}

.blog-featured__cta {
  display: inline-flex;
  align-items: center;
  gap: 0.4rem;
  margin-top: 0.35rem;
  width: fit-content;
  font-size: 0.8125rem;
  font-weight: 600;
  letter-spacing: 0.04em;
  text-transform: uppercase;
  color: var(--sf-gold-dark, #9e7340);
  text-decoration: none;
}

.blog-featured__cta:hover {
  color: var(--sf-accent, #a33b1c);
}

.blog-grid-section__head {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 1rem;
  margin-bottom: 1.25rem;
}

.blog-grid-section__head h2 {
  font-family: var(--sf-font-display, 'Playfair Display', serif);
  font-size: 1.5rem;
  margin: 0;
  color: var(--sf-espresso, #241a12);
}

.blog-grid-section__count {
  font-size: 0.8125rem;
  color: var(--sf-light-mid, #8a8278);
}

.blog-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 1.25rem;
}

.blog-empty {
  padding: 2.5rem 1rem;
  text-align: center;
  color: var(--sf-mid, #5a5248);
  background: var(--sf-cream, #f9f5f0);
  border-radius: 12px;
  border: 1px dashed var(--sf-hairline, #ede5d8);
}

@media (max-width: 900px) {
  .blog-intro__grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
  .blog-featured {
    grid-template-columns: 1fr;
  }
  .blog-featured__media {
    min-height: 200px;
  }
  .blog-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 600px) {
  .blog-intro__grid {
    grid-template-columns: 1fr;
  }
  .blog-grid {
    grid-template-columns: 1fr;
  }
}
</style>
