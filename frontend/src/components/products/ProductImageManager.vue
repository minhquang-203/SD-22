<script setup>
import { ref } from 'vue'
import { resolveProductImageUrl } from '@/utils/productForm'

const images = defineModel({ type: Array, required: true })
const fileInputRef = ref(null)

function previewSrc(img) {
  if (img.previewUrl) return img.previewUrl
  return resolveProductImageUrl(img.url) || ''
}

function openFilePicker() {
  fileInputRef.value?.click()
}

function onFilesSelected(event) {
  const selected = Array.from(event.target.files || [])
  selected.forEach((file) => {
    images.value.push({
      url: '',
      file,
      previewUrl: URL.createObjectURL(file),
      laAnhChinh: images.value.length === 0,
      thuTu: images.value.length,
    })
  })
  event.target.value = ''
}

function removeImage(index) {
  const img = images.value[index]
  if (img?.previewUrl?.startsWith('blob:')) {
    URL.revokeObjectURL(img.previewUrl)
  }
  images.value.splice(index, 1)
}

function setMain(index) {
  images.value.forEach((img, i) => {
    img.laAnhChinh = i === index
  })
}
</script>

<template>
  <div>
    <input
      ref="fileInputRef"
      type="file"
      accept="image/jpeg,image/png,image/webp,image/gif"
      multiple
      class="hidden"
      @change="onFilesSelected"
    />

    <div class="flex items-center justify-between mb-3">
      <h4 class="font-semibold text-sm">Ảnh sản phẩm</h4>
      <button type="button" class="admin-btn admin-btn-success" @click="openFilePicker">
        Tải ảnh lên
      </button>
    </div>

    <div
      v-if="images.length === 0"
      class="text-sm text-[var(--admin-muted)] py-8 text-center border rounded-lg"
    >
      Chưa có ảnh — nhấn <strong>Tải ảnh lên</strong> để chọn ảnh từ máy tính
    </div>

    <div class="space-y-3">
      <div
        v-for="(img, index) in images"
        :key="index"
        class="grid grid-cols-1 md:grid-cols-[220px_1fr_auto] gap-4 items-start border rounded-lg p-4"
      >
        <img
          v-if="previewSrc(img)"
          :src="previewSrc(img)"
          alt="preview"
          class="product-form-preview"
        />
        <div v-else class="product-form-preview--empty">
          Chưa có preview
        </div>

        <div class="space-y-2">
          <div class="text-sm text-[var(--admin-muted)]">
            {{ img.file?.name || (img.url ? 'Ảnh đã lưu trên server' : 'Ảnh mới') }}
          </div>
          <div class="flex items-center gap-4 flex-wrap">
            <label class="flex items-center gap-2 text-sm">
              <input
                type="radio"
                name="main-image"
                :checked="img.laAnhChinh"
                @change="setMain(index)"
              />
              Ảnh chính
            </label>
            <div class="flex items-center gap-2">
              <span class="text-sm">Thứ tự</span>
              <input v-model.number="img.thuTu" type="number" class="admin-input !w-20" min="0" />
            </div>
          </div>
        </div>

        <button type="button" class="admin-btn admin-btn-danger" @click="removeImage(index)">
          Xóa
        </button>
      </div>
    </div>

    <p class="text-xs text-[var(--admin-muted)] mt-2">
      Hỗ trợ JPG, PNG, WEBP, GIF. Ảnh sẽ được upload khi bạn lưu sản phẩm.
    </p>
  </div>
</template>
