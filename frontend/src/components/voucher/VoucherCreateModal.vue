<template>
  <div
    v-if="modelValue"
    class="admin-modal-backdrop voucher-modal"
    @click.self="close"
  >
    <div class="voucher-modal-box">
      <div class="voucher-modal-head">
        <div>
          <div class="voucher-modal-title">
            {{ isEdit ? "Cập nhật phiếu giảm giá" : "Tạo phiếu mới" }}
          </div>
          <div class="voucher-modal-sub">Cấu hình chương trình ưu đãi</div>
        </div>

        <button type="button" class="voucher-modal-close" @click="close">
          <Icon icon="mdi:close" />
        </button>
      </div>

      <div class="voucher-modal-body">
        <div class="mb-3">
          <label class="voucher-label">Loại giảm giá</label>
          <div class="flex gap-2">
            <button
              type="button"
              class="type-btn"
              :class="{ active: form.loai === 'PHAN_TRAM' }"
              @click="setType('PHAN_TRAM')"
            >
              % Phần trăm
            </button>
            <button
              type="button"
              class="type-btn"
              :class="{ active: form.loai === 'TIEN_MAT' }"
              @click="setType('TIEN_MAT')"
            >
              ₫ Số tiền
            </button>
            <button
              type="button"
              class="type-btn"
              :class="{ active: form.loai === 'FREE_SHIP' }"
              @click="setType('FREE_SHIP')"
            >
              Miễn ship
            </button>
          </div>
        </div>

        <div class="mb-3">
          <label class="voucher-label">Phạm vi áp dụng</label>
          <div class="flex gap-2">
            <button
              type="button"
              class="type-btn"
              :class="{ active: form.phamVi === 'CONG_KHAI' }"
              @click="form.phamVi = 'CONG_KHAI'"
            >
              Công khai
            </button>
            <button
              type="button"
              class="type-btn"
              :class="{ active: form.phamVi === 'CA_NHAN' }"
              @click="form.phamVi = 'CA_NHAN'"
            >
              Cá nhân (gán riêng)
            </button>
          </div>
          <p class="voucher-hint">
            {{
              form.phamVi === "CA_NHAN"
                ? "Chỉ khách được gán mới dùng được. Sau khi lưu, bấm nút gán trên bảng để chuyển sang Quản lý khách hàng."
                : "Mọi khách hàng đều thấy và dùng được."
            }}
          </p>
        </div>

        <div class="grid grid-cols-12 gap-3">
          <div :class="showGiaTri ? 'col-span-7' : 'col-span-12'">
            <label class="voucher-label">Mã phiếu</label>
            <input
              :value="maLoading ? 'Đang sinh mã...' : form.ma"
              disabled
              class="voucher-input mono"
            />
            <p class="voucher-hint">Định dạng SNO-XXXXXX</p>
          </div>

          <div v-if="showGiaTri" class="col-span-5">
            <label class="voucher-label">{{ giaTriLabel }} *</label>
            <input
              v-model.number="form.giaTri"
              type="number"
              :min="1"
              :max="form.loai === 'PHAN_TRAM' ? 100 : undefined"
              :class="inputClass('giaTri')"
              class="voucher-input"
              :placeholder="form.loai === 'PHAN_TRAM' ? 'VD: 20' : 'VD: 50000'"
              @input="clearError('giaTri')"
            />
            <p v-if="errors.giaTri" class="voucher-field-error">{{ errors.giaTri }}</p>
          </div>

          <div class="col-span-12">
            <label class="voucher-label">Tên chương trình *</label>
            <input
              v-model="form.ten"
              :class="inputClass('ten')"
              class="voucher-input"
              placeholder="VD: Ưu đãi hè"
              @input="clearError('ten')"
            />
            <p v-if="errors.ten" class="voucher-field-error">{{ errors.ten }}</p>
          </div>

          <div class="col-span-6">
            <label class="voucher-label">Bắt đầu *</label>
            <input
              v-model="form.ngayBatDau"
              type="date"
              :min="minDate"
              :class="inputClass('ngayBatDau')"
              class="voucher-input"
              @change="clearError('ngayBatDau')"
            />
            <p v-if="errors.ngayBatDau" class="voucher-field-error">{{ errors.ngayBatDau }}</p>
          </div>

          <div class="col-span-6">
            <label class="voucher-label">Kết thúc *</label>
            <input
              v-model="form.ngayKetThuc"
              type="date"
              :min="form.ngayBatDau || minDate"
              :class="inputClass('ngayKetThuc')"
              class="voucher-input"
              @change="clearError('ngayKetThuc')"
            />
            <p v-if="errors.ngayKetThuc" class="voucher-field-error">{{ errors.ngayKetThuc }}</p>
          </div>

          <div class="col-span-6">
            <label class="voucher-label">Số lượng *</label>
            <input
              v-model.number="form.soLuong"
              type="number"
              min="1"
              step="1"
              :class="inputClass('soLuong')"
              class="voucher-input"
              placeholder="VD: 100"
              @input="clearError('soLuong')"
            />
            <p v-if="errors.soLuong" class="voucher-field-error">{{ errors.soLuong }}</p>
          </div>

          <div class="col-span-6">
            <label class="voucher-label">Đơn tối thiểu</label>
            <input
              v-model.number="form.giaTriDonToiThieu"
              type="number"
              min="0"
              :class="inputClass('giaTriDonToiThieu')"
              class="voucher-input"
              placeholder="0 = không yêu cầu"
              @input="clearError('giaTriDonToiThieu')"
            />
            <p v-if="errors.giaTriDonToiThieu" class="voucher-field-error">
              {{ errors.giaTriDonToiThieu }}
            </p>
          </div>

          <div v-if="showGiamToiDa" class="col-span-6">
            <label class="voucher-label">{{ giamToiDaLabel }}</label>
            <input
              v-model.number="form.giamToiDa"
              type="number"
              min="1"
              :class="inputClass('giamToiDa')"
              class="voucher-input"
              :placeholder="
                form.loai === 'FREE_SHIP'
                  ? 'Miễn toàn bộ phí ship nếu để trống'
                  : 'Không giới hạn nếu để trống'
              "
              @input="clearError('giamToiDa')"
            />
            <p v-if="errors.giamToiDa" class="voucher-field-error">{{ errors.giamToiDa }}</p>
          </div>
        </div>
      </div>

      <div class="voucher-modal-foot">
        <button type="button" class="btn-outline-sol" @click="close">Huỷ</button>
        <button type="button" class="btn-primary-sol" @click="submit">
          {{ isEdit ? "Cập nhật" : "Tạo mới" }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, watch, computed, ref } from "vue";
import { Icon } from "@iconify/vue";
import { toast } from "@/composables/useToast";
import { fetchNextVoucherMa } from "@/api/voucherApi";

const props = defineProps({
  modelValue: Boolean,
  voucher: Object,
});

const emit = defineEmits(["update:modelValue", "create", "update"]);

const maLoading = ref(false);
const isEdit = computed(() => !!props.voucher);
const showGiamToiDa = computed(() => form.loai !== "TIEN_MAT");
const showGiaTri = computed(() => form.loai !== "FREE_SHIP");

const giaTriLabel = computed(() => {
  if (form.loai === "PHAN_TRAM") return "Phần trăm (%)";
  if (form.loai === "TIEN_MAT") return "Số tiền giảm";
  return "Giá trị";
});

const giamToiDaLabel = computed(() =>
  form.loai === "FREE_SHIP" ? "Miễn ship tối đa" : "Giảm tối đa",
);

const form = reactive({
  ma: "",
  ten: "",
  loai: "PHAN_TRAM",
  phamVi: "CONG_KHAI",
  giaTri: null,
  giaTriDonToiThieu: null,
  giamToiDa: null,
  soLuong: null,
  ngayBatDau: null,
  ngayKetThuc: null,
});

const errors = reactive({
  ma: "",
  ten: "",
  giaTri: "",
  ngayBatDau: "",
  ngayKetThuc: "",
  soLuong: "",
  giaTriDonToiThieu: "",
  giamToiDa: "",
});

const INPUT_BASE = "voucher-input";
const INPUT_INVALID = "is-invalid";
const INPUT_NORMAL = "";

function inputClass(field) {
  return [INPUT_BASE, errors[field] ? INPUT_INVALID : INPUT_NORMAL];
}

function resetErrors() {
  Object.keys(errors).forEach((key) => {
    errors[key] = "";
  });
}

function clearError(field) {
  errors[field] = "";
}

function setType(type) {
  form.loai = type;

  if (type === "FREE_SHIP") {
    // FREE_SHIP không dùng "giá trị giảm", nhưng vẫn có "miễn ship tối đa" (giamToiDa).
    form.giaTri = null;
  } else if (type === "TIEN_MAT") {
    form.giamToiDa = null;
  }

  clearError("giaTri");
  clearError("giamToiDa");
}

function close() {
  emit("update:modelValue", false);
};

const todayISO = () => {
  const now = new Date();
  const y = now.getFullYear();
  const m = String(now.getMonth() + 1).padStart(2, "0");
  const d = String(now.getDate()).padStart(2, "0");
  return `${y}-${m}-${d}`;
};

const minDate = todayISO();

const formatStartDate = (d) => {
  if (!d) return null;
  return `${d}T00:00:00`;
};

const formatEndDate = (d) => {
  if (!d) return null;
  return `${d}T23:59:59`;
};

function isEmptyNumber(value) {
  return value === null || value === undefined || value === "";
}

function validateForm() {
  resetErrors();
  const next = {};
  const today = todayISO();

  const ten = form.ten?.trim();
  if (!ten) {
    next.ten = "Tên chương trình không được để trống";
  } else if (ten.length < 2) {
    next.ten = "Tên chương trình quá ngắn";
  } else if (ten.length > 200) {
    next.ten = "Tên chương trình tối đa 200 ký tự";
  }

  if (form.loai === "PHAN_TRAM") {
    const value = Number(form.giaTri);
    if (isEmptyNumber(form.giaTri)) {
      next.giaTri = "Nhập phần trăm giảm";
    } else if (!Number.isFinite(value) || value <= 0 || value > 100) {
      next.giaTri = "Phần trăm phải từ 1 đến 100";
    }
  } else if (form.loai === "TIEN_MAT") {
    const value = Number(form.giaTri);
    if (isEmptyNumber(form.giaTri)) {
      next.giaTri = "Nhập số tiền giảm";
    } else if (!Number.isFinite(value) || value <= 0) {
      next.giaTri = "Số tiền giảm phải lớn hơn 0";
    } else if (!Number.isInteger(value)) {
      next.giaTri = "Số tiền phải là số nguyên";
    }
  }

  if (!form.ngayBatDau) {
    next.ngayBatDau = "Chọn ngày bắt đầu";
  } else if (!isEdit.value && form.ngayBatDau < today) {
    next.ngayBatDau = "Ngày bắt đầu không được nhỏ hơn ngày hiện tại";
  }

  if (!form.ngayKetThuc) {
    next.ngayKetThuc = "Chọn ngày kết thúc";
  } else if (form.ngayKetThuc < today) {
    next.ngayKetThuc = "Ngày kết thúc không được nhỏ hơn ngày hiện tại";
  } else if (form.ngayBatDau && form.ngayKetThuc < form.ngayBatDau) {
    next.ngayKetThuc = "Ngày kết thúc phải sau hoặc bằng ngày bắt đầu";
  }

  const soLuong = Number(form.soLuong);
  if (isEmptyNumber(form.soLuong)) {
    next.soLuong = "Nhập số lượng phiếu";
  } else if (!Number.isFinite(soLuong) || !Number.isInteger(soLuong) || soLuong < 1) {
    next.soLuong = "Số lượng phải là số nguyên từ 1 trở lên";
  }

  if (!isEmptyNumber(form.giaTriDonToiThieu)) {
    const minOrder = Number(form.giaTriDonToiThieu);
    if (!Number.isFinite(minOrder) || minOrder < 0) {
      next.giaTriDonToiThieu = "Đơn tối thiểu không hợp lệ";
    } else if (!Number.isInteger(minOrder)) {
      next.giaTriDonToiThieu = "Đơn tối thiểu phải là số nguyên";
    }
  }

  if (showGiamToiDa.value && !isEmptyNumber(form.giamToiDa)) {
    const cap = Number(form.giamToiDa);
    if (!Number.isFinite(cap) || cap <= 0) {
      next.giamToiDa = "Giảm tối đa phải lớn hơn 0";
    } else if (!Number.isInteger(cap)) {
      next.giamToiDa = "Giảm tối đa phải là số nguyên";
    }
  }

  Object.assign(errors, next);

  if (Object.keys(next).length > 0) {
    toast(Object.values(next)[0], "warn");
    return false;
  }

  return true;
}

function buildPayload() {
  return {
    ...form,
    ma: form.ma?.trim()?.toUpperCase() || undefined,
    ten: form.ten.trim(),
    giaTri: form.loai === "FREE_SHIP" ? 1 : Number(form.giaTri),
    giaTriDonToiThieu: isEmptyNumber(form.giaTriDonToiThieu)
      ? null
      : Number(form.giaTriDonToiThieu),
    giamToiDa:
      (form.loai === "PHAN_TRAM" || form.loai === "FREE_SHIP") &&
      !isEmptyNumber(form.giamToiDa)
        ? Number(form.giamToiDa)
        : null,
    soLuong: Number(form.soLuong),
    ngayBatDau: formatStartDate(form.ngayBatDau),
    ngayKetThuc: formatEndDate(form.ngayKetThuc),
  };
}

const submit = () => {
  if (!validateForm()) return;
  if (!isEdit.value && (!form.ma || maLoading.value)) {
    toast("Đang sinh mã phiếu, vui lòng đợi giây lát", "warn");
    return;
  }

  const payload = buildPayload();

  if (isEdit.value) emit("update", payload);
  else emit("create", payload);
};

watch(
  () => props.modelValue,
  async (open) => {
    if (!open) return;

    resetErrors();

    if (props.voucher) {
      Object.assign(form, {
        ...props.voucher,
        phamVi: props.voucher.phamVi || "CONG_KHAI",
        ngayBatDau: props.voucher.ngayBatDau?.slice(0, 10),
        ngayKetThuc: props.voucher.ngayKetThuc?.slice(0, 10),
      });
      return;
    }

    Object.assign(form, {
      ma: "",
      ten: "",
      loai: "PHAN_TRAM",
      phamVi: "CONG_KHAI",
      giaTri: null,
      giaTriDonToiThieu: null,
      giamToiDa: null,
      soLuong: null,
      ngayBatDau: null,
      ngayKetThuc: null,
    });

    maLoading.value = true;
    try {
      const res = await fetchNextVoucherMa();
      form.ma = res.data?.ma || "";
      if (!form.ma) {
        toast("Không sinh được mã phiếu", "warn");
      }
    } catch {
      toast("Không sinh được mã phiếu", "warn");
    } finally {
      maLoading.value = false;
    }
  },
);
</script>

<style scoped>
.voucher-modal {
  font-family: 'Archivo', system-ui, sans-serif;
}

.voucher-modal-box {
  background: #fff;
  border-radius: 3px;
  width: 540px;
  max-width: calc(100vw - 40px);
  max-height: calc(100vh - 60px);
  overflow-y: auto;
  border: 1.5px solid #14181c;
  box-shadow: 0 16px 48px rgba(20, 24, 28, 0.18);
  animation: voucherSlideUp 0.25s ease;
}

@keyframes voucherSlideUp {
  from {
    transform: translateY(16px);
    opacity: 0;
  }
  to {
    transform: translateY(0);
    opacity: 1;
  }
}

.voucher-modal-head {
  padding: 24px 28px 18px;
  border-bottom: 1.5px solid #14181c;
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
}

.voucher-modal-title {
  font-size: 24px;
  font-weight: 800;
  letter-spacing: -0.01em;
  color: #14181c;
  line-height: 1.15;
}

.voucher-modal-sub {
  font-size: 12.5px;
  color: #6b6f76;
  margin-top: 4px;
  font-weight: 600;
}

.voucher-modal-close {
  width: 32px;
  height: 32px;
  border: 1px solid #d7d4cb;
  border-radius: 3px;
  background: #fff;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #3c4147;
  flex-shrink: 0;
}

.voucher-modal-close:hover {
  border-color: #ac3527;
  color: #ac3527;
}

.voucher-modal-body {
  padding: 22px 28px;
}

.voucher-label {
  font-size: 12px;
  font-weight: 700;
  color: #3c4147;
  display: block;
  margin-bottom: 6px;
}

.voucher-hint {
  font-size: 11.5px;
  color: #6b6f76;
  margin: 6px 0 0;
  font-weight: 500;
}

.type-btn {
  flex: 1;
  padding: 9px;
  border: 1px solid #d7d4cb;
  border-radius: 3px;
  background: #f3f1eb;
  font-size: 12px;
  font-weight: 700;
  cursor: pointer;
  color: #3c4147;
  text-align: center;
  transition:
    background 0.15s,
    color 0.15s,
    border-color 0.15s;
}

.type-btn.active {
  background: #14181c;
  border-color: #14181c;
  color: #f3f1eb;
}

.voucher-input {
  width: 100%;
  border: 1px solid #d7d4cb;
  border-radius: 3px;
  padding: 10px 12px;
  font-size: 13px;
  font-weight: 500;
  color: #14181c;
  background: #fff;
  outline: none;
  font-family: inherit;
}

.voucher-input:focus {
  border-color: #14181c;
}

.voucher-input.mono {
  text-transform: uppercase;
  font-family: 'IBM Plex Mono', ui-monospace, monospace;
  font-weight: 600;
}

.voucher-input.is-invalid {
  border-color: #ac3527;
  background: #f3e4e1;
}

.voucher-input:disabled {
  opacity: 0.65;
  background: #f3f1eb;
}

.voucher-modal-foot {
  padding: 16px 28px 22px;
  display: flex;
  gap: 10px;
  justify-content: flex-end;
  border-top: 1.5px solid #14181c;
  background: #edebe3;
}

.voucher-modal-foot .btn-primary-sol {
  background: #14181c;
  color: #f3f1eb;
  border: 1px solid #14181c;
  font-weight: 700;
  font-size: 13px;
  padding: 10px 16px;
  border-radius: 3px;
  cursor: pointer;
}

.voucher-modal-foot .btn-primary-sol:hover {
  background: #262b31;
}

.voucher-modal-foot .btn-outline-sol {
  height: auto;
  background: #fff;
  color: #14181c;
  border: 1px solid #14181c;
  font-weight: 700;
  font-size: 13px;
  padding: 10px 16px;
  border-radius: 3px;
  cursor: pointer;
}

.voucher-modal-foot .btn-outline-sol:hover {
  background: #14181c;
  color: #f3f1eb;
}

.voucher-field-error {
  margin: 4px 0 0;
  font-size: 11px;
  line-height: 1.35;
  color: #ac3527;
  font-weight: 600;
}
</style>
