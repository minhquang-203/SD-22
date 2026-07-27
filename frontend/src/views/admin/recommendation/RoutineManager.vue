<template>
  <div class="routine-manager-container">
    <div class="page-header">
      <div>
        <h1 class="page-title">Quản lý Routine & Combo</h1>
        <p class="page-subtitle">Thiết lập các bộ sản phẩm (Routine) để gợi ý sau khi khách làm Quiz</p>
      </div>
      <button class="btn-primary" @click="openAddModal">
        <span class="icon">+</span> Tạo Routine Mới
      </button>
    </div>

    <div class="card">
      <div v-if="routines.length === 0" class="empty-state">
        <div class="empty-icon">🧴</div>
        <h3>Chưa có Routine nào</h3>
        <p>Hãy tạo các bộ combo (ví dụ: Combo Da Dầu, Combo Đi Biển) để gợi ý cho khách.</p>
        <button class="btn-outline" @click="openAddModal">Bắt đầu ngay</button>
      </div>

      <div v-else class="table-responsive">
        <table class="admin-table">
          <thead>
            <tr>
              <th>Tên Routine / Combo</th>
              <th>Sản phẩm trong bộ</th>
              <th>Dành cho loại da</th>
              <th>Trạng thái</th>
              <th class="actions-col">Thao tác</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="routine in routines" :key="routine.id">
              <td>
                <div class="routine-info">
                  <span class="routine-name">{{ routine.ten }}</span>
                  <span class="routine-id">ID: {{ routine.id }}</span>
                </div>
              </td>
              <td>
                <div class="product-pills">
                  <span v-for="ct in routine.chiTiets" :key="ct.idSanPham" class="p-pill">
                    📦 {{ ct.tenSanPham }}
                  </span>
                </div>
              </td>
              <td>
                <span class="skin-tag">{{ getSkinName(routine.idLoaiDa) }}</span>
              </td>
              <td>
                <span class="status-badge" :class="routine.trangThai ? 'active' : 'inactive'">
                  {{ routine.trangThai ? 'Đang dùng' : 'Tạm ẩn' }}
                </span>
              </td>
              <td class="actions-col">
                <button class="btn-icon" @click="editRoutine(routine)">✏️</button>
                <button class="btn-icon" @click="confirmDeleteRoutine(routine.id)">🗑️</button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <div v-if="isModalOpen" class="modal-overlay" @click.self="closeModal">
      <div class="modal-content">
        <div class="modal-header">
          <h2>{{ isEditing ? 'Cập nhật Routine' : 'Tạo Combo Routine Mới' }}</h2>
          <button class="btn-close" @click="closeModal">×</button>
        </div>

        <div class="modal-body">
          <div class="form-group">
            <label>Tên bộ Routine <span class="required">*</span></label>
            <input type="text" v-model="currentRoutine.ten" placeholder="VD: Combo Chống Nắng Cho Da Dầu Mụn">
          </div>

          <div class="form-group">
            <label>Mô tả ngắn gọn</label>
            <textarea v-model="currentRoutine.moTa" rows="2" placeholder="Giải thích lý do combo này hiệu quả..."></textarea>
          </div>

          <div class="form-group">
            <label>Chọn loại da mục tiêu</label>
            <select v-model="currentRoutine.idLoaiDa">
              <option v-for="tag in availableTags" :key="tag.id" :value="tag.id">
                {{ tag.name }}
              </option>
            </select>
          </div>

          <div class="form-group">
            <label>Trạng thái</label>
            <label style="display: flex; gap: 8px; font-weight: normal;">
              <input type="checkbox" v-model="currentRoutine.trangThai" style="width: auto;"> Đang hoạt động
            </label>
          </div>

          <div class="form-group">
            <label>Danh sách sản phẩm trong bộ</label>
            <div class="product-selector">
              <label v-for="p in availableProducts" :key="p.id" class="product-checkbox">
                <input type="checkbox" :value="p.id" v-model="selectedProductIds">
                <span class="p-name">{{ p.ten }}</span>
              </label>
            </div>
          </div>
        </div>

        <div class="modal-footer">
          <button class="btn-outline" @click="closeModal">Hủy</button>
          <button class="btn-primary" @click="saveRoutine">Lưu Routine</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import { confirm } from '@/composables/useConfirm';
import { getAllRoutinesAdmin, createRoutineAdmin, updateRoutineAdmin, deleteRoutineAdmin } from '@/api/routineApi';
import { getProducts } from '@/api/sanPhamApi';

const routines = ref([]);
const availableProducts = ref([]);
const availableTags = ref([
  { id: 1, name: 'Da Dầu' },      
  { id: 2, name: 'Da Khô' },
  { id: 3, name: 'Da Hỗn Hợp' },
  { id: 4, name: 'Da Thường' },
  { id: 5, name: 'Da Nhạy Cảm' },
]);

const isModalOpen = ref(false);
const isEditing = ref(false);
const currentRoutine = ref({ id: null, ten: '', moTa: '', idLoaiDa: 1, trangThai: true, chiTiets: [] });

// Dùng mảng tạm để bind checkbox v-model với id sản phẩm
const selectedProductIds = ref([]);

const loadData = async () => {
  try {
    const [routineRes, productRes] = await Promise.all([
      getAllRoutinesAdmin(),
      getProducts()
    ]);
    routines.value = routineRes.data;
    availableProducts.value = productRes.data || [];
  } catch (error) {
    console.error("Lỗi tải dữ liệu Routine:", error);
  }
};

onMounted(() => loadData());

const openAddModal = () => {
  isEditing.value = false;
  currentRoutine.value = { id: null, ten: '', moTa: '', idLoaiDa: 1, trangThai: true, chiTiets: [] };
  selectedProductIds.value = [];
  isModalOpen.value = true;
};

const editRoutine = (routine) => {
  isEditing.value = true;
  currentRoutine.value = { 
    id: routine.id, 
    ten: routine.ten, 
    moTa: routine.moTa, 
    idLoaiDa: routine.idLoaiDa, 
    trangThai: routine.trangThai,
    chiTiets: [...routine.chiTiets] 
  };
  selectedProductIds.value = routine.chiTiets.map(ct => ct.idSanPham);
  isModalOpen.value = true;
};

const closeModal = () => isModalOpen.value = false;

const saveRoutine = async () => {
  if (!currentRoutine.value.ten.trim()) { alert("Vui lòng nhập tên Routine!"); return; }
  
  // Chuyển selectedProductIds thành mảng chiTiets
  currentRoutine.value.chiTiets = selectedProductIds.value.map((id, index) => {
    return { idSanPham: id, thuTu: index + 1, ghiChu: '' };
  });

  try {
    if (isEditing.value) {
      await updateRoutineAdmin(currentRoutine.value.id, currentRoutine.value);
    } else {
      await createRoutineAdmin(currentRoutine.value);
    }
    closeModal();
    loadData();
  } catch (error) {
    alert("Có lỗi xảy ra khi lưu: " + error);
  }
};

const confirmDeleteRoutine = async (id) => {
  const ok = await confirm({
    title: 'Xóa combo',
    message: 'Xóa combo routine này?',
    confirmText: 'Xóa',
    danger: true,
  });
  if (!ok) return;
  
  try {
    await deleteRoutineAdmin(id);
    loadData();
  } catch (error) {
    alert("Lỗi khi xóa: " + error);
  }
};

// Helper hiển thị
const getSkinName = (id) => {
  const t = availableTags.value.find(x => x.id === id);
  return t ? t.name : 'Chưa rõ';
};
</script>

<style scoped>
.routine-manager-container { padding: 24px; background-color: #f8f9fa; min-height: 100vh; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 24px; }
.page-title { font-size: 24px; font-weight: 600; color: #1a1412; margin: 0; }
.page-subtitle { color: #6b7280; font-size: 14px; margin-top: 4px; }

.card { background: white; border-radius: 8px; box-shadow: 0 1px 3px rgba(0,0,0,0.1); padding: 20px; }

.admin-table { width: 100%; border-collapse: collapse; margin-top: 10px; }
.admin-table th { text-align: left; padding: 12px; background: #f9fafb; color: #4b5563; font-weight: 600; border-bottom: 1px solid #e5e7eb; }
.admin-table td { padding: 16px 12px; border-bottom: 1px solid #f3f4f6; }

.routine-info { display: flex; flex-direction: column; }
.routine-name { font-weight: 600; color: #111827; }
.routine-id { font-size: 11px; color: #9ca3af; }

.product-pills { display: flex; flex-wrap: wrap; gap: 6px; }
.p-pill { background: #f3f4f6; padding: 4px 8px; border-radius: 4px; font-size: 12px; color: #374151; border: 1px solid #e5e7eb; }

.skin-tag { background: #eef2ff; color: #4338ca; padding: 4px 10px; border-radius: 12px; font-size: 12px; font-weight: 500; }

.status-badge { padding: 4px 8px; border-radius: 12px; font-size: 12px; font-weight: 500; }
.status-badge.active { background: #dcfce7; color: #15803d; }
.status-badge.inactive { background: #f3f4f6; color: #6b7280; }

.btn-primary { background: #4f46e5; color: white; border: none; padding: 10px 18px; border-radius: 6px; cursor: pointer; font-weight: 500; }
.btn-primary:hover { background: #4338ca; }

.actions-col { text-align: right; }
.btn-icon { background: none; border: none; cursor: pointer; font-size: 16px; padding: 5px; opacity: 0.6; }
.btn-icon:hover { opacity: 1; background: #f3f4f6; border-radius: 4px; }

/* MODAL */
.modal-overlay { position: fixed; top: 0; left: 0; right: 0; bottom: 0; background: rgba(0,0,0,0.5); display: flex; align-items: flex-start; justify-content: center; padding-top: 50px; z-index: var(--admin-z-modal, 5000); }
.modal-content { background: white; border-radius: 8px; width: 500px; max-height: 90vh; overflow-y: auto; box-shadow: 0 20px 25px -5px rgba(0,0,0,0.1); }
.modal-header { padding: 16px 20px; border-bottom: 1px solid #e5e7eb; display: flex; justify-content: space-between; align-items: center; }
.modal-body { padding: 20px; }
.modal-footer { padding: 16px 20px; border-top: 1px solid #e5e7eb; display: flex; justify-content: flex-end; gap: 10px; }

.form-group { margin-bottom: 16px; }
.form-group label { display: block; font-size: 14px; font-weight: 500; color: #374151; margin-bottom: 5px; }
.form-group input, .form-group select, .form-group textarea { width: 100%; padding: 8px 12px; border: 1px solid #d1d5db; border-radius: 6px; }

.product-selector { border: 1px solid #e5e7eb; border-radius: 6px; padding: 10px; max-height: 150px; overflow-y: auto; }
.product-checkbox { display: flex; align-items: center; gap: 10px; margin-bottom: 8px; cursor: pointer; }
.p-name { font-size: 14px; }

.required { color: #ef4444; }
</style>