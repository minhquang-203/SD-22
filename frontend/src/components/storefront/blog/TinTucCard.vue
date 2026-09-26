<script setup>
import { computed } from 'vue'
import { RouterLink } from 'vue-router'
import { formatTinTucDate } from '@/constants/tinTuc'

const props = defineProps({
  bai: { type: Object, required: true },
})

const tone = computed(() => {
  const m = props.bai.danhMuc || ''
  if (m.includes('SUNOVA')) return 'gold'
  if (m.includes('Hướng dẫn')) return 'sand'
  return 'sky'
})

const icon = computed(() => props.bai.icon || 'sun')
</script>

<template>
  <RouterLink :to="`/tin-tuc/${bai.slug}`" class="tt-card">
    <div class="tt-card__media" :class="`tt-card__media--${tone}`">
      <img
        v-if="bai.anhBia"
        :src="bai.anhBia"
        :alt="bai.tieuDe"
        class="tt-card__img"
        loading="lazy"
      />
      <template v-else>
        <span class="tt-card__glow" aria-hidden="true" />
        <svg viewBox="0 0 48 48" fill="none" class="tt-card__icon" aria-hidden="true">
          <use :href="`#blog-icon-${icon}`" />
        </svg>
      </template>
    </div>
    <div class="tt-card__body">
      <span class="tt-card__cat">{{ bai.danhMuc }}</span>
      <h3 class="tt-card__title">{{ bai.tieuDe }}</h3>
      <p class="tt-card__excerpt">{{ bai.tomTat }}</p>
      <div class="tt-card__foot">
        <time :datetime="bai.ngay">{{ formatTinTucDate(bai.ngay) }}</time>
        <span class="tt-card__cta">Đọc tiếp →</span>
      </div>
    </div>
  </RouterLink>
</template>

<style scoped>
.tt-card {
  display: flex;
  flex-direction: column;
  height: 100%;
  text-decoration: none;
  color: inherit;
  background: var(--sf-warm-white, #fffaf4);
  border: 1px solid rgba(158, 115, 64, 0.14);
  border-radius: 16px;
  overflow: hidden;
  transition: transform 0.22s ease, box-shadow 0.22s ease;
}

.tt-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 14px 32px rgba(62, 44, 28, 0.1);
}

.tt-card__media {
  position: relative;
  aspect-ratio: 16 / 10;
  display: grid;
  place-items: center;
  overflow: hidden;
}

.tt-card__media--gold {
  background: linear-gradient(145deg, #f3e4c8, #e8d3a8 55%, #d4b87a);
}
.tt-card__media--sky {
  background: linear-gradient(145deg, #f7efe3, #efe0c8 50%, #e2cba0);
}
.tt-card__media--sand {
  background: linear-gradient(145deg, #f5ebe0, #ead9c4 55%, #dcc4a0);
}

.tt-card__img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.tt-card__glow {
  position: absolute;
  inset: 18% 22%;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.35);
  filter: blur(18px);
}

.tt-card__icon {
  position: relative;
  width: 48px;
  height: 48px;
  color: var(--sf-espresso, #3e2c1c);
  opacity: 0.72;
  transition: transform 0.22s ease;
}

.tt-card:hover .tt-card__icon {
  transform: scale(1.06);
}

.tt-card__body {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 16px 16px 18px;
  flex: 1;
}

.tt-card__cat {
  font-size: 11px;
  font-weight: 600;
  letter-spacing: 0.06em;
  text-transform: uppercase;
  color: var(--sf-gold-dark, #9e7340);
}

.tt-card__title {
  margin: 0;
  font-family: var(--sf-font-display, 'Playfair Display', Georgia, serif);
  font-size: 1.1rem;
  font-weight: 600;
  line-height: 1.35;
  color: var(--sf-espresso, #3e2c1c);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.tt-card__excerpt {
  margin: 0;
  font-size: 0.9rem;
  line-height: 1.55;
  color: var(--sf-mid, #5a5248);
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
  flex: 1;
}

.tt-card__foot {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  margin-top: 4px;
  font-size: 0.82rem;
  color: var(--sf-mid, #5a5248);
}

.tt-card__cta {
  font-weight: 600;
  color: var(--sf-gold-dark, #9e7340);
}
</style>
