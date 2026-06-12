import { createRouter, createWebHistory } from 'vue-router'

import AdminLayout from '@/layouts/AdminLayout.vue'



const router = createRouter({

  history: createWebHistory(),

  routes: [

    { path: '/', redirect: '/admin/products' },

    // =======================================================
    // 1. ROUTE KHÁCH HÀNG (STOREFRONT) - GIAO DIỆN QUIZ NÂU GOLD
    // =======================================================
    {
      path: "/quiz",
      name: "SkinQuiz",
      // Đã trỏ đúng vào thư mục storefront
      component: () => import("@/views/storefront/SkinQuiz.vue"),
      meta: { title: "Tìm chân ái chống nắng - SUNOVA" },
    },

    // =======================================================
    // 2. ROUTE QUẢN TRỊ (ADMIN)
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

          name: 'AdminOrders',

          component: () => import('@/views/admin/orders/OrderList.vue'),

          meta: { title: 'Quản lý đơn hàng', breadcrumb: 'Quản lý đơn hàng' },

        },

        {

          path: 'products',

          name: 'AdminProducts',

          component: () => import('@/views/product/ProductList.vue'),

          meta: { title: 'Danh sách sản phẩm', breadcrumb: 'Quản lý sản phẩm / Danh sách' },

        },

        {

          path: 'products/variants',

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



export default router