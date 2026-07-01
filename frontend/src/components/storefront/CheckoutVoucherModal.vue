<script setup>
import { computed, onUnmounted, ref, watch } from 'vue'
import { Icon } from '@iconify/vue'
import { fetchCheckoutVouchers } from '@/api/onlineCheckout'
import { formatVND } from '@/utils/formatVND'

const props = defineProps({
  visible: { type: Boolean, default: false },
  selectedCode: { type: String, default: '' },
  subtotal: { type: Number, default: 0 },
  hasSaleItems: { type: Boolean, default: false },
})

const emit = defineEmits(['update:visible', 'select'])

const search = ref('')
const loading = ref(false)
const vouchers = ref([])
const loadError = ref('')

let searchTimer = null

const hasResults = computed(() => vouchers.value.length > 0)

function closeModal() {
  emit('update:visible', false)
}

function onBackdrop(event) {
  if (event.target === event.currentTarget) {
    closeModal()
  }
}

function formatDiscount(voucher) {
  if (voucher.loai === 'PHAN_TRAM') {
    const suffix = voucher.giamToiDa ? ` (tối đa ${formatVND(voucher.giamToiDa)})` : ''
    return `Giảm ${voucher.giaTri}%${suffix}`
  }
  return `Giảm ${formatVND(voucher.giaTri)}`
}

function formatExpiry(value) {
  if (!value) return '—'
  return new Date(value).toLocaleDateString('vi-VN', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
  })
}

function isEligible(voucher) {
  if (props.hasSaleItems) return false
  const min = Number(voucher.giaTriDonToiThieu) || 0
  return props.subtotal >= min
}

function eligibilityMessage(voucher) {
  if (props.hasSaleItems) {
    return 'Không áp dụng khi có sản phẩm đang giảm giá'
  }
  const min = Number(voucher.giaTriDonToiThieu) || 0
  if (props.subtotal < min) {
    return `Đơn tối thiểu ${formatVND(min)}`
  }
  return ''
}

function selectVoucher(voucher) {
  if (!isEligible(voucher)) return
  emit('select', voucher.ma)
  closeModal()
}

function clearSelection() {
  emit('select', '')
  closeModal()
}

async function loadVouchers() {
  loading.value = true
  loadError.value = ''
  try {
    const res = await fetchCheckoutVouchers(search.value.trim())
    vouchers.value = res.data?.content || []
  } catch (error) {
    vouchers.value = []
    loadError.value = typeof error === 'string' ? error : 'Không tải được danh sách voucher'
  } finally {
    loading.value = false
  }
}

function scheduleSearch() {
  if (searchTimer) clearTimeout(searchTimer)
  searchTimer = setTimeout(() => {
    void loadVouchers()
  }, 300)
}

function onKeydown(event) {
  if (event.key === 'Escape' && props.visible) {
    closeModal()
  }
}

watch(
  () => props.visible,
  (open) => {
    if (open) {
      search.value = ''
      document.body.style.overflow = 'hidden'
      document.addEventListener('keydown', onKeydown)
      void loadVouchers()
    } else {
      document.body.style.overflow = ''
      document.removeEventListener('keydown', onKeydown)
    }
  },
)

watch(search, () => {
  if (props.visible) scheduleSearch()
})

onUnmounted(() => {
  if (searchTimer) clearTimeout(searchTimer)
  document.body.style.overflow = ''
  document.removeEventListener('keydown', onKeydown)
})
</script>

<template>
  <Teleport to="body">
    <div v-if="visible" class="sf-voucher-modal-backdrop" @click="onBackdrop">
      <div
        class="sf-voucher-modal"
        role="dialog"
        aria-modal="true"
        aria-labelledby="sf-voucher-modal-title"
        @click.stop
      >
        <header class="sf-voucher-modal__header">
          <div>
            <p class="sf-voucher-modal__eyebrow">Ưu đãi</p>
            <h2 id="sf-voucher-modal-title">Chọn mã giảm giá</h2>
          </div>
          <button type="button" class="sf-voucher-modal__close" aria-label="Đóng" @click="closeModal">
            <Icon icon="mdi:close" width="22" />
          </button>
        </header>

        <div class="sf-voucher-modal__search">
          <Icon icon="solar:magnifer-linear" width="18" />
          <input
            v-model.trim="search"
            type="search"
            placeholder="Tìm theo mã hoặc tên chương trình..."
            autocomplete="off"
          />
        </div>

        <div v-if="hasSaleItems" class="sf-voucher-modal__alert">
          <Icon icon="solar:info-circle-linear" width="18" />
          <p>Đơn có sản phẩm đang trong đợt giảm giá nên không thể dùng mã voucher.</p>
        </div>

        <div class="sf-voucher-modal__body">
          <div v-if="loading" class="sf-voucher-modal__state">
            <Icon icon="svg-spinners:ring-resize" width="28" />
            <span>Đang tải voucher...</span>
          </div>

          <div v-else-if="loadError" class="sf-voucher-modal__state sf-voucher-modal__state--error">
            <Icon icon="solar:danger-circle-linear" width="28" />
            <span>{{ loadError }}</span>
          </div>

          <div v-else-if="!hasResults" class="sf-voucher-modal__state">
            <Icon icon="solar:ticket-linear" width="28" />
            <span>Không tìm thấy voucher phù hợp.</span>
          </div>

          <ul v-else class="sf-voucher-modal__list">
            <li v-for="voucher in vouchers" :key="voucher.id">
              <button
                type="button"
                class="sf-voucher-card"
                :class="{
                  selected: selectedCode === voucher.ma,
                  disabled: !isEligible(voucher),
                }"
                :disabled="!isEligible(voucher)"
                @click="selectVoucher(voucher)"
              >
                <div class="sf-voucher-card__main">
                  <span class="sf-voucher-card__code">{{ voucher.ma }}</span>
                  <strong class="sf-voucher-card__title">{{ voucher.ten }}</strong>
                  <p class="sf-voucher-card__discount">{{ formatDiscount(voucher) }}</p>
                  <p class="sf-voucher-card__meta">
                    Đơn tối thiểu {{ formatVND(voucher.giaTriDonToiThieu || 0) }}
                    · HSD {{ formatExpiry(voucher.ngayKetThuc) }}
                  </p>
                </div>
                <div class="sf-voucher-card__side">
                  <span v-if="selectedCode === voucher.ma" class="sf-voucher-card__picked">
                    <Icon icon="solar:check-circle-bold" width="20" />
                    Đang chọn
                  </span>
                  <span v-else-if="!isEligible(voucher)" class="sf-voucher-card__note">
                    {{ eligibilityMessage(voucher) }}
                  </span>
                  <span v-else class="sf-voucher-card__action">Chọn</span>
                </div>
              </button>
            </li>
          </ul>
        </div>

        <footer class="sf-voucher-modal__footer">
          <button
            v-if="selectedCode"
            type="button"
            class="sf-voucher-modal__clear"
            @click="clearSelection"
          >
            Bỏ mã đã chọn
          </button>
          <button type="button" class="sf-voucher-modal__done" @click="closeModal">
            Đóng
          </button>
        </footer>
      </div>
    </div>
  </Teleport>
</template>

<style>
@import '@/styles/checkoutVoucherModal.css';
</style>
