<template>
  <div class="blog-page">
    <BlogIconSprite />

    <!-- Hero -->
    <section class="blog-hero">
      <p class="blog-hero__eyebrow">SUNOVA BLOG</p>
      <h1 class="blog-hero__title">
        Kiến thức chống nắng &amp; thời tiết, dễ hiểu, dễ áp dụng
      </h1>
      <p class="blog-hero__subtitle">
        Những bài viết giúp bạn hiểu đúng về chỉ số UV, thời tiết bốn mùa và cách chọn,
        sử dụng kem chống nắng phù hợp với khí hậu Việt Nam.
      </p>

      <div class="blog-hero__search">
        <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
          <circle cx="7" cy="7" r="5.2" stroke="currentColor" stroke-width="1.4" />
          <line
            x1="11"
            y1="11"
            x2="15"
            y2="15"
            stroke="currentColor"
            stroke-width="1.4"
            stroke-linecap="round"
          />
        </svg>
        <input
          v-model="searchTerm"
          type="text"
          placeholder="Tìm bài viết về kem chống nắng, UV, thời tiết..."
        />
      </div>
    </section>

    <!-- Category filter -->
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

    <!-- Featured post -->
    <section v-if="featuredPost" class="blog-featured">
      <RouterLink :to="`/blog/${featuredPost.slug}`" class="blog-featured__media">
        <svg viewBox="0 0 48 48" fill="none" class="blog-featured__icon">
          <use :href="`#blog-icon-${featuredPost.icon}`" />
        </svg>
      </RouterLink>
      <div class="blog-featured__content">
        <span class="blog-featured__badge">Bài viết nổi bật</span>
        <h2 class="blog-featured__title">
          <RouterLink :to="`/blog/${featuredPost.slug}`">{{ featuredPost.title }}</RouterLink>
        </h2>
        <p class="blog-featured__excerpt">{{ featuredPost.excerpt }}</p>
        <div class="blog-featured__meta">
          <span>{{ formatDate(featuredPost.publishDate) }}</span>
          <span class="blog-card__dot">·</span>
          <span>{{ featuredPost.readTime }}</span>
        </div>
        <RouterLink :to="`/blog/${featuredPost.slug}`" class="blog-featured__cta">
          Đọc bài viết
          <svg width="14" height="14" viewBox="0 0 14 14" fill="none">
            <path
              d="M2.5 7H11.5M11.5 7L7.5 3M11.5 7L7.5 11"
              stroke="currentColor"
              stroke-width="1.4"
              stroke-linecap="round"
              stroke-linejoin="round"
            />
          </svg>
        </RouterLink>
      </div>
    </section>

    <!-- Post grid -->
    <section class="blog-grid-section">
      <div class="blog-grid-section__head">
        <h2>Tất cả bài viết</h2>
        <span class="blog-grid-section__count">{{ filteredPosts.length }} bài viết</span>
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
import { blogCategories, blogPosts } from '@/constants/blogPosts'
import BlogCard from '@/components/storefront/blog/BlogCard.vue'
import BlogIconSprite from '@/components/storefront/blog/BlogIconSprite.vue'

const activeCategory = ref('tat-ca')
const searchTerm = ref('')

const featuredPost = computed(() => blogPosts[0])

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
  --sn-bg: #1b130c;
  --sn-bg-elevated: #241a12;
  --sn-border: rgba(201, 163, 106, 0.18);
  --sn-gold: #c9a36a;
  --sn-gold-bright: #e0bb84;
  --sn-text: #f3ead9;
  --sn-text-muted: #b8a890;
  --sn-font-display: 'Playfair Display', Georgia, serif;
  --sn-font-body: 'Be Vietnam Pro', 'Inter', sans-serif;

  max-width: 1280px;
  margin: 0 auto;
  padding: 48px 24px 96px;
  color: var(--sn-text);
  font-family: var(--sn-font-body);
}

/* Hero */
.blog-hero {
  max-width: 760px;
  margin-bottom: 40px;
}

.blog-hero__eyebrow {
  font-size: 12px;
  letter-spacing: 0.18em;
  text-transform: uppercase;
  color: var(--sn-gold);
  margin: 0 0 14px;
}

.blog-hero__title {
  font-family: var(--sn-font-display);
  font-size: clamp(30px, 4vw, 44px);
  line-height: 1.2;
  font-weight: 600;
  margin: 0 0 16px;
  color: var(--sn-text);
}

.blog-hero__subtitle {
  font-size: 15px;
  line-height: 1.7;
  color: var(--sn-text-muted);
  margin: 0 0 28px;
}

.blog-hero__search {
  display: flex;
  align-items: center;
  gap: 10px;
  max-width: 460px;
  padding: 12px 16px;
  background: var(--sn-bg-elevated);
  border: 1px solid var(--sn-border);
  border-radius: 999px;
  color: var(--sn-text-muted);
}

.blog-hero__search input {
  flex: 1;
  background: transparent;
  border: none;
  outline: none;
  color: var(--sn-text);
  font-family: var(--sn-font-body);
  font-size: 13.5px;
}

.blog-hero__search input::placeholder {
  color: var(--sn-text-muted);
}

/* Category filter */
.blog-filter {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 40px;
}

.blog-filter__pill {
  padding: 9px 18px;
  border-radius: 999px;
  border: 1px solid var(--sn-border);
  background: transparent;
  color: var(--sn-text-muted);
  font-family: var(--sn-font-body);
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.blog-filter__pill:hover {
  border-color: var(--sn-gold);
  color: var(--sn-text);
}

.blog-filter__pill.is-active {
  background: var(--sn-gold);
  border-color: var(--sn-gold);
  color: #1b130c;
  font-weight: 600;
}

/* Featured post */
.blog-featured {
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(0, 1.1fr);
  gap: 0;
  background: var(--sn-bg-elevated);
  border: 1px solid var(--sn-border);
  border-radius: 4px;
  overflow: hidden;
  margin-bottom: 56px;
}

.blog-featured__media {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 280px;
  background: radial-gradient(
      circle at 25% 20%,
      rgba(201, 163, 106, 0.2),
      transparent 60%
    ),
    #1b130c;
  border-right: 1px solid var(--sn-border);
}

.blog-featured__icon {
  width: 96px;
  height: 96px;
  color: var(--sn-gold);
}

.blog-featured__content {
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 14px;
  padding: 40px 44px;
}

.blog-featured__badge {
  align-self: flex-start;
  font-size: 11px;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: #1b130c;
  background: var(--sn-gold);
  padding: 5px 12px;
  border-radius: 999px;
  font-weight: 600;
}

.blog-featured__title {
  font-family: var(--sn-font-display);
  font-size: clamp(22px, 2.6vw, 30px);
  line-height: 1.3;
  margin: 0;
}

.blog-featured__title a {
  color: var(--sn-text);
  text-decoration: none;
}

.blog-featured__title a:hover {
  color: var(--sn-gold-bright);
}

.blog-featured__excerpt {
  font-size: 14.5px;
  line-height: 1.7;
  color: var(--sn-text-muted);
  margin: 0;
}

.blog-featured__meta {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12.5px;
  color: var(--sn-text-muted);
}

.blog-featured__cta {
  margin-top: 8px;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  width: fit-content;
  font-size: 13px;
  font-weight: 600;
  letter-spacing: 0.04em;
  text-transform: uppercase;
  color: var(--sn-gold);
  text-decoration: none;
}

/* Grid */
.blog-grid-section__head {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  margin-bottom: 24px;
}

.blog-grid-section__head h2 {
  font-family: var(--sn-font-display);
  font-size: 26px;
  font-weight: 600;
  margin: 0;
  color: var(--sn-text);
}

.blog-grid-section__count {
  font-size: 13px;
  color: var(--sn-text-muted);
}

.blog-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 24px;
}

.blog-empty {
  padding: 60px 0;
  text-align: center;
  color: var(--sn-text-muted);
}

@media (max-width: 980px) {
  .blog-featured {
    grid-template-columns: 1fr;
  }

  .blog-featured__media {
    border-right: none;
    border-bottom: 1px solid var(--sn-border);
    min-height: 200px;
  }

  .blog-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 640px) {
  .blog-grid {
    grid-template-columns: 1fr;
  }

  .blog-featured__content {
    padding: 28px 24px;
  }
}
</style>