# HƯỚNG DẪN HIỂU CÁCH CODE GIAO DIỆN FRONTEND — DỰ ÁN SUNOVA

**Dự án:** SUNOVA — Hệ thống quản lý cửa hàng mỹ phẩm chống nắng  
**Công nghệ frontend:** Vue 3 + Vue Router + Axios + Tailwind CSS v4 + CSS tùy chỉnh (Soleil)  
**Ngày tạo tài liệu:** 03/06/2026

---

## MỤC LỤC

1. Tổng thể: App chạy như thế nào?
2. Cấu trúc thư mục frontend
3. Cấu trúc một file .vue
4. Khung Admin (AdminLayout)
5. Cách gọi API
6. Cách làm giao diện (CSS)
7. Component — chia nhỏ màn hình
8. Ví dụ luồng hoàn chỉnh: POS bán hàng
9. So sánh với HTML thuần
10. Bảng tra cứu nhanh
11. Giải thích cho thầy về phần Admin (chưa phân quyền)

---

## 1. TỔNG THỂ: APP CHẠY NHƯ THẾ NÀO?

### Luồng đơn giản

1. Người dùng mở trình duyệt, ví dụ: `localhost:5173/admin/pos`
2. **Router** (`router/index.js`) đọc URL và quyết định load màn hình nào
3. Với URL `/admin/*`, app load **AdminLayout** — khung cố định gồm sidebar + header
4. Trong khung đó, vùng **router-view** hiển thị màn hình tương ứng (ví dụ `PosPage.vue`)
5. Màn hình gọi **API** (`api/*.js`) → backend Spring Boot trả JSON
6. Dữ liệu JSON được gán vào biến → giao diện tự cập nhật

### Sơ đồ luồng

```
main.js (khởi động app)
    ↓
App.vue (vỏ ngoài cùng)
    ↓
router/index.js (điều hướng theo URL)
    ├── /admin/*  → AdminLayout (sidebar + header + nội dung)
    │                   └── PosPage, VariantList, OrderList...
    └── /quiz       → SkinQuiz (giao diện khách hàng)
                            ↓
                    api/*.js → Backend /api/...
```

### File khởi động

**`frontend/src/main.js`**
```js
import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import './styles/main.css'

createApp(App).use(router).mount('#app')
```

Ý nghĩa: Tạo app Vue, gắn router (điều hướng), import CSS chung, mount vào thẻ `#app` trong `index.html`.

---

## 2. CẤU TRÚC THƯ MỤC FRONTEND

| Thư mục / File | Vai trò | Ví dụ cụ thể |
|---|---|---|
| `views/` | **Màn hình chính** — mỗi trang một file | `PosPage.vue`, `VariantList.vue`, `OrderList.vue` |
| `components/` | **Khối tái sử dụng** — dùng lại nhiều màn | `ProductTable.vue`, `ConfirmDialog.vue` |
| `layouts/` | **Khung bố cục** — sidebar, header cố định | `AdminLayout.vue` |
| `router/` | **Bảng đường đi** — URL map tới màn hình | `/admin/pos` → `PosPage.vue` |
| `api/` | **Gọi backend** — tách riêng khỏi view | `banHangApi.js`, `sanPhamApi.js` |
| `styles/main.css` | **Giao diện chung** — màu, nút, bảng, POS | `.admin-card`, `.pos-layout` |
| `constants/` | **Cấu hình cố định** | `adminMenu.js` — menu sidebar |
| `composables/` | **Logic dùng chung** | `useConfirm.js` — hộp thoại xác nhận |
| `utils/` | **Hàm tiện ích** | `format.js` — format tiền, ngày |

### Quy tắc nhớ nhanh

- Muốn sửa **một trang** → vào `views/`
- Muốn sửa **menu trái** → `constants/adminMenu.js` + `router/index.js`
- Muốn sửa **màu / nút / card** → `styles/main.css`
- Muốn sửa **API gọi server** → `api/`
- Muốn sửa **hộp thoại xác nhận** → `composables/useConfirm.js` + `ConfirmDialog.vue`

---

## 3. CẤU TRÚC MỘT FILE .VUE

Mỗi màn hình Vue gồm **2 phần chính**:

### Phần A — `<script setup>` (Logic / Não)

Ví dụ từ `VariantList.vue`:

```js
// 1. Import thư viện và API
import { ref, computed, onMounted } from 'vue'
import { getProducts } from '@/api/sanPhamApi'

// 2. Khai báo biến dữ liệu
const loading = ref(false)      // Cờ: đang tải?
const products = ref([])        // Danh sách sản phẩm từ API

// 3. Biến tính toán tự động
const filteredVariants = computed(() => {
  // Tự lọc lại khi dữ liệu thay đổi
})

// 4. Hàm xử lý sự kiện
async function loadProducts() {
  loading.value = true
  const res = await getProducts()   // Gọi API
  products.value = res.data           // Gán data → UI tự cập nhật
  loading.value = false
}

// 5. Chạy khi vào trang lần đầu
onMounted(() => loadProducts())
```

**Giải thích các khái niệm:**

| Khái niệm | Ý nghĩa đơn giản |
|---|---|
| `ref()` | Hộp chứa dữ liệu. Đổi giá trị → màn hình tự render lại |
| `computed()` | Giá trị tự tính lại khi dữ liệu gốc thay đổi (lọc, tổng tiền...) |
| `reactive()` | Tương tự ref nhưng cho object nhiều field (ví dụ bộ lọc) |
| `onMounted()` | Chạy 1 lần khi mở trang (thường dùng để gọi API) |
| `watch()` | Theo dõi biến, khi đổi thì chạy hàm (ví dụ: gõ tìm kiếm → gọi API) |

### Phần B — `<template>` (Giao diện / Mặt)

```html
<div class="admin-card">
  <!-- Ô nhập gắn 2 chiều với biến keyword -->
  <input v-model="filter.keyword" class="admin-input" />

  <!-- Hiện khi đang tải -->
  <div v-if="loading">Đang tải...</div>

  <!-- Lặp danh sách -->
  <tr v-for="item in pagedVariants" :key="item.id">
    <td>{{ item.sku }}</td>
    <td>{{ formatCurrency(item.giaBan) }}</td>
    <td>
      <button @click="openEdit(item)">Sửa</button>
    </td>
  </tr>
</div>
```

**Cú pháp Vue thường gặp:**

| Cú pháp | Ý nghĩa |
|---|---|
| `v-model="keyword"` | Ô input ↔ biến `keyword` (gõ là biến đổi, biến đổi là ô đổi) |
| `v-for="item in list"` | Lặp qua danh sách, render nhiều dòng |
| `v-if="loading"` | Hiện/ẩn theo điều kiện |
| `v-else` | Nhánh còn lại của v-if |
| `@click="save"` | Bấm nút → gọi hàm `save()` |
| `{{ item.ten }}` | In giá trị biến ra màn hình |
| `:disabled="saving"` | Ràng buộc thuộc tính HTML với biến JS |

### Nguyên tắc cốt lõi Vue

> **Đổi data → UI tự đổi.** Không cần thao tác DOM thủ công như JavaScript thuần.

---

## 4. KHUNG ADMIN (AdminLayout)

File: `frontend/src/layouts/AdminLayout.vue`

### Cấu trúc trực quan

```
┌─────────────────┬────────────────────────────────┐
│                 │  AdminHeader (tiêu đề trang)   │
│  AdminSidebar   ├────────────────────────────────┤
│  (menu trái)    │                                │
│                 │  <router-view>                 │
│                 │  ← Màn hình thay đổi ở đây     │
│                 │                                │
└─────────────────┴────────────────────────────────┘
         + ConfirmDialog (hộp thoại xác nhận toàn cục)
```

### Cách hoạt động

- **Sidebar** đọc danh sách menu từ `constants/adminMenu.js`
- **Header** lấy tiêu đề từ `route.meta.title` (khai báo trong router)
- **router-view** là "ô trống" — router thay component bên trong khi đổi URL
- **ConfirmDialog** gắn 1 lần ở layout → mọi màn hình đều dùng được

### Ví dụ route trong `router/index.js`

```js
{
  path: '/admin',
  component: AdminLayout,
  children: [
    {
      path: 'pos',
      component: () => import('@/views/admin/pos/PosPage.vue'),
      meta: { title: 'Bán hàng tại quầy', breadcrumb: 'Bán hàng tại quầy' },
    },
    {
      path: 'products/variants',
      component: () => import('@/views/product/VariantList.vue'),
      meta: { title: 'Biến thể sản phẩm', breadcrumb: 'Biến thể' },
    },
  ],
}
```

Khi đổi từ `/admin/pos` sang `/admin/orders`: sidebar và header **giữ nguyên**, chỉ nội dung giữa **đổi**.

---

## 5. CÁCH GỌI API

### 3 tầng tách biệt

**Tầng 1 — `api/request.js`:** Cấu hình Axios chung

```js
const request = axios.create({
  baseURL: '/api',      // Mọi request đều có prefix /api
  timeout: 120000,
})
```

**Tầng 2 — `api/banHangApi.js`:** Mỗi chức năng một hàm

```js
import request from './request'

export function getSanPhamBan(keyword, page) {
  return request.get('/ban-hang/san-pham', { params: { keyword, page } })
}

export function taoDonTaiQuay(payload) {
  return request.post('/ban-hang/tai-quay', payload)
}
```

**Tầng 3 — Trong view (ví dụ `PosPage.vue`):**

```js
import { getSanPhamBan, taoDonTaiQuay } from '@/api/banHangApi'

// Tải sản phẩm
const res = await getSanPhamBan('', 0)
searchResults.value = res.data

// Thanh toán
const res = await taoDonTaiQuay(payload)
receipt.value = res.data
```

### Tại sao tách riêng file API?

- View chỉ lo **hiển thị và xử lý UI**
- Đổi URL backend → sửa **1 file API**, không phải sửa 10 màn hình
- Dễ đọc, dễ bảo trì, dễ demo cho thầy

### Xử lý lỗi

`request.js` có interceptor: nếu backend không chạy → báo lỗi tiếng Việt *"Không kết nối được backend..."*.

---

## 6. CÁCH LÀM GIAO DIỆN (CSS)

Project dùng **2 lớp CSS song song**:

### Lớp 1 — Class có sẵn trong `styles/main.css`

Đặt tên theo nghiệp vụ admin, dùng biến màu Soleil (nâu/kem):

```html
<div class="admin-card">
  <div class="admin-card-header">Tiêu đề</div>
  <input class="admin-input" />
  <select class="admin-select"></select>
  <button class="admin-btn admin-btn-primary">Lưu</button>
  <table class="admin-table">...</table>
</div>
```

Biến màu chính (trong `:root`):

| Biến | Màu | Dùng cho |
|---|---|---|
| `--cream` | #f9f5f0 | Nền trang |
| `--warm-tan` | #c9a96e | Màu chủ đạo / primary |
| `--ink` | #1e1510 | Chữ chính |
| `--admin-muted` | #8a7b6a | Chữ phụ |

### Lớp 2 — Tailwind CSS (class tiện lợi)

```html
<div class="flex justify-between gap-2 mb-4">
<p class="text-sm text-[var(--admin-muted)]">
```

Dùng cho layout nhanh: flexbox, margin, padding, responsive.

### Khi nào dùng gì?

| Tình huống | Cách làm |
|---|---|
| Nút, card, bảng chuẩn admin | Class trong `main.css` (`.admin-*`) |
| Căn chỉnh nhanh, khoảng cách | Tailwind (`flex`, `mb-4`, `gap-2`...) |
| Màn phức tạp (POS, hóa đơn) | Class riêng trong `main.css` (`.pos-*`, `.hoa-don-*`) |

---

## 7. COMPONENT — CHIA NHỎ MÀN HÌNH

Thay vì 1 file 2000 dòng, tách thành nhiều file nhỏ:

```
ProductList.vue              ← Màn chính (điều phối)
  ├── ProductFilter.vue      ← Khối bộ lọc
  ├── ProductTable.vue       ← Bảng danh sách
  └── ProductFormModal.vue   ← Popup thêm/sửa sản phẩm
```

### Phân vai

| Vai trò | Trách nhiệm |
|---|---|
| **View (màn chính)** | Tải data, điều hướng, mở/đóng modal, gọi API lưu/xóa |
| **Component con** | Hiển thị 1 phần UI, nhận `props` từ cha, bắn `emit` event lên cha |

### Ví dụ luồng component

1. `ProductList` gọi API lấy danh sách
2. Truyền `products` xuống `ProductTable` qua prop
3. User bấm "Sửa" trên bảng → `ProductTable` emit event `edit`
4. `ProductList` nhận event → mở `ProductFormModal` với data tương ứng

---

## 8. VÍ DỤ LUỒNG HOÀN CHỈNH: POS BÁN HÀNG

File: `frontend/src/views/admin/pos/PosPage.vue`

| Bước | Hành động | Code / Biến liên quan |
|---|---|---|
| 1 | Vào `/admin/pos` | Router load `PosPage` |
| 2 | `onMounted` chạy | `loadProducts()`, `loadMeta()`, `loadHeldOrders()` |
| 3 | Hiện lưới sản phẩm | `searchResults` — bind `v-for` |
| 4 | Click sản phẩm | `addToCart()` → `cart` thêm dòng |
| 5 | Chọn PTTT, nhập tiền | `selectedPaymentId`, `cashGiven` |
| 6 | Bấm Thanh toán | `confirm()` → `checkout()` → API `taoDonTaiQuay` |
| 7 | Thành công | `receipt` = data trả về, `showReceipt = true` |
| 8 | Modal biên lai hiện | Teleport ra `body`, nút In / Bán đơn mới |
| 9 | Bấm "Bán đơn mới" | `resetSale()` → xóa giỏ, gọi lại `loadProducts()` |

### Các biến quan trọng trong POS

| Biến | Ý nghĩa |
|---|---|
| `searchResults` | Danh sách sản phẩm hiển thị bên trái |
| `cart` | Giỏ hàng (cột phải) |
| `selectedCustomer` | Khách đang chọn (hoặc khách lẻ) |
| `selectedPaymentId` | Phương thức thanh toán |
| `cashGiven` | Tiền khách đưa (tiền mặt) |
| `receipt` | Dữ liệu biên lai sau thanh toán |
| `showReceipt` | Có hiện modal biên lai không |

---

## 9. SO SÁNH VỚI HTML THUẦN

| HTML / JS thuần | Vue trong project SUNOVA |
|---|---|
| Viết HTML tĩnh | HTML + `{{ biến }}` — nội dung động |
| `document.getElementById()` | `ref()` + `v-model` — tự đồng bộ |
| Fetch API rải rác trong file | Tập trung trong `api/*.js` |
| Copy header/sidebar mỗi trang | `AdminLayout` dùng chung 1 lần |
| CSS mỗi trang một kiểu | `main.css` class chuẩn toàn hệ thống |
| Tự cập nhật DOM sau fetch | Gán `data.value = res.data` → Vue tự render |

---

## 10. BẢNG TRA CỨU NHANH

### Muốn sửa gì → Vào file nào?

| Muốn sửa | File cần mở |
|---|---|
| Thêm mục menu sidebar | `constants/adminMenu.js` + `router/index.js` |
| Màn biến thể sản phẩm | `views/product/VariantList.vue` |
| Màn danh sách sản phẩm | `views/product/ProductList.vue` |
| Màn POS bán hàng | `views/admin/pos/PosPage.vue` |
| Màn danh sách hóa đơn | `views/admin/orders/OrderList.vue` |
| Chi tiết hóa đơn | `views/admin/orders/HoaDonDetailPage.vue` |
| Màu, nút, card, bảng | `styles/main.css` |
| API bán hàng | `api/banHangApi.js` |
| API sản phẩm | `api/sanPhamApi.js` |
| Hộp thoại xác nhận | `composables/useConfirm.js` + `components/ui/ConfirmDialog.vue` |
| Khung sidebar/header | `layouts/AdminLayout.vue` + `components/admin/` |

### 5 điểm cốt lõi cần nhớ

1. **Vue = Data + Template** — đổi data, UI tự đổi
2. **Router = URL → Màn hình**
3. **AdminLayout = Khung cố định**, `views/` = Nội dung thay đổi
4. **`api/` = Cầu nối với backend**
5. **`main.css` = Giao diện đồng bộ** toàn hệ thống

---

## 11. GIẢI THÍCH CHO THẦY VỀ PHẦN ADMIN (CHƯA PHÂN QUYỀN)

### Câu trả lời ngắn (30 giây khi demo)

> *"Em tách hệ thống thành 2 phần: **giao diện quản trị** (`/admin`) cho nhân viên/cửa hàng xử lý sản phẩm, bán hàng, hóa đơn; và **giao diện khách** (quiz chống nắng) cho người mua.*
>
> *Phần em demo hôm nay là **nghiệp vụ cửa hàng** nên em ưu tiên làm admin trước. **Phân quyền đăng nhập** (quản lý / nhân viên / khách hàng) em **chưa triển khai xong** — database đã có bảng `vai_tro`, nhưng em mới tập trung hoàn thiện luồng bán hàng, quản lý đơn, in hóa đơn. Đây là bước em làm tiếp theo."*

### Phân biệt "admin" vs "vai trò"

| Trong project | Ý nghĩa |
|---|---|
| `/admin` | **Khu vực quản trị / back-office** (POS, sản phẩm, hóa đơn...) |
| `QUAN_LY` / `NHAN_VIEN` | **Vai trò người dùng** trong database — chưa gắn vào UI |
| `KHACH_HANG` | Khách mua online — phần storefront (`/quiz`) |

### Đã chuẩn bị sẵn trong hệ thống

- Bảng `vai_tro` trong SQL: `QUAN_LY`, `NHAN_VIEN`, `KHACH_HANG`
- `nhan_vien` gắn `id_vai_tro` — POS chọn nhân viên khi bán
- Router tách `/admin` và `/quiz`
- `adminMenu.js` có `CURRENT_ROLE` và `roles` trên từng menu (sẵn sàng cho phân quyền)
- Khung JWT (`JwtAuthFilter`, `SecurityConfig`) — chưa bật đầy đủ

### Roadmap 3 bước (nói với thầy)

1. **Đã xong:** Nghiệp vụ cửa hàng — POS, hóa đơn, sản phẩm, khách nhanh
2. **Tiếp theo:** Login + JWT + phân quyền theo `vai_tro`
3. **Sau nữa:** Khách hàng đăng nhập, đặt hàng online

---

## PHỤ LỤC: LỆNH CHẠY DỰ ÁN

```powershell
# Backend
cd backend
.\start-backend.cmd

# Frontend
cd frontend
npm run dev

# Truy cập
http://localhost:5173/admin/pos      # Bán hàng
http://localhost:5173/admin/products # Sản phẩm
http://localhost:5173/admin/orders   # Hóa đơn
http://localhost:5173/quiz           # Quiz khách hàng
```

---

*Tài liệu này mô tả cách tổ chức và code giao diện frontend dự án SUNOVA, phục vụ demo và tự học.*
