<script setup>
import "@/styles/voucherCss.css";
import DashboardStat from "@/components/voucher/DashboardStat.vue";
import { ref, onMounted, watch } from "vue";

import {
  searchVoucher,
  deleteVoucher,
  createVoucher,
  updateVoucher,
} from "@/api/voucherApi.js";

import VoucherTable from "@/components/voucher/VoucherTable.vue";
import VoucherToolBar from "@/components/voucher/VoucherToolBar.vue";
import Pagination from "@/components/voucher/Pagination.vue";
import VoucherCreateModal from "@/components/voucher/VoucherCreateModal.vue";
import { confirm } from "@/composables/useConfirm";

/* ================= STATE ================= */
const showModal = ref(false);
const editingVoucher = ref(null);

const danhSach = ref([]);
const dangTai = ref(false);
const loi = ref("");

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

/* ================= LOAD DATA ================= */
const loadData = async (page = 1) => {
  dangTai.value = true;
  loi.value = "";

  try {
    const statusParam = locTrangThai.value
      ? locTrangThai.value.toUpperCase()
      : null;

    const res = await searchVoucher(
      tuKhoa.value || null,
      statusParam,
      locLoai.value || null,
      page,
      soDong
    );

    danhSach.value = res.data.content || [];
    voucherPage.value = res.data;
    trangHienTai.value = page;
  } catch (e) {
    console.error(e);
    loi.value = "Không thể tải dữ liệu";
  } finally {
    dangTai.value = false;
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
  daDuocChon.value = checked
    ? danhSach.value.map((p) => p.id)
    : [];
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

  try {
    dangTai.value = true;
    await deleteVoucher(phieu.id);
    await loadData(trangHienTai.value);
    refreshStats();
  } catch (e) {
    alert("Xóa thất bại");
    console.error(e);
  } finally {
    dangTai.value = false;
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
    alert("Tạo voucher thành công");
    await loadData(1);
    refreshStats();
  } catch (e) {
    console.error(e);
    alert("Tạo thất bại");
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
    alert("Cập nhật thành công");
    await loadData(trangHienTai.value);
    refreshStats();
  } catch (e) {
    console.error(e);
    alert("Cập nhật thất bại");
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

      <div v-if="dangTai">Loading...</div>
      <div v-else-if="loi">{{ loi }}</div>

      <VoucherTable
        v-else
        :items="danhSach"
        v-model:selected="daDuocChon"
        @xem="$emit('xem', $event)"
        @sua="openEdit"
        @xoa="xacNhanXoa"
        @chon-tat-ca="chonTatCa"
      />

      <!-- PAGINATION -->
      <Pagination
        :number="voucherPage.number"
        :total-pages="voucherPage.totalPages"
        :total-elements="voucherPage.totalElements"
        :number-of-elements="voucherPage.numberOfElements"
        :first="voucherPage.first"
        :last="voucherPage.last"
        @page-change="changePage"
      />
    </div>
  </div>
</template>

<style scoped>
.font-display {
  font-family: "Cormorant Garamond", serif;
}
</style>