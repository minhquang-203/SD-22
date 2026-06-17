<script setup>
import PageHeader from '@/components/ui/PageHeader.vue'
import { useRouter } from 'vue-router'

const router = useRouter()

function goList() {
  router.push('/admin/hoa-don')
}
</script>

<template>
  <div class="space-y-6">
    <PageHeader
      title="Chi tiết hóa đơn"
      description="Chọn một hóa đơn từ danh sách để xem chi tiết và nhật ký hành động"
    />
    <div class="soleil-card text-center py-16 px-6">
      <p class="text-[var(--admin-muted)] mb-6 max-w-md mx-auto">
        Vui lòng mở danh sách hóa đơn và bấm <strong>Xem</strong> trên đơn cần tra cứu.
      </p>
      <button type="button" class="soleil-btn-primary" @click="goList">
        Đi tới danh sách hóa đơn
      </button>
    </div>
  </div>
</template>

<style scoped>
.soleil-card {
  background: #fff;
  border: 1px solid var(--admin-border, rgba(30, 21, 16, 0.1));
  border-radius: 1rem;
}
</style>
