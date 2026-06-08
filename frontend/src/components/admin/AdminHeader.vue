<script setup>
defineProps({
  title: { type: String, default: 'SUNOVA Admin' },
  breadcrumb: { type: String, default: '' },
})

const emit = defineEmits(['toggle-sidebar'])
</script>

<template>
  <header
    class="h-[60px] bg-[var(--admin-card)] border-b flex items-center justify-between px-5 shrink-0"
    style="border-color: var(--admin-border)"
  >
    <div class="flex items-center gap-3 min-w-0">
      <button
        type="button"
        class="admin-btn admin-btn-default !px-2.5 !py-2"
        @click="emit('toggle-sidebar')"
      >
        ☰
      </button>
      <div class="min-w-0">
        <div class="font-semibold text-[15px] truncate text-[var(--admin-text)]">{{ title }}</div>
        <div v-if="breadcrumb" class="text-xs truncate">
          <span class="text-[var(--admin-muted)]">{{ breadcrumb }}</span>
        </div>
      </div>
    </div>

    <div class="flex items-center gap-3 text-sm text-[var(--admin-muted)]">
      <span class="hidden sm:inline">Quản trị viên</span>
      <div class="w-8 h-8 rounded-full bg-[var(--admin-primary)]/10 text-[var(--admin-primary)] flex items-center justify-center font-semibold">
        A
      </div>
    </div>
  </header>
</template>
