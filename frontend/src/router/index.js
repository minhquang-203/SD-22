import { createRouter, createWebHistory } from 'vue-router'
import { isCustomerLoggedIn } from '@/composables/useAuth'
import { useAuthModal } from '@/composables/useAuthModal'
import { INFO_PAGES } from '@/constants/storefrontInfo'
import AdminLayout from '@/layouts/AdminLayout.vue'
import StorefrontLayout from '@/layouts/StorefrontLayout.vue'

const infoRoutes = Object.keys(INFO_PAGES).map((slug) => ({
  path: slug,
  name: `Info_${slug}`,
  component: () => import('@/views/storefront/InfoPage.vue'),
  meta: { title: INFO_PAGES[slug].metaTitle },
}))

const router = createRouter({
  history: createWebHistory(),
  routes: [
    // =======================================================
    // STOREFRONT (khách hàng) — Soleil + Bootstrap
    // =======================================================
    {
      path: '/',
      component: StorefrontLayout,
      children: [
        {
          path: '',
          name: 'Home',
          component: () => import('@/views/storefront/Home.vue'),
          meta: { title: 'SUNOVA — Chống nắng & chăm sóc da' },
        },
        {
          path: 'dang-nhap',
          beforeEnter: (to) => {
            const { openAuthModal } = useAuthModal()
            openAuthModal('login', typeof to.query.redirect === 'string' ? to.query.redirect : null)
            return '/'
          },
        },
        {
          path: 'dang-ky',
          beforeEnter: (to) => {
            const { openAuthModal } = useAuthModal()
            openAuthModal('register', typeof to.query.redirect === 'string' ? to.query.redirect : null)
            return '/'
          },
        },
        {
          path: 'san-pham',
          name: 'SanPhamList',
          component: () => import('@/views/storefront/ProductList.vue'),
          meta: { title: 'Sản phẩm — SUNOVA' },
        },
        {
          path: 'san-pham/:id',
          name: 'ProductDetail',
          component: () => import('@/views/storefront/ProductDetail.vue'),
          meta: { title: 'Chi tiết sản phẩm — SUNOVA' },
        },
        {
          path: 'gio-hang',
          name: 'GioHang',
          component: () => import('@/views/storefront/GioHang.vue'),
          meta: { title: 'Giỏ hàng — SUNOVA' },
        },
        {
          path: 'dat-hang',
          name: 'DatHang',
          component: () => import('@/views/storefront/DatHang.vue'),
          meta: { title: 'Đặt hàng — SUNOVA', requiresAuth: true },
        },
        {
          path: 'tai-khoan',
          name: 'TaiKhoan',
          component: () => import('@/views/storefront/TaiKhoan.vue'),
          meta: { title: 'Tài khoản — SUNOVA', requiresAuth: true },
        },
        {
          path: 'don-hang',
          name: 'DonHang',
          component: () => import('@/views/storefront/DonHang.vue'),
          meta: { title: 'Đơn hàng — SUNOVA', requiresAuth: true },
        },
        ...infoRoutes,
      ],
    },

    {
      path: '/quiz',
      name: 'QuizPlaceholder',
      component: () => import('@/views/storefront/QuizPlaceholder.vue'),
      meta: { title: 'Quiz da — SUNOVA' },
    },

    // =======================================================
    // ADMIN
    // =======================================================
    {
      path: '/admin',
      component: AdminLayout,
      children: [
        {
          path: 'stats',
          name: 'AdminStats',
          component: () => import('@/views/admin/stats/StatsDashboard.vue'),
          meta: { title: 'Thống kê', breadcrumb: 'Thống kê' },
        },
        {
          path: 'pos',
          name: 'AdminPos',
          component: () => import('@/views/admin/pos/PosPage.vue'),
          meta: { title: 'Bán hàng tại quầy', breadcrumb: 'Bán hàng tại quầy' },
        },
        {
          path: 'orders',
          redirect: '/admin/hoa-don',
        },
        {
          path: 'hoa-don',
          name: 'AdminHoaDonList',
          component: () => import('@/views/admin/orders/OrderList.vue'),
          meta: { title: 'Hóa đơn', breadcrumb: 'Quản lý hóa đơn / Hóa đơn' },
        },
        {
          path: 'hoa-don/chi-tiet',
          name: 'AdminHoaDonEmpty',
          component: () => import('@/views/admin/orders/HoaDonEmptyPage.vue'),
          meta: { title: 'Chi tiết hóa đơn', breadcrumb: 'Quản lý hóa đơn / Chi tiết' },
        },
        {
          path: 'hoa-don/chi-tiet/:id',
          name: 'AdminHoaDonDetail',
          component: () => import('@/views/admin/orders/HoaDonDetailPage.vue'),
          meta: { title: 'Chi tiết hóa đơn', breadcrumb: 'Quản lý hóa đơn / Chi tiết' },
        },
        {
          path: 'products',
          name: 'AdminProducts',
          component: () => import('@/views/product/ProductList.vue'),
          meta: { title: 'Danh sách sản phẩm', breadcrumb: 'Quản lý sản phẩm / Danh sách' },
        },
        {
          path: 'products/variants',
          redirect: '/admin/products',
        },
        {
          path: 'san-pham/:id/bien-the',
          name: 'AdminProductVariants',
          component: () => import('@/views/product/VariantList.vue'),
          meta: { title: 'Biến thể sản phẩm', breadcrumb: 'Quản lý sản phẩm / Biến thể' },
        },
        {
          path: 'attributes/:type',
          name: 'AdminAttributes',
          component: () => import('@/views/attributes/AttributeManager.vue'),
          meta: { title: 'Thuộc tính', breadcrumb: 'Thuộc tính' },
        },
        {
          path: 'reviews',
          name: 'AdminReviews',
          component: () => import('@/views/admin/review/ReviewList.vue'),
          meta: { title: 'Quản lý đánh giá', breadcrumb: 'Quản lý đánh giá' },
        },
        {
          path: 'voucher',
          name: 'AdminVoucher',
          component: () => import('@/views/admin/voucher/VoucherView.vue'),
          meta: { title: 'Phiếu giảm giá', breadcrumb: 'Phiếu giảm giá' },
        },
        {
          path: 'sale',
          name: 'AdminSale',
          component: () => import('@/views/admin/voucher/SaleView.vue'),
          meta: { title: 'Đợt giảm giá', breadcrumb: 'Đợt giảm giá' },
        },
        {
          path: 'sale/:id',
          name: 'AdminSaleDetail',
          component: () => import('@/views/admin/voucher/saleDetail.vue'),
          meta: { title: 'Chi tiết đợt giảm giá', breadcrumb: 'Đợt giảm giá / Chi tiết' },
        },
        {
          path: 'recommendation/quiz',
          name: 'AdminQuiz',
          component: () => import('@/views/admin/recommendation/QuizManager.vue'),
          meta: { title: 'Quiz loại da', breadcrumb: 'Gợi ý & Cá nhân hóa / Quiz loại da' },
        },
        {
          path: 'recommendation/rules',
          name: 'AdminRecommendRules',
          component: () => import('@/views/admin/recommendation/RecommendRules.vue'),
          meta: { title: 'Luật gợi ý', breadcrumb: 'Gợi ý & Cá nhân hóa / Luật gợi ý' },
        },
        {
          path: 'recommendation/routine',
          name: 'AdminRoutine',
          component: () => import('@/views/admin/recommendation/RoutineManager.vue'),
          meta: { title: 'Routine chống nắng', breadcrumb: 'Gợi ý & Cá nhân hóa / Routine chống nắng' },
        },
        {
          path: 'uv',
          name: 'AdminUv',
          component: () => import('@/views/admin/uv/UVConfig.vue'),
          meta: { title: 'UV & Thời tiết', breadcrumb: 'UV & Thời tiết' },
        },
        {
          path: 'users',
          name: 'AdminUsers',
          component: () => import('@/views/admin/users/UserList.vue'),
          meta: { title: 'Quản lý người dùng', breadcrumb: 'Quản lý người dùng' },
        },
        {
          path: 'support',
          name: 'AdminSupport',
          component: () => import('@/views/admin/support/SupportPage.vue'),
          meta: { title: 'Hỗ trợ khách hàng', breadcrumb: 'Hỗ trợ khách hàng' },
        },
        {
          path: 'reports',
          name: 'AdminReports',
          component: () => import('@/views/admin/report/ReportPage.vue'),
          meta: { title: 'Báo cáo', breadcrumb: 'Báo cáo' },
        },
        {
          path: 'config',
          name: 'AdminConfig',
          component: () => import('@/views/admin/config/SystemConfig.vue'),
          meta: { title: 'Cấu hình hệ thống', breadcrumb: 'Cấu hình hệ thống' },
        },
      ],
    },
  ],
})

router.beforeEach((to) => {
  if (to.meta.requiresAuth && !isCustomerLoggedIn()) {
    const { openAuthModal } = useAuthModal()
    openAuthModal('login', to.fullPath)
    return false
  }

  if (to.meta.guestOnly && isCustomerLoggedIn()) {
    return { path: '/' }
  }

  if (to.meta.title) {
    document.title = to.meta.title
  }

  return true
})

export default router
