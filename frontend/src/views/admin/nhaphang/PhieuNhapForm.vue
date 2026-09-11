<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Icon } from '@iconify/vue'
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

const STATUS_LABEL = {
  PHIEU_TAM: 'Phiếu tạm',
  DA_NHAP: 'Đã nhập',
  DA_HUY: 'Đã hủy',
}

function statusTone(st) {
  if (st === 'DA_NHAP') return 'ok'
  if (st === 'DA_HUY') return 'muted'
  return 'draft'
}

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
      <div class="pn-form__title-block">
        <button type="button" class="pn-back" @click="router.push('/admin/nhap-hang')">
          <Icon icon="icon-park-outline:left" width="16" />
          Danh sách phiếu nhập
        </button>
        <div class="pn-form__title-row">
          <h1 class="pn-form__title">
            {{ phieuId ? maPhieu : 'Tạo phiếu nhập' }}
          </h1>
          <span
            v-if="phieuId"
            class="pn-badge"
            :class="`pn-badge--${statusTone(trangThai)}`"
          >
            {{ STATUS_LABEL[trangThai] || trangThai }}
          </span>
        </div>
        <p class="pn-form__hint">
          <template v-if="readonly">Phiếu đã khóa — chỉ xem, không chỉnh sửa.</template>
          <template v-else>Thêm hàng bên trái, điền thông tin NCC bên phải, rồi lưu tạm hoặc hoàn thành.</template>
        </p>
      </div>
    </div>

    <div v-if="loading" class="pn-loading">Đang tải phiếu…</div>

    <div v-else class="pn-form__grid">
      <!-- LEFT: lines -->
      <section class="pn-panel pn-left">
        <div class="pn-panel__head">
          <div>
            <h2 class="pn-panel__title">Dòng hàng</h2>
            <p class="pn-panel__sub">{{ lines.length }} sản phẩm trên phiếu</p>
          </div>
          <button
            type="button"
            class="soleil-btn-primary"
            :disabled="readonly"
            @click="openSkuModal"
          >
            <Icon icon="icon-park-outline:plus" width="15" />
            Thêm hàng
          </button>
        </div>

        <div v-if="!readonly" class="pn-search-row">
          <div class="pn-search">
            <Icon icon="icon-park-outline:search" class="pn-search__icon" />
            <input
              v-model="skuQuery"
              class="pn-search__input"
              placeholder="Tìm theo mã SKU hoặc tên sản phẩm…"
              @keyup.enter="openSkuModal"
            />
          </div>
          <button type="button" class="soleil-btn-outline" @click="openSkuModal">
            Tìm
          </button>
        </div>

        <div v-if="!lines.length" class="pn-empty-lines">
          <Icon icon="icon-park-outline:inbox" width="28" class="pn-empty-lines__icon" />
          <p>Chưa có dòng hàng</p>
          <span>Tìm SKU rồi thêm vào phiếu để bắt đầu nhập kho.</span>
        </div>

        <div v-else class="pn-lines">
          <article
            v-for="(row, idx) in lines"
            :key="row.idChiTietSanPham"
            class="pn-line"
          >
            <div class="pn-line__index">{{ idx + 1 }}</div>

            <div class="pn-line__product">
              <div class="pn-line__sku">{{ row.sku }}</div>
              <div class="pn-line__name">{{ row.tenSanPham }}</div>
              <div class="pn-line__meta">
                <span v-if="row.tenMauSac">{{ row.tenMauSac }}</span>
                <span v-if="row.dungTichMl">{{ row.dungTichMl }}ml</span>
                <span class="pn-line__ref">Giá bán {{ formatMoney(row.giaBan) }}</span>
              </div>
            </div>

            <div class="pn-line__fields">
              <label class="pn-line__field">
                <span>Số lượng</span>
                <input
                  v-model.number="row.soLuong"
                  type="number"
                  min="1"
                  class="pn-line__input"
                  :disabled="readonly"
                />
              </label>
              <label class="pn-line__field">
                <span>Đơn giá nhập</span>
                <input
                  v-model.number="row.donGia"
                  type="number"
                  min="0"
                  class="pn-line__input"
                  :disabled="readonly"
                  placeholder="0"
                />
              </label>
              <label class="pn-line__field">
                <span>Hạn sử dụng</span>
                <input
                  v-model="row.hanSuDung"
                  type="date"
                  class="pn-line__input"
                  :disabled="readonly"
                />
              </label>
              <div class="pn-line__field pn-line__field--total">
                <span>Thành tiền</span>
                <strong>{{ formatMoney(lineThanhTien(row)) }}</strong>
              </div>
            </div>

            <button
              v-if="!readonly"
              type="button"
              class="pn-line__remove"
              title="Xóa dòng"
              @click="removeLine(idx)"
            >
              <Icon icon="icon-park-outline:delete" width="16" />
            </button>
          </article>
        </div>
      </section>

      <!-- RIGHT: meta -->
      <aside class="pn-panel pn-right">
        <div class="pn-panel__head pn-panel__head--compact">
          <h2 class="pn-panel__title">Thông tin phiếu</h2>
        </div>

        <label class="pn-field">
          <span>Nhà cung cấp</span>
          <div class="pn-ncc-row">
            <select v-model="idNhaCungCap" class="pn-control" :disabled="readonly">
              <option :value="null">— Chọn NCC —</option>
              <option v-for="n in nccOptions" :key="n.id" :value="n.id">
                {{ n.ma }} — {{ n.ten }}
              </option>
            </select>
            <button
              type="button"
              class="soleil-btn-outline pn-icon-btn"
              :disabled="readonly"
              title="Thêm NCC"
              @click="showNccModal = true"
            >
              <Icon icon="icon-park-outline:plus" width="15" />
            </button>
          </div>
        </label>

        <div class="pn-field-grid">
          <label class="pn-field">
            <span>Mã phiếu</span>
            <input class="pn-control" :value="maPhieu" readonly />
          </label>
          <label class="pn-field">
            <span>Ngày nhập</span>
            <input
              v-model="ngayNhap"
              type="date"
              class="pn-control"
              :max="maxNgayNhap"
              :disabled="readonly"
            />
          </label>
        </div>

        <label class="pn-field">
          <span>Số HĐ đầu vào</span>
          <input
            v-model="soHoaDonDauVao"
            class="pn-control"
            :disabled="readonly"
            placeholder="Tuỳ chọn"
          />
        </label>

        <div class="pn-totals">
          <div class="pn-totals__row">
            <span>Tổng tiền hàng</span>
            <strong>{{ formatMoney(tongTien) }}</strong>
          </div>
          <label class="pn-field pn-field--inline">
            <span>Giảm giá</span>
            <input
              v-model.number="giamGia"
              type="number"
              min="0"
              class="pn-control"
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
          <textarea
            v-model="ghiChu"
            class="pn-control pn-control--area"
            rows="3"
            :disabled="readonly"
            placeholder="Ghi chú nội bộ…"
          />
        </label>

        <div v-if="!readonly" class="pn-right__actions">
          <button
            type="button"
            class="soleil-btn-outline"
            :disabled="saving"
            @click="onLuuTam"
          >
            <Icon icon="icon-park-outline:save-one" width="15" />
            Lưu tạm
          </button>
          <button
            type="button"
            class="soleil-btn-primary"
            :disabled="saving"
            @click="onHoanThanh"
          >
            <Icon icon="icon-park-outline:check-one" width="15" />
            Hoàn thành nhập kho
          </button>
        </div>
      </aside>
    </div>

    <!-- SKU modal -->
    <div v-if="showSkuModal" class="pn-modal" @click.self="showSkuModal = false">
      <div class="pn-modal__panel">
        <div class="pn-modal__head">
          <div>
            <h3>Thêm hàng vào phiếu</h3>
            <p>Chọn biến thể (SKU) để thêm dòng nhập</p>
          </div>
          <button type="button" class="soleil-btn-outline pn-icon-btn" @click="showSkuModal = false">
            <Icon icon="icon-park-outline:close" width="15" />
          </button>
        </div>
        <div class="pn-search">
          <Icon icon="icon-park-outline:search" class="pn-search__icon" />
          <input
            v-model="skuQuery"
            class="pn-search__input"
            placeholder="Nhập mã hoặc tên sản phẩm…"
            autofocus
            @input="scheduleSkuSearch"
          />
        </div>
        <div class="pn-modal__list">
          <p v-if="skuLoading" class="pn-modal__empty">Đang tìm…</p>
          <p v-else-if="!skuResults.length" class="pn-modal__empty">Không có biến thể phù hợp.</p>
          <button
            v-for="v in skuResults"
            :key="v.idChiTietSanPham"
            type="button"
            class="pn-sku-item"
            @click="addVariant(v)"
          >
            <div class="pn-sku-item__body">
              <strong class="pn-sku-item__sku">{{ v.sku }}</strong>
              <div class="pn-sku-item__name">{{ v.tenSanPham }}</div>
              <div class="pn-sku-item__meta">
                <span v-if="v.tenMauSac">{{ v.tenMauSac }}</span>
                <span v-if="v.dungTichMl">{{ v.dungTichMl }}ml</span>
                <span>Tồn {{ v.soLuongTon ?? 0 }}</span>
                <span>Giá bán {{ formatMoney(v.giaBan) }}</span>
              </div>
            </div>
            <span class="pn-sku-item__add">Thêm</span>
          </button>
        </div>
      </div>
    </div>

    <!-- NCC modal -->
    <div v-if="showNccModal" class="pn-modal" @click.self="showNccModal = false">
      <div class="pn-modal__panel pn-modal__panel--sm">
        <div class="pn-modal__head">
          <div>
            <h3>Thêm nhà cung cấp</h3>
            <p>Tạo nhanh NCC để gắn vào phiếu</p>
          </div>
          <button type="button" class="soleil-btn-outline pn-icon-btn" @click="showNccModal = false">
            <Icon icon="icon-park-outline:close" width="15" />
          </button>
        </div>
        <label class="pn-field">
          <span>Tên *</span>
          <input v-model="nccForm.ten" class="pn-control" />
        </label>
        <label class="pn-field">
          <span>SĐT</span>
          <input v-model="nccForm.soDienThoai" class="pn-control" />
        </label>
        <label class="pn-field">
          <span>Email</span>
          <input v-model="nccForm.email" class="pn-control" />
        </label>
        <label class="pn-field">
          <span>Địa chỉ</span>
          <input v-model="nccForm.diaChi" class="pn-control" />
        </label>
        <label class="pn-field">
          <span>Ghi chú</span>
          <input v-model="nccForm.ghiChu" class="pn-control" />
        </label>
        <button
          type="button"
          class="soleil-btn-primary"
          :disabled="nccSaving"
          @click="saveNcc"
        >
          Lưu NCC
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.pn-form {
  --pn-ink: #1a120c;
  --pn-muted: #5c4f42;
  --pn-line: #c9b8a4;
  --pn-line-strong: #a89278;
  --pn-surface: #ffffff;
  --pn-mist: #f3ebe1;
  --pn-accent: #0f4c52;
  --pn-ok: #14532d;
  --pn-ok-bg: #dcfce7;
  --pn-draft: #9a3412;
  --pn-draft-bg: #ffedd5;
  --pn-cancel: #3f3f46;
  --pn-cancel-bg: #e4e4e7;

  display: flex;
  flex-direction: column;
  gap: 1rem;
  color: var(--pn-ink);
}

.pn-back {
  display: inline-flex;
  align-items: center;
  gap: 0.35rem;
  border: none;
  background: none;
  color: var(--pn-ink);
  cursor: pointer;
  padding: 0;
  margin-bottom: 0.5rem;
  font-size: 0.8125rem;
  font-weight: 600;
}

.pn-back:hover {
  color: var(--pn-accent);
}

.pn-form__title-row {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  flex-wrap: wrap;
}

.pn-form__title {
  margin: 0;
  font-size: 1.4rem;
  font-weight: 800;
  letter-spacing: 0.01em;
  color: var(--pn-ink);
}

.pn-form__hint {
  margin: 0.4rem 0 0;
  color: var(--pn-muted);
  font-size: 0.875rem;
  line-height: 1.45;
}

.pn-badge {
  display: inline-flex;
  align-items: center;
  padding: 0.3rem 0.7rem;
  border-radius: 3px;
  border: 1px solid transparent;
  font-size: 11.5px;
  font-weight: 800;
  letter-spacing: 0.02em;
}

.pn-badge--draft {
  background: var(--pn-draft-bg);
  border-color: #fdba74;
  color: var(--pn-draft);
}

.pn-badge--ok {
  background: var(--pn-ok-bg);
  border-color: #86efac;
  color: var(--pn-ok);
}

.pn-badge--muted {
  background: var(--pn-cancel-bg);
  border-color: #a1a1aa;
  color: var(--pn-cancel);
}

.pn-loading {
  padding: 3rem 1rem;
  text-align: center;
  color: var(--pn-muted);
  background: var(--pn-surface);
  border: 1px solid var(--pn-line);
  border-radius: 12px;
}

.pn-form__grid {
  display: grid;
  grid-template-columns: minmax(0, 1.75fr) minmax(300px, 0.85fr);
  gap: 1rem;
  align-items: start;
}

.pn-panel {
  background: var(--pn-surface);
  border: 1px solid var(--pn-line-strong);
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 1px 2px rgba(26, 18, 12, 0.05);
}

.pn-panel__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 0.75rem;
  flex-wrap: wrap;
  padding: 1rem 1.15rem;
  border-bottom: 1px solid var(--pn-line);
  background: #efe4d4;
}

.pn-panel__head--compact {
  padding: 0.9rem 1.15rem;
}

.pn-panel__title {
  margin: 0;
  font-size: 0.95rem;
  font-weight: 800;
  color: var(--pn-ink);
}

.pn-panel__sub {
  margin: 0.2rem 0 0;
  font-size: 12px;
  font-weight: 600;
  color: var(--pn-muted);
}

.pn-left {
  padding-bottom: 1rem;
}

.pn-search-row {
  display: flex;
  gap: 0.5rem;
  padding: 1rem 1.15rem 0;
}

.pn-search {
  position: relative;
  flex: 1;
  min-width: 0;
}

.pn-search__icon {
  position: absolute;
  left: 12px;
  top: 50%;
  transform: translateY(-50%);
  color: var(--pn-muted);
  font-size: 16px;
  pointer-events: none;
}

.pn-search__input {
  width: 100%;
  border: 1px solid var(--pn-line-strong);
  border-radius: 8px;
  padding: 0.65rem 0.85rem 0.65rem 2.35rem;
  font-size: 0.875rem;
  font-weight: 500;
  background: #fff;
  color: var(--pn-ink);
  outline: none;
}

.pn-search__input:focus {
  border-color: #8f7349;
  background: #fff;
  box-shadow: 0 0 0 2px rgba(143, 115, 73, 0.18);
}

.pn-empty-lines {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.35rem;
  margin: 1.25rem 1.15rem;
  padding: 2.25rem 1rem;
  border: 1px dashed var(--pn-line-strong);
  border-radius: 10px;
  text-align: center;
  color: var(--pn-muted);
  background: #faf6f0;
}

.pn-empty-lines__icon {
  opacity: 0.45;
  margin-bottom: 0.25rem;
}

.pn-empty-lines p {
  margin: 0;
  font-weight: 700;
  color: var(--pn-ink);
}

.pn-empty-lines span {
  font-size: 0.8125rem;
}

.pn-lines {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
  padding: 1rem 1.15rem 0;
}

.pn-line {
  display: grid;
  grid-template-columns: 36px minmax(0, 1.2fr) minmax(0, 1.6fr) auto;
  gap: 0.85rem;
  align-items: start;
  padding: 1rem;
  border: 1px solid var(--pn-line-strong);
  border-radius: 10px;
  background: #fff;
}

.pn-line:hover {
  border-color: #8f7349;
  background: #fffdf9;
}

.pn-line__index {
  width: 28px;
  height: 28px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 999px;
  background: #efe4d4;
  border: 1px solid var(--pn-line-strong);
  font-size: 12px;
  font-weight: 800;
  color: var(--pn-ink);
  margin-top: 0.15rem;
}

.pn-line__sku {
  font-family: ui-monospace, 'Cascadia Mono', monospace;
  font-size: 12px;
  font-weight: 700;
  color: var(--pn-accent);
}

.pn-line__name {
  margin-top: 0.2rem;
  font-size: 0.9375rem;
  font-weight: 700;
  line-height: 1.35;
  color: var(--pn-ink);
}

.pn-line__meta {
  display: flex;
  flex-wrap: wrap;
  gap: 0.35rem 0.65rem;
  margin-top: 0.35rem;
  font-size: 12px;
  font-weight: 600;
  color: var(--pn-muted);
}

.pn-line__ref {
  color: #6b542f;
}

.pn-line__fields {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 0.65rem 0.75rem;
}

.pn-line__field {
  display: flex;
  flex-direction: column;
  gap: 0.3rem;
  font-size: 11px;
  font-weight: 800;
  letter-spacing: 0.04em;
  text-transform: uppercase;
  color: #4a3f34;
}

.pn-line__field--total strong {
  font-family: ui-monospace, 'Cascadia Mono', monospace;
  font-size: 0.95rem;
  font-weight: 700;
  color: var(--pn-ink);
  text-transform: none;
  letter-spacing: 0;
  padding-top: 0.45rem;
}

.pn-line__input {
  width: 100%;
  border: 1px solid var(--pn-line-strong);
  border-radius: 8px;
  padding: 0.55rem 0.7rem;
  font-size: 0.875rem;
  font-weight: 600;
  color: var(--pn-ink);
  background: #fff;
  outline: none;
}

.pn-line__input:focus {
  border-color: #8f7349;
  background: #fff;
  box-shadow: 0 0 0 2px rgba(143, 115, 73, 0.18);
}

.pn-line__input:disabled {
  opacity: 0.75;
  cursor: not-allowed;
}

.pn-line__remove {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 34px;
  height: 34px;
  border: 1px solid #f87171;
  border-radius: 8px;
  background: #fff;
  color: #991b1b;
  cursor: pointer;
  margin-top: 0.1rem;
}

.pn-line__remove:hover {
  background: #fee2e2;
  border-color: #ef4444;
}

.pn-right {
  padding: 0 1.15rem 1.15rem;
  position: sticky;
  top: 0.75rem;
}

.pn-field {
  display: flex;
  flex-direction: column;
  gap: 0.4rem;
  margin-top: 0.95rem;
  font-size: 11px;
  font-weight: 800;
  letter-spacing: 0.04em;
  text-transform: uppercase;
  color: #4a3f34;
}

.pn-field-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 0.75rem;
}

.pn-ncc-row {
  display: flex;
  gap: 0.4rem;
}

.pn-control {
  width: 100%;
  border: 1px solid var(--pn-line-strong);
  border-radius: 8px;
  padding: 0.6rem 0.75rem;
  font-size: 0.875rem;
  font-weight: 600;
  text-transform: none;
  letter-spacing: 0;
  color: var(--pn-ink);
  background: #fff;
  outline: none;
}

.pn-control:focus {
  border-color: #8f7349;
  background: #fff;
  box-shadow: 0 0 0 2px rgba(143, 115, 73, 0.18);
}

.pn-control:disabled,
.pn-control[readonly] {
  opacity: 0.85;
}

.pn-control--area {
  resize: vertical;
  min-height: 84px;
  font-family: inherit;
}

.pn-icon-btn {
  padding: 0.55rem 0.7rem !important;
  flex-shrink: 0;
}

.pn-totals {
  margin-top: 1rem;
  padding: 0.95rem;
  border: 1px solid var(--pn-line-strong);
  border-radius: 10px;
  background: #efe4d4;
}

.pn-totals__row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 0.75rem;
  font-size: 0.875rem;
  font-weight: 500;
  text-transform: none;
  letter-spacing: 0;
  color: var(--pn-ink);
}

.pn-totals__row strong {
  font-family: ui-monospace, 'Cascadia Mono', monospace;
}

.pn-field--inline {
  margin-top: 0.75rem;
  margin-bottom: 0.75rem;
}

.pn-totals__row--emph {
  padding-top: 0.75rem;
  border-top: 1px solid var(--pn-line);
}

.pn-totals__row--emph strong {
  color: #6b4520;
  font-size: 1.15rem;
  font-weight: 800;
}

.pn-right__actions {
  display: flex;
  flex-direction: column;
  gap: 0.55rem;
  margin-top: 1.1rem;
}

.pn-right__actions .soleil-btn-primary,
.pn-right__actions .soleil-btn-outline {
  width: 100%;
  justify-content: center;
}

.pn-modal {
  position: fixed;
  inset: 0;
  background: rgba(15, 26, 28, 0.45);
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
  background: var(--pn-surface);
  border-radius: 14px;
  padding: 1.15rem;
  border: 1px solid var(--pn-line);
  display: flex;
  flex-direction: column;
  gap: 0.85rem;
  box-shadow: 0 16px 40px rgba(15, 26, 28, 0.18);
}

.pn-modal__panel--sm {
  width: min(420px, 100%);
}

.pn-modal__head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 0.75rem;
}

.pn-modal__head h3 {
  margin: 0;
  font-size: 1.05rem;
  font-weight: 700;
}

.pn-modal__head p {
  margin: 0.25rem 0 0;
  font-size: 0.8125rem;
  color: var(--pn-muted);
}

.pn-modal__list {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  max-height: 380px;
  overflow: auto;
}

.pn-modal__empty {
  text-align: center;
  color: var(--pn-muted);
  padding: 1.5rem 0.5rem;
  margin: 0;
}

.pn-sku-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 0.75rem;
  text-align: left;
  padding: 0.85rem 0.95rem;
  border-radius: 10px;
  border: 1px solid var(--pn-line-strong);
  background: #fff;
  cursor: pointer;
  transition: border-color 0.15s, background 0.15s;
}

.pn-sku-item:hover {
  border-color: #8f7349;
  background: #fff7ed;
}

.pn-sku-item__sku {
  font-family: ui-monospace, 'Cascadia Mono', monospace;
  font-size: 12px;
  color: var(--pn-accent);
}

.pn-sku-item__name {
  margin-top: 0.2rem;
  font-weight: 700;
  color: var(--pn-ink);
}

.pn-sku-item__meta {
  display: flex;
  flex-wrap: wrap;
  gap: 0.35rem 0.65rem;
  margin-top: 0.3rem;
  font-size: 12px;
  font-weight: 600;
  color: var(--pn-muted);
}

.pn-sku-item__add {
  flex-shrink: 0;
  font-size: 0.75rem;
  font-weight: 800;
  color: #6b4520;
}

@media (max-width: 1100px) {
  .pn-line {
    grid-template-columns: 36px minmax(0, 1fr) auto;
  }

  .pn-line__fields {
    grid-column: 1 / -1;
    grid-template-columns: repeat(4, minmax(0, 1fr));
  }

  .pn-line__remove {
    grid-column: 3;
    grid-row: 1;
  }
}

@media (max-width: 960px) {
  .pn-form__grid {
    grid-template-columns: 1fr;
  }

  .pn-right {
    position: static;
  }
}

@media (max-width: 640px) {
  .pn-line {
    grid-template-columns: 28px minmax(0, 1fr) auto;
  }

  .pn-line__fields {
    grid-template-columns: 1fr 1fr;
  }

  .pn-field-grid {
    grid-template-columns: 1fr;
  }
}
</style>
