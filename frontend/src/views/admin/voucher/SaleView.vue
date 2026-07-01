<template>
  <div class="sale-page">

    <!-- Page Header -->
    <div class="page-header">
      <div>
        <h1 class="page-title">Đợt giảm giá</h1>
        <p class="page-sub">Quản lý tất cả chiến dịch khuyến mãi và mã giảm giá</p>
      </div>
      <button class="btn btn-primary" @click="openModal">
        <i class="ti ti-plus"></i> Tạo đợt giảm giá
      </button>
    </div>

    <!-- Metrics -->
    <div class="metrics-grid">
      <div class="metric-card">
        <div class="metric-icon" style="background:#EBF0FF; color:#2D5BE3;">
          <i class="ti ti-layout-grid"></i>
        </div>
        <div class="metric-label">Tổng chiến dịch</div>
        <div class="metric-value">{{ metrics.total }}</div>
        <div class="metric-trend trend-neutral"><i class="ti ti-database"></i> Tổng tất cả chiến dịch</div>
      </div>
      <div class="metric-card">
        <div class="metric-icon" style="background:#DCFCE7; color:#15803D;">
          <i class="ti ti-player-play"></i>
        </div>
        <div class="metric-label">Đang chạy</div>
        <div class="metric-value">{{ metrics.active }}</div>
        <div class="metric-trend trend-neutral"><i class="ti ti-clock"></i> Đang áp dụng</div>
      </div>
      <div class="metric-card">
        <div class="metric-icon" style="background:#FEF3C7; color:#B45309;">
          <i class="ti ti-coin"></i>
        </div>
        <div class="metric-label">Sắp diễn ra</div>
        <div class="metric-value">{{ metrics.upcoming }}</div>
        <div class="metric-trend trend-neutral"><i class="ti ti-calendar"></i> Theo ngày bắt đầu</div>
      </div>
      <div class="metric-card">
        <div class="metric-icon" style="background:#F3E8FF; color:#7C3AED;">
          <i class="ti ti-ticket"></i>
        </div>
        <div class="metric-label">Đã kết thúc</div>
        <div class="metric-value">{{ metrics.expired }}</div>
        <div class="metric-trend trend-neutral"><i class="ti ti-percentage"></i> Theo ngày kết thúc</div>
      </div>
    </div>

    <!-- Tabs -->
    <div class="tabs">
      <button
        v-for="tab in tabs"
        :key="tab.value"
        class="tab-btn"
        :class="{ active: currentTab === tab.value }"
        @click="currentTab = tab.value"
      >{{ tab.label }}</button>
    </div>

    <!-- Filters -->
    <div class="filters-bar">
      <div class="search-wrap">
        <i class="ti ti-search"></i>
        <input type="text" v-model="searchQuery" placeholder="Tìm mã hoặc tên đợt giảm giá..." />
      </div>
      <div class="filters-right">
        <button class="btn btn-secondary" @click="loadData(currentPage)"><i class="ti ti-refresh"></i> Làm mới</button>
      </div>
    </div>

    <!-- Table -->
    <div class="table-card">
      <table>
        <thead>
          <tr>
            <th style="width:30%">Đợt giảm giá</th>
            <th style="width:12%">Loại</th>
            <th style="width:12%">Giá trị</th>
            <th style="width:20%">Thời gian</th>
            <th style="width:12%">Trạng thái</th>
            <th style="width:14%">Hành động</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="initialLoading">
            <td colspan="6">
              <div class="empty-state">
                <i class="ti ti-loader-2"></i>
                <p>Đang tải dữ liệu...</p>
              </div>
            </td>
          </tr>
          <template v-else-if="campaigns.length">
            <tr v-for="c in campaigns" :key="c.id" class="clickable-row" @click="goToDetail(c.id)">
              <td>
                <div class="campaign-name">{{ c.name }}</div>
                <span class="campaign-code">{{ c.code }}</span>
              </td>
              <td><span class="type-badge">{{ c.typeLabel }}</span></td>
              <td><span class="value-cell">{{ c.value }}</span></td>
              <td>
                <div class="date-range">
                  <div class="date-from">{{ c.start }}</div>
                  <div class="date-to">→ {{ c.end }}</div>
                </div>
              </td>
              <td>
                <span class="badge" :class="statusMeta[c.status]?.cls">
                  <i :class="'ti ' + statusMeta[c.status]?.icon" style="font-size:12px;"></i>
                  {{ c.statusLabel }}
                </span>
              </td>
              <td>
                <div class="action-btns">
                  <button class="icon-btn" title="Chỉnh sửa" @click.stop="openEdit(c)">
                    <i class="ti ti-edit"></i>
                  </button>
                  <button
                    v-if="dangHoatDong(c)"
                    class="icon-btn warn"
                    title="Dừng chương trình"
                    :disabled="processingId === c.id"
                    @click.stop="stopRow(c.id)"
                  >
                    <i class="ti ti-player-pause"></i>
                  </button>
                  <button
                    v-else
                    class="icon-btn success"
                    title="Kích hoạt lại"
                    :disabled="processingId === c.id"
                    @click.stop="activateRow(c.id)"
                  >
                    <i class="ti ti-player-play"></i>
                  </button>
                  <button
                    class="icon-btn danger"
                    title="Xóa"
                    :disabled="processingId === c.id"
                    @click.stop="deleteRow(c.id)"
                  >
                    <i class="ti ti-trash"></i>
                  </button>
                </div>
              </td>
            </tr>
          </template>
          <tr v-else>
            <td colspan="6">
              <div class="empty-state">
                <i class="ti ti-mood-empty"></i>
                <p>{{ errorMessage || 'Không tìm thấy đợt giảm giá nào phù hợp' }}</p>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
      <div class="pagination-wrap" v-if="pageInfo.totalPages > 0">
        <div class="page-info">
          Hiển thị {{ pageInfo.numberOfElements }} / {{ pageInfo.totalElements }} kết quả
        </div>
        <div class="page-btns">
          <button class="page-btn" :disabled="pageInfo.first" @click="changePage(1)">
            <i class="ti ti-chevrons-left"></i>
          </button>
          <button
            v-for="page in visiblePages"
            :key="page"
            class="page-btn"
            :class="{ active: page === currentPage }"
            @click="changePage(page)"
          >
            {{ page }}
          </button>
          <button class="page-btn" :disabled="pageInfo.last" @click="changePage(pageInfo.totalPages)">
            <i class="ti ti-chevrons-right"></i>
          </button>
        </div>
      </div>
    </div>

    <!-- Create Modal -->
    <div v-if="modalOpen" class="sale-modal-backdrop" @click.self="closeModal">
      <div class="sale-modal" role="dialog" aria-modal="true">
        <div class="modal-header">
          <h2 class="modal-title">{{ editingId ? 'Cập nhật đợt giảm giá' : 'Tạo đợt giảm giá mới' }}</h2>
          <button class="icon-btn" @click="closeModal"><i class="ti ti-x"></i></button>
        </div>
        <div class="modal-body">
          <div class="form-grid">
            <div class="form-group">
              <label>Mã đợt giảm giá *</label>
              <input type="text" v-model="form.code" placeholder="VD: SUMMER26" />
            </div>
            <div class="form-group">
              <label>Phần trăm giảm (%) *</label>
              <input type="number" min="1" max="100" v-model.number="form.value" placeholder="VD: 20" />
            </div>
            <div class="form-group full">
              <label>Tên chiến dịch *</label>
              <input type="text" v-model="form.name" placeholder="VD: Flash Sale cuối tuần" />
            </div>
            <div class="form-group">
              <label>Ngày bắt đầu *</label>
              <input type="date" v-model="form.start" />
            </div>
            <div class="form-group">
              <label>Ngày kết thúc *</label>
              <input type="date" v-model="form.end" />
            </div>
            <div class="form-group full">
              <label>Ghi chú nội bộ</label>
              <textarea placeholder=""></textarea>
            </div>
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn btn-secondary" @click="closeModal">Hủy</button>
          <button class="btn btn-primary" :disabled="saving" @click="handleSubmit">
            <i class="ti ti-check"></i> {{ editingId ? 'Cập nhật' : 'Tạo chiến dịch' }}
          </button>
        </div>
      </div>
    </div>

  </div>
</template>

<script setup>

import '@/styles/saleCss.css'

import { computed, onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { createSale, deleteSale, searchSale, stopSale, activateSale, updateSale } from '@/api/saleApi.js'
import { confirm } from '@/composables/useConfirm'
import { toast } from '@/composables/useToast'

const router = useRouter()

const PAGE_SIZE = 10

const campaigns = ref([])
const initialLoading = ref(true)
const saving = ref(false)
const errorMessage = ref('')
const currentPage = ref(1)
const processingId = ref(null)

const pageInfo = ref({
  totalElements: 0,
  totalPages: 0,
  number: 0,
  numberOfElements: 0,
  first: true,
  last: true,
})

const metrics = ref({
  total: 0,
  active: 0,
  upcoming: 0,
  expired: 0,
})

const tabs = [
  { label: 'Tất cả', value: '' },
  { label: 'Đang chạy', value: 'active' },
  { label: 'Sắp diễn ra', value: 'scheduled' },
  { label: 'Đã kết thúc', value: 'ended' },
  { label: 'Ngừng áp dụng', value: 'paused' },
]

const currentTab = ref('')
const searchQuery = ref('')

const statusMeta = {
  active: { cls: 'badge-active', icon: 'ti-circle-check', label: 'Đang chạy' },
  scheduled: { cls: 'badge-scheduled', icon: 'ti-clock', label: 'Sắp diễn ra' },
  ended: { cls: 'badge-ended', icon: 'ti-circle-x', label: 'Đã kết thúc' },
  paused: { cls: 'badge-paused', icon: 'ti-player-pause', label: 'Ngừng áp dụng' },
}

const tabToApiStatus = {
  active: 'ACTIVE',
  scheduled: 'UPCOMING',
  ended: 'EXPIRED',
  paused: 'INACTIVE',
}

const apiToUiStatus = {
  ACTIVE: 'active',
  UPCOMING: 'scheduled',
  EXPIRED: 'ended',
  INACTIVE: 'paused',
}

const visiblePages = computed(() => {
  const total = pageInfo.value.totalPages
  const current = currentPage.value
  const start = Math.max(1, Math.min(current - 2, total - 4))
  const end = Math.min(total, Math.max(current + 2, 5))

  return Array.from({ length: Math.max(end - start + 1, 0) }, (_, i) => start + i)
})

function mapSale(item) {
  const uiStatus = apiToUiStatus[item.timeStatus] || 'ended'
  return {
    id: item.id,
    name: item.ten,
    code: item.ma,
    type: 'percent',
    typeLabel: '% giảm giá',
    value: `${item.phanTramGiam ?? 0}%`,
    rawValue: item.phanTramGiam,
    start: formatDisplayDate(item.ngayBatDau),
    end: formatDisplayDate(item.ngayKetThuc),
    rawStart: item.ngayBatDau,
    rawEnd: item.ngayKetThuc,
    status: uiStatus,
    statusLabel: item.timeStatusLabel || statusMeta[uiStatus]?.label || 'Không xác định',
    isActive: item.isActive,
    timeStatus: item.timeStatus,
  }
}

function resolveCampaignStatus(campaign, isActive) {
  if (!isActive) {
    return {
      ...campaign,
      isActive: false,
      timeStatus: 'INACTIVE',
      status: 'paused',
      statusLabel: 'Ngừng áp dụng',
    }
  }

  const now = new Date()
  const batDau = new Date(campaign.rawStart || '')
  const ketThuc = new Date(campaign.rawEnd || '')

  if (now < batDau) {
    return {
      ...campaign,
      isActive: true,
      timeStatus: 'UPCOMING',
      status: 'scheduled',
      statusLabel: 'Sắp diễn ra',
    }
  }
  if (now > ketThuc) {
    return {
      ...campaign,
      isActive: true,
      timeStatus: 'EXPIRED',
      status: 'ended',
      statusLabel: 'Đã kết thúc',
    }
  }
  return {
    ...campaign,
    isActive: true,
    timeStatus: 'ACTIVE',
    status: 'active',
    statusLabel: 'Đang chạy',
  }
}

function capNhatCampaignLocal(id, isActive) {
  campaigns.value = campaigns.value.map((c) =>
    c.id === id ? resolveCampaignStatus(c, isActive) : c,
  )
}

function dangHoatDong(campaign) {
  return campaign.isActive !== false
}

async function loadData(page = 1, { silent = false } = {}) {
  if (!silent && campaigns.value.length === 0) {
    initialLoading.value = true
  }
  errorMessage.value = ''

  try {
    const response = await searchSale(
      searchQuery.value || null,
      tabToApiStatus[currentTab.value] || null,
      page,
      PAGE_SIZE,
    )

    const data = response.data || {}
    campaigns.value = (data.content || []).map(mapSale)
    pageInfo.value = {
      totalElements: data.totalElements || 0,
      totalPages: data.totalPages || 0,
      number: data.number || 0,
      numberOfElements: data.numberOfElements ?? data.content?.length ?? 0,
      first: data.first ?? true,
      last: data.last ?? true,
    }
    currentPage.value = page
  } catch (error) {
    if (campaigns.value.length === 0) {
      campaigns.value = []
      errorMessage.value = normalizeError(error) || 'Không thể tải dữ liệu'
    } else {
      toast(normalizeError(error) || 'Không thể tải dữ liệu', 'warn')
    }
  } finally {
    initialLoading.value = false
  }
}

async function loadMetrics() {
  try {
    const [all, active, upcoming, expired] = await Promise.all([
      searchSale(null, null, 1, 1),
      searchSale(null, 'ACTIVE', 1, 1),
      searchSale(null, 'UPCOMING', 1, 1),
      searchSale(null, 'EXPIRED', 1, 1),
    ])

    metrics.value = {
      total: all.data?.totalElements || 0,
      active: active.data?.totalElements || 0,
      upcoming: upcoming.data?.totalElements || 0,
      expired: expired.data?.totalElements || 0,
    }
  } catch (error) {
    console.error(error)
  }
}

function changePage(page) {
  if (page < 1 || page > pageInfo.value.totalPages || page === currentPage.value) return
  loadData(page)
}

function goToDetail(id) {
  router.push(`/admin/sale/${id}`)
}

async function stopRow(id) {
  const campaign = campaigns.value.find((item) => item.id === id)
  if (!campaign) return
  const ok = await confirm({
    title: 'Dừng chương trình',
    message: `Dừng đợt giảm giá "${campaign.name}"? Đợt giảm giá sẽ không còn hiệu lực.`,
    confirmText: 'Dừng',
    danger: true,
  })
  if (!ok) return

  processingId.value = id
  try {
    await stopSale(id)
    capNhatCampaignLocal(id, false)
    loadMetrics()
    toast(`Đã dừng: ${campaign.name}`, 'info')
  } catch (error) {
    toast(normalizeError(error) || 'Dừng chương trình thất bại', 'warn')
  } finally {
    processingId.value = null
  }
}

async function activateRow(id) {
  const campaign = campaigns.value.find((item) => item.id === id)
  if (!campaign) return
  const ok = await confirm({
    title: 'Kích hoạt lại',
    message: `Kích hoạt lại đợt giảm giá "${campaign.name}"?`,
    confirmText: 'Kích hoạt',
  })
  if (!ok) return

  processingId.value = id
  try {
    await activateSale(id)
    capNhatCampaignLocal(id, true)
    loadMetrics()
    toast(`Đã kích hoạt: ${campaign.name}`, 'info')
  } catch (error) {
    toast(normalizeError(error) || 'Kích hoạt thất bại', 'warn')
  } finally {
    processingId.value = null
  }
}

async function deleteRow(id) {
  const campaign = campaigns.value.find((item) => item.id === id)
  if (!campaign) return
  const ok = await confirm({
    title: 'Xóa đợt giảm giá',
    message: `Xóa đợt giảm giá "${campaign.name}"? Hành động này không thể hoàn tác.`,
    confirmText: 'Xóa',
    danger: true,
  })
  if (!ok) return

  processingId.value = id
  try {
    await deleteSale(id)
    campaigns.value = campaigns.value.filter((item) => item.id !== id)
    if (pageInfo.value.totalElements > 0) {
      pageInfo.value.totalElements -= 1
      pageInfo.value.numberOfElements = campaigns.value.length
    }
    loadMetrics()
    toast(`Đã xóa: ${campaign.name}`, 'info')
  } catch (error) {
    toast(normalizeError(error) || 'Xóa thất bại', 'warn')
  } finally {
    processingId.value = null
  }
}

const modalOpen = ref(false)
const editingId = ref(null)

const emptyForm = () => ({
  code: '',
  name: '',
  value: '',
  start: '',
  end: '',
})

const form = ref(emptyForm())

function openModal() {
  editingId.value = null
  form.value = emptyForm()
  modalOpen.value = true
}

function openEdit(campaign) {
  editingId.value = campaign.id
  form.value = {
    code: campaign.code,
    name: campaign.name,
    value: Number(campaign.rawValue),
    start: toDateInput(campaign.rawStart),
    end: toDateInput(campaign.rawEnd),
  }
  modalOpen.value = true
}

function closeModal() {
  modalOpen.value = false
}

async function handleSubmit() {
  const payload = buildPayload()
  if (!payload) return

  saving.value = true
  try {
    if (editingId.value) {
      await updateSale(editingId.value, payload)
      toast('Cập nhật đợt giảm giá thành công', 'info')
    } else {
      await createSale(payload)
      toast('Tạo đợt giảm giá thành công', 'info')
    }

    closeModal()
    form.value = emptyForm()
    await Promise.all([loadData(editingId.value ? currentPage.value : 1, { silent: true }), loadMetrics()])
    editingId.value = null
  } catch (error) {
    toast(normalizeError(error) || 'Lưu thất bại', 'warn')
  } finally {
    saving.value = false
  }
}

function buildPayload() {
  const code = form.value.code?.trim()
  const name = form.value.name?.trim()
  const value = Number(form.value.value)

  if (!code || !name || !form.value.start || !form.value.end || !value) {
    toast('Vui lòng nhập đầy đủ thông tin bắt buộc', 'warn')
    return null
  }

  if (value <= 0 || value > 100) {
    toast('Phần trăm giảm phải từ 1 đến 100', 'warn')
    return null
  }

  return {
    ma: code.toUpperCase(),
    ten: name,
    phanTramGiam: value,
    ngayBatDau: `${form.value.start}T00:00:00`,
    ngayKetThuc: `${form.value.end}T23:59:59`,
  }
}

function formatDisplayDate(value) {
  if (!value) return '--'
  return new Intl.DateTimeFormat('vi-VN').format(new Date(value))
}

function toDateInput(value) {
  return value ? String(value).slice(0, 10) : ''
}

function normalizeError(error) {
  return typeof error === 'string' ? error : error?.message
}

let searchTimer
watch([searchQuery, currentTab], () => {
  clearTimeout(searchTimer)
  searchTimer = setTimeout(() => loadData(1), 300)
})

onMounted(async () => {
  await Promise.all([loadData(1), loadMetrics()])
})
</script>

