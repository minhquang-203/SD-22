# HƯỚNG DẪN CODE FRONTEND — DỰ ÁN SUNOVA

**Dành cho:** Thành viên mới vào nhóm, cần biết *file nào làm gì*, *sửa ở đâu*, *luồng chạy ra sao*.  
**Công nghệ:** Vue 3 + Vue Router + Axios + Vite + Tailwind CSS v4 + CSS tùy chỉnh (theme Soleil)  
**Backend kèm theo:** Spring Boot — chạy port `8080`  
**Frontend dev:** Vite — chạy port `5173`  
**Cập nhật:** 03/06/2026

---

## MỤC LỤC

1. [Chạy dự án từ đầu (bắt buộc đọc trước)](#1-chạy-dự-án-từ-đầu-bắt-buộc-đọc-trước)
2. [App hoạt động như thế nào?](#2-app-hoạt-động-như-thế-nào)
3. [Bản đồ thư mục — từng file một](#3-bản-đồ-thư-mục--từng-file-một)
4. [Bảng URL đầy đủ — biết vào link nào ra màn nào](#4-bảng-url-đầy-đủ--biết-vào-link-nào-ra-màn-nào)
5. [Menu sidebar — cấu hình ở đâu](#5-menu-sidebar--cấu-hình-ở-đâu)
6. [Học Vue cơ bản trong 10 phút](#6-học-vue-cơ-bản-trong-10-phút)
7. [Cấu trúc 1 file .vue — đọc code không bị lạ](#7-cấu-trúc-1-file-vue--đọc-code-không-bị-lạ)
8. [Khung Admin (AdminLayout)](#8-khung-admin-adminlayout)
9. [Gọi API — 3 tầng, không gọi fetch rải rác](#9-gọi-api--3-tầng-không-gọi-fetch-rải-rác)
10. [CSS — dùng class nào, sửa màu ở đâu](#10-css--dùng-class-nào-sửa-màu-ở-đâu)
11. [Component — chia nhỏ màn hình thế nào](#11-component--chia-nhỏ-màn-hình-thế-nào)
12. [Từng module nghiệp vụ — chi tiết từng bước](#12-từng-module-nghiệp-vụ--chi-tiết-từng-bước)
13. [Công thức: Muốn làm X → mở file Y](#13-công-thức-muốn-làm-x--mở-file-y)
14. [Hướng dẫn thêm màn hình mới (từng bước)](#14-hướng-dẫn-thêm-màn-hình-mới-từng-bước)
15. [Làm việc nhóm với Git](#15-làm-việc-nhóm-với-git)
16. [Giải thích phần Admin chưa đăng nhập (demo với thầy)](#16-giải-thích-phần-admin-chưa-đăng-nhập-demo-với-thầy)
17. [Lỗi thường gặp & cách xử lý](#17-lỗi-thường-gặp--cách-xử-lý)

---

## 1. CHẠY DỰ ÁN TỪ ĐẦU (BẮT BUỘC ĐỌC TRƯỚC)

### Bước 1 — Chuẩn bị

- Cài **Node.js** (LTS) và **Java 17**
- Cài **SQL Server**, import database từ `backend/sql/SUNOVA_full.sql` hoặc `SUNOVA_full1.sql`
- Clone repo: `https://github.com/minhquang-203/SD-22.git`

### Bước 2 — Chạy Backend (bắt buộc chạy trước)

```powershell
cd backend
.\start-backend.cmd
```

Hoặc:

```powershell
cd backend
.\mvnw.cmd spring-boot:run
```

Kiểm tra: mở `http://localhost:8080` — không lỗi là được.

> **Lưu ý:** Mật khẩu DB cấu hình trong `backend/src/main/resources/application.properties` — **mỗi người có thể khác**, không commit password cá nhân lên Git.

### Bước 3 — Chạy Frontend

```powershell
cd frontend
npm install        # chỉ lần đầu
npm run dev
```

Mở trình duyệt: `http://localhost:5173`

### Bước 4 — Vào thử các màn chính

| URL | Màn hình |
|-----|----------|
| `http://localhost:5173/admin/products` | Danh sách sản phẩm |
| `http://localhost:5173/admin/pos` | Bán hàng tại quầy (POS) |
| `http://localhost:5173/admin/hoa-don` | Danh sách hóa đơn |
| `http://localhost:5173/admin/attributes/thuong-hieu` | Quản lý thương hiệu |
| `http://localhost:5173/quiz` | Quiz loại da (khách hàng) |

### Frontend gọi Backend thế nào?

File `frontend/vite.config.js` có **proxy**:

```
Trình duyệt gọi:  /api/san-pham
       ↓ (Vite chuyển tiếp)
Backend nhận:     http://localhost:8080/api/san-pham
```

Bạn **không cần** gõ `localhost:8080` trong code Vue — chỉ gọi `/api/...`.

---

## 2. APP HOẠT ĐỘNG NHƯ THẾ NÀO?

### Luồng tổng quát (đọc 1 lần là nhớ)

```
Người dùng gõ URL (vd: /admin/pos)
        ↓
router/index.js  →  chọn component nào hiển thị
        ↓
Nếu URL bắt đầu /admin  →  bọc trong AdminLayout (sidebar + header cố định)
        ↓
<router-view>  →  hiện màn hình (PosPage, ProductList, ...)
        ↓
Màn hình gọi api/*.js  →  backend trả JSON
        ↓
Gán JSON vào biến ref()  →  Vue tự vẽ lại giao diện
```

### Sơ đồ

```
main.js
  └── App.vue
        └── <router-view>
              ├── /quiz          → SkinQuiz.vue (giao diện khách)
              └── /admin/*       → AdminLayout.vue
                    ├── AdminSidebar   (menu trái)
                    ├── AdminHeader    (tiêu đề trang)
                    ├── <router-view> (nội dung thay đổi)
                    └── ConfirmDialog  (hộp thoại xác nhận dùng chung)
```

### File khởi động — `frontend/src/main.js`

```js
import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import './styles/main.css'

createApp(App).use(router).mount('#app')
```

**Ý nghĩa từng dòng:**
- `createApp(App)` — tạo ứng dụng Vue
- `.use(router)` — bật điều hướng theo URL
- `import './styles/main.css'` — nạp CSS chung toàn hệ thống
- `.mount('#app')` — gắn vào thẻ `<div id="app">` trong `index.html`

---

## 3. BẢN ĐỒ THƯ MỤC — TỪNG FILE MỘT

```
frontend/src/
│
├── main.js                 ← Điểm vào app
├── App.vue                 ← Vỏ ngoài, chỉ chứa <router-view>
│
├── router/
│   └── index.js            ← BẢNG ĐƯỜNG ĐI: URL → màn hình nào
│
├── layouts/
│   └── AdminLayout.vue     ← Khung admin: sidebar + header + router-view
│
├── views/                  ← MÀN HÌNH CHÍNH (mỗi trang 1 file)
│   ├── product/
│   │   ├── ProductList.vue     ← Danh sách sản phẩm
│   │   └── VariantList.vue     ← Biến thể + lô hàng của 1 sản phẩm
│   ├── attributes/
│   │   └── AttributeManager.vue ← 1 file dùng cho 6 loại thuộc tính
│   ├── admin/
│   │   ├── pos/PosPage.vue       ← Bán hàng tại quầy
│   │   ├── orders/               ← Hóa đơn
│   │   ├── voucher/              ← Phiếu giảm giá, đợt giảm giá
│   │   ├── recommendation/       ← Quiz admin, luật gợi ý, routine
│   │   └── ...                   ← Thống kê, báo cáo, UV, users...
│   └── storefront/
│       └── SkinQuiz.vue          ← Quiz cho khách (không có sidebar admin)
│
├── components/             ← KHỐI UI TÁI SỬ DỤNG
│   ├── admin/              ← Sidebar, Header, Switch...
│   ├── products/           ← Bảng SP, modal form, modal lô hàng...
│   ├── ui/                 ← ConfirmDialog, PageHeader, StatCard...
│   └── voucher/            ← Các khối voucher
│
├── api/                    ← GỌI BACKEND (tách riêng, không viết trong view)
│   ├── request.js          ← Cấu hình Axios chung
│   ├── sanPhamApi.js
│   ├── banHangApi.js
│   ├── hoaDonApi.js
│   ├── loHangApi.js
│   ├── thuocTinhApi.js
│   └── ...
│
├── constants/
│   ├── adminMenu.js        ← Cấu hình menu sidebar
│   └── attributeConfig.js  ← Cấu hình 6 màn thuộc tính (cột, field, API)
│
├── composables/
│   └── useConfirm.js       ← Hàm confirm() — hộp thoại "Bạn có chắc?"
│
├── utils/
│   ├── format.js           ← formatCurrency, formatDate...
│   ├── productForm.js      ← Chuyển form sản phẩm → FormData upload ảnh
│   └── apiError.js         ← Đọc lỗi từ backend
│
└── styles/
    └── main.css            ← CSS chung: màu, nút, bảng, POS, hóa đơn...
```

### Quy tắc vàng (nhớ 4 câu này)

| Muốn sửa... | Mở file... |
|-------------|------------|
| Một **trang / màn hình** | `views/...` |
| **Menu trái** | `constants/adminMenu.js` + `router/index.js` |
| **Màu, nút, card, bảng** | `styles/main.css` |
| **Gọi server / API** | `api/...` |

---

## 4. BẢNG URL ĐẦY ĐỦ — BIẾT VÀO LINK NÀO RA MÀN NÀO

Tất cả route khai báo trong `frontend/src/router/index.js`.

### Giao diện khách (không có sidebar admin)

| URL | File Vue | Chức năng |
|-----|----------|-----------|
| `/` | redirect → `/admin/products` | Trang chủ admin |
| `/quiz` | `views/storefront/SkinQuiz.vue` | Quiz tìm loại da (khách) |

### Giao diện admin (`/admin/...`)

| URL | File Vue | Chức năng |
|-----|----------|-----------|
| `/admin/stats` | `admin/stats/StatsDashboard.vue` | Thống kê |
| `/admin/pos` | `admin/pos/PosPage.vue` | **Bán hàng tại quầy (POS)** |
| `/admin/hoa-don` | `admin/orders/OrderList.vue` | **Danh sách hóa đơn** |
| `/admin/hoa-don/chi-tiet` | `admin/orders/HoaDonEmptyPage.vue` | Trang trống — hướng dẫn chọn HĐ |
| `/admin/hoa-don/chi-tiet/:id` | `admin/orders/HoaDonDetailPage.vue` | **Chi tiết 1 hóa đơn** |
| `/admin/products` | `product/ProductList.vue` | **Danh sách sản phẩm** |
| `/admin/san-pham/:id/bien-the` | `product/VariantList.vue` | **Biến thể + lô hàng** của SP `:id` |
| `/admin/attributes/:type` | `attributes/AttributeManager.vue` | Thuộc tính (`:type` = xem mục 12.2) |
| `/admin/reviews` | `admin/review/ReviewList.vue` | Đánh giá sản phẩm |
| `/admin/voucher` | `admin/voucher/VoucherView.vue` | Phiếu giảm giá |
| `/admin/sale` | `admin/voucher/SaleView.vue` | Đợt giảm giá |
| `/admin/recommendation/quiz` | `admin/recommendation/QuizManager.vue` | Quiz loại da (admin CRUD) |
| `/admin/recommendation/rules` | `admin/recommendation/RecommendRules.vue` | Luật gợi ý |
| `/admin/recommendation/routine` | `admin/recommendation/RoutineManager.vue` | Routine chống nắng |
| `/admin/uv` | `admin/uv/UVConfig.vue` | UV & thời tiết |
| `/admin/users` | `admin/users/UserList.vue` | Người dùng |
| `/admin/support` | `admin/support/SupportPage.vue` | Hỗ trợ KH |
| `/admin/reports` | `admin/report/ReportPage.vue` | Báo cáo |
| `/admin/config` | `admin/config/SystemConfig.vue` | Cấu hình hệ thống |

### URL redirect (cũ → mới)

| URL cũ | Chuyển đến |
|--------|------------|
| `/admin/orders` | `/admin/hoa-don` |
| `/admin/products/variants` | `/admin/products` |

> **Quan trọng:** Biến thể **không còn** menu riêng. Vào từ **Danh sách sản phẩm** → click dòng SP → mở `/admin/san-pham/{id}/bien-the`.

---

## 5. MENU SIDEBAR — CẤU HÌNH Ở ĐÂU

File: `frontend/src/constants/adminMenu.js`

### Cấu trúc menu hiện tại

```
Vận hành (Nhân viên)
├── Bán hàng tại quầy          → /admin/pos
├── Quản lý hóa đơn
│   ├── Hóa đơn                → /admin/hoa-don
│   └── Chi tiết hóa đơn       → /admin/hoa-don/chi-tiet
├── Quản lý sản phẩm
│   ├── Danh sách sản phẩm     → /admin/products
│   └── Thuộc tính
│       ├── Thương hiệu        → /admin/attributes/thuong-hieu
│       ├── Danh mục           → /admin/attributes/danh-muc
│       ├── Dạng sản phẩm      → /admin/attributes/dang-san-pham
│       ├── Màu sắc            → /admin/attributes/mau-sac
│       ├── Công dụng          → /admin/attributes/cong-dung
│       └── Thành phần         → /admin/attributes/thanh-phan
├── Quản lý đánh giá           → /admin/reviews
└── Hỗ trợ khách hàng          → /admin/support

Quản trị (Quản lý)
├── Thống kê, Báo cáo
├── Khuyến mãi & Voucher
├── Gợi ý & Cá nhân hóa (Quiz, Luật, Routine)
├── UV & Thời tiết
├── Quản lý người dùng
└── Cấu hình hệ thống
```

### Mỗi mục menu có các field

```js
{
  key: 'pos',                              // ID duy nhất
  label: 'Bán hàng tại quầy',              // Chữ hiển thị
  icon: 'icon-park-outline:shopping-cart', // Icon (Iconify)
  path: '/admin/pos',                      // URL khi click
  roles: ['QUAN_LY', 'NHAN_VIEN'],         // Ai được thấy (tạm thời)
}
```

### Phân quyền menu (tạm thời, chưa login)

- `CURRENT_ROLE` trong `adminMenu.js` — đổi `'QUAN_LY'` hoặc `'NHAN_VIEN'` để thử ẩn/hiện menu
- Hàm `filterMenuByRole()` lọc menu theo role
- **Chưa có login thật** — ẩn menu chỉ là UX, backend chưa chặn API

### Thêm 1 mục menu mới — 2 bước

**Bước 1:** Thêm vào `adminMenu.js`:

```js
{
  key: 'ten-moi',
  label: 'Tên hiển thị',
  icon: 'icon-park-outline:xxx',
  path: '/admin/ten-moi',
  roles: BOTH_ROLES,
}
```

**Bước 2:** Thêm route tương ứng trong `router/index.js` (xem mục 14).

---

## 6. HỌC VUE CƠ BẢN TRONG 10 PHÚT

Project dùng **Vue 3 Composition API** + `<script setup>`.

### 6.1. `ref()` — hộp chứa dữ liệu

```js
const loading = ref(false)    // boolean
const products = ref([])      // mảng
const keyword = ref('')       // chuỗi

// ĐỌC giá trị trong script:  loading.value
// ĐỌC trong template:        loading  (không cần .value)
loading.value = true
```

**Quy tắc:** Đổi `ref` → Vue **tự vẽ lại** phần HTML liên quan.

### 6.2. `computed()` — giá trị tự tính

```js
const filteredList = computed(() => {
  return products.value.filter(p => p.ten.includes(keyword.value))
})
```

Khi `products` hoặc `keyword` đổi → `filteredList` tự cập nhật.

### 6.3. `onMounted()` — chạy khi mở trang

```js
onMounted(() => {
  loadProducts()   // thường gọi API ở đây
})
```

### 6.4. Cú pháp template hay gặp

| Viết trong HTML | Nghĩa |
|-----------------|-------|
| `{{ product.ten }}` | In giá trị ra màn hình |
| `v-model="keyword"` | Ô input ↔ biến `keyword` (2 chiều) |
| `v-if="loading"` | Hiện khi `loading === true` |
| `v-else` | Nhánh còn lại của `v-if` |
| `v-for="item in list" :key="item.id"` | Lặp danh sách |
| `@click="save"` | Click → gọi hàm `save()` |
| `:disabled="saving"` | Gắn thuộc tính HTML với biến JS |
| `:class="{ active: isActive }"` | Class động |

### 6.5. Nguyên tắc cốt lõi

> **Đổi data → UI tự đổi.** Không cần `document.getElementById` như JavaScript thuần.

---

## 7. CẤU TRÚC 1 FILE .VUE — ĐỌC CODE KHÔNG BỊ LẠ

Mỗi file `.vue` gồm tối đa 3 phần:

```vue
<script setup>
// LOGIC: import, biến, hàm, gọi API
</script>

<template>
  <!-- GIAO DIỆN: HTML + cú pháp Vue -->
</template>

<style scoped>
/* CSS chỉ áp dụng cho file này (tùy chọn) */
</style>
```

### Thứ tự đọc code 1 màn hình (ví dụ ProductList.vue)

1. **Import** — biết màn này dùng API/component nào
2. **Khai báo biến** `ref`, `reactive` — biết dữ liệu gì
3. **`computed`** — biết lọc/phân trang thế nào
4. **Hàm async** — `loadProducts`, `handleSave`... — biết sự kiện làm gì
5. **`onMounted`** — biết chạy gì khi vào trang
6. **`<template>`** — giao diện bind với biến ở trên

### Ví dụ rút gọn

```js
// === SCRIPT ===
import { ref, onMounted } from 'vue'
import { getProducts } from '@/api/sanPhamApi'

const loading = ref(false)
const products = ref([])

async function loadProducts() {
  loading.value = true
  try {
    const res = await getProducts()
    products.value = res.data
  } finally {
    loading.value = false
  }
}

onMounted(() => loadProducts())
```

```html
<!-- === TEMPLATE === -->
<div v-if="loading">Đang tải...</div>
<table v-else>
  <tr v-for="p in products" :key="p.id">
    <td>{{ p.ten }}</td>
    <td><button @click="openEdit(p)">Sửa</button></td>
  </tr>
</table>
```

---

## 8. KHUNG ADMIN (AdminLayout)

File: `frontend/src/layouts/AdminLayout.vue`

```
┌──────────────────┬─────────────────────────────────────┐
│                  │  AdminHeader (tiêu đề + breadcrumb) │
│  AdminSidebar    ├─────────────────────────────────────┤
│  (menu trái)     │                                     │
│                  │  <router-view>  ← nội dung đổi ở đây│
│                  │                                     │
└──────────────────┴─────────────────────────────────────┘
              + ConfirmDialog (popup xác nhận toàn cục)
```

| Thành phần | File | Việc làm |
|------------|------|----------|
| Sidebar | `components/admin/AdminSidebar.vue` | Đọc `adminMenu.js`, highlight mục đang chọn |
| Header | `components/admin/AdminHeader.vue` | Lấy `route.meta.title` làm tiêu đề |
| Nội dung | `<router-view>` | Router thay component khi đổi URL |
| Confirm | `components/ui/ConfirmDialog.vue` | Dùng `confirm()` từ `useConfirm.js` |

**Khi đổi từ `/admin/pos` sang `/admin/products`:** sidebar và header **giữ nguyên**, chỉ khối giữa **đổi**.

---

## 9. GỌI API — 3 TẦNG, KHÔNG GỌI FETCH RẢI RÁC

### Tầng 1 — `api/request.js` (cấu hình chung)

```js
const request = axios.create({
  baseURL: '/api',      // mọi request có prefix /api
  timeout: 120000,
})
```

- Tự báo lỗi tiếng Việt nếu backend không chạy
- Hỗ trợ upload `FormData` (ảnh sản phẩm)

### Tầng 2 — File API theo nghiệp vụ

| File | Gọi API gì |
|------|------------|
| `sanPhamApi.js` | Sản phẩm, biến thể (chi tiết SP) |
| `loHangApi.js` | Lô hàng: xem danh sách, nhập lô |
| `banHangApi.js` | POS: tìm SP, tạo đơn, treo đơn... |
| `hoaDonApi.js` | Hóa đơn: danh sách, chi tiết |
| `thuocTinhApi.js` | 6 loại thuộc tính (CRUD) |
| `danhMucApi.js` | Helper lấy list dropdown (danh mục, màu...) |
| `khachHangApi.js` | Khách hàng |
| `voucherApi.js` | Voucher, đợt giảm giá |

Ví dụ `loHangApi.js`:

```js
export function getLoHangByChiTiet(idChiTietSanPham) {
  return request.get(`/lo-hang/chi-tiet-san-pham/${idChiTietSanPham}`)
}

export function nhapLoHang(payload) {
  return request.post('/lo-hang', payload)
}
```

### Tầng 3 — Trong View

```js
import { getProducts } from '@/api/sanPhamApi'

const res = await getProducts()
products.value = res.data    // res.data = JSON từ Spring Boot
```

### Tại sao tách 3 tầng?

- View chỉ lo **hiển thị**
- Đổi URL backend → sửa **1 file API**, không sửa 10 màn hình
- Dễ đọc, dễ chia việc trong nhóm

### Xử lý lỗi trong view

```js
try {
  await addProduct(formData)
  notify('Lưu thành công')
} catch (e) {
  notify(e, 'error')   // e đã là chuỗi tiếng Việt từ interceptor
}
```

---

## 10. CSS — DÙNG CLASS NÀO, SỬA MÀU Ở ĐÂU

Project dùng **2 lớp CSS song song**.

### Lớp 1 — Class trong `styles/main.css` (ưu tiên dùng)

Dùng cho giao diện admin thống nhất:

```html
<div class="admin-card">
  <div class="admin-card-header">Tiêu đề khối</div>
  <input class="admin-input" placeholder="Nhập..." />
  <select class="admin-select"></select>
  <button class="admin-btn admin-btn-primary">Lưu</button>
  <button class="admin-btn admin-btn-outline">Hủy</button>
  <table class="admin-table">...</table>
</div>
```

**Biến màu theme Soleil** (đầu file `main.css`):

| Biến CSS | Màu | Dùng cho |
|----------|-----|----------|
| `--cream` | #f9f5f0 | Nền trang |
| `--warm-tan` | #c9a96e | Màu chủ đạo / nút primary |
| `--ink` | #1e1510 | Chữ chính |
| `--admin-muted` | #8a7b6a | Chữ phụ |

**Class theo màn phức tạp:**
- POS: `.pos-layout`, `.pos-cart`, `.pos-receipt`...
- Hóa đơn: `.hoa-don-*`

### Lớp 2 — Tailwind CSS (căn layout nhanh)

```html
<div class="flex justify-between items-center gap-4 mb-4">
<p class="text-sm text-[var(--admin-muted)]">
```

### Khi nào dùng gì?

| Tình huống | Dùng |
|------------|------|
| Nút, card, bảng chuẩn admin | `.admin-*` trong `main.css` |
| Căn hàng, khoảng cách, flex | Tailwind (`flex`, `gap-2`, `mb-4`...) |
| Sửa màu toàn hệ thống | Đổi biến `:root` trong `main.css` |

---

## 11. COMPONENT — CHIA NHỎ MÀN HÌNH THẾ NÀO

### Mô hình View + Component con

```
ProductList.vue (VIEW — điều phối)
  ├── PageHeader.vue         ← Tiêu đề trang
  ├── StatCard.vue           ← Thẻ thống kê
  ├── ProductFilter.vue      ← Bộ lọc
  ├── ProductTable.vue       ← Bảng danh sách
  └── ProductFormModal.vue   ← Popup thêm/sửa SP
```

### Phân vai rõ ràng

| Vai trò | Làm gì | Không làm gì |
|---------|--------|--------------|
| **View** | Gọi API, giữ state, mở/đóng modal, điều hướng | Không nên chứa HTML quá dài |
| **Component con** | Hiển thị 1 khối UI, nhận `props`, bắn `emit` | Không tự gọi API phức tạp (trừ khi cần) |

### Luồng props / emit (ví dụ ProductTable)

```
ProductList                          ProductTable
    │                                     │
    │  :products="pagedProducts"  ──────► │ hiển thị bảng
    │  @row-click="handleManage"  ◄────── │ user click dòng
    │                                     │
    └── handleManage(id)                  │
        router.push(`/admin/san-pham/${id}/bien-the`)
```

**Trong component con:**

```js
const props = defineProps({ products: Array })
const emit = defineEmits(['row-click'])
// emit('row-click', product.id)
```

**Trong view cha:**

```html
<ProductTable :products="list" @row-click="handleManage" />
```

---

## 12. TỪNG MODULE NGHIỆP VỤ — CHI TIẾT TỪNG BƯỚC

### 12.1. Quản lý sản phẩm

**File chính:** `views/product/ProductList.vue`

**Luồng người dùng:**

```
Vào /admin/products
    → onMounted: loadProducts() + loadDropdownOptions()
    → Hiện bảng ProductTable
    → Click "Thêm mới" → mở ProductFormModal
    → Điền form + upload ảnh → Lưu → API addProduct (FormData)
    → Click 1 dòng SP → chuyển sang /admin/san-pham/{id}/bien-the
```

**Component liên quan:**

| File | Việc làm |
|------|----------|
| `ProductFilter.vue` | Lọc theo tên, danh mục, thương hiệu, SPF, PA |
| `ProductTable.vue` | Bảng SP — click dòng → quản lý biến thể |
| `ProductFormModal.vue` | Form thêm/sửa: tên, SPF, PA, loại chống nắng, ảnh... |
| `ProductSkuTable.vue` | Bảng biến thể trong modal (khi sửa SP) |
| `ProductImageManager.vue` | Upload/xóa ảnh SP |

**API:** `api/sanPhamApi.js`

**Lưu ý:** `ProductFormModal` có dropdown **Loại chống nắng**: `VAT_LY` / `HOA_HOC` / `LAI`.

---

### 12.2. Biến thể sản phẩm + Lô hàng

**File chính:** `views/product/VariantList.vue`

**URL:** `/admin/san-pham/:id/bien-the` — `:id` là ID sản phẩm.

**Luồng người dùng:**

```
Từ ProductList click 1 SP
    → VariantList đọc route.params.id
    → getProductDetail(id) → lấy danh sách chiTiets (biến thể)
    → Hiện bảng: SKU, màu, dung tích, giá, tồn, HSD gần nhất
    → "Thêm biến thể" → VariantModal → API addChiTiet
    → "Quản lý lô" → LoHangListModal (xem các lô)
    → "Nhập lô mới" → LoHangLotModal → API nhapLoHang
```

**Tại sao có lô hàng?**

- Mỗi biến thể (SKU) có thể nhập **nhiều lô** với HSD khác nhau
- Tồn kho = tổng `soLuongCon` các lô
- Khi bán (POS), backend trừ tồn theo **FEFO** (lô hết hạn sớm trước)

**Component lô hàng:**

| File | Việc làm |
|------|----------|
| `LoHangListModal.vue` | Danh sách lô của 1 biến thể |
| `LoHangLotModal.vue` | Form nhập lô mới (số lô, ngày nhập, HSD, SL) |
| `VariantModal.vue` | Thêm/sửa biến thể (màu, dung tích, giá, SKU) |

**API:**

- Biến thể: `sanPhamApi.js` → `addChiTiet`, `updateChiTiet`, `hideChiTiet`
- Lô hàng: `loHangApi.js` → `getLoHangByChiTiet`, `nhapLoHang`

---

### 12.3. Thuộc tính sản phẩm (6 loại — 1 file Vue)

**File chính:** `views/attributes/AttributeManager.vue`  
**Cấu hình:** `constants/attributeConfig.js`  
**API:** `api/thuocTinhApi.js`

**1 file Vue phục vụ 6 URL** nhờ param `:type`:

| URL | Loại | Mã tự sinh | Có toggle Ngưng? |
|-----|------|------------|------------------|
| `/admin/attributes/thuong-hieu` | Thương hiệu | TH | Có |
| `/admin/attributes/danh-muc` | Danh mục | DM | Có |
| `/admin/attributes/dang-san-pham` | Dạng SP | DSP | Không |
| `/admin/attributes/mau-sac` | Màu sắc | MS | Không |
| `/admin/attributes/cong-dung` | Công dụng | CD | Không |
| `/admin/attributes/thanh-phan` | Thành phần | TP | Không |

**Luồng:**

```
Vào /admin/attributes/thuong-hieu
    → AttributeManager đọc route.params.type
    → Lấy config từ ATTRIBUTE_TYPES['thuong-hieu']
    → Gọi API tương ứng (thuongHieuApi.getAll...)
    → Hiện bảng theo config.columns
    → Thêm mới: field "Mã" read-only (autoGenerated: true)
    → Backend tự sinh mã khi lưu (MaGenerator.java)
```

**Muốn thêm loại thuộc tính mới:**

1. Thêm API trong `thuocTinhApi.js`
2. Thêm config trong `attributeConfig.js`
3. Thêm route + menu trong `router/index.js` và `adminMenu.js`

---

### 12.4. POS — Bán hàng tại quầy

**File chính:** `views/admin/pos/PosPage.vue`  
**API:** `api/banHangApi.js`

**Luồng từng bước:**

| Bước | Hành động | Biến / Hàm |
|------|-----------|------------|
| 1 | Vào `/admin/pos` | Router load PosPage |
| 2 | Tải dữ liệu ban đầu | `onMounted` → `loadProducts()`, `loadMeta()`, `loadHeldOrders()` |
| 3 | Hiện lưới SP bên trái | `searchResults` + `v-for` |
| 4 | Gõ tìm kiếm | `keyword` → gọi lại API tìm SP |
| 5 | Click SP | `addToCart()` → thêm vào `cart` |
| 6 | Chọn khách, PTTT | `selectedCustomer`, `selectedPaymentId` |
| 7 | Nhập tiền mặt | `cashGiven` |
| 8 | Bấm Thanh toán | `checkout()` → API `taoDonTaiQuay` |
| 9 | Thành công | `receipt` + `showReceipt = true` → modal biên lai |
| 10 | In biên lai | CSS class `printing-receipt` trên `body` |
| 11 | "Bán đơn mới" | `resetSale()` → xóa giỏ + **gọi lại `loadProducts()`** |

**Biến quan trọng:**

| Biến | Ý nghĩa |
|------|---------|
| `searchResults` | SP hiển thị bên trái |
| `cart` | Giỏ hàng (cột phải) |
| `selectedCustomer` | Khách đang chọn |
| `selectedPaymentId` | Phương thức thanh toán |
| `cashGiven` | Tiền khách đưa |
| `receipt` | Dữ liệu biên lai sau thanh toán |
| `heldOrders` | Đơn đang treo |

---

### 12.5. Hóa đơn

**Files:**

| File | URL | Việc làm |
|------|-----|----------|
| `OrderList.vue` | `/admin/hoa-don` | Danh sách HĐ — lọc, phân trang |
| `HoaDonEmptyPage.vue` | `/admin/hoa-don/chi-tiet` | Trang trống — chưa chọn HĐ |
| `HoaDonDetailPage.vue` | `/admin/hoa-don/chi-tiet/:id` | Chi tiết 1 HĐ — SP, thanh toán, lịch sử |

**API:** `api/hoaDonApi.js`

**Luồng xem chi tiết:**

```
/admin/hoa-don → danh sách
    → Click 1 hóa đơn
    → /admin/hoa-don/chi-tiet/123
    → HoaDonDetailPage đọc route.params.id
    → Gọi API lấy chi tiết → hiển thị
```

---

### 12.6. Quiz loại da

**2 màn tách biệt:**

| Màn | File | Đối tượng |
|-----|------|-----------|
| Khách làm quiz | `storefront/SkinQuiz.vue` | URL `/quiz` |
| Admin CRUD câu hỏi | `admin/recommendation/QuizManager.vue` | URL `/admin/recommendation/quiz` |

Admin Quiz gọi trực tiếp `http://localhost:8080/api/admin/quizzes` (có thể chuẩn hóa sang file `api/` sau).

---

### 12.7. Voucher & Đợt giảm giá

| File | URL |
|------|-----|
| `VoucherView.vue` | `/admin/voucher` |
| `SaleView.vue` | `/admin/sale` |

**API:** `api/voucherApi.js`  
**Component:** `components/voucher/*`

---

## 13. CÔNG THỨC: MUỐN LÀM X → MỞ FILE Y

| Muốn làm | Mở file |
|----------|---------|
| Thêm/sửa mục menu sidebar | `constants/adminMenu.js` + `router/index.js` |
| Đổi tiêu đề trang admin | `meta.title` trong `router/index.js` |
| Sửa màn danh sách sản phẩm | `views/product/ProductList.vue` |
| Sửa bảng sản phẩm (cột, click dòng) | `components/products/ProductTable.vue` |
| Sửa form thêm/sửa sản phẩm | `components/products/ProductFormModal.vue` |
| Sửa màn biến thể + lô hàng | `views/product/VariantList.vue` |
| Sửa popup nhập lô | `components/products/LoHangLotModal.vue` |
| Sửa màn thuộc tính (chung) | `views/attributes/AttributeManager.vue` |
| Sửa cột/field 1 loại thuộc tính | `constants/attributeConfig.js` |
| Sửa POS bán hàng | `views/admin/pos/PosPage.vue` |
| Sửa in biên lai POS | `styles/main.css` (tìm `.pos-receipt`, `printing-receipt`) |
| Sửa danh sách hóa đơn | `views/admin/orders/OrderList.vue` |
| Sửa chi tiết hóa đơn | `views/admin/orders/HoaDonDetailPage.vue` |
| Sửa API sản phẩm | `api/sanPhamApi.js` |
| Sửa API lô hàng | `api/loHangApi.js` |
| Sửa API bán hàng | `api/banHangApi.js` |
| Sửa màu/nút/card toàn hệ thống | `styles/main.css` |
| Sửa hộp thoại "Bạn có chắc?" | `composables/useConfirm.js` + `components/ui/ConfirmDialog.vue` |
| Sửa sidebar/header | `components/admin/AdminSidebar.vue`, `AdminHeader.vue` |
| Sửa proxy backend | `vite.config.js` |

---

## 14. HƯỚNG DẪN THÊM MÀN HÌNH MỚI (TỪNG BƯỚC)

**Ví dụ:** Thêm màn "Quản lý nhà cung cấp" tại `/admin/nha-cung-cap`

### Bước 1 — Tạo file View

Tạo `frontend/src/views/admin/supplier/SupplierList.vue`:

```vue
<script setup>
import { ref, onMounted } from 'vue'
// import API khi có

const loading = ref(false)
const items = ref([])

onMounted(() => {
  // loadData()
})
</script>

<template>
  <div class="admin-card">
    <h2>Quản lý nhà cung cấp</h2>
    <div v-if="loading">Đang tải...</div>
    <div v-else><!-- bảng dữ liệu --></div>
  </div>
</template>
```

### Bước 2 — Thêm route

Trong `router/index.js`, trong `children` của `/admin`:

```js
{
  path: 'nha-cung-cap',
  name: 'AdminSupplier',
  component: () => import('@/views/admin/supplier/SupplierList.vue'),
  meta: { title: 'Nhà cung cấp', breadcrumb: 'Nhà cung cấp' },
},
```

### Bước 3 — Thêm menu

Trong `constants/adminMenu.js`, thêm vào nhóm phù hợp:

```js
{
  key: 'supplier',
  label: 'Nhà cung cấp',
  icon: 'icon-park-outline:factory',
  path: '/admin/nha-cung-cap',
  roles: BOTH_ROLES,
},
```

### Bước 4 — Tạo file API (khi có backend)

Tạo `api/nhaCungCapApi.js`:

```js
import request from './request'

export const getNhaCungCapList = () => request.get('/nha-cung-cap')
```

### Bước 5 — Kiểm tra

1. `npm run dev`
2. Mở `http://localhost:5173/admin/nha-cung-cap`
3. Kiểm tra sidebar có mục mới, header hiện đúng tiêu đề

---

## 15. LÀM VIỆC NHÓM VỚI GIT

Repo: `https://github.com/minhquang-203/SD-22.git` — nhánh chính: `main`

### Quy trình an toàn (không đè code người khác)

```powershell
# 1. Lấy code mới nhất TRƯỚC khi làm việc
git pull origin main

# 2. Tạo nhánh riêng cho tính năng của bạn
git checkout -b feat/ten-tinh-nang

# 3. Code xong → commit
git add .
git commit -m "Mo ta ngan gon tinh nang"

# 4. Push nhánh (KHÔNG push thẳng main nếu chưa chắc)
git push -u origin feat/ten-tinh-nang

# 5. Báo nhóm review, merge vào main trên GitHub
```

### Không nên commit

- `application.properties` với password DB cá nhân
- File `.env` chứa secret
- Thư mục `node_modules/`, `target/`

### Khi bị conflict (2 người sửa cùng file)

```powershell
git pull origin main
# Git báo conflict → mở file, tìm <<<<<<< ======= >>>>>>>
# Chọn giữ phần nào, xóa marker conflict
git add .
git commit -m "Giai quyet conflict"
```

---

## 16. GIẢI THÍCH PHẦN ADMIN CHƯA ĐĂNG NHẬP (DEMO VỚI THẦY)

### Câu trả lời ngắn (30 giây)

> *"Em tách hệ thống thành 2 phần: **giao diện quản trị** (`/admin`) cho nhân viên/cửa hàng; và **giao diện khách** (`/quiz`) cho người mua.*
>
> *Phần em demo là **nghiệp vụ cửa hàng**: POS, sản phẩm, hóa đơn, lô hàng. **Đăng nhập phân quyền** em chưa làm xong — database đã có bảng `vai_tro`, menu đã chuẩn bị field `roles`, nhưng chưa gắn JWT/login. Đây là bước tiếp theo."*

### Phân biệt

| Thuật ngữ | Ý nghĩa trong project |
|-----------|----------------------|
| `/admin` | Khu vực quản trị (back-office) |
| `QUAN_LY` / `NHAN_VIEN` | Vai trò trong DB — menu đã chuẩn bị, chưa enforce |
| `KHACH_HANG` | Khách mua — storefront `/quiz` |

### Đã chuẩn bị sẵn

- Bảng `vai_tro` trong SQL
- `adminMenu.js`: `CURRENT_ROLE`, `filterMenuByRole()`
- POS chọn nhân viên khi bán
- Backend có khung Security/JWT — chưa bật đầy đủ

---

## 17. LỖI THƯỜNG GẶP & CÁCH XỬ LÝ

| Triệu chứng | Nguyên nhân | Cách sửa |
|-------------|-------------|----------|
| "Không kết nối được backend" | Backend chưa chạy | Chạy `.\start-backend.cmd` trong `backend/` |
| Trang trắng, lỗi console import | Sai đường dẫn `@/...` | Kiểm tra file tồn tại, đúng tên hoa/thường |
| API 404 | Sai URL hoặc backend chưa có endpoint | Đối chiếu `api/*.js` với Controller Java |
| Backend lỗi Schema-validation | DB lệch với entity Java | Chạy SQL mới / `ALTER TABLE` cho đúng schema |
| Ảnh SP không hiện | Proxy `/uploads` | Kiểm tra `vite.config.js` proxy `/uploads` → 8080 |
| Sau bán POS, danh sách SP trống | Chưa reload | `resetSale()` phải gọi `loadProducts()` |
| Menu không hiện mục mới | Thiếu route hoặc sai `roles` | Kiểm tra cả `adminMenu.js` và `router/index.js` |

### Backend schema cần có (quan trọng)

- Bảng `lo_hang` — quản lý lô hàng
- Cột `dap_an_quiz.icon` — quiz admin
- Bảng `cong_dung`, `mau_sac`... **không có** `trang_thai` (chỉ `danh_muc`, `thuong_hieu` có)

---

## PHỤ LỤC — DANH SÁCH API FRONTEND

| File API | Endpoint chính (prefix `/api`) |
|----------|-------------------------------|
| `sanPhamApi.js` | `/san-pham`, `/chi-tiet-san-pham` |
| `loHangApi.js` | `/lo-hang` |
| `banHangApi.js` | `/ban-hang` |
| `hoaDonApi.js` | `/hoa-don` |
| `thuocTinhApi.js` | `/thuong-hieu`, `/danh-muc`, `/mau-sac`... |
| `khachHangApi.js` | `/khach-hang` |
| `voucherApi.js` | `/phieu-giam-gia`, `/dot-giam-gia` |

---

## PHỤ LỤC — CHECKLIST CHO NGƯỜI MỚI

- [ ] Clone repo và chạy được backend + frontend
- [ ] Mở được `/admin/products`, `/admin/pos`, `/admin/hoa-don`
- [ ] Đọc xong mục 3 (bản đồ thư mục) và mục 4 (bảng URL)
- [ ] Hiểu `ref`, `v-model`, `v-for`, `@click`
- [ ] Biết sửa menu → `adminMenu.js` + `router/index.js`
- [ ] Biết gọi API qua `api/*.js`, không fetch trực tiếp trong view
- [ ] Biết `git pull` trước khi code, commit trên nhánh riêng

---

*Tài liệu mô tả frontend SUNOVA theo code thực tế trên nhánh `main`. Khi thêm tính năng mới, nhớ cập nhật mục 4 (bảng URL) và mục 13 (công thức tra cứu).*
