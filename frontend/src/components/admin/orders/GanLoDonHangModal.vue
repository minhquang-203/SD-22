<script setup>
import { computed, ref, watch } from 'vue'
import { fetchGoiYGanLo, xacNhanGanLo } from '@/api/hoaDonApi'
import { productImageUrl } from '@/utils/productImage'

const props = defineProps({
  visible: { type: Boolean, default: false },
  orderId: { type: [Number, String], default: null },
  maHoaDon: { type: String, default: '' },
  tenKhachHang: { type: String, default: '' },
  ngayTao: { type: [String, Date], default: null },
})

const emit = defineEmits(['update:visible', 'success', 'error'])

const loading = ref(false)
const submitting = ref(false)
const loadError = ref('')
const maDon = ref('')
const tenKhach = ref('')
const ngayTaoDon = ref(null)
/** @type {import('vue').Ref<Array>} */
const lines = ref([])

const canSubmit = computed(() => {
  if (!lines.value.length || loading.value || submitting.value) return false
  return lines.value.every((line) => lineStatus(line).tone === 'ok')
})

function formatDate(value) {
  if (!value) return '—'
  return new Date(value).toLocaleDateString('vi-VN')
}

function formatDateTime(value) {
  if (!value) return '—'
  return new Date(value).toLocaleString('vi-VN')
}

function lotLabel(lot) {
  if (!lot) return '—'
  const hsd = lot.hanSuDung ? formatDate(lot.hanSuDung) : 'Không HSD'
  return `Lô ${lot.soLo} (${hsd})`
}

function findLot(line, idLoHang) {
  if (idLoHang == null) return null
  return (line.loCoTheChon || []).find((l) => l.id === idLoHang) || null
}

function lineTotal(line) {
  return (line.lotRows || []).reduce((sum, row) => sum + (Number(row.soLuong) || 0), 0)
}

function lineStatus(line) {
  const total = lineTotal(line)
  const need = Number(line.soLuong) || 0
  if (total === 0) return { text: 'Chưa gán', tone: 'missing' }
  if (total < need) return { text: `Thiếu ${need - total}`, tone: 'partial' }
  if (total > need) return { text: `Vượt ${total - need}`, tone: 'missing' }

  // Kiểm tra tồn từng lô (gộp nếu chọn trùng)
  const used = {}
  for (const row of line.lotRows || []) {
    if (row.idLoHang == null) continue
    used[row.idLoHang] = (used[row.idLoHang] || 0) + (Number(row.soLuong) || 0)
  }
  for (const [id, qty] of Object.entries(used)) {
    const lot = findLot(line, Number(id))
    const max = Number(lot?.soLuongCoTheChon) || 0
    if (qty > max) {
      return { text: `Lô ${(lot?.soLo) || id} vượt tồn`, tone: 'missing' }
    }
  }
  return { text: 'Đủ', tone: 'ok' }
}

function lotMax(line, row) {
  const lot = findLot(line, row.idLoHang)
  return Number(lot?.soLuongCoTheChon) || 0
}

function lotStockText(line, row) {
  if (row.idLoHang == null) return 'Tồn: —'
  return `Tồn: ${lotMax(line, row)}`
}

function closeModal() {
  if (submitting.value) return
  emit('update:visible', false)
}

function toggleLot(line) {
  line.expanded = !line.expanded
}

function onLotChange(line, row) {
  const max = lotMax(line, row)
  if ((Number(row.soLuong) || 0) > max) {
    row.soLuong = max
  }
}

function setRowQty(line, row, raw) {
  let n = Number(raw)
  if (!Number.isFinite(n) || n < 0) n = 0
  const max = lotMax(line, row)
  if (row.idLoHang != null && n > max) n = max
  row.soLuong = Math.floor(n)
}

function addLotRow(line) {
  line.lotRows.push({ idLoHang: null, soLuong: 0 })
  line.expanded = true
}

function removeLotRow(line, index) {
  if ((line.lotRows || []).length <= 1) {
    line.lotRows[0].soLuong = 0
    return
  }
  line.lotRows.splice(index, 1)
}

function buildInitialRows(dong) {
  const daGan = dong.loDaGan || []
  if (daGan.length) {
    return daGan.map((lo) => ({
      idLoHang: lo.idLoHang,
      soLuong: Number(lo.soLuong) || 0,
    }))
  }
  const first = (dong.loCoTheChon || [])[0]
  if (first) {
    return [{ idLoHang: first.id, soLuong: Number(dong.soLuong) || 0 }]
  }
  return [{ idLoHang: null, soLuong: 0 }]
}

async function loadData() {
  if (!props.orderId) return
  loading.value = true
  loadError.value = ''
  lines.value = []
  try {
    const res = await fetchGoiYGanLo(props.orderId)
    const data = res.data || {}
    maDon.value = data.maHoaDon || props.maHoaDon || ''
    tenKhach.value = data.tenKhachHang || props.tenKhachHang || 'Khách lẻ'
    ngayTaoDon.value = data.ngayTao || props.ngayTao || null
    lines.value = (data.dongHang || []).map((dong) => ({
      idHoaDonChiTiet: dong.idHoaDonChiTiet,
      tenSanPham: dong.tenSanPham,
      sku: dong.sku,
      bienThe: dong.bienThe,
      anhUrl: dong.anhUrl,
      soLuong: dong.soLuong,
      loCoTheChon: dong.loCoTheChon || [],
      lotRows: buildInitialRows(dong),
      expanded: true,
    }))
  } catch (err) {
    loadError.value = typeof err === 'string' ? err : 'Không tải được gợi ý phân bổ lô'
  } finally {
    loading.value = false
  }
}

async function submit() {
  if (!canSubmit.value || !props.orderId) return
  submitting.value = true
  try {
    const dongHang = lines.value.map((line) => {
      const merged = {}
      for (const row of line.lotRows || []) {
        if (row.idLoHang == null) continue
        const qty = Number(row.soLuong) || 0
        if (qty <= 0) continue
        merged[row.idLoHang] = (merged[row.idLoHang] || 0) + qty
      }
      return {
        idHoaDonChiTiet: line.idHoaDonChiTiet,
        phanBoLo: Object.entries(merged).map(([idLoHang, soLuong]) => ({
          idLoHang: Number(idLoHang),
          soLuong,
        })),
      }
    })
    await xacNhanGanLo(props.orderId, {
      dongHang,
      ghiChu: 'Admin xác nhận đơn và gán lô',
    })
    emit('success')
    emit('update:visible', false)
  } catch (err) {
    emit('error', typeof err === 'string' ? err : 'Không xác nhận / gán lô được')
  } finally {
    submitting.value = false
  }
}

watch(
  () => props.visible,
  (open) => {
    if (open) {
      document.body.style.overflow = 'hidden'
      loadData()
    } else {
      document.body.style.overflow = ''
      lines.value = []
      loadError.value = ''
    }
  },
)
</script>

<template>
  <Teleport to="body">
    <div v-if="visible" class="mpl-overlay" @click.self="closeModal">
      <div class="mpl-container" role="dialog" aria-modal="true" aria-label="Gán lô cho đơn hàng">
        <div class="mpl-header">
          <div>
            <h1>Đơn hàng #{{ maDon || maHoaDon || orderId }}</h1>
            <div class="mpl-meta">
              Khách: {{ tenKhach || 'Khách lẻ' }}
              <template v-if="ngayTaoDon"> · {{ formatDateTime(ngayTaoDon) }}</template>
              <template v-if="lines.length"> · {{ lines.length }} sản phẩm</template>
            </div>
          </div>
          <div class="mpl-status">Chờ gán lô</div>
        </div>

        <div class="mpl-body">
          <div v-if="loading" class="mpl-state">Đang tải phân bổ lô...</div>
          <div v-else-if="loadError" class="mpl-state mpl-state--error">{{ loadError }}</div>
          <div v-else-if="!lines.length" class="mpl-state">Đơn không có dòng hàng.</div>

          <div v-else class="mpl-table-wrap">
            <table class="mpl-table">
              <thead>
                <tr>
                  <th style="width: 48px">STT</th>
                  <th style="width: 64px">Ảnh</th>
                  <th style="width: 24%">Sản phẩm</th>
                  <th style="width: 9%">SL đặt</th>
                  <th style="width: 11%">Đã gán lô</th>
                  <th>Lô xuất</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="(line, idx) in lines" :key="line.idHoaDonChiTiet">
                  <td class="mpl-stt">{{ idx + 1 }}</td>
                  <td>
                    <div class="mpl-thumb">
                      <img :src="productImageUrl(line.anhUrl)" :alt="line.tenSanPham || 'SP'" />
                    </div>
                  </td>
                  <td>
                    <div class="mpl-product-name">{{ line.tenSanPham || 'Sản phẩm' }}</div>
                    <div class="mpl-product-code">
                      <template v-if="line.sku">{{ line.sku }}</template>
                      <template v-if="line.bienThe"> · {{ line.bienThe }}</template>
                    </div>
                  </td>
                  <td class="mpl-qty">{{ line.soLuong }}</td>
                  <td>
                    <span>{{ lineTotal(line) }}</span>
                    <div class="mpl-lot-status" :class="`mpl-lot-status--${lineStatus(line).tone}`">
                      {{ lineStatus(line).text }}
                    </div>
                  </td>
                  <td>
                    <button
                      type="button"
                      class="mpl-btn-toggle"
                      :class="{ active: line.expanded }"
                      :disabled="submitting"
                      @click="toggleLot(line)"
                    >
                      Chi tiết lô
                    </button>

                    <div v-show="line.expanded" class="mpl-lot-section">
                      <div
                        v-for="(row, rowIdx) in line.lotRows"
                        :key="`${line.idHoaDonChiTiet}-${rowIdx}`"
                        class="mpl-lot-row"
                      >
                        <select
                          class="mpl-lot-select"
                          :value="row.idLoHang ?? ''"
                          :disabled="submitting || !line.loCoTheChon.length"
                          @change="row.idLoHang = $event.target.value ? Number($event.target.value) : null; onLotChange(line, row)"
                        >
                          <option value="">-- Chọn lô --</option>
                          <option
                            v-for="lot in line.loCoTheChon"
                            :key="lot.id"
                            :value="lot.id"
                          >
                            {{ lotLabel(lot) }}
                          </option>
                        </select>
                        <span class="mpl-lot-stock">{{ lotStockText(line, row) }}</span>
                        <input
                          type="number"
                          class="mpl-lot-qty"
                          min="0"
                          :max="lotMax(line, row) || undefined"
                          :value="row.soLuong"
                          :disabled="submitting || row.idLoHang == null"
                          @input="setRowQty(line, row, $event.target.value)"
                        />
                        <button
                          type="button"
                          class="mpl-btn-icon"
                          :disabled="submitting"
                          title="Xóa dòng lô"
                          @click="removeLotRow(line, rowIdx)"
                        >
                          ×
                        </button>
                      </div>
                      <button
                        type="button"
                        class="mpl-btn-add"
                        :disabled="submitting || !line.loCoTheChon.length"
                        @click="addLotRow(line)"
                      >
                        + Thêm lô
                      </button>
                    </div>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>

        <div class="mpl-footer">
          <div class="mpl-footer-info">
            Hệ thống đã gợi ý lô theo FEFO. Bạn chỉ cần kiểm tra / chỉnh sửa trước khi xác nhận.
          </div>
          <div class="mpl-btn-group">
            <button type="button" class="mpl-btn mpl-btn-secondary" :disabled="submitting" @click="closeModal">
              Hủy
            </button>
            <button
              type="button"
              class="mpl-btn mpl-btn-primary"
              :disabled="!canSubmit"
              @click="submit"
            >
              {{ submitting ? 'Đang xác nhận...' : 'Xác nhận đơn & gán lô' }}
            </button>
          </div>
        </div>
      </div>
    </div>
  </Teleport>
</template>

<style scoped>
.mpl-overlay {
  position: fixed;
  inset: 0;
  z-index: 5000;
  background: rgba(0, 0, 0, 0.35);
  display: flex;
  align-items: flex-start;
  justify-content: center;
  padding: 4vh 16px 24px;
  overflow-y: auto;
  font-size: 13px;
  color: #212529;
}

.mpl-container {
  width: min(960px, 100%);
  background: #fff;
  border-radius: 4px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.12);
  overflow: hidden;
  display: flex;
  flex-direction: column;
  max-height: 92vh;
}

.mpl-header {
  padding: 14px 20px;
  border-bottom: 1px solid #dee2e6;
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
  background: #f8f9fa;
}

.mpl-header h1 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
}

.mpl-meta {
  font-size: 12px;
  color: #6c757d;
  margin-top: 3px;
}

.mpl-status {
  background: #fff3cd;
  color: #856404;
  font-size: 12px;
  padding: 3px 8px;
  border-radius: 3px;
  font-weight: 500;
  white-space: nowrap;
}

.mpl-body {
  overflow-y: auto;
  flex: 1;
  background: #fff;
}

.mpl-state {
  text-align: center;
  padding: 48px 16px;
  color: #6c757d;
}

.mpl-state--error {
  color: #dc3545;
}

.mpl-table {
  width: 100%;
  border-collapse: collapse;
}

.mpl-table th {
  text-align: left;
  padding: 8px 12px;
  font-size: 12px;
  font-weight: 600;
  color: #6c757d;
  background: #f8f9fa;
  border-bottom: 1px solid #dee2e6;
}

.mpl-table td {
  padding: 10px 12px;
  border-bottom: 1px solid #f1f1f1;
  vertical-align: top;
}

.mpl-table tr:last-child td {
  border-bottom: none;
}

.mpl-stt {
  color: #6c757d;
  font-weight: 600;
}

.mpl-thumb {
  width: 44px;
  height: 44px;
  border-radius: 4px;
  overflow: hidden;
  border: 1px solid #e9ecef;
  background: #f8f9fa;
}

.mpl-thumb img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.mpl-product-name {
  font-weight: 500;
  color: #212529;
}

.mpl-product-code {
  font-size: 12px;
  color: #6c757d;
  margin-top: 1px;
}

.mpl-qty {
  font-weight: 600;
}

.mpl-lot-status {
  font-size: 12px;
  margin-top: 4px;
}

.mpl-lot-status--ok {
  color: #198754;
}

.mpl-lot-status--missing {
  color: #dc3545;
}

.mpl-lot-status--partial {
  color: #fd7e14;
}

.mpl-btn-toggle {
  background: #e9ecef;
  border: 1px solid #ced4da;
  border-radius: 3px;
  padding: 3px 8px;
  font-size: 12px;
  cursor: pointer;
  color: #495057;
}

.mpl-btn-toggle:hover:not(:disabled) {
  background: #dee2e6;
}

.mpl-btn-toggle.active {
  background: #714b67;
  border-color: #714b67;
  color: #fff;
}

.mpl-lot-section {
  background: #f8f9fa;
  border-radius: 3px;
  padding: 8px 10px;
  margin-top: 6px;
}

.mpl-lot-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 6px;
}

.mpl-lot-row:last-of-type {
  margin-bottom: 0;
}

.mpl-lot-select {
  flex: 1;
  min-width: 160px;
  padding: 4px 8px;
  border: 1px solid #ced4da;
  border-radius: 3px;
  font-size: 13px;
  background: #fff;
}

.mpl-lot-stock {
  width: 70px;
  font-size: 12px;
  color: #198754;
  text-align: right;
  flex-shrink: 0;
}

.mpl-lot-qty {
  width: 60px;
  padding: 4px 6px;
  border: 1px solid #ced4da;
  border-radius: 3px;
  text-align: right;
  font-size: 13px;
}

.mpl-btn-icon {
  background: none;
  border: none;
  color: #dc3545;
  cursor: pointer;
  font-size: 15px;
  padding: 2px 4px;
  line-height: 1;
}

.mpl-btn-icon:hover:not(:disabled) {
  color: #a71d2a;
}

.mpl-btn-add {
  background: none;
  border: none;
  color: #0d6efd;
  font-size: 12px;
  cursor: pointer;
  padding: 2px 0;
  margin-top: 4px;
}

.mpl-btn-add:hover:not(:disabled) {
  text-decoration: underline;
}

.mpl-btn-add:disabled,
.mpl-btn-icon:disabled,
.mpl-btn-toggle:disabled {
  opacity: 0.55;
  cursor: not-allowed;
}

.mpl-footer {
  padding: 12px 20px;
  border-top: 1px solid #dee2e6;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  background: #f8f9fa;
  flex-wrap: wrap;
}

.mpl-footer-info {
  font-size: 12px;
  color: #6c757d;
  flex: 1;
  min-width: 200px;
}

.mpl-btn-group {
  display: flex;
  gap: 8px;
}

.mpl-btn {
  padding: 7px 16px;
  border-radius: 3px;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  border: 1px solid transparent;
}

.mpl-btn:disabled {
  opacity: 0.55;
  cursor: not-allowed;
}

.mpl-btn-secondary {
  background: #fff;
  border-color: #ced4da;
  color: #212529;
}

.mpl-btn-secondary:hover:not(:disabled) {
  background: #e9ecef;
}

.mpl-btn-primary {
  background: #714b67;
  color: #fff;
}

.mpl-btn-primary:hover:not(:disabled) {
  background: #5a3c52;
}

@media (max-width: 720px) {
  .mpl-lot-row {
    flex-wrap: wrap;
  }

  .mpl-lot-select {
    min-width: 100%;
  }
}
</style>
