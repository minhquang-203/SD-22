<script setup>
import { computed, ref } from 'vue'
import { Icon } from '@iconify/vue'
import PageHeader from '@/components/ui/PageHeader.vue'
import StatCard from '@/components/ui/StatCard.vue'
import CodeBadge from '@/components/ui/CodeBadge.vue'
import StatusDot from '@/components/ui/StatusDot.vue'
import PillBadge from '@/components/ui/PillBadge.vue'

/** Dữ liệu mẫu — sẽ thay bằng API khi module khuyến mãi hoàn thiện */
const coupons = ref([
  {
    id: 1,
    code: 'SUNOVA20',
    name: 'Chào hè 2026',
    note: 'Áp dụng cho SPF 50+',
    type: 'percent',
    typeLabel: 'Phần trăm',
    typeIcon: 'icon-park-outline:percentage',
    amount: '20%',
    amountClass: 'text-[var(--bronze)]',
    period: '01/06 — 30/06/2026',
    periodNote: 'Còn 8 ngày',
    periodNoteClass: 'text-[var(--coral)]',
    used: 342,
    total: 500,
    progress: 68,
    progressTone: 'default',
    status: 'active',
    statusLabel: 'Đang hoạt động',
  },
  {
    id: 2,
    code: 'FREESHIP',
    name: 'Miễn phí vận chuyển',
    note: 'Đơn từ 299.000đ',
    type: 'ship',
    typeLabel: 'Miễn ship',
    typeIcon: 'icon-park-outline:truck',
    amount: '100%',
    amountClass: 'text-[var(--sage)]',
    period: '01/05 — 31/07/2026',
    periodNote: 'Còn 38 ngày',
    periodNoteClass: 'text-[rgba(30,21,16,0.35)]',
    used: 1204,
    total: null,
    progress: 100,
    progressTone: 'sage',
    status: 'active',
    statusLabel: 'Đang hoạt động',
  },
  {
    id: 3,
    code: 'MEMBER50',
    name: 'Ưu đãi thành viên VIP',
    note: 'Chỉ dành cho Gold+',
    type: 'fixed',
    typeLabel: 'Số tiền',
    typeIcon: 'icon-park-outline:finance',
    amount: '50.000đ',
    amountClass: 'text-[var(--sky)]',
    period: '15/06 — 15/09/2026',
    periodNote: 'Bắt đầu sau 7 ngày',
    periodNoteClass: 'text-[var(--sky)]',
    used: 0,
    total: 200,
    progress: 0,
    progressTone: 'default',
    status: 'upcoming',
    statusLabel: 'Sắp diễn ra',
  },
  {
    id: 4,
    code: 'TET2025',
    name: 'Tết An Khang',
    note: 'Toàn bộ sản phẩm',
    type: 'percent',
    typeLabel: 'Phần trăm',
    typeIcon: 'icon-park-outline:percentage',
    amount: '15%',
    amountClass: 'text-[var(--bronze)]',
    period: '20/01 — 10/02/2025',
    periodNote: 'Đã kết thúc',
    periodNoteClass: 'text-[rgba(30,21,16,0.35)]',
    used: 890,
    total: 890,
    progress: 100,
    progressTone: 'default',
    status: 'expired',
    statusLabel: 'Đã hết hạn',
  },
  {
    id: 5,
    code: 'FLASH30',
    name: 'Flash sale cuối tuần',
    note: 'Giới hạn 100 lượt',
    type: 'percent',
    typeLabel: 'Phần trăm',
    typeIcon: 'icon-park-outline:percentage',
    amount: '30%',
    amountClass: 'text-[var(--bronze)]',
    period: '10/06 — 12/06/2026',
    periodNote: 'Tạm dừng',
    periodNoteClass: 'text-[var(--coral)]',
    used: 45,
    total: 100,
    progress: 45,
    progressTone: 'default',
    status: 'paused',
    statusLabel: 'Tạm dừng',
  },
])

const keyword = ref('')
const statusFilter = ref('all')
const typeFilter = ref('all')

const filteredCoupons = computed(() => {
  const q = keyword.value.trim().toLowerCase()
  return coupons.value.filter((item) => {
    const matchKeyword =
      !q ||
      item.code.toLowerCase().includes(q) ||
      item.name.toLowerCase().includes(q) ||
      item.note.toLowerCase().includes(q)
    const matchStatus = statusFilter.value === 'all' || item.status === statusFilter.value
    const matchType = typeFilter.value === 'all' || item.type === typeFilter.value
    return matchKeyword && matchStatus && matchType
  })
})

function usageLabel(item) {
  return item.total ? `${item.used} / ${item.total}` : `${item.used} / —`
}

function onCreate() {
  window.alert('Chức năng tạo phiếu mới sẽ kết nối API sau.')
}

function onView(item) {
  window.alert(`Xem phiếu: ${item.code}`)
}

function onEdit(item) {
  window.alert(`Sửa phiếu: ${item.code}`)
}

function onDelete(item) {
  window.alert(`Xóa phiếu: ${item.code}`)
}
</script>

<template>
  <div class="space-y-5">
    <PageHeader
      title="Khuyến mãi & Voucher"
      description="Quản lý và theo dõi toàn bộ chương trình ưu đãi của cửa hàng"
    >
      <template #actions>
        <button type="button" class="soleil-btn-primary" @click="onCreate">
          <Icon icon="icon-park-outline:plus" />
          Tạo phiếu mới
        </button>
      </template>
    </PageHeader>

    <div class="soleil-stat-grid">
      <StatCard
        label="Đang hoạt động"
        value="24"
        trend="+3 tháng này"
        trend-type="up"
        icon="icon-park-outline:ticket-one"
        icon-tone="gold"
      />
      <StatCard
        label="Lượt sử dụng"
        value="1.842"
        trend="12.4%"
        trend-type="up"
        icon="icon-park-outline:check-one"
        icon-tone="sage"
      />
      <StatCard
        label="Doanh thu tiết kiệm"
        value="47M"
        trend="-2.1%"
        trend-type="down"
        icon="icon-park-outline:dollar"
        icon-tone="coral"
      />
      <StatCard
        label="Sắp hết hạn"
        value="6"
        trend="7 ngày tới"
        trend-type="neutral"
        icon="icon-park-outline:hourglass"
        icon-tone="sky"
      />
    </div>

    <div class="soleil-toolbar">
      <div class="soleil-toolbar__search">
        <Icon icon="icon-park-outline:search" class="soleil-toolbar__search-icon" />
        <input
          v-model="keyword"
          class="soleil-toolbar__input"
          type="text"
          placeholder="Tìm mã phiếu, tên chương trình…"
        />
      </div>
      <select v-model="statusFilter" class="soleil-toolbar__select">
        <option value="all">Tất cả trạng thái</option>
        <option value="active">Đang hoạt động</option>
        <option value="paused">Tạm dừng</option>
        <option value="upcoming">Sắp diễn ra</option>
        <option value="expired">Đã hết hạn</option>
      </select>
      <select v-model="typeFilter" class="soleil-toolbar__select">
        <option value="all">Tất cả loại</option>
        <option value="percent">Phần trăm (%)</option>
        <option value="fixed">Số tiền cố định</option>
        <option value="ship">Miễn phí ship</option>
      </select>
      <button type="button" class="soleil-btn-outline soleil-toolbar__spacer">
        <Icon icon="icon-park-outline:sort" />
        Sắp xếp
      </button>
      <button type="button" class="soleil-btn-outline">
        <Icon icon="icon-park-outline:download" />
        Xuất CSV
      </button>
    </div>

    <div class="soleil-table-card">
      <div class="overflow-x-auto">
        <table class="soleil-table admin-table--soleil">
          <thead>
            <tr>
              <th style="width: 40px">
                <input type="checkbox" class="soleil-checkbox" aria-label="Chọn tất cả" />
              </th>
              <th>Mã phiếu</th>
              <th>Tên chương trình</th>
              <th>Loại giảm giá</th>
              <th>Mức giảm</th>
              <th>Hiệu lực</th>
              <th>Sử dụng</th>
              <th>Trạng thái</th>
              <th style="width: 120px" />
            </tr>
          </thead>
          <tbody>
            <tr v-for="item in filteredCoupons" :key="item.id">
              <td>
                <input type="checkbox" class="soleil-checkbox" :aria-label="`Chọn ${item.code}`" />
              </td>
              <td><CodeBadge :code="item.code" /></td>
              <td>
                <div class="font-normal">{{ item.name }}</div>
                <div class="text-[11px] text-[rgba(30,21,16,0.4)]">{{ item.note }}</div>
              </td>
              <td>
                <PillBadge :variant="item.type" :icon="item.typeIcon" :label="item.typeLabel" />
              </td>
              <td :class="['font-medium', item.amountClass]">{{ item.amount }}</td>
              <td>
                <div class="text-xs">{{ item.period }}</div>
                <div class="text-[11px]" :class="item.periodNoteClass">{{ item.periodNote }}</div>
              </td>
              <td>
                <div class="text-xs mb-1">{{ usageLabel(item) }}</div>
                <div class="soleil-progress-mini">
                  <div
                    class="soleil-progress-mini__bar"
                    :class="{ 'soleil-progress-mini__bar--sage': item.progressTone === 'sage' }"
                    :style="{ width: `${item.progress}%` }"
                  />
                </div>
              </td>
              <td>
                <StatusDot :status="item.status" :label="item.statusLabel" />
              </td>
              <td>
                <div class="soleil-actions-cell">
                  <button
                    type="button"
                    class="soleil-act-btn"
                    title="Xem"
                    @click="onView(item)"
                  >
                    <Icon icon="icon-park-outline:preview-open" />
                  </button>
                  <button
                    type="button"
                    class="soleil-act-btn"
                    title="Sửa"
                    @click="onEdit(item)"
                  >
                    <Icon icon="icon-park-outline:edit" />
                  </button>
                  <button
                    type="button"
                    class="soleil-act-btn soleil-act-btn--danger"
                    title="Xóa"
                    @click="onDelete(item)"
                  >
                    <Icon icon="icon-park-outline:delete" />
                  </button>
                </div>
              </td>
            </tr>
            <tr v-if="!filteredCoupons.length">
              <td colspan="9" class="text-center py-10 text-[var(--admin-muted)]">
                Không tìm thấy phiếu giảm giá phù hợp.
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <div class="soleil-pagination">
        <span class="soleil-pagination__info">
          Hiển thị {{ filteredCoupons.length }} / {{ coupons.length }} phiếu
        </span>
        <div class="soleil-pagination__btns">
          <button type="button" class="soleil-page-btn soleil-page-btn--active">1</button>
          <button type="button" class="soleil-page-btn">2</button>
          <button type="button" class="soleil-page-btn">
            <Icon icon="icon-park-outline:right" />
          </button>
        </div>
      </div>
    </div>
  </div>
</template>
