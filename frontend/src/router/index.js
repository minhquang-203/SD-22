import { createRouter, createWebHistory } from 'vue-router'
import { isCustomerLoggedIn } from '@/composables/useAuth'
import { useAuthModal } from '@/composables/useAuthModal'
import { isAdminLoggedIn, useAdminAuth } from '@/composables/useAdminAuth'
import { ADMIN_ROLES } from '@/constants/adminMenu'
import { isManagerOrOwner } from '@/utils/adminAuth'
import { getAdminHomePath } from '@/utils/adminAuth'
import { toast } from '@/composables/useToast'
import { INFO_PAGES } from '@/constants/storefrontInfo'
import AdminLayout from '@/layouts/AdminLayout.vue'
import StorefrontLayout from '@/layouts/StorefrontLayout.vue'
import BlogView from '@/views/BlogView.vue'
import BlogDetailView from '@/views/BlogDetailView.vue'

const infoRoutes = Object.keys(INFO_PAGES).map((slug) => ({
  path: slug,
  name: `Info_${slug}`,
  component: () => import('@/views/storefront/InfoPage.vue'),
  meta: { title: INFO_PAGES[slug].metaTitle },
}))

const router = createRouter({
  history: createWebHistory(),
  scrollBehavior(to, from, savedPosition) {
    if (savedPosition) return savedPosition
    if (to.hash) return { el: to.hash, behavior: 'smooth' }
    return { top: 0, left: 0 }
  },
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
          path: 'san-pham/khuyen-mai',
          name: 'SanPhamKhuyenMai',
          component: () => import('@/views/storefront/ProductList.vue'),
          meta: { title: 'Khuyến mãi — SUNOVA', khuyenMai: true },
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
          meta: { title: 'Đơn hàng của tôi — SUNOVA', requiresAuth: true },
        },
        {
          path: 'tra-cuu-don',
          name: 'TraCuuDon',
          component: () => import('@/views/storefront/TraCuuDon.vue'),
          meta: { title: 'Tra cứu đơn hàng — SUNOVA', requiresAuth: true },
        },
        ...infoRoutes,
        {
          path: 'quiz',
          name: 'QuizPlaceholder',
          component: () => import('@/views/storefront/QuizPlaceholder.vue'),
          meta: { title: 'Quiz da — SUNOVA' },
        },
          {
    path: '/blog',
    name: 'blog',
    component: BlogView,
  },
  {
    path: '/blog/:slug',
    name: 'blog-detail',
    component: BlogDetailView,
  },
      ],
    },

    // =======================================================
    // ADMIN
    // =======================================================
    {
      path: '/admin/dang-nhap',
      name: 'AdminLogin',
      component: () => import('@/views/admin/AdminLogin.vue'),
      meta: { title: 'Đăng nhập quản trị — SUNOVA', guestOnlyAdmin: true },
    },
    {
      path: '/admin',
      component: AdminLayout,
      meta: { requiresAdmin: true },
      children: [
        {
          path: '',
          name: 'AdminRoot',
          redirect: () => getAdminHomePath(useAdminAuth().vaiTro.value),
        },
        {
          path: 'stats',
          name: 'AdminStats',
          component: () => import('@/views/admin/stats/StatsDashboard.vue'),
          meta: { title: 'Thống kê', breadcrumb: 'Thống kê', role: ADMIN_ROLES.QUAN_LY },
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
          meta: { title: 'Quản lý đánh giá', breadcrumb: 'Quản lý đánh giá', role: ADMIN_ROLES.QUAN_LY },
        },
        {
          path: 'voucher',
          name: 'AdminVoucher',
          component: () => import('@/views/admin/voucher/VoucherView.vue'),
          meta: { title: 'Phiếu giảm giá', breadcrumb: 'Phiếu giảm giá', role: ADMIN_ROLES.QUAN_LY },
        },
        {
          path: 'sale',
          name: 'AdminSale',
          component: () => import('@/views/admin/voucher/SaleView.vue'),
          meta: { title: 'Đợt giảm giá', breadcrumb: 'Đợt giảm giá', role: ADMIN_ROLES.QUAN_LY },
        },
        {
          path: 'sale/:id',
          name: 'AdminSaleDetail',
          component: () => import('@/views/admin/voucher/saleDetail.vue'),
          meta: { title: 'Chi tiết đợt giảm giá', breadcrumb: 'Đợt giảm giá / Chi tiết', role: ADMIN_ROLES.QUAN_LY },
        },
        {
          path: 'recommendation/quiz',
          name: 'AdminQuiz',
          component: () => import('@/views/admin/recommendation/QuizManager.vue'),
          meta: { title: 'Quiz loại da', breadcrumb: 'Gợi ý & Cá nhân hóa / Quiz loại da', role: ADMIN_ROLES.QUAN_LY },
        },
        {
          path: 'recommendation/rules',
          name: 'AdminRecommendRules',
          component: () => import('@/views/admin/recommendation/RecommendRules.vue'),
          meta: { title: 'Luật gợi ý', breadcrumb: 'Gợi ý & Cá nhân hóa / Luật gợi ý', role: ADMIN_ROLES.QUAN_LY },
        },
        {
          path: 'recommendation/routine',
          name: 'AdminRoutine',
          component: () => import('@/views/admin/recommendation/RoutineManager.vue'),
          meta: { title: 'Routine chống nắng', breadcrumb: 'Gợi ý & Cá nhân hóa / Routine chống nắng', role: ADMIN_ROLES.QUAN_LY },
        },
        {
          path: 'uv',
          name: 'AdminUv',
          component: () => import('@/views/admin/uv/UVConfig.vue'),
          meta: { title: 'UV & Thời tiết', breadcrumb: 'UV & Thời tiết', role: ADMIN_ROLES.QUAN_LY },
        },
        {
          path: 'users',
          name: 'AdminUsers',
          component: () => import('@/views/admin/users/UserList.vue'),
          meta: { title: 'Quản lý người dùng', breadcrumb: 'Quản lý người dùng', role: ADMIN_ROLES.QUAN_LY },
        },
        {
          path: 'staff',
          name: 'AdminStaff',
          component: () => import('@/views/admin/staff/StaffList.vue'),
          meta: { title: 'Quản lý nhân viên', breadcrumb: 'Quản lý nhân viên', role: ADMIN_ROLES.QUAN_LY },
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
          meta: { title: 'Báo cáo', breadcrumb: 'Báo cáo', role: ADMIN_ROLES.QUAN_LY },
        },
        {
          path: 'config',
          name: 'AdminConfig',
          component: () => import('@/views/admin/config/SystemConfig.vue'),
          meta: { title: 'Cấu hình hệ thống', breadcrumb: 'Cấu hình hệ thống', role: ADMIN_ROLES.QUAN_LY },
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

  if (to.meta.guestOnlyAdmin && isAdminLoggedIn()) {
    return { path: getAdminHomePath(useAdminAuth().vaiTro.value) }
  }

  if (to.matched.some((record) => record.meta.requiresAdmin) && !isAdminLoggedIn()) {
    return {
      path: '/admin/dang-nhap',
      query: { redirect: to.fullPath },
    }
  }

  if (to.meta.role === ADMIN_ROLES.QUAN_LY && !isManagerOrOwner(useAdminAuth().vaiTro.value)) {
    toast('Không đủ quyền truy cập trang này', 'warn')
    return { path: '/admin/pos', query: { denied: '1' } }
  }

  if (to.meta.title) {
    document.title = to.meta.title
  }

  return true
})

export default router
