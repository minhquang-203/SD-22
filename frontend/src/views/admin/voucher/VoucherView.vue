<script setup>
import "@/styles/voucherCss.css";
import DashboardStat from "@/components/voucher/DashboardStat.vue";
import { ref, onMounted, watch } from "vue";

import {
  searchVoucher,
  deleteVoucher,
  stopVoucher,
  activateVoucher,
  createVoucher,
  updateVoucher,
} from "@/api/voucherApi.js";

import VoucherTable from "@/components/voucher/VoucherTable.vue";
import VoucherToolBar from "@/components/voucher/VoucherToolBar.vue";
import Pagination from "@/components/voucher/Pagination.vue";
import VoucherCreateModal from "@/components/voucher/VoucherCreateModal.vue";
import { confirm } from "@/composables/useConfirm";
import { toast } from "@/composables/useToast";

/* ================= STATE ================= */
const showModal = ref(false);
const editingVoucher = ref(null);

const danhSach = ref([]);
const taiLanDau = ref(true);
const loi = ref("");
const dangXuLyId = ref(null);

const daDuocChon = ref([]);

/* filter */
const tuKhoa = ref("");
const locTrangThai = ref("");
const locLoai = ref("");

/* pagination */
const trangHienTai = ref(1);
const soDong = 10;

const voucherPage = ref({
  content: [],
  totalElements: 0,
  totalPages: 0,
  number: 0,
  numberOfElements: 0,
  first: true,
  last: true,
});

const statsRefreshKey = ref(0);
const refreshStats = () => {
  statsRefreshKey.value += 1;
};

function resolveVoucherStatus(phieu, isActive) {
  if (!isActive) {
    return {
      ...phieu,
      isActive: false,
      timeStatus: "INACTIVE",
      timeStatusLabel: "Ngừng áp dụng",
    };
  }

  const now = new Date();
  const batDau = new Date(phieu.ngayBatDau || "");
  const ketThuc = new Date(phieu.ngayKetThuc || "");

  if (now < batDau) {
    return {
      ...phieu,
      isActive: true,
      timeStatus: "UPCOMING",
      timeStatusLabel: "Sắp diễn ra",
    };
  }
  if (now > ketThuc) {
    return {
      ...phieu,
      isActive: true,
      timeStatus: "EXPIRED",
      timeStatusLabel: "Đã hết hạn",
    };
  }
  return {
    ...phieu,
    isActive: true,
    timeStatus: "ACTIVE",
    timeStatusLabel: "Đang hoạt động",
  };
}

function capNhatPhieuLocal(id, isActive) {
  danhSach.value = danhSach.value.map((p) =>
    p.id === id ? resolveVoucherStatus(p, isActive) : p,
  );
}

/* ================= LOAD DATA ================= */
const loadData = async (page = 1, { silent = false } = {}) => {
  if (!silent && danhSach.value.length === 0) {
    taiLanDau.value = true;
  }
  loi.value = "";

  try {
    const statusParam = locTrangThai.value
      ? locTrangThai.value.toUpperCase() === "INACTIVE"
        ? "INACTIVE"
        : locTrangThai.value.toUpperCase()
      : null;

    const res = await searchVoucher(
      tuKhoa.value || null,
      statusParam,
      locLoai.value || null,
      page,
      soDong,
    );

    danhSach.value = res.data.content || [];
    voucherPage.value = res.data;
    trangHienTai.value = page;
  } catch (e) {
    console.error(e);
    if (danhSach.value.length === 0) {
      loi.value = "Không thể tải dữ liệu";
    } else {
      toast("Không thể tải dữ liệu", "warn");
    }
  } finally {
    taiLanDau.value = false;
  }
};

/* ================= INIT ================= */
onMounted(() => loadData(1));

watch([tuKhoa, locTrangThai, locLoai], () => {
  loadData(1);
});

/* ================= PAGINATION ================= */
const changePage = (pageIndex) => {
  loadData(pageIndex + 1);
};

/* ================= SELECTION ================= */
const chonTatCa = (checked) => {
  daDuocChon.value = checked ? danhSach.value.map((p) => p.id) : [];
};

/* ================= STOP / ACTIVATE ================= */
const xacNhanDung = async (phieu) => {
  const ok = await confirm({
    title: "Dừng chương trình",
    message: `Dừng phiếu "${phieu.ma}"? Phiếu sẽ không còn hiệu lực.`,
    confirmText: "Dừng",
    danger: true,
  });
  if (!ok) return;

  dangXuLyId.value = phieu.id;
  try {
    await stopVoucher(phieu.id);
    capNhatPhieuLocal(phieu.id, false);
    refreshStats();
    toast(`Đã dừng phiếu "${phieu.ma}"`, "info");
  } catch (e) {
    toast("Dừng chương trình thất bại", "warn");
    console.error(e);
  } finally {
    dangXuLyId.value = null;
  }
};

const xacNhanKichHoat = async (phieu) => {
  const ok = await confirm({
    title: "Kích hoạt lại",
    message: `Kích hoạt lại phiếu "${phieu.ma}"?`,
    confirmText: "Kích hoạt",
  });
  if (!ok) return;

  dangXuLyId.value = phieu.id;
  try {
    await activateVoucher(phieu.id);
    capNhatPhieuLocal(phieu.id, true);
    refreshStats();
    toast(`Đã kích hoạt phiếu "${phieu.ma}"`, "info");
  } catch (e) {
    toast("Kích hoạt thất bại", "warn");
    console.error(e);
  } finally {
    dangXuLyId.value = null;
  }
};

/* ================= DELETE ================= */
const xacNhanXoa = async (phieu) => {
  const ok = await confirm({
    title: "Xóa phiếu giảm giá",
    message: `Xóa phiếu "${phieu.ma}"?`,
    confirmText: "Xóa",
    danger: true,
  });
  if (!ok) return;

  dangXuLyId.value = phieu.id;
  try {
    await deleteVoucher(phieu.id);
    danhSach.value = danhSach.value.filter((p) => p.id !== phieu.id);
    daDuocChon.value = daDuocChon.value.filter((id) => id !== phieu.id);
    if (voucherPage.value.totalElements > 0) {
      voucherPage.value.totalElements -= 1;
      voucherPage.value.numberOfElements = danhSach.value.length;
    }
    refreshStats();
    toast(`Đã xóa phiếu "${phieu.ma}"`, "info");
  } catch (e) {
    toast("Xóa thất bại", "warn");
    console.error(e);
  } finally {
    dangXuLyId.value = null;
  }
};

/* ================= CREATE ================= */
const handleCreate = async (payload) => {
  const ok = await confirm({
    title: "Tạo phiếu giảm giá",
    message: "Lưu phiếu giảm giá mới?",
    confirmText: "Tạo",
  });
  if (!ok) return;
  try {
    await createVoucher(payload);
    showModal.value = false;
    editingVoucher.value = null;
    toast("Tạo phiếu giảm giá thành công", "info");
    await loadData(1);
    refreshStats();
  } catch (e) {
    console.error(e);
    toast("Tạo thất bại", "warn");
  }
};

/* ================= UPDATE ================= */
const handleUpdate = async (payload) => {
  const ok = await confirm({
    title: "Cập nhật phiếu",
    message: "Cập nhật phiếu giảm giá này?",
    confirmText: "Cập nhật",
  });
  if (!ok) return;
  try {
    await updateVoucher(payload.id, payload);
    showModal.value = false;
    editingVoucher.value = null;
    toast("Cập nhật phiếu giảm giá thành công", "info");
    await loadData(trangHienTai.value, { silent: true });
    refreshStats();
  } catch (e) {
    console.error(e);
    toast("Cập nhật thất bại", "warn");
  }
};

/* ================= MODAL CONTROL ================= */
const openCreate = () => {
  editingVoucher.value = null;
  showModal.value = true;
};

const openEdit = (voucher) => {
  editingVoucher.value = voucher;
  showModal.value = true;
};

/* ================= PLACEHOLDER ================= */
const handleSort = () => console.log("sort");
const handleExport = () => console.log("export");
</script>

<template>
  <div class="flex flex-col gap-6">

    <!-- HEADER -->
    <div class="flex justify-between items-end">
      <div>
        <h1 class="font-display text-[42px] italic">
          Phiếu Giảm Giá
        </h1>
        <p class="text-[13px] opacity-60">
          Quản lý chương trình ưu đãi
        </p>
      </div>

      <button
        @click="openCreate"
        class="bg-black text-[#C9A96E] px-5 py-2 rounded-lg"
      >
        + Tạo phiếu mới
      </button>
    </div>

    <!-- MODAL -->
    <VoucherCreateModal
      v-model="showModal"
      :voucher="editingVoucher"
      @create="handleCreate"
      @update="handleUpdate"
    />

    <!-- STATS -->
    <DashboardStat :refresh-key="statsRefreshKey" />

    <!-- TOOLBAR -->
    <VoucherToolBar
      v-model:search="tuKhoa"
      v-model:status="locTrangThai"
      v-model:type="locLoai"
      @sort="handleSort"
      @export="handleExport"
    />

    <!-- TABLE -->
    <div class="table-card">
      <div v-if="taiLanDau" class="table-loading">Đang tải...</div>
      <div v-else-if="loi" class="table-error">{{ loi }}</div>

      <template v-else>
        <VoucherTable
          :items="danhSach"
          v-model:selected="daDuocChon"
          :processing-id="dangXuLyId"
          @sua="openEdit"
          @dung="xacNhanDung"
          @kich-hoat="xacNhanKichHoat"
          @xoa="xacNhanXoa"
          @chon-tat-ca="chonTatCa"
        />

        <Pagination
          :number="voucherPage.number"
          :total-pages="voucherPage.totalPages"
          :total-elements="voucherPage.totalElements"
          :number-of-elements="voucherPage.numberOfElements"
          :first="voucherPage.first"
          :last="voucherPage.last"
          @page-change="changePage"
        />
      </template>
    </div>
  </div>
</template>

<style scoped>
.font-display {
  font-family: "Cormorant Garamond", serif;
}

.table-loading,
.table-error {
  padding: 48px 24px;
  text-align: center;
  color: rgba(30, 21, 16, 0.55);
  font-size: 14px;
}
</style>
