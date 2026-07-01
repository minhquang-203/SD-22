<template>
  <div
    v-if="modelValue"
    class="admin-modal-backdrop backdrop-blur-sm"
    @click.self="close"
  >
    <!-- MODAL BOX -->
    <div
      class="bg-white rounded-[16px] w-[540px] max-w-[calc(100vw-40px)] max-h-[calc(100vh-60px)] overflow-y-auto border border-[#e6d8c8] shadow-[0_24px_80px_rgba(30,21,16,0.2)] animate-[slideUp_0.3s_ease]"
    >
      <!-- HEADER -->
      <div class="p-[28px_32px_20px] border-b border-[#e6d8c8] flex items-start justify-between">
        <div>
          <div class="text-[28px] font-light italic leading-[1.1] text-[#1e1510]">
            {{ isEdit ? "Cập nhật phiếu giảm giá" : "Tạo phiếu mới" }}
          </div>

          <div class="text-[12px] text-[rgba(30,21,16,0.4)] mt-[3px]">
            Cấu hình chương trình ưu đãi
          </div>
        </div>

        <button
          class="w-8 h-8 border border-[#e6d8c8] rounded-[8px] flex items-center justify-center text-[rgba(30,21,16,0.5)] text-sm hover:border-[#ff6b6b] hover:text-[#ff6b6b]"
          @click="close"
        >
          <Icon icon="mdi:close" />
        </button>
      </div>

      <!-- BODY -->
      <div class="p-[24px_32px]">
        <!-- LOẠI GIẢM GIÁ -->
        <div class="mb-3">
          <label class="text-[11px] uppercase text-[rgba(30,21,16,0.5)] block mb-[6px]">
            Loại giảm giá
          </label>

          <div class="flex gap-2">
            <button
              type="button"
              class="flex-1 py-[9px] border rounded-[8px] text-[12px]"
              :class="form.loai === 'PHAN_TRAM'
                ? 'bg-[#1e1510] border-[#1e1510] text-[#c8a97e]'
                : 'border-[#e6d8c8]'"
              @click="setType('PHAN_TRAM')"
            >
              % Phần trăm
            </button>

            <button
              type="button"
              class="flex-1 py-[9px] border rounded-[8px] text-[12px]"
              :class="form.loai === 'TIEN_MAT'
                ? 'bg-[#1e1510] border-[#1e1510] text-[#c8a97e]'
                : 'border-[#e6d8c8]'"
              @click="setType('TIEN_MAT')"
            >
              ₫ Số tiền
            </button>

            <button
              type="button"
              class="flex-1 py-[9px] border rounded-[8px] text-[12px]"
              :class="form.loai === 'FREE_SHIP'
                ? 'bg-[#1e1510] border-[#1e1510] text-[#c8a97e]'
                : 'border-[#e6d8c8]'"
              @click="setType('FREE_SHIP')"
            >
              🚚 Miễn ship
            </button>
          </div>
        </div>

        <!-- GRID -->
        <div class="grid grid-cols-12 gap-3">

          <!-- MÃ -->
          <div :class="showGiaTri ? 'col-span-7' : 'col-span-12'">
            <label class="text-[11px] uppercase text-[rgba(30,21,16,0.5)] block mb-[6px]">
              Mã phiếu *
            </label>

            <input
              v-model="form.ma"
              :disabled="isEdit"
              :class="inputClass('ma')"
              style="text-transform: uppercase; font-family: monospace"
              placeholder="VD: SUMMER25"
              @input="clearError('ma')"
            />
            <p v-if="errors.ma" class="voucher-field-error">{{ errors.ma }}</p>
          </div>

          <!-- GIÁ TRỊ -->
          <div v-if="showGiaTri" class="col-span-5">
            <label class="text-[11px] uppercase text-[rgba(30,21,16,0.5)] block mb-[6px]">
              {{ giaTriLabel }} *
            </label>

            <input
              v-model.number="form.giaTri"
              type="number"
              :min="form.loai === 'PHAN_TRAM' ? 1 : 1"
              :max="form.loai === 'PHAN_TRAM' ? 100 : undefined"
              :class="inputClass('giaTri')"
              :placeholder="form.loai === 'PHAN_TRAM' ? 'VD: 20' : 'VD: 50000'"
              @input="clearError('giaTri')"
            />
            <p v-if="errors.giaTri" class="voucher-field-error">{{ errors.giaTri }}</p>
          </div>

          <!-- TÊN -->
          <div class="col-span-12">
            <label class="text-[11px] uppercase text-[rgba(30,21,16,0.5)] block mb-[6px]">
              Tên chương trình *
            </label>

            <input
              v-model="form.ten"
              :class="inputClass('ten')"
              placeholder="VD: Ưu đãi hè"
              @input="clearError('ten')"
            />
            <p v-if="errors.ten" class="voucher-field-error">{{ errors.ten }}</p>
          </div>

          <!-- NGÀY -->
          <div class="col-span-6">
            <label class="text-[11px] uppercase text-[rgba(30,21,16,0.5)] block mb-[6px]">
              Bắt đầu *
            </label>

            <input
              v-model="form.ngayBatDau"
              type="date"
              :class="inputClass('ngayBatDau')"
              @change="clearError('ngayBatDau')"
            />
            <p v-if="errors.ngayBatDau" class="voucher-field-error">{{ errors.ngayBatDau }}</p>
          </div>

          <div class="col-span-6">
            <label class="text-[11px] uppercase text-[rgba(30,21,16,0.5)] block mb-[6px]">
              Kết thúc *
            </label>

            <input
              v-model="form.ngayKetThuc"
              type="date"
              :min="form.ngayBatDau || undefined"
              :class="inputClass('ngayKetThuc')"
              @change="clearError('ngayKetThuc')"
            />
            <p v-if="errors.ngayKetThuc" class="voucher-field-error">{{ errors.ngayKetThuc }}</p>
          </div>

          <!-- SỐ LƯỢNG -->
          <div class="col-span-6">
            <label class="text-[11px] uppercase text-[rgba(30,21,16,0.5)] block mb-[6px]">
              Số lượng *
            </label>
            <input
              v-model.number="form.soLuong"
              type="number"
              min="1"
              step="1"
              :class="inputClass('soLuong')"
              placeholder="VD: 100"
              @input="clearError('soLuong')"
            />
            <p v-if="errors.soLuong" class="voucher-field-error">{{ errors.soLuong }}</p>
          </div>

          <!-- ĐƠN TỐI THIỂU -->
          <div class="col-span-6">
            <label class="text-[11px] uppercase text-[rgba(30,21,16,0.5)] block mb-[6px]">
              Đơn tối thiểu
            </label>
            <input
              v-model.number="form.giaTriDonToiThieu"
              type="number"
              min="0"
              :class="inputClass('giaTriDonToiThieu')"
              placeholder="0 = không yêu cầu"
              @input="clearError('giaTriDonToiThieu')"
            />
            <p v-if="errors.giaTriDonToiThieu" class="voucher-field-error">{{ errors.giaTriDonToiThieu }}</p>
          </div>

          <!-- GIẢM TỐI ĐA (chỉ áp dụng khi giảm theo %) -->
          <div v-if="showGiamToiDa" class="col-span-6">
            <label class="text-[11px] uppercase text-[rgba(30,21,16,0.5)] block mb-[6px]">
              Giảm tối đa
            </label>
            <input
              v-model.number="form.giamToiDa"
              type="number"
              min="1"
              :class="inputClass('giamToiDa')"
              placeholder="Không giới hạn nếu để trống"
              @input="clearError('giamToiDa')"
            />
            <p v-if="errors.giamToiDa" class="voucher-field-error">{{ errors.giamToiDa }}</p>
          </div>
        </div>
      </div>

      <!-- FOOTER -->
      <div class="p-[20px_32px] flex justify-end gap-2 border-t">
        <button type="button" class="border px-4 py-2 rounded-[8px]" @click="close">
          Huỷ
        </button>

        <button type="button" class="bg-black text-[#c8a97e] px-4 py-2 rounded-[8px]" @click="submit">
          {{ isEdit ? "Cập nhật" : "Tạo mới" }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, watch, computed } from "vue";
import { Icon } from "@iconify/vue";
import { toast } from "@/composables/useToast";

const props = defineProps({
  modelValue: Boolean,
  voucher: Object,
});

const emit = defineEmits(["update:modelValue", "create", "update"]);

const isEdit = computed(() => !!props.voucher);
const showGiamToiDa = computed(() => form.loai === "PHAN_TRAM");
const showGiaTri = computed(() => form.loai !== "FREE_SHIP");

const giaTriLabel = computed(() => {
  if (form.loai === "PHAN_TRAM") return "Phần trăm (%)";
  if (form.loai === "TIEN_MAT") return "Số tiền giảm";
  return "Giá trị";
});

const form = reactive({
  ma: "",
  ten: "",
  loai: "PHAN_TRAM",
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

const INPUT_BASE = "w-full border rounded-[8px] py-[10px] px-[14px]";
const INPUT_INVALID = "border-[#ff6b6b] bg-[#fff8f7]";
const INPUT_NORMAL = "border-[#e6d8c8]";

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
    form.giaTri = null;
    form.giamToiDa = null;
  } else if (type === "TIEN_MAT") {
    form.giamToiDa = null;
  }

  clearError("giaTri");
  clearError("giamToiDa");
}

function close() {
  emit("update:modelValue", false);
};

const formatDate = (d) => {
  if (!d) return null;
  return `${d}T00:00:00`;
};

function isEmptyNumber(value) {
  return value === null || value === undefined || value === "";
}

function validateForm() {
  resetErrors();
  const next = {};

  if (!isEdit.value) {
    const ma = form.ma?.trim();
    if (!ma) {
      next.ma = "Mã phiếu không được để trống";
    } else if (!/^[A-Z0-9_-]{2,30}$/i.test(ma)) {
      next.ma = "Mã chỉ gồm chữ, số, gạch ngang (2–30 ký tự)";
    }
  }

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
  }

  if (!form.ngayKetThuc) {
    next.ngayKetThuc = "Chọn ngày kết thúc";
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
    ma: form.ma.trim().toUpperCase(),
    ten: form.ten.trim(),
    giaTri: form.loai === "FREE_SHIP" ? 1 : Number(form.giaTri),
    giaTriDonToiThieu: isEmptyNumber(form.giaTriDonToiThieu)
      ? null
      : Number(form.giaTriDonToiThieu),
    giamToiDa:
      form.loai === "PHAN_TRAM" && !isEmptyNumber(form.giamToiDa)
        ? Number(form.giamToiDa)
        : null,
    soLuong: Number(form.soLuong),
    ngayBatDau: formatDate(form.ngayBatDau),
    ngayKetThuc: formatDate(form.ngayKetThuc),
  };
}

const submit = () => {
  if (!validateForm()) return;

  const payload = buildPayload();

  if (isEdit.value) emit("update", payload);
  else emit("create", payload);

  close();
};

watch(
  () => props.modelValue,
  (open) => {
    if (!open) return;

    resetErrors();

    if (props.voucher) {
      Object.assign(form, {
        ...props.voucher,
        ngayBatDau: props.voucher.ngayBatDau?.slice(0, 10),
        ngayKetThuc: props.voucher.ngayKetThuc?.slice(0, 10),
      });
    } else {
      Object.assign(form, {
        ma: "",
        ten: "",
        loai: "PHAN_TRAM",
        giaTri: null,
        giaTriDonToiThieu: null,
        giamToiDa: null,
        soLuong: null,
        ngayBatDau: null,
        ngayKetThuc: null,
      });
    }
  },
);
</script>

<style scoped>
.voucher-field-error {
  margin: 4px 0 0;
  font-size: 11px;
  line-height: 1.35;
  color: #ff6b6b;
}
</style>
