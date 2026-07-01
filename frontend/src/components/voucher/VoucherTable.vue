<template>
  <div class="table-responsive">
    <table class="coupon-table">
      <thead>
        <tr>
          <th>
            <input
              type="checkbox"
              style="accent-color: var(--bronze)"
              @change="chonTatCa"
            />
          </th>
          <th>Mã phiếu</th>
          <th>Tên chương trình</th>
          <th>Loại giảm giá</th>
          <th>Mức giảm</th>
          <th>Hiệu lực</th>
          <th>Số lượng</th>
          <th>Trạng thái</th>
          <th></th>
        </tr>
      </thead>
      <tbody>
        <tr v-if="items.length === 0">
          <td colspan="9" class="trang-thai-trong">
            <i class="bi bi-inbox"></i>
            <span>Không có phiếu nào</span>
          </td>
        </tr>
        <tr v-for="phieu in items" :key="phieu.id">
          <td>
            <input
              type="checkbox"
              style="accent-color: var(--bronze)"
              :value="phieu.id"
              :checked="selected.includes(phieu.id)"
              @change="toggleSelect(phieu.id, $event)"
            />
          </td>
          <td>
            <span class="coupon-code">{{ phieu.ma }}</span>
          </td>
          <td>
            <div style="font-weight: 400">{{ phieu.ten }}</div>
            <div style="font-size: 11px; color: rgba(30, 21, 16, 0.4)">
              Tối thiểu {{ formatTien(phieu.giaTriDonToiThieu) }}
            </div>
          </td>
          <td>
            <span class="discount-pill" :class="pillLoai(phieu.loai)">
              <i class="bi" :class="iconLoai(phieu.loai)"></i>
              {{ tenLoai(phieu.loai) }}
            </span>
          </td>
          <td
            style="font-weight: 500"
            :style="{ color: mauGiaTri(phieu.loai) }"
          >
            {{ hienThiGiaTri(phieu) }}
          </td>
          <td>
            <div style="font-size: 12px">
              {{ formatNgay(phieu.ngayKetThuc) }}
            </div>
            <div style="font-size: 11px" :style="{ color: mauHanCon(phieu) }">
              {{ tinhHanCon(phieu) }}
            </div>
          </td>
          <td>
            <div style="font-size: 12px; margin-bottom: 5px">
              {{
                phieu.soLuong != null ? phieu.soLuong.toLocaleString("vi") : "∞"
              }}
            </div>
            <div class="progress-mini">
              <div class="progress-mini-bar" style="width: 50%"></div>
            </div>
          </td>
          <td>
            <span class="status-dot" :class="classStatusDot(phieu)">
              {{ tenTrangThai(phieu) }}
            </span>
          </td>
          <td>
            <div class="actions-cell">
              <button class="act-btn" title="Sửa" @click="$emit('sua', phieu)">
                <Icon icon="mdi:pencil"></Icon>
              </button>
              <button
                v-if="dangHoatDong(phieu)"
                class="act-btn warn"
                title="Dừng chương trình"
                :disabled="processingId === phieu.id"
                @click="$emit('dung', phieu)"
              >
                <Icon icon="mdi:pause-circle"></Icon>
              </button>
              <button
                v-else
                class="act-btn success"
                title="Kích hoạt lại"
                :disabled="processingId === phieu.id"
                @click="$emit('kich-hoat', phieu)"
              >
                <Icon icon="mdi:play-circle"></Icon>
              </button>
              <button
                class="act-btn danger"
                title="Xóa"
                :disabled="processingId === phieu.id"
                @click="$emit('xoa', phieu)"
              >
                <Icon icon="mdi:trash"></Icon>
              </button>
            </div>
          </td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<script setup>
import { defineProps, defineEmits } from "vue";
import { Icon } from "@iconify/vue";

const props = defineProps({
  items: { type: Array, required: true, default: () => [] },
  selected: { type: Array, default: () => [] },
  processingId: { type: Number, default: null },
});

const emit = defineEmits(["sua", "dung", "kich-hoat", "xoa", "chon-tat-ca"]);

const toggleSelect = (id, e) => {
  if (e.target.checked) {
    emit("update:selected", [...props.selected, id]);
  } else {
    emit(
      "update:selected",
      props.selected.filter((item) => item !== id),
    );
  }
};

// ====================== HELPER FUNCTIONS ======================
const formatTien = (so) => {
  if (!so && so !== 0) return "—";
  return new Intl.NumberFormat("vi-VN", {
    style: "currency",
    currency: "VND",
  }).format(so);
};

const formatNgay = (chuoi) => {
  if (!chuoi) return "—";
  const d = new Date(chuoi);
  return d.toLocaleDateString("vi-VN", {
    day: "2-digit",
    month: "2-digit",
    year: "numeric",
  });
};

const tinhTrangThai = (phieu) => {
  if (phieu.isActive === false || phieu.timeStatus === "INACTIVE") {
    return "paused";
  }
  const now = new Date();
  const batDau = new Date(phieu.ngayBatDau || "");
  const ketThuc = new Date(phieu.ngayKetThuc || "");
  if (now < batDau) return "upcoming";
  if (now > ketThuc) return "expired";
  return "active";
};

const tenTrangThai = (phieu) => {
  if (phieu.timeStatusLabel) return phieu.timeStatusLabel;
  const map = {
    active: "Đang hoạt động",
    upcoming: "Sắp diễn ra",
    expired: "Đã hết hạn",
    paused: "Ngừng áp dụng",
  };
  return map[tinhTrangThai(phieu)] || "Không xác định";
};

const classStatusDot = (phieu) => {
  const map = {
    active: "status-active",
    paused: "status-paused",
    upcoming: "status-upcoming",
    expired: "status-expired",
  };
  return map[tinhTrangThai(phieu)] || "";
};

const dangHoatDong = (phieu) => phieu.isActive !== false;

const tenLoai = (loai) => {
  const map = {
    PHAN_TRAM: "Phần trăm",
    TIEN_MAT: "Số tiền",
    FREE_SHIP: "Miễn ship",
  };
  return map[loai] ?? loai;
};

const pillLoai = (loai) => {
  const map = {
    PHAN_TRAM: "dp-percent",
    TIEN_MAT: "dp-fixed",
    FREE_SHIP: "dp-ship",
  };
  return map[loai] ?? "";
};

const iconLoai = (loai) => {
  const map = {
    PHAN_TRAM: "bi-percent",
    TIEN_MAT: "bi-cash",
    FREE_SHIP: "bi-truck",
  };
  return map[loai] ?? "";
};

const mauGiaTri = (loai) => {
  const map = {
    PHAN_TRAM: "var(--bronze)",
    TIEN_MAT: "var(--sky)",
    FREE_SHIP: "var(--sage)",
  };
  return map[loai] ?? "var(--ink)";
};

const hienThiGiaTri = (phieu) => {
  if (phieu.loai === "PHAN_TRAM") return phieu.giaTri + "%";
  if (phieu.loai === "FREE_SHIP") return "Miễn phí";
  return formatTien(phieu.giaTri);
};

const tinhHanCon = (phieu) => {
  const now = new Date();
  const ketThuc = new Date(phieu.ngayKetThuc || "");
  const batDau = new Date(phieu.ngayBatDau || "");
  const ms = 1000 * 60 * 60 * 24;

  if (now < batDau) {
    const con = Math.ceil((batDau - now) / ms);
    return `Bắt đầu sau ${con} ngày`;
  }
  if (now > ketThuc) return "Đã kết thúc";
  const con = Math.ceil((ketThuc - now) / ms);
  return `Còn ${con} ngày`;
};

const mauHanCon = (phieu) => {
  const ts = tinhTrangThai(phieu);
  if (ts === "expired") return "rgba(30,21,16,.35)";
  if (ts === "upcoming") return "var(--sky)";
  const now = new Date();
  const con = Math.ceil(
    (new Date(phieu.ngayKetThuc || "") - now) / (1000 * 60 * 60 * 24),
  );
  return con <= 7 ? "var(--coral)" : "rgba(30,21,16,.35)";
};

const chonTatCa = (e) => emit("chon-tat-ca", e.target.checked);
</script>
