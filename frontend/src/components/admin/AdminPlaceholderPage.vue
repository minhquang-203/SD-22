<script setup>
defineProps({
  title: { type: String, required: true },
  subtitle: { type: String, default: '' },
  icon: { type: String, default: '🚧' },
})
</script>

<template>
  <div class="space-y-4">
    <div class="admin-page-header">
      <div class="admin-page-header__icon">{{ icon }}</div>
      <div class="flex-1 min-w-0">
        <h1 class="admin-page-title">{{ title }}</h1>
        <p v-if="subtitle" class="admin-page-subtitle">{{ subtitle }}</p>
      </div>
    </div>

    <div class="admin-card admin-card--rounded">
      <div class="p-10 text-center">
        <p class="text-[var(--admin-muted)] text-sm">Trang đang được phát triển.</p>
      </div>
    </div>
  </div>
</template>
