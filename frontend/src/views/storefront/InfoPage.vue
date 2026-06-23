<script setup>
import { computed, reactive } from 'vue'
import { RouterLink, useRoute } from 'vue-router'
import {
  CONTACT_FAKE,
  getGroupForPage,
  getInfoPageByPath,
  INFO_GROUPS,
  STORES_FAKE,
} from '@/constants/storefrontInfo'

const route = useRoute()
const page = computed(() => getInfoPageByPath(route.path))
const group = computed(() => getGroupForPage(page.value))

const contactForm = reactive({ hoTen: '', email: '', noiDung: '' })

function onContactSubmit(e) {
  e.preventDefault()
  contactForm.hoTen = ''
  contactForm.email = ''
  contactForm.noiDung = ''
}
</script>

<template>
  <div v-if="page" class="sf-info-page">
    <div class="sf-container">
      <nav class="sf-breadcrumb">
        <RouterLink to="/">Trang chủ</RouterLink>
        <span>/</span>
        <span>{{ group?.label }}</span>
        <span>/</span>
        <span>{{ page.title }}</span>
      </nav>

      <div class="sf-info-layout">
        <aside class="sf-info-sidebar">
          <div v-for="g in INFO_GROUPS" :key="g.id" class="sf-info-sidebar__group">
            <p class="sf-info-sidebar__heading">{{ g.label }}</p>
            <RouterLink
              v-for="link in g.links"
              :key="link.path"
              :to="link.path"
              class="sf-info-sidebar__link"
              :class="{ active: route.path === link.path }"
            >
              {{ link.title }}
            </RouterLink>
          </div>
        </aside>

        <article class="sf-info-content">
          <h1 class="sf-info-content__title">{{ page.title }}</h1>

          <template v-for="(block, idx) in page.blocks" :key="idx">
            <template v-if="block.type === 'text'">
              <p v-for="(para, pi) in block.paragraphs" :key="`${idx}-${pi}`" class="sf-info-p">{{ para }}</p>
            </template>
            <h2 v-else-if="block.type === 'heading'" class="sf-info-h2">{{ block.text }}</h2>
            <ul v-else-if="block.type === 'list'" class="sf-info-list">
              <li v-for="(item, li) in block.items" :key="li">{{ item }}</li>
            </ul>
          </template>

          <div v-if="page.showContactForm" class="sf-info-contact">
            <div class="sf-info-contact__cards">
              <div class="sf-info-contact__card">
                <strong>Hotline</strong>
                <p>{{ CONTACT_FAKE.hotline }}</p>
              </div>
              <div class="sf-info-contact__card">
                <strong>Email</strong>
                <p>{{ CONTACT_FAKE.email }}</p>
              </div>
              <div class="sf-info-contact__card">
                <strong>Trụ sở</strong>
                <p>{{ CONTACT_FAKE.address }}</p>
              </div>
              <div class="sf-info-contact__card">
                <strong>Giờ làm việc</strong>
                <p>{{ CONTACT_FAKE.hours }}</p>
              </div>
            </div>

            <form class="sf-info-form" @submit="onContactSubmit">
              <h2 class="sf-info-h2">Gửi tin nhắn</h2>
              <div class="sf-field">
                <label for="contact-name">Họ tên</label>
                <input id="contact-name" v-model="contactForm.hoTen" type="text" required />
              </div>
              <div class="sf-field">
                <label for="contact-email">Email</label>
                <input id="contact-email" v-model="contactForm.email" type="email" required />
              </div>
              <div class="sf-field">
                <label for="contact-msg">Nội dung</label>
                <textarea id="contact-msg" v-model="contactForm.noiDung" rows="4" required />
              </div>
              <button type="submit" class="btn-soleil btn-soleil--espresso"><span>Gửi liên hệ</span></button>
              <p class="sf-info-form__hint">Biểu mẫu chỉ mang tính minh họa — chưa kết nối backend.</p>
            </form>
          </div>

          <div v-if="page.showStores" class="sf-info-stores">
            <div v-for="store in STORES_FAKE" :key="store.name" class="sf-info-store">
              <span class="sf-info-store__city">{{ store.city }}</span>
              <h3 class="sf-info-store__name">{{ store.name }}</h3>
              <p>{{ store.address }}</p>
              <p><strong>Điện thoại:</strong> {{ store.phone }}</p>
              <p><strong>Giờ mở cửa:</strong> {{ store.hours }}</p>
            </div>
          </div>
        </article>
      </div>
    </div>
  </div>
</template>
