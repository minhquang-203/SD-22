<template>
  <div class="admin-weather-manager">
    <div class="page-header">
      <h2>Quản trị UV & Thời tiết</h2>
    </div>

    <div class="admin-grid">
      <div class="admin-card">
        <h3>Trạng thái Realtime</h3>
        <div class="search-box">
          <input type="text" v-model="searchCity" @keyup.enter="loadData" placeholder="Nhập tên thành phố..." />
          <button @click="loadData" class="btn-search">Tìm</button>
        </div>

        <div v-if="loading" class="loading-state">Đang tải dữ liệu...</div>
        <div v-else-if="weatherData" class="weather-data">
          <div class="metric-row">
            <span>Nhiệt độ hiện tại ({{ searchCity }})</span>
            <strong>{{ weatherData.temp }}°C</strong>
          </div>
          
          <div class="metric-row">
            <span>Chỉ số UV (UVI)</span>
            <strong :class="{'uv-high': isHighAlert}">{{ weatherData.uvIndex }}</strong>
          </div>

          <div v-if="isHighAlert" class="alert-box">
            ⚠️ CẢNH BÁO: UV đã vượt ngưỡng an toàn!
          </div>

          <h4 class="mt-4">✨ Sản phẩm phù hợp với UV {{ weatherData.uvIndex }}:</h4>
          <div class="product-grid" v-if="suggestedProducts && suggestedProducts.length > 0">
            <div v-for="sp in suggestedProducts" :key="sp.ma_san_pham" class="product-card">
              <div class="sp-info">
                <p class="sp-title">{{ sp.ten }}</p>
                <span class="sp-spf">SPF: {{ sp.chi_so_spf }}</span>
              </div>
              <button class="btn-detail" @click="viewDetail(sp.ma_san_pham)">✏️</button>
            </div>
          </div>
          <div v-else class="empty-state">
            <p>🚫 Không tìm thấy sản phẩm phù hợp.</p>
          </div>
        </div>
      </div>

      <div class="admin-card">
        <h3>Cấu hình hệ thống</h3>
        <form @submit.prevent="saveConfiguration" class="config-form">
          <div class="form-group">
            <label>Ngưỡng UV Cảnh báo</label>
            <input type="number" step="0.1" v-model="configForm.uvHighThreshold" />
          </div>
          <button type="submit" class="btn-save">Lưu Cấu Hình</button>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'

const weatherData = ref(null)
const suggestedProducts = ref([])
const loading = ref(true)
const searchCity = ref('Hà Nội')
const isHighAlert = ref(false)
const configForm = ref({ uvHighThreshold: 6.0 })

const loadData = async () => {
  loading.value = true
  try {
    const res = await axios.get(`http://localhost:8080/api/v1/weather/current?city=${searchCity.value}`)
    weatherData.value = res.data.weather
    isHighAlert.value = res.data.isHighAlert
    
    const sRes = await axios.get(`http://localhost:8080/api/v1/weather/suggest?uvIndex=${weatherData.value.uvIndex}`)
    suggestedProducts.value = sRes.data
  } catch (err) { console.error(err) }
  finally { loading.value = false }
}

const saveConfiguration = () => alert('✅ Đã lưu cấu hình!')
const viewDetail = (id) => window.location.href = `/admin/products/detail/${id}`

onMounted(loadData)
</script>

<style scoped>
.admin-weather-manager { padding: 20px; color: #fff; background: #09090b; min-height: 100vh; }
.admin-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 24px; }
.admin-card { background: #18181b; padding: 24px; border-radius: 12px; border: 1px solid #3f3f46; }
.search-box { display: flex; gap: 10px; margin: 20px 0; }
.search-box input { flex: 1; padding: 10px; border-radius: 8px; border: 1px solid #3f3f46; background: #09090b; color: white; }
/* CÁC NÚT CSS CỦA BẠN */
.btn-search, .btn-save { background: #6366f1; color: white; padding: 10px 20px; border-radius: 8px; border: none; cursor: pointer; transition: 0.3s; font-weight: 500; }
.btn-search:hover, .btn-save:hover { background: #4f46e5; }
.btn-detail { background: transparent; color: #a1a1aa; border: none; cursor: pointer; font-size: 16px; transition: 0.2s; }
.btn-detail:hover { color: #f43f5e; }
/* CẢNH BÁO */
.uv-high { color: #ef4444 !important; font-size: 24px; font-weight: bold; }
.alert-box { background: #991b1b; color: white; padding: 10px; border-radius: 8px; margin: 10px 0; text-align: center; animation: pulse 1.5s infinite; font-weight: 600; }
@keyframes pulse { 0% { opacity: 1; } 50% { opacity: 0.6; } 100% { opacity: 1; } }
/* CÁC STYLE KHÁC */
.metric-row { display: flex; justify-content: space-between; padding: 12px 0; border-bottom: 1px solid #27272a; }
.product-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 12px; margin-top: 15px; }
.product-card { background: #27272a; padding: 12px; border-radius: 8px; display: flex; justify-content: space-between; align-items: center; }
.empty-state { background: #262626; border: 1px dashed #52525b; padding: 20px; border-radius: 8px; text-align: center; color: #a1a1aa; margin-top: 15px; font-style: italic; }
.config-form { display: flex; flex-direction: column; gap: 15px; margin-top: 20px; }
.form-group { display: flex; flex-direction: column; gap: 5px; }
</style>