<template>
  <div class="blog-detail">
    <BlogIconSprite />

    <template v-if="post">
      <!-- Breadcrumb -->
      <nav class="blog-detail__breadcrumb" aria-label="Breadcrumb">
        <RouterLink to="/">Trang chủ</RouterLink>
        <span>/</span>
        <RouterLink to="/blog">Blog</RouterLink>
        <span>/</span>
        <span class="is-current">{{ post.title }}</span>
      </nav>

      <!-- Header -->
      <header class="blog-detail__header">
        <span class="blog-detail__tag">{{ post.tag }}</span>
        <h1 class="blog-detail__title">{{ post.title }}</h1>
        <p class="blog-detail__excerpt">{{ post.excerpt }}</p>

        <div class="blog-detail__meta">
          <span>{{ post.author }}</span>
          <span class="blog-card__dot">·</span>
          <span>{{ formatDate(post.publishDate) }}</span>
          <span class="blog-card__dot">·</span>
          <span>{{ post.readTime }}</span>
        </div>
      </header>

      <!-- Hero icon banner -->
      <div class="blog-detail__banner">
        <svg viewBox="0 0 48 48" fill="none" class="blog-detail__banner-icon">
          <use :href="`#blog-icon-${post.icon}`" />
        </svg>
      </div>

      <!-- Content -->
      <article class="blog-detail__content">
        <section v-for="(section, idx) in post.sections" :key="idx" class="blog-detail__section">
          <h2>{{ section.heading }}</h2>
          <p v-for="(para, pIdx) in section.paragraphs" :key="pIdx">{{ para }}</p>
        </section>
      </article>

      <!-- Disclaimer -->
      <div class="blog-detail__disclaimer">
        <p>
          Nội dung trong bài viết mang tính chất tham khảo chung về kiến thức chống nắng và
          thời tiết, không thay thế cho tư vấn từ bác sĩ da liễu. Nếu da bạn có dấu hiệu kích
          ứng hoặc bệnh lý, vui lòng tham khảo ý kiến chuyên gia trước khi sử dụng sản phẩm.
        </p>
      </div>

      <!-- Related posts -->
      <section v-if="relatedPosts.length" class="blog-detail__related">
        <h2>Bài viết liên quan</h2>
        <div class="blog-grid">
          <BlogCard v-for="related in relatedPosts" :key="related.id" :post="related" />
        </div>
      </section>

      <RouterLink to="/blog" class="blog-detail__back">
        <svg width="14" height="14" viewBox="0 0 14 14" fill="none">
          <path
            d="M11.5 7H2.5M2.5 7L6.5 3M2.5 7L6.5 11"
            stroke="currentColor"
            stroke-width="1.4"
            stroke-linecap="round"
            stroke-linejoin="round"
          />
        </svg>
        Quay lại danh sách Blog
      </RouterLink>
    </template>

    <!-- Not found -->
    <div v-else class="blog-detail__not-found">
      <h1>Không tìm thấy bài viết</h1>
      <p>Bài viết bạn tìm có thể đã bị xoá hoặc đường dẫn không đúng.</p>
      <RouterLink to="/blog" class="blog-detail__back">Quay lại Blog</RouterLink>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { getPostBySlug, getRelatedPosts } from '@/constants/blogPosts'
import BlogCard from '@/components/storefront/blog/BlogCard.vue'
import BlogIconSprite from '@/components/storefront/blog/BlogIconSprite.vue'

const route = useRoute()

const post = computed(() => getPostBySlug(route.params.slug))
const relatedPosts = computed(() => getRelatedPosts(post.value, 3))

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
.blog-detail {
  max-width: 720px;
  margin: 0 auto;
  padding: 2.5rem 1.25rem 5.5rem;
  color: var(--sf-charcoal, #1a1814);
  font-family: var(--sf-font-body, 'Be Vietnam Pro', sans-serif);
}

.blog-detail__breadcrumb {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
  font-size: 0.8125rem;
  color: var(--sf-light-mid, #8a8278);
  margin-bottom: 1.75rem;
}

.blog-detail__breadcrumb a {
  color: var(--sf-light-mid, #8a8278);
  text-decoration: none;
}

.blog-detail__breadcrumb a:hover {
  color: var(--sf-gold-dark, #9e7340);
}

.blog-detail__breadcrumb .is-current {
  color: var(--sf-espresso, #241a12);
  max-width: 280px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.blog-detail__tag {
  display: inline-block;
  font-size: 0.6875rem;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: var(--sf-espresso, #241a12);
  background: var(--sf-gold, #c9a96e);
  padding: 0.35rem 0.75rem;
  border-radius: 999px;
  font-weight: 600;
  margin-bottom: 1rem;
}

.blog-detail__title {
  font-family: var(--sf-font-display, 'Playfair Display', serif);
  font-size: clamp(1.75rem, 4vw, 2.5rem);
  line-height: 1.25;
  font-weight: 600;
  margin: 0 0 1rem;
  color: var(--sf-espresso, #241a12);
}

.blog-detail__excerpt {
  font-size: 1.05rem;
  line-height: 1.75;
  color: var(--sf-mid, #5a5248);
  margin: 0 0 1.15rem;
}

.blog-detail__meta {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 0.5rem;
  font-size: 0.875rem;
  color: var(--sf-light-mid, #8a8278);
  padding-bottom: 1.5rem;
  border-bottom: 1px solid var(--sf-hairline, #ede5d8);
}

.blog-detail__banner {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 200px;
  margin: 2rem 0 2.5rem;
  border-radius: 14px;
  border: 1px solid var(--sf-hairline, #ede5d8);
  background: linear-gradient(145deg, #f6ead4, #e8d5a8);
}

.blog-detail__banner-icon {
  width: 72px;
  height: 72px;
  color: var(--sf-espresso, #241a12);
}

.blog-detail__content {
  display: flex;
  flex-direction: column;
  gap: 2rem;
  max-width: 65ch;
}

.blog-detail__section h2 {
  font-family: var(--sf-font-display, 'Playfair Display', serif);
  font-size: 1.4rem;
  font-weight: 600;
  color: var(--sf-espresso, #241a12);
  margin: 0 0 0.85rem;
  line-height: 1.35;
}

.blog-detail__section p {
  font-size: 1.0625rem;
  line-height: 1.8;
  color: var(--sf-mid, #5a5248);
  margin: 0 0 1rem;
}

.blog-detail__section p:last-child {
  margin-bottom: 0;
}

.blog-detail__disclaimer {
  margin-top: 2.5rem;
  padding: 1.1rem 1.25rem;
  border-left: 3px solid var(--sf-gold, #c9a96e);
  background: var(--sf-cream, #f9f5f0);
  border-radius: 0 10px 10px 0;
}

.blog-detail__disclaimer p {
  margin: 0;
  font-size: 0.875rem;
  line-height: 1.7;
  color: var(--sf-mid, #5a5248);
}

.blog-detail__related {
  margin-top: 3.5rem;
}

.blog-detail__related h2 {
  font-family: var(--sf-font-display, 'Playfair Display', serif);
  font-size: 1.5rem;
  font-weight: 600;
  margin: 0 0 1.25rem;
  color: var(--sf-espresso, #241a12);
}

.blog-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 1.15rem;
}

.blog-detail__back {
  display: inline-flex;
  align-items: center;
  gap: 0.4rem;
  margin-top: 2.5rem;
  font-size: 0.875rem;
  font-weight: 600;
  color: var(--sf-gold-dark, #9e7340);
  text-decoration: none;
}

.blog-detail__back:hover {
  color: var(--sf-accent, #a33b1c);
}

.blog-detail__not-found {
  text-align: center;
  padding: 4rem 1rem;
}

.blog-detail__not-found h1 {
  font-family: var(--sf-font-display, 'Playfair Display', serif);
  margin-bottom: 0.75rem;
  color: var(--sf-espresso, #241a12);
}

.blog-detail__not-found p {
  color: var(--sf-mid, #5a5248);
  margin-bottom: 1.5rem;
}

@media (max-width: 800px) {
  .blog-grid {
    grid-template-columns: 1fr;
  }
}
</style>
