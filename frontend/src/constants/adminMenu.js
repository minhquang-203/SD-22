/** Cấu trúc menu giống routes.static.ts trong FE mẫu (nova-admin) */
export const ADMIN_MENU = [
  {
    key: 'stats',
    label: 'Thống kê',
    icon: 'icon-park-outline:analysis',
    path: '/admin/stats',
  },
  {
    key: 'pos',
    label: 'Bán hàng tại quầy',
    icon: 'icon-park-outline:shopping-cart',
    path: '/admin/pos',
  },
  {
    key: 'orders',
    label: 'Quản lý đơn hàng',
    icon: 'icon-park-outline:list',
    path: '/admin/orders',
  },
  {
    key: 'products',
    label: 'Quản lý sản phẩm',
    icon: 'icon-park-outline:box',
    children: [
      {
        key: 'product-list',
        label: 'Danh sách sản phẩm',
        icon: 'icon-park-outline:list',
        path: '/admin/products',
      },
      {
        key: 'product-variants',
        label: 'Biến thể sản phẩm',
        icon: 'icon-park-outline:nine-points-connected',
        path: '/admin/products/variants',
      },
      {
        key: 'attributes',
        label: 'Thuộc tính',
        icon: 'icon-park-outline:setting',
        children: [
          { key: 'thuong-hieu', label: 'Thương hiệu', icon: 'carbon:carbon-ui-builder', path: '/admin/attributes/thuong-hieu' },
          { key: 'danh-muc', label: 'Danh mục', icon: 'icon-park-outline:category-management', path: '/admin/attributes/danh-muc' },
          { key: 'dang-san-pham', label: 'Dạng sản phẩm', icon: 'icon-park-outline:ad-product', path: '/admin/attributes/dang-san-pham' },
          { key: 'mau-sac', label: 'Màu sắc', icon: 'icon-park-outline:platte', path: '/admin/attributes/mau-sac' },
          { key: 'cong-dung', label: 'Công dụng', icon: 'icon-park-outline:star', path: '/admin/attributes/cong-dung' },
          { key: 'thanh-phan', label: 'Thành phần', icon: 'icon-park-outline:experiment', path: '/admin/attributes/thanh-phan' },
        ],
      },
    ],
  },
  {
    key: 'reviews',
    label: 'Quản lý đánh giá',
    icon: 'icon-park-outline:star',
    path: '/admin/reviews',
  },
  {
    key: 'discounts',
    label: 'Khuyến mãi & Voucher',
    icon: 'carbon:ticket',
    path: '/admin/discounts',
  },
  {
    key: 'recommendation',
    label: 'Gợi ý & Cá nhân hóa',
    icon: 'icon-park-outline:brain',
    children: [
      {
        key: 'quiz',
        label: 'Quiz loại da',
        icon: 'icon-park-outline:quiz',
        path: '/admin/recommendation/quiz',
      },
      {
        key: 'rules',
        label: 'Luật gợi ý',
        icon: 'icon-park-outline:lightning',
        path: '/admin/recommendation/rules',
      },
      {
        key: 'routine',
        label: 'Routine chống nắng',
        icon: 'icon-park-outline:sun',
        path: '/admin/recommendation/routine',
      },
    ],
  },
  {
    key: 'uv',
    label: 'UV & Thời tiết',
    icon: 'icon-park-outline:cloudy',
    path: '/admin/uv',
  },
  {
    key: 'users',
    label: 'Quản lý người dùng',
    icon: 'icon-park-outline:people',
    path: '/admin/users',
  },
  {
    key: 'support',
    label: 'Hỗ trợ khách hàng',
    icon: 'icon-park-outline:headset-one',
    path: '/admin/support',
  },
  {
    key: 'reports',
    label: 'Báo cáo',
    icon: 'icon-park-outline:file-excel',
    path: '/admin/reports',
  },
  {
    key: 'config',
    label: 'Cấu hình hệ thống',
    icon: 'icon-park-outline:setting-config',
    path: '/admin/config',
  },
]

/** Tìm các key menu cha cần mở theo route hiện tại */
export function findExpandedKeys(path, items = ADMIN_MENU, parents = []) {
  for (const item of items) {
    if (item.path === path) return parents
    if (item.children?.length) {
      const found = findExpandedKeys(path, item.children, [...parents, item.key])
      if (found.length) return found
    }
  }
  return []
}
