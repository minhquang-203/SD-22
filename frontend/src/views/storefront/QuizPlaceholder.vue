<template>
  <div class="sg-quiz-root">

    <transition name="modal-fade">
      <div v-if="showExitModal" class="sunova-modal-overlay">
        <div class="sunova-modal">
          <div class="modal-icon custom-warning">
            <svg width="72" height="72" viewBox="0 0 100 100" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M30 35 L20 25" stroke="#E55B5B" stroke-width="5" stroke-linecap="round"/>
              <path d="M22 50 L10 50" stroke="#E55B5B" stroke-width="5" stroke-linecap="round"/>
              <path d="M30 65 L20 75" stroke="#E55B5B" stroke-width="5" stroke-linecap="round"/>
              <path d="M70 35 L80 25" stroke="#E55B5B" stroke-width="5" stroke-linecap="round"/>
              <path d="M78 50 L90 50" stroke="#E55B5B" stroke-width="5" stroke-linecap="round"/>
              <path d="M70 65 L80 75" stroke="#E55B5B" stroke-width="5" stroke-linecap="round"/>
              <path d="M50 20 C46 20 43 22 41 26 L19 66 C16 71 19 78 25 78 L75 78 C81 78 84 71 81 66 L59 26 C57 22 54 20 50 20 Z" fill="#E55B5B" stroke="#1A1412" stroke-width="4" stroke-linejoin="round"/>
              <path d="M50 36 L50 54" stroke="#1A1412" stroke-width="10" stroke-linecap="round"/>
              <path d="M50 36 L50 54" stroke="#FFFFFF" stroke-width="5" stroke-linecap="round"/>
              <circle cx="50" cy="66" r="4.5" fill="#1A1412"/>
              <circle cx="50" cy="66" r="2.5" fill="#FFFFFF"/>
            </svg>
          </div>
          <h3 class="modal-title">Bạn muốn dừng lại?</h3>
          <p class="modal-desc">
            Quá trình phân tích da đang diễn ra. Nếu bạn chuyển sang trang khác lúc này, tiến trình và kết quả làm bài sẽ bị mất.
          </p>
          <div class="modal-actions">
            <button class="modal-btn btn-cancel" @click="cancelExit">TIẾP TỤC QUIZ</button>
            <button class="modal-btn btn-confirm" @click="executeExit">THOÁT QUIZ</button>
          </div>
        </div>
      </div>
    </transition>

    <div v-if="!quizStarted" class="sg-landing">
      <div class="sg-landing__content">
        <div class="sg-landing__visual">
          <div class="sg-landing__circle"></div>
          <div class="sg-landing__product">SPF</div> 
        </div>

        <div class="sg-landing__text">
          <p class="sg-landing__brand">SUNOVA</p>
          <h1 class="sg-landing__title">TÌM SẢN PHẨM<br/>CHỐNG NẮNG<br/>YÊU THÍCH MỚI</h1>
          <p class="sg-landing__desc">
            Sản phẩm chống nắng hoàn hảo chỉ cách bạn vài câu hỏi! Hệ thống SUNOVA sẽ phân tích và gợi ý sản phẩm phù hợp nhất với làn da bạn.
          </p>
          <button class="sg-landing__cta" @click="startQuiz" :disabled="loading">
            {{ loading ? 'ĐANG TẢI...' : 'BẮT ĐẦU QUIZ' }}
          </button>
        </div>
      </div>

    </div>

    <div v-else class="sg-quiz">

      <div class="sg-quiz__header">
        <div class="sg-progress">
          <button
            v-if="currentStep > 1 && !analyzing && !showResult"
            class="sg-progress__back"
            @click="prevStep"
            title="Quay lại"
          >
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M19 12H5M12 19l-7-7 7-7"/></svg>
          </button>
          <div v-else class="sg-progress__back-placeholder"></div>

          <div class="sg-progress__track">
            <div class="sg-progress__fill" :style="{ width: progressPercentage + '%' }">
              <div class="sg-progress__sun">☀️</div>
            </div>
          </div>
        </div>
      </div>

      <transition name="sg-slide" mode="out-in">

        <div v-if="!analyzing && !showResult" class="sg-question" :key="currentStep">

          <div class="sg-question__layout" :class="{ 'has-quote': currentQuote }">

            <div v-if="currentQuote" class="sg-question__quote-col">
              <blockquote class="sg-quote">
                <p class="sg-quote__text">"{{ currentQuote.text }}"</p>
                <footer class="sg-quote__author">
                  <strong>{{ currentQuote.author }}</strong>
                  <span>{{ currentQuote.role }}</span>
                </footer>
              </blockquote>
            </div>

            <div class="sg-question__main">
              <h2 class="sg-question__title">{{ currentQuestion.title }}</h2>
              <p class="sg-question__hint">Chọn một đáp án.</p>

              <div class="sg-answers" :class="answerLayoutClass">
                <div
                  v-for="(answer, index) in currentQuestion.answers"
                  :key="index"
                  class="sg-answer-card"
                  :class="{ 'sg-answer-card--selected': isSelected(answer) }"
                  @click="selectAnswer(answer)"
                >
                  <h3 class="sg-answer-card__label">{{ answer.label }}</h3>
                </div>
              </div>

              <div class="sg-question__footer">
                <button
                  class="sg-btn-next"
                  :disabled="!isCurrentAnswered"
                  @click="handleNext"
                >
                  {{ isLastStep ? 'XEM KẾT QUẢ' : 'TIẾP THEO' }}
                </button>
              </div>
            </div>
          </div>

          <div class="sg-why" v-if="currentWhyWeAsk">
            <div class="sg-why__badge">?</div>
            <div class="sg-why__card">
              <h4 class="sg-why__title">TẠI SAO CHÚNG TÔI HỎI</h4>
              <p class="sg-why__text">{{ currentWhyWeAsk }}</p>
            </div>
          </div>

        </div>

        <div v-else-if="analyzing" class="sg-analyzing" key="analyzing">
          <div class="sg-analyzing__spinner">☀️</div>
          <h2 class="sg-analyzing__title">Đang phân tích làn da của bạn...</h2>
          <p class="sg-analyzing__desc">SUNOVA đang dựa trên câu trả lời để tìm sản phẩm phù hợp nhất.</p>
          <div class="sg-analyzing__dots"><span></span><span></span><span></span></div>
        </div>

        <div v-else class="sg-result" key="result">
          <div class="sg-result__hero">
            <h2 class="sg-result__title">KẾT QUẢ PHÂN TÍCH DA CỦA BẠN</h2>
          </div>

          <div class="sg-result__skin-card">
            <h3 class="sg-result__skin-name">{{ resultData.skinName }}</h3>
            <p class="sg-result__skin-desc">{{ resultData.description }}</p>
          </div>

          <div class="sg-result__scores">
            <h4 class="sg-result__scores-title">Phân bố điểm loại da</h4>
            <div class="sg-result__score-list">
              <div v-for="score in sortedScores" :key="score.id" class="sg-score-row">
                <span class="sg-score-row__label">{{ score.name }}</span>
                <div class="sg-score-row__bar">
                  <div class="sg-score-row__fill" :style="{ width: score.percent + '%' }"></div>
                </div>
                <span class="sg-score-row__value">{{ score.points }}đ</span>
              </div>
            </div>
          </div>

          <div class="sg-result__products" v-if="recommendedProducts.length > 0">
            <h3 class="sg-result__products-title">Sản phẩm được gợi ý cho bạn</h3>
            <div class="sg-product-grid">
              <div
                v-for="product in recommendedProducts"
                :key="product.id"
                class="sg-product-card"
                @click="goToProduct(product.id)"
              >
                <div class="sg-product-card__img">
                  <img v-if="product.hinhAnh" :src="getImageUrl(product.hinhAnh)" :alt="product.ten" />
                  <div v-else class="sg-product-card__placeholder">SUNOVA</div>
                </div>
                <h4 class="sg-product-card__name">{{ product.ten }}</h4>
                <p class="sg-product-card__price" :class="{ 'price-cta': !product.giaMin && !product.gia }">
                  {{ (product.giaMin || product.gia) ? formatPrice(product.giaMin || product.gia) : 'Khám phá ngay ➔' }}
                </p>
              </div>
            </div>
          </div>

          <div class="sg-result__actions">
            <button class="sg-btn-primary" @click="retakeQuiz">LÀM LẠI QUIZ</button>
            <button class="sg-btn-outline" @click="goToProducts">KHÁM PHÁ TẤT CẢ SẢN PHẨM</button>
          </div>
        </div>

      </transition>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount } from 'vue';
import { useRouter, onBeforeRouteLeave } from 'vue-router';

// CHUẨN KIẾN TRÚC GỌI API (Đã thay đổi theo hướng dẫn đồ án)

import { getProducts } from '@/api/sanPhamApi';
import { getQuizQuestions } from '@/api/quizApi';

const router = useRouter();

// ============================================
// STATE
// ============================================
const quizStarted = ref(false);
const loading = ref(true);
const analyzing = ref(false);
const showResult = ref(false);
const showExitModal = ref(false);
const currentStep = ref(1);
const questions = ref([]);
const selectedAnswers = ref({});
const resultData = ref({ skinName: '', description: '' });
const scoreMap = ref({});
const recommendedProducts = ref([]);
const allProducts = ref([]);

// Biến điều khiển Router Guard
const pendingNavigation = ref(null);
const allowLeave = ref(false);

const LOAI_DA_INFO = {
  1: { name: 'Da Dầu', desc: 'Da bạn tiết nhiều dầu, dễ bóng nhờn. Nên chọn sản phẩm chống nắng dạng gel, kiềm dầu, kết cấu mỏng nhẹ.' },
  2: { name: 'Da Khô', desc: 'Da bạn thiếu ẩm, dễ bong tróc. Nên chọn sản phẩm chống nắng dưỡng ẩm sâu, dạng kem đặc.' },
  3: { name: 'Da Hỗn Hợp', desc: 'Da bạn vừa dầu vừa khô theo vùng. Nên chọn sản phẩm chống nắng cân bằng, không quá đặc cũng không quá loãng.' },
  4: { name: 'Da Nhạy Cảm', desc: 'Da bạn dễ kích ứng, mẩn đỏ. Nên chọn sản phẩm chống nắng vật lý (mineral), lành tính, không cồn.' },
  5: { name: 'Da Thường', desc: 'Da bạn cân bằng, khỏe mạnh. Bạn có thể dùng hầu hết các loại chống nắng, hãy chọn theo sở thích!' },
};

const WHY_WE_ASK = [
  'Hiểu về tình trạng da giúp chúng tôi chọn được kết cấu sản phẩm (gel, kem, sữa) phù hợp nhất với bạn.',
  'Mỗi loại da phản ứng khác nhau với tia UV. Thông tin này giúp chúng tôi xác định chỉ số SPF lý tưởng.',
  'Mức độ nhạy cảm của da quyết định loại màng lọc UV (vật lý hay hóa học) phù hợp với bạn.',
  'Sở thích về kết cấu sản phẩm rất quan trọng — để bạn thực sự muốn dùng chống nắng mỗi ngày!',
  'Hoạt động hàng ngày ảnh hưởng đến mức độ tiếp xúc UV và khả năng kháng nước cần thiết.',
  'Chúng tôi có hơn 20 công thức khác nhau, thông tin này giúp chọn đúng sản phẩm cho bạn.',
  'Vùng sử dụng (mặt hay body) cần công thức khác nhau để bảo vệ tối ưu.',
  'Thói quen sử dụng giúp chúng tôi gợi ý sản phẩm có kết cấu và mức bảo vệ phù hợp nhất.',
  'Môi trường sống ảnh hưởng trực tiếp đến loại bảo vệ UV bạn cần mỗi ngày.',
  'Thông tin thêm giúp hệ thống AI phân tích chính xác hơn cho gợi ý cá nhân hóa.',
];

const QUOTES = [
  null,
  null,
  { text: 'Chống nắng là bước skincare quan trọng nhất. Mỗi ngày. Không ngoại lệ.', author: 'SUNOVA', role: 'Triết lý thương hiệu' },
  null,
  { text: 'Chúng tôi tạo ra chống nắng cho mọi dịp, để bạn có thể ra ngoài và sống trọn từng khoảnh khắc tươi sáng nhất.', author: 'SUNOVA TEAM', role: 'Đội ngũ phát triển' },
  null,
  { text: 'Làn da khỏe mạnh bắt đầu từ việc bảo vệ khỏi tia UV đúng cách, phù hợp với chính bạn.', author: 'CHUYÊN GIA DA LIỄU', role: 'Cố vấn SUNOVA' },
];

// COMPUTED

const totalSteps = computed(() => questions.value.length);
const currentQuestion = computed(() => questions.value[currentStep.value - 1] || { title: '', answers: [] });
const progressPercentage = computed(() => totalSteps.value > 0 ? (currentStep.value / totalSteps.value) * 100 : 0);
const isLastStep = computed(() => currentStep.value === totalSteps.value);

// Kiểm tra xem khách có đang ở màn hình Quiz không
const isQuizInProgress = computed(() => quizStarted.value && !showResult.value);

const isCurrentAnswered = computed(() => {
  const q = currentQuestion.value;
  return q && selectedAnswers.value[q.id] !== undefined;
});

const answerLayoutClass = computed(() => {
  const count = currentQuestion.value?.answers?.length || 0;
  if (count <= 2) return 'sg-answers--2';
  if (count <= 3) return 'sg-answers--3';
  if (count <= 4) return 'sg-answers--4';
  return 'sg-answers--list';
});

const currentWhyWeAsk = computed(() => WHY_WE_ASK[(currentStep.value - 1) % WHY_WE_ASK.length] || null);
const currentQuote = computed(() => QUOTES[currentStep.value - 1] || null);

const sortedScores = computed(() => {
  return Object.entries(scoreMap.value)
    .map(([id, data]) => ({ id: Number(id), name: data.name, points: data.points, percent: 0 }))
    .map((item, _, arr) => {
      const maxPoints = Math.max(...arr.map(a => a.points), 1);
      item.percent = Math.round((item.points / maxPoints) * 100);
      return item;
    })
    .sort((a, b) => b.points - a.points);
});

// NAVIGATION GUARDS - BẮT SỰ KIỆN CHUYỂN TRANG

onBeforeRouteLeave((to, from, next) => {
  if (isQuizInProgress.value && !allowLeave.value) {
    pendingNavigation.value = to;
    showExitModal.value = true;
    next(false);
  } else {
    next();
  }
});

const handleBeforeUnload = (e) => {
  if (isQuizInProgress.value) {
    e.preventDefault();
    e.returnValue = '';
  }
};

onMounted(() => {
  window.addEventListener('beforeunload', handleBeforeUnload);
  fetchQuizQuestions();
  fetchProducts();
});

onBeforeUnmount(() => {
  window.removeEventListener('beforeunload', handleBeforeUnload);
});

const cancelExit = () => {
  showExitModal.value = false;
  pendingNavigation.value = null;
};

const executeExit = () => {
  showExitModal.value = false;
  allowLeave.value = true; 
  const target = pendingNavigation.value?.fullPath || '/';
  pendingNavigation.value = null;
  router.push(target);
};

// ============================================
// FETCH DATA
// ============================================
const fetchQuizQuestions = async () => {
  loading.value = true;
  try {
    const res = await getQuizQuestions();
    questions.value = res.data || [];
  } catch (error) {
    console.error('Lỗi tải quiz:', error);
    questions.value = [];
  } finally {
    loading.value = false;
  }
};

const fetchProducts = async () => {
  try {
    const res = await getProducts();
    allProducts.value = (res.data || []).filter(p => p.trangThai !== false);
  } catch (error) {
    console.error('Lỗi tải sản phẩm:', error);
  }
};

// ============================================
// QUIZ LOGIC
// ============================================
const startQuiz = () => {
  if (questions.value.length === 0) return;
  quizStarted.value = true;
  currentStep.value = 1;
  allowLeave.value = false; 
};

const isSelected = (answer) => {
  const q = currentQuestion.value;
  const selected = selectedAnswers.value[q.id];
  return selected && selected.label === answer.label;
};

const selectAnswer = (answer) => { 
  selectedAnswers.value[currentQuestion.value.id] = answer; 
};

const prevStep = () => { if (currentStep.value > 1) currentStep.value--; };

const handleNext = () => {
  if (isLastStep.value) {
    currentStep.value++;
    analyzing.value = true;
    calculateResult();
  } else {
    currentStep.value++;
  }
};

const calculateResult = () => {
  const scores = {};
  Object.values(selectedAnswers.value).forEach(answer => {
    const tagId = answer.tagId;
    const points = answer.scoreValue || 0;
    if (tagId) {
      if (!scores[tagId]) {
        const info = LOAI_DA_INFO[tagId] || { name: `Loại da #${tagId}` };
        scores[tagId] = { name: info.name, points: 0 };
      }
      scores[tagId].points += points;
    }
  });
  scoreMap.value = scores;

  let topId = null, topPoints = 0;
  Object.entries(scores).forEach(([id, data]) => {
    if (data.points > topPoints) { topPoints = data.points; topId = Number(id); }
  });

  const info = LOAI_DA_INFO[topId] || { name: 'Da Thường', desc: 'Kết quả phân tích chung.' };
  resultData.value = { skinName: info.name, description: info.desc };
  recommendProducts(topId);

  setTimeout(() => { analyzing.value = false; showResult.value = true; }, 2500);
};

const recommendProducts = (topId) => {
  let candidates = allProducts.value;
  
  if (topId) {
    // Lọc các sản phẩm có chứa ID loại da tương ứng (idLoaiDas là mảng ID loại da của sản phẩm)
    const suitableProducts = allProducts.value.filter(p => 
      p.idLoaiDas && p.idLoaiDas.includes(topId)
    );
    
    // Nếu có sản phẩm phù hợp, ưu tiên dùng sản phẩm đó
    if (suitableProducts.length > 0) {
      candidates = suitableProducts;
    }
  }

  // Xáo trộn ngẫu nhiên danh sách đã lọc
  const shuffled = [...candidates].sort(() => 0.5 - Math.random());
  recommendedProducts.value = shuffled.slice(0, 4);
};

// ============================================
// NAVIGATION HELPERS
// ============================================
const formatPrice = (p) => p ? Math.round(p).toLocaleString('vi-VN') + ' đ' : '0 đ';
const getImageUrl = (path) => { if (!path) return ''; return path.startsWith('http') ? path : `/uploads/${path}`; };

const goToProducts = () => {
  allowLeave.value = true;
  router.push('/san-pham');
};

const goToProduct = (id) => {
  allowLeave.value = true;
  router.push(`/san-pham/${id}`);
};

const goHome = () => {
  allowLeave.value = true;
  router.push('/');
}

const retakeQuiz = () => {
  selectedAnswers.value = {};
  scoreMap.value = {};
  resultData.value = { skinName: '', description: '' };
  recommendedProducts.value = [];
  showResult.value = false;
  analyzing.value = false;
  currentStep.value = 1;
  allowLeave.value = false; 
};
</script>

<style scoped>
/* ============================================
   SUNOVA QUIZ — NÂU / KEM / GOLD
   ============================================ */
.sg-quiz-root {
  --sq-espresso: #241a12;
  --sq-dark: #1a1412;
  --sq-card-bg: #2e2218;
  --sq-cream: #f9f5f0;
  --sq-warm-white: #fffdfa;
  --sq-gold: #c9a96e;
  --sq-gold-dark: #9e7340;
  --sq-gold-light: #d4bc8a;
  --sq-sand: #e8dcc8;
  --sq-text-muted: #a09488;
  --sq-border: #3e3228;

  font-family: 'Be Vietnam Pro', 'Inter', sans-serif;
  min-height: 80vh;
}

/* ============================================
   MODAL CẢNH BÁO (RUNG)
   ============================================ */
.sunova-modal-overlay {
  position: fixed; inset: 0; z-index: 9999;
  background: rgba(26,20,18,0.75); backdrop-filter: blur(8px);
  display: flex; align-items: center; justify-content: center; padding: 20px;
}

.sunova-modal {
  background: var(--sq-cream); border-radius: 20px; padding: 40px 36px;
  max-width: 440px; width: 100%; text-align: center;
  box-shadow: 0 20px 60px rgba(0,0,0,0.4);
}

.modal-icon { margin-bottom: 20px; }

.custom-warning {
  display: inline-block;
  animation: icon-shake 2s ease-in-out infinite;
}

@keyframes icon-shake {
  0%, 100% { transform: rotate(0deg); }
  10%, 30%, 50% { transform: rotate(-8deg); }
  20%, 40%, 60% { transform: rotate(8deg); }
  70% { transform: rotate(0deg); }
}

.modal-title {
  font-family: 'Playfair Display', serif;
  font-size: 22px; font-weight: 700;
  color: var(--sq-espresso); margin: 0 0 10px;
}

.modal-desc {
  font-size: 14px; color: #6b5e53;
  margin: 0 0 28px; line-height: 1.65;
}

.modal-actions { display: flex; gap: 12px; justify-content: center; flex-wrap: wrap; }

.modal-btn {
  padding: 13px 28px; border-radius: 8px; font-size: 12px; font-weight: 700;
  letter-spacing: 1.5px; cursor: pointer; transition: all 0.25s; border: none;
}

.btn-cancel {
  background: var(--sq-espresso); color: var(--sq-cream);
}
.btn-cancel:hover { background: #3a2a1e; }

.btn-confirm {
  background: transparent; border: 2px solid var(--sq-sand); color: #6b5e53;
}
.btn-confirm:hover { border-color: #E55B5B; color: #E55B5B; }

.modal-fade-enter-active, .modal-fade-leave-active { transition: opacity 0.3s; }
.modal-fade-enter-from, .modal-fade-leave-to { opacity: 0; }

/* ============================================
   LANDING PAGE
   ============================================ */
.sg-landing {
  min-height: 75vh;
  background: linear-gradient(165deg, var(--sq-dark) 0%, var(--sq-espresso) 50%, #1e1510 100%);
  display: flex; flex-direction: column; align-items: center;
  justify-content: center; padding: 60px 24px 40px;
}

.sg-landing__content {
  display: flex; align-items: center; gap: 60px;
  max-width: 960px; width: 100%; margin-bottom: 48px;
}

.sg-landing__visual {
  position: relative; flex-shrink: 0; width: 340px; height: 340px;
  display: flex; align-items: center; justify-content: center;
}

.sg-landing__circle {
  position: absolute; width: 300px; height: 300px; border-radius: 50%;
  background: linear-gradient(135deg, var(--sq-gold) 0%, var(--sq-gold-light) 100%);
  animation: lp-pulse 3s ease-in-out infinite;
}

@keyframes lp-pulse { 0%,100% { transform: scale(1); } 50% { transform: scale(1.04); } }

.sg-landing__product { 
  position: relative; z-index: 1; font-family: 'Playfair Display', serif; 
  font-size: 52px; font-weight: 700; color: var(--sq-espresso); letter-spacing: 4px;
}

.sg-landing__text { flex: 1; }
.sg-landing__brand { font-size: 18px; font-style: italic; color: var(--sq-gold); margin: 0 0 12px; letter-spacing: 1px; }

.sg-landing__title {
  font-family: 'Playfair Display', serif; font-size: 42px; font-weight: 700;
  color: var(--sq-cream); line-height: 1.1; margin: 0 0 18px;
}

.sg-landing__desc { font-size: 15px; color: var(--sq-text-muted); line-height: 1.7; margin: 0 0 28px; max-width: 420px; }

.sg-landing__cta {
  background: var(--sq-gold); color: var(--sq-espresso); border: none;
  padding: 16px 42px; font-size: 14px; font-weight: 700; letter-spacing: 2px;
  border-radius: 8px; cursor: pointer; transition: all 0.3s;
}
.sg-landing__cta:hover:not(:disabled) { background: var(--sq-gold-dark); color: var(--sq-cream); transform: translateY(-2px); box-shadow: 0 6px 20px rgba(201,169,110,0.3); }
.sg-landing__cta:disabled { opacity: 0.5; cursor: not-allowed; }

/* Testimonial */
.sg-landing__testimonial { position: relative; max-width: 520px; }
.sg-testimonial__card { background: var(--sq-espresso); border: 1px solid var(--sq-border); border-radius: 10px; padding: 20px 24px; text-align: center;}
.sg-testimonial__text { font-size: 13px; font-style: italic; line-height: 1.6; margin: 0 0 10px; color: var(--sq-cream); opacity: 0.9; }
.sg-testimonial__author { font-size: 12px; margin: 0; color: var(--sq-gold); }

/* ============================================
   QUIZ MAIN AREA
   ============================================ */
.sg-quiz {
  min-height: 75vh;
  background: linear-gradient(180deg, var(--sq-dark) 0%, var(--sq-espresso) 30%, #1e1510 100%);
  padding: 30px 24px 60px;
  display: flex; flex-direction: column; align-items: center;
}

/* Progress */
.sg-quiz__header { width: 100%; max-width: 800px; margin-bottom: 40px; }
.sg-progress { display: flex; align-items: center; gap: 16px; }
.sg-progress__back {
  background: none; border: 1.5px solid rgba(249,245,240,0.25); border-radius: 50%;
  width: 42px; height: 42px; color: var(--sq-cream); cursor: pointer;
  display: flex; align-items: center; justify-content: center; transition: all 0.3s; flex-shrink: 0;
}
.sg-progress__back:hover { background: rgba(249,245,240,0.08); border-color: rgba(249,245,240,0.4); }
.sg-progress__back-placeholder { width: 42px; flex-shrink: 0; }
.sg-progress__track { flex: 1; height: 10px; background: rgba(249,245,240,0.12); border-radius: 10px; position: relative; overflow: visible; }
.sg-progress__fill { height: 100%; background: linear-gradient(90deg, var(--sq-gold-dark), var(--sq-gold)); border-radius: 10px; transition: width 0.5s ease-out; position: relative; min-width: 10px; }
.sg-progress__sun { position: absolute; top: -16px; right: -18px; font-size: 36px; filter: drop-shadow(0 2px 8px rgba(201,169,110,0.5)); animation: sun-spin 4s linear infinite; }
@keyframes sun-spin { 0% { transform: rotate(0deg); } 100% { transform: rotate(360deg); } }

/* Question Layout */
.sg-question { width: 100%; max-width: 960px; display: flex; flex-direction: column; align-items: center; }
.sg-question__layout { width: 100%; display: flex; gap: 40px; }
.sg-question__layout:not(.has-quote) { justify-content: center; }
.sg-question__layout:not(.has-quote) .sg-question__main { max-width: 800px; }

/* Quote bên trái */
.sg-question__quote-col { flex: 0 0 260px; display: flex; align-items: center; }
.sg-quote { margin: 0; padding: 0; border: none; text-align: center; }
.sg-quote__text { font-size: 15px; font-style: italic; color: var(--sq-cream); line-height: 1.7; margin: 0 0 16px; opacity: 0.8; }
.sg-quote__author { display: flex; flex-direction: column; gap: 2px; }
.sg-quote__author strong { font-size: 12px; letter-spacing: 2px; color: var(--sq-gold); }
.sg-quote__author span { font-size: 12px; color: var(--sq-text-muted); }

/* Khối câu hỏi chính */
.sg-question__main { flex: 1; text-align: center; }
.sg-question__title { font-family: 'Playfair Display', serif; font-size: 28px; font-weight: 500; color: var(--sq-cream); margin: 0 0 8px; line-height: 1.35; }
.sg-question__hint { font-size: 14px; font-weight: 600; color: var(--sq-gold); margin: 0 0 28px; opacity: 0.8; }

/* Thẻ đáp án - Text Centered - No Icon */
.sg-answers { display: flex; justify-content: center; gap: 14px; flex-wrap: wrap; margin-bottom: 24px; }
.sg-answers--2 .sg-answer-card { width: 220px; min-height: 130px; }
.sg-answers--3 .sg-answer-card { width: 200px; min-height: 130px; }
.sg-answers--4 .sg-answer-card { width: 180px; min-height: 130px; }
.sg-answers--list { flex-direction: column; max-width: 500px; margin-left: auto; margin-right: auto; }
.sg-answers--list .sg-answer-card { width: 100%; min-height: auto; padding: 20px 24px; text-align: center; }

.sg-answer-card {
  background: var(--sq-cream); border: 2px solid transparent; border-radius: 12px;
  padding: 24px 20px; cursor: pointer; transition: all 0.25s;
  display: flex; flex-direction: column; align-items: center; justify-content: center; text-align: center;
  box-shadow: 0 2px 12px rgba(0,0,0,0.15);
}
.sg-answer-card:hover:not(.sg-answer-card--selected) { transform: translateY(-4px); box-shadow: 0 8px 24px rgba(0,0,0,0.2); border-color: var(--sq-gold); }
.sg-answer-card--selected { border-color: var(--sq-gold); background: var(--sq-warm-white); box-shadow: 0 0 0 2px var(--sq-gold); }
.sg-answer-card__label { font-size: 16px; font-weight: 600; color: var(--sq-espresso); margin: 0; line-height: 1.5; }

/* Next Button */
.sg-question__footer { margin-top: 8px; }
.sg-btn-next {
  background: var(--sq-gold); color: var(--sq-espresso); border: none;
  padding: 14px 40px; font-size: 13px; font-weight: 700; letter-spacing: 1.5px;
  border-radius: 8px; cursor: pointer; transition: all 0.25s;
}
.sg-btn-next:hover:not(:disabled) { background: var(--sq-gold-dark); color: var(--sq-cream); transform: translateY(-2px); }
.sg-btn-next:disabled { opacity: 0.35; cursor: not-allowed; }

/* WHY WE ASK */
.sg-why { position: relative; max-width: 500px; margin: 36px auto 0; }
.sg-why__badge { position: absolute; top: -16px; right: -10px; z-index: 2; width: 38px; height: 38px; border-radius: 50%; background: var(--sq-gold); display: flex; align-items: center; justify-content: center; font-size: 18px; font-weight: bold; color: var(--sq-espresso); box-shadow: 0 3px 10px rgba(0,0,0,0.2); }
.sg-why__card { background: var(--sq-espresso); border: 1px solid var(--sq-border); border-radius: 10px; padding: 18px 22px; }
.sg-why__title { font-size: 12px; font-weight: 800; letter-spacing: 1.5px; margin: 0 0 6px; color: var(--sq-gold); }
.sg-why__text { font-size: 13px; line-height: 1.6; margin: 0; color: var(--sq-cream); opacity: 0.85; }

/* ============================================
   ANALYZING
   ============================================ */
.sg-analyzing { text-align: center; padding-top: 80px; width: 100%; }
.sg-analyzing__spinner { font-size: 80px; margin-bottom: 24px; animation: sun-spin 2s linear infinite; filter: drop-shadow(0 0 15px rgba(201,169,110,0.5));}
.sg-analyzing__title { font-size: 28px; font-weight: 600; color: var(--sq-cream); margin: 0 0 12px; }
.sg-analyzing__desc { font-size: 15px; color: var(--sq-text-muted); margin: 0 0 24px; }
.sg-analyzing__dots { display: flex; justify-content: center; gap: 8px; }
.sg-analyzing__dots span { width: 10px; height: 10px; border-radius: 50%; background: var(--sq-gold); animation: dot-b 1.4s infinite ease-in-out; }
.sg-analyzing__dots span:nth-child(2) { animation-delay: 0.16s; }
.sg-analyzing__dots span:nth-child(3) { animation-delay: 0.32s; }
@keyframes dot-b { 0%,80%,100% { transform: scale(0.6); opacity: 0.3; } 40% { transform: scale(1); opacity: 1; } }

/* ============================================
   RESULT
   ============================================ */
.sg-result { width: 100%; max-width: 800px; display: flex; flex-direction: column; align-items: center; }
.sg-result__hero { text-align: center; margin-bottom: 28px; }
.sg-result__title { font-family: 'Playfair Display', serif; font-size: 26px; font-weight: 600; color: var(--sq-cream); margin: 0; }

.sg-result__skin-card { background: var(--sq-cream); border-radius: 16px; padding: 40px 36px; text-align: center; width: 100%; max-width: 480px; margin-bottom: 36px; box-shadow: 0 8px 32px rgba(0,0,0,0.2); }
.sg-result__skin-name { font-family: 'Playfair Display', serif; font-size: 28px; font-weight: 700; color: var(--sq-espresso); margin: 0 0 12px; }
.sg-result__skin-desc { font-size: 15px; color: #6b5e53; line-height: 1.7; margin: 0; }

.sg-result__scores { width: 100%; max-width: 480px; margin-bottom: 40px; }
.sg-result__scores-title { font-size: 14px; font-weight: 700; color: var(--sq-gold); letter-spacing: 1px; margin: 0 0 16px; text-align: center; text-transform: uppercase;}
.sg-score-row { display: flex; align-items: center; gap: 10px; margin-bottom: 8px; }
.sg-score-row__label { width: 85px; text-align: right; font-size: 13px; color: var(--sq-cream); font-weight: 500; flex-shrink: 0; opacity: 0.9; }
.sg-score-row__bar { flex: 1; height: 8px; background: rgba(249,245,240,0.12); border-radius: 4px; overflow: hidden; }
.sg-score-row__fill { height: 100%; background: linear-gradient(90deg, var(--sq-gold-dark), var(--sq-gold)); border-radius: 4px; transition: width 0.8s ease-out; }
.sg-score-row__value { width: 40px; font-size: 13px; color: var(--sq-gold); font-weight: 700; }

.sg-result__products { width: 100%; margin-bottom: 36px; }
.sg-result__products-title { font-size: 20px; font-weight: 700; color: var(--sq-cream); margin: 0 0 20px; text-align: center; }
.sg-product-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 14px; }
.sg-product-card { background: var(--sq-cream); border-radius: 12px; overflow: hidden; cursor: pointer; transition: all 0.25s; box-shadow: 0 2px 12px rgba(0,0,0,0.15); }
.sg-product-card:hover { transform: translateY(-4px); box-shadow: 0 8px 24px rgba(0,0,0,0.2); }
.sg-product-card__img { width: 100%; aspect-ratio: 1; background: var(--sq-sand); display: flex; align-items: center; justify-content: center; overflow: hidden; }
.sg-product-card__img img { width: 100%; height: 100%; object-fit: cover; }
.sg-product-card__placeholder { font-family: 'Playfair Display', serif; font-size: 20px; font-weight: 700; color: var(--sq-espresso); letter-spacing: 2px; }
.sg-product-card__name { font-size: 13px; font-weight: 600; color: var(--sq-espresso); margin: 0; padding: 10px 12px 4px; line-height: 1.4; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; }
.sg-product-card__price { font-size: 14px; font-weight: 700; color: var(--sq-gold-dark); margin: 0; padding: 0 12px 12px; }

/* CSS XỬ LÝ LỖI GIÁ BẰNG 0 (NÚT KHÁM PHÁ NGAY) */
.price-cta {
  font-size: 13px !important;
  color: var(--sq-gold) !important;
  text-decoration: underline;
  cursor: pointer;
}
.price-cta:hover {
  color: var(--sq-gold-dark) !important;
}

.sg-result__actions { display: flex; gap: 14px; flex-wrap: wrap; justify-content: center; margin-top: 8px; margin-bottom: 20px; }
.sg-btn-primary { background: var(--sq-gold); color: var(--sq-espresso); border: none; padding: 14px 36px; font-size: 13px; font-weight: 700; letter-spacing: 1.5px; border-radius: 8px; cursor: pointer; transition: all 0.25s; }
.sg-btn-primary:hover { background: var(--sq-gold-dark); color: var(--sq-cream); transform: translateY(-2px); }
.sg-btn-outline { background: transparent; border: 2px solid var(--sq-cream); color: var(--sq-cream); padding: 14px 36px; font-size: 13px; font-weight: 700; letter-spacing: 1.5px; border-radius: 8px; cursor: pointer; transition: all 0.25s; }
.sg-btn-outline:hover { background: rgba(249,245,240,0.08); transform: translateY(-2px); }

/* TRANSITIONS */
.sg-slide-enter-active, .sg-slide-leave-active { transition: all 0.4s ease; }
.sg-slide-enter-from { opacity: 0; transform: translateX(40px); }
.sg-slide-leave-to { opacity: 0; transform: translateX(-40px); }

/* Nút Đóng */
.quiz-close-btn {
  position: fixed; top: 20px; right: 24px; z-index: 100;
  background: transparent; border: 1.5px solid rgba(249,245,240,0.3); color: var(--sq-cream);
  width: 44px; height: 44px; border-radius: 50%; font-size: 24px;
  cursor: pointer; display: flex; align-items: center; justify-content: center; transition: all 0.3s;
}
.quiz-close-btn:hover { background: rgba(249,245,240,0.1); border-color: var(--sq-cream); transform: scale(1.05); }

/* RESPONSIVE */
@media (max-width: 900px) {
  .sg-landing__content { flex-direction: column; text-align: center; gap: 28px; }
  .sg-landing__visual { width: 220px; height: 220px; }
  .sg-landing__circle { width: 200px; height: 200px; }
  .sg-landing__product { font-size: 38px; }
  .sg-landing__title { font-size: 30px; }
  .sg-landing__desc { margin-left: auto; margin-right: auto; }
  .sg-question__layout.has-quote { flex-direction: column; gap: 24px; }
  .sg-question__quote-col { display: none; }
  .sg-question__title { font-size: 22px; }
}

@media (max-width: 640px) {
  .sg-answers--2 .sg-answer-card, .sg-answers--3 .sg-answer-card, .sg-answers--4 .sg-answer-card { width: calc(50% - 8px); min-height: 100px; padding: 16px 12px; }
  .sg-answer-card__label { font-size: 14px; }
  .sg-product-grid { grid-template-columns: repeat(2, 1fr); }
  .sg-landing__title { font-size: 26px; }
  .sg-progress__sun { font-size: 28px; top: -12px; right: -14px; }
  .sg-result__title { font-size: 22px; }
  .sg-result__skin-name { font-size: 24px; }
}
</style>