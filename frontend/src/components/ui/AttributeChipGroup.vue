<script setup>
import { computed, onUnmounted, ref, watch } from 'vue'
import { matchesSearch } from '@/utils/textMatch'

const props = defineProps({
  title: { type: String, required: true },
  options: { type: Array, default: () => [] },
  modelValue: { type: Array, default: () => [] },
})

const emit = defineEmits(['update:modelValue'])

const searchRaw = ref('')
const searchDebounced = ref('')
let debounceTimer = null

watch(searchRaw, (val) => {
  clearTimeout(debounceTimer)
  debounceTimer = setTimeout(() => {
    searchDebounced.value = val
  }, 200)
})

onUnmounted(() => {
  clearTimeout(debounceTimer)
})

const selectedIds = computed(() => new Set(props.modelValue || []))

const selectedOptions = computed(() =>
  props.options.filter((item) => selectedIds.value.has(item.id)),
)

const unselectedOptions = computed(() =>
  props.options.filter((item) => !selectedIds.value.has(item.id)),
)

const filteredResults = computed(() =>
  unselectedOptions.value.filter((item) => matchesSearch(item.ten, searchDebounced.value)),
)

const showNoResults = computed(
  () => searchDebounced.value.trim() !== '' && filteredResults.value.length === 0,
)

function selectItem(id) {
  if (selectedIds.value.has(id)) return
  emit('update:modelValue', [...(props.modelValue || []), id])
}

function removeItem(id) {
  emit(
    'update:modelValue',
    (props.modelValue || []).filter((x) => x !== id),
  )
}
</script>

<template>
  <div class="attribute-chip-group">
    <div class="attribute-chip-group__header">
      <div class="attribute-chip-group__title-row">
        <h4 class="attribute-chip-group__title">{{ title }}</h4>
        <span class="attribute-chip-group__count">đã chọn {{ modelValue.length }}</span>
      </div>
      <div class="attribute-chip-group__search-wrap">
        <svg
          class="attribute-chip-group__search-icon"
          viewBox="0 0 24 24"
          fill="none"
          stroke="currentColor"
          stroke-width="2"
          aria-hidden="true"
        >
          <circle cx="11" cy="11" r="7" />
          <path d="M20 20l-3-3" stroke-linecap="round" />
        </svg>
        <input
          v-model="searchRaw"
          type="text"
          class="admin-input attribute-chip-group__search"
          placeholder="Tìm..."
          autocomplete="off"
        />
      </div>
    </div>

    <div v-if="selectedOptions.length" class="attribute-chip-group__section">
      <p class="attribute-chip-group__section-label">Đã chọn</p>
      <div class="attribute-chip-group__chips">
        <span
          v-for="item in selectedOptions"
          :key="`sel-${item.id}`"
          class="attribute-chip attribute-chip--selected"
        >
          <span>{{ item.ten }}</span>
          <button
            type="button"
            class="attribute-chip__remove"
            aria-label="Bỏ chọn"
            @click="removeItem(item.id)"
          >
            ✕
          </button>
        </span>
      </div>
    </div>

    <div class="attribute-chip-group__section">
      <p class="attribute-chip-group__section-label">Kết quả</p>
      <p v-if="showNoResults" class="attribute-chip-group__empty">Không tìm thấy</p>
      <div v-else class="attribute-chip-group__chips">
        <button
          v-for="item in filteredResults"
          :key="`res-${item.id}`"
          type="button"
          class="attribute-chip attribute-chip--option admin-btn admin-btn-default"
          @click="selectItem(item.id)"
        >
          {{ item.ten }}
        </button>
        <p
          v-if="!filteredResults.length && !searchDebounced.trim()"
          class="attribute-chip-group__empty attribute-chip-group__empty--muted"
        >
          Tất cả mục đã được chọn
        </p>
      </div>
    </div>
  </div>
</template>

<style scoped>
.attribute-chip-group {
  padding-bottom: 0.25rem;
}

.attribute-chip-group__header {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
  margin-bottom: 0.75rem;
}

@media (min-width: 640px) {
  .attribute-chip-group__header {
    flex-direction: row;
    align-items: center;
    justify-content: space-between;
  }
}

.attribute-chip-group__title-row {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  flex-wrap: wrap;
}

.attribute-chip-group__title {
  font-size: 0.875rem;
  font-weight: 600;
  margin: 0;
}

.attribute-chip-group__count {
  font-size: 0.75rem;
  color: var(--admin-muted);
}

.attribute-chip-group__search-wrap {
  position: relative;
  width: 100%;
}

@media (min-width: 640px) {
  .attribute-chip-group__search-wrap {
    max-width: 14rem;
    margin-left: auto;
  }
}

.attribute-chip-group__search-icon {
  position: absolute;
  left: 0.65rem;
  top: 50%;
  transform: translateY(-50%);
  width: 1rem;
  height: 1rem;
  color: var(--admin-muted);
  pointer-events: none;
}

.attribute-chip-group__search {
  padding-left: 2.25rem !important;
  width: 100%;
}

.attribute-chip-group__section {
  margin-bottom: 0.75rem;
}

.attribute-chip-group__section-label {
  font-size: 0.75rem;
  font-weight: 600;
  color: var(--admin-muted);
  margin: 0 0 0.5rem;
  text-transform: uppercase;
  letter-spacing: 0.03em;
}

.attribute-chip-group__chips {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
}

.attribute-chip {
  display: inline-flex;
  align-items: center;
  gap: 0.35rem;
  padding: 0.35rem 0.65rem;
  border-radius: 9999px;
  font-size: 0.8125rem;
  border: 1px solid var(--admin-border);
  transition: background-color 0.2s, border-color 0.2s, color 0.2s;
}

.attribute-chip--selected {
  background: #faf3e3;
  border-color: var(--warm-tan);
  color: var(--ink);
}

.attribute-chip--option {
  border-radius: 9999px !important;
  padding: 0.35rem 0.75rem !important;
  font-size: 0.8125rem !important;
}

.attribute-chip--option:hover {
  border-color: var(--warm-tan);
  color: var(--warm-tan);
}

.attribute-chip__remove {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 1.125rem;
  height: 1.125rem;
  border-radius: 9999px;
  font-size: 0.65rem;
  line-height: 1;
  color: var(--admin-muted);
  background: transparent;
  border: none;
  cursor: pointer;
  transition: color 0.2s, background-color 0.2s;
}

.attribute-chip__remove:hover {
  color: var(--ink);
  background: rgba(201, 169, 110, 0.25);
}

.attribute-chip-group__empty {
  font-size: 0.8125rem;
  color: var(--admin-muted);
  margin: 0;
}

.attribute-chip-group__empty--muted {
  font-style: italic;
}
</style>
