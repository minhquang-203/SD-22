<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import AdminSwitch from '@/components/admin/AdminSwitch.vue'
import { ATTRIBUTE_TYPES } from '@/constants/attributeConfig'

const route = useRoute()

const config = computed(() => ATTRIBUTE_TYPES[route.params.type])

const loading = ref(false)
const saving = ref(false)
const message = ref('')
const messageType = ref('success')

const allRows = ref([])
const filter = reactive({
  keyword: '',
  status: null,
})

const page = ref(1)
const pageSize = ref(10)

const showModal = ref(false)
const modalMode = ref('add')
const editingId = ref(null)
const form = ref({})
const formError = ref('')

let searchTimer = null

const filteredRows = computed(() => {
  if (!config.value) return []
  let rows = [...allRows.value]
  const kw = filter.keyword.trim().toLowerCase()
  if (kw) {
    rows = rows.filter((row) =>
      Object.values(row).some((v) => String(v || '').toLowerCase().includes(kw)),
    )
  }
  if (config.value.hasStatus && filter.status !== null) {
    const active = filter.status === 'active'
    rows = rows.filter((row) => (row.trangThai !== false) === active)
  }
  return rows
})

const totalPages = computed(() => Math.max(1, Math.ceil(filteredRows.value.length / pageSize.value)))

const pagedRows = computed(() => {
  const start = (page.value - 1) * pageSize.value
  return filteredRows.value.slice(start, start + pageSize.value)
})

function notify(text, type = 'success') {
  message.value = text
  messageType.value = type
  setTimeout(() => { message.value = '' }, 3000)
}

function emptyForm() {
  const data = {}
  config.value?.fields.forEach((f) => {
    data[f.key] = f.default ?? (f.type === 'color' ? '#000000' : '')
  })
  return data
}

async function fetchData() {
  if (!config.value) return
  loading.value = true
  try {
    const res = await config.value.api.list()
    allRows.value = res.data || []
    page.value = 1
  } catch (err) {
    notify(String(err), 'error')
  } finally {
    loading.value = false
  }
}

function resetFilters() {
  filter.keyword = ''
  filter.status = null
  page.value = 1
}

function openAdd() {
  modalMode.value = 'add'
  editingId.value = null
  form.value = emptyForm()
  formError.value = ''
  showModal.value = true
}

function openEdit(row) {
  modalMode.value = 'edit'
  editingId.value = row.id
  form.value = emptyForm()
  formError.value = ''
  config.value.fields.forEach((f) => {
    form.value[f.key] = row[f.key] ?? (f.type === 'color' ? '#000000' : '')
  })
  showModal.value = true
}

function validateForm() {
  for (const field of config.value.fields) {
    if (field.required && !String(form.value[field.key] || '').trim()) {
      return `Vui lòng nhập ${field.label.toLowerCase()}`
    }
  }

  const maField = config.value.fields.find((f) => f.key === 'ma')
  if (maField) {
    const ma = String(form.value.ma || '').trim().toLowerCase()
    if (ma) {
      const duplicated = allRows.value.some(
        (row) =>
          String(row.ma || '').trim().toLowerCase() === ma
          && (modalMode.value === 'add' || row.id !== editingId.value),
      )
      if (duplicated) {
        return `Mã "${form.value.ma}" đã tồn tại. Vui lòng dùng mã khác.`
      }
    }
  }

  return null
}

async function handleSave() {
  formError.value = ''
  const err = validateForm()
  if (err) {
    formError.value = err
    return
  }
  if (!confirm(modalMode.value === 'add' ? 'Thêm mới bản ghi này?' : 'Cập nhật bản ghi này?')) return

  saving.value = true
  try {
    const payload = { ...form.value }
    if (modalMode.value === 'add') {
      await config.value.api.add(payload)
      notify('Thêm mới thành công')
    } else {
      await config.value.api.update(editingId.value, payload)
      notify('Cập nhật thành công')
    }
    showModal.value = false
    await fetchData()
  } catch (e) {
    formError.value = String(e)
  } finally {
    saving.value = false
  }
}

function rowToUpdatePayload(row, trangThai) {
  const payload = { trangThai }
  config.value.fields.forEach((f) => {
    payload[f.key] = row[f.key] ?? (f.type === 'color' ? '#000000' : '')
  })
  return payload
}

async function handleToggleStatus(row) {
  if (!config.value.hasStatus) return
  const isActive = row.trangThai !== false
  const action = isActive ? 'ngưng hoạt động' : 'kích hoạt lại'
  if (!confirm(`Bạn có chắc muốn ${action} "${row.ten}"?`)) return

  try {
    if (isActive) {
      await config.value.api.remove(row.id)
      notify('Đã chuyển sang ngưng hoạt động')
    } else {
      await config.value.api.update(row.id, rowToUpdatePayload(row, true))
      notify('Đã kích hoạt lại')
    }
    await fetchData()
  } catch (e) {
    notify(String(e), 'error')
  }
}

watch(() => route.params.type, () => {
  resetFilters()
  fetchData()
})

watch(() => filter.keyword, () => {
  clearTimeout(searchTimer)
  searchTimer = setTimeout(() => { page.value = 1 }, 300)
})

watch(filteredRows, () => {
  if (page.value > totalPages.value) page.value = totalPages.value
})

onMounted(fetchData)
</script>

<template>
  <div v-if="config" class="space-y-4">
    <!-- Page header -->
    <div class="admin-page-header">
      <div class="admin-page-header__icon">🏷️</div>
      <div>
        <h1 class="admin-page-title">{{ config.title }}</h1>
        <p class="admin-page-subtitle">{{ config.subtitle }}</p>
      </div>
    </div>

    <div
      v-if="message"
      class="admin-alert rounded-xl px-4 py-3 text-sm"
      :class="messageType === 'error' ? 'admin-alert-error' : 'admin-alert-success'"
    >
      {{ message }}
    </div>

    <!-- Filter card -->
    <div class="admin-card admin-card--rounded">
      <div class="admin-card-header">
        <h3>Bộ lọc tìm kiếm</h3>
        <button type="button" class="admin-icon-btn admin-icon-btn--primary" title="Làm mới bộ lọc" @click="resetFilters">
          ↺
        </button>
      </div>
      <div class="p-4 md:p-5 grid grid-cols-1 md:grid-cols-2 gap-4">
        <div>
          <label class="admin-label">Tìm kiếm chung</label>
          <div class="relative">
            <span class="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400">🔍</span>
            <input
              v-model="filter.keyword"
              class="admin-input !pl-9"
              :placeholder="config.searchPlaceholder"
            />
          </div>
        </div>
        <div v-if="config.hasStatus">
          <label class="admin-label">Trạng thái</label>
          <div class="flex flex-wrap gap-4 pt-1">
            <label class="admin-radio">
              <input v-model="filter.status" type="radio" :value="null" />
              <span>Tất cả</span>
            </label>
            <label class="admin-radio">
              <input v-model="filter.status" type="radio" value="active" />
              <span>Hoạt động</span>
            </label>
            <label class="admin-radio">
              <input v-model="filter.status" type="radio" value="inactive" />
              <span>Ngưng</span>
            </label>
          </div>
        </div>
      </div>
    </div>

    <!-- Table card -->
    <div class="admin-card admin-card--rounded">
      <div class="admin-card-header">
        <h3>{{ config.listTitle }}</h3>
        <div class="flex gap-2">
          <button type="button" class="admin-fab-btn admin-fab-btn--primary group" @click="openAdd">
            <span>＋</span>
            <span class="admin-fab-btn__label">Thêm mới</span>
          </button>
          <button type="button" class="admin-fab-btn admin-fab-btn--info group" @click="fetchData">
            <span>⟳</span>
            <span class="admin-fab-btn__label">Tải lại</span>
          </button>
        </div>
      </div>

      <div class="overflow-x-auto">
        <table class="admin-table admin-table--striped">
          <thead>
            <tr>
              <th class="w-14 text-center">STT</th>
              <th v-for="col in config.columns" :key="col.key">{{ col.label }}</th>
              <th v-if="config.hasStatus" class="text-center w-28">Trạng thái</th>
              <th class="text-center w-24">Thao tác</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="loading">
              <td :colspan="config.columns.length + (config.hasStatus ? 3 : 2)" class="text-center py-10 text-gray-400">
                Đang tải...
              </td>
            </tr>
            <tr v-else-if="pagedRows.length === 0">
              <td :colspan="config.columns.length + (config.hasStatus ? 3 : 2)" class="text-center py-10 text-gray-400">
                Không có dữ liệu
              </td>
            </tr>
            <tr v-for="(row, index) in pagedRows" :key="row.id">
              <td class="text-center">{{ (page - 1) * pageSize + index + 1 }}</td>
              <td v-for="col in config.columns" :key="col.key">
                <template v-if="col.type === 'color'">
                  <div class="flex items-center gap-2">
                    <span
                      class="w-6 h-6 rounded-md border border-gray-200 shadow-sm shrink-0"
                      :style="{ backgroundColor: row.maHex || '#ccc' }"
                    />
                    <span class="font-mono text-xs text-gray-500">{{ row.maHex || '—' }}</span>
                  </div>
                </template>
                <template v-else>
                  <span
                    :class="{
                      'font-semibold text-[var(--admin-primary)]': col.primary,
                      'font-semibold text-gray-700': col.bold,
                    }"
                  >
                    {{ row[col.key] || '—' }}
                  </span>
                </template>
              </td>
              <td v-if="config.hasStatus" class="text-center">
                <div class="flex flex-col items-center gap-1">
                  <AdminSwitch
                    :model-value="row.trangThai !== false"
                    @update:model-value="handleToggleStatus(row)"
                  />
                  <span
                    class="text-xs"
                    :class="row.trangThai !== false ? 'text-[var(--admin-primary)]' : 'text-gray-400'"
                  >
                    {{ row.trangThai !== false ? 'Hoạt động' : 'Ngưng' }}
                  </span>
                </div>
              </td>
              <td class="text-center">
                <button
                  type="button"
                  class="admin-icon-btn admin-icon-btn--warning"
                  title="Sửa"
                  @click="openEdit(row)"
                >
                  ✏️
                </button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <div class="px-4 py-3 border-t flex items-center justify-between" style="border-color: var(--admin-border)">
        <span class="text-sm text-gray-500">{{ filteredRows.length }} bản ghi</span>
        <div class="flex items-center gap-2">
          <select v-model.number="pageSize" class="admin-select !w-auto">
            <option :value="5">5</option>
            <option :value="10">10</option>
            <option :value="20">20</option>
            <option :value="50">50</option>
          </select>
          <button type="button" class="admin-btn admin-btn-default" :disabled="page <= 1" @click="page--">‹</button>
          <span class="text-sm">{{ page }} / {{ totalPages }}</span>
          <button type="button" class="admin-btn admin-btn-default" :disabled="page >= totalPages" @click="page++">›</button>
        </div>
      </div>
    </div>

    <!-- Modal -->
    <div v-if="showModal" class="modal-overlay" @click.self="showModal = false">
      <div class="modal-panel" style="max-width: 500px">
        <div class="px-5 py-4 border-b flex justify-between items-center" style="border-color: var(--admin-border)">
          <h2 class="text-lg font-semibold text-gray-800">
            {{ modalMode === 'add' ? 'Thêm mới' : 'Cập nhật' }}
          </h2>
          <button type="button" class="admin-btn admin-btn-default !px-2" @click="showModal = false">✕</button>
        </div>
        <div class="p-5 space-y-4">
          <div
            v-if="formError"
            class="admin-alert admin-alert-error rounded-lg px-4 py-3 text-sm flex items-start gap-2"
          >
            <span class="shrink-0">⚠️</span>
            <span>{{ formError }}</span>
          </div>
          <div v-for="field in config.fields" :key="field.key">
            <label class="admin-label">
              {{ field.label }}
              <span v-if="field.required" class="text-[var(--admin-primary)]">*</span>
            </label>
            <textarea
              v-if="field.type === 'textarea'"
              v-model="form[field.key]"
              class="admin-input min-h-[80px]"
              :placeholder="field.placeholder"
            />
            <div v-else-if="field.type === 'color'" class="space-y-2">
              <input v-model="form[field.key]" type="color" class="w-full h-10 rounded-lg cursor-pointer border" />
              <input v-model="form[field.key]" class="admin-input font-mono" placeholder="#000000" />
              <div class="flex flex-wrap gap-2">
                <button
                  v-for="hex in ['#000000','#FFFFFF','#FF0000','#F5C6C6','#FFE4B5','#90EE90','#87CEEB','#DDA0DD','#C0C0C0']"
                  :key="hex"
                  type="button"
                  class="w-7 h-7 rounded-md border"
                  :style="{ backgroundColor: hex }"
                  @click="form[field.key] = hex"
                />
              </div>
            </div>
            <input
              v-else
              v-model="form[field.key]"
              class="admin-input"
              :placeholder="field.placeholder"
            />
          </div>
        </div>
        <div class="px-5 py-4 border-t flex justify-end gap-3" style="border-color: var(--admin-border)">
          <button type="button" class="admin-btn admin-btn-default" @click="showModal = false">Đóng</button>
          <button type="button" class="admin-btn admin-btn-primary" :disabled="saving" @click="handleSave">
            {{ saving ? 'Đang lưu...' : 'Lưu thay đổi' }}
          </button>
        </div>
      </div>
    </div>
  </div>

  <div v-else class="admin-card p-8 text-center text-gray-500">
    Không tìm thấy loại thuộc tính
  </div>
</template>
