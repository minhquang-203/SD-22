<script setup>
import { NConfigProvider } from 'naive-ui'
import { naiveThemeOverrides } from '@/theme/naiveTheme'
</script>

<template>
  <NConfigProvider class="h-full" :theme-overrides="naiveThemeOverrides">
    <router-view />
  </NConfigProvider>
</template>
