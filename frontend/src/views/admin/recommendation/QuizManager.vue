<template>
  <div class="admin-quiz-manager">
    <div class="page-header">
      <div>
        <h1 class="page-title">Quản lý Quiz Loại Da</h1>
      </div>
      <button class="btn-primary" @click="openAddModal">
        + Thêm Câu Hỏi Mới
      </button>
    </div>

    <div class="question-list">
      <div v-if="questions.length === 0" class="empty-state card">
        Chưa có câu hỏi nào. Hãy bấm thêm mới!
      </div>

      <div v-for="(q, index) in questions" :key="q.id" class="question-card">
        <div class="q-header">
          <h3 class="q-title">Câu {{ index + 1 }}: {{ q.title }}</h3>
          <div class="q-actions">
            <button class="btn-icon edit" @click="editQuestion(q)" title="Sửa">✏️</button>
            <button class="btn-icon delete" @click="deleteQuestion(q.id)" title="Xóa">🗑️</button>
          </div>
        </div>

        <div class="q-body">
          <p class="ans-label-text">Các đáp án:</p>
          <div class="answers-container">
            <div v-for="(ans, aIndex) in q.answers" :key="aIndex" class="answer-row">
              <div class="ans-left">
                <span class="ans-icon">{{ ans.icon || '✨' }}</span>
                <span class="ans-text">{{ ans.label }}</span>
              </div>
              <div class="ans-badges">
                <span class="score-badge">Điểm: {{ ans.scoreValue }}</span>
                <span class="tag-badge" :class="getTagStyle(ans.tagId)">{{ getTagName(ans.tagId) }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div v-if="isModalOpen" class="modal-overlay" @click.self="closeModal">
      <div class="modal-content">
        <div class="modal-header">
          <h2>{{ isEditing ? 'Cập Nhật Câu Hỏi' : 'Thêm Câu Hỏi Mới' }}</h2>
          <button class="btn-close" @click="closeModal">×</button>
        </div>

        <div class="modal-body">
          <div class="form-group">
            <label>Nội dung câu hỏi <span class="required">*</span></label>
            <input type="text" v-model="currentQuestion.title" placeholder="VD: Làn da của bạn thường cảm thấy thế nào sau khi rửa mặt?">
          </div>

          <div class="answers-section">
            <div class="section-header">
              <h3>Các đáp án lựa chọn</h3>
              <button class="btn-sm btn-outline" @click="addAnswer">+ Thêm đáp án</button>
            </div>

            <div v-for="(ans, index) in currentQuestion.answers" :key="index" class="answer-edit-box">
              <div class="ans-row-edit">
                
                <div class="flex-none form-group mb-0 icon-input-group">
                  <label>Icon</label>
                  <input type="text" v-model="ans.icon" placeholder="💧" class="text-center">
                </div>

                <div class="flex-2 form-group mb-0">
                  <label>Nội dung đáp án</label>
                  <input type="text" v-model="ans.label" placeholder="VD: Bóng nhờn">
                </div>
                
                <div class="flex-1 form-group mb-0">
                  <label>Điểm</label>
                  <input type="number" v-model="ans.scoreValue" placeholder="10">
                </div>
                
                <div class="flex-1 form-group mb-0">
                  <label>Tag Nhãn (Loại da)</label>
                  <select v-model="ans.tagId">
                    <option value="" disabled>-- Chọn --</option>
                    <option v-for="tag in availableTags" :key="tag.id" :value="tag.id">{{ tag.name }}</option>
                  </select>
                </div>
                
                <button class="btn-icon delete-ans mt-20" @click="removeAnswer(index)">✖</button>
              </div>
            </div>
          </div>
        </div>

        <div class="modal-footer">
          <button class="btn-outline" @click="closeModal">Hủy bỏ</button>
          <button class="btn-primary" @click="saveQuestion">Lưu Câu Hỏi</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';

const questions = ref([]);
const isModalOpen = ref(false);
const isEditing = ref(false);

// Đã chuẩn hóa ID khớp 100% với file SUNOVA_full.sql của bạn
const availableTags = ref([
  { id: 1, name: 'Da Dầu' },      
  { id: 2, name: 'Da Khô' },
  { id: 3, name: 'Da Hỗn Hợp' },
  { id: 4, name: 'Da Thường' },
  { id: 5, name: 'Da Nhạy Cảm' },
]);

const defaultQuestion = {
  id: null,
  title: '',
  answers: []
};

const currentQuestion = ref(JSON.parse(JSON.stringify(defaultQuestion)));

// MÓC NỐI VỚI BACKEND SPRING BOOT
const API_URL = 'http://localhost:8080/api/admin/quizzes';

const loadQuestions = async () => {
  try {
    const response = await fetch(API_URL);
    if (response.ok) {
      questions.value = await response.json();
    }
  } catch (error) {
    console.error("Lỗi kết nối Backend:", error);
  }
};

onMounted(() => {
  loadQuestions();
});

const getTagName = (tagId) => {
  const t = availableTags.value.find(x => x.id === tagId);
  return t ? t.name : 'Chưa có tag';
};

const getTagStyle = (tagId) => {
  if (tagId === 1) return 'tag-blue';
  if (tagId === 2) return 'tag-orange';
  if (tagId === 5) return 'tag-red';
  if (tagId === 3) return 'tag-purple';
  return 'tag-green'; 
};

// --- CHỨC NĂNG MỞ FORM ---
const openAddModal = () => {
  isEditing.value = false;
  currentQuestion.value = JSON.parse(JSON.stringify({
    id: null,
    title: '',
    answers: [
      { icon: '✨', label: '', tagId: '', scoreValue: 10 },
      { icon: '✨', label: '', tagId: '', scoreValue: 10 }
    ]
  }));
  isModalOpen.value = true;
};

const editQuestion = (q) => {
  isEditing.value = true;
  currentQuestion.value = JSON.parse(JSON.stringify(q));
  isModalOpen.value = true;
};

const closeModal = () => { isModalOpen.value = false; };
const addAnswer = () => { currentQuestion.value.answers.push({ icon: '✨', label: '', tagId: '', scoreValue: 10 }); };
const removeAnswer = (index) => { currentQuestion.value.answers.splice(index, 1); };

// --- GỌI API LƯU VÀ XÓA ---
const saveQuestion = async () => {
  if (!currentQuestion.value.title.trim()) { alert("Vui lòng nhập câu hỏi!"); return; }
  if (currentQuestion.value.answers.length < 2) { alert("Cần ít nhất 2 đáp án!"); return; }
  
  try {
    if (isEditing.value) {
      await fetch(`${API_URL}/${currentQuestion.value.id}`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(currentQuestion.value)
      });
    } else {
      await fetch(API_URL, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(currentQuestion.value)
      });
    }
    closeModal();
    loadQuestions(); // Tải lại bảng ngay lập tức
  } catch (error) {
    alert("Có lỗi xảy ra khi lưu: " + error);
  }
};

const deleteQuestion = async (id) => {
  if (confirm("Xóa câu hỏi này?")) {
    try {
      await fetch(`${API_URL}/${id}`, { method: 'DELETE' });
      loadQuestions(); 
    } catch (error) {
      alert("Lỗi khi xóa: " + error);
    }
  }
};
</script>

<style scoped>
/* =========================================
   GIAO DIỆN ADMIN CHÍNH
   ========================================= */
.admin-quiz-manager { padding: 30px; background-color: #fcfcfc; min-height: 100vh; font-family: 'Helvetica Neue', sans-serif; }

.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 30px; }
.page-title { font-size: 22px; font-weight: normal; color: #1a1a1a; font-style: italic; font-family: serif; }

.btn-primary { background-color: #5a4bfa; color: white; border: none; padding: 12px 20px; border-radius: 6px; font-size: 14px; font-weight: 500; cursor: pointer; transition: 0.2s; }
.btn-primary:hover { background-color: #4a3ae0; }

.question-list { display: flex; flex-direction: column; gap: 24px; }
.question-card { background: white; border-radius: 10px; border: 1px solid #eaeaea; box-shadow: 0 4px 12px rgba(0,0,0,0.03); overflow: hidden; }

.q-header { display: flex; justify-content: space-between; align-items: center; padding: 20px 24px; border-bottom: 1px solid #f0f0f0; }
.q-title { margin: 0; font-size: 16px; font-weight: 600; color: #222; }

.btn-icon { background: none; border: none; font-size: 16px; cursor: pointer; padding: 4px; opacity: 0.5; transition: 0.2s; }
.btn-icon:hover { opacity: 1; }

.q-body { padding: 20px 24px; }
.ans-label-text { font-size: 14px; color: #666; margin-top: 0; margin-bottom: 12px; }
.answers-container { display: flex; flex-direction: column; gap: 10px; }

.answer-row { display: flex; justify-content: space-between; align-items: center; background-color: #f8f9fa; padding: 14px 20px; border-radius: 8px; }
.ans-left { display: flex; align-items: center; gap: 12px; }
.ans-icon { font-size: 20px; background: white; width: 36px; height: 36px; display: flex; justify-content: center; align-items: center; border-radius: 6px; box-shadow: 0 2px 4px rgba(0,0,0,0.05); }
.ans-text { font-size: 14px; color: #333; font-weight: 500; }
.ans-badges { display: flex; gap: 10px; }

.score-badge { background-color: #eef2ff; color: #5a4bfa; padding: 4px 12px; border-radius: 20px; font-size: 12px; font-weight: 600; }
.tag-badge { padding: 4px 12px; border-radius: 20px; font-size: 12px; font-weight: 600; }
.tag-blue { background-color: #e0f2fe; color: #0369a1; }
.tag-orange { background-color: #ffedd5; color: #c2410c; }
.tag-red { background-color: #fee2e2; color: #b91c1c; }
.tag-purple { background-color: #f3e8ff; color: #7e22ce; }
.tag-green { background-color: #dcfce7; color: #15803d; }

/* =========================================
   MODAL FORM
   ========================================= */
.modal-overlay { position: fixed; top: 0; left: 0; right: 0; bottom: 0; background: rgba(0,0,0,0.4); display: flex; justify-content: center; align-items: flex-start; padding-top: 5vh; z-index: 1000; }
.modal-content { background: white; width: 700px; border-radius: 10px; box-shadow: 0 10px 30px rgba(0,0,0,0.1); display: flex; flex-direction: column; max-height: 90vh; }
.modal-header { padding: 20px; border-bottom: 1px solid #eee; display: flex; justify-content: space-between; align-items: center; }
.modal-header h2 { margin: 0; font-size: 18px; }
.btn-close { background: none; border: none; font-size: 24px; cursor: pointer; color: #999; }
.modal-body { padding: 20px; overflow-y: auto; }
.modal-footer { padding: 20px; border-top: 1px solid #eee; display: flex; justify-content: flex-end; gap: 10px; background: #fafafa; border-radius: 0 0 10px 10px; }

.form-group { margin-bottom: 15px; }
.form-group label { display: block; font-size: 13px; font-weight: 600; margin-bottom: 5px; color: #444; }
.form-group input, .form-group select { width: 100%; padding: 10px; border: 1px solid #ddd; border-radius: 6px; box-sizing: border-box; font-family: inherit; }

.answers-section { margin-top: 25px; border-top: 1px dashed #ddd; padding-top: 20px; }
.section-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 15px; }
.section-header h3 { margin: 0; font-size: 15px; }
.btn-outline { background: white; border: 1px solid #ccc; padding: 8px 15px; border-radius: 6px; cursor: pointer; }

.answer-edit-box { background: #f9f9f9; padding: 15px; border-radius: 8px; margin-bottom: 10px; border: 1px solid #eee; }
.ans-row-edit { display: flex; gap: 12px; align-items: flex-start; }
.flex-none { flex: none; }
.icon-input-group { width: 60px; }
.text-center { text-align: center; font-size: 18px !important; }
.flex-1 { flex: 1; } .flex-2 { flex: 2; }
.mb-0 { margin-bottom: 0; }
.mt-20 { margin-top: 28px; }
.delete-ans { color: red; font-size: 18px; padding: 0 5px; }
.required { color: red; }
</style>