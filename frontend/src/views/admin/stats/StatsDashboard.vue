<template>
  <div class="sunova-dashboard">
    <div class="dash-header">
      <h1 class="page-title">Thống kê & Báo cáo</h1>
    </div>

    <div class="advanced-filter-section">
      <div class="filter-info">
        <h3>Bộ Lọc Tìm Kiếm</h3>
        <p>Chọn khoảng thời gian để xem số liệu</p>
      </div>
      <div class="filter-controls">
        <div class="quick-tags">
          <button :class="['tag', { active: activeFilter === 'Hôm nay' }]" @click="applyQuickFilter('Hôm nay')">Hôm nay</button>
          <button :class="['tag', { active: activeFilter === 'Tuần này' }]" @click="applyQuickFilter('Tuần này')">Tuần này</button>
          <button :class="['tag', { active: activeFilter === 'Tháng này' }]" @click="applyQuickFilter('Tháng này')">Tháng này</button>
          <button :class="['tag', { active: activeFilter === 'Năm nay' }]" @click="applyQuickFilter('Năm nay')">Năm nay</button>
          <button :class="['tag', { active: activeFilter === 'Tùy chỉnh' }]" @click="applyQuickFilter('Tùy chỉnh')">Tùy chỉnh</button>
        </div>
        
        <div class="export-actions">
          <button class="btn-outline" @click="sendEmailReport">✉ Gửi Báo Cáo</button>
          <button class="btn-outline" @click="exportToExcel">⬇ Xuất Excel</button>
        </div>
      </div>
    </div>

    <div class="custom-date-row" v-if="activeFilter === 'Tùy chỉnh'">
      <div class="date-picker"><label>Từ ngày:</label><input type="date" v-model="fromDate" class="sunova-input" /></div>
      <div class="date-picker"><label>Đến ngày:</label><input type="date" v-model="toDate" class="sunova-input" /></div>
      <button class="btn-filter" @click="fetchRealData">🔍 Lọc dữ liệu</button>
    </div>

    <div class="summary-grid">
      <div class="sunova-card">
        <div class="card-top">
          <span class="card-name">Tổng Doanh Thu</span>
          <div class="card-icon"><span>💰</span></div>
        </div>
        <div class="card-bottom">
          <h2>{{ summaryMetrics.totalRevenue }}</h2>
          <p class="trend">↗ Thực tế + Dự kiến</p>
        </div>
      </div>

      <div class="sunova-card">
        <div class="card-top">
          <span class="card-name">Doanh Thu Thực Tế</span>
          <div class="card-icon"><span>💳</span></div>
        </div>
        <div class="card-bottom">
          <h2>{{ summaryMetrics.actualRevenue }}</h2>
          <p class="trend" style="color: #28a745;">✓ Đã thanh toán (Hoàn thành)</p>
        </div>
      </div>

      <div class="sunova-card">
        <div class="card-top">
          <span class="card-name">Doanh Thu Dự Kiến</span>
          <div class="card-icon"><span>⏳</span></div>
        </div>
        <div class="card-bottom">
          <h2>{{ summaryMetrics.expectedRevenue }}</h2>
          <p class="trend" style="color: #d97706;">... Đang giao & Chờ xác nhận</p>
        </div>
      </div>

      <div class="sunova-card">
        <div class="card-top">
          <span class="card-name">Số Đơn Hàng</span>
          <div class="card-icon"><span>📦</span></div>
        </div>
        <div class="card-bottom">
          <h2>{{ summaryMetrics.totalOrders }}</h2>
          <div class="omni-stats">
            <div class="omni-item"><span>🌐</span> Web: <strong>{{ summaryMetrics.webOrders }}</strong></div>
            <div class="omni-item"><span>🏪</span> POS: <strong>{{ summaryMetrics.posOrders }}</strong></div>
          </div>
          <p class="trend aov-text">
            🛒 TB/Đơn (AOV): <strong>{{ summaryMetrics.aov }}</strong>
          </p>
        </div>
      </div>
    </div>

    <div class="main-grid">
      <div class="panel main-chart-panel">
        <div class="panel-header chart-header-custom">
          <div class="title-group"><h3>Phân tích Doanh thu Thực tế</h3></div>
          <div class="chart-tabs">
            <button class="tab" :class="{ active: currentChartType === 'bar' }" @click="toggleChartType('bar')">Biểu đồ Cột</button>
            <button class="tab" :class="{ active: currentChartType === 'area' }" @click="toggleChartType('area')">Biểu đồ Sóng</button>
          </div>
        </div>
        <div class="chart-container">
          <apexchart :type="currentChartType" height="320" :options="barOptions" :series="barSeries"></apexchart>
        </div>
      </div>

      <div class="side-column">
        <div class="panel insight-panel">
          <div class="panel-header"><h3>Cơ Cấu Dòng Tiền</h3><span class="dots">⋮</span></div>
          <div class="insight-list">
            <div class="insight-item">
              <div class="platform-info"><span class="platform">Chuyển khoản (VNPay/QR)</span><span class="platform-percent">{{ paymentStats.transferPercent }}%</span></div>
              <div class="progress-bar"><div class="progress-fill" :style="{width: paymentStats.transferPercent + '%'}"></div></div>
            </div>
            <div class="insight-item">
              <div class="platform-info"><span class="platform">Tiền mặt (COD/Tại quầy)</span><span class="platform-percent">{{ paymentStats.cashPercent }}%</span></div>
              <div class="progress-bar"><div class="progress-fill" :style="{width: paymentStats.cashPercent + '%'}"></div></div>
            </div>
          </div>
        </div>

        <div class="panel latest-panel">
          <div class="panel-header"><h3>Top 5 SP Bán Chạy Nhất</h3><span class="dots">⋮</span></div>
          <div class="pie-chart-container">
            <apexchart type="pie" height="360" :options="pieOptions" :series="pieSeries"></apexchart>
          </div>
        </div>
      </div>
    </div>

    <div class="bottom-grid">
      <div class="panel mini-chart-panel">
        <div class="mini-info">
          <span class="mini-label">Hiệu Quả Khuyến Mãi (Voucher)</span>
          <h3>Đã giảm: {{ summaryMetrics.voucherDiscount }}</h3>
          <span class="trend negative">↗ Thu hút {{ summaryMetrics.voucherUsage }} lượt dùng mã</span>
        </div>
        <div class="mini-graph">
          <apexchart type="area" height="80" width="120" :options="sparkBrownOptions" :series="sparkBrownSeries1"></apexchart>
        </div>
      </div>

      <div class="panel mini-chart-panel">
        <div class="mini-info">
          <span class="mini-label">Insight Khách Hàng (Từ Quiz Da)</span>
          <h3>{{ summaryMetrics.quizTopSkin }}: {{ summaryMetrics.quizPercent }}</h3>
          <span class="trend">💡 Đề xuất: {{ summaryMetrics.quizAdvice }}</span>
        </div>
        <div class="mini-graph">
          <apexchart type="area" height="80" width="120" :options="sparkBrownOptions" :series="sparkBrownSeries2"></apexchart>
        </div>
      </div>
    </div>

  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import VueApexCharts from 'vue3-apexcharts';
import axios from 'axios';

const apexchart = VueApexCharts;
const currentChartType = ref('bar'); 
const activeFilter = ref('Năm nay'); 

const fromDate = ref('');
const toDate = ref('');

// ===============================================
// STATE CHỨA DỮ LIỆU ĐẦY ĐỦ 
// ===============================================
const summaryMetrics = ref({
  totalRevenue: '0đ', actualRevenue: '0đ', expectedRevenue: '0đ', 
  totalOrders: '0', webOrders: 0, posOrders: 0, aov: '0đ',
  voucherDiscount: '0đ', voucherUsage: '0',
  quizTopSkin: 'Chưa có dữ liệu', quizPercent: '0%', quizAdvice: 'Đợi khách làm bài test...'
});

const paymentStats = ref({ transferPercent: 0, cashPercent: 0 });
const rawChartData = ref([]);

const formatMoney = (amount) => {
  if (!amount || amount === 0) return '0đ';
  if (amount >= 1000000000) return (amount / 1000000000).toFixed(2).replace('.', ',') + ' Tỷ';
  if (amount >= 1000000) return Math.round(amount / 1000000) + ' Tr';
  return amount.toLocaleString('vi-VN') + ' đ';
};

const formatMoneyFull = (amount) => {
  if (!amount || amount === 0) return '0đ';
  return Math.round(amount).toLocaleString('vi-VN') + ' đ';
};

const applyQuickFilter = (type) => {
  activeFilter.value = type;
  if (type === 'Tùy chỉnh') return; 

  const today = new Date();
  let start = new Date();

  if (type === 'Hôm nay') start = today;
  else if (type === 'Tuần này') {
    const day = today.getDay();
    start = new Date(today.setDate(today.getDate() - day + (day === 0 ? -6 : 1)));
  } 
  else if (type === 'Tháng này') start = new Date(today.getFullYear(), today.getMonth(), 1);
  else if (type === 'Năm nay') start = new Date(today.getFullYear(), 0, 1);

  fromDate.value = start.toISOString().split('T')[0];
  toDate.value = new Date().toISOString().split('T')[0];

  fetchRealData(); 
};

// ===============================================
// XUẤT EXCEL & GỬI MAIL
// ===============================================
const exportToExcel = () => {
  if (rawChartData.value.length === 0) {
    alert("Không có dữ liệu để xuất Excel!");
    return;
  }
  let csvContent = "data:text/csv;charset=utf-8,\uFEFF";
  csvContent += "Thời gian,Doanh Thu Thực Tế (VNĐ)\n";
  rawChartData.value.forEach(item => {
    csvContent += `${item.name || item.NAME},${item.value || item.VALUE}\n`;
  });
  const encodedUri = encodeURI(csvContent);
  const link = document.createElement("a");
  link.setAttribute("href", encodedUri);
  link.setAttribute("download", `Bao_Cao_Sunova_${activeFilter.value}.csv`);
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
};

const sendEmailReport = () => {
  const subject = encodeURIComponent(`Báo Cáo Doanh Thu SUNOVA - ${activeFilter.value}`);
  const body = encodeURIComponent(
    `Kính gửi Giám đốc,\n\n` +
    `Dưới đây là tóm tắt doanh thu (${activeFilter.value}):\n` +
    `- Tổng doanh thu: ${summaryMetrics.value.totalRevenue}\n` +
    `- Doanh thu thực tế: ${summaryMetrics.value.actualRevenue}\n` +
    `- Doanh thu dự kiến: ${summaryMetrics.value.expectedRevenue}\n` +
    `- Tổng số đơn hàng: ${summaryMetrics.value.totalOrders} đơn\n` +
    `- Trung bình mỗi đơn (AOV): ${summaryMetrics.value.aov}\n\n` +
    `Trân trọng,\nHệ thống quản trị SUNOVA`
  );
  window.location.href = `mailto:?subject=${subject}&body=${body}`;
};

// ===============================================
// GỌI 6 API ĐỒNG THỜI ĐỂ LẤY DỮ LIỆU THẬT
// ===============================================
const fetchRealData = async () => {
  if (!fromDate.value || !toDate.value) return;

  const startStr = `${fromDate.value}T00:00:00`;
  const endStr = `${toDate.value}T23:59:59`;

  const config = { params: { fromDate: startStr, toDate: endStr } };
  const BASE_URL = 'http://localhost:8080/api/statistic';

  try {
    const [resSum, resTop, resFlow, resChart, resVoucher, resQuiz] = await Promise.all([
      axios.get(`${BASE_URL}/summary`, config),
      axios.get(`${BASE_URL}/top-products`, config),
      axios.get(`${BASE_URL}/payment-flow`, config),
      axios.get(`${BASE_URL}/chart`, config),
      axios.get(`${BASE_URL}/voucher`, config),
      axios.get(`${BASE_URL}/quiz`, config)
    ]);

    // 1. DATA TỔNG QUAN
    const dSum = resSum.data;
    summaryMetrics.value.totalRevenue = formatMoney(dSum.totalRevenue);
    summaryMetrics.value.actualRevenue = formatMoney(dSum.actualRevenue);
    summaryMetrics.value.expectedRevenue = formatMoney(dSum.expectedRevenue);
    summaryMetrics.value.totalOrders = dSum.totalOrders ? dSum.totalOrders.toLocaleString('vi-VN') : '0';
    summaryMetrics.value.webOrders = dSum.webOrders || 0;
    summaryMetrics.value.posOrders = dSum.posOrders || 0;
    const aovValue = dSum.totalOrders > 0 ? Math.round(dSum.actualRevenue / dSum.totalOrders) : 0;
    summaryMetrics.value.aov = formatMoneyFull(aovValue);

    // 2. DATA TOP 5 SẢN PHẨM
    if(resTop.data && resTop.data.length > 0) {
        pieOptions.value = { ...pieOptions.value, labels: resTop.data.map(item => item.name || item.NAME) };
        pieSeries.value = resTop.data.map(item => item.value || item.VALUE);
    } else {
        pieOptions.value = { ...pieOptions.value, labels: [] };
        pieSeries.value = [];
    }

    // 3. DATA DÒNG TIỀN
    let totalTransfer = 0; let totalCash = 0;
    resFlow.data.forEach(item => {
        const name = item.name || item.NAME;
        const val = item.value || item.VALUE;
        if (['VNPAY', 'MOMO', 'CHUYEN_KHOAN'].includes(name)) totalTransfer += val;
        if (['TIEN_MAT', 'COD'].includes(name)) totalCash += val;
    });
    const totalFlow = totalTransfer + totalCash;
    if(totalFlow > 0) {
        paymentStats.value.transferPercent = Math.round((totalTransfer / totalFlow) * 100);
        paymentStats.value.cashPercent = 100 - paymentStats.value.transferPercent;
    } else {
        paymentStats.value.transferPercent = 0; paymentStats.value.cashPercent = 0;
    }

    // 4. DATA BIỂU ĐỒ CHÍNH
    rawChartData.value = resChart.data; 
    const labels = []; const revenues = []; 
    resChart.data.forEach(item => {
        labels.push(item.name || item.NAME); 
        revenues.push(item.value || item.VALUE);
    });
    barOptions.value = { ...barOptions.value, xaxis: { ...barOptions.value.xaxis, categories: labels } };
    barSeries.value = [{ name: 'Doanh Thu Thực Tế', data: revenues }];

    // 5. DATA VOUCHER
    if(resVoucher.data && resVoucher.data.length > 0) {
        const vData = resVoucher.data[0];
        const tDiscount = vData.totalDiscount || vData.TOTALDISCOUNT || 0;
        const tUsage = vData.totalUsage || vData.TOTALUSAGE || 0;
        summaryMetrics.value.voucherDiscount = formatMoney(tDiscount);
        summaryMetrics.value.voucherUsage = tUsage;
    }

    // 6. DATA QUIZ (INSIGHT KHÁCH HÀNG)
    if(resQuiz.data && resQuiz.data.length > 0) {
        let totalQ = 0;
        resQuiz.data.forEach(q => totalQ += (q.value || q.VALUE));
        
        const topQ = resQuiz.data[0]; 
        const topName = topQ.name || topQ.NAME;
        const topVal = topQ.value || topQ.VALUE;
        const percent = totalQ > 0 ? Math.round((topVal / totalQ) * 100) : 0;
        
        summaryMetrics.value.quizTopSkin = topName;
        summaryMetrics.value.quizPercent = percent + '%';
        
        const nameLower = topName.toLowerCase();
        if(nameLower.includes('dầu')) summaryMetrics.value.quizAdvice = "Tăng nhập Sữa rửa mặt tạo bọt, KCN kiềm dầu";
        else if(nameLower.includes('khô')) summaryMetrics.value.quizAdvice = "Tăng nhập Kem dưỡng ẩm sâu";
        else if(nameLower.includes('nhạy cảm')) summaryMetrics.value.quizAdvice = "Tăng nhập dược mỹ phẩm làm dịu da";
        else summaryMetrics.value.quizAdvice = "Duy trì nhập hàng đều đặn các dòng cơ bản";
    } else {
        summaryMetrics.value.quizTopSkin = "Chưa có dữ liệu";
        summaryMetrics.value.quizPercent = "0%";
        summaryMetrics.value.quizAdvice = "Cần khuyến khích khách làm Quiz...";
    }

  } catch (error) {
    console.error("LỖI GỌI API THỐNG KÊ:", error);
  }
};

onMounted(() => {
  applyQuickFilter('Năm nay'); 
});

// ===============================================
// CẤU HÌNH BIỂU ĐỒ TRÒN
// ===============================================
const pieSeries = ref([]);
const pieOptions = ref({
  chart: { type: 'pie', fontFamily: 'Inter' },
  labels: [],
  colors: ['#4a3319', '#73522f', '#9a754b', '#c29d71', '#e0c8aa'],
  noData: { 
    text: "Chưa có dữ liệu bán hàng", 
    align: 'center', 
    verticalAlign: 'middle', 
    style: { color: '#8c6b4a', fontSize: '14px', fontFamily: 'Inter' } 
  },
  dataLabels: { enabled: true, formatter: function (val) { return Math.round(val) + "%"; } },
  legend: { show: true, position: 'bottom' }
});

// ===============================================
// CẤU HÌNH BIỂU ĐỒ CHÍNH
// ===============================================
const barSeries = ref([]);
const barOptions = ref({
  chart: { toolbar: { show: false }, fontFamily: 'Inter' },
  colors: ['#8c6b4a'], 
  plotOptions: { bar: { horizontal: false, columnWidth: '35%', borderRadius: 4 } },
  dataLabels: { enabled: false },
  stroke: { show: true, width: 3, curve: 'smooth', colors: ['transparent'] },
  xaxis: { categories: [], labels: { style: { colors: '#8c6b4a', fontWeight: 600 } } },
  yaxis: {
    min: 0,
    labels: {
      style: { colors: '#8c6b4a', fontSize: '13px', fontWeight: 600 },
      formatter: function(value) {
        if (!value || value === 0) return "0";
        if (value >= 1000000000) return (value / 1000000000).toFixed(1).replace('.0', '').replace('.', ',') + " Tỷ";
        if (value >= 1000000) return Math.round(value / 1000000) + " Tr";
        return Math.round(value).toLocaleString('vi-VN') + " đ";
      }
    }
  },
  tooltip: {
    y: { formatter: function (val) { return formatMoney(val); } }
  }
});

const toggleChartType = (type) => {
  currentChartType.value = type;
  if (type === 'area') {
    barOptions.value = { ...barOptions.value, stroke: { show: true, width: 3, curve: 'smooth', colors: ['#8c6b4a'] } };
  } else {
    barOptions.value = { ...barOptions.value, stroke: { show: true, width: 2, colors: ['transparent'] } };
  }
};

// ===============================================
// MINI SPARKLINE CHARTS (Chỉ giữ phần nền hình sóng sóng ở dưới để cho đẹp)
// ===============================================
const sparkBrownSeries1 = ref([{ data: [20, 30, 45, 60, 75, 85, 95] }]);
const sparkBrownSeries2 = ref([{ data: [12, 18, 25, 22, 35, 40, 55] }]);
const sparkBrownOptions = ref({ chart: { type: 'area', sparkline: { enabled: true } }, stroke: { curve: 'smooth', width: 2 }, fill: { opacity: 0.2 }, colors: ['#8c6b4a'], tooltip: { fixed: { enabled: false }, x: { show: false }, y: { title: { formatter: function () { return '' } } }, marker: { show: false } }});
</script>

<style scoped>
.sunova-dashboard { background-color: #f8f9fa; padding: 24px; min-height: 100vh; font-family: 'Inter', -apple-system, sans-serif; color: #5c4322; }
.dash-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px;}
.page-title { font-size: 24px; font-weight: bold; margin: 0; }

.advanced-filter-section { display: flex; justify-content: space-between; align-items: center; background: #ffffff; padding: 16px 24px; border-radius: 12px; margin-bottom: 24px; border: 1px solid #e6dcd3; }
.filter-info h3 { margin: 0 0 4px 0; font-size: 16px; color: #2d3748; font-weight: 700; }
.filter-info p { margin: 0; font-size: 13px; color: #718096; }

.filter-controls { display: flex; align-items: center; gap: 20px; flex-wrap: wrap;}
.quick-tags { display: flex; gap: 8px; }
.tag { background: #ffffff; border: 1px solid #e2e8f0; color: #4a5568; padding: 8px 16px; border-radius: 6px; font-size: 13px; font-weight: 500; cursor: pointer; }
.tag.active { background: #1a202c; color: #ffffff; border-color: #1a202c; }

.export-actions { display: flex; gap: 8px; }
.btn-outline { background: #ffffff; border: 1px solid #e2e8f0; color: #4a5568; padding: 8px 16px; border-radius: 6px; font-size: 13px; font-weight: 500; cursor: pointer; transition: 0.2s; }
.btn-outline:hover { background: #fdfbf7; border-color: #8c6b4a; color: #8c6b4a; }

.custom-date-row { display: flex; gap: 12px; align-items: center; background: #ffffff; padding: 12px 24px; border-radius: 12px; margin-bottom: 24px; }
.sunova-input { border: 1px solid #cbd5e0; border-radius: 6px; padding: 8px 12px; outline: none; }
.btn-filter { background: #8c6b4a; color: #fff; border: none; border-radius: 6px; padding: 8px 20px; cursor: pointer; font-weight: 600; }

.summary-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 20px; margin-bottom: 24px; }
.sunova-card { border-radius: 16px; background: #ffffff; border: 1px solid #e6dcd3; display: flex; flex-direction: column; }
.card-top { background: #8c6b4a; color: #ffffff; padding: 16px 20px; display: flex; justify-content: space-between; align-items: center; }
.card-name { font-weight: 600; font-size: 14px; }
.card-icon { background: #ffffff; color: #8c6b4a; width: 28px; height: 28px; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-weight: bold; }
.card-bottom { padding: 20px; }
.card-bottom h2 { margin: 0 0 8px 0; font-size: 28px; font-weight: 600; color: #5c4322; }

/* STYLE CHO AOV BÊN TRONG THẺ SỐ ĐƠN HÀNG */
.aov-text { margin-top: 12px; font-size: 13px; border-top: 1px dashed #e6dcd3; padding-top: 10px; color: #8c6b4a; }

.main-grid { display: grid; grid-template-columns: 2fr 1fr; gap: 20px; margin-bottom: 24px; }
.panel { background: #ffffff; border-radius: 16px; padding: 24px; border: 1px solid #e6dcd3; }

.chart-header-custom { display: flex; justify-content: space-between; align-items: flex-end; margin-bottom: 20px;}
.chart-tabs { display: flex; background: #fdfbf7; border: 1px solid #e6dcd3; border-radius: 8px; padding: 4px; gap: 4px;}
.tab { background: transparent; border: none; color: #8c6b4a; padding: 8px 14px; border-radius: 6px; font-weight: 600; cursor: pointer; }
.tab.active { background: #8c6b4a; color: #ffffff; }

.insight-list { display: flex; flex-direction: column; gap: 18px; }
.insight-item { display: flex; flex-direction: column; gap: 8px; }
.platform-info { display: flex; justify-content: space-between; align-items: center;}
.platform { font-size: 14px; color: #5c4322; font-weight: 600; }
.platform-percent { font-size: 14px; color: #8c6b4a; font-weight: bold;}
.progress-bar { width: 100%; height: 8px; background: #e6dcd3; border-radius: 4px; overflow: hidden;}
.progress-fill { height: 100%; background: #8c6b4a; transition: width 0.5s;}

.bottom-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 20px; }
.mini-chart-panel { display: flex; justify-content: space-between; align-items: center; padding: 20px 24px; }
.mini-info { display: flex; flex-direction: column; gap: 6px; }
.mini-label { font-size: 13px; color: #8c6b4a; font-weight: 500; }
.mini-info h3 { margin: 0; font-size: 20px; font-weight: bold; color: #5c4322; }

@media (max-width: 1024px) { .summary-grid { grid-template-columns: repeat(2, 1fr); } .main-grid { grid-template-columns: 1fr; } }
@media (max-width: 768px) { .summary-grid { grid-template-columns: 1fr; } .bottom-grid { grid-template-columns: 1fr; } }
</style>