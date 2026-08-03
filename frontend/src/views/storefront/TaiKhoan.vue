<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { RouterLink, useRouter } from 'vue-router'
import { Icon } from '@iconify/vue'
import {
  fetchKhachToi,
  updateKhachToi,
  doiMatKhauToi,
  fetchDiaChiToi,
  updateDiaChiToi,
  fetchMyQuizResult,
} from '@/api/khachHangApi'
import { useAuth } from '@/composables/useAuth'
import { confirm } from '@/composables/useConfirm'
import CheckoutRecipientModal from '@/components/storefront/CheckoutRecipientModal.vue'
import { getProducts } from '@/api/sanPhamApi'
import { getRoutinesByLoaiDa } from '@/api/routineApi'
import { formatVND } from '@/utils/formatVND'
import { productImageUrl } from '@/utils/productImage'

const router = useRouter()
const { dangXuat, updateHoTen } = useAuth()

const activeSection = ref('info')
const loading = ref(true)
const savingProfile = ref(false)
const savingPassword = ref(false)
const profileMsg = ref('')
const profileError = ref('')
const passwordMsg = ref('')
const passwordError = ref('')

const addresses = ref([])
const addressesLoading = ref(false)
const addressMsg = ref('')
const addressError = ref('')
const selectedAddressId = ref(null)
const showRecipientModal = ref(false)
const recipientModalMode = ref('pick')
const editingAddress = ref(null)
const settingDefaultId = ref(null)

const profileForm = reactive({
  hoTen: '',
  email: '',
  soDienThoai: '',
})

const passwordForm = reactive({
  matKhauCu: '',
  matKhauMoi: '',
  matKhauMoiXacNhan: '',
})
const showOldPw = ref(false)
const showNewPw = ref(false)
const showConfirmPw = ref(false)

const menuItems = [
  { id: 'info', label: 'Thông tin tài khoản', icon: 'solar:user-circle-linear' },
  { id: 'addresses', label: 'Địa chỉ', icon: 'solar:map-point-linear' },
  { id: 'orders', label: 'Đơn hàng của tôi', icon: 'solar:bag-check-linear', to: '/don-hang' },
  { id: 'quiz', label: 'Hồ sơ da (Quiz)', icon: 'solar:clipboard-list-linear' },
  { id: 'password', label: 'Đổi mật khẩu', icon: 'solar:lock-keyhole-linear' },
]

const selectedAddress = computed(() => {
  return addresses.value.find((item) => item.id === selectedAddressId.value)
    || addresses.value.find((item) => item.macDinh)
    || addresses.value[0]
    || null
})

const selectedAddressLine = computed(() => {
  if (!selectedAddress.value) return ''
  return formatAddressLine(selectedAddress.value)
})

const selectedRecipientSummary = computed(() => {
  if (!selectedAddress.value) return ''
  return [selectedAddress.value.hoTenNguoiNhan, selectedAddress.value.soDienThoai]
    .map((part) => String(part || '').trim())
    .filter(Boolean)
    .join(' · ')
})

function formatAddressLine(address) {
  return [address.diaChiChiTiet, address.phuongXa, address.quanHuyen, address.tinhThanh]
    .map((part) => String(part || '').trim())
    .filter(Boolean)
    .join(', ')
}

async function loadProfile() {
  loading.value = true
  profileError.value = ''
  try {
    const res = await fetchKhachToi()
    profileForm.hoTen = res.data.hoTen || ''
    profileForm.email = res.data.email || ''
    profileForm.soDienThoai = res.data.soDienThoai || ''
  } catch (e) {
    profileError.value = typeof e === 'string' ? e : 'Không tải được thông tin tài khoản'
  } finally {
    loading.value = false
  }
}

const quizData = ref(null)
const quizLoading = ref(false)
const quizError = ref('')
const recommendedProducts = ref([])
const routineCombo = ref(null)

async function loadQuizResult() {
  quizLoading.value = true
  quizError.value = ''
  try {
    const res = await fetchMyQuizResult()
    quizData.value = res.data

    if (quizData.value && quizData.value.idLoaiDa) {
      try {
        const routineRes = await getRoutinesByLoaiDa(quizData.value.idLoaiDa)
        if (routineRes.data && routineRes.data.length > 0) {
          routineCombo.value = routineRes.data[0]
        } else {
          routineCombo.value = null
        }
      } catch (err) {
        routineCombo.value = null
      }

      const productRes = await getProducts()
      const allProds = productRes.data || []
      
      // Lọc các sản phẩm hỗ trợ loại da này
      let suitable = allProds.filter(p => p.trangThai !== false && p.idLoaiDas && p.idLoaiDas.includes(quizData.value.idLoaiDa))
      
      // Nếu không có sản phẩm nào hợp chuẩn, lấy top bán chạy hoặc lấy đại
      if (suitable.length === 0) {
        suitable = allProds.filter(p => p.trangThai !== false).slice(0, 4)
      } else {
        suitable = suitable.slice(0, 4) // Lấy tối đa 4 sản phẩm
      }
      recommendedProducts.value = suitable
    }
  } catch (e) {
    quizError.value = typeof e === 'string' ? e : 'Không tải được kết quả quiz'
  } finally {
    quizLoading.value = false
  }
}

async function saveProfile() {
  profileMsg.value = ''
  profileError.value = ''
  if (!profileForm.hoTen.trim() || !profileForm.email.trim() || !profileForm.soDienThoai.trim()) {
    profileError.value = 'Vui lòng nhập đầy đủ thông tin'
    return
  }
  savingProfile.value = true
  try {
    const res = await updateKhachToi({
      hoTen: profileForm.hoTen.trim(),
      email: profileForm.email.trim(),
      soDienThoai: profileForm.soDienThoai.trim(),
    })
    updateHoTen(res.data.hoTen)
    profileMsg.value = 'Đã cập nhật thông tin tài khoản.'
  } catch (e) {
    profileError.value = typeof e === 'string' ? e : 'Cập nhật thất bại'
  } finally {
    savingProfile.value = false
  }
}

async function savePassword() {
  passwordMsg.value = ''
  passwordError.value = ''
  if (!passwordForm.matKhauCu || !passwordForm.matKhauMoi) {
    passwordError.value = 'Vui lòng nhập đầy đủ mật khẩu'
    return
  }
  if (passwordForm.matKhauMoi.length < 6) {
    passwordError.value = 'Mật khẩu mới tối thiểu 6 ký tự'
    return
  }
  if (passwordForm.matKhauMoi !== passwordForm.matKhauMoiXacNhan) {
    passwordError.value = 'Mật khẩu nhập lại không khớp'
    return
  }
  savingPassword.value = true
  try {
    await doiMatKhauToi({
      matKhauCu: passwordForm.matKhauCu,
      matKhauMoi: passwordForm.matKhauMoi,
    })
    passwordMsg.value = 'Đã đổi mật khẩu thành công.'
    passwordForm.matKhauCu = ''
    passwordForm.matKhauMoi = ''
    passwordForm.matKhauMoiXacNhan = ''
  } catch (e) {
    passwordError.value = typeof e === 'string' ? e : 'Đổi mật khẩu thất bại'
  } finally {
    savingPassword.value = false
  }
}

async function loadAddresses(preferId = null) {
  addressesLoading.value = true
  addressError.value = ''
  try {
    const res = await fetchDiaChiToi()
    addresses.value = res.data || []
    const preferred = preferId != null
      ? addresses.value.find((item) => item.id === preferId)
      : null
    const selected = preferred
      || addresses.value.find((item) => item.id === selectedAddressId.value)
      || addresses.value.find((item) => item.macDinh)
      || addresses.value[0]
      || null
    selectedAddressId.value = selected?.id ?? null
  } catch (e) {
    addresses.value = []
    selectedAddressId.value = null
    addressError.value = typeof e === 'string' ? e : 'Không tải được danh sách địa chỉ'
  } finally {
    addressesLoading.value = false
  }
}

function openRecipientModal(mode = 'pick', address = null) {
  recipientModalMode.value = mode
  editingAddress.value = address
  showRecipientModal.value = true
}

async function setDefaultAddress(address) {
  if (!address?.id || address.macDinh) {
    selectedAddressId.value = address?.id ?? null
    return
  }

  settingDefaultId.value = address.id
  addressMsg.value = ''
  addressError.value = ''
  try {
    await updateDiaChiToi(address.id, {
      hoTenNguoiNhan: address.hoTenNguoiNhan,
      soDienThoai: address.soDienThoai,
      provinceId: address.provinceId,
      districtId: address.districtId,
      wardCode: address.wardCode,
      tinhThanh: address.tinhThanh,
      quanHuyen: address.quanHuyen,
      phuongXa: address.phuongXa,
      diaChiChiTiet: address.diaChiChiTiet,
      macDinh: true,
    })
    await loadAddresses(address.id)
    addressMsg.value = 'Đã đặt địa chỉ mặc định.'
  } catch (e) {
    addressError.value = typeof e === 'string' ? e : 'Không đặt được địa chỉ mặc định'
  } finally {
    settingDefaultId.value = null
  }
}

async function onRecipientSelected(address) {
  selectedAddressId.value = address?.id ?? null
  if (recipientModalMode.value === 'pick') {
    await setDefaultAddress(address)
  }
}

async function onRecipientSaved(address) {
  await loadAddresses(address?.id ?? null)
  addressMsg.value = 'Đã lưu địa chỉ.'
}

function selectSection(id) {
  activeSection.value = id
  profileMsg.value = ''
  profileError.value = ''
  passwordMsg.value = ''
  passwordError.value = ''
  addressMsg.value = ''
  addressError.value = ''
  if (id === 'addresses') {
    void loadAddresses()
  } else if (id === 'quiz') {
    void loadQuizResult()
  }
}

async function handleLogout() {
  const ok = await confirm({
    title: 'Đăng xuất',
    message: 'Bạn có chắc muốn đăng xuất?',
    confirmText: 'Đăng xuất',
    danger: true,
  })
  if (!ok) return
  dangXuat()
  router.push('/')
}

onMounted(loadProfile)
</script>

<template>
  <div class="sf-account-page">
    <div class="sf-container">
      <nav class="sf-breadcrumb">
        <RouterLink to="/">Trang chủ</RouterLink>
        <span>/</span>
        <span>Tài khoản</span>
      </nav>

      <h1 class="sf-account-page__title">Trung tâm tài khoản</h1>

      <div class="sf-account-layout">
        <aside class="sf-account-sidebar">
          <template v-for="item in menuItems" :key="item.id">
            <RouterLink
              v-if="item.to"
              :to="item.to"
              class="sf-account-sidebar__item"
            >
              <Icon v-if="item.icon" :icon="item.icon" width="18" />
              {{ item.label }}
            </RouterLink>
            <button
              v-else
              type="button"
              class="sf-account-sidebar__item"
              :class="{ active: activeSection === item.id }"
              @click="selectSection(item.id)"
            >
              <Icon v-if="item.icon" :icon="item.icon" width="18" />
              {{ item.label }}
            </button>
          </template>
          <button type="button" class="sf-account-sidebar__item sf-account-sidebar__item--logout" @click="handleLogout">
            <Icon icon="solar:logout-2-linear" width="18" />
            Đăng xuất
          </button>
        </aside>

        <div class="sf-account-main">
          <div v-if="loading" class="sf-account-loading">Đang tải...</div>

          <template v-else-if="activeSection === 'info'">
            <h2 class="sf-account-main__heading">Thông tin tài khoản</h2>
            <p class="sf-account-main__sub">Cập nhật họ tên, email và số điện thoại của bạn.</p>

            <div v-if="profileMsg" class="sf-account-alert sf-account-alert--ok">{{ profileMsg }}</div>
            <div v-if="profileError" class="sf-account-alert sf-account-alert--err">{{ profileError }}</div>

            <form class="sf-account-form" @submit.prevent="saveProfile">
              <div class="sf-field">
                <label for="acc-hoTen">Họ tên</label>
                <input id="acc-hoTen" v-model="profileForm.hoTen" type="text" autocomplete="name" />
              </div>
              <div class="sf-field">
                <label for="acc-email">Email</label>
                <input id="acc-email" v-model="profileForm.email" type="email" autocomplete="email" />
              </div>
              <div class="sf-field">
                <label for="acc-sdt">Số điện thoại</label>
                <input id="acc-sdt" v-model="profileForm.soDienThoai" type="tel" autocomplete="tel" />
              </div>
              <button type="submit" class="sf-account-submit" :disabled="savingProfile">
                {{ savingProfile ? 'Đang lưu...' : 'Cập nhật thông tin' }}
              </button>
            </form>
          </template>

          <template v-else-if="activeSection === 'addresses'">
            <h2 class="sf-account-main__heading">Địa chỉ giao hàng</h2>
            <p class="sf-account-main__sub">
              Chọn hoặc thêm địa chỉ nhận hàng — giống lúc đặt hàng. Địa chỉ mặc định sẽ được ưu tiên khi checkout.
            </p>

            <div v-if="addressMsg" class="sf-account-alert sf-account-alert--ok">{{ addressMsg }}</div>
            <div v-if="addressError" class="sf-account-alert sf-account-alert--err">{{ addressError }}</div>

            <div v-if="addressesLoading" class="sf-checkout-recipient-loading">
              <Icon icon="svg-spinners:ring-resize" width="22" />
              <span>Đang tải địa chỉ...</span>
            </div>

            <template v-else>
              <div
                v-if="selectedAddress"
                class="sf-checkout-recipient-summary"
                role="button"
                tabindex="0"
                @click="openRecipientModal('pick')"
                @keydown.enter.prevent="openRecipientModal('pick')"
                @keydown.space.prevent="openRecipientModal('pick')"
              >
                <div class="sf-checkout-recipient-summary__main">
                  <strong>{{ selectedRecipientSummary }}</strong>
                  <p>{{ selectedAddressLine }}</p>
                </div>
                <span class="sf-checkout-recipient-summary__action">
                  Thay đổi
                  <Icon icon="solar:alt-arrow-right-linear" width="16" />
                </span>
              </div>

              <div v-else class="sf-checkout-recipient-empty">
                <Icon icon="solar:map-point-linear" width="28" />
                <p>Chưa có địa chỉ người nhận. Vui lòng thêm để dùng khi đặt hàng.</p>
                <button type="button" class="sf-checkout-recipient-empty__btn" @click="openRecipientModal('form')">
                  Thêm địa chỉ người nhận
                </button>
              </div>

              <div v-if="addresses.length" class="sf-account-address-list">
                <div class="sf-account-address-list__head">
                  <h3>Danh sách địa chỉ</h3>
                  <button type="button" class="sf-account-address-add" @click="openRecipientModal('form')">
                    <Icon icon="solar:add-circle-linear" width="18" />
                    Thêm địa chỉ
                  </button>
                </div>

                <ul>
                  <li v-for="address in addresses" :key="address.id" class="sf-account-address-card">
                    <button
                      type="button"
                      class="sf-account-address-card__main"
                      :class="{ selected: selectedAddressId === address.id }"
                      @click="openRecipientModal('pick')"
                    >
                      <div class="sf-account-address-card__top">
                        <div>
                          <div class="sf-account-address-card__name">{{ address.hoTenNguoiNhan }}</div>
                          <div class="sf-account-address-card__phone">{{ address.soDienThoai }}</div>
                        </div>
                        <span v-if="address.macDinh" class="sf-account-address-card__badge">Mặc định</span>
                      </div>
                      <p class="sf-account-address-card__line">{{ formatAddressLine(address) }}</p>
                    </button>
                    <div class="sf-account-address-card__actions">
                      <button
                        v-if="!address.macDinh"
                        type="button"
                        class="sf-account-address-card__btn"
                        :disabled="settingDefaultId === address.id"
                        @click="setDefaultAddress(address)"
                      >
                        {{ settingDefaultId === address.id ? 'Đang lưu...' : 'Đặt mặc định' }}
                      </button>
                      <button
                        type="button"
                        class="sf-account-address-card__btn"
                        @click="openRecipientModal('form', address)"
                      >
                        Sửa
                      </button>
                    </div>
                  </li>
                </ul>
              </div>
            </template>

            <CheckoutRecipientModal
              v-model:visible="showRecipientModal"
              :mode="recipientModalMode"
              :selected-id="selectedAddressId"
              :editing-address="editingAddress"
              :default-contact="{ hoTen: profileForm.hoTen, soDienThoai: profileForm.soDienThoai }"
              @select="onRecipientSelected"
              @saved="onRecipientSaved"
            />
          </template>

          <template v-else-if="activeSection === 'quiz'">
            <h2 class="sf-account-main__heading">Hồ sơ da (Quiz)</h2>
            <p class="sf-account-main__sub">Kết quả bài phân tích da gần nhất của bạn.</p>
            
            <div v-if="quizLoading" class="sf-account-alert">Đang tải...</div>
            <div v-else-if="quizError" class="sf-account-alert sf-account-alert--err">{{ quizError }}</div>
            <div v-else-if="quizData" class="sf-account-card" style="padding: 24px; background: #fff; border-radius: 8px; border: 1px solid var(--border-color);">
              <h3 style="font-size: 1.25rem; margin-bottom: 8px;">Loại da của bạn: <strong style="color: var(--primary-color);">{{ quizData.tenLoaiDa }}</strong></h3>
              <p style="color: #666; margin-bottom: 16px;">{{ quizData.moTaLoaiDa }}</p>
              <p style="font-size: 0.875rem; color: #999; margin-bottom: 24px;">Ngày làm bài: {{ new Date(quizData.thoiGianLam).toLocaleString('vi-VN') }}</p>
              
              <!-- Hiển thị sản phẩm gợi ý ngay tại đây -->
              <div v-if="routineCombo" style="margin-top: 24px; padding-top: 24px; border-top: 1px solid #eee;">
                <h4 style="font-size: 1.1rem; margin-bottom: 8px; color: var(--text-color);">Combo dành riêng cho bạn</h4>
                <p style="color: #666; margin-bottom: 16px;"><strong>{{ routineCombo.ten }}</strong>: {{ routineCombo.moTa }}</p>
                <div style="display: grid; grid-template-columns: repeat(auto-fill, minmax(180px, 1fr)); gap: 16px; margin-bottom: 24px;">
                  <div 
                    v-for="ct in routineCombo.chiTiets" 
                    :key="ct.idSanPham" 
                    style="border: 1px solid #eee; border-radius: 8px; padding: 12px; cursor: pointer; transition: transform 0.2s;"
                    @click="router.push(`/san-pham/${ct.idSanPham}`)"
                    onmouseover="this.style.transform='translateY(-4px)'; this.style.boxShadow='0 4px 12px rgba(0,0,0,0.1)'"
                    onmouseout="this.style.transform='translateY(0)'; this.style.boxShadow='none'"
                  >
                    <div style="width: 100%; aspect-ratio: 1; margin-bottom: 12px; background: #f9f9f9; border-radius: 4px; overflow: hidden; display: flex; align-items: center; justify-content: center;">
                      <img v-if="ct.anhChinhUrl" :src="productImageUrl(ct.anhChinhUrl)" :alt="ct.tenSanPham" style="width: 100%; height: 100%; object-fit: contain;" />
                    </div>
                    <div style="font-size: 0.9rem; font-weight: 600; color: #333; margin-bottom: 8px; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden;">
                      Bước {{ ct.thuTu }}: {{ ct.tenSanPham }}
                    </div>
                  </div>
                </div>
              </div>

              <div v-else-if="recommendedProducts.length > 0" style="margin-top: 24px; padding-top: 24px; border-top: 1px solid #eee;">
                <h4 style="font-size: 1.1rem; margin-bottom: 16px; color: var(--text-color);">Sản phẩm chân ái dành cho bạn</h4>
                <div style="display: grid; grid-template-columns: repeat(auto-fill, minmax(180px, 1fr)); gap: 16px; margin-bottom: 24px;">
                  <div 
                    v-for="product in recommendedProducts" 
                    :key="product.id" 
                    style="border: 1px solid #eee; border-radius: 8px; padding: 12px; cursor: pointer; transition: transform 0.2s;"
                    @click="router.push(`/san-pham/${product.id}`)"
                    onmouseover="this.style.transform='translateY(-4px)'; this.style.boxShadow='0 4px 12px rgba(0,0,0,0.1)'"
                    onmouseout="this.style.transform='translateY(0)'; this.style.boxShadow='none'"
                  >
                    <div style="width: 100%; aspect-ratio: 1; margin-bottom: 12px; background: #f9f9f9; border-radius: 4px; overflow: hidden; display: flex; align-items: center; justify-content: center;">
                      <img v-if="product.anhChinhUrl" :src="productImageUrl(product.anhChinhUrl)" :alt="product.ten" style="width: 100%; height: 100%; object-fit: contain;" />
                    </div>
                    <div style="font-size: 0.9rem; font-weight: 600; color: #333; margin-bottom: 8px; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden;">
                      {{ product.ten }}
                    </div>
                    <div style="font-size: 0.9rem; font-weight: 700; color: var(--primary-color);">
                      {{ formatVND(product.giaSauGiamMin || product.giaMin || 0) }}
                    </div>
                  </div>
                </div>
              </div>

              <div style="display: flex; gap: 12px;">
                <RouterLink to="/quiz" class="sf-btn-primary">
                  Làm lại Quiz
                </RouterLink>
                <RouterLink :to="`/san-pham?loaiDa=${quizData.idLoaiDa}`" class="sf-btn-outline">
                  Xem tất cả sản phẩm
                </RouterLink>
              </div>
            </div>
            <div v-else class="sf-account-card" style="padding: 24px; text-align: center; background: #fff; border-radius: 8px; border: 1px solid var(--border-color);">
              <p style="margin-bottom: 16px; color: #666;">Bạn chưa thực hiện bài kiểm tra da nào.</p>
              <RouterLink to="/quiz" class="sf-btn-primary">
                Làm Quiz ngay
              </RouterLink>
            </div>
          </template>

          <template v-else-if="activeSection === 'password'">
            <h2 class="sf-account-main__heading">Đổi mật khẩu</h2>
            <p class="sf-account-main__sub">Mật khẩu mới tối thiểu 6 ký tự.</p>

            <div v-if="passwordMsg" class="sf-account-alert sf-account-alert--ok">{{ passwordMsg }}</div>
            <div v-if="passwordError" class="sf-account-alert sf-account-alert--err">{{ passwordError }}</div>

            <form class="sf-account-form" @submit.prevent="savePassword">
              <div class="sf-field">
                <label for="acc-pw-old">Mật khẩu hiện tại</label>
                <div class="sf-password-wrap">
                  <input
                    id="acc-pw-old"
                    v-model="passwordForm.matKhauCu"
                    :type="showOldPw ? 'text' : 'password'"
                    autocomplete="current-password"
                  />
                  <button type="button" class="sf-account-eye" @click="showOldPw = !showOldPw">
                    <Icon :icon="showOldPw ? 'mdi:eye-off' : 'mdi:eye'" width="20" />
                  </button>
                </div>
              </div>
              <div class="sf-field">
                <label for="acc-pw-new">Mật khẩu mới</label>
                <div class="sf-password-wrap">
                  <input
                    id="acc-pw-new"
                    v-model="passwordForm.matKhauMoi"
                    :type="showNewPw ? 'text' : 'password'"
                    autocomplete="new-password"
                  />
                  <button type="button" class="sf-account-eye" @click="showNewPw = !showNewPw">
                    <Icon :icon="showNewPw ? 'mdi:eye-off' : 'mdi:eye'" width="20" />
                  </button>
                </div>
              </div>
              <div class="sf-field">
                <label for="acc-pw-confirm">Nhập lại mật khẩu mới</label>
                <div class="sf-password-wrap">
                  <input
                    id="acc-pw-confirm"
                    v-model="passwordForm.matKhauMoiXacNhan"
                    :type="showConfirmPw ? 'text' : 'password'"
                    autocomplete="new-password"
                  />
                  <button type="button" class="sf-account-eye" @click="showConfirmPw = !showConfirmPw">
                    <Icon :icon="showConfirmPw ? 'mdi:eye-off' : 'mdi:eye'" width="20" />
                  </button>
                </div>
              </div>
              <button type="submit" class="sf-account-submit" :disabled="savingPassword">
                {{ savingPassword ? 'Đang xử lý...' : 'Đổi mật khẩu' }}
              </button>
            </form>
          </template>
        </div>
      </div>
    </div>
  </div>
</template>
