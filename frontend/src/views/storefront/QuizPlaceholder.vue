<template>
  <div class="sg-quiz-root">

    <!-- HIỆU ỨNG BONG BÓNG BAY LƠ LỬNG NỀN XUNG QUANH QUIZ KÈM ẢNH SẢN PHẨM -->
    <div class="sg-quiz-bubbles" aria-hidden="true">
      <div
        v-for="(bubble, idx) in backgroundBubbles"
        :key="bubble.id"
        class="sg-quiz-bubble"
        :style="bubble.style"
      >
        <div class="sg-quiz-bubble__inner">
          <div class="sg-quiz-bubble__specular"></div>
          <div class="sg-quiz-bubble__rim"></div>
          <img
            :src="(!bubbleImgErrors[idx] && bubble.image) ? bubble.image : sunovaMarkImg"
            :alt="bubble.name"
            class="sg-quiz-bubble__img"
            loading="lazy"
            @error="handleBubbleImgError(idx)"
          />
        </div>
      </div>
    </div>

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
      <div class="sg-landing__card">
        <div class="sg-landing__content">
          <!-- VISUAL BÊN TRÁI -->
          <div class="sg-landing__visual">
            <div class="sg-visual-orbit">
              <div class="sg-visual-orbit__glow"></div>
              <div class="sg-visual-orbit__ring"></div>
              <div class="sg-visual-orbit__ring-outer"></div>

              <!-- Center Luxury Logo Emblem -->
              <div class="sg-visual-center">
                <img
                  src="@/assets/logo/sunova_mark.png"
                  alt="SUNOVA Logo"
                  class="sg-visual-center__logo"
                />
                <div class="sg-visual-center__brand">SUNOVA</div>
                <div class="sg-visual-center__pa">SPF 50+ · DERMA UV</div>
              </div>

              <!-- Floating Luxury Pills -->
              <div class="sg-visual-pill sg-visual-pill--top">
                <Icon icon="solar:shield-check-bold" width="16" class="sg-pill-icon" />
                <span>Màng lọc UV quang phổ rộng</span>
              </div>
              <div class="sg-visual-pill sg-visual-pill--bottom">
                <Icon icon="solar:stars-bold" width="16" class="sg-pill-icon" />
                <span>Routine chuẩn từng tuýp da</span>
              </div>
              <div class="sg-visual-pill sg-visual-pill--side">
                <Icon icon="solar:heart-bold" width="15" class="sg-pill-icon" />
                <span>99% Khách hàng hài lòng</span>
              </div>
            </div>
          </div>

          <!-- NỘI DUNG BÊN PHẢI -->
          <div class="sg-landing__text">
            <div class="sg-landing__kicker">
              <span class="sg-landing__kicker-dot"></span>
              <span>SUNOVA DERMA-AI · 60 GIÂY PHÂN TÍCH</span>
            </div>
            <h1 class="sg-landing__title">
              Khám Phá Kem Chống Nắng
              <span class="sg-landing__title-accent">Hoàn Hảo Cho Làn Da</span>
            </h1>
            <p class="sg-landing__desc">
              Chỉ với vài câu hỏi trắc nghiệm nhanh, hệ thống da liễu chuyên sâu của SUNOVA sẽ phân tích chính xác tình trạng da và gợi ý công thức màng lọc UV phù hợp nhất với bạn.
            </p>

            <div class="sg-landing__cta-wrap">
              <button class="sg-landing__cta" @click="startQuiz" :disabled="loading">
                <span>{{ loading ? 'ĐANG TẢI CÂU HỎI...' : 'BẮT ĐẦU BÀI TEST NGAY' }}</span>
                <Icon icon="solar:arrow-right-linear" width="18" class="sg-landing__cta-icon" />
              </button>
              <div class="sg-landing__cta-sub">
                <Icon icon="solar:check-circle-bold" width="16" />
                <span>100% Miễn phí · Nhận ngay kết quả & gợi ý combo</span>
              </div>
            </div>
          </div>
        </div>

        <!-- 3 TRUST POINTS BÊN DƯỚI -->
        <div class="sg-landing__features">
          <div class="sg-feature-item">
            <div class="sg-feature-item__icon">
              <Icon icon="solar:stopwatch-bold" width="22" />
            </div>
            <div class="sg-feature-item__body">
              <strong>Chỉ mất 1 phút</strong>
              <p>5 câu hỏi trắc nghiệm trực quan, dễ thực hiện</p>
            </div>
          </div>
          <div class="sg-feature-item">
            <div class="sg-feature-item__icon">
              <Icon icon="solar:test-tube-bold" width="22" />
            </div>
            <div class="sg-feature-item__body">
              <strong>Chuẩn khoa học da liễu</strong>
              <p>Đánh giá theo loại da, độ nhạy cảm & môi trường</p>
            </div>
          </div>
          <div class="sg-feature-item">
            <div class="sg-feature-item__icon">
              <Icon icon="solar:magic-stick-3-bold" width="22" />
            </div>
            <div class="sg-feature-item__body">
              <strong>Gợi ý cá nhân hoá</strong>
              <p>Chính xác chỉ số SPF/PA và combo chăm sóc da lý tưởng</p>
            </div>
          </div>
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

          <div class="sg-progress__track-wrapper" style="flex: 1; display: flex; flex-direction: column; gap: 12px; width: 100%;">
            <div class="sg-progress__track" style="width: 100%; flex: none;">
              <div class="sg-progress__fill" :style="{ width: progressPercentage + '%' }">
                <div class="sg-progress__sun">☀️</div>
              </div>
            </div>
            <div style="font-size: 13px; color: var(--sq-espresso); text-align: center; font-weight: 600; letter-spacing: 1px;">
              BẠN ĐÃ HOÀN THÀNH {{ Math.round(progressPercentage) }}%
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
                  <Icon v-if="answer.icon" :icon="answer.icon" class="sg-answer-card__icon" />
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
          
          <!-- LỰA CHỌN CHÂN ÁI (HERO PRODUCT) -->
          <div class="sg-result__hero-product" v-if="recommendedProducts.length > 0">
            <h2 class="sg-result__hero-label">SẢN PHẨM CHÂN ÁI CỦA BẠN</h2>
            <div class="sg-hero-card" @click="goToProduct(recommendedProducts[0].id)">
              <div class="sg-hero-card__img">
                <img v-if="recommendedProducts[0].anhChinhUrl" :src="productImageUrl(recommendedProducts[0].anhChinhUrl)" :alt="recommendedProducts[0].ten" />
                <div v-else class="sg-hero-card__placeholder">SUNOVA</div>
              </div>
              <div class="sg-hero-card__info">
                <h3 class="sg-hero-card__name">{{ recommendedProducts[0].ten }}</h3>
                <p class="sg-hero-card__price">{{ (recommendedProducts[0].giaMin || recommendedProducts[0].gia) ? formatPrice(recommendedProducts[0].giaMin || recommendedProducts[0].gia) : 'Khám phá ngay ➔' }}</p>
                <p class="sg-hero-card__desc">Sản phẩm hoàn hảo nhất đáp ứng các nhu cầu về làn da của bạn. Công thức mỏng nhẹ, bảo vệ tối ưu dưới tác động của tia UV.</p>
                <button class="sg-btn-buy">XEM CHI TIẾT</button>
              </div>
            </div>
          </div>

          <!-- GIẢI THÍCH KẾT QUẢ -->
          <div class="sg-result__explanation">
            <div class="sg-explanation-col">
              <h4>Tại sao chúng tôi gợi ý sản phẩm này?</h4>
              <p>Gợi ý được đưa ra dựa trên phân tích chuyên sâu về tình trạng và loại da thực tế của bạn.</p>
            </div>
            <div class="sg-explanation-col">
              <h4>LOẠI DA CỦA BẠN</h4>
              <p class="sg-skin-highlight">✔️ {{ resultData.skinName }}</p>
              <p class="sg-skin-desc">{{ resultData.description }}</p>
            </div>
            <div class="sg-explanation-col">
              <h4>PHÂN BỐ ĐIỂM DA</h4>
              <div class="sg-result__score-list">
                <div v-for="score in sortedScores.slice(0,3)" :key="score.id" class="sg-score-row">
                  <span class="sg-score-row__label">{{ score.name }}</span>
                  <div class="sg-score-row__bar">
                    <div class="sg-score-row__fill" :style="{ width: score.percent + '%' }"></div>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- ROUTINE COMBO (TỪ ADMIN) -->
          <div class="sg-result__routine" v-if="routineCombo">
            <h3 class="sg-result__routine-title">COMBO DÀNH RIÊNG CHO BẠN</h3>
            <p class="sg-result__routine-subtitle"><strong>{{ routineCombo.ten }}</strong>: {{ routineCombo.moTa }}</p>
            <div class="sg-routine-grid">
              <div
                v-for="ct in routineCombo.chiTiets"
                :key="ct.idSanPham"
                class="sg-routine-card"
                @click="goToProduct(ct.idSanPham)"
              >
                <div class="sg-routine-step">BƯỚC {{ ct.thuTu }}</div>
                <div class="sg-routine-card__img">
                  <img v-if="ct.anhChinhUrl" :src="productImageUrl(ct.anhChinhUrl)" :alt="ct.tenSanPham" />
                  <div v-else class="sg-routine-card__placeholder">SUNOVA</div>
                </div>
                <h4 class="sg-routine-card__name">{{ ct.tenSanPham }}</h4>
                <p style="font-size: 11px; color: #a09488; text-align: center; margin-top: 5px;">{{ ct.ghiChu }}</p>
                <a class="sg-routine-card__link">XEM THÊM</a>
              </div>
            </div>
          </div>

          <!-- FALLBACK NẾU KHÔNG CÓ ROUTINE COMBO -> HIỂN THỊ CÁC GỢI Ý THAY THẾ -->
          <div class="sg-result__routine" v-else-if="recommendedProducts.length > 1">
            <h3 class="sg-result__routine-title">CÁC LỰA CHỌN THAY THẾ KHÁC</h3>
            <p class="sg-result__routine-subtitle">Dưới đây là các sản phẩm cũng có độ tương thích rất cao với làn da của bạn.</p>
            <div class="sg-routine-grid">
              <div
                v-for="(product, index) in recommendedProducts.slice(1)"
                :key="product.id"
                class="sg-routine-card"
                @click="goToProduct(product.id)"
              >
                <div class="sg-routine-step">LỰA CHỌN {{ index + 2 }}</div>
                <div class="sg-routine-card__img">
                  <img v-if="product.anhChinhUrl" :src="productImageUrl(product.anhChinhUrl)" :alt="product.ten" />
                  <div v-else class="sg-routine-card__placeholder">SUNOVA</div>
                </div>
                <h4 class="sg-routine-card__name">{{ product.ten }}</h4>
                <a class="sg-routine-card__link">XEM THÊM</a>
              </div>
            </div>
          </div>

          <div class="sg-result__actions">
            <button class="sg-btn-outline" @click="retakeQuiz">LÀM LẠI QUIZ</button>
            <button class="sg-btn-primary" @click="goToProducts">KHÁM PHÁ TẤT CẢ SẢN PHẨM</button>
          </div>
        </div>

      </transition>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount } from 'vue';
import { useRouter, onBeforeRouteLeave } from 'vue-router';
import { Icon } from '@iconify/vue';

// CHUẨN KIẾN TRÚC GỌI API (Đã thay đổi theo hướng dẫn đồ án)

import { getProducts } from '@/api/sanPhamApi';
import { getQuizQuestions, saveQuizResult } from '@/api/quizApi';
import { getRoutinesByLoaiDa } from '@/api/routineApi';
import { rankProductsByQuiz, saveQuizProfile } from '@/utils/quizRecommend';
import { productImageUrl } from '@/utils/productImage';
import sunovaMarkImg from '@/assets/logo/sunova_mark.png';

const router = useRouter();

// ============================================
// HIỆU ỨNG BONG BÓNG BAY LƠ LỬNG NỀN QUIZ
// ============================================
const bubbleImgErrors = ref({});
const handleBubbleImgError = (idx) => {
  bubbleImgErrors.value[idx] = true;
};

// Cấu hình 20 bong bóng bay chìm khắp nền (trải đều toàn bộ bề ngang, đi chìm sau lưng phần Quiz)
const BUBBLE_CONFIGS = [
  // Nhóm lề trái
  { left: '3%',  size: 74, duration: 21, delay: -3,  swayDuration: 4.2, opacity: 0.88 },
  { left: '8%',  size: 88, duration: 25, delay: -11, swayDuration: 5.0, opacity: 0.90 },
  { left: '15%', size: 62, duration: 19, delay: -7,  swayDuration: 3.8, opacity: 0.85 },
  
  // Nhóm chìm sau nửa trái phần Quiz (visual orbit & câu hỏi bên trái)
  { left: '21%', size: 84, duration: 23, delay: -17, swayDuration: 4.6, opacity: 0.88 },
  { left: '27%', size: 66, duration: 20, delay: -4,  swayDuration: 4.1, opacity: 0.80 },
  { left: '33%', size: 90, duration: 26, delay: -14, swayDuration: 5.2, opacity: 0.90 },
  { left: '38%', size: 58, duration: 28, delay: -9,  swayDuration: 4.4, opacity: 0.75 },

  // Nhóm chìm chính giữa phía sau Quiz (tâm card & giữa các đáp án)
  { left: '44%', size: 76, duration: 22, delay: -2,  swayDuration: 4.0, opacity: 0.85 },
  { left: '50%', size: 86, duration: 24, delay: -19, swayDuration: 4.8, opacity: 0.88 },
  { left: '56%', size: 68, duration: 21, delay: -6,  swayDuration: 4.3, opacity: 0.82 },

  // Nhóm chìm sau nửa phải phần Quiz (nội dung text, CTA & câu hỏi bên phải)
  { left: '62%', size: 92, duration: 27, delay: -15, swayDuration: 5.4, opacity: 0.90 },
  { left: '67%', size: 64, duration: 19, delay: -10, swayDuration: 3.9, opacity: 0.82 },
  { left: '73%', size: 82, duration: 23, delay: -22, swayDuration: 4.7, opacity: 0.86 },
  { left: '78%', size: 58, duration: 20, delay: -1,  swayDuration: 4.2, opacity: 0.78 },

  // Nhóm lề phải
  { left: '83%', size: 86, duration: 24, delay: -8,  swayDuration: 4.9, opacity: 0.90 },
  { left: '88%', size: 70, duration: 22, delay: -16, swayDuration: 4.5, opacity: 0.85 },
  { left: '93%', size: 90, duration: 26, delay: -12, swayDuration: 5.1, opacity: 0.90 },
  { left: '97%', size: 56, duration: 18, delay: -13, swayDuration: 3.7, opacity: 0.78 },

  // Nhóm nền sâu tăng hiệu ứng đa tầng
  { left: '18%', size: 78, duration: 24, delay: -24, swayDuration: 4.5, opacity: 0.82 },
  { left: '48%', size: 94, duration: 25, delay: -18, swayDuration: 5.0, opacity: 0.88 },
];

// Danh sách sản phẩm có ảnh hợp lệ
const bubbleProducts = computed(() => {
  return allProducts.value.filter(p => p.anhChinhUrl && typeof p.anhChinhUrl === 'string' && p.anhChinhUrl.trim() !== '');
});

// Danh sách bong bóng hoàn chỉnh kèm style & ảnh sản phẩm
const backgroundBubbles = computed(() => {
  const prods = bubbleProducts.value;
  return BUBBLE_CONFIGS.map((cfg, idx) => {
    const prod = prods.length > 0 ? prods[idx % prods.length] : null;
    const rawImg = prod?.anhChinhUrl ? productImageUrl(prod.anhChinhUrl) : null;
    return {
      id: idx,
      name: prod?.ten || 'SUNOVA Sunscreen',
      image: rawImg,
      style: {
        left: cfg.left,
        width: `${cfg.size}px`,
        height: `${cfg.size}px`,
        animationDuration: `${cfg.duration}s`,
        animationDelay: `${cfg.delay}s`,
        '--bubble-opacity': cfg.opacity,
        '--bubble-sway-duration': `${cfg.swayDuration}s`,
      },
    };
  });
});

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
const routineCombo = ref(null); // Lưu routine lấy từ admin

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
const progressPercentage = computed(() => totalSteps.value > 0 ? Math.min(100, ((currentStep.value - 1) / totalSteps.value) * 100) : 0);
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
  const collectedFilters = []; // Thu thập tất cả Từ khóa lọc cứng mà khách đã chọn

  Object.values(selectedAnswers.value).forEach(answer => {
    const tagId = answer.tagId || answer.idLoaiDa;
    const points = answer.scoreValue || answer.diem || 0;

    // Thu thập filterKeyword (nếu có)
    if (answer.filterKeyword) {
      collectedFilters.push(answer.filterKeyword);
    }

    // Chỉ cộng điểm nếu có tagId VÀ điểm > 0 (bỏ qua câu hỏi Filter thuần túy)
    if (tagId && points > 0) {
      if (!scores[tagId]) {
        const info = LOAI_DA_INFO[tagId] || { name: `Loại da #${tagId}` };
        scores[tagId] = { name: info.name, points: 0 };
      }
      scores[tagId].points += points;
    }
  });
  scoreMap.value = scores;

  let topPoints = 0;
  // Tìm điểm cao nhất
  Object.values(scores).forEach(data => {
    if (data.points > topPoints) { topPoints = data.points; }
  });

  // Tìm TẤT CẢ các loại da đạt điểm cao nhất (xử lý hòa điểm)
  const topSkinTypes = [];
  Object.entries(scores).forEach(([id, data]) => {
    if (data.points === topPoints && topPoints > 0) {
      topSkinTypes.push(Number(id));
    }
  });

  // Fallback nếu không có điểm nào
  if (topSkinTypes.length === 0) topSkinTypes.push(5);

  // Hiển thị tên kết hợp (VD: "Da Dầu & Da Nhạy Cảm")
  const skinNames = topSkinTypes.map(id => LOAI_DA_INFO[id].name).join(' & ');
  
  // Hiển thị mô tả
  let skinDesc = '';
  if (topSkinTypes.length === 1) {
    skinDesc = LOAI_DA_INFO[topSkinTypes[0]].desc;
  } else {
    skinDesc = 'Làn da của bạn có sự kết hợp của nhiều yếu tố. ' + topSkinTypes.map(id => LOAI_DA_INFO[id].desc).join(' ');
  }

  resultData.value = { skinName: skinNames, description: skinDesc };
  recommendProducts(collectedFilters);

  saveQuizProfile({
    idLoaiDa: topSkinTypes[0],
    tenLoaiDa: skinNames,
    scoreMap: scores,
    filters: collectedFilters,
  });

  // FETCH ROUTINE COMBO TỪ ADMIN DÀNH CHO LOẠI DA NÀY
  getRoutinesByLoaiDa(topSkinTypes[0]).then(res => {
    if (res.data && res.data.length > 0) {
      const fetchedRoutine = res.data[0];
      let isRoutineValid = true;

      // KIỂM TRA QUYỀN PHỦ QUYẾT Y KHOA (Medical Filter)
      if (collectedFilters.length > 0 && fetchedRoutine.chiTiets) {
        for (const ct of fetchedRoutine.chiTiets) {
          const p = allProducts.value.find(x => x.id === ct.idSanPham);
          if (p && p.loaiChongNang) {
            const passed = collectedFilters.some(f => {
              if (f === 'VAT_LY') return p.loaiChongNang === 'VAT_LY' || p.loaiChongNang === 'LAI';
              if (f === 'HOA_HOC') return p.loaiChongNang === 'HOA_HOC' || p.loaiChongNang === 'LAI';
              return p.loaiChongNang === f;
            });
            if (!passed) {
              isRoutineValid = false;
              break;
            }
          }
        }
      }

      if (isRoutineValid) {
        // API routine trả anhChinhUrl = null — lấy ảnh từ danh sách sản phẩm đã tải.
        routineCombo.value = {
          ...fetchedRoutine,
          chiTiets: (fetchedRoutine.chiTiets || []).map((ct) => {
            const p = allProducts.value.find((x) => x.id === ct.idSanPham);
            return {
              ...ct,
              anhChinhUrl: ct.anhChinhUrl || p?.anhChinhUrl || null,
            };
          }),
        };
      } else {
        console.warn("Routine bị từ chối do vi phạm bộ lọc y khoa của khách hàng.");
        routineCombo.value = null; // Lùi về gợi ý tự động
      }
    } else {
      routineCombo.value = null;
    }
  }).catch(err => {
    console.error("Lỗi lấy routine", err);
    routineCombo.value = null;
  });

  // Lưu kết quả vào DB ngầm
  saveQuizResult({ idLoaiDa: topSkinTypes[0] }).catch(err => console.error("Lỗi lưu quiz:", err));

  setTimeout(() => { analyzing.value = false; showResult.value = true; }, 2500);
};

const recommendProducts = (filters = []) => {
  const ranked = rankProductsByQuiz(allProducts.value, {
    scoreMap: scoreMap.value,
    filters,
  });
  // Màn kết quả quiz giữ layout top 4; trang /san-pham/goi-y hiện toàn bộ
  recommendedProducts.value = ranked.slice(0, 4);
};

// ============================================
// NAVIGATION HELPERS
// ============================================
const formatPrice = (p) => p ? Math.round(p).toLocaleString('vi-VN') + ' đ' : '0 đ';
const getImageUrl = (path) => { if (!path) return ''; return (path.startsWith('http') || path.startsWith('/')) ? path : `/uploads/${path}`; };

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
  routineCombo.value = null;
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
  --sq-cream: #ffffff;
  --sq-warm-white: #ffffff;
  --sq-gold: #c9a96e;
  --sq-gold-dark: #9e7340;
  --sq-gold-light: #e5d3ab;
  --sq-sand: #e8dcc8;
  --sq-text-muted: #5a4f46;
  --sq-border: #3e3228;

  font-family: 'Be Vietnam Pro', 'Inter', sans-serif;
  min-height: 100%;
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  background: radial-gradient(ellipse at 50% 12%, #fffdf8 0%, #fbf6ec 45%, #f5ece0 100%);
  position: relative;
  overflow: hidden;
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
   BONG BÓNG BAY LƠ LỬNG NỀN XUNG QUANH QUIZ
   ============================================ */
.sg-quiz-bubbles {
  position: absolute;
  inset: 0;
  overflow: hidden;
  pointer-events: none;
  z-index: 0;
}

.sg-quiz-bubble {
  position: absolute;
  bottom: -130px;
  animation-name: bubble-rise;
  animation-timing-function: linear;
  animation-iteration-count: infinite;
  will-change: bottom, opacity;
  pointer-events: none;
}

.sg-quiz-bubble__inner {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  overflow: hidden;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0;
  background: radial-gradient(
    circle at 35% 28%,
    rgba(255, 255, 255, 0.98) 0%,
    rgba(255, 255, 255, 0.85) 40%,
    rgba(250, 244, 235, 0.6) 70%,
    rgba(201, 169, 110, 0.35) 100%
  );
  border: 1.5px solid rgba(255, 255, 255, 0.95);
  box-shadow:
    inset 0 0 14px rgba(255, 255, 255, 0.85),
    inset -3px -3px 8px rgba(201, 169, 110, 0.2),
    0 8px 22px rgba(36, 26, 18, 0.08),
    0 2px 6px rgba(201, 169, 110, 0.14);
  backdrop-filter: blur(4px);
  -webkit-backdrop-filter: blur(4px);
  animation-name: bubble-sway;
  animation-duration: var(--bubble-sway-duration, 4s);
  animation-timing-function: ease-in-out;
  animation-iteration-count: infinite;
  animation-direction: alternate;
  will-change: transform;
}

/* Ánh sáng bóng sáng góc trên bên trái */
.sg-quiz-bubble__specular {
  position: absolute;
  top: 8%;
  left: 12%;
  width: 34%;
  height: 22%;
  border-radius: 50%;
  background: radial-gradient(
    ellipse at center,
    rgba(255, 255, 255, 0.98) 0%,
    rgba(255, 255, 255, 0.65) 45%,
    rgba(255, 255, 255, 0) 80%
  );
  transform: rotate(-35deg);
  pointer-events: none;
  z-index: 4;
}

/* Viền xà cừ óng ánh tinh tế */
.sg-quiz-bubble__rim {
  position: absolute;
  inset: 0;
  border-radius: 50%;
  background: radial-gradient(
    circle at 75% 78%,
    rgba(201, 169, 110, 0.3) 0%,
    rgba(255, 215, 140, 0.15) 40%,
    transparent 70%
  );
  box-shadow: inset 0 0 10px rgba(255, 255, 255, 0.7);
  pointer-events: none;
  z-index: 3;
}

/* Ảnh sản phẩm bo tròn 100% theo hình dạng bong bóng */
.sg-quiz-bubble__img {
  width: 100%;
  height: 100%;
  border-radius: 50% !important;
  object-fit: cover !important;
  user-select: none;
  pointer-events: none;
  z-index: 2;
  display: block;
  transform: scale(0.92);
  transition: transform 0.3s ease;
}

/* Keyframes bong bóng bay lên */
@keyframes bubble-rise {
  0% {
    bottom: -130px;
    opacity: 0;
  }
  5% {
    opacity: var(--bubble-opacity, 0.85);
  }
  88% {
    opacity: var(--bubble-opacity, 0.85);
  }
  100% {
    bottom: 108%;
    opacity: 0;
  }
}

/* Keyframes lắc lư uốn lượn */
@keyframes bubble-sway {
  0% {
    transform: translateX(-16px) rotate(-6deg) scale(0.96);
  }
  50% {
    transform: translateX(12px) rotate(4deg) scale(1.02);
  }
  100% {
    transform: translateX(-14px) rotate(-5deg) scale(0.98);
  }
}

/* ============================================
   LANDING PAGE — LUXURY EDITORIAL
   ============================================ */
.sg-landing {
  flex: 1;
  width: 100%;
  max-width: 1140px;
  margin: 0 auto;
  padding: 40px 24px 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  z-index: 1;
}

.sg-landing__card {
  width: 100%;
  background: rgba(255, 255, 255, 0.72);
  backdrop-filter: blur(14px);
  -webkit-backdrop-filter: blur(14px);
  border: 1px solid rgba(201, 169, 110, 0.32);
  border-radius: 28px;
  padding: 56px 48px 44px;
  box-shadow: 0 20px 60px rgba(36, 26, 18, 0.06), 0 4px 16px rgba(201, 169, 110, 0.08);
}

.sg-landing__content {
  display: flex;
  align-items: center;
  gap: 60px;
  margin-bottom: 48px;
}

/* VISUAL BÊN TRÁI */
.sg-landing__visual {
  position: relative;
  flex-shrink: 0;
  width: 380px;
  height: 380px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.sg-visual-orbit {
  position: relative;
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.sg-visual-orbit__glow {
  position: absolute;
  width: 280px;
  height: 280px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(201, 169, 110, 0.3) 0%, rgba(201, 169, 110, 0.06) 60%, transparent 80%);
  filter: blur(24px);
  animation: visual-pulse 4s ease-in-out infinite;
}

.sg-visual-orbit__ring {
  position: absolute;
  width: 310px;
  height: 310px;
  border-radius: 50%;
  border: 1px dashed rgba(201, 169, 110, 0.45);
  animation: visual-spin 35s linear infinite;
}

.sg-visual-orbit__ring-outer {
  position: absolute;
  width: 360px;
  height: 360px;
  border-radius: 50%;
  border: 1px solid rgba(201, 169, 110, 0.18);
}

.sg-visual-center {
  position: relative;
  z-index: 2;
  width: 220px;
  height: 220px;
  border-radius: 50%;
  background: linear-gradient(145deg, #ffffff 0%, #fdfbf7 50%, #f5ebd7 100%);
  border: 2px solid rgba(201, 169, 110, 0.55);
  box-shadow: 0 16px 40px rgba(36, 26, 18, 0.1), inset 0 2px 6px rgba(255, 255, 255, 0.9);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;
  padding: 16px;
  transition: transform 0.4s ease;
}

.sg-visual-center:hover {
  transform: scale(1.03);
}

.sg-visual-center__logo {
  width: 92px;
  height: 92px;
  object-fit: contain;
  margin-bottom: 6px;
  filter: drop-shadow(0 4px 12px rgba(201, 169, 110, 0.35));
  animation: sun-float 3s ease-in-out infinite;
}

.sg-visual-center__brand {
  font-family: 'Playfair Display', serif;
  font-size: 18px;
  font-weight: 700;
  letter-spacing: 0.22em;
  color: var(--sq-espresso);
  text-transform: uppercase;
  line-height: 1.1;
}

.sg-visual-center__pa {
  font-size: 10px;
  font-weight: 700;
  letter-spacing: 0.14em;
  color: var(--sq-gold-dark);
  margin-top: 4px;
}

/* Floating Pills */
.sg-visual-pill {
  position: absolute;
  z-index: 3;
  display: inline-flex;
  align-items: center;
  gap: 7px;
  padding: 7px 14px;
  background: rgba(255, 255, 255, 0.94);
  backdrop-filter: blur(8px);
  -webkit-backdrop-filter: blur(8px);
  border: 1px solid rgba(201, 169, 110, 0.35);
  border-radius: 999px;
  box-shadow: 0 6px 18px rgba(36, 26, 18, 0.08);
  font-size: 11.5px;
  font-weight: 600;
  color: var(--sq-espresso);
  white-space: nowrap;
  animation: pill-float 4s ease-in-out infinite;
}

.sg-pill-icon {
  color: var(--sq-gold-dark);
  flex-shrink: 0;
}

.sg-visual-pill--top {
  top: 15px;
  right: -10px;
  animation-delay: 0s;
}

.sg-visual-pill--bottom {
  bottom: 25px;
  right: -20px;
  animation-delay: 1.5s;
}

.sg-visual-pill--side {
  bottom: 40px;
  left: -20px;
  animation-delay: 2.5s;
}

@keyframes visual-spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

@keyframes visual-pulse {
  0%, 100% { transform: scale(1); opacity: 0.8; }
  50% { transform: scale(1.08); opacity: 1; }
}

@keyframes sun-float {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-3px); }
}

@keyframes pill-float {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-4px); }
}

/* NỘI DUNG BÊN PHẢI */
.sg-landing__text {
  flex: 1;
}

.sg-landing__kicker {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 5px 14px;
  border-radius: 999px;
  background: rgba(201, 169, 110, 0.12);
  border: 1px solid rgba(201, 169, 110, 0.35);
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.14em;
  color: var(--sq-gold-dark);
  text-transform: uppercase;
  margin-bottom: 16px;
}

.sg-landing__kicker-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: var(--sq-gold);
  box-shadow: 0 0 6px var(--sq-gold);
}

.sg-landing__title {
  font-family: 'Playfair Display', serif;
  font-size: 42px;
  font-weight: 700;
  color: var(--sq-espresso);
  line-height: 1.18;
  margin: 0 0 16px;
  letter-spacing: -0.5px;
}

.sg-landing__title-accent {
  display: block;
  font-style: italic;
  font-weight: 600;
  color: var(--sq-gold-dark);
}

.sg-landing__desc {
  font-size: 15px;
  color: var(--sq-text-muted);
  line-height: 1.7;
  margin: 0 0 28px;
  max-width: 520px;
}

/* CTA */
.sg-landing__cta-wrap {
  display: flex;
  flex-direction: column;
  gap: 12px;
  align-items: flex-start;
}

.sg-landing__cta {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  background: linear-gradient(135deg, #241a12 0%, #3a2a1e 100%);
  color: #fffdf8;
  border: 1px solid rgba(201, 169, 110, 0.4);
  padding: 16px 36px;
  font-size: 13.5px;
  font-weight: 700;
  letter-spacing: 0.12em;
  text-transform: uppercase;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.16, 1, 0.3, 1);
  box-shadow: 0 8px 24px rgba(36, 26, 18, 0.22);
}

.sg-landing__cta:hover:not(:disabled) {
  transform: translateY(-2px);
  background: linear-gradient(135deg, #1a1412 0%, #2e2016 100%);
  box-shadow: 0 12px 32px rgba(36, 26, 18, 0.32);
  border-color: var(--sq-gold);
}

.sg-landing__cta-icon {
  transition: transform 0.25s;
}

.sg-landing__cta:hover .sg-landing__cta-icon {
  transform: translateX(4px);
}

.sg-landing__cta:disabled {
  opacity: 0.55;
  cursor: not-allowed;
}

.sg-landing__cta-sub {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 12.5px;
  color: var(--sq-gold-dark);
  font-weight: 500;
}

/* 3 TRUST FEATURES BÊN DƯỚI */
.sg-landing__features {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 24px;
  padding-top: 36px;
  border-top: 1px solid rgba(201, 169, 110, 0.2);
}

.sg-feature-item {
  display: flex;
  align-items: flex-start;
  gap: 14px;
}

.sg-feature-item__icon {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  background: rgba(201, 169, 110, 0.12);
  border: 1px solid rgba(201, 169, 110, 0.3);
  color: var(--sq-gold-dark);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.sg-feature-item__body strong {
  display: block;
  font-size: 14px;
  font-weight: 700;
  color: var(--sq-espresso);
  margin-bottom: 3px;
}

.sg-feature-item__body p {
  font-size: 12.5px;
  color: var(--sq-text-muted);
  line-height: 1.5;
  margin: 0;
}

/* ============================================
   QUIZ MAIN AREA
   ============================================ */
.sg-quiz {
  flex: 1;
  padding: 30px 24px 60px;
  display: flex; flex-direction: column; align-items: center;
  position: relative;
  z-index: 1;
}

/* Progress */
.sg-quiz__header { width: 100%; max-width: 800px; margin-bottom: 40px; }
.sg-progress { display: flex; align-items: center; gap: 16px; }
.sg-progress__back {
  background: none; border: 1.5px solid rgba(249,245,240,0.25); border-radius: 50%;
  width: 42px; height: 42px; color: var(--sq-espresso); cursor: pointer;
  display: flex; align-items: center; justify-content: center; transition: all 0.3s; flex-shrink: 0;
}
.sg-progress__back:hover { background: rgba(36,26,18,0.1); }
.sg-progress__back-placeholder { width: 42px; flex-shrink: 0; }
.sg-progress__track { flex: 1; height: 10px; background: rgba(249,245,240,0.12); border-radius: 10px; position: relative; overflow: visible; }
.sg-progress__fill { height: 100%; background: var(--sq-espresso); border-radius: 10px; transition: width 0.5s ease-out; position: relative; min-width: 10px; }
.sg-progress__sun { position: absolute; top: 50%; margin-top: -18px; right: -18px; font-size: 36px; line-height: 1; filter: drop-shadow(0 2px 8px rgba(201,169,110,0.5)); animation: sun-spin 4s linear infinite; }
@keyframes sun-spin { 0% { transform: rotate(0deg); } 100% { transform: rotate(360deg); } }

/* Question Layout */
.sg-question { width: 100%; max-width: 960px; display: flex; flex-direction: column; align-items: center; }
.sg-question__layout { width: 100%; display: flex; gap: 40px; }
.sg-question__layout:not(.has-quote) { justify-content: center; }
.sg-question__layout:not(.has-quote) .sg-question__main { max-width: 800px; }

/* Quote bên trái */
.sg-question__quote-col { flex: 0 0 260px; display: flex; align-items: center; }
.sg-quote { margin: 0; padding: 0; border: none; text-align: center; }
.sg-quote__text { font-size: 15px; font-style: italic; color: var(--sq-espresso); line-height: 1.7; margin: 0 0 16px; opacity: 0.8; }
.sg-quote__author { display: flex; flex-direction: column; gap: 2px; }
.sg-quote__author strong { font-size: 12px; letter-spacing: 2px; color: var(--sq-espresso); }
.sg-quote__author span { font-size: 12px; color: var(--sq-text-muted); }

/* Khối câu hỏi chính */
.sg-question__main { flex: 1; text-align: center; }
.sg-question__title { font-family: 'Playfair Display', serif; font-size: 28px; font-weight: 500; color: var(--sq-espresso); margin: 0 0 8px; line-height: 1.35; }
.sg-question__hint { font-size: 14px; font-weight: 600; color: var(--sq-espresso); margin: 0 0 28px; opacity: 0.8; }

.sg-answers { display: flex; justify-content: center; gap: 14px; flex-wrap: wrap; margin-bottom: 24px; }
.sg-answers--2 .sg-answer-card { width: 220px; min-height: 130px; }
.sg-answers--3 .sg-answer-card { width: 200px; min-height: 130px; }
.sg-answers--4 .sg-answer-card { width: 180px; min-height: 130px; }
.sg-answers--list { flex-direction: column; max-width: 500px; margin-left: auto; margin-right: auto; }
.sg-answers--list .sg-answer-card { width: 100%; min-height: auto; padding: 16px 24px; text-align: left; flex-direction: row; justify-content: flex-start; }

.sg-answer-card {
  background: var(--sq-cream); border: 2px solid transparent; border-radius: 12px;
  padding: 24px 20px; cursor: pointer; transition: all 0.25s;
  display: flex; flex-direction: column; align-items: center; justify-content: center; text-align: center;
  box-shadow: 0 2px 12px rgba(0,0,0,0.15);
}
.sg-answer-card:hover:not(.sg-answer-card--selected) { transform: translateY(-4px); box-shadow: 0 8px 24px rgba(0,0,0,0.2); border-color: var(--sq-espresso); }
.sg-answer-card--selected { border-color: var(--sq-espresso); background: var(--sq-warm-white); box-shadow: 0 0 0 2px var(--sq-espresso); }
.sg-answer-card__label { font-size: 16px; font-weight: 600; color: var(--sq-espresso); margin: 0; line-height: 1.5; }

.sg-answer-card__icon {
  font-size: 32px;
  color: var(--sq-gold-dark);
  transition: all 0.3s;
}
.sg-answers--list .sg-answer-card__icon {
  margin-bottom: 0;
  margin-right: 16px;
}
.sg-answer-card--selected .sg-answer-card__icon {
  color: var(--sq-espresso);
}

/* Next Button */
.sg-question__footer { margin-top: 8px; }
.sg-btn-next {
  background: var(--sq-espresso); color: var(--sq-cream); border: none;
  padding: 14px 40px; font-size: 13px; font-weight: 700; letter-spacing: 1.5px;
  border-radius: 8px; cursor: pointer; transition: all 0.25s;
}
.sg-btn-next:hover:not(:disabled) { background: var(--sq-dark); color: var(--sq-cream); transform: translateY(-2px); }
.sg-btn-next:disabled { opacity: 0.35; cursor: not-allowed; }

/* WHY WE ASK — LUXURY FROSTED IVORY & GOLD */
.sg-why {
  position: relative;
  max-width: 540px;
  width: 100%;
  margin: 36px auto 0;
}

.sg-why__badge {
  position: absolute;
  top: -13px;
  right: 18px;
  z-index: 2;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: linear-gradient(135deg, #e4cca0 0%, #c9a96e 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 15px;
  font-weight: 800;
  color: #ffffff;
  border: 2px solid #ffffff;
  box-shadow: 0 4px 12px rgba(201, 169, 110, 0.35);
}

.sg-why__card {
  background: linear-gradient(145deg, rgba(255, 255, 255, 0.94) 0%, rgba(254, 250, 244, 0.88) 100%);
  backdrop-filter: blur(14px);
  -webkit-backdrop-filter: blur(14px);
  border: 1px solid rgba(201, 169, 110, 0.35);
  border-radius: 16px;
  padding: 18px 24px;
  box-shadow: 0 10px 30px rgba(36, 26, 18, 0.05), 0 2px 8px rgba(201, 169, 110, 0.07);
  text-align: left;
}

.sg-why__title {
  font-size: 11.5px;
  font-weight: 800;
  letter-spacing: 1.4px;
  margin: 0 0 6px;
  color: var(--sq-gold-dark);
}

.sg-why__text {
  font-size: 13.5px;
  line-height: 1.6;
  margin: 0;
  color: var(--sq-espresso);
  opacity: 0.88;
}

/* ============================================
   ANALYZING
   ============================================ */
.sg-analyzing { flex: 1; display: flex; flex-direction: column; justify-content: center; align-items: center; text-align: center; padding-top: 80px; padding-bottom: 80px; width: 100%; }
.sg-analyzing__spinner { font-size: 80px; margin-bottom: 24px; animation: sun-spin 2s linear infinite; filter: drop-shadow(0 0 15px rgba(36,26,18,0.3));}
.sg-analyzing__title { font-size: 28px; font-weight: 600; color: var(--sq-espresso); margin: 0 0 12px; }
.sg-analyzing__desc { font-size: 15px; color: var(--sq-text-muted); margin: 0 0 24px; }
.sg-analyzing__dots { display: flex; justify-content: center; gap: 8px; }
.sg-analyzing__dots span { width: 10px; height: 10px; border-radius: 50%; background: var(--sq-espresso); animation: dot-b 1.4s infinite ease-in-out; }
.sg-analyzing__dots span:nth-child(2) { animation-delay: 0.16s; }
.sg-analyzing__dots span:nth-child(3) { animation-delay: 0.32s; }
@keyframes dot-b { 0%,80%,100% { transform: scale(0.6); opacity: 0.3; } 40% { transform: scale(1); opacity: 1; } }

/* ============================================
   RESULT - HERO PRODUCT
   ============================================ */
.sg-result { flex: 1; width: 100%; max-width: 900px; display: flex; flex-direction: column; align-items: center; padding: 40px 0 60px; }

.sg-result__hero-product { width: 100%; margin-bottom: 40px; text-align: center; }
.sg-result__hero-label { font-family: 'Playfair Display', serif; font-size: 32px; font-weight: 700; color: var(--sq-espresso); margin: 0 0 24px; letter-spacing: 1px; }

.sg-hero-card {
  display: flex; background: var(--sq-cream); border-radius: 16px; overflow: hidden;
  box-shadow: 0 10px 40px rgba(0,0,0,0.3); cursor: pointer; transition: transform 0.3s;
  text-align: left;
}
.sg-hero-card:hover { transform: translateY(-5px); }

.sg-hero-card__img { flex: 0 0 50%; aspect-ratio: 1; background: var(--sq-sand); display: flex; align-items: center; justify-content: center; }
.sg-hero-card__img img { width: 100%; height: 100%; object-fit: cover; }
.sg-hero-card__placeholder { font-family: 'Playfair Display', serif; font-size: 32px; font-weight: 700; color: var(--sq-espresso); letter-spacing: 2px; opacity: 0.5; }

.sg-hero-card__info { flex: 1; padding: 40px; display: flex; flex-direction: column; justify-content: center; }
.sg-hero-card__name { font-family: 'Playfair Display', serif; font-size: 28px; font-weight: 700; color: var(--sq-espresso); margin: 0 0 12px; line-height: 1.2; }
.sg-hero-card__price { font-size: 18px; font-weight: 700; color: var(--sq-gold-dark); margin: 0 0 20px; }
.sg-hero-card__desc { font-size: 14px; color: #5a4f46; line-height: 1.7; margin: 0 0 30px; }
.sg-btn-buy { background: var(--sq-espresso); color: var(--sq-cream); border: none; padding: 16px 32px; font-size: 13px; font-weight: 700; letter-spacing: 1.5px; border-radius: 8px; cursor: pointer; transition: background 0.3s; align-self: flex-start; }
.sg-btn-buy:hover { background: var(--sq-dark); color: var(--sq-cream); }

/* GIẢI THÍCH KẾT QUẢ (HOW WE FORMULATE) */
.sg-result__explanation {
  width: 100%; display: grid; grid-template-columns: repeat(3, 1fr); gap: 24px;
  background: var(--sq-cream); border-radius: 12px; padding: 30px; margin-bottom: 60px;
}
.sg-explanation-col h4 { font-size: 12px; font-weight: 800; letter-spacing: 1.5px; color: var(--sq-espresso); margin: 0 0 12px; border-bottom: 1px solid rgba(0,0,0,0.1); padding-bottom: 8px; }
.sg-explanation-col p { font-size: 13px; color: #5a4f46; line-height: 1.6; margin: 0; }
.sg-skin-highlight { font-weight: 700; color: var(--sq-gold-dark) !important; margin-bottom: 6px !important; }

/* PHÂN BỐ ĐIỂM Ở TRONG EXPLANATION */
.sg-result__score-list { display: flex; flex-direction: column; gap: 8px; }
.sg-score-row { display: flex; align-items: center; gap: 8px; }
.sg-score-row__label { width: 80px; font-size: 12px; color: var(--sq-espresso); font-weight: 600; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.sg-score-row__bar { flex: 1; height: 6px; background: rgba(0,0,0,0.06); border-radius: 4px; }
.sg-score-row__fill { height: 100%; background: var(--sq-gold); border-radius: 4px; }

/* ROUTINE CHÉO */
.sg-result__routine { width: 100%; margin-bottom: 50px; text-align: center; }
.sg-result__routine-title { font-family: 'Playfair Display', serif; font-size: 28px; font-weight: 700; color: var(--sq-espresso); margin: 0 0 8px; }
.sg-result__routine-subtitle { font-size: 14px; color: var(--sq-text-muted); margin: 0 0 30px; }

.sg-routine-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 20px; }
.sg-routine-card { background: var(--sq-cream); border-radius: 12px; padding: 20px; cursor: pointer; transition: transform 0.3s; position: relative; }
.sg-routine-card:hover { transform: translateY(-5px); }
.sg-routine-step { position: absolute; top: -12px; left: 50%; transform: translateX(-50%); background: var(--sq-gold); color: var(--sq-espresso); font-size: 11px; font-weight: 800; padding: 4px 12px; border-radius: 20px; letter-spacing: 1px; box-shadow: 0 4px 10px rgba(0,0,0,0.2); z-index: 2; }
.sg-routine-card__img { width: 100%; aspect-ratio: 1; margin-bottom: 16px; display: flex; align-items: center; justify-content: center; }
.sg-routine-card__img img { width: 100%; height: 100%; object-fit: contain; }
.sg-routine-card__placeholder { font-weight: 700; opacity: 0.3; }
.sg-routine-card__name { font-size: 13px; font-weight: 600; color: var(--sq-espresso); margin: 0 0 12px; line-height: 1.4; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; }
.sg-routine-card__link { font-size: 12px; font-weight: 700; color: var(--sq-gold-dark); text-decoration: none; border-bottom: 1px solid var(--sq-gold-dark); padding-bottom: 2px; }

/* ACTIONS */
.sg-result__actions { display: flex; gap: 14px; flex-wrap: wrap; justify-content: center; margin-top: 10px; margin-bottom: 20px; }
.sg-btn-primary { background: var(--sq-espresso); color: var(--sq-cream); border: none; padding: 14px 36px; font-size: 13px; font-weight: 700; letter-spacing: 1.5px; border-radius: 8px; cursor: pointer; transition: all 0.25s; }
.sg-btn-primary:hover { background: var(--sq-dark); color: var(--sq-cream); transform: translateY(-2px); }
.sg-btn-outline { background: transparent; border: 2px solid var(--sq-espresso); color: var(--sq-espresso); padding: 14px 36px; font-size: 13px; font-weight: 700; letter-spacing: 1.5px; border-radius: 8px; cursor: pointer; transition: all 0.25s; }
.sg-btn-outline:hover { background: rgba(36,26,18,0.08); transform: translateY(-2px); }

/* TRANSITIONS */
.sg-slide-enter-active, .sg-slide-leave-active { transition: all 0.4s ease; }
.sg-slide-enter-from { opacity: 0; transform: translateX(40px); }
.sg-slide-leave-to { opacity: 0; transform: translateX(-40px); }

/* Nút Đóng */
.quiz-close-btn {
  position: fixed; top: 20px; right: 24px; z-index: 100;
  background: transparent; border: 1.5px solid rgba(36,26,18,0.3); color: var(--sq-espresso);
  width: 44px; height: 44px; border-radius: 50%; font-size: 24px;
  cursor: pointer; display: flex; align-items: center; justify-content: center; transition: all 0.3s;
}
.quiz-close-btn:hover { background: rgba(36,26,18,0.1); border-color: var(--sq-espresso); transform: scale(1.05); }

/* RESPONSIVE */
@media (max-width: 960px) {
  .sg-landing { padding: 24px 16px 40px; }
  .sg-landing__card { padding: 36px 20px 32px; border-radius: 20px; }
  .sg-landing__content { flex-direction: column; text-align: center; gap: 36px; margin-bottom: 36px; }
  .sg-landing__visual { width: 300px; height: 300px; }
  .sg-visual-orbit__ring { width: 250px; height: 250px; }
  .sg-visual-orbit__ring-outer { width: 290px; height: 290px; }
  .sg-visual-center { width: 180px; height: 180px; }
  .sg-visual-center__logo { width: 76px; height: 76px; }
  .sg-visual-center__brand { font-size: 15px; }
  .sg-visual-pill--top { top: 0; right: 0; }
  .sg-visual-pill--bottom { bottom: 0; right: 0; }
  .sg-visual-pill--side { display: none; }
  .sg-landing__title { font-size: 30px; }
  .sg-landing__desc { margin-left: auto; margin-right: auto; font-size: 14px; }
  .sg-landing__cta-wrap { align-items: center; }
  .sg-landing__cta { width: 100%; max-width: 320px; justify-content: center; }
  .sg-landing__features { grid-template-columns: 1fr; gap: 16px; padding-top: 24px; }
  .sg-feature-item { justify-content: center; text-align: left; }
  .sg-question__layout.has-quote { flex-direction: column; gap: 24px; }
  .sg-question__quote-col { display: none; }
  .sg-question__title { font-size: 22px; }
}

@media (max-width: 640px) {
  .sg-answers--2 .sg-answer-card, .sg-answers--3 .sg-answer-card, .sg-answers--4 .sg-answer-card { width: calc(50% - 8px); min-height: 100px; padding: 16px 12px; }
  .sg-answer-card__label { font-size: 14px; }
  .sg-product-grid { grid-template-columns: repeat(2, 1fr); }
  .sg-landing__title { font-size: 24px; }
  .sg-progress__sun { font-size: 28px; top: -12px; right: -14px; }
  .sg-result__title { font-size: 22px; }
  .sg-result__skin-name { font-size: 24px; }
}


</style>
