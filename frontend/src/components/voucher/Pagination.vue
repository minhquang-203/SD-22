<template>
  <div class="pagination-wrap">
    <div class="page-info">
      Hiển thị {{ numberOfElements }} / {{ totalElements }} kết quả
    </div>

    <div class="page-btns">
      <button
        class="page-btn"
        :disabled="first"
        @click="$emit('page-change', 0)"
      >
        <Icon icon="mdi:page-first"></Icon>
      </button>

      <button
        v-for="page in visiblePages"
        :key="page"
        class="page-btn"
        :class="{ active: page - 1 === number }"
        @click="$emit('page-change', page - 1)"
      >
        {{ page }}
      </button>

      <button
        class="page-btn"
        :disabled="last"
       @click="$emit('page-change', totalPages - 1)"
      >
        <Icon icon="mdi:page-last"></Icon>
      </button>
    </div>
  </div>
</template>

<script setup>
import { Icon } from "@iconify/vue";
import { computed } from "vue";

const props = defineProps({
  number: {
    type: Number,
    required: true,
  },
  totalPages: {
    type: Number,
    required: true,
  },
  totalElements: {
    type: Number,
    required: true,
  },
  numberOfElements: {
    type: Number,
    required: true,
  },
  first: {
    type: Boolean,
    required: true,
  },
  last: {
    type: Boolean,
    required: true,
  },
});

defineEmits(["page-change"]);

const visiblePages = computed(() => {
  const current = props.number + 1;

  let start = Math.max(1, current - 2);
  let end = Math.min(props.totalPages, current + 2);

  if (end - start < 4) {
    if (start === 1) {
      end = Math.min(props.totalPages, start + 4);
    } else if (end === props.totalPages) {
      start = Math.max(1, end - 4);
    }
  }

  return Array.from(
    { length: end - start + 1 },
    (_, i) => start + i
  );
});
</script>