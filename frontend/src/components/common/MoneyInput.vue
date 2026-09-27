<script setup>
import { ref, watch, nextTick } from 'vue'

const props = defineProps({
  modelValue: { type: [Number, String, null], default: null },
  min: { type: Number, default: undefined },
  max: { type: Number, default: undefined },
  placeholder: { type: String, default: '0' },
  disabled: { type: Boolean, default: false },
  /** Đánh dấu lỗi validate (thêm class is-invalid / --error) */
  error: { type: Boolean, default: false },
  inputClass: { type: [String, Object, Array], default: '' },
  id: { type: String, default: undefined },
})

const emit = defineEmits(['update:modelValue', 'input', 'blur', 'focus'])

const inputEl = ref(null)
const display = ref(formatDots(props.modelValue))

function digitsOnly(raw) {
  return String(raw ?? '').replace(/\D/g, '')
}

/** Chuỗi bất kỳ (có chấm/phẩy/khoảng trắng/đ) → Number hoặc null nếu rỗng */
function parseMoney(raw) {
  const digits = digitsOnly(raw)
  if (!digits) return null
  // Bỏ số 0 đứng đầu thừa nhưng giữ "0"
  const normalized = digits.replace(/^0+(?=\d)/, '')
  const n = Number(normalized)
  if (!Number.isFinite(n)) return null
  return Math.trunc(n)
}

function formatDots(value) {
  if (value == null || value === '') return ''
  const n = Number(value)
  if (!Number.isFinite(n)) return ''
  const abs = Math.trunc(Math.abs(n))
  // 0 để trống: placeholder hiện "0", gõ số mới không bị dính số cũ, xóa hết thì không nhảy lại.
  if (abs === 0) return ''
  return String(abs).replace(/\B(?=(\d{3})+(?!\d))/g, '.')
}

function countDigitsBefore(str, caret) {
  let count = 0
  const end = Math.max(0, Math.min(caret, str.length))
  for (let i = 0; i < end; i++) {
    if (/\d/.test(str[i])) count++
  }
  return count
}

function caretFromDigitCount(formatted, digitCount) {
  if (digitCount <= 0) return 0
  let seen = 0
  for (let i = 0; i < formatted.length; i++) {
    if (/\d/.test(formatted[i])) {
      seen++
      if (seen >= digitCount) return i + 1
    }
  }
  return formatted.length
}

function syncFromModel(v) {
  const nextNum = v == null || v === '' ? null : Number(v)
  const current = parseMoney(display.value)
  const same =
    (current == null && (nextNum == null || Number.isNaN(nextNum))) ||
    (current != null && nextNum != null && current === Math.trunc(nextNum))
  if (!same) {
    display.value = formatDots(nextNum)
  }
}

watch(
  () => props.modelValue,
  (v) => {
    syncFromModel(v)
  },
)

function onInput(e) {
  const el = e.target
  const raw = el.value
  const caret = el.selectionStart ?? raw.length
  const digitsBefore = countDigitsBefore(raw, caret)

  let num = parseMoney(raw)
  if (num != null && props.max != null && Number.isFinite(props.max) && num > props.max) {
    num = Math.trunc(props.max)
  }
  // Chặn số âm (không cho dấu -)
  if (num != null && num < 0) num = 0
  if (num != null && props.min != null && props.min >= 0 && num < 0) num = props.min

  const formatted = formatDots(num)
  display.value = formatted

  emit('update:modelValue', num)
  emit('input', num)

  nextTick(() => {
    const node = inputEl.value
    if (!node) return
    const pos = caretFromDigitCount(formatted, digitsBefore)
    try {
      node.setSelectionRange(pos, pos)
    } catch {
      /* ignore */
    }
  })
}

function onBlur(e) {
  // Chuẩn hoá lại hiển thị sau blur
  display.value = formatDots(props.modelValue)
  emit('blur', e)
}

function onFocus(e) {
  emit('focus', e)
}

function onKeydown(e) {
  // Cho phép điều khiển; chặn chữ cái (trừ Ctrl/Meta shortcuts)
  if (e.ctrlKey || e.metaKey || e.altKey) return
  const allow = [
    'Backspace',
    'Delete',
    'Tab',
    'Enter',
    'Escape',
    'ArrowLeft',
    'ArrowRight',
    'ArrowUp',
    'ArrowDown',
    'Home',
    'End',
  ]
  if (allow.includes(e.key)) return
  if (/^\d$/.test(e.key)) return
  // Chấm ngăn cách có thể gõ nhưng sẽ bị strip — vẫn cho để paste UX; chặn chữ
  if (e.key.length === 1 && !/\d/.test(e.key)) {
    e.preventDefault()
  }
}
</script>

<template>
  <div
    class="money-input"
    :class="[
      inputClass,
      {
        'money-input--error': error,
        'money-input--disabled': disabled,
      },
    ]"
  >
    <input
      :id="id"
      ref="inputEl"
      type="text"
      inputmode="numeric"
      autocomplete="off"
      :value="display"
      :placeholder="placeholder"
      :disabled="disabled"
      class="money-input__control"
      @input="onInput"
      @keydown="onKeydown"
      @blur="onBlur"
      @focus="onFocus"
    />
    <span class="money-input__suffix" aria-hidden="true">đ</span>
  </div>
</template>

<style scoped>
.money-input {
  display: flex;
  align-items: center;
  width: 100%;
  min-width: 0;
  gap: 8px;
  box-sizing: border-box;
}

.money-input__control {
  flex: 1 1 0;
  width: 0;
  min-width: 0;
  margin: 0;
  padding: 0;
  border: 0;
  border-radius: 0;
  background: transparent;
  box-shadow: none;
  outline: none;
  font: inherit;
  line-height: inherit;
  color: inherit;
  text-align: inherit;
  font-variant-numeric: tabular-nums;
  appearance: none;
}

.money-input__control:focus {
  outline: none;
  box-shadow: none;
}

.money-input__suffix {
  flex-shrink: 0;
  font: inherit;
  line-height: 1;
  opacity: 0.55;
  user-select: none;
}

.money-input--disabled {
  opacity: 0.65;
  pointer-events: none;
}

.money-input--error {
  border-color: #dc2626;
}
</style>
