<template>
  <div class="admin-quiz-manager">
    
    <div class="builder-container">
      
      <div class="header-section">
        <h1 class="page-title">Quản lý Quiz Loại Da</h1>
        <p class="subtitle">Thiết lập câu hỏi và luật tính điểm cho hệ thống gợi ý tự động.</p>
      </div>

      <div class="questions-section">
        <div class="section-title">
          <span>Cấu trúc bài trắc nghiệm</span>
        </div>

        <div v-for="(q, index) in questions" :key="q.id" class="question-box">
          
          <div v-if="editingId === q.id" class="edit-mode">
            <div class="box-header edit-header">
              <h4>Cập nhật Câu {{ index + 1 }}</h4>
            </div>
            
            <div class="form-group">
              <input type="text" v-model="currentQuestion.title" class="input-title-edit" placeholder="Nhập câu hỏi (VD: Làn da của bạn...)">
            </div>

            <div class="answers-edit-list">
              <p class="label-sm">Các đáp án:</p>
              <div v-for="(ans, aIndex) in currentQuestion.answers" :key="aIndex" class="inline-answer-row">
                <input type="text" v-model="ans.label" placeholder="Nội dung đáp án" class="flex-2">
                <input type="number" v-model="ans.scoreValue" placeholder="Điểm" class="flex-1 text-center">
                <select v-model="ans.tagId" class="flex-1">
                  <option value="" disabled>-- Loại Da --</option>
                  <option v-for="tag in availableTags" :key="tag.id" :value="tag.id">{{ tag.name }}</option>
                </select>
                <button class="btn-remove-ans" @click="removeAnswer(aIndex)" title="Xóa đáp án">✖</button>
              </div>
              <button class="btn-text" @click="addAnswer">+ Thêm đáp án</button>
            </div>

            <div class="action-footer">
              <button class="btn-cancel" @click="cancelEdit">Hủy</button>
              <button class="btn-save" @click="saveQuestion">Lưu thay đổi</button>
            </div>
          </div>

          <div v-else class="view-mode">
            <div class="box-header">
              <h4 class="drag-handle">⠿ Câu {{ index + 1 }}</h4>
              <div class="actions">
               <label class="required-check">
                   <input type="checkbox" v-model="q.batBuoc" @change="saveQuestionInline(q)"> Bắt buộc
               </label>
                <button class="btn-icon" @click="startEdit(q)" title="Sửa câu này">✏️</button>
                <button class="btn-icon delete" @click="deleteQuestion(q.id)" title="Xóa">🗑️</button>
              </div>
            </div>
            <div class="box-body">
              <p class="q-title-display">{{ q.title }}</p>
              <div class="ans-badges-preview">
                <span v-for="(ans, aIndex) in q.answers" :key="aIndex" class="preview-badge">
                  {{ ans.label }} ({{ ans.scoreValue }}đ)
                </span>
              </div>
            </div>
          </div>

        </div>

        <div v-if="editingId === 'new'" class="question-box edit-mode new-box">
          <div class="box-header edit-header">
            <h4>Thêm Câu Hỏi Mới</h4>
          </div>
          <div class="form-group">
            <input type="text" v-model="currentQuestion.title" class="input-title-edit" placeholder="Nhập câu hỏi mới...">
          </div>
          <div class="answers-edit-list">
            <div v-for="(ans, aIndex) in currentQuestion.answers" :key="aIndex" class="inline-answer-row">
              <input type="text" v-model="ans.label" placeholder="Nội dung đáp án" class="flex-2">
              <input type="number" v-model="ans.scoreValue" placeholder="Điểm" class="flex-1 text-center">
              <select v-model="ans.tagId" class="flex-1">
                <option value="" disabled>-- Chọn --</option>
                <option v-for="tag in availableTags" :key="tag.id" :value="tag.id">{{ tag.name }}</option>
              </select>
              <button class="btn-remove-ans" @click="removeAnswer(aIndex)">✖</button>
            </div>
            <button class="btn-text" @click="addAnswer">+ Thêm đáp án</button>
          </div>
          <div class="action-footer">
            <button class="btn-cancel" @click="cancelEdit">Hủy</button>
            <button class="btn-save" @click="saveQuestion">Lưu câu hỏi</button>
          </div>
        </div>

        <div class="add-btn-wrapper" v-if="editingId !== 'new'">
          <button class="btn-add-outline" @click="addNewInline">
            + Thêm câu hỏi trắc nghiệm
          </button>
        </div>

      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';

const questions = ref([]);
const editingId = ref(null); 
const currentQuestion = ref(null);

const availableTags = ref([
  { id: 1, name: 'Da Dầu' },      
  { id: 2, name: 'Da Khô' },
  { id: 3, name: 'Da Hỗn Hợp' },
  { id: 4, name: 'Da Thường' },
  { id: 5, name: 'Da Nhạy Cảm' },
]);

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

onMounted(() => loadQuestions());

const startEdit = (q) => {
  editingId.value = q.id;
  currentQuestion.value = JSON.parse(JSON.stringify(q));
};

const addNewInline = () => {
  editingId.value = 'new';
  currentQuestion.value = {
    id: null,
    title: '',
    answers: [
      { label: '', tagId: '', scoreValue: 10 },
      { label: '', tagId: '', scoreValue: 10 }
    ]
  };
};

const cancelEdit = () => {
  editingId.value = null;
  currentQuestion.value = null;
};

const addAnswer = () => { currentQuestion.value.answers.push({ label: '', tagId: '', scoreValue: 10 }); };
const removeAnswer = (index) => { currentQuestion.value.answers.splice(index, 1); };

const saveQuestion = async () => {
  if (!currentQuestion.value.title.trim()) { alert("Vui lòng nhập câu hỏi!"); return; }
  if (currentQuestion.value.answers.length < 2) { alert("Cần ít nhất 2 đáp án!"); return; }
  
  try {
    if (editingId.value !== 'new') {
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
    cancelEdit();
    loadQuestions(); 
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
.admin-quiz-manager { 
  background-color: #f3f4f6; 
  padding: 40px 20px; 
  min-height: 100vh; 
  font-family: 'Inter', 'Helvetica Neue', sans-serif; 
  display: flex;
  justify-content: center;
}

.builder-container {
  background-color: #ffffff; 
  width: 100%;
  max-width: 850px;
  border-radius: 12px;
  padding: 40px;
  box-shadow: 0 10px 30px rgba(0,0,0,0.04);
}

.header-section { margin-bottom: 30px; }
.page-title { font-size: 24px; font-weight: 700; color: #111; margin: 0 0 8px 0; }
.subtitle { font-size: 14px; color: #6b7280; margin: 0; }

.section-title { background: #f9fafb; padding: 12px 16px; border-radius: 8px; margin-bottom: 24px; font-size: 14px; font-weight: 600; color: #374151; }

.question-box {
  border: 1px solid #e5e7eb;
  border-radius: 10px;
  margin-bottom: 20px; 
  background: #ffffff;
  transition: all 0.2s;
}
.question-box:hover { border-color: #d1d5db; box-shadow: 0 4px 12px rgba(0,0,0,0.02); }

.box-header { display: flex; justify-content: space-between; align-items: center; padding: 16px 20px; border-bottom: 1px solid #f3f4f6; }
.drag-handle { margin: 0; font-size: 14px; font-weight: 600; color: #111; display: flex; align-items: center; gap: 8px; cursor: grab; }
.drag-handle::before { color: #9ca3af; }

.actions { display: flex; align-items: center; gap: 12px; }
.required-check { font-size: 13px; color: #4b5563; display: flex; align-items: center; gap: 6px; }
.btn-icon { background: none; border: none; cursor: pointer; color: #6b7280; font-size: 14px; opacity: 0.7; transition: 0.2s; }
.btn-icon:hover { opacity: 1; color: #111; }
.btn-icon.delete:hover { color: #ef4444; }

.box-body { padding: 20px; }
.q-title-display { font-size: 15px; color: #1f2937; margin: 0 0 16px 0; font-weight: 500; }
.ans-badges-preview { display: flex; flex-wrap: wrap; gap: 8px; }
.preview-badge { background: #f3f4f6; color: #4b5563; font-size: 12px; padding: 6px 12px; border-radius: 20px; border: 1px solid #e5e7eb; }

.edit-mode { border: 1px solid #3b82f6; box-shadow: 0 0 0 4px rgba(59, 130, 246, 0.1); }
.edit-header h4 { margin: 0; color: #2563eb; font-size: 14px; }
.input-title-edit { width: 100%; border: none; border-bottom: 2px solid #e5e7eb; padding: 12px 20px; font-size: 16px; outline: none; transition: 0.2s; background: transparent; }
.input-title-edit:focus { border-bottom-color: #3b82f6; }

.answers-edit-list { padding: 20px; background: #fafafa; border-bottom-left-radius: 10px; border-bottom-right-radius: 10px; }
.label-sm { font-size: 13px; font-weight: 600; color: #4b5563; margin-top: 0; margin-bottom: 12px; }

.inline-answer-row { display: flex; gap: 10px; margin-bottom: 12px; align-items: center; }
.inline-answer-row input, .inline-answer-row select { padding: 10px 12px; border: 1px solid #d1d5db; border-radius: 6px; font-size: 13px; outline: none; }
.inline-answer-row input:focus, .inline-answer-row select:focus { border-color: #3b82f6; }
.flex-2 { flex: 2; } .flex-1 { flex: 1; }
.text-center { text-align: center; }

.btn-remove-ans { background: #fee2e2; color: #ef4444; border: none; width: 32px; height: 32px; border-radius: 6px; cursor: pointer; }
.btn-remove-ans:hover { background: #fecaca; }
.btn-text { background: none; border: none; color: #3b82f6; font-size: 13px; font-weight: 600; cursor: pointer; padding: 0; margin-top: 8px; }

.action-footer { display: flex; justify-content: flex-end; gap: 12px; padding: 16px 20px; border-top: 1px solid #e5e7eb; background: #fff; border-bottom-left-radius: 10px; border-bottom-right-radius: 10px; }
.btn-cancel { background: white; border: 1px solid #d1d5db; padding: 8px 16px; border-radius: 6px; cursor: pointer; font-weight: 500; color: #374151; }
.btn-save { background: #111827; color: white; border: none; padding: 8px 16px; border-radius: 6px; cursor: pointer; font-weight: 500; }
.btn-save:hover { background: #374151; }

.add-btn-wrapper { text-align: center; margin-top: 20px; }
.btn-add-outline { background: transparent; border: 2px dashed #d1d5db; color: #6b7280; width: 100%; padding: 16px; border-radius: 8px; font-size: 14px; font-weight: 600; cursor: pointer; transition: 0.2s; }
.btn-add-outline:hover { border-color: #9ca3af; color: #374151; background: #f9fafb; }
</style>