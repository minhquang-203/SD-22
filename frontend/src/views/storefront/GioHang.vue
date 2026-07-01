<script setup>
import { onMounted, ref } from 'vue'
import { RouterLink, useRouter } from 'vue-router'
import { Icon } from '@iconify/vue'
import { confirm } from '@/composables/useConfirm'
import { useAuth } from '@/composables/useAuth'
import { useAuthModal } from '@/composables/useAuthModal'
import {
  maxQtyFor,
  useCart,
  variantLabel,
} from '@/composables/useCart'
import { toast } from '@/composables/useToast'
import { formatDiscountPercent, formatVND } from '@/utils/formatVND'
import { productImageUrl } from '@/utils/productImage'

const router = useRouter()
const { isLoggedIn } = useAuth()
const { openAuthModal } = useAuthModal()
const {
  items,
  selectedCount,
  selectedSubtotal,
  selectedSavings,
  allSelected,
  removeItem,
  removeSelected,
  toggleSelect,
  setSelectAll,
  decreaseQty,
  increaseQty,
  setQty,
  refreshCart,
} = useCart()

const qtyInputs = ref({})

function syncQtyInput(idChiTietSanPham) {
  const fresh = items.value.find((l) => l.idChiTietSanPham === idChiTietSanPham)
  if (fresh) {
    qtyInputs.value[idChiTietSanPham] = fresh.soLuong
  } else {
    delete qtyInputs.value[idChiTietSanPham]
  }
}

onMounted(() => {
  void refreshCart().catch((error) => {
    toast(typeof error === 'string' ? error : 'Không tải được giỏ hàng')
  })
})

async function onDecrease(line) {
  try {
    const result = await decreaseQty(line.idChiTietSanPham)
    if (result === 'min') {
      toast('Đã đạt số lượng tối thiểu')
    }
    syncQtyInput(line.idChiTietSanPham)
  } catch (error) {
    toast(typeof error === 'string' ? error : 'Không cập nhật được giỏ hàng')
  }
}

async function onIncrease(line) {
  try {
    const result = await increaseQty(line.idChiTietSanPham)
    if (result === 'max') {
      const fresh = items.value.find((l) => l.idChiTietSanPham === line.idChiTietSanPham) ?? line
      const stock = fresh.soLuongTon ?? maxQtyFor(fresh)
      toast(`Số lượng đã đạt mức tối đa (còn ${stock} trong kho)`)
    }
    syncQtyInput(line.idChiTietSanPham)
  } catch (error) {
    toast(typeof error === 'string' ? error : 'Không cập nhật được giỏ hàng')
  }
}

async function onQtyInput(line, e) {
  const raw = e.target.value
  qtyInputs.value[line.idChiTietSanPham] = raw
  if (raw === '' || raw === undefined) return
  try {
    const { hitMin, hitMax } = await setQty(line.idChiTietSanPham, raw)
    syncQtyInput(line.idChiTietSanPham)
    if (hitMin) toast('Đã đạt số lượng tối thiểu')
    if (hitMax) {
      const fresh = items.value.find((l) => l.idChiTietSanPham === line.idChiTietSanPham) ?? line
      const stock = fresh.soLuongTon ?? maxQtyFor(fresh)
      toast(`Số lượng đã đạt mức tối đa (còn ${stock} trong kho)`)
    }
  } catch (error) {
    syncQtyInput(line.idChiTietSanPham)
    toast(typeof error === 'string' ? error : 'Không cập nhật được giỏ hàng')
  }
}

function onQtyBlur(line) {
  syncQtyInput(line.idChiTietSanPham)
}

async function confirmRemoveOne(line) {
  const ok = await confirm({
    title: 'Xóa sản phẩm',
    message: `Bạn có chắc muốn xóa "${line.tenSanPham}" khỏi giỏ hàng?`,
    confirmText: 'Xóa',
    danger: true,
  })
  if (!ok) return
  try {
    await removeItem(line.idChiTietSanPham)
  } catch (error) {
    toast(typeof error === 'string' ? error : 'Không xóa được sản phẩm')
  }
}

async function confirmRemoveSelected() {
  const n = items.value.filter((l) => l.selected).length
  if (!n) return
  const ok = await confirm({
    title: 'Xóa sản phẩm đã chọn',
    message: `Xóa ${n} sản phẩm đã chọn khỏi giỏ hàng?`,
    confirmText: 'Xóa',
    danger: true,
  })
  if (!ok) return
  try {
    await removeSelected()
  } catch (error) {
    toast(typeof error === 'string' ? error : 'Không xóa được sản phẩm đã chọn')
  }
}

function toggleAll() {
  setSelectAll(!allSelected.value)
}

function buySelected() {
  if (!selectedCount.value) return
  if (!isLoggedIn.value) {
    openAuthModal('login', '/dat-hang')
    return
  }
  router.push('/dat-hang')
}

function hasDiscount(line) {
  const goc = Number(line.giaGoc)
  const ban = Number(line.giaBan)
  return (goc > 0 && goc > ban) || Boolean(line.phanTramGiam)
}

function saleLabel(line) {
  return formatDiscountPercent(line.phanTramGiam)
}
</script>

<template>
  <div class="sf-cart-page">
    <div class="sf-container">
      <nav class="sf-breadcrumb">
        <RouterLink to="/">Trang chủ</RouterLink>
        <span>/</span>
        <span>Giỏ hàng</span>
      </nav>

      <h1 class="sf-cart-page__title">Giỏ hàng</h1>

      <div v-if="!items.length" class="sf-cart-empty">
        <Icon icon="solar:bag-4-linear" width="64" class="sf-cart-empty__icon" />
        <p>Giỏ hàng của bạn đang trống</p>
        <RouterLink to="/san-pham" class="sf-account-submit sf-cart-empty__btn">Tiếp tục mua sắm</RouterLink>
      </div>

      <div v-else class="sf-cart-layout">
        <div class="sf-cart-main">
          <div class="sf-cart-toolbar">
            <label class="sf-cart-check-all">
              <input type="checkbox" :checked="allSelected" @change="toggleAll" />
              <span>{{ allSelected ? 'Bỏ chọn tất cả' : 'Chọn tất cả' }}</span>
            </label>
            <button
              type="button"
              class="sf-cart-toolbar__delete"
              :disabled="!items.some((l) => l.selected)"
              @click="confirmRemoveSelected"
            >
              Xóa sản phẩm đã chọn
            </button>
          </div>

          <article v-for="line in items" :key="line.idChiTietSanPham" class="sf-cart-line">
            <label class="sf-cart-line__check">
              <input
                type="checkbox"
                :checked="line.selected"
                @change="toggleSelect(line.idChiTietSanPham)"
              />
            </label>

            <RouterLink :to="`/san-pham/${line.idSanPham}`" class="sf-cart-line__img">
              <img :src="productImageUrl(line.anhUrl)" :alt="line.tenSanPham" />
            </RouterLink>

            <div class="sf-cart-line__body">
              <p v-if="line.tenThuongHieu" class="sf-cart-line__brand">{{ line.tenThuongHieu }}</p>
              <RouterLink :to="`/san-pham/${line.idSanPham}`" class="sf-cart-line__name">
                {{ line.tenSanPham }}
              </RouterLink>
              <p v-if="variantLabel(line)" class="sf-cart-line__variant">{{ variantLabel(line) }}</p>
              <div class="sf-cart-line__prices">
                <span v-if="hasDiscount(line) && saleLabel(line)" class="sf-cart-line__sale-badge">{{ saleLabel(line) }}</span>
                <span class="sf-cart-line__price" :class="{ 'sf-cart-line__price--sale': hasDiscount(line) }">
                  {{ formatVND(line.giaBan) }}
                </span>
                <span v-if="hasDiscount(line)" class="sf-cart-line__price-old">{{ formatVND(line.giaGoc) }}</span>
              </div>
            </div>

            <div class="sf-cart-line__actions">
              <div class="sf-qty-control">
                <button type="button" aria-label="Giảm" @click="onDecrease(line)">−</button>
                <input
                  type="number"
                  min="1"
                  :max="maxQtyFor(line)"
                  :value="qtyInputs[line.idChiTietSanPham] ?? line.soLuong"
                  @input="onQtyInput(line, $event)"
                  @blur="onQtyBlur(line)"
                />
                <button type="button" aria-label="Tăng" @click="onIncrease(line)">+</button>
              </div>
              <button
                type="button"
                class="sf-cart-line__remove"
                aria-label="Xóa"
                @click="confirmRemoveOne(line)"
              >
                <Icon icon="solar:trash-bin-trash-linear" width="20" />
              </button>
            </div>
          </article>
        </div>

        <aside class="sf-cart-summary">
          <h2 class="sf-cart-summary__title">Tóm tắt đơn hàng</h2>
          <div class="sf-cart-summary__row">
            <span>Tạm tính ({{ selectedCount }} sản phẩm)</span>
            <strong>{{ formatVND(selectedSubtotal) }}</strong>
          </div>
          <div v-if="selectedSavings > 0" class="sf-cart-summary__row sf-cart-summary__row--save">
            <span>Tiết kiệm</span>
            <strong>−{{ formatVND(selectedSavings) }}</strong>
          </div>
          <button
            type="button"
            class="sf-cart-summary__buy"
            :disabled="!selectedCount"
            @click="buySelected"
          >
            Mua ngay ({{ selectedCount }})
          </button>
          <RouterLink to="/san-pham" class="sf-cart-summary__continue">Tiếp tục mua sắm</RouterLink>
        </aside>
      </div>
    </div>
  </div>
</template>
