<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { RouterLink, useRoute, useRouter } from 'vue-router'
import { Icon } from '@iconify/vue'
import { createOnlineCheckout } from '@/api/onlineCheckout'
import { fetchKhachToi } from '@/api/khachHangApi'
import { calcShippingFee, fetchDistricts, fetchProvinces, fetchWards } from '@/api/shipping'
import { useAuth } from '@/composables/useAuth'
import { useCart, variantLabel } from '@/composables/useCart'
import { toast } from '@/composables/useToast'
import { formatVND } from '@/utils/formatVND'
import { getPhoneValidationError, normalizePhoneDigits } from '@/utils/phone'
import { productImageUrl } from '@/utils/productImage'

const SHIPPING_FEE = 30000
const FREE_SHIPPING_FROM = 500000

const route = useRoute()
const router = useRouter()

const { id: idKhachHang } = useAuth()
const {
  loading: cartLoading,
  selectedItems,
  selectedCount,
  selectedSubtotal,
  selectedSavings,
  refreshCart,
} = useCart()

const profileLoading = ref(false)
const submitting = ref(false)
const selectedPayment = ref('COD')
const addressMode = ref('account')
const orderResult = ref(null)
const paymentCallback = ref(null)

const form = reactive({
  hoTen: '',
  soDienThoai: '',
  email: '',
  provinceId: null,
  districtId: null,
  wardCode: '',
  tinhThanh: '',
  quanHuyen: '',
  phuongXa: '',
  diaChiCuThe: '',
  maPhieuGiamGia: '',
  ghiChu: '',
})

const provinces = ref([])
const districts = ref([])
const wards = ref([])
const addressLoading = reactive({ provinces: false, districts: false, wards: false })

const ghnFee = ref(null)
const feeLoading = ref(false)
const feeNotice = ref('')
const fieldErrors = reactive({ soDienThoai: '' })

const paymentMethods = [
  {
    code: 'COD',
    name: 'Thanh toán khi nhận',
    icon: 'solar:wallet-money-linear',
    description: 'Thanh toán tiền mặt khi nhận hàng. Đơn sẽ được ghi nhận ngay.',
  },
  {
    code: 'VNPAY',
    name: 'VNPay',
    icon: 'solar:card-transfer-linear',
    description: 'Bạn sẽ được chuyển đến cổng VNPay sau khi tạo đơn.',
  },
]

const shippingFee = computed(() => {
  if (selectedSubtotal.value >= FREE_SHIPPING_FROM) return 0
  if (ghnFee.value != null) return ghnFee.value
  return SHIPPING_FEE
})
const estimatedTotal = computed(() => selectedSubtotal.value + shippingFee.value)
const isFreeShipping = computed(() => selectedSubtotal.value >= FREE_SHIPPING_FROM)
const hasSelectedCartItems = computed(() => selectedItems.value.length > 0)

const showCheckoutForm = computed(
  () => !showSuccess.value && !showFailure.value,
)

const showSuccess = computed(
  () =>
    Boolean(orderResult.value && !orderResult.value.paymentUrl) ||
    Boolean(paymentCallback.value?.success),
)

const showFailure = computed(
  () => Boolean(paymentCallback.value && !paymentCallback.value.success),
)

const successOrderCode = computed(
  () => orderResult.value?.maHoaDon || paymentCallback.value?.orderCode || '',
)

const successAmount = computed(() => orderResult.value?.thanhTien ?? null)

const successStatusLabel = computed(
  () => orderResult.value?.trangThaiLabel || (paymentCallback.value?.success ? 'Đã thanh toán' : ''),
)

const checkoutPhase = computed(() => {
  if (showSuccess.value || showFailure.value) return 'complete'
  return 'payment'
})

function compactText(value, maxLength = 250) {
  const text = String(value || '').replace(/\s+/g, ' ').trim()
  return text.length > maxLength ? `${text.slice(0, maxLength - 1)}…` : text
}

function buildAddress() {
  return [form.diaChiCuThe, form.phuongXa, form.quanHuyen, form.tinhThanh]
    .map((part) => String(part || '').trim())
    .filter(Boolean)
    .join(', ')
}

function resetRecipient() {
  addressMode.value = 'new'
  form.hoTen = ''
  form.soDienThoai = ''
  form.email = ''
  fieldErrors.soDienThoai = ''
}

function useAccountRecipient() {
  addressMode.value = 'account'
  void loadProfile()
}

async function loadProfile() {
  profileLoading.value = true
  try {
    const res = await fetchKhachToi()
    form.hoTen = res.data?.hoTen || ''
    form.email = res.data?.email || ''
    form.soDienThoai = res.data?.soDienThoai || ''
  } catch (error) {
    toast(typeof error === 'string' ? error : 'Không tải được thông tin tài khoản')
  } finally {
    profileLoading.value = false
  }
}

async function loadProvinces() {
  addressLoading.provinces = true
  try {
    const res = await fetchProvinces()
    provinces.value = res.data || []
  } catch (error) {
    toast(typeof error === 'string' ? error : 'Không tải được danh sách tỉnh/thành')
  } finally {
    addressLoading.provinces = false
  }
}

async function onProvinceChange() {
  form.districtId = null
  form.wardCode = ''
  districts.value = []
  wards.value = []
  ghnFee.value = null
  feeNotice.value = ''
  const selected = provinces.value.find((p) => p.provinceId === form.provinceId)
  form.tinhThanh = selected?.provinceName || ''
  form.quanHuyen = ''
  form.phuongXa = ''
  if (!form.provinceId) return

  addressLoading.districts = true
  try {
    const res = await fetchDistricts(form.provinceId)
    districts.value = res.data || []
  } catch (error) {
    toast(typeof error === 'string' ? error : 'Không tải được danh sách quận/huyện')
  } finally {
    addressLoading.districts = false
  }
}

async function onDistrictChange() {
  form.wardCode = ''
  wards.value = []
  ghnFee.value = null
  feeNotice.value = ''
  const selected = districts.value.find((d) => d.districtId === form.districtId)
  form.quanHuyen = selected?.districtName || ''
  form.phuongXa = ''
  if (!form.districtId) return

  addressLoading.wards = true
  try {
    const res = await fetchWards(form.districtId)
    wards.value = res.data || []
  } catch (error) {
    toast(typeof error === 'string' ? error : 'Không tải được danh sách phường/xã')
  } finally {
    addressLoading.wards = false
  }
}

async function onWardChange() {
  const selected = wards.value.find((w) => w.wardCode === form.wardCode)
  form.phuongXa = selected?.wardName || ''
  await recalcShippingFee()
}

async function recalcShippingFee() {
  if (!form.districtId || !form.wardCode) {
    ghnFee.value = null
    feeNotice.value = ''
    return
  }
  feeLoading.value = true
  try {
    const res = await calcShippingFee({
      toDistrictId: form.districtId,
      toWardCode: form.wardCode,
      insuranceValue: selectedSubtotal.value,
    })
    ghnFee.value = typeof res.data?.total === 'number' ? res.data.total : null
    feeNotice.value = res.data?.fromGhn ? '' : res.data?.message || ''
  } catch {
    ghnFee.value = null
    feeNotice.value = 'Không tính được phí GHN, tạm áp dụng phí mặc định.'
  } finally {
    feeLoading.value = false
  }
}

function onPhoneInput(event) {
  form.soDienThoai = normalizePhoneDigits(event.target.value)
  fieldErrors.soDienThoai = ''
}

function validatePhoneField() {
  fieldErrors.soDienThoai = getPhoneValidationError(form.soDienThoai)
  return !fieldErrors.soDienThoai
}

function validateCheckout() {
  if (!hasSelectedCartItems.value) return 'Vui lòng chọn sản phẩm trong giỏ hàng để đặt hàng'
  if (!form.hoTen.trim()) return 'Vui lòng nhập họ tên người nhận'
  const phoneError = getPhoneValidationError(form.soDienThoai)
  if (phoneError) {
    fieldErrors.soDienThoai = phoneError
    return phoneError
  }
  if (!form.provinceId) return 'Vui lòng chọn tỉnh / thành phố'
  if (!form.districtId) return 'Vui lòng chọn quận / huyện'
  if (!form.wardCode) return 'Vui lòng chọn phường / xã'
  if (!form.diaChiCuThe.trim()) return 'Vui lòng nhập địa chỉ cụ thể'
  if (!buildAddress()) return 'Vui lòng nhập địa chỉ giao hàng'
  if (!selectedItems.value.every((line) => line.idChiTietGioHang)) {
    return 'Giỏ hàng chưa đồng bộ. Vui lòng tải lại giỏ hàng rồi thử lại'
  }
  return ''
}

function parsePaymentCallback() {
  const { success, orderCode, orderId, message, provider, transactionRef } = route.query
  if (success === undefined || success === null || success === '') return

  paymentCallback.value = {
    success: success === 'true' || success === true,
    orderCode: typeof orderCode === 'string' ? orderCode : '',
    orderId: typeof orderId === 'string' ? orderId : '',
    message: typeof message === 'string' ? message : '',
    provider: typeof provider === 'string' ? provider : '',
    transactionRef: typeof transactionRef === 'string' ? transactionRef : '',
  }

  router.replace({ path: route.path })

  if (paymentCallback.value.success) {
    void refreshCart().catch(() => {})
    toast('Thanh toán thành công')
  } else {
    toast(paymentCallback.value.message || 'Thanh toán thất bại')
  }
}

async function submitCheckout() {
  const message = validateCheckout()
  if (message) {
    toast(message)
    return
  }

  submitting.value = true
  orderResult.value = null
  try {
    const diaChiGiao = compactText(`${form.hoTen.trim()} - ${form.soDienThoai.trim()}, ${buildAddress()}`)
    const noteParts = []
    if (form.email.trim()) noteParts.push(`Email: ${form.email.trim()}`)
    if (form.ghiChu.trim()) noteParts.push(form.ghiChu.trim())

    const res = await createOnlineCheckout({
      idKhachHang: Number(idKhachHang.value),
      idsChiTietGioHang: selectedItems.value.map((line) => line.idChiTietGioHang),
      maPhuongThucThanhToan: selectedPayment.value,
      maPhieuGiamGia: form.maPhieuGiamGia.trim() || null,
      diaChiGiao,
      phiVanChuyen: shippingFee.value,
      ghiChu: compactText(noteParts.join(' | ')),
    })

    orderResult.value = res.data
    await refreshCart()

    if (res.data?.paymentUrl) {
      toast('Tạo đơn thành công. Đang chuyển sang VNPay...')
      window.location.assign(res.data.paymentUrl)
      return
    }

    toast('Đặt hàng thành công')
  } catch (error) {
    toast(typeof error === 'string' ? error : 'Không thể đặt hàng, vui lòng thử lại')
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  parsePaymentCallback()
  if (!paymentCallback.value) {
    void refreshCart().catch((error) => {
      toast(typeof error === 'string' ? error : 'Không tải được giỏ hàng')
    })
  }
  void loadProfile()
  void loadProvinces()
})
</script>

<template>
  <div class="sf-checkout-page">
    <div class="sf-checkout-steps">
      <div class="sf-checkout-step done">
        <span class="sf-checkout-step__dot"><Icon icon="solar:check-linear" width="12" /></span>
        <span>Giỏ hàng</span>
      </div>
      <span class="sf-checkout-step__line" :class="{ done: checkoutPhase === 'complete' }"></span>
      <div
        class="sf-checkout-step"
        :class="{ done: checkoutPhase === 'complete', active: checkoutPhase === 'payment' }"
      >
        <span class="sf-checkout-step__dot">
          <Icon v-if="checkoutPhase === 'complete'" icon="solar:check-linear" width="12" />
          <template v-else>2</template>
        </span>
        <span>Đặt hàng</span>
      </div>
      <span
        class="sf-checkout-step__line"
        :class="{ done: showSuccess, 'done--success': showSuccess, failed: showFailure }"
      ></span>
      <div
        class="sf-checkout-step"
        :class="{
          done: showSuccess,
          'done--success': showSuccess,
          active: showFailure,
          failed: showFailure,
        }"
      >
        <span class="sf-checkout-step__dot">
          <Icon v-if="showSuccess" icon="solar:check-linear" width="12" />
          <Icon v-else-if="showFailure" icon="solar:close-linear" width="12" />
          <template v-else>3</template>
        </span>
        <span>Hoàn tất</span>
      </div>
    </div>

    <div class="sf-container">
      <nav class="sf-breadcrumb">
        <RouterLink to="/">Trang chủ</RouterLink>
        <span>/</span>
        <RouterLink to="/gio-hang">Giỏ hàng</RouterLink>
        <span>/</span>
        <span>Đặt hàng</span>
      </nav>

      <section v-if="showSuccess" class="sf-checkout-success sf-checkout-success--paid">
        <span class="sf-checkout-success__badge">
          <Icon icon="solar:check-circle-bold" width="72" />
        </span>
        <p class="sf-eyebrow">
          {{ paymentCallback?.success ? 'Thanh toán thành công' : 'Đặt hàng thành công' }}
        </p>
        <h1>Đơn {{ successOrderCode }} đã được ghi nhận</h1>
        <p>
          <template v-if="successStatusLabel">
            Trạng thái hiện tại: <strong>{{ successStatusLabel }}</strong>.
          </template>
          <template v-if="successAmount != null">
            Tổng thanh toán <strong>{{ formatVND(successAmount) }}</strong>.
          </template>
          <template v-else-if="paymentCallback?.message">
            {{ paymentCallback.message }}
          </template>
        </p>
        <div class="sf-checkout-success__actions">
          <RouterLink to="/don-hang" class="btn-soleil"><span>Xem đơn hàng</span></RouterLink>
          <RouterLink to="/san-pham" class="sf-checkout-link">Tiếp tục mua sắm</RouterLink>
        </div>
      </section>

      <section v-else-if="showFailure" class="sf-checkout-success sf-checkout-success--failed">
        <span class="sf-checkout-success__badge sf-checkout-success__badge--failed">
          <Icon icon="solar:close-circle-bold" width="72" />
        </span>
        <p class="sf-eyebrow sf-checkout-success__eyebrow--failed">Thanh toán thất bại</p>
        <h1>Không thể hoàn tất thanh toán</h1>
        <p>
          <template v-if="paymentCallback?.orderCode">
            Mã đơn: <strong>{{ paymentCallback.orderCode }}</strong>.
          </template>
          {{ paymentCallback?.message || 'Giao dịch không hợp lệ hoặc đã bị hủy. Vui lòng thử lại.' }}
        </p>
        <div class="sf-checkout-success__actions">
          <RouterLink to="/don-hang" class="btn-soleil"><span>Xem đơn hàng</span></RouterLink>
          <RouterLink to="/gio-hang" class="sf-checkout-link">Quay lại giỏ hàng</RouterLink>
        </div>
      </section>

      <section v-else-if="!cartLoading && !hasSelectedCartItems" class="sf-cart-empty sf-checkout-empty">
        <Icon icon="solar:bag-4-linear" width="64" class="sf-cart-empty__icon" />
        <p>Bạn chưa chọn sản phẩm nào để đặt hàng.</p>
        <RouterLink to="/gio-hang" class="sf-account-submit sf-cart-empty__btn">
          Quay lại giỏ hàng
        </RouterLink>
      </section>

      <form v-else-if="showCheckoutForm" class="sf-checkout-layout" @submit.prevent="submitCheckout">
        <div class="sf-checkout-main">
          <section class="sf-checkout-card">
            <h2 class="sf-checkout-card__title">
              <span><Icon icon="solar:clipboard-list-linear" width="18" /></span>
              Thông tin người nhận
            </h2>

            <div class="sf-checkout-tabs">
              <button
                type="button"
                class="sf-checkout-tab"
                :class="{ active: addressMode === 'account' }"
                :disabled="profileLoading"
                @click="useAccountRecipient"
              >
                Thông tin tài khoản
              </button>
              <button
                type="button"
                class="sf-checkout-tab"
                :class="{ active: addressMode === 'new' }"
                @click="resetRecipient"
              >
                + Người nhận mới
              </button>
            </div>

            <div class="sf-checkout-form-row">
              <div class="sf-checkout-field">
                <label for="checkout-name">Họ và tên</label>
                <input id="checkout-name" v-model="form.hoTen" type="text" autocomplete="name" />
              </div>
              <div class="sf-checkout-field">
                <label for="checkout-phone">Số điện thoại</label>
                <input
                  id="checkout-phone"
                  :value="form.soDienThoai"
                  type="tel"
                  autocomplete="tel"
                  inputmode="numeric"
                  maxlength="10"
                  pattern="0[35789][0-9]{8}"
                  placeholder="0xxxxxxxxx"
                  :class="{ 'is-invalid': fieldErrors.soDienThoai }"
                  @input="onPhoneInput"
                  @blur="validatePhoneField"
                />
                <span v-if="fieldErrors.soDienThoai" class="sf-field-error">{{ fieldErrors.soDienThoai }}</span>
              </div>
            </div>

            <div class="sf-checkout-field">
              <label for="checkout-email">Email</label>
              <input id="checkout-email" v-model="form.email" type="email" autocomplete="email" />
            </div>

            <div class="sf-checkout-form-row">
              <div class="sf-checkout-field">
                <label for="checkout-city">Tỉnh / Thành phố</label>
                <select
                  id="checkout-city"
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
              <div class="sf-checkout-field">
                <label for="checkout-district">Quận / Huyện</label>
                <select
                  id="checkout-district"
                  v-model="form.districtId"
                  :disabled="!form.provinceId || addressLoading.districts"
                  @change="onDistrictChange"
                >
                  <option :value="null">
                    {{ addressLoading.districts ? 'Đang tải...' : 'Chọn quận / huyện' }}
                  </option>
                  <option v-for="d in districts" :key="d.districtId" :value="d.districtId">
                    {{ d.districtName }}
                  </option>
                </select>
              </div>
            </div>

            <div class="sf-checkout-form-row">
              <div class="sf-checkout-field">
                <label for="checkout-ward">Phường / Xã</label>
                <select
                  id="checkout-ward"
                  v-model="form.wardCode"
                  :disabled="!form.districtId || addressLoading.wards"
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
              <div class="sf-checkout-field">
                <label for="checkout-address">Địa chỉ cụ thể</label>
                <input id="checkout-address" v-model="form.diaChiCuThe" type="text" autocomplete="street-address" placeholder="Số nhà, tên đường..." />
              </div>
            </div>

            <div class="sf-checkout-field sf-checkout-field--last">
              <label for="checkout-note">Ghi chú</label>
              <textarea
                id="checkout-note"
                v-model="form.ghiChu"
                placeholder="Giao giờ hành chính, gọi trước khi giao..."
              ></textarea>
            </div>
          </section>

          <section class="sf-checkout-card">
            <h2 class="sf-checkout-card__title">
              <span><Icon icon="solar:ticket-sale-linear" width="18" /></span>
              Mã giảm giá
            </h2>
            <label class="sf-checkout-voucher">
              <Icon icon="solar:ticket-linear" width="18" />
              <input
                v-model.trim="form.maPhieuGiamGia"
                type="text"
                placeholder="Nhập mã voucher nếu có"
                autocomplete="off"
              />
            </label>
            <p class="sf-checkout-hint">Mã giảm giá sẽ được hệ thống kiểm tra khi đặt hàng.</p>
          </section>

          <section class="sf-checkout-card">
            <h2 class="sf-checkout-card__title">
              <span><Icon icon="solar:card-linear" width="18" /></span>
              Phương thức thanh toán
            </h2>
            <div class="sf-checkout-payments">
              <button
                v-for="method in paymentMethods"
                :key="method.code"
                type="button"
                class="sf-checkout-pay-card"
                :class="{ active: selectedPayment === method.code }"
                @click="selectedPayment = method.code"
              >
                <span class="sf-checkout-pay-card__dot"></span>
                <span class="sf-checkout-pay-card__icon">
                  <Icon :icon="method.icon" width="24" />
                </span>
                <span class="sf-checkout-pay-card__name">{{ method.name }}</span>
              </button>
            </div>
            <div class="sf-checkout-payment-detail">
              <Icon :icon="paymentMethods.find((m) => m.code === selectedPayment)?.icon" width="28" />
              <p>{{ paymentMethods.find((m) => m.code === selectedPayment)?.description }}</p>
            </div>
          </section>
        </div>

        <aside class="sf-checkout-summary">
          <h2>Đơn hàng của bạn</h2>

          <div class="sf-checkout-items">
            <article
              v-for="line in selectedItems"
              :key="line.idChiTietGioHang || line.idChiTietSanPham"
              class="sf-checkout-item"
            >
              <div class="sf-checkout-item__img">
                <img :src="productImageUrl(line.anhUrl)" :alt="line.tenSanPham" />
              </div>
              <div class="sf-checkout-item__info">
                <h3>{{ line.tenSanPham }}</h3>
                <p v-if="variantLabel(line)">{{ variantLabel(line) }}</p>
              </div>
              <div class="sf-checkout-item__right">
                <strong>{{ formatVND((line.giaBan || 0) * (line.soLuong || 0)) }}</strong>
                <span>x{{ line.soLuong }}</span>
              </div>
            </article>
          </div>

          <div class="sf-checkout-summary__row">
            <span>Tạm tính ({{ selectedCount }} sản phẩm)</span>
            <strong>{{ formatVND(selectedSubtotal) }}</strong>
          </div>
          <div v-if="selectedSavings > 0" class="sf-checkout-summary__row sf-checkout-summary__row--save">
            <span>Tiết kiệm từ giá gốc</span>
            <strong>-{{ formatVND(selectedSavings) }}</strong>
          </div>
          <div class="sf-checkout-summary__row">
            <span>Phí vận chuyển (GHN)</span>
            <strong v-if="feeLoading">Đang tính...</strong>
            <strong v-else>{{ shippingFee ? formatVND(shippingFee) : 'Miễn phí' }}</strong>
          </div>
          <p v-if="isFreeShipping" class="sf-checkout-hint">
            Đơn từ {{ formatVND(FREE_SHIPPING_FROM) }} được miễn phí vận chuyển.
          </p>
          <p v-else-if="feeNotice" class="sf-checkout-hint">{{ feeNotice }}</p>
          <div v-if="form.maPhieuGiamGia" class="sf-checkout-summary__row">
            <span>Mã giảm giá</span>
            <strong>{{ form.maPhieuGiamGia }}</strong>
          </div>

          <hr />

          <div class="sf-checkout-summary__total">
            <span>Tổng tạm tính</span>
            <strong>{{ formatVND(estimatedTotal) }}</strong>
          </div>

          <button type="submit" class="sf-checkout-submit" :disabled="submitting || cartLoading">
            <Icon icon="solar:shield-check-linear" width="18" />
            {{ submitting ? 'Đang xử lý...' : 'Đặt hàng ngay' }}
          </button>
          <p class="sf-checkout-safety">
            <Icon icon="solar:lock-keyhole-linear" width="13" />
            Thông tin được bảo vệ trong phiên thanh toán
          </p>
        </aside>
      </form>
    </div>
  </div>
</template>

<style scoped>
  @import '@/styles/checkoutCss.css'
</style>
