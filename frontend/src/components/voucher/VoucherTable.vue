<template>
  <div class="table-responsive">
    <table class="coupon-table">
      <thead>
        <tr>
          <th style="width: 36px">
            <input type="checkbox" class="cb" @change="chonTatCa" />
          </th>
          <th style="width: 44px">STT</th>
          <th>Mã phiếu</th>
          <th>Tên chương trình</th>
          <th>Loại giảm giá</th>
          <th class="num">Mức giảm</th>
          <th>Hiệu lực</th>
          <th class="num">Đã dùng / tổng</th>
          <th>Trạng thái</th>
          <th style="width: 120px">Thao tác</th>
        </tr>
      </thead>
      <tbody>
        <tr v-if="items.length === 0">
          <td colspan="10" class="trang-thai-trong">
            Không tìm thấy phiếu giảm giá phù hợp với bộ lọc hiện tại.
          </td>
        </tr>
        <template v-else>
          <tr v-for="(phieu, idx) in items" :key="phieu.id">
            <td>
              <input
                type="checkbox"
                class="cb"
                :value="phieu.id"
                :checked="selected.includes(phieu.id)"
                @change="toggleSelect(phieu.id, $event)"
              />
            </td>
            <td class="stt-cell">{{ startIndex + idx + 1 }}</td>
            <td>
              <span class="code-chip">{{ phieu.ma }}</span>
            </td>
            <td>
              <div class="campaign-name">
                {{ phieu.ten }}
                <span v-if="phieu.phamVi === 'CA_NHAN'" class="private-tag">
                  Dành riêng
                </span>
              </div>
              <div class="campaign-min">
                Tối thiểu {{ formatTien(phieu.giaTriDonToiThieu) }}
              </div>
            </td>
            <td>
              <span class="type-tag" :class="typeClass(phieu.loai)">
                {{ tenLoai(phieu.loai) }}
              </span>
            </td>
            <td class="value-cell num">{{ hienThiGiaTri(phieu) }}</td>
            <td>
              <div class="date-primary">{{ formatNgay(phieu.ngayKetThuc) }}</div>
              <div
                class="date-secondary"
                :class="{ warn: isExpiringSoon(phieu) }"
              >
                {{ tinhHanCon(phieu) }}
              </div>
            </td>
            <td class="num">
              <div class="usage-cell">
                {{ soLuongDaDung(phieu).toLocaleString("vi") }} /
                {{ hienThiTong(phieu) }}
              </div>
              <div class="usage-bar">
                <div
                  class="usage-bar-fill"
                  :style="{ width: usagePercent(phieu) + '%' }"
                />
              </div>
              <div class="usage-remain">Còn lại {{ hienThiConLai(phieu) }}</div>
            </td>
            <td>
              <span class="status-dot" :class="classStatusDot(phieu)">
                {{ tenTrangThai(phieu) }}
              </span>
            </td>
            <td>
              <div class="row-actions">
                <button class="act-btn" title="Sửa" @click="$emit('sua', phieu)">
                  <Icon icon="mdi:pencil" />
                </button>
                <button
                  v-if="phieu.phamVi === 'CA_NHAN'"
                  class="act-btn"
                  title="Gán khách hàng"
                  @click="$emit('gan-khach', phieu)"
                >
                  <Icon icon="mdi:account-multiple-plus" />
                </button>
                <button
                  v-if="dangHoatDong(phieu)"
                  class="act-btn warn"
                  title="Tạm dừng"
                  :disabled="processingId === phieu.id"
                  @click="$emit('dung', phieu)"
                >
                  <Icon icon="mdi:pause" />
                </button>
                <button
                  v-else
                  class="act-btn success"
                  title="Kích hoạt lại"
                  :disabled="processingId === phieu.id"
                  @click="$emit('kich-hoat', phieu)"
                >
                  <Icon icon="mdi:play" />
                </button>
                <button
                  class="act-btn danger"
                  title="Xóa"
                  :disabled="processingId === phieu.id"
                  @click="$emit('xoa', phieu)"
                >
                  <Icon icon="mdi:trash-can-outline" />
                </button>
              </div>
            </td>
          </tr>
          <tr
            v-for="n in Math.max(0, 5 - items.length)"
            :key="`pad-${n}`"
            class="table-pad-row"
            aria-hidden="true"
          >
            <td colspan="10" />
          </tr>
        </template>
      </tbody>
    </table>
  </div>
</template>

<script setup>
import { Icon } from "@iconify/vue";

const props = defineProps({
  items: { type: Array, required: true, default: () => [] },
  selected: { type: Array, default: () => [] },
  processingId: { type: Number, default: null },
  startIndex: { type: Number, default: 0 },
});

const emit = defineEmits([
  "sua",
  "dung",
  "kich-hoat",
  "xoa",
  "chon-tat-ca",
  "gan-khach",
  "update:selected",
]);

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

const soLuongDaDung = (phieu) => Number(phieu.daDung ?? 0);

const soLuongConLai = (phieu) =>
  phieu.soLuong != null ? Number(phieu.soLuong) : null;

const hienThiConLai = (phieu) => {
  const con = soLuongConLai(phieu);
  return con != null ? con.toLocaleString("vi") : "∞";
};

const hienThiTong = (phieu) => {
  const con = soLuongConLai(phieu);
  if (con == null) return "∞";
  return (soLuongDaDung(phieu) + con).toLocaleString("vi");
};

const usagePercent = (phieu) => {
  const used = soLuongDaDung(phieu);
  const remain = soLuongConLai(phieu);
  if (remain == null) return 0;
  const total = used + remain;
  if (total <= 0) return 0;
  return Math.min(100, Math.round((used / total) * 100));
};

const formatTien = (so) => {
  if (!so && so !== 0) return "0 đ";
  return `${new Intl.NumberFormat("vi-VN").format(so)} đ`;
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

const daysLeft = (phieu) => {
  const now = new Date();
  const ketThuc = new Date(phieu.ngayKetThuc || "");
  return Math.ceil((ketThuc - now) / (1000 * 60 * 60 * 24));
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
  if (daysLeft(phieu) <= 7) return "expiring";
  return "active";
};

const tenTrangThai = (phieu) => {
  const map = {
    active: "Đang hoạt động",
    expiring: "Sắp hết hạn",
    upcoming: "Sắp diễn ra",
    expired: "Đã hết hạn",
    paused: "Ngừng áp dụng",
  };
  const key = tinhTrangThai(phieu);
  if (key === "expiring") return map.expiring;
  if (phieu.timeStatusLabel && key !== "expiring") return phieu.timeStatusLabel;
  return map[key] || "Không xác định";
};

const classStatusDot = (phieu) => {
  const map = {
    active: "status-active",
    expiring: "status-expiring",
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

const typeClass = (loai) => {
  const map = {
    PHAN_TRAM: "percent",
    TIEN_MAT: "amount",
    FREE_SHIP: "ship",
  };
  return map[loai] ?? "";
};

const hienThiGiaTri = (phieu) => {
  if (phieu.loai === "PHAN_TRAM") return phieu.giaTri + "%";
  if (phieu.loai === "FREE_SHIP") return "Miễn phí";
  return formatTien(phieu.giaTri);
};

const isExpiringSoon = (phieu) => {
  const ts = tinhTrangThai(phieu);
  return ts === "expiring" || (ts === "active" && daysLeft(phieu) <= 7);
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

const chonTatCa = (e) => emit("chon-tat-ca", e.target.checked);
</script>
