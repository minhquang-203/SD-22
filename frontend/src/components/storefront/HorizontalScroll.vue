<script setup>
import { nextTick, onMounted, onUnmounted, ref, watch } from 'vue'
import { Icon } from '@iconify/vue'

const props = defineProps({
  ariaLabel: { type: String, default: 'Cuộn ngang' },
  itemCount: { type: Number, default: 0 },
})

const track = ref(null)
const canScrollLeft = ref(false)
const canScrollRight = ref(false)
const hasOverflow = ref(false)
const fitsInView = ref(true)

function updateScrollState() {
  const el = track.value
  if (!el) return
  const overflow = el.scrollWidth > el.clientWidth + 4
  hasOverflow.value = overflow
  fitsInView.value = !overflow
  canScrollLeft.value = overflow && el.scrollLeft > 4
  canScrollRight.value = overflow && el.scrollLeft < el.scrollWidth - el.clientWidth - 4
}

function scrollBy(dx) {
  track.value?.scrollBy({ left: dx, behavior: 'smooth' })
}

let resizeObserver

onMounted(() => {
  nextTick(updateScrollState)
  resizeObserver = new ResizeObserver(() => updateScrollState())
  if (track.value) {
    resizeObserver.observe(track.value)
    track.value.addEventListener('scroll', updateScrollState, { passive: true })
  }
})

onUnmounted(() => {
  resizeObserver?.disconnect()
  track.value?.removeEventListener('scroll', updateScrollState)
})

watch(() => props.itemCount, () => nextTick(updateScrollState))
</script>

<template>
  <div class="sf-hscroll" :class="{ 'sf-hscroll--fits': fitsInView }">
    <button
      v-show="hasOverflow && canScrollLeft"
      type="button"
      class="sf-hscroll__arrow sf-hscroll__arrow--left"
      aria-label="Cuộn trái"
      @click="scrollBy(-300)"
    >
      <Icon icon="solar:alt-arrow-left-linear" width="20" />
    </button>
    <div
      ref="track"
      class="sf-hscroll__track"
      :class="{ 'sf-hscroll__track--spread': fitsInView }"
      :aria-label="ariaLabel"
    >
      <slot />
    </div>
    <button
      v-show="hasOverflow && canScrollRight"
      type="button"
      class="sf-hscroll__arrow sf-hscroll__arrow--right"
      aria-label="Cuộn phải"
      @click="scrollBy(300)"
    >
      <Icon icon="solar:alt-arrow-right-linear" width="20" />
    </button>
  </div>
</template>
