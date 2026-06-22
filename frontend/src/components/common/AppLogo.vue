<script setup>
import { computed } from 'vue'
import markSrc from '@/assets/logo/sunova_mark.svg'

defineOptions({ inheritAttrs: false })

const props = defineProps({
  variant: {
    type: String,
    default: 'dark',
    validator: (value) => ['dark', 'light'].includes(value),
  },
  size: {
    type: Number,
    default: 40,
  },
  collapsed: {
    type: Boolean,
    default: false,
  },
})

const markStyle = computed(() => ({
  height: `${props.size}px`,
  width: 'auto',
}))

const titleStyle = computed(() => ({
  fontSize: `${Math.round(props.size * 0.66)}px`,
  color: props.variant === 'dark' ? '#F2EAD9' : '#1E1510',
}))

const taglineStyle = computed(() => ({
  color: props.variant === 'dark' ? '#B7A88C' : '#9E8E7E',
}))
</script>

<template>
  <div
    class="app-logo"
    :class="{ 'app-logo--collapsed': collapsed }"
    v-bind="$attrs"
    aria-label="SUNOVA"
  >
    <img
      :src="markSrc"
      alt=""
      class="app-logo__mark shrink-0"
      :style="markStyle"
      aria-hidden="true"
    />
    <div v-if="!collapsed" class="app-logo__text min-w-0">
      <div class="app-logo__title" :style="titleStyle">SUNOVA</div>
      <div class="app-logo__tagline" :style="taglineStyle">
        CHỐNG NẮNG &amp; CHĂM SÓC DA
      </div>
    </div>
  </div>
</template>

<style scoped>
.app-logo {
  display: flex;
  align-items: center;
  gap: 12px;
}

.app-logo--collapsed {
  justify-content: center;
  width: 100%;
  gap: 0;
}

.app-logo__mark {
  display: block;
  flex-shrink: 0;
}

.app-logo__title {
  font-family: 'Cormorant Garamond', serif;
  font-weight: 600;
  letter-spacing: 0.14em;
  line-height: 1;
  text-transform: uppercase;
}

.app-logo__tagline {
  font-family: 'DM Sans', sans-serif;
  font-size: 9px;
  font-weight: 400;
  letter-spacing: 0.22em;
  line-height: 1.35;
  text-transform: uppercase;
  margin-top: 4px;
}
</style>
