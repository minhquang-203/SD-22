<script setup>
import { useToastState, dismissToast } from '@/composables/useToast'

const { toasts } = useToastState()
</script>

<template>
  <Teleport to="body">
    <div class="admin-toast-stack" aria-live="polite">
      <div
        v-for="item in toasts"
        :key="item.id"
        class="admin-toast-item"
        :class="`admin-toast-item--${item.type}`"
        @click="dismissToast(item.id)"
      >
        {{ item.message }}
      </div>
    </div>
  </Teleport>
</template>
