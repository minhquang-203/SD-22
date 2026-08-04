<script setup>
import { computed, onUnmounted, ref, watch } from 'vue'
import { Icon } from '@iconify/vue'
import { fetchPosVouchers } from '@/api/banHangApi'
import { formatCurrency } from '@/utils/format'

const props = defineProps({
  visible: { type: Boolean, default: false },
  selectedCode: { type: String, default: '' },
  subtotal: { type: Number, default: 0 },
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
  if (event.target === event.currentTarget) closeModal()
}

function formatDiscount(voucher) {
  if (voucher.loai === 'PHAN_TRAM') {
    const suffix = voucher.giamToiDa ? ` (tối đa ${formatCurrency(voucher.giamToiDa)})` : ''
    return `Giảm ${voucher.giaTri}%${suffix}`
  }
  return `Giảm ${formatCurrency(voucher.giaTri)}`
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
  const min = Number(voucher.giaTriDonToiThieu) || 0
  return props.subtotal >= min
}

function eligibilityMessage(voucher) {
  const min = Number(voucher.giaTriDonToiThieu) || 0
  if (props.subtotal < min) {
    return `Đơn tối thiểu ${formatCurrency(min)}`
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
    const res = await fetchPosVouchers(search.value.trim())
    vouchers.value = (res.data?.content || []).filter((v) => v.loai !== 'FREE_SHIP')
  } catch (error) {
    vouchers.value = []
    loadError.value = typeof error === 'string' ? error : 'Không tải được danh sách mã giảm giá'
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
  if (event.key === 'Escape' && props.visible) closeModal()
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
    <div v-if="visible" class="pos-voucher-overlay" @click="onBackdrop">
      <div
        class="pos-voucher-modal"
        role="dialog"
        aria-modal="true"
        aria-labelledby="pos-voucher-title"
        @click.stop
      >
        <header class="pos-voucher-modal__head">
          <div>
            <h2 id="pos-voucher-title">Chọn mã giảm giá</h2>
            <p>Mã đang hiệu lực tại quầy (không gồm freeship)</p>
          </div>
          <button type="button" class="admin-icon-btn" aria-label="Đóng" @click="closeModal">
            <Icon icon="mdi:close" width="20" />
          </button>
        </header>

        <div class="pos-voucher-modal__search">
          <Icon icon="icon-park-outline:search" width="18" />
          <input
            v-model.trim="search"
            type="search"
            placeholder="Tìm theo mã hoặc tên chương trình..."
            autocomplete="off"
          />
        </div>

        <div class="pos-voucher-modal__body">
          <div v-if="loading" class="pos-voucher-modal__state">Đang tải mã giảm giá...</div>
          <div v-else-if="loadError" class="pos-voucher-modal__state pos-voucher-modal__state--error">
            {{ loadError }}
          </div>
          <div v-else-if="!hasResults" class="pos-voucher-modal__state">
            Không tìm thấy mã phù hợp.
          </div>
          <ul v-else class="pos-voucher-list">
            <li v-for="voucher in vouchers" :key="voucher.id">
              <button
                type="button"
                class="pos-voucher-card"
                :class="{
                  'pos-voucher-card--selected': selectedCode === voucher.ma,
                  'pos-voucher-card--disabled': !isEligible(voucher),
                }"
                :disabled="!isEligible(voucher)"
                @click="selectVoucher(voucher)"
              >
                <div class="pos-voucher-card__main">
                  <span class="pos-voucher-card__code">{{ voucher.ma }}</span>
                  <strong>{{ voucher.ten }}</strong>
                  <p class="pos-voucher-card__discount">{{ formatDiscount(voucher) }}</p>
                  <p class="pos-voucher-card__meta">
                    Đơn tối thiểu {{ formatCurrency(voucher.giaTriDonToiThieu || 0) }}
                    · HSD {{ formatExpiry(voucher.ngayKetThuc) }}
                  </p>
                </div>
                <div class="pos-voucher-card__side">
                  <span v-if="selectedCode === voucher.ma">Đang chọn</span>
                  <span v-else-if="!isEligible(voucher)">{{ eligibilityMessage(voucher) }}</span>
                  <span v-else class="pos-voucher-card__pick">Chọn</span>
                </div>
              </button>
            </li>
          </ul>
        </div>

        <footer class="pos-voucher-modal__foot">
          <button
            v-if="selectedCode"
            type="button"
            class="soleil-btn-outline"
            @click="clearSelection"
          >
            Bỏ mã đã chọn
          </button>
          <button type="button" class="soleil-btn-primary" @click="closeModal">Đóng</button>
        </footer>
      </div>
    </div>
  </Teleport>
</template>

<style scoped>
.pos-voucher-overlay {
  position: fixed;
  inset: 0;
  z-index: 1200;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 16px;
  background: rgba(0, 0, 0, 0.45);
}

.pos-voucher-modal {
  width: min(100%, 520px);
  max-height: min(88vh, 680px);
  display: flex;
  flex-direction: column;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 18px 50px rgba(15, 23, 42, 0.25);
  overflow: hidden;
}

.pos-voucher-modal__head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  padding: 16px 18px 12px;
  border-bottom: 1px solid #f1f5f9;
}

.pos-voucher-modal__head h2 {
  margin: 0 0 4px;
  font-size: 16px;
  font-weight: 700;
  color: #1e1510;
}

.pos-voucher-modal__head p {
  margin: 0;
  font-size: 12px;
  color: #64748b;
}

.pos-voucher-modal__search {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 12px 18px 0;
  padding: 0 12px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  background: #fafafa;
}

.pos-voucher-modal__search:focus-within {
  border-color: #c49554;
  box-shadow: 0 0 0 3px rgba(196, 149, 84, 0.15);
  background: #fff;
}

.pos-voucher-modal__search input {
  flex: 1;
  border: none;
  background: transparent;
  padding: 10px 0;
  font: inherit;
  font-size: 14px;
  outline: none;
}

.pos-voucher-modal__body {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  padding: 12px 18px;
}

.pos-voucher-modal__state {
  padding: 28px 12px;
  text-align: center;
  font-size: 13px;
  color: #64748b;
}

.pos-voucher-modal__state--error {
  color: #a83a3a;
}

.pos-voucher-list {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.pos-voucher-card {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 12px 14px;
  border: 1px solid #e5e7eb;
  border-radius: 10px;
  background: #fff;
  text-align: left;
  cursor: pointer;
  font: inherit;
  transition: border-color 0.15s, background 0.15s;
}

.pos-voucher-card:hover:not(:disabled) {
  border-color: #c49554;
  background: rgba(196, 149, 84, 0.06);
}

.pos-voucher-card--selected {
  border-color: #c49554;
  background: rgba(196, 149, 84, 0.1);
}

.pos-voucher-card--disabled,
.pos-voucher-card:disabled {
  opacity: 0.55;
  cursor: not-allowed;
}

.pos-voucher-card__main {
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 0;
}

.pos-voucher-card__code {
  font-size: 12px;
  font-weight: 700;
  color: #8a6428;
  letter-spacing: 0.03em;
}

.pos-voucher-card__main strong {
  font-size: 14px;
  color: #1e1510;
}

.pos-voucher-card__discount {
  margin: 0;
  font-size: 13px;
  font-weight: 600;
  color: #3d7a4a;
}

.pos-voucher-card__meta {
  margin: 0;
  font-size: 12px;
  color: #64748b;
}

.pos-voucher-card__side {
  flex-shrink: 0;
  font-size: 12px;
  color: #64748b;
  text-align: right;
  max-width: 110px;
}

.pos-voucher-card__pick {
  color: #8a6428;
  font-weight: 600;
}

.pos-voucher-modal__foot {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  padding: 12px 18px 16px;
  border-top: 1px solid #f1f5f9;
}
</style>
