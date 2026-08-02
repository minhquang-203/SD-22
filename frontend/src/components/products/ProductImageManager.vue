<script setup>
import { computed, ref } from 'vue'
import { resolveProductImageUrl } from '@/utils/productForm'

const images = defineModel({ type: Array, required: true })

const props = defineProps({
  /** Danh sách màu có thể gán ảnh — thường lấy từ biến thể đang có */
  mauOptions: { type: Array, default: () => [] },
})

const fileInputRef = ref(null)
const dragOver = ref(false)

const colorSelectOptions = computed(() => {
  const map = new Map()
  ;(props.mauOptions || []).forEach((m) => {
    if (m?.id != null && !map.has(m.id)) {
      map.set(m.id, { id: m.id, ten: m.ten || `Màu #${m.id}` })
    }
  })
  return [...map.values()]
})

function previewSrc(img) {
  if (img.previewUrl) return img.previewUrl
  return resolveProductImageUrl(img.url) || ''
}

function openFilePicker() {
  fileInputRef.value?.click()
}

function syncThuTu() {
  images.value.forEach((img, i) => {
    img.thuTu = i
  })
  if (images.value.length && !images.value.some((img) => img.laAnhChinh)) {
    images.value[0].laAnhChinh = true
  }
}

function addFiles(fileList) {
  const selected = Array.from(fileList || []).filter((f) => f?.type?.startsWith('image/'))
  selected.forEach((file) => {
    images.value.push({
      url: '',
      file,
      previewUrl: URL.createObjectURL(file),
      laAnhChinh: images.value.length === 0,
      thuTu: images.value.length,
      idMauSac: null,
    })
  })
  syncThuTu()
}

function onFilesSelected(event) {
  addFiles(event.target.files)
  event.target.value = ''
}

function onDrop(event) {
  event.preventDefault()
  dragOver.value = false
  addFiles(event.dataTransfer?.files)
}

function removeImage(index) {
  const img = images.value[index]
  if (img?.previewUrl?.startsWith('blob:')) {
    URL.revokeObjectURL(img.previewUrl)
  }
  const wasMain = !!img?.laAnhChinh
  images.value.splice(index, 1)
  if (wasMain && images.value.length) {
    images.value[0].laAnhChinh = true
  }
  syncThuTu()
}

function setMain(index) {
  images.value.forEach((img, i) => {
    img.laAnhChinh = i === index
  })
}

function moveImage(index, delta) {
  const next = index + delta
  if (next < 0 || next >= images.value.length) return
  const list = [...images.value]
  const [item] = list.splice(index, 1)
  list.splice(next, 0, item)
  images.value = list
  syncThuTu()
}

function onColorChange(img, event) {
  const raw = event.target.value
  img.idMauSac = raw === '' || raw === 'null' ? null : Number(raw)
}
</script>

<template>
  <div class="pim">
    <input
      ref="fileInputRef"
      type="file"
      accept="image/jpeg,image/png,image/webp,image/gif"
      multiple
      class="hidden"
      @change="onFilesSelected"
    />

    <div class="pim__head">
      <h4 class="pim__title">Ảnh sản phẩm</h4>
      <button type="button" class="admin-btn admin-btn-success" @click="openFilePicker">
        Tải ảnh lên
      </button>
    </div>

    <div
      class="pim__drop"
      :class="{ 'pim__drop--active': dragOver }"
      @dragover.prevent="dragOver = true"
      @dragleave.prevent="dragOver = false"
      @drop="onDrop"
      @click="openFilePicker"
    >
      <p>
        Kéo thả ảnh vào đây hoặc <strong>bấm để chọn</strong>
      </p>
      <span>JPG, PNG, WEBP, GIF — có thể chọn nhiều ảnh</span>
    </div>

    <div v-if="images.length === 0" class="pim__empty">
      Chưa có ảnh — thêm ít nhất một ảnh (ảnh đầu sẽ là ảnh chính nếu chưa chọn).
    </div>

    <div v-else class="pim__grid">
      <div
        v-for="(img, index) in images"
        :key="img.previewUrl || img.url || index"
        class="pim__card"
        :class="{ 'pim__card--main': img.laAnhChinh }"
      >
        <div class="pim__preview">
          <img v-if="previewSrc(img)" :src="previewSrc(img)" alt="preview" />
          <span v-else>Không có preview</span>
          <span v-if="img.laAnhChinh" class="pim__badge">Ảnh chính</span>
        </div>

        <div class="pim__meta">
          <p class="pim__name">
            {{ img.file?.name || (img.url ? 'Ảnh đã lưu' : 'Ảnh mới') }}
          </p>

          <label class="pim__field">
            <span>Màu sắc</span>
            <select
              class="admin-select pim__color-select"
              :value="img.idMauSac ?? ''"
              @change="onColorChange(img, $event)"
            >
              <option value="">Dùng chung (mọi màu)</option>
              <option
                v-for="m in colorSelectOptions"
                :key="m.id"
                :value="m.id"
              >
                {{ m.ten }}
              </option>
            </select>
          </label>

          <label class="pim__radio">
            <input
              type="radio"
              name="main-image"
              :checked="img.laAnhChinh"
              @change="setMain(index)"
            />
            Đặt làm ảnh chính
          </label>

          <div class="pim__actions">
            <button
              type="button"
              class="admin-icon-btn"
              title="Lên"
              :disabled="index === 0"
              @click="moveImage(index, -1)"
            >
              ↑
            </button>
            <button
              type="button"
              class="admin-icon-btn"
              title="Xuống"
              :disabled="index === images.length - 1"
              @click="moveImage(index, 1)"
            >
              ↓
            </button>
            <button
              type="button"
              class="admin-btn admin-btn-danger"
              @click="removeImage(index)"
            >
              Xóa
            </button>
          </div>
        </div>
      </div>
    </div>

    <p class="pim__hint">
      Mỗi ảnh có thể gắn 1 màu (hiện theo biến thể) hoặc «Dùng chung».
      Ảnh chính dùng làm ảnh đại diện ở danh sách sản phẩm.
    </p>
  </div>
</template>

<style scoped>
.pim__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 12px;
}

.pim__title {
  margin: 0;
  font-size: 14px;
  font-weight: 600;
  color: var(--admin-text, var(--ink, #1a1814));
}

.pim__drop {
  border: 1.5px dashed var(--admin-border, #e8dcc8);
  border-radius: 12px;
  background: rgba(201, 169, 110, 0.06);
  padding: 22px 16px;
  text-align: center;
  cursor: pointer;
  transition: border-color 0.2s ease, background 0.2s ease;
  margin-bottom: 14px;
}

.pim__drop:hover,
.pim__drop--active {
  border-color: var(--admin-primary, #c9a96e);
  background: rgba(201, 169, 110, 0.12);
}

.pim__drop p {
  margin: 0 0 4px;
  font-size: 14px;
  color: var(--admin-text, #1a1814);
}

.pim__drop span {
  font-size: 12px;
  color: var(--admin-muted, #8a7b6a);
}

.pim__empty {
  text-align: center;
  padding: 28px 12px;
  font-size: 13px;
  color: var(--admin-muted, #8a7b6a);
  border: 1px solid var(--admin-border, #e8dcc8);
  border-radius: 12px;
}

.pim__grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 12px;
}

.pim__card {
  border: 1px solid var(--admin-border, #e8dcc8);
  border-radius: 12px;
  overflow: hidden;
  background: var(--admin-card, #fff);
  display: flex;
  flex-direction: column;
}

.pim__card--main {
  border-color: var(--admin-primary, #c9a96e);
  box-shadow: 0 0 0 1px rgba(201, 169, 110, 0.35);
}

.pim__preview {
  position: relative;
  aspect-ratio: 1;
  background: var(--admin-bg, #f9f5f0);
  display: flex;
  align-items: center;
  justify-content: center;
}

.pim__preview img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.pim__preview span:not(.pim__badge) {
  font-size: 12px;
  color: var(--admin-muted, #8a7b6a);
}

.pim__badge {
  position: absolute;
  top: 8px;
  left: 8px;
  font-size: 11px;
  font-weight: 700;
  padding: 3px 8px;
  border-radius: 999px;
  background: var(--sage, #7a8c6e);
  color: #fff;
}

.pim__meta {
  padding: 10px 12px 12px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.pim__name {
  margin: 0;
  font-size: 12px;
  color: var(--admin-muted, #8a7b6a);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.pim__field {
  display: flex;
  flex-direction: column;
  gap: 4px;
  font-size: 12px;
  color: var(--admin-muted, #8a7b6a);
}

.pim__color-select {
  font-size: 13px;
  padding: 6px 8px;
}

.pim__radio {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: var(--admin-text, #1a1814);
  cursor: pointer;
}

.pim__actions {
  display: flex;
  align-items: center;
  gap: 6px;
  flex-wrap: wrap;
}

.pim__hint {
  margin: 12px 0 0;
  font-size: 12px;
  color: var(--admin-muted, #8a7b6a);
}
</style>
