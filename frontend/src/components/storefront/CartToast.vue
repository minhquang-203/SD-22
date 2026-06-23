<script setup>
import { useToastState, dismissToast } from '@/composables/useToast'

const { toasts } = useToastState()
</script>

<template>
  <Teleport to="body">
    <div class="sf-toast-stack" aria-live="polite">
      <div
        v-for="item in toasts"
        :key="item.id"
        class="sf-toast-item"
        :class="`sf-toast-item--${item.type}`"
        @click="dismissToast(item.id)"
      >
        {{ item.message }}
      </div>
    </div>
  </Teleport>
</template>
