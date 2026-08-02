<template>
  <RouterLink
    :to="`/blog/${post.slug}`"
    class="blog-card"
    :data-cat="post.category"
  >
    <div class="blog-card__media" :class="`blog-card__media--${tone}`">
      <span class="blog-card__glow" aria-hidden="true" />
      <svg
        class="blog-card__icon"
        viewBox="0 0 48 48"
        fill="none"
        xmlns="http://www.w3.org/2000/svg"
      >
        <use :href="`#blog-icon-${post.icon}`" />
      </svg>
    </div>

    <div class="blog-card__body">
      <div class="blog-card__meta">
        <span class="blog-card__tag" :class="`blog-card__tag--${tone}`">{{ post.tag }}</span>
        <span class="blog-card__dot">·</span>
        <span class="blog-card__date">{{ formatDate(post.publishDate) }}</span>
        <span class="blog-card__dot">·</span>
        <span class="blog-card__read-time">{{ post.readTime }}</span>
      </div>

      <h3 class="blog-card__title">{{ post.title }}</h3>
      <p class="blog-card__excerpt">{{ post.excerpt }}</p>

      <span class="blog-card__cta">
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
      </span>
    </div>
  </RouterLink>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  post: {
    type: Object,
    required: true,
  },
})

const CAT_TONE = {
  'kien-thuc-spf': 'gold',
  'thoi-tiet-da': 'sky',
  'huong-dan-chon': 'sage',
  'mua-mua': 'teal',
  'cham-soc-da': 'coral',
}

const tone = computed(() => CAT_TONE[props.post.category] || 'gold')

function formatDate(dateStr) {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleDateString('vi-VN', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
  })
}
</script>

<style scoped>
.blog-card {
  display: flex;
  flex-direction: column;
  text-decoration: none;
  color: inherit;
  background: var(--sf-warm-white, #fffdfa);
  border: 1px solid var(--sf-hairline, #ede5d8);
  border-radius: 14px;
  overflow: hidden;
  transition: border-color 0.25s ease, transform 0.25s ease, box-shadow 0.25s ease;
  height: 100%;
}

.blog-card:hover {
  border-color: var(--sf-gold, #c9a96e);
  transform: translateY(-4px);
  box-shadow: 0 12px 28px rgba(36, 26, 18, 0.08);
}

.blog-card:hover .blog-card__icon {
  transform: scale(1.08);
}

.blog-card__media {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  height: 148px;
  overflow: hidden;
  border-bottom: 1px solid var(--sf-hairline, #ede5d8);
}

.blog-card__glow {
  position: absolute;
  inset: -20%;
  opacity: 0.55;
  background: radial-gradient(circle at 30% 25%, rgba(255, 255, 255, 0.7), transparent 55%);
  pointer-events: none;
}

.blog-card__media--gold {
  background: linear-gradient(145deg, #f3e6cf 0%, #e8d5a8 55%, #dcc48a 100%);
}
.blog-card__media--sky {
  background: linear-gradient(145deg, #dceaf3 0%, #b9d4e8 55%, #8fb8d4 100%);
}
.blog-card__media--sage {
  background: linear-gradient(145deg, #e4ecdf 0%, #c5d6b8 55%, #a8c294 100%);
}
.blog-card__media--teal {
  background: linear-gradient(145deg, #d7ebea 0%, #aed4d2 55%, #7fb8b5 100%);
}
.blog-card__media--coral {
  background: linear-gradient(145deg, #f5e0d8 0%, #e8bfb2 55%, #d49a88 100%);
}

.blog-card__icon {
  position: relative;
  z-index: 1;
  width: 48px;
  height: 48px;
  color: var(--sf-espresso, #241a12);
  transition: transform 0.3s ease;
}

.blog-card__body {
  display: flex;
  flex-direction: column;
  gap: 10px;
  padding: 1.15rem 1.25rem 1.35rem;
  flex: 1;
}

.blog-card__meta {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 6px;
  font-size: 0.75rem;
  color: var(--sf-light-mid, #8a8278);
}

.blog-card__tag {
  font-weight: 600;
  letter-spacing: 0.04em;
  text-transform: uppercase;
  font-size: 0.6875rem;
}

.blog-card__tag--gold { color: var(--sf-gold-dark, #9e7340); }
.blog-card__tag--sky { color: #3a6ea8; }
.blog-card__tag--sage { color: #4d6b45; }
.blog-card__tag--teal { color: #2f6f6c; }
.blog-card__tag--coral { color: var(--sf-accent, #a33b1c); }

.blog-card__dot {
  opacity: 0.5;
}

.blog-card__title {
  font-family: var(--sf-font-display, 'Playfair Display', serif);
  font-size: 1.15rem;
  line-height: 1.35;
  font-weight: 600;
  color: var(--sf-espresso, #241a12);
  margin: 0;
}

.blog-card__excerpt {
  font-size: 0.875rem;
  line-height: 1.65;
  color: var(--sf-mid, #5a5248);
  margin: 0;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.blog-card__cta {
  margin-top: auto;
  padding-top: 8px;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 0.75rem;
  font-weight: 600;
  letter-spacing: 0.04em;
  text-transform: uppercase;
  color: var(--sf-gold-dark, #9e7340);
}
</style>
