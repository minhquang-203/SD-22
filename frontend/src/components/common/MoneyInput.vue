<script setup>
import { ref, watch } from 'vue'

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
/** Đang soạn chữ bằng bộ gõ (Unikey/EVKey/IME) — không được sửa value giữa chừng. */
let composing = false

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

function countDigitsAfter(str, caret) {
  let count = 0
  const start = Math.max(0, Math.min(caret, str.length))
  for (let i = start; i < str.length; i++) {
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

function clamp(num) {
  if (num == null) return null
  if (props.max != null && Number.isFinite(props.max) && num > props.max) {
    return Math.trunc(props.max)
  }
  if (num < 0) return 0
  return num
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
  (v) => syncFromModel(v),
)

/**
 * Đọc lại value trình duyệt vừa gõ, format và đặt con trỏ ngay (đồng bộ).
 * Con trỏ neo theo số chữ số bên PHẢI: dấu chấm nghìn chỉ chèn thêm bên trái
 * nên gõ nối cuối luôn giữ con trỏ ở cuối.
 */
function reformat(el) {
  const raw = el.value
  const caret = el.selectionStart ?? raw.length
  const digitsAfter = countDigitsAfter(raw, caret)

  const parsed = parseMoney(raw)
  const num = clamp(parsed)
  const formatted = formatDots(num)

  display.value = formatted
  if (el.value !== formatted) el.value = formatted

  const totalDigits = digitsOnly(formatted).length
  const pos =
    num !== parsed
      ? formatted.length
      : caretFromDigitCount(formatted, Math.max(0, totalDigits - digitsAfter))
  if (document.activeElement === el) {
    try {
      el.setSelectionRange(pos, pos)
    } catch {
      /* ignore */
    }
  }

  const current = props.modelValue == null || props.modelValue === '' ? null : Number(props.modelValue)
  if (current !== num) {
    emit('update:modelValue', num)
    emit('input', num)
  }
}

function onInput(e) {
  if (composing || e.isComposing) return
  reformat(e.target)
}

function onCompositionStart() {
  composing = true
}

function onCompositionEnd(e) {
  composing = false
  reformat(e.target)
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
  if (e.ctrlKey || e.metaKey || e.altKey || e.isComposing) return
  const el = e.target
  const start = el.selectionStart
  const end = el.selectionEnd
  // Backspace/Delete ngay cạnh dấu chấm nghìn: nhảy qua dấu chấm để xoá chữ số kế bên,
  // nếu không trình duyệt chỉ xoá dấu chấm rồi format lại y như cũ.
  if (start != null && start === end) {
    if (e.key === 'Backspace' && start > 0 && el.value[start - 1] === '.') {
      el.setSelectionRange(start - 1, start - 1)
      return
    }
    if (e.key === 'Delete' && el.value[start] === '.') {
      el.setSelectionRange(start + 1, start + 1)
      return
    }
  }
  if (e.key.length === 1 && !/^\d$/.test(e.key)) e.preventDefault()
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
      @compositionstart="onCompositionStart"
      @compositionend="onCompositionEnd"
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
