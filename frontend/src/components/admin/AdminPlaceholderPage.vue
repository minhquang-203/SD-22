<script setup>
import PageHeader from '@/components/ui/PageHeader.vue'

defineProps({
  title: { type: String, required: true },
  subtitle: { type: String, default: '' },
  icon: { type: String, default: '' },
})
</script>

<template>
  <div class="space-y-5">
    <PageHeader :title="title" :description="subtitle || 'Trang đang được phát triển.'" />

    <!-- <div class="admin-card admin-card--rounded">
      <div class="p-10 text-center">
        <p v-if="icon" class="text-3xl mb-3">{{ icon }}</p>
        <p class="text-[var(--admin-muted)] text-sm">Trang đang được phát triển.</p>
      </div>
    </div> -->
  </div>
</template>
