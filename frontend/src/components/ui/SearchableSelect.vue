<script setup>
import { computed, nextTick, onMounted, onUnmounted, ref, watch } from 'vue'
import { matchesSearch } from '@/utils/textMatch'

const props = defineProps({
  modelValue: { type: [Number, String, null], default: null },
  options: { type: Array, default: () => [] },
  placeholder: { type: String, default: 'Chọn...' },
  disabled: { type: Boolean, default: false },
})

const emit = defineEmits(['update:modelValue'])

const rootRef = ref(null)
const open = ref(false)
const searchRaw = ref('')
const highlightedIndex = ref(0)

const selectedOption = computed(() =>
  props.options.find((o) => o.value === props.modelValue) ?? null,
)

const displayLabel = computed(() => selectedOption.value?.label ?? '')

const filteredOptions = computed(() =>
  props.options.filter((o) => matchesSearch(o.label, searchRaw.value)),
)

watch(open, (isOpen) => {
  if (isOpen) {
    searchRaw.value = ''
    highlightedIndex.value = 0
    nextTick(() => {
      rootRef.value?.querySelector('.searchable-select__search')?.focus()
    })
  }
})

watch(filteredOptions, () => {
  highlightedIndex.value = 0
})

function toggleOpen() {
  if (props.disabled) return
  open.value = !open.value
}

function closePanel() {
  open.value = false
}

function selectOption(option) {
  emit('update:modelValue', option.value)
  closePanel()
}

function clearSelection(event) {
  event.stopPropagation()
  emit('update:modelValue', null)
  closePanel()
}

function onKeydown(event) {
  if (!open.value) {
    if (event.key === 'Enter' || event.key === ' ' || event.key === 'ArrowDown') {
      event.preventDefault()
      open.value = true
    }
    return
  }

  if (event.key === 'Escape') {
    event.preventDefault()
    closePanel()
    return
  }

  if (event.key === 'ArrowDown') {
    event.preventDefault()
    if (!filteredOptions.value.length) return
    highlightedIndex.value = (highlightedIndex.value + 1) % filteredOptions.value.length
    scrollHighlightedIntoView()
    return
  }

  if (event.key === 'ArrowUp') {
    event.preventDefault()
    if (!filteredOptions.value.length) return
    highlightedIndex.value =
      (highlightedIndex.value - 1 + filteredOptions.value.length) % filteredOptions.value.length
    scrollHighlightedIntoView()
    return
  }

  if (event.key === 'Enter') {
    event.preventDefault()
    const option = filteredOptions.value[highlightedIndex.value]
    if (option) selectOption(option)
  }
}

function scrollHighlightedIntoView() {
  nextTick(() => {
    rootRef.value
      ?.querySelector('.searchable-select__option--active')
      ?.scrollIntoView({ block: 'nearest' })
  })
}

function onClickOutside(event) {
  if (rootRef.value && !rootRef.value.contains(event.target)) {
    closePanel()
  }
}

onMounted(() => {
  document.addEventListener('click', onClickOutside)
})

onUnmounted(() => {
  document.removeEventListener('click', onClickOutside)
})
</script>

<template>
  <div
    ref="rootRef"
    class="searchable-select"
    :class="{ 'searchable-select--open': open, 'searchable-select--disabled': disabled }"
  >
    <div
      class="searchable-select__trigger admin-input"
      tabindex="0"
      role="combobox"
      :aria-expanded="open"
      @keydown="onKeydown"
    >
      <button
        type="button"
        class="searchable-select__main"
        :disabled="disabled"
        @click="toggleOpen"
      >
        <span
          class="searchable-select__value"
          :class="{ 'searchable-select__value--placeholder': !displayLabel }"
        >
          {{ displayLabel || placeholder }}
        </span>
      </button>
      <span class="searchable-select__actions">
        <button
          v-if="modelValue != null && modelValue !== ''"
          type="button"
          class="searchable-select__clear"
          aria-label="Xóa lựa chọn"
          @click="clearSelection"
        >
          ✕
        </button>
        <button
          type="button"
          class="searchable-select__toggle"
          :disabled="disabled"
          aria-label="Mở danh sách"
          @click="toggleOpen"
        >
          <svg
            class="searchable-select__chevron"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            stroke-width="2"
            aria-hidden="true"
          >
            <path d="M6 9l6 6 6-6" stroke-linecap="round" stroke-linejoin="round" />
          </svg>
        </button>
      </span>
    </div>

    <div v-if="open" class="searchable-select__panel admin-card">
      <div class="searchable-select__search-wrap">
        <svg
          class="searchable-select__search-icon"
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
          class="admin-input searchable-select__search"
          placeholder="Tìm..."
          autocomplete="off"
          @keydown="onKeydown"
        />
      </div>

      <ul class="searchable-select__list" role="listbox">
        <li v-if="!filteredOptions.length" class="searchable-select__empty">
          Không tìm thấy
        </li>
        <li
          v-for="(option, index) in filteredOptions"
          :key="option.value"
          role="option"
          class="searchable-select__option"
          :class="{
            'searchable-select__option--active': index === highlightedIndex,
            'searchable-select__option--selected': option.value === modelValue,
          }"
          @mousedown.prevent="selectOption(option)"
          @mouseenter="highlightedIndex = index"
        >
          {{ option.label }}
        </li>
      </ul>
    </div>
  </div>
</template>

<style scoped>
.searchable-select {
  position: relative;
  width: 100%;
}

.searchable-select--disabled {
  opacity: 0.6;
  pointer-events: none;
}

.searchable-select__trigger {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
  gap: 0.5rem;
  padding: 0 !important;
  overflow: hidden;
  transition: border-color 0.2s;
}

.searchable-select__main {
  flex: 1;
  min-width: 0;
  display: flex;
  align-items: center;
  padding: 0.5rem 0.75rem;
  border: none;
  background: transparent;
  text-align: left;
  cursor: pointer;
  font: inherit;
  color: inherit;
}

.searchable-select__toggle {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 0.25rem;
  border: none;
  background: transparent;
  cursor: pointer;
}

.searchable-select--open .searchable-select__trigger {
  border-color: var(--warm-tan);
}

.searchable-select__value {
  flex: 1;
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.searchable-select__value--placeholder {
  color: var(--admin-muted);
}

.searchable-select__actions {
  display: inline-flex;
  align-items: center;
  gap: 0.25rem;
  flex-shrink: 0;
  padding-right: 0.5rem;
}

.searchable-select__clear {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 1.25rem;
  height: 1.25rem;
  border: none;
  background: transparent;
  color: var(--admin-muted);
  border-radius: 9999px;
  font-size: 0.7rem;
  cursor: pointer;
  transition: color 0.2s, background-color 0.2s;
}

.searchable-select__clear:hover {
  color: var(--ink);
  background: rgba(201, 169, 110, 0.2);
}

.searchable-select__chevron {
  width: 1rem;
  height: 1rem;
  color: var(--admin-muted);
  transition: transform 0.2s;
}

.searchable-select--open .searchable-select__chevron {
  transform: rotate(180deg);
}

.searchable-select__panel {
  position: absolute;
  z-index: 40;
  top: calc(100% + 4px);
  left: 0;
  right: 0;
  padding: 0.5rem;
  box-shadow: 0 8px 24px rgba(30, 21, 16, 0.12);
}

.searchable-select__search-wrap {
  position: relative;
  margin-bottom: 0.5rem;
}

.searchable-select__search-icon {
  position: absolute;
  left: 0.65rem;
  top: 50%;
  transform: translateY(-50%);
  width: 1rem;
  height: 1rem;
  color: var(--admin-muted);
  pointer-events: none;
}

.searchable-select__search {
  padding-left: 2.25rem !important;
  width: 100%;
}

.searchable-select__list {
  list-style: none;
  margin: 0;
  padding: 0;
  max-height: 12rem;
  overflow-y: auto;
}

.searchable-select__option {
  padding: 0.5rem 0.65rem;
  border-radius: 0.375rem;
  font-size: 0.875rem;
  cursor: pointer;
  transition: background-color 0.2s, color 0.2s;
}

.searchable-select__option:hover,
.searchable-select__option--active {
  background: #faf3e3;
  color: var(--ink);
}

.searchable-select__option--selected {
  font-weight: 600;
  color: var(--warm-tan);
}

.searchable-select__empty {
  padding: 0.65rem;
  font-size: 0.8125rem;
  color: var(--admin-muted);
  text-align: center;
}
</style>
