<template>
  <div class="rule-manager-container">
    <div class="page-header">
      <div>
        <h1 class="page-title">Quản lý Luật Gợi Ý (Rules Engine)</h1>
        <p class="page-subtitle">Thiết lập logic tổ hợp điểm Quiz để tự động đề xuất Tag sản phẩm tương ứng</p>
      </div>
      <button class="btn-primary" @click="openAddModal">
        <span class="icon">+</span> Thêm Luật Mới
      </button>
    </div>

    <div class="card">
      <div v-if="rules.length === 0" class="empty-state">
        <div class="empty-icon">⚡</div>
        <h3>Chưa có luật gợi ý nào</h3>
        <p>Hệ thống cần các quy tắc để biết cách gợi ý routine cho khách hàng.</p>
        <button class="btn-outline" @click="openAddModal">Tạo Luật Đầu Tiên</button>
      </div>

      <div v-else class="table-responsive">
        <table class="admin-table">
          <thead>
            <tr>
              <th style="width: 22%">Tên Quy Tắc Hệ Thống</th>
              <th style="width: 40%">Mô tả Điều Kiện Gộp </th>
              <th style="width: 23%">Tag Sản Phẩm Đề Xuất</th>
              <th style="width: 10%">Trạng thái</th>
              <th class="actions-col" style="width: 5%">Thao tác</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="rule in rules" :key="rule.id">
              <td class="font-medium-title">{{ rule.name }}</td>
              <td>
                <div class="condition-tags-wrapper">
                  <div v-for="(cond, i) in rule.conditions" :key="i" class="cond-tag-block">
                    <span class="logic-indicator" v-if="i > 0">AND</span>
                    <span class="cond-tag">
                      <strong>{{ getTagName(cond.tagId) }}</strong> 
                      <span class="operator">{{ cond.operator }}</span> 
                      <mark>{{ cond.value }}</mark>
                    </span>
                  </div>
                </div>
              </td>
              <td>
                <div class="result-tags">
                  <span v-for="tagId in rule.resultTags" :key="tagId" class="res-tag">
                    #{{ getTagName(tagId) }}
                  </span>
                </div>
              </td>
              <td>
                <span class="status-badge" :class="rule.isActive ? 'active' : 'inactive'">
                  {{ rule.isActive ? 'Đang kích hoạt' : 'Đang tắt' }}
                </span>
              </td>
              <td class="actions-col">
                <button class="btn-icon edit" @click="editRule(rule)" title="Sửa luật">✏️</button>
                <button class="btn-icon delete" @click="deleteRule(rule.id)" title="Xóa luật">🗑️</button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <div v-if="isModalOpen" class="modal-overlay" @click.self="closeModal">
      <div class="modal-content">
        <div class="modal-header">
          <h2>{{ isEditing ? 'Cập Nhật Luật Gợi Ý' : 'Khởi Tạo Luật Gợi Ý Mới' }}</h2>
          <button class="btn-close" @click="closeModal">×</button>
        </div>

        <div class="modal-body">
          <div class="form-group">
            <label>Tên quy tắc định danh <span class="required">*</span></label>
            <input type="text" v-model="currentRule.name" placeholder="VD: Luật 1: Da dầu mụn làm việc văn phòng">
          </div>

          <div class="logic-section condition-section">
            <div class="section-header">
              <h3>Vùng 1: Điều Kiện Logic Điểm Số (IF)</h3>
              <button class="btn-sm btn-outline" @click="addCondition">+ Thêm mệnh đề AND</button>
            </div>
            
            <div v-if="currentRule.conditions.length === 0" class="no-data">
              Chưa thiết lập điều kiện. Luật này tạm thời áp dụng mặc định cho mọi bài test.
            </div>

            <div v-for="(cond, index) in currentRule.conditions" :key="index" class="logic-row">
              <span class="logic-label" :class="{ 'and-label': index > 0 }">
                {{ index === 0 ? 'IF' : 'AND' }}
              </span>
              
              <select v-model="cond.tagId" class="flex-2">
                <option value="" disabled>-- Chọn thuộc tính da --</option>
                <option v-for="tag in availableTags.filter(t => t.type === 'condition')" :key="tag.id" :value="tag.id">
                  {{ tag.name }}
                </option>
              </select>

              <select v-model="cond.operator" class="flex-1 text-center">
                <option value=">=">&ge;</option>
                <option value=">">&gt;</option>
                <option value="==">==</option>
                <option value="<">&lt;</option>
              </select>

              <input type="number" v-model="cond.value" class="flex-1" placeholder="Điểm ngưỡng">
              
              <button class="btn-icon remove" @click="removeCondition(index)">✖</button>
            </div>
          </div>

          <div class="logic-section result-section">
            <div class="section-header">
              <h3>Vùng 2: Định Hướng Gợi Ý Sản Phẩm</h3>
            </div>
            <p class="help-text">Nếu thỏa mãn các điều kiện điểm số trên, hệ thống tự động lọc các sản phẩm có các nhãn (Tag) dưới đây:</p>
            
            <div class="tag-selector-grid">
              <label v-for="tag in availableTags.filter(t => t.type === 'result')" :key="tag.id" class="checkbox-card" :class="{'checked': currentRule.resultTags.includes(tag.id)}">
                <input type="checkbox" :value="tag.id" v-model="currentRule.resultTags">
                <span class="checkbox-custom"></span>
                <span class="tag-text">#{{ tag.name }}</span>
              </label>
            </div>
          </div>

          <div class="form-group toggle-group">
            <label>Trạng thái vận hành quy tắc:</label>
            <label class="switch">
              <input type="checkbox" v-model="currentRule.isActive">
              <span class="slider round"></span>
            </label>
            <span class="status-text-live" :class="currentRule.isActive ? 'text-green' : 'text-gray'">
              {{ currentRule.isActive ? 'ĐANG BẬT' : 'ĐANG TẮT' }}
            </span>
          </div>
        </div>

        <div class="modal-footer">
          <button class="btn-outline" @click="closeModal">Hủy bỏ</button>
          <button class="btn-primary" @click="saveRule">Lưu Hệ Thống Luật</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';

const rules = ref([]);
const isModalOpen = ref(false);
const isEditing = ref(false);

// --- HỆ THỐNG TAG TOÀN DIỆN MÁP GIỮA QUIZ VÀ KHO HÀNG ---
const availableTags = ref([
  // Thẻ điều kiện đầu vào (Từ kết quả tích lũy điểm của Quiz)
  { id: 'TAG_DA_DAU', name: 'Điểm Da Dầu', type: 'condition' },
  { id: 'TAG_DA_KHO', name: 'Điểm Da Khô', type: 'condition' },
  { id: 'TAG_HON_HOP', name: 'Điểm Da Hỗn Hợp', type: 'condition' },
  { id: 'TAG_NHAY_CAM', name: 'Điểm Da Nhạy Cảm', type: 'condition' },
  { id: 'TAG_MUN', name: 'Điểm Mụn & Lỗ Chân Lông', type: 'condition' },
  { id: 'TAG_LAO_HOA', name: 'Điểm Lão Hóa', type: 'condition' },
  { id: 'TAG_THAM_NAM', name: 'Điểm Thâm Nám', type: 'condition' },
  { id: 'TAG_VAN_PHONG', name: 'Môi Trường Văn Phòng', type: 'condition' },
  { id: 'TAG_NGOAI_TROI', name: 'Môi Trường Ngoài Trời', type: 'condition' },
  { id: 'TAG_THE_THAO', name: 'Hoạt Động Thể Thao', type: 'condition' },
  { id: 'TAG_WANT_TINTED', name: 'Thích Nâng Tone', type: 'condition' },
  { id: 'TAG_WANT_GLOWY', name: 'Thích Căng Bóng', type: 'condition' },

  // Thẻ kết quả đầu ra (Dùng để truy vấn bảng sản phẩm trong Database)
  { id: 'RES_KIEM_DAU', name: 'Kiềm Dầu chuyên sâu', type: 'result' },
  { id: 'RES_DUONG_AM', name: 'Dưỡng Ẩm Sâu', type: 'result' },
  { id: 'RES_KHANG_NUOC', name: 'Kháng Nước & Mồ Hôi', type: 'result' },
  { id: 'RES_NANG_TONE', name: 'Nâng Tone Tự Nhiên', type: 'result' },
  { id: 'RES_ANH_SANG_XANH', name: 'Chống Ánh Sáng Xanh', type: 'result' },
  { id: 'RES_PHO_RONG', name: 'Màng Lọc Phổ Rộng (UV Strong)', type: 'result' },
  { id: 'RES_VAT_LY_LANH', name: 'Thuần Vật Lý Lành Tính', type: 'result' },
  { id: 'RES_MONG_NHE', name: 'Kết Cấu Fluid Mỏng Nhẹ', type: 'result' }
]);

const defaultRule = {
  id: null,
  name: '',
  conditions: [],
  resultTags: [],
  isActive: true
};

const currentRule = ref(JSON.parse(JSON.stringify(defaultRule)));

onMounted(() => {
  fetchRules();
});

// --- NẠP TOÀN BỘ 5 KỊCH BẢN LUẬT CHUYÊN SÂU THEO BẢN THIẾT KẾ ---
const fetchRules = () => {
  rules.value = [
    {
      id: 1,
      name: 'Luật 1: Da dầu mụn làm việc văn phòng',
      conditions: [
        { tagId: 'TAG_DA_DAU', operator: '>=', value: 4 },
        { tagId: 'TAG_MUN', operator: '>=', value: 1 },
        { tagId: 'TAG_VAN_PHONG', operator: '==', value: 1 }
      ],
      resultTags: ['RES_KIEM_DAU', 'RES_ANH_SANG_XANH', 'RES_MONG_NHE'],
      isActive: true
    },
    {
      id: 2,
      name: 'Luật 2: Da khô lão hóa ngoài trời',
      conditions: [
        { tagId: 'TAG_DA_KHO', operator: '>=', value: 4 },
        { tagId: 'TAG_LAO_HOA', operator: '>=', value: 1 },
        { tagId: 'TAG_NGOAI_TROI', operator: '==', value: 1 }
      ],
      resultTags: ['RES_DUONG_AM', 'RES_PHO_RONG'],
      isActive: true
    },
    {
      id: 3,
      name: 'Luật 3: Da nhạy cảm chơi thể thao, dã ngoại',
      conditions: [
        { tagId: 'TAG_NHAY_CAM', operator: '>=', value: 4 },
        { tagId: 'TAG_THE_THAO', operator: '==', value: 1 }
      ],
      resultTags: ['RES_KHANG_NUOC', 'RES_VAT_LY_LANH'],
      isActive: true
    },
    {
      id: 4,
      name: 'Luật 4: Khách hàng thích nâng tone thay makeup',
      conditions: [
        { tagId: 'TAG_WANT_TINTED', operator: '>=', value: 1 },
        { tagId: 'TAG_THAM_NAM', operator: '>=', value: 1 }
      ],
      resultTags: ['RES_NANG_TONE', 'RES_KIEM_DAU'],
      isActive: true
    },
    {
      id: 5,
      name: 'Luật 5: Da hỗn hợp thích căng bóng mọng nước',
      conditions: [
        { tagId: 'TAG_HON_HOP', operator: '>=', value: 4 },
        { tagId: 'TAG_WANT_GLOWY', operator: '>=', value: 1 }
      ],
      resultTags: ['RES_DUONG_AM', 'RES_MONG_NHE'],
      isActive: true
    },
    {
      id: 6,
      name: 'Luật 6: Da đang Treatment/Thâm nám đi nắng gắt',
      conditions: [
        { tagId: 'TAG_THAM_NAM', operator: '>=', value: 1 },
        { tagId: 'TAG_LAO_HOA', operator: '>=', value: 1 },
        { tagId: 'TAG_NGOAI_TROI', operator: '==', value: 1 }
      ],
      resultTags: ['RES_PHO_RONG', 'RES_DUONG_AM'],
      isActive: true
    },
    {
      id: 7,
      name: 'Luật 7: Da nhạy cảm dễ nổi mụn kích ứng',
      conditions: [
        { tagId: 'TAG_NHAY_CAM', operator: '>=', value: 4 },
        { tagId: 'TAG_MUN', operator: '>=', value: 1 }
      ],
      resultTags: ['RES_VAT_LY_LANH', 'RES_KIEM_DAU'],
      isActive: true
    },
    {
      id: 8,
      name: 'Luật 8: Da dầu bơi lội, hoạt động ngoài trời gắt',
      conditions: [
        { tagId: 'TAG_DA_DAU', operator: '>=', value: 4 },
        { tagId: 'TAG_NGOAI_TROI', operator: '==', value: 1 }
      ],
      resultTags: ['RES_KIEM_DAU', 'RES_KHANG_NUOC', 'RES_PHO_RONG'],
      isActive: true
    }
  ];
};

const getTagName = (tagId) => {
  const t = availableTags.value.find(x => x.id === tagId);
  return t ? t.name : tagId;
};

// --- MODAL & LOGIC ĐIỀU HƯỚNG MÀN HÌNH ---
const openAddModal = () => {
  isEditing.value = false;
  currentRule.value = JSON.parse(JSON.stringify(defaultRule));
  isModalOpen.value = true;
};

const editRule = (rule) => {
  isEditing.value = true;
  currentRule.value = JSON.parse(JSON.stringify(rule));
  isModalOpen.value = true;
};

const closeModal = () => { isModalOpen.value = false; };

const addCondition = () => {
  currentRule.value.conditions.push({ tagId: '', operator: '>=', value: 0 });
};

const removeCondition = (index) => {
  currentRule.value.conditions.splice(index, 1);
};

const saveRule = () => {
  if (!currentRule.value.name.trim()) { alert("Vui lòng nhập tên quy tắc nhận diện!"); return; }
  if (currentRule.value.resultTags.length === 0) { alert("Vui lòng gắn ít nhất 1 nhãn sản phẩm hệ quả!"); return; }

  if (isEditing.value) {
    const idx = rules.value.findIndex(r => r.id === currentRule.value.id);
    if (idx !== -1) rules.value[idx] = JSON.parse(JSON.stringify(currentRule.value));
  } else {
    const newRule = JSON.parse(JSON.stringify(currentRule.value));
    newRule.id = Date.now();
    rules.value.push(newRule);
  }
  closeModal();
};

const deleteRule = (id) => {
  if (confirm("Xác nhận xóa luật phân tích này ra khỏi hệ thống cốt lõi?")) {
    rules.value = rules.value.filter(r => r.id !== id);
  }
};
</script>

<style scoped>
.rule-manager-container { padding: 24px; background-color: #fcfaf7; min-height: 100vh; color: #333; font-family: system-ui, sans-serif; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 24px; border-bottom: 1px solid #eae2d8; padding-bottom: 16px; }
.page-title { font-size: 26px; font-weight: 600; color: #2a201b; margin: 0 0 6px 0; }
.page-subtitle { font-size: 14px; color: #8a7e75; margin: 0; }

/* Buttons */
.btn-primary { background-color: #4f46e5; color: white; border: none; padding: 12px 20px; border-radius: 6px; font-weight: 500; cursor: pointer; display: inline-flex; align-items: center; gap: 8px; box-shadow: 0 2px 4px rgba(79, 70, 229, 0.2); }
.btn-primary:hover { background-color: #4338ca; }
.btn-outline { background-color: #fff; color: #5c4f46; border: 1px solid #d1c7bd; padding: 8px 16px; border-radius: 6px; font-weight: 500; cursor: pointer; }
.btn-outline:hover { background-color: #f7f4f0; }
.btn-sm { padding: 6px 12px; font-size: 13px; }
.btn-icon { background: none; border: none; font-size: 16px; cursor: pointer; padding: 6px; border-radius: 4px; opacity: 0.7; transition: all 0.2s; }
.btn-icon:hover { opacity: 1; background-color: #f0eae1; }
.btn-icon.delete { color: #dc2626; }

.card { background-color: white; border-radius: 12px; border: 1px solid #eae2d8; box-shadow: 0 4px 6px -1px rgba(0,0,0,0.05); padding: 24px; overflow: hidden; }

/* Table Design */
.table-responsive { overflow-x: auto; }
.admin-table { width: 100%; border-collapse: collapse; text-align: left; }
.admin-table th { padding: 14px 16px; background-color: #f5f0ea; border-bottom: 2px solid #eae2d8; color: #5c4f46; font-weight: 600; font-size: 14px; }
.admin-table td { padding: 18px 16px; border-bottom: 1px solid #f0eae1; vertical-align: middle; }
.font-medium-title { font-weight: 600; color: #2a201b; font-size: 14.5px; }

/* Khối hiển thị mệnh đề */
.condition-tags-wrapper { display: flex; flex-direction: column; gap: 6px; }
.cond-tag-block { display: flex; align-items: center; gap: 8px; }
.logic-indicator { font-size: 11px; font-weight: bold; background: #34d399; color: white; padding: 2px 5px; border-radius: 4px; min-width: 28px; text-align: center; }
.cond-tag { background-color: #faf8f5; padding: 6px 12px; border-radius: 6px; font-size: 13px; color: #4a3e3d; border: 1px solid #eae2d8; border-left: 4px solid #4f46e5; display: inline-flex; gap: 6px; }
.cond-tag .operator { color: #dc2626; font-weight: bold; }
.cond-tag mark { background: none; color: #4f46e5; font-weight: bold; }

/* Thẻ kết quả đầu ra */
.result-tags { display: flex; flex-wrap: wrap; gap: 6px; }
.res-tag { background-color: #fdf2f8; color: #9d174d; border: 1px solid #fbcfe8; padding: 4px 10px; border-radius: 20px; font-size: 12px; font-weight: 500; }

.status-badge { padding: 4px 10px; border-radius: 20px; font-size: 12px; font-weight: 500; display: inline-block; }
.status-badge.active { background-color: #d1fae5; color: #065f46; }
.status-badge.inactive { background-color: #f3f4f6; color: #374151; }

/* CSS Modal Form */
.modal-overlay { position: fixed; top: 0; left: 0; right: 0; bottom: 0; background-color: rgba(42, 32, 27, 0.4); display: flex; justify-content: center; align-items: flex-start; padding-top: 4vh; z-index: 1000; backdrop-filter: blur(2px); }
.modal-content { background-color: white; border-radius: 12px; width: 100%; max-width: 750px; max-height: 92vh; display: flex; flex-direction: column; box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.15); border: 1px solid #eae2d8; }
.modal-header { padding: 18px 24px; border-bottom: 1px solid #eae2d8; display: flex; justify-content: space-between; align-items: center; background: #faf8f5; border-radius: 12px 12px 0 0; }
.modal-header h2 { margin: 0; font-size: 18px; color: #2a201b; }
.btn-close { background: none; border: none; font-size: 28px; color: #8a7e75; cursor: pointer; }
.modal-body { padding: 24px; overflow-y: auto; }
.modal-footer { padding: 16px 24px; border-top: 1px solid #eae2d8; display: flex; justify-content: flex-end; gap: 12px; background: #faf8f5; border-radius: 0 0 12px 12px; }

.form-group { margin-bottom: 22px; }
.form-group label { display: block; font-size: 14px; font-weight: 600; color: #4a3e3d; margin-bottom: 8px; }
.form-group input[type="text"] { width: 100%; padding: 11px 14px; border: 1px solid #d1c7bd; border-radius: 6px; font-size: 14px; box-sizing: border-box; }

.logic-section { background-color: #faf8f5; border: 1px solid #eae2d8; border-radius: 8px; padding: 18px; margin-bottom: 22px; }
.section-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 14px; }
.section-header h3 { margin: 0; font-size: 13.5px; font-weight: 700; color: #4f46e5; text-transform: uppercase; letter-spacing: 0.5px; }

.logic-row { display: flex; gap: 10px; align-items: center; margin-bottom: 12px; background: white; padding: 10px; border-radius: 6px; border: 1px solid #d1c7bd; }
.logic-label { font-weight: bold; color: #4f46e5; width: 38px; text-align: center; font-size: 13px; background: #e0e7ff; padding: 4px 0; border-radius: 4px; }
.logic-label.and-label { color: #059669; background: #d1fae5; }
.flex-1 { flex: 1; }
.flex-2 { flex: 2; }
.text-center { text-align-last: center; }
.logic-row select, .logic-row input { padding: 10px; border: 1px solid #d1c7bd; border-radius: 5px; font-size: 14px; }

/* Checkbox kiểu hiện đại cho vùng kết quả */
.tag-selector-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(210px, 1-flex)); gap: 10px; margin-top: 10px; }
.checkbox-card { display: flex; align-items: center; gap: 10px; background: white; padding: 12px; border: 1px solid #d1c7bd; border-radius: 6px; cursor: pointer; transition: all 0.2s; user-select: none; }
.checkbox-card:hover { border-color: #bfae9e; background-color: #faf8f5; }
.checkbox-card.checked { border-color: #9d174d; background-color: #fff1f2; }
.checkbox-card input { display: none; }
.checkbox-custom { width: 16px; height: 16px; border: 2px solid #cbd5e1; border-radius: 4px; position: relative; }
.checkbox-card.checked .checkbox-custom { background-color: #9d174d; border-color: #9d174d; }
.checkbox-card.checked .checkbox-custom::after { content: "✓"; position: absolute; color: white; font-size: 11px; font-weight: bold; top: -2px; left: 2px; }
.tag-text { font-size: 13px; font-weight: 500; color: #374151; }

/* Switch toggle */
.toggle-group { display: flex; align-items: center; gap: 12px; }
.switch { position: relative; display: inline-block; width: 44px; height: 24px; }
.switch input { opacity: 0; width: 0; height: 0; }
.slider { position: absolute; cursor: pointer; top: 0; left: 0; right: 0; bottom: 0; background-color: #cbd5e1; transition: .3s; border-radius: 24px; }
.slider:before { position: absolute; content: ""; height: 16px; width: 16px; left: 4px; bottom: 4px; background-color: white; transition: .3s; border-radius: 50%; }
input:checked + .slider { background-color: #10b981; }
input:checked + .slider:before { transform: translateX(20px); }
.status-text-live { font-size: 12px; font-weight: bold; letter-spacing: 0.5px; }
.text-green { color: #10b981; }
.text-gray { color: #6b7280; }
.required { color: #ef4444; }
.no-data { text-align: center; font-size: 13px; color: #9ca3af; padding: 16px 0; font-style: italic; }
.help-text { font-size: 13px; color: #8a7e75; margin: -4px 0 12px 0; }
</style>