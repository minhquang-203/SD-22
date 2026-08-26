<script setup>
import { computed } from 'vue'
import { Icon } from '@iconify/vue'
import { formatVND } from '@/utils/formatVND'
import { toast } from '@/composables/useToast'

const props = defineProps({
  voucher: { type: Object, required: true },
})

const giaTriHienThi = computed(() => {
  const v = props.voucher
  if (v.loai === 'PHAN_TRAM') return `${Number(v.giaTri)}%`
  if (v.loai === 'FREE_SHIP') return 'Freeship'
  return formatVND(v.giaTri)
})

const giaTriSub = computed(() => {
  const v = props.voucher
  if (v.loai === 'PHAN_TRAM') return 'GIẢM'
  if (v.loai === 'FREE_SHIP') return 'SHIP'
  return 'GIẢM'
})

const dieuKien = computed(() => {
  const min = Number(props.voucher.giaTriDonToiThieu)
  if (!min || min <= 0) return 'Không giới hạn đơn tối thiểu'
  return `Đơn tối thiểu ${formatVND(min)}`
})

const giamToiDa = computed(() => {
  const max = Number(props.voucher.giamToiDa)
  if (!max || max <= 0) return ''
  return `Giảm tối đa ${formatVND(max)}`
})

const hanSuDung = computed(() => {
  if (!props.voucher.ngayKetThuc) return ''
  const d = new Date(props.voucher.ngayKetThuc)
  return `HSD ${d.toLocaleDateString('vi-VN', { day: '2-digit', month: '2-digit', year: 'numeric' })}`
})

const daHetHan = computed(() => {
  if (!props.voucher.ngayKetThuc) return false
  return new Date(props.voucher.ngayKetThuc) < new Date()
})

async function copyMa() {
  try {
    await navigator.clipboard.writeText(props.voucher.ma)
    toast(`Đã sao chép mã ${props.voucher.ma}`, 'info')
  } catch {
    toast('Không sao chép được mã', 'warn')
  }
}
</script>

<template>
  <article class="sf-voucher-card" :class="{ 'sf-voucher-card--expired': daHetHan }">
    <div class="sf-voucher-card__value">
      <span class="sf-voucher-card__value-label">{{ giaTriSub }}</span>
      <span class="sf-voucher-card__value-num">{{ giaTriHienThi }}</span>
    </div>

    <div class="sf-voucher-card__body">
      <div class="sf-voucher-card__top">
        <h3 class="sf-voucher-card__name">{{ voucher.ten || voucher.ma }}</h3>
        <span v-if="voucher.phamVi === 'CA_NHAN'" class="sf-voucher-card__tag">Riêng bạn</span>
      </div>
      <p class="sf-voucher-card__cond">{{ dieuKien }}</p>
      <p v-if="giamToiDa" class="sf-voucher-card__cond">{{ giamToiDa }}</p>
      <div class="sf-voucher-card__foot">
        <span class="sf-voucher-card__exp">{{ daHetHan ? 'Đã hết hạn' : hanSuDung }}</span>
        <button
          type="button"
          class="sf-voucher-card__copy"
          :disabled="daHetHan"
          @click="copyMa"
        >
          {{ voucher.ma }}
          <Icon icon="solar:copy-linear" width="15" />
        </button>
      </div>
    </div>
  </article>
</template>

<style scoped>
.sf-voucher-card {
  display: flex;
  align-items: stretch;
  min-height: 108px;
  background: #fff;
  border: 1px solid #d9d0c4;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 1px 2px rgba(30, 21, 16, 0.04);
}

.sf-voucher-card--expired {
  opacity: 0.48;
  filter: grayscale(0.35);
}

.sf-voucher-card__value {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 108px;
  flex-shrink: 0;
  padding: 14px 10px;
  background: #1e1510;
  color: #c9a96e;
  text-align: center;
}

.sf-voucher-card__value-label {
  font-size: 0.7rem;
  font-weight: 700;
  letter-spacing: 0.08em;
  color: rgba(201, 169, 110, 0.75);
}

.sf-voucher-card__value-num {
  margin-top: 4px;
  font-size: 1.45rem;
  font-weight: 800;
  line-height: 1.1;
  color: #c9a96e;
  word-break: break-word;
}

.sf-voucher-card__body {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 3px;
  padding: 14px 16px;
}

.sf-voucher-card__top {
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 0;
}

.sf-voucher-card__name {
  margin: 0;
  font-size: 1rem;
  font-weight: 700;
  color: #1e1510;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.sf-voucher-card__tag {
  flex-shrink: 0;
  font-size: 0.68rem;
  font-weight: 700;
  color: #1e1510;
  background: #f3ebe0;
  border-radius: 4px;
  padding: 2px 7px;
}

.sf-voucher-card__cond {
  margin: 0;
  font-size: 0.82rem;
  color: #4a4038;
  line-height: 1.4;
}

.sf-voucher-card__foot {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  margin-top: 8px;
}

.sf-voucher-card__exp {
  font-size: 0.78rem;
  font-weight: 500;
  color: #8a7355;
}

.sf-voucher-card__copy {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  margin: 0;
  padding: 5px 10px;
  border: 1px solid #1e1510;
  border-radius: 6px;
  background: #1e1510;
  font-family: ui-monospace, SFMono-Regular, Menlo, Consolas, monospace;
  font-size: 0.78rem;
  font-weight: 700;
  color: #c9a96e;
  cursor: pointer;
}

.sf-voucher-card__copy:hover:not(:disabled) {
  background: #2c211a;
}

.sf-voucher-card__copy:disabled {
  opacity: 0.45;
  cursor: not-allowed;
}
</style>
