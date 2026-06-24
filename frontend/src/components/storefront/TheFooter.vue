<script setup>
import { ref } from 'vue'
import { RouterLink } from 'vue-router'
import { Icon } from '@iconify/vue'

const email = ref('')

function onNewsletter(e) {
  e.preventDefault()
  email.value = ''
}
</script>

<template>
  <footer class="sf-footer sf-footer--rich">
    <div class="sf-footer__inner">
      <div class="row g-4 g-lg-5">
        <div class="col-lg-4 col-md-6">
          <div class="sf-footer__brand">SUN<span>OVA</span></div>
          <p class="sf-footer__desc">
            Thương hiệu chống nắng và chăm sóc da cao cấp, kết hợp khoa học bảo vệ tia UV
            với kết cấu nhẹ, phù hợp khí hậu nhiệt đới.
          </p>
          <p class="sf-footer__slogan">Vẻ đẹp dưới ánh nắng — Soleil by SUNOVA</p>
        </div>

        <div class="col-6 col-lg-2 col-md-3">
          <div class="sf-footer__title">Chính sách</div>
          <RouterLink class="sf-footer__link" to="/chinh-sach-chung">Chính sách chung</RouterLink>
          <RouterLink class="sf-footer__link" to="/thanh-toan">Thanh toán</RouterLink>
          <RouterLink class="sf-footer__link" to="/doi-tra-hoan-tien">Đổi trả &amp; hoàn tiền</RouterLink>
          <RouterLink class="sf-footer__link" to="/van-chuyen">Vận chuyển</RouterLink>
          <RouterLink class="sf-footer__link" to="/bao-mat">Bảo mật</RouterLink>
        </div>

        <div class="col-6 col-lg-2 col-md-3">
          <div class="sf-footer__title">Về SUNOVA</div>
          <RouterLink class="sf-footer__link" to="/gioi-thieu">Giới thiệu</RouterLink>
          <span class="sf-footer__link sf-footer__link--muted" title="Sắp ra mắt">Tin tức</span>
          <RouterLink class="sf-footer__link" to="/lien-he">Liên hệ</RouterLink>
          <RouterLink class="sf-footer__link" to="/he-thong-cua-hang">Hệ thống cửa hàng</RouterLink>
        </div>

        <div class="col-lg-4 col-md-12">
          <div class="sf-footer__title">Đăng ký nhận bản tin</div>
          <p class="sf-footer__news-desc">Nhận ưu đãi và mẹo chăm sóc da mỗi tuần.</p>
          <form class="sf-footer__newsletter" @submit="onNewsletter">
            <input v-model="email" type="email" placeholder="Email của bạn" aria-label="Email đăng ký" />
            <button type="submit">Đăng ký</button>
          </form>
          <div class="sf-footer__social">
            <a href="#" aria-label="Facebook"><Icon icon="mdi:facebook" width="18" /></a>
            <a href="#" aria-label="Instagram"><Icon icon="mdi:instagram" width="18" /></a>
            <a href="#" aria-label="YouTube"><Icon icon="mdi:youtube" width="18" /></a>
            <a href="#" aria-label="TikTok"><Icon icon="ic:baseline-tiktok" width="18" /></a>
            <a href="#" aria-label="Zalo"><Icon icon="simple-icons:zalo" width="18" /></a>
          </div>
        </div>
      </div>

      <hr class="sf-footer__divider" />
      <div class="sf-footer__copy">© 2026 SUNOVA. Bảo vệ làn da bạn mỗi ngày.</div>
    </div>
  </footer>
</template>
