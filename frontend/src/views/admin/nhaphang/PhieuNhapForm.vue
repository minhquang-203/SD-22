<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  createNhaCungCap,
  getNhaCungCapList,
  getPhieuNhapDetail,
  hoanThanhPhieuNhap,
  luuTamPhieuNhap,
  timBienTheNhapHang,
  updatePhieuNhap,
} from '@/api/nhapHangApi'
import { toast } from '@/composables/useToast'
import { confirm } from '@/composables/useConfirm'
import { formatApiError } from '@/utils/apiError'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const saving = ref(false)
const phieuId = ref(null)
const maPhieu = ref('(tự sinh khi lưu)')
const trangThai = ref('PHIEU_TAM')
const readonly = computed(() => trangThai.value !== 'PHIEU_TAM')

const lines = ref([])
const idNhaCungCap = ref(null)
const soHoaDonDauVao = ref('')
const giamGia = ref(0)
const ghiChu = ref('')
const ngayNhap = ref(todayLocal())
const nccOptions = ref([])

const showSkuModal = ref(false)
const skuQuery = ref('')
const skuLoading = ref(false)
const skuResults = ref([])
let skuSearchTimer = null

const showNccModal = ref(false)
const nccForm = ref({ ten: '', soDienThoai: '', email: '', diaChi: '', ghiChu: '' })
const nccSaving = ref(false)

const maxNgayNhap = todayLocal()

const tongTien = computed(() =>
  lines.value.reduce((sum, row) => sum + Number(row.soLuong || 0) * Number(row.donGia || 0), 0),
)
const canTraNcc = computed(() => Math.max(0, tongTien.value - Number(giamGia.value || 0)))

function todayLocal() {
  const d = new Date()
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${y}-${m}-${day}`
}

function formatMoney(v) {
  return Number(v || 0).toLocaleString('vi-VN') + ' ₫'
}

function lineThanhTien(row) {
  return Number(row.soLuong || 0) * Number(row.donGia || 0)
}

function buildPayload() {
  return {
    idNhaCungCap: idNhaCungCap.value || null,
    soHoaDonDauVao: soHoaDonDauVao.value || null,
    ngayNhap: ngayNhap.value || null,
    giamGia: Number(giamGia.value || 0),
    ghiChu: ghiChu.value || null,
    chiTiets: lines.value.map((row) => ({
      idChiTietSanPham: row.idChiTietSanPham,
      soLuong: Number(row.soLuong),
      donGia: Number(row.donGia || 0),
      hanSuDung: row.hanSuDung || null,
      soLo: row.soLo || null,
    })),
  }
}

async function loadNcc() {
  const res = await getNhaCungCapList()
  nccOptions.value = res.data || []
}

async function loadDetail(id) {
  loading.value = true
  try {
    const res = await getPhieuNhapDetail(id)
    const p = res.data
    phieuId.value = p.id
    maPhieu.value = p.maPhieuNhap
    trangThai.value = p.trangThai
    idNhaCungCap.value = p.idNhaCungCap
    soHoaDonDauVao.value = p.soHoaDonDauVao || ''
    giamGia.value = Number(p.giamGia || 0)
    ghiChu.value = p.ghiChu || ''
    ngayNhap.value = p.ngayTao ? String(p.ngayTao).slice(0, 10) : todayLocal()
    lines.value = (p.chiTiets || []).map((d) => ({
      idChiTietSanPham: d.idChiTietSanPham,
      sku: d.sku,
      tenSanPham: d.tenSanPham,
      tenMauSac: d.tenMauSac,
      dungTichMl: d.dungTichMl,
      soLuong: d.soLuong,
      donGia: Number(d.donGia || 0),
      giaBan: Number(d.giaBan || 0),
      hanSuDung: d.hanSuDung || '',
      soLo: d.soLo || '',
    }))
  } catch (e) {
    toast(formatApiError(e, 'Không tải được phiếu'), 'error')
    router.push('/admin/nhap-hang')
  } finally {
    loading.value = false
  }
}

function removeLine(index) {
  lines.value.splice(index, 1)
}

async function searchSku(keyword = skuQuery.value) {
  skuLoading.value = true
  try {
    const res = await timBienTheNhapHang(String(keyword || '').trim(), 0, 20)
    skuResults.value = res.data || []
  } catch (e) {
    toast(formatApiError(e, 'Không tìm được hàng'), 'error')
  } finally {
    skuLoading.value = false
  }
}

function scheduleSkuSearch() {
  if (skuSearchTimer) clearTimeout(skuSearchTimer)
  skuSearchTimer = setTimeout(() => searchSku(skuQuery.value), 350)
}

async function openSkuModal() {
  if (readonly.value) return
  showSkuModal.value = true
  await searchSku(skuQuery.value)
}

function addVariant(v) {
  if (lines.value.some((l) => l.idChiTietSanPham === v.idChiTietSanPham)) {
    toast('SKU đã có trên phiếu', 'warn')
    return
  }
  lines.value.push({
    idChiTietSanPham: v.idChiTietSanPham,
    sku: v.sku,
    tenSanPham: v.tenSanPham,
    tenMauSac: v.tenMauSac,
    dungTichMl: v.dungTichMl,
    soLuong: 1,
    donGia: 0,
    giaBan: Number(v.giaBan || 0),
    hanSuDung: '',
    soLo: '',
  })
  showSkuModal.value = false
  skuQuery.value = ''
  skuResults.value = []
}

async function saveNcc() {
  if (!nccForm.value.ten?.trim()) {
    toast('Nhập tên nhà cung cấp', 'warn')
    return
  }
  nccSaving.value = true
  try {
    const res = await createNhaCungCap({ ...nccForm.value, ten: nccForm.value.ten.trim() })
    await loadNcc()
    idNhaCungCap.value = res.data.id
    showNccModal.value = false
    nccForm.value = { ten: '', soDienThoai: '', email: '', diaChi: '', ghiChu: '' }
    toast('Đã thêm nhà cung cấp', 'success')
  } catch (e) {
    toast(formatApiError(e, 'Không tạo được NCC'), 'error')
  } finally {
    nccSaving.value = false
  }
}

function validateBeforeSave(requireHsd) {
  if (!lines.value.length) {
    toast('Thêm ít nhất 1 dòng hàng', 'warn')
    return false
  }
  if (!ngayNhap.value) {
    toast('Chọn ngày nhập', 'warn')
    return false
  }
  if (ngayNhap.value > todayLocal()) {
    toast('Ngày nhập không được lớn hơn ngày hiện tại', 'warn')
    return false
  }
  for (const row of lines.value) {
    if (!row.soLuong || Number(row.soLuong) <= 0) {
      toast(`SKU ${row.sku}: số lượng phải > 0`, 'warn')
      return false
    }
    if (requireHsd && !row.hanSuDung) {
      toast(`SKU ${row.sku}: cần nhập hạn sử dụng trước khi hoàn thành`, 'warn')
      return false
    }
  }
  return true
}

async function onLuuTam() {
  if (readonly.value) return
  if (!validateBeforeSave(false)) return
  saving.value = true
  try {
    const payload = buildPayload()
    if (phieuId.value) {
      const res = await updatePhieuNhap(phieuId.value, payload)
      applySaved(res.data)
      toast('Đã cập nhật phiếu tạm', 'success')
    } else {
      const res = await luuTamPhieuNhap(payload)
      applySaved(res.data)
      toast('Đã lưu phiếu tạm', 'success')
      router.replace(`/admin/nhap-hang/${res.data.id}`)
    }
  } catch (e) {
    toast(formatApiError(e, 'Không lưu được phiếu'), 'error')
  } finally {
    saving.value = false
  }
}

async function onHoanThanh() {
  if (readonly.value) return
  if (!validateBeforeSave(true)) return
  const ok = await confirm({
    title: 'Hoàn thành phiếu nhập',
    message: 'Hoàn thành phiếu sẽ sinh lô và cộng tồn. Không hoàn tác được. Tiếp tục?',
    confirmText: 'Hoàn thành',
  })
  if (!ok) return
  saving.value = true
  try {
    const payload = buildPayload()
    let id = phieuId.value
    if (id) {
      await updatePhieuNhap(id, payload)
    } else {
      const res = await luuTamPhieuNhap(payload)
      id = res.data.id
      phieuId.value = id
    }
    await hoanThanhPhieuNhap(id)
    toast('Đã nhập kho thành công', 'success')
    router.push('/admin/nhap-hang')
  } catch (e) {
    toast(formatApiError(e, 'Không hoàn thành được phiếu'), 'error')
  } finally {
    saving.value = false
  }
}

function applySaved(p) {
  phieuId.value = p.id
  maPhieu.value = p.maPhieuNhap
  trangThai.value = p.trangThai
}

watch(skuQuery, () => {
  if (!showSkuModal.value) return
  scheduleSkuSearch()
})

onMounted(async () => {
  await loadNcc()
  const id = route.params.id
  if (id && id !== 'tao') {
    await loadDetail(Number(id))
  }
})
</script>

<template>
  <div class="pn-form">
    <div class="pn-form__head">
      <div>
        <button type="button" class="pn-back" @click="router.push('/admin/nhap-hang')">← Danh sách</button>
        <h1 class="pn-form__title">
          {{ phieuId ? `Phiếu ${maPhieu}` : 'Tạo phiếu nhập' }}
        </h1>
        <p v-if="readonly" class="pn-form__hint">Phiếu đã khóa — chỉ xem.</p>
      </div>
    </div>

    <div v-if="loading" class="pn-empty">Đang tải…</div>

    <div v-else class="pn-form__grid">
      <!-- LEFT: lines -->
      <div class="admin-card pn-left">
        <div class="pn-left__toolbar">
          <div class="pn-search-row">
            <input
              v-model="skuQuery"
              class="admin-input"
              placeholder="Tìm hàng theo mã / tên…"
              :disabled="readonly"
              @keyup.enter="openSkuModal"
            />
            <button
              type="button"
              class="admin-btn admin-btn-primary"
              :disabled="readonly"
              @click="openSkuModal"
            >
              ＋
            </button>
          </div>
        </div>

        <div class="pn-table-scroll">
          <table class="pn-lines">
            <thead>
              <tr>
                <th>#</th>
                <th>SKU</th>
                <th>Tên hàng</th>
                <th>SL</th>
                <th>Đơn giá nhập</th>
                <th>Giá bán</th>
                <th>HSD</th>
                <th>Thành tiền</th>
                <th></th>
              </tr>
            </thead>
            <tbody>
              <tr v-if="!lines.length">
                <td colspan="9" class="pn-empty-cell">Chưa có dòng hàng — tìm và thêm SKU.</td>
              </tr>
              <tr v-for="(row, idx) in lines" :key="row.idChiTietSanPham">
                <td>{{ idx + 1 }}</td>
                <td class="pn-mono">{{ row.sku }}</td>
                <td>
                  <div class="pn-ten">{{ row.tenSanPham }}</div>
                  <div class="pn-meta">
                    <span v-if="row.tenMauSac">{{ row.tenMauSac }}</span>
                    <span v-if="row.dungTichMl"> · {{ row.dungTichMl }}ml</span>
                  </div>
                </td>
                <td>
                  <input
                    v-model.number="row.soLuong"
                    type="number"
                    min="1"
                    class="admin-input pn-input-sm"
                    :disabled="readonly"
                  />
                </td>
                <td>
                  <input
                    v-model.number="row.donGia"
                    type="number"
                    min="0"
                    class="admin-input pn-input-sm"
                    :disabled="readonly"
                    placeholder="Giá nhập"
                  />
                </td>
                <td class="pn-money pn-ref-price" :title="'Giá bán hiện tại — chỉ tham khảo'">
                  {{ formatMoney(row.giaBan) }}
                </td>
                <td>
                  <input
                    v-model="row.hanSuDung"
                    type="date"
                    class="admin-input pn-input-sm"
                    :disabled="readonly"
                  />
                </td>
                <td class="pn-money">{{ formatMoney(lineThanhTien(row)) }}</td>
                <td>
                  <button
                    v-if="!readonly"
                    type="button"
                    class="admin-btn admin-btn-danger"
                    @click="removeLine(idx)"
                  >
                    Xóa
                  </button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- RIGHT: meta -->
      <aside class="admin-card pn-right">
        <label class="pn-field">
          <span>Nhà cung cấp</span>
          <div class="pn-ncc-row">
            <select v-model="idNhaCungCap" class="admin-select" :disabled="readonly">
              <option :value="null">— Chọn NCC —</option>
              <option v-for="n in nccOptions" :key="n.id" :value="n.id">{{ n.ma }} — {{ n.ten }}</option>
            </select>
            <button
              type="button"
              class="admin-btn admin-btn-default"
              :disabled="readonly"
              @click="showNccModal = true"
            >
              ＋
            </button>
          </div>
        </label>

        <label class="pn-field">
          <span>Mã phiếu</span>
          <input class="admin-input" :value="maPhieu" readonly />
        </label>

        <label class="pn-field">
          <span>Ngày nhập</span>
          <input
            v-model="ngayNhap"
            type="date"
            class="admin-input"
            :max="maxNgayNhap"
            :disabled="readonly"
          />
        </label>

        <label class="pn-field">
          <span>Số HĐ đầu vào</span>
          <input v-model="soHoaDonDauVao" class="admin-input" :disabled="readonly" placeholder="Tuỳ chọn" />
        </label>

        <div class="pn-totals">
          <div class="pn-totals__row">
            <span>Tổng tiền hàng</span>
            <strong>{{ formatMoney(tongTien) }}</strong>
          </div>
          <label class="pn-field">
            <span>Giảm giá</span>
            <input
              v-model.number="giamGia"
              type="number"
              min="0"
              class="admin-input"
              :disabled="readonly"
            />
          </label>
          <div class="pn-totals__row pn-totals__row--emph">
            <span>Cần trả NCC</span>
            <strong>{{ formatMoney(canTraNcc) }}</strong>
          </div>
        </div>

        <label class="pn-field">
          <span>Ghi chú</span>
          <textarea v-model="ghiChu" class="admin-input" rows="3" :disabled="readonly" />
        </label>

        <div v-if="!readonly" class="pn-right__actions">
          <button type="button" class="admin-btn admin-btn-default" :disabled="saving" @click="onLuuTam">
            Lưu tạm
          </button>
          <button type="button" class="admin-btn admin-btn-primary" :disabled="saving" @click="onHoanThanh">
            Hoàn thành
          </button>
        </div>
      </aside>
    </div>

    <!-- SKU modal -->
    <div v-if="showSkuModal" class="pn-modal" @click.self="showSkuModal = false">
      <div class="pn-modal__panel">
        <div class="pn-modal__head">
          <h3>Tìm biến thể (SKU)</h3>
          <button type="button" class="admin-btn admin-btn-default" @click="showSkuModal = false">✕</button>
        </div>
        <input
          v-model="skuQuery"
          class="admin-input"
          placeholder="Nhập mã hoặc tên sản phẩm… (để trống = ~20 SKU gần nhất)"
          autofocus
          @input="scheduleSkuSearch"
        />
        <div class="pn-modal__list">
          <p v-if="skuLoading" class="pn-empty">Đang tìm…</p>
          <p v-else-if="!skuResults.length" class="pn-empty">Không có biến thể phù hợp.</p>
          <button
            v-for="v in skuResults"
            :key="v.idChiTietSanPham"
            type="button"
            class="pn-sku-item"
            @click="addVariant(v)"
          >
            <div>
              <strong class="pn-mono">{{ v.sku }}</strong>
              <div>{{ v.tenSanPham }}</div>
              <div class="pn-meta">
                <span v-if="v.tenMauSac">{{ v.tenMauSac }}</span>
                <span v-if="v.dungTichMl"> · {{ v.dungTichMl }}ml</span>
                <span> · Tồn {{ v.soLuongTon ?? 0 }}</span>
              </div>
            </div>
            <span class="pn-add">Thêm</span>
          </button>
        </div>
      </div>
    </div>

    <!-- NCC modal -->
    <div v-if="showNccModal" class="pn-modal" @click.self="showNccModal = false">
      <div class="pn-modal__panel pn-modal__panel--sm">
        <div class="pn-modal__head">
          <h3>Thêm nhà cung cấp</h3>
          <button type="button" class="admin-btn admin-btn-default" @click="showNccModal = false">✕</button>
        </div>
        <label class="pn-field">
          <span>Tên *</span>
          <input v-model="nccForm.ten" class="admin-input" />
        </label>
        <label class="pn-field">
          <span>SĐT</span>
          <input v-model="nccForm.soDienThoai" class="admin-input" />
        </label>
        <label class="pn-field">
          <span>Email</span>
          <input v-model="nccForm.email" class="admin-input" />
        </label>
        <label class="pn-field">
          <span>Địa chỉ</span>
          <input v-model="nccForm.diaChi" class="admin-input" />
        </label>
        <label class="pn-field">
          <span>Ghi chú</span>
          <input v-model="nccForm.ghiChu" class="admin-input" />
        </label>
        <button type="button" class="admin-btn admin-btn-primary" :disabled="nccSaving" @click="saveNcc">
          Lưu NCC
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.pn-form {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.pn-back {
  border: none;
  background: none;
  color: var(--admin-muted, #8a7b6a);
  cursor: pointer;
  padding: 0;
  margin-bottom: 0.35rem;
  font-size: 0.8125rem;
}

.pn-form__title {
  margin: 0;
  font-size: 1.35rem;
  font-weight: 700;
  color: var(--admin-text, #1a1814);
}

.pn-form__hint {
  margin: 0.35rem 0 0;
  color: var(--admin-muted, #8a7b6a);
  font-size: 0.875rem;
}

.pn-form__grid {
  display: grid;
  grid-template-columns: minmax(0, 1.7fr) minmax(280px, 0.9fr);
  gap: 1rem;
  align-items: start;
}

.pn-left,
.pn-right {
  padding: 1rem;
}

.pn-search-row {
  display: flex;
  gap: 0.5rem;
  margin-bottom: 0.85rem;
}

.pn-table-scroll {
  overflow-x: auto;
}

.pn-lines {
  width: 100%;
  border-collapse: collapse;
  font-size: 0.8125rem;
}

.pn-lines th,
.pn-lines td {
  padding: 0.55rem 0.45rem;
  border-bottom: 1px solid var(--admin-border, #e8dcc8);
  vertical-align: middle;
}

.pn-lines th {
  font-size: 0.7rem;
  text-transform: uppercase;
  letter-spacing: 0.04em;
  color: var(--admin-muted, #8a7b6a);
  background: rgba(201, 169, 110, 0.1);
}

.pn-input-sm {
  min-width: 88px;
  padding: 0.4rem 0.5rem;
}

.pn-mono {
  font-family: ui-monospace, monospace;
  font-weight: 600;
}

.pn-ten {
  font-weight: 600;
  color: var(--admin-text, #1a1814);
}

.pn-meta {
  font-size: 0.75rem;
  color: var(--admin-muted, #8a7b6a);
}

.pn-money {
  font-weight: 600;
  white-space: nowrap;
}

.pn-ref-price {
  color: var(--admin-muted, #8a7b6a);
  font-weight: 500;
  font-size: 0.8rem;
}

.pn-empty-cell {
  text-align: center;
  color: var(--admin-muted, #8a7b6a);
  padding: 1.5rem !important;
}

.pn-field {
  display: flex;
  flex-direction: column;
  gap: 0.35rem;
  margin-bottom: 0.85rem;
  font-size: 0.75rem;
  font-weight: 600;
  color: var(--admin-muted, #8a7b6a);
}

.pn-ncc-row {
  display: flex;
  gap: 0.4rem;
}

.pn-totals {
  padding: 0.75rem 0;
  border-top: 1px solid var(--admin-border, #e8dcc8);
  border-bottom: 1px solid var(--admin-border, #e8dcc8);
  margin-bottom: 0.85rem;
}

.pn-totals__row {
  display: flex;
  justify-content: space-between;
  gap: 0.75rem;
  margin-bottom: 0.65rem;
  font-size: 0.875rem;
  color: var(--admin-text, #1a1814);
}

.pn-totals__row--emph strong {
  color: var(--admin-primary, #9e7340);
  font-size: 1.05rem;
}

.pn-right__actions {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.pn-modal {
  position: fixed;
  inset: 0;
  background: rgba(36, 26, 18, 0.45);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 80;
  padding: 1rem;
}

.pn-modal__panel {
  width: min(560px, 100%);
  max-height: 85vh;
  overflow: auto;
  background: var(--admin-card, #fff);
  border-radius: 14px;
  padding: 1.15rem;
  border: 1px solid var(--admin-border, #e8dcc8);
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.pn-modal__panel--sm {
  width: min(420px, 100%);
}

.pn-modal__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.pn-modal__head h3 {
  margin: 0;
  font-size: 1.05rem;
}

.pn-modal__list {
  display: flex;
  flex-direction: column;
  gap: 0.4rem;
  max-height: 360px;
  overflow: auto;
}

.pn-sku-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 0.75rem;
  text-align: left;
  padding: 0.75rem 0.85rem;
  border-radius: 10px;
  border: 1px solid var(--admin-border, #e8dcc8);
  background: rgba(249, 245, 240, 0.6);
  cursor: pointer;
}

.pn-sku-item:hover {
  border-color: var(--admin-primary, #c9a96e);
}

.pn-add {
  font-size: 0.75rem;
  font-weight: 700;
  color: var(--admin-primary, #9e7340);
}

.pn-empty {
  text-align: center;
  color: var(--admin-muted, #8a7b6a);
  padding: 1rem;
}

@media (max-width: 960px) {
  .pn-form__grid {
    grid-template-columns: 1fr;
  }
}
</style>
