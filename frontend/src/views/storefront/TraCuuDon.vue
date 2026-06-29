<script setup>
import { ref } from 'vue'
import { RouterLink } from 'vue-router'
import OrderCard from '@/components/storefront/OrderCard.vue'
import { traCuuDon } from '@/api/donHangApi'

const ma = ref('')
const sdt = ref('')
const loading = ref(false)
const order = ref(null)
const notFound = ref(false)
const error = ref('')

async function onSearch() {
  const maVal = ma.value.trim()
  const sdtVal = sdt.value.trim()
  if (!maVal || !sdtVal) {
    error.value = 'Vui lòng nhập đầy đủ mã đơn và số điện thoại.'
    return
  }
  loading.value = true
  error.value = ''
  notFound.value = false
  order.value = null
  try {
    const res = await traCuuDon(maVal, sdtVal)
    order.value = res.data
  } catch (e) {
    if (e?.response?.status === 404) {
      notFound.value = true
    } else {
      error.value = 'Không tra cứu được. Vui lòng thử lại sau.'
    }
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="sf-order-page">
    <div class="sf-container">
      <nav class="sf-breadcrumb">
        <RouterLink to="/">Trang chủ</RouterLink>
        <span>/</span>
        <span>Tra cứu đơn hàng</span>
      </nav>

      <h1 class="sf-order-page__title">Tra cứu đơn hàng</h1>
      <p class="sf-order-page__desc">
        Nhập <strong>mã đơn</strong> và <strong>SĐT người nhận</strong> để xem trạng thái đơn hàng.
      </p>

      <form class="sf-order-lookup" @submit.prevent="onSearch">
        <label>
          <span>Mã đơn</span>
          <input v-model="ma" type="text" placeholder="VD: HD001" autocomplete="off" />
        </label>
        <label>
          <span>Số điện thoại</span>
          <input v-model="sdt" type="tel" placeholder="VD: 0911000001" autocomplete="tel" />
        </label>
        <button type="submit" class="sf-order-lookup__btn" :disabled="loading">
          {{ loading ? 'Đang tra cứu...' : 'Tra cứu' }}
        </button>
      </form>

      <p v-if="error" class="sf-order-msg sf-order-msg--err">{{ error }}</p>
      <p v-else-if="notFound" class="sf-order-msg">
        Không tìm thấy đơn khớp mã và SĐT này.
      </p>

      <OrderCard v-if="order" :order="order" />
    </div>
  </div>
</template>
