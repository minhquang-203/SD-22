<script setup>
import { computed, onUnmounted, reactive, ref, watch } from 'vue'
import { Icon } from '@iconify/vue'
import { createDiaChiToi, fetchDiaChiToi, updateDiaChiToi } from '@/api/khachHangApi'
import { fetchProvinces, fetchWards, isNewWardCode } from '@/api/shipping'
import { getPhoneValidationError, normalizePhoneDigits } from '@/utils/phone'

const props = defineProps({
  visible: { type: Boolean, default: false },
  mode: { type: String, default: 'pick' },
  selectedId: { type: Number, default: null },
  editingAddress: { type: Object, default: null },
  defaultContact: { type: Object, default: () => ({ hoTen: '', soDienThoai: '' }) },
})

const emit = defineEmits(['update:visible', 'select', 'saved'])

const view = ref('pick')
const loading = ref(false)
const saving = ref(false)
const loadError = ref('')
const addresses = ref([])

const provinces = ref([])
const wards = ref([])
const addressLoading = reactive({ provinces: false, wards: false })

const form = reactive({
  hoTenNguoiNhan: '',
  soDienThoai: '',
  provinceId: null,
  wardCode: '',
  tinhThanh: '',
  phuongXa: '',
  diaChiChiTiet: '',
  macDinh: false,
})

const fieldErrors = reactive({ soDienThoai: '', diaChiChiTiet: '' })
const editingId = ref(null)

const hasAddresses = computed(() => addresses.value.length > 0)
const modalTitle = computed(() => {
  if (view.value === 'form') {
    return editingId.value ? 'Cập nhật địa chỉ' : 'Thêm địa chỉ người nhận'
  }
  return 'Chọn địa chỉ người nhận'
})

function formatAddressLine(address) {
  return [address.diaChiChiTiet, address.phuongXa, address.tinhThanh]
    .map((part) => String(part || '').trim())
    .filter(Boolean)
    .join(', ')
}

function isUsableAddress(address) {
  return Boolean(address?.provinceId && isNewWardCode(address?.wardCode))
}

function closeModal() {
  emit('update:visible', false)
}

function onBackdrop(event) {
  if (event.target === event.currentTarget) {
    closeModal()
  }
}

function resetForm() {
  form.hoTenNguoiNhan = ''
  form.soDienThoai = ''
  form.provinceId = null
  form.wardCode = ''
  form.tinhThanh = ''
  form.phuongXa = ''
  form.diaChiChiTiet = ''
  form.macDinh = false
  fieldErrors.soDienThoai = ''
  fieldErrors.diaChiChiTiet = ''
  editingId.value = null
  wards.value = []
}

function fillFormFromAddress(address) {
  if (!address) {
    resetForm()
    return
  }
  editingId.value = address.id ?? null
  form.hoTenNguoiNhan = address.hoTenNguoiNhan || ''
  form.soDienThoai = address.soDienThoai || ''
  form.provinceId = isUsableAddress(address) ? (address.provinceId ?? null) : null
  form.wardCode = isUsableAddress(address) ? (address.wardCode || '') : ''
  form.tinhThanh = address.tinhThanh || ''
  form.phuongXa = address.phuongXa || ''
  form.diaChiChiTiet = address.diaChiChiTiet || ''
  form.macDinh = Boolean(address.macDinh)
}

async function loadProvinces() {
  if (provinces.value.length) return
  addressLoading.provinces = true
  try {
    const res = await fetchProvinces()
    provinces.value = res.data || []
  } finally {
    addressLoading.provinces = false
  }
}

async function loadWardsForProvince(provinceId) {
  if (!provinceId) {
    wards.value = []
    return
  }
  addressLoading.wards = true
  try {
    const res = await fetchWards(provinceId)
    wards.value = res.data || []
  } finally {
    addressLoading.wards = false
  }
}

async function onProvinceChange() {
  form.wardCode = ''
  wards.value = []
  const selected = provinces.value.find((p) => p.provinceId === form.provinceId)
  form.tinhThanh = selected?.provinceName || ''
  form.phuongXa = ''
  await loadWardsForProvince(form.provinceId)
}

function onWardChange() {
  const selected = wards.value.find((w) => w.wardCode === form.wardCode)
  form.phuongXa = selected?.wardName || ''
}

function onPhoneInput(event) {
  form.soDienThoai = normalizePhoneDigits(event.target.value)
  fieldErrors.soDienThoai = ''
}

function validateForm() {
  if (!form.hoTenNguoiNhan.trim()) return 'Vui lòng nhập họ tên người nhận'
  const phoneError = getPhoneValidationError(form.soDienThoai)
  if (phoneError) {
    fieldErrors.soDienThoai = phoneError
    return phoneError
  }
  if (!form.provinceId) return 'Vui lòng chọn tỉnh / thành phố'
  if (!form.wardCode) return 'Vui lòng chọn phường / xã'
  if (!form.diaChiChiTiet.trim()) {
    fieldErrors.diaChiChiTiet = 'Vui lòng nhập địa chỉ cụ thể'
    return fieldErrors.diaChiChiTiet
  }
  return ''
}

async function loadAddresses() {
  loading.value = true
  loadError.value = ''
  try {
    const res = await fetchDiaChiToi()
    addresses.value = res.data || []
  } catch (error) {
    addresses.value = []
    loadError.value = typeof error === 'string' ? error : 'Không tải được danh sách địa chỉ'
  } finally {
    loading.value = false
  }
}

function openForm(address = null) {
  view.value = 'form'
  fillFormFromAddress(address)
  if (!address) {
    if (!form.hoTenNguoiNhan.trim()) {
      form.hoTenNguoiNhan = props.defaultContact.hoTen || ''
    }
    if (!form.soDienThoai.trim()) {
      form.soDienThoai = props.defaultContact.soDienThoai || ''
    }
  } else if (!isUsableAddress(address)) {
    loadError.value = 'Địa chỉ cũ (3 cấp) cần chọn lại Tỉnh/Thành và Phường/Xã theo đơn vị hành chính mới.'
    form.provinceId = null
    form.wardCode = ''
  }
  void loadProvinces().then(async () => {
    if (form.provinceId) {
      await loadWardsForProvince(form.provinceId)
    }
  })
}

function openPick() {
  view.value = 'pick'
  resetForm()
}

function selectAddress(address) {
  if (!isUsableAddress(address)) {
    loadError.value = 'Địa chỉ này cần cập nhật theo đơn vị hành chính 2 cấp (Tỉnh → Phường/Xã).'
    openForm(address)
    return
  }
  emit('select', address)
  closeModal()
}

async function saveAddress() {
  const message = validateForm()
  if (message) {
    loadError.value = message
    return
  }

  saving.value = true
  loadError.value = ''
  const payload = {
    hoTenNguoiNhan: form.hoTenNguoiNhan.trim(),
    soDienThoai: form.soDienThoai.trim(),
    provinceId: form.provinceId,
    districtId: null,
    wardCode: form.wardCode,
    tinhThanh: form.tinhThanh.trim(),
    quanHuyen: '',
    phuongXa: form.phuongXa.trim(),
    diaChiChiTiet: form.diaChiChiTiet.trim(),
    macDinh: form.macDinh,
  }

  try {
    const res = editingId.value
      ? await updateDiaChiToi(editingId.value, payload)
      : await createDiaChiToi(payload)
    await loadAddresses()
    emit('saved', res.data)
    emit('select', res.data)
    closeModal()
  } catch (error) {
    loadError.value = typeof error === 'string' ? error : 'Không lưu được địa chỉ'
  } finally {
    saving.value = false
  }
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
      loadError.value = ''
      document.body.style.overflow = 'hidden'
      document.addEventListener('keydown', onKeydown)
      view.value = props.mode === 'form' ? 'form' : 'pick'
      void loadAddresses().then(() => {
        if (view.value === 'form') {
          openForm(props.editingAddress)
        }
      })
    } else {
      document.body.style.overflow = ''
      document.removeEventListener('keydown', onKeydown)
      resetForm()
      view.value = 'pick'
      loadError.value = ''
    }
  },
)

onUnmounted(() => {
  document.body.style.overflow = ''
  document.removeEventListener('keydown', onKeydown)
})
</script>

<template>
  <Teleport to="body">
    <div v-if="visible" class="sf-recipient-modal-backdrop" @click="onBackdrop">
      <div
        class="sf-recipient-modal"
        role="dialog"
        aria-modal="true"
        aria-labelledby="sf-recipient-modal-title"
        @click.stop
      >
        <header class="sf-recipient-modal__header">
          <div>
            <p class="sf-recipient-modal__eyebrow">Giao hàng</p>
            <h2 id="sf-recipient-modal-title">{{ modalTitle }}</h2>
          </div>
          <button type="button" class="sf-recipient-modal__close" aria-label="Đóng" @click="closeModal">
            <Icon icon="mdi:close" width="22" />
          </button>
        </header>

        <div class="sf-recipient-modal__body">
          <div v-if="view === 'pick'">
            <div v-if="loading" class="sf-recipient-modal__state">
              <Icon icon="svg-spinners:ring-resize" width="28" />
              <span>Đang tải địa chỉ...</span>
            </div>

            <div v-else-if="loadError && !hasAddresses" class="sf-recipient-modal__state">
              <Icon icon="solar:danger-circle-linear" width="28" />
              <span>{{ loadError }}</span>
            </div>

            <template v-else>
              <ul v-if="hasAddresses" class="sf-recipient-modal__list">
                <li v-for="address in addresses" :key="address.id">
                  <button
                    type="button"
                    class="sf-recipient-card"
                    :class="{ selected: selectedId === address.id }"
                    @click="selectAddress(address)"
                  >
                    <div class="sf-recipient-card__top">
                      <div>
                        <div class="sf-recipient-card__name">{{ address.hoTenNguoiNhan }}</div>
                        <div class="sf-recipient-card__phone">{{ address.soDienThoai }}</div>
                      </div>
                      <span v-if="address.macDinh" class="sf-recipient-card__badge">Mặc định</span>
                    </div>
                    <p class="sf-recipient-card__address">{{ formatAddressLine(address) }}</p>
                    <p v-if="!isUsableAddress(address)" class="sf-recipient-card__hint">
                      Cần cập nhật địa chỉ 2 cấp
                    </p>
                  </button>
                </li>
              </ul>

              <div v-else class="sf-recipient-modal__state">
                <Icon icon="solar:map-point-linear" width="28" />
                <span>Bạn chưa có địa chỉ người nhận nào.</span>
              </div>

              <button type="button" class="sf-recipient-modal__add" @click="openForm()">
                <Icon icon="solar:add-circle-linear" width="18" />
                Thêm địa chỉ mới
              </button>
            </template>
          </div>

          <div v-else class="sf-recipient-form">
            <div class="sf-recipient-form__row">
              <div class="sf-recipient-form__field">
                <label for="recipient-name">Họ và tên</label>
                <input id="recipient-name" v-model="form.hoTenNguoiNhan" type="text" autocomplete="name" />
              </div>
              <div class="sf-recipient-form__field">
                <label for="recipient-phone">Số điện thoại</label>
                <input
                  id="recipient-phone"
                  :value="form.soDienThoai"
                  type="tel"
                  inputmode="numeric"
                  maxlength="10"
                  placeholder="0xxxxxxxxx"
                  :class="{ 'is-invalid': fieldErrors.soDienThoai }"
                  @input="onPhoneInput"
                />
                <span v-if="fieldErrors.soDienThoai" class="sf-recipient-form__error">{{ fieldErrors.soDienThoai }}</span>
              </div>
            </div>

            <div class="sf-recipient-form__row">
              <div class="sf-recipient-form__field">
                <label for="recipient-province">Tỉnh / Thành phố</label>
                <select
                  id="recipient-province"
                  v-model="form.provinceId"
                  :disabled="addressLoading.provinces"
                  @change="onProvinceChange"
                >
                  <option :value="null">
                    {{ addressLoading.provinces ? 'Đang tải...' : 'Chọn tỉnh / thành phố' }}
                  </option>
                  <option v-for="p in provinces" :key="p.provinceId" :value="p.provinceId">
                    {{ p.provinceName }}
                  </option>
                </select>
              </div>
              <div class="sf-recipient-form__field">
                <label for="recipient-ward">Phường / Xã</label>
                <select
                  id="recipient-ward"
                  v-model="form.wardCode"
                  :disabled="!form.provinceId || addressLoading.wards"
                  @change="onWardChange"
                >
                  <option value="">
                    {{ addressLoading.wards ? 'Đang tải...' : 'Chọn phường / xã' }}
                  </option>
                  <option v-for="w in wards" :key="w.wardCode" :value="w.wardCode">
                    {{ w.wardName }}
                  </option>
                </select>
              </div>
            </div>

            <div class="sf-recipient-form__row">
              <div class="sf-recipient-form__field sf-recipient-form__field--full">
                <label for="recipient-detail">Địa chỉ cụ thể <span class="sf-required">*</span></label>
                <input
                  id="recipient-detail"
                  v-model="form.diaChiChiTiet"
                  type="text"
                  placeholder="Số nhà, tên đường..."
                  :class="{ 'is-invalid': fieldErrors.diaChiChiTiet }"
                  @input="fieldErrors.diaChiChiTiet = ''"
                  @blur="form.diaChiChiTiet.trim() || (fieldErrors.diaChiChiTiet = 'Vui lòng nhập địa chỉ cụ thể')"
                />
                <span v-if="fieldErrors.diaChiChiTiet" class="sf-recipient-form__error">{{ fieldErrors.diaChiChiTiet }}</span>
              </div>
            </div>

            <label class="sf-recipient-form__check">
              <input v-model="form.macDinh" type="checkbox" />
              Đặt làm địa chỉ mặc định
            </label>

            <p v-if="loadError" class="sf-recipient-form__error">{{ loadError }}</p>
          </div>
        </div>

        <footer class="sf-recipient-modal__footer">
          <button
            v-if="view === 'form' && hasAddresses"
            type="button"
            class="sf-recipient-modal__back"
            @click="openPick"
          >
            Quay lại danh sách
          </button>
          <button type="button" class="sf-recipient-modal__cancel" @click="closeModal">
            Đóng
          </button>
          <button
            v-if="view === 'form'"
            type="button"
            class="sf-recipient-modal__save"
            :disabled="saving"
            @click="saveAddress"
          >
            {{ saving ? 'Đang lưu...' : 'Lưu địa chỉ' }}
          </button>
        </footer>
      </div>
    </div>
  </Teleport>
</template>

<style>
@import '@/styles/checkoutRecipientModal.css';
</style>
