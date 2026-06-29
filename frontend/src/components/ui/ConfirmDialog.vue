<script setup>
import { useConfirmState, handleConfirmOk, handleConfirmCancel } from '@/composables/useConfirm'

const { visible, options } = useConfirmState()
</script>

<template>
  <Teleport to="body">
    <div v-if="visible" class="confirm-dialog-overlay">
      <div class="confirm-dialog-backdrop" @click="handleConfirmCancel" />
      <div class="confirm-dialog-panel" role="dialog" aria-modal="true">
        <h2 class="confirm-dialog-title">{{ options.title }}</h2>
        <p class="confirm-dialog-message">{{ options.message }}</p>
        <div class="confirm-dialog-actions">
          <button type="button" class="soleil-btn-outline" @click="handleConfirmCancel">
            {{ options.cancelText }}
          </button>
          <button
            type="button"
            class="confirm-dialog-confirm"
            :class="{ 'confirm-dialog-confirm--danger': options.danger }"
            @click="handleConfirmOk"
          >
            {{ options.confirmText }}
          </button>
        </div>
      </div>
    </div>
  </Teleport>
</template>

<style scoped>
.confirm-dialog-title {
  font-family: var(--font-serif, Georgia, serif);
  font-size: 1.2rem;
  font-weight: 600;
  color: var(--ink, #1e1510);
  margin-bottom: 0.75rem;
}
.confirm-dialog-message {
  font-size: 0.9rem;
  line-height: 1.55;
  color: rgba(30, 21, 16, 0.72);
  margin-bottom: 1.5rem;
}
.confirm-dialog-actions {
  display: flex;
  justify-content: flex-end;
  gap: 0.75rem;
}
.confirm-dialog-confirm {
  padding: 0.65rem 1.25rem;
  border-radius: 0.5rem;
  font-size: 0.875rem;
  font-weight: 600;
  letter-spacing: 0.04em;
  border: none;
  cursor: pointer;
  background: var(--warm-tan, #c9a96e);
  color: var(--ink, #1e1510);
  transition: background 0.2s, opacity 0.2s;
}
.confirm-dialog-confirm:hover {
  background: var(--bronze, #a67c3d);
  color: #fff;
}
.confirm-dialog-confirm--danger {
  background: #b44a4a;
  color: #fff;
}
.confirm-dialog-confirm--danger:hover {
  background: #933838;
}
</style>
