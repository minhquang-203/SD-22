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
  --sn-bg: #1b130c;
  --sn-bg-elevated: #241a12;
  --sn-border: rgba(201, 163, 106, 0.18);
  --sn-gold: #c9a36a;
  --sn-gold-bright: #e0bb84;
  --sn-text: #f3ead9;
  --sn-text-muted: #b8a890;
  --sn-font-display: 'Playfair Display', Georgia, serif;
  --sn-font-body: 'Be Vietnam Pro', 'Inter', sans-serif;

  max-width: 820px;
  margin: 0 auto;
  padding: 40px 24px 100px;
  color: var(--sn-text);
  font-family: var(--sn-font-body);
}

/* Breadcrumb */
.blog-detail__breadcrumb {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  font-size: 12.5px;
  color: var(--sn-text-muted);
  margin-bottom: 28px;
}

.blog-detail__breadcrumb a {
  color: var(--sn-text-muted);
  text-decoration: none;
}

.blog-detail__breadcrumb a:hover {
  color: var(--sn-gold);
}

.blog-detail__breadcrumb .is-current {
  color: var(--sn-text);
  max-width: 320px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* Header */
.blog-detail__tag {
  display: inline-block;
  font-size: 11px;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: #1b130c;
  background: var(--sn-gold);
  padding: 5px 12px;
  border-radius: 999px;
  font-weight: 600;
  margin-bottom: 16px;
}

.blog-detail__title {
  font-family: var(--sn-font-display);
  font-size: clamp(28px, 4vw, 40px);
  line-height: 1.25;
  font-weight: 600;
  margin: 0 0 16px;
}

.blog-detail__excerpt {
  font-size: 15.5px;
  line-height: 1.75;
  color: var(--sn-text-muted);
  margin: 0 0 18px;
}

.blog-detail__meta {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: var(--sn-text-muted);
  padding-bottom: 28px;
  border-bottom: 1px solid var(--sn-border);
}

/* Banner */
.blog-detail__banner {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 220px;
  margin: 32px 0;
  border-radius: 4px;
  border: 1px solid var(--sn-border);
  background: radial-gradient(
      circle at 30% 25%,
      rgba(201, 163, 106, 0.18),
      transparent 60%
    ),
    var(--sn-bg-elevated);
}

.blog-detail__banner-icon {
  width: 84px;
  height: 84px;
  color: var(--sn-gold);
}

/* Content */
.blog-detail__content {
  display: flex;
  flex-direction: column;
  gap: 32px;
}

.blog-detail__section h2 {
  font-family: var(--sn-font-display);
  font-size: 22px;
  font-weight: 600;
  color: var(--sn-text);
  margin: 0 0 14px;
}

.blog-detail__section p {
  font-size: 15px;
  line-height: 1.85;
  color: #d8cab3;
  margin: 0 0 14px;
}

.blog-detail__section p:last-child {
  margin-bottom: 0;
}

/* Disclaimer */
.blog-detail__disclaimer {
  margin-top: 40px;
  padding: 18px 22px;
  border-left: 3px solid var(--sn-gold);
  background: var(--sn-bg-elevated);
  border-radius: 0 4px 4px 0;
}

.blog-detail__disclaimer p {
  margin: 0;
  font-size: 13px;
  line-height: 1.7;
  color: var(--sn-text-muted);
}

/* Related */
.blog-detail__related {
  margin-top: 64px;
}

.blog-detail__related h2 {
  font-family: var(--sn-font-display);
  font-size: 24px;
  font-weight: 600;
  margin: 0 0 22px;
}

.blog-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 20px;
}

/* Back link */
.blog-detail__back {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  margin-top: 48px;
  font-size: 13.5px;
  font-weight: 600;
  letter-spacing: 0.02em;
  color: var(--sn-gold);
  text-decoration: none;
}

.blog-detail__back:hover {
  color: var(--sn-gold-bright);
}

/* Not found */
.blog-detail__not-found {
  text-align: center;
  padding: 80px 0;
}

.blog-detail__not-found h1 {
  font-family: var(--sn-font-display);
  font-size: 28px;
  margin-bottom: 12px;
}

.blog-detail__not-found p {
  color: var(--sn-text-muted);
  margin-bottom: 24px;
}

@media (max-width: 760px) {
  .blog-grid {
    grid-template-columns: 1fr;
  }
}
</style>