<script setup>
import { Icon } from '@iconify/vue'
import { RouterLink, useRouter } from 'vue-router'
import { productImageUrl } from '@/utils/productImage'
import { formatPriceRange } from '@/utils/formatVND'

const props = defineProps({
  product: { type: Object, required: true },
  showQuickAdd: { type: Boolean, default: true },
  layout: { type: String, default: 'grid' },
})

const router = useRouter()

function quickAdd(e) {
  e.preventDefault()
  e.stopPropagation()
  router.push(`/san-pham/${props.product.id}`)
}

function spfLabel() {
  const parts = []
  if (props.product.chiSoSpf) parts.push(`SPF ${props.product.chiSoSpf}`)
  if (props.product.chiSoPa) parts.push(`PA ${props.product.chiSoPa}`)
  return parts.join(' · ')
}
</script>

<template>
  <article class="sf-product-card" :class="{ 'sf-product-card--list': layout === 'list' }">
    <RouterLink :to="`/san-pham/${product.id}`" class="sf-product-card__link">
      <div class="sf-product-card__img-wrap">
        <img
          :src="productImageUrl(product.anhChinhUrl)"
          :alt="product.ten"
          class="sf-product-card__img"
          loading="lazy"
        />
        <span v-if="product.noiBat" class="sf-product-card__badge sf-product-card__badge--hot">Nổi bật</span>
        <span v-if="spfLabel()" class="sf-product-card__badge sf-product-card__badge--spf">{{ spfLabel() }}</span>
      </div>
      <div class="sf-product-card__body">
        <p v-if="product.tenThuongHieu" class="sf-product-card__brand">{{ product.tenThuongHieu }}</p>
        <h3 class="sf-product-card__name">{{ product.ten }}</h3>
        <p class="sf-product-card__price">{{ formatPriceRange(product.giaMin, product.giaMax) }}</p>
      </div>
    </RouterLink>
    <button
      v-if="showQuickAdd && layout === 'grid'"
      type="button"
      class="sf-product-card__quick-add"
      title="Xem chi tiết để chọn biến thể"
      @click="quickAdd"
    >
      <Icon icon="solar:bag-3-linear" width="18" />
    </button>
  </article>
</template>
