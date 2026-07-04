<template>
  <RouterLink :to="`/blog/${post.slug}`" class="blog-card">
    <div class="blog-card__media">
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
        <span class="blog-card__tag">{{ post.tag }}</span>
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
defineProps({
  post: {
    type: Object,
    required: true,
  },
})
</script>

<style scoped>
.blog-card {
  display: flex;
  flex-direction: column;
  text-decoration: none;
  color: inherit;
  background: var(--sn-bg-elevated, #241a12);
  border: 1px solid var(--sn-border, rgba(201, 163, 106, 0.18));
  border-radius: 4px;
  overflow: hidden;
  transition: border-color 0.25s ease, transform 0.25s ease;
  height: 100%;
}

.blog-card:hover {
  border-color: var(--sn-gold, #c9a36a);
  transform: translateY(-3px);
}

.blog-card:hover .blog-card__icon {
  transform: scale(1.08);
}

.blog-card__media {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 140px;
  background: radial-gradient(
    circle at 30% 20%,
    rgba(201, 163, 106, 0.16),
    transparent 65%
  ),
  var(--sn-bg, #1b130c);
  border-bottom: 1px solid var(--sn-border, rgba(201, 163, 106, 0.18));
}

.blog-card__icon {
  width: 44px;
  height: 44px;
  color: var(--sn-gold, #c9a36a);
  transition: transform 0.3s ease;
}

.blog-card__body {
  display: flex;
  flex-direction: column;
  gap: 10px;
  padding: 20px 22px 22px;
  flex: 1;
}

.blog-card__meta {
  display: flex;
  align-items: center;
  gap: 8px;
  font-family: var(--sn-font-body, 'Be Vietnam Pro', sans-serif);
  font-size: 11px;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: var(--sn-gold, #c9a36a);
}

.blog-card__dot {
  color: var(--sn-text-muted, #8a7a64);
}

.blog-card__read-time {
  color: var(--sn-text-muted, #b8a890);
  text-transform: none;
  letter-spacing: normal;
}

.blog-card__title {
  font-family: var(--sn-font-display, 'Playfair Display', serif);
  font-size: 19px;
  line-height: 1.35;
  font-weight: 600;
  color: var(--sn-text, #f3ead9);
  margin: 0;
}

.blog-card__excerpt {
  font-family: var(--sn-font-body, 'Be Vietnam Pro', sans-serif);
  font-size: 13.5px;
  line-height: 1.6;
  color: var(--sn-text-muted, #b8a890);
  margin: 0;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.blog-card__cta {
  margin-top: auto;
  padding-top: 6px;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-family: var(--sn-font-body, 'Be Vietnam Pro', sans-serif);
  font-size: 12.5px;
  font-weight: 600;
  letter-spacing: 0.04em;
  text-transform: uppercase;
  color: var(--sn-gold, #c9a36a);
}
</style>