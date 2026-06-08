<script setup>
import { NConfigProvider } from 'naive-ui'
import { naiveThemeOverrides } from '@/theme/naiveTheme'
</script>

<template>
  <NConfigProvider :theme-overrides="naiveThemeOverrides">
    <router-view />
  </NConfigProvider>
</template>
