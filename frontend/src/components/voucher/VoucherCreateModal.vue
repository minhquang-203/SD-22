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
              class="flex-1 py-[9px] border rounded-[8px] text-[12px]"
              :class="form.loai === 'PHAN_TRAM'
                ? 'bg-[#1e1510] border-[#1e1510] text-[#c8a97e]'
                : 'border-[#e6d8c8]'"
              @click="setType('PHAN_TRAM')"
            >
              % Phần trăm
            </button>

            <button
              class="flex-1 py-[9px] border rounded-[8px] text-[12px]"
              :class="form.loai === 'TIEN_MAT'
                ? 'bg-[#1e1510] border-[#1e1510] text-[#c8a97e]'
                : 'border-[#e6d8c8]'"
              @click="setType('TIEN_MAT')"
            >
              ₫ Số tiền
            </button>

            <button
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
          <div class="col-span-7">
            <label class="text-[11px] uppercase text-[rgba(30,21,16,0.5)] block mb-[6px]">
              Mã phiếu
            </label>

            <input
              v-model="form.ma"
              :disabled="isEdit"
              class="w-full border rounded-[8px] py-[10px] px-[14px]"
              style="text-transform: uppercase; font-family: monospace"
              placeholder="VD: SUMMER25"
            />
          </div>

          <!-- GIÁ TRỊ -->
          <div class="col-span-5">
            <label class="text-[11px] uppercase text-[rgba(30,21,16,0.5)] block mb-[6px]">
              Giá trị
            </label>

            <input
              v-model.number="form.giaTri"
              type="number"
              class="w-full border rounded-[8px] py-[10px] px-[14px]"
            />
          </div>

          <!-- TÊN -->
          <div class="col-span-12">
            <label class="text-[11px] uppercase text-[rgba(30,21,16,0.5)] block mb-[6px]">
              Tên chương trình
            </label>

            <input
              v-model="form.ten"
              class="w-full border rounded-[8px] py-[10px] px-[14px]"
              placeholder="VD: Ưu đãi hè"
            />
          </div>

          <!-- NGÀY -->
          <div class="col-span-6">
            <label class="text-[11px] uppercase text-[rgba(30,21,16,0.5)] block mb-[6px]">
              Bắt đầu
            </label>

            <input v-model="form.ngayBatDau" type="date" class="w-full border rounded-[8px] p-2" />
          </div>

          <div class="col-span-6">
            <label class="text-[11px] uppercase text-[rgba(30,21,16,0.5)] block mb-[6px]">
              Kết thúc
            </label>

            <input v-model="form.ngayKetThuc" type="date" class="w-full border rounded-[8px] p-2" />
          </div>

          <!-- SỐ LƯỢNG -->
          <div class="col-span-6">
            <label class="text-[11px] uppercase">Số lượng</label>
            <input v-model.number="form.soLuong" type="number" class="w-full border rounded-[8px] p-2" />
          </div>

          <!-- ĐƠN TỐI THIỂU -->
          <div class="col-span-6">
            <label class="text-[11px] uppercase">Đơn tối thiểu</label>
            <input v-model.number="form.giaTriDonToiThieu" type="number" class="w-full border rounded-[8px] p-2" />
          </div>

          <!-- GIẢM TỐI ĐA -->
          <div class="col-span-6">
            <label class="text-[11px] uppercase">Giảm tối đa</label>
            <input v-model.number="form.giamToiDa" type="number" class="w-full border rounded-[8px] p-2" />
          </div>
        </div>
      </div>

      <!-- FOOTER -->
      <div class="p-[20px_32px] flex justify-end gap-2 border-t">
        <button class="border px-4 py-2 rounded-[8px]" @click="close">
          Huỷ
        </button>

        <button class="bg-black text-[#c8a97e] px-4 py-2 rounded-[8px]" @click="submit">
          {{ isEdit ? "Cập nhật" : "Tạo mới" }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, watch, computed } from "vue";
import { Icon } from "@iconify/vue";

const props = defineProps({
  modelValue: Boolean,
  voucher: Object, // dùng khi edit
});

const emit = defineEmits(["update:modelValue", "create", "update"]);

const isEdit = computed(() => !!props.voucher);

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

const setType = (type) => {
  form.loai = type;

  if (type === "FREE_SHIP") {
    form.giaTri = 0;
    form.giamToiDa = 0;
  }
};

const close = () => {
  emit("update:modelValue", false);
};

const formatDate = (d) => {
  if (!d) return null;
  return `${d}T00:00:00`;
};

const submit = () => {
  const payload = {
    ...form,
    ngayBatDau: formatDate(form.ngayBatDau),
    ngayKetThuc: formatDate(form.ngayKetThuc),
  };

  if (isEdit.value) emit("update", payload);
  else emit("create", payload);

  close();
};

/* fill form khi edit */
watch(
  () => props.modelValue,
  (open) => {
    if (!open) return;

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
  }
);
</script>