<!-- < <template>
  <div class="sunova-quiz-wrapper">
    
    <div class="quiz-header">
      <div class="progress-container">
        <button 
          v-if="currentStep > 0 && currentStep <= totalSteps" 
          class="nav-btn-round" 
          @click="prevStep"
          title="Quay lại"
        >
          &larr;
        </button>
        <div v-else class="nav-placeholder"></div>

        <div class="progress-bar-track">
          <div 
            class="progress-bar-fill" 
            :style="{ width: progressPercentage + '%' }"
          >
            <div class="progress-icon">☀️</div> 
          </div>
        </div>
      </div>
    </div>

    <transition name="fade-slide" mode="out-in">
      
      <div v-if="currentStep === 0" class="step-container welcome-step" key="welcome">
        <h4 class="brand-subtitle">SUNOVA</h4>
        <h1 class="quiz-title">KHÁM PHÁ ROUTINE CHỐNG NẮNG CỦA BẠN</h1>
        <p class="quiz-desc">Trả lời 5 câu hỏi ngắn để chúng tôi phân tích làn da và gợi ý bộ sản phẩm bảo vệ da hoàn hảo nhất dành riêng cho bạn.</p>
        <button class="cream-btn large-btn" @click="nextStep">BẮT ĐẦU NGAY</button>
      </div>

      <div v-else-if="currentStep <= totalSteps" class="step-container" :key="currentStep">
        <p class="step-count">Câu {{ currentStep }} / {{ totalSteps }}</p>
        <h2 class="question-title">{{ currentQuestion.title }}</h2>

        <div class="answer-grid">
          <div 
            v-for="(answer, index) in currentQuestion.answers" 
            :key="index"
            class="answer-card"
            :class="{ 'selected': selectedAnswers[currentQuestion.id] === answer.value }"
            @click="selectAnswer(currentQuestion.id, answer.value)"
          >
            <div class="icon-wrapper">{{ answer.icon }}</div>
            <h3 class="answer-label">{{ answer.label }}</h3>
            <p v-if="answer.desc" class="answer-desc">{{ answer.desc }}</p>
          </div>
        </div>

        <div class="navigation-footer">
          <button 
            class="cream-btn next-btn" 
            :disabled="!isCurrentAnswered" 
            @click="handleNextButtonClick"
          >
            {{ isLastStep ? 'XEM KẾT QUẢ & GỢI Ý ROUTINE' : 'TIẾP THEO &rarr;' }}
          </button>
        </div>
      </div>

      <div v-else class="step-container welcome-step" key="calculating">
        <div class="loader-icon">☀️</div>
        <h2 class="quiz-title">ĐANG PHÂN TÍCH...</h2>
        <p class="quiz-desc">SUNOVA đang dựa trên câu trả lời của bạn để xây dựng Routine chống nắng tối ưu nhất. Đợi một chút nhé!</p>
      </div>

    </transition>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue';
import { useRouter } from 'vue-router'; // ĐÃ THÊM: Import thư viện chuyển trang

// Khởi tạo router để có thể chuyển trang
const router = useRouter(); 

// --- CẤU HÌNH DỮ LIỆU & TRẠNG THÁI ---
const currentStep = ref(0);
const totalSteps = 5; 
const selectedAnswers = ref({}); 

// --- BỘ CÂU HỎI QUIZ ---
const questions = [
  {
    id: 'skin_type',
    title: "Làn da của bạn thường cảm thấy thế nào khoảng 1 tiếng sau khi rửa mặt?",
    answers: [
      { label: "Bóng nhờn", desc: "Dầu thừa đổ nhiều khắp mặt", icon: "💧", value: "oily" },
      { label: "Khô căng", desc: "Cảm giác thiếu ẩm, hơi bong tróc", icon: "🌵", value: "dry" },
      { label: "Hỗn hợp", desc: "Dầu vùng chữ T, khô vùng má", icon: "🌗", value: "combination" },
      { label: "Bình thường", desc: "Cân bằng, không quá dầu hay khô", icon: "✨", value: "normal" }
    ]
  },
  {
    id: 'primary_concern',
    title: "Mối quan tâm lớn nhất của bạn về làn da là gì? (Chọn 1)",
    answers: [
      { label: "Mụn & Lỗ chân lông", icon: "🔴", value: "acne" },
      { label: "Lão hóa & Nếp nhăn", icon: "🕰️", value: "aging" },
      { label: "Thâm nám & Đều màu", icon: "🎯", value: "pigmentation" },
      { label: "Dưỡng ẩm sâu", icon: "🌊", value: "hydration" }
    ]
  },
  {
    id: 'sensitivity',
    title: "Da bạn có dễ bị kích ứng, mẩn đỏ khi dùng mỹ phẩm mới không?",
    answers: [
      { label: "Rất thường xuyên", icon: "🚨", value: "high" },
      { label: "Thỉnh thoảng", icon: "⚠️", value: "medium" },
      { label: "Hiếm khi", icon: "✅", value: "low" }
    ]
  },
  {
    id: 'finish_preference',
    title: "Bạn thích lớp nền (finish) sau khi bôi chống nắng trông như thế nào?",
    answers: [
      { label: "Tự nhiên (Natural)", desc: "Như không bôi gì", icon: "🌿", value: "natural" },
      { label: "Căng bóng (Glowy)", desc: "Kiểu Hàn Quốc căng mọng", icon: "💎", value: "glowy" },
      { label: "Lì (Matte)", desc: "Ráo mịn, kiềm dầu", icon: "🧱", value: "matte" },
      { label: "Nâng tone (Tinted)", desc: "Che khuyết điểm nhẹ nhàng", icon: "🎨", value: "tinted" }
    ]
  },
  {
    id: 'daily_activity',
    title: "Thói quen sinh hoạt hàng ngày của bạn ra sao?",
    answers: [
      { label: "Văn phòng", desc: "Ngồi máy tính nhiều, ít ra nắng", icon: "💻", value: "indoors" },
      { label: "Di chuyển nhiều", desc: "Thường xuyên ngoài trời", icon: "🚗", value: "outdoors" },
      { label: "Thể thao/Bơi lội", desc: "Cần kháng nước, mồ hôi", icon: "🏊‍♀️", value: "sport" }
    ]
  }
];

// --- LOGIC TÍNH TOÁN ---
const currentQuestion = computed(() => questions[currentStep.value - 1]);
const progressPercentage = computed(() => (currentStep.value / totalSteps) * 100);
const isLastStep = computed(() => currentStep.value === totalSteps);

const isCurrentAnswered = computed(() => {
  if (currentStep.value > 0 && currentStep.value <= totalSteps) {
    return selectedAnswers.value[currentQuestion.value.id] !== undefined;
  }
  return false;
});

// --- HÀM XỬ LÝ ĐIỀU HƯỚNG ---
const nextStep = () => { if (currentStep.value <= totalSteps) currentStep.value++; };
const prevStep = () => { if (currentStep.value > 0) currentStep.value--; };

const selectAnswer = (questionId, value) => {
  selectedAnswers.value[questionId] = value;
};

const handleNextButtonClick = () => {
  if (isLastStep.value) {
    nextStep();
    submitQuiz(); // Gọi hàm submit khi ở bước cuối
  } else {
    nextStep();
  }
};

const submitQuiz = () => {
  // Hiệu ứng delay 2 giây để giả vờ đang phân tích dữ liệu
  setTimeout(() => {
    // ĐÃ FIX: Lệnh chuyển trang sang file QuizResult.vue hoạt động ngay lập tức
    router.push({ name: 'QuizResult' }); 
  }, 2000);
};
</script>

<style scoped>
/* CSS DESIGN SYSTEM - TONE MÀU TRẮNG SỮA (SUNOVA CREAM) */
.sunova-quiz-wrapper {
  --sunova-cream: #f5ece3; 
  --sunova-cream-light: #fff; 
  --sunova-cream-hover: #eae0d5; 
  --sunova-dark: #1a1412; 
  --sunova-card: #2a201b; 
  --sunova-text-muted: #a99d96; 

  background-color: var(--sunova-dark);
  color: var(--sunova-cream-light);
  min-height: 100vh;
  padding: 40px 20px;
  font-family: 'Avenir', 'Helvetica Neue', Helvetica, Arial, sans-serif; 
  display: flex;
  flex-direction: column;
  align-items: center;
}

.quiz-header { width: 100%; max-width: 900px; margin-bottom: 50px; }
.progress-container { display: flex; align-items: center; justify-content: space-between; gap: 20px; }
.nav-btn-round {
  background: none; border: 1px solid var(--sunova-cream-muted, #3a2e28); border-radius: 50%;
  width: 44px; height: 44px; font-size: 20px; cursor: pointer; color: var(--sunova-cream);
  display: flex; align-items: center; justify-content: center; transition: all 0.3s;
}
.nav-btn-round:hover { background: #3a2e28; }
.nav-placeholder { width: 44px; }

.progress-bar-track {
  flex-grow: 1; height: 8px; background: #3a2e28; border-radius: 10px; position: relative; overflow: visible; 
}
.progress-bar-fill {
  height: 100%; background: var(--sunova-cream); border-radius: 10px; transition: width 0.4s ease-out; position: relative;
}
.progress-icon {
  position: absolute; top: -12px; right: -12px; font-size: 26px; transform: rotate(0deg); transition: right 0.4s ease-out, transform 0.4s ease-out;
}
.progress-bar-fill:hover .progress-icon { transform: rotate(45deg); }

.step-container { max-width: 900px; width: 100%; text-align: center; display: flex; flex-direction: column; align-items: center; }

.brand-subtitle { letter-spacing: 2px; color: var(--sunova-cream); font-weight: 300; margin-bottom: 10px;}
.quiz-title { font-size: 44px; font-weight: 300; margin-bottom: 20px; line-height: 1.2;}
.question-title { font-size: 36px; font-weight: 400; margin-bottom: 10px; color: #fff;}
.step-count { font-size: 14px; color: var(--sunova-cream); letter-spacing: 1px; margin-bottom: 20px;}
.quiz-desc { color: var(--sunova-text-muted); margin-bottom: 40px; font-size: 16px; line-height: 1.6;}

.cream-btn {
  background-color: var(--sunova-cream); color: var(--sunova-dark); padding: 18px 40px;
  font-size: 16px; font-weight: bold; border: none; border-radius: 8px; cursor: pointer; letter-spacing: 1px; transition: background-color 0.3s;
}
.cream-btn:hover:not(:disabled) { background-color: var(--sunova-cream-hover); }
.cream-btn:disabled { opacity: 0.4; cursor: not-allowed; }
.large-btn { padding: 20px 60px; font-size: 18px; }

.answer-grid { display: flex; justify-content: center; gap: 20px; flex-wrap: wrap; margin-bottom: 60px; width: 100%; }
.answer-card {
  background-color: var(--sunova-card); border: 1px solid #3a2e28; border-radius: 12px;
  padding: 40px 20px; width: 250px; cursor: pointer; transition: transform 0.2s, border-color 0.2s;
  display: flex; flex-direction: column; align-items: center; justify-content: center;
}
.answer-card:hover:not(.selected) { transform: translateY(-5px); border-color: #5c473b; }
.answer-card.selected { border: 2px solid var(--sunova-cream); }

.icon-wrapper { font-size: 50px; margin-bottom: 20px;}
.answer-label { font-size: 20px; font-weight: 500; margin-bottom: 10px; color: #fff;}
.answer-desc { font-size: 14px; color: var(--sunova-text-muted); line-height: 1.5; margin: 0;}

.navigation-footer { width: 100%; max-width: 900px; margin-top: 40px; display: flex; justify-content: flex-end; }
.next-btn { padding: 18px 40px; font-size: 16px; border-radius: 8px; }

.fade-slide-enter-active, .fade-slide-leave-active { transition: all 0.4s; }
.fade-slide-enter-from { opacity: 0; transform: translateX(20px); }
.fade-slide-leave-to { opacity: 0; transform: translateX(-20px); }

.loader-icon { font-size: 80px; margin-bottom: 30px; animation: spin 2s linear infinite; }
@keyframes spin { from { transform: rotate(0deg); } to { transform: rotate(360deg); } }
</style> -->