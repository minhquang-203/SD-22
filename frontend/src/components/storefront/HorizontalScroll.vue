<script setup>
import { ref } from 'vue'
import { Icon } from '@iconify/vue'

defineProps({
  ariaLabel: { type: String, default: 'Cuộn ngang' },
})

const track = ref(null)

function scrollBy(dx) {
  track.value?.scrollBy({ left: dx, behavior: 'smooth' })
}
</script>

<template>
  <div class="sf-hscroll">
    <button type="button" class="sf-hscroll__arrow sf-hscroll__arrow--left" aria-label="Cuộn trái" @click="scrollBy(-280)">
      <Icon icon="solar:alt-arrow-left-linear" width="20" />
    </button>
    <div ref="track" class="sf-hscroll__track" :aria-label="ariaLabel">
      <slot />
    </div>
    <button type="button" class="sf-hscroll__arrow sf-hscroll__arrow--right" aria-label="Cuộn phải" @click="scrollBy(280)">
      <Icon icon="solar:alt-arrow-right-linear" width="20" />
    </button>
  </div>
</template>
