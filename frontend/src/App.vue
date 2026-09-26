<script setup>
import { NConfigProvider } from 'naive-ui'
import { naiveThemeOverrides } from '@/theme/naiveTheme'
import ErrorBoundary from '@/components/ui/ErrorBoundary.vue'
</script>

<template>
  <NConfigProvider class="h-full" :theme-overrides="naiveThemeOverrides">
    <ErrorBoundary variant="storefront">
      <router-view />
    </ErrorBoundary>
  </NConfigProvider>
</template>
