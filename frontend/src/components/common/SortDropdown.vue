<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from "vue";
import { Icon } from "@iconify/vue";

const props = defineProps({
  modelValue: { type: String, default: "" },
  options: { type: Array, required: true },
  label: { type: String, default: "Sắp xếp" },
});

const emit = defineEmits(["update:modelValue"]);

const open = ref(false);
const root = ref(null);

const activeLabel = computed(() => {
  const found = props.options.find((o) => o.value === props.modelValue);
  return found ? found.label : props.label;
});

function toggle() {
  open.value = !open.value;
}

function select(option) {
  emit("update:modelValue", option.value === props.modelValue ? "" : option.value);
  open.value = false;
}

function onClickOutside(event) {
  if (root.value && !root.value.contains(event.target)) {
    open.value = false;
  }
}

onMounted(() => document.addEventListener("click", onClickOutside));
onBeforeUnmount(() => document.removeEventListener("click", onClickOutside));
</script>

<template>
  <div ref="root" class="sort-dd" :class="{ 'sort-dd--active': modelValue }">
    <button type="button" class="sort-dd__btn" @click.stop="toggle">
      <Icon icon="tabler:arrows-sort" class="sort-dd__lead" />
      <span class="sort-dd__text">{{ activeLabel }}</span>
      <Icon
        icon="tabler:chevron-down"
        class="sort-dd__caret"
        :class="{ open }"
      />
    </button>

    <transition name="sort-dd-fade">
      <ul v-if="open" class="sort-dd__menu">
        <li
          v-for="option in options"
          :key="option.value"
          class="sort-dd__item"
          :class="{ selected: option.value === modelValue }"
          @click="select(option)"
        >
          <Icon :icon="option.icon || 'tabler:sort-descending'" class="sort-dd__ico" />
          <span>{{ option.label }}</span>
          <Icon
            v-if="option.value === modelValue"
            icon="tabler:check"
            class="sort-dd__check"
          />
        </li>
      </ul>
    </transition>
  </div>
</template>

<style scoped>
.sort-dd {
  position: relative;
  display: inline-block;
}

.sort-dd__btn {
  background: transparent;
  color: #1e1510;
  border: 1px solid #e2d9cc;
  border-radius: 8px;
  padding: 9px 14px;
  font-size: 13px;
  font-weight: 400;
  line-height: 1;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  gap: 8px;
  white-space: nowrap;
  transition:
    border-color 0.2s,
    color 0.2s,
    background 0.2s;
}

.sort-dd__btn:hover {
  border-color: #c9a96e;
  color: #a97f45;
}

.sort-dd--active .sort-dd__btn {
  border-color: #c9a96e;
  color: #a97f45;
  background: #fbf7f0;
}

.sort-dd__lead {
  font-size: 16px;
}

.sort-dd__caret {
  font-size: 14px;
  transition: transform 0.2s;
}
.sort-dd__caret.open {
  transform: rotate(180deg);
}

.sort-dd__menu {
  position: absolute;
  top: calc(100% + 6px);
  right: 0;
  z-index: 50;
  min-width: 236px;
  margin: 0;
  padding: 6px;
  list-style: none;
  background: #fff;
  border: 1px solid #e2d9cc;
  border-radius: 10px;
  box-shadow: 0 12px 30px rgba(30, 21, 16, 0.12);
}

.sort-dd__item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 9px 10px;
  border-radius: 7px;
  font-size: 13px;
  color: #1e1510;
  cursor: pointer;
  transition: background 0.15s;
}

.sort-dd__ico {
  font-size: 16px;
  color: #a97f45;
  flex-shrink: 0;
}

.sort-dd__item:hover {
  background: #f6f1e8;
}

.sort-dd__item.selected {
  background: #fbf7f0;
  font-weight: 500;
}

.sort-dd__check {
  margin-left: auto;
  color: #a97f45;
  font-size: 16px;
}

.sort-dd-fade-enter-active,
.sort-dd-fade-leave-active {
  transition:
    opacity 0.15s ease,
    transform 0.15s ease;
}
.sort-dd-fade-enter-from,
.sort-dd-fade-leave-to {
  opacity: 0;
  transform: translateY(-4px);
}
</style>
