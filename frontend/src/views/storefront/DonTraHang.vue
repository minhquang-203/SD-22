<script setup>
import { computed, onMounted, onUnmounted, ref, watch } from 'vue'
import { RouterLink, useRoute } from 'vue-router'
import AccountSidebar from '@/components/storefront/AccountSidebar.vue'
import ReturnDetailCard from '@/components/storefront/ReturnDetailCard.vue'
import ReturnPickShiftModal from '@/components/storefront/ReturnPickShiftModal.vue'
import { getCustomerId } from '@/composables/useAuth'
import { toast } from '@/composables/useToast'
import { subscribeCustomerOrders } from '@/composables/useRealtime'
import { fetchChiTietTraHangCuaToi, taoVanDonTra } from '@/api/traHangApi'

const route = useRoute()
const loading = ref(true)
const error = ref('')
const detail = ref(null)
const showPickShiftModal = ref(false)
const creatingLabel = ref(false)

let unsubscribeRealtime = null

const returnId = computed(() => Number(route.params.id))

const pickShiftOrder = computed(() => {
  if (!detail.value) return null
  return {
    ...detail.value,
    diaChiGiao: detail.value.diaChiTra || detail.value.diaChiGiao,
  }
})

async function loadDetail() {
  const id = returnId.value
  const idKhachHang = getCustomerId()
  if (!id || Number.isNaN(id)) {
    error.value = 'Không xác định được đơn trả hàng.'
    loading.value = false
    return
  }
  if (!idKhachHang) {
    error.value = 'Vui lòng đăng nhập để xem đơn trả hàng.'
    loading.value = false
    return
  }

  loading.value = true
  error.value = ''
  try {
    const res = await fetchChiTietTraHangCuaToi(id, idKhachHang)
    detail.value = res.data || null
    if (!detail.value) {
      error.value = 'Không tìm thấy yêu cầu trả hàng.'
    }
  } catch (err) {
    detail.value = null
    error.value = typeof err === 'string' ? err : 'Không tải được chi tiết đơn trả hàng.'
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadDetail()
  unsubscribeRealtime = subscribeCustomerOrders(async (event) => {
    if (!event?.idHoaDon || !detail.value) return
    if (Number(event.idHoaDon) === Number(detail.value.idHoaDon)) {
      await loadDetail()
    }
  })
})

onUnmounted(() => {
  unsubscribeRealtime?.()
  unsubscribeRealtime = null
})

watch(returnId, () => {
  loadDetail()
})

function openPickShift() {
  if (!detail.value?.id || creatingLabel.value) return
  if (!getCustomerId()) {
    toast('Vui lòng đăng nhập để tạo vận đơn hoàn hàng.', 'warn')
    return
  }
  showPickShiftModal.value = true
}

function closePickShift() {
  showPickShiftModal.value = false
}

async function handleCreateReturnLabel(pickShiftId) {
  const current = detail.value
  if (!current?.id || creatingLabel.value) return

  const idKhachHang = getCustomerId()
  if (!idKhachHang) {
    toast('Vui lòng đăng nhập để tạo vận đơn hoàn hàng.', 'warn')
    return
  }

  creatingLabel.value = true
  try {
    const res = await taoVanDonTra(current.id, idKhachHang, pickShiftId)
    const updated = res.data
    toast(
      updated?.maVanDonTra ? `Đã tạo vận đơn hoàn: ${updated.maVanDonTra}` : 'Đã tạo vận đơn hoàn hàng.',
      'info',
    )
    closePickShift()
    await loadDetail()
  } catch (err) {
    toast(typeof err === 'string' ? err : 'Không tạo được vận đơn hoàn hàng.', 'warn')
  } finally {
    creatingLabel.value = false
  }
}
</script>

<template>
  <div class="sf-account-page">
    <div class="sf-container">
      <nav class="sf-breadcrumb">
        <RouterLink to="/">Trang chủ</RouterLink>
        <span>/</span>
        <RouterLink to="/tai-khoan">Tài khoản</RouterLink>
        <span>/</span>
        <RouterLink to="/tra-cuu-don">Tra cứu đơn</RouterLink>
        <span>/</span>
        <span>Đơn trả hàng</span>
      </nav>

      <h1 class="sf-account-page__title">Trung tâm tài khoản</h1>

      <div class="sf-account-layout">
        <AccountSidebar />

        <div class="sf-account-main sf-account-main--orders">
          <h2 class="sf-account-main__heading">Đơn trả hàng</h2>
          <p class="sf-account-main__sub">Theo dõi tiến trình hoàn hàng và hoàn tiền của yêu cầu trả hàng.</p>

          <div v-if="loading" class="sf-order-skeleton" />
          <p v-else-if="error" class="sf-order-msg sf-order-msg--err">{{ error }}</p>
          <ReturnDetailCard
            v-else-if="detail"
            :detail="detail"
            :creating-label="creatingLabel"
            @create-return-label="openPickShift"
          />
        </div>
      </div>
    </div>

    <ReturnPickShiftModal
      :visible="showPickShiftModal"
      :order="pickShiftOrder"
      :submitting="creatingLabel"
      @close="closePickShift"
      @confirm="handleCreateReturnLabel"
    />
  </div>
</template>
