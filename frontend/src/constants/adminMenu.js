/** Vai trò admin — đồng bộ mã backend (vai_tro.ma) khi có login */
export const ADMIN_ROLES = {
  QUAN_LY: 'QUAN_LY',
  NHAN_VIEN: 'NHAN_VIEN',
}

/**
 * Tạm thời: đổi 'QUAN_LY' | 'NHAN_VIEN' để thử phân quyền menu.
 * Sau này thay bằng vai trò từ login (localStorage / auth store).
 */
export const CURRENT_ROLE = ADMIN_ROLES.QUAN_LY

const BOTH_ROLES = [ADMIN_ROLES.QUAN_LY, ADMIN_ROLES.NHAN_VIEN]
const MANAGER_ONLY = [ADMIN_ROLES.QUAN_LY]

/** Cấu trúc menu giống routes.static.ts trong FE mẫu (nova-admin) */
const PRODUCTS_MENU = {
  key: 'products',
  label: 'Quản lý sản phẩm',
  icon: 'icon-park-outline:box',
  roles: BOTH_ROLES,
  children: [
    {
      key: 'product-list',
      label: 'Danh sách sản phẩm',
      icon: 'icon-park-outline:list',
      path: '/admin/products',
      roles: BOTH_ROLES,
    },
    {
      key: 'product-variants',
      label: 'Biến thể sản phẩm',
      icon: 'icon-park-outline:nine-points-connected',
      path: '/admin/products/variants',
      roles: BOTH_ROLES,
    },
    {
      key: 'attributes',
      label: 'Thuộc tính',
      icon: 'icon-park-outline:setting',
      roles: BOTH_ROLES,
      children: [
        { key: 'thuong-hieu', label: 'Thương hiệu', icon: 'carbon:carbon-ui-builder', path: '/admin/attributes/thuong-hieu', roles: BOTH_ROLES },
        { key: 'danh-muc', label: 'Danh mục', icon: 'icon-park-outline:category-management', path: '/admin/attributes/danh-muc', roles: BOTH_ROLES },
        { key: 'dang-san-pham', label: 'Dạng sản phẩm', icon: 'icon-park-outline:ad-product', path: '/admin/attributes/dang-san-pham', roles: BOTH_ROLES },
        { key: 'mau-sac', label: 'Màu sắc', icon: 'icon-park-outline:platte', path: '/admin/attributes/mau-sac', roles: BOTH_ROLES },
        { key: 'cong-dung', label: 'Công dụng', icon: 'icon-park-outline:star', path: '/admin/attributes/cong-dung', roles: BOTH_ROLES },
        { key: 'thanh-phan', label: 'Thành phần', icon: 'icon-park-outline:experiment', path: '/admin/attributes/thanh-phan', roles: BOTH_ROLES },
      ],
    },
  ],
}

const RECOMMENDATION_MENU = {
  key: 'recommendation',
  label: 'Gợi ý & Cá nhân hóa',
  icon: 'icon-park-outline:brain',
  roles: MANAGER_ONLY,
  children: [
    {
      key: 'quiz',
      label: 'Quiz loại da',
      icon: 'icon-park-outline:quiz',
      path: '/admin/recommendation/quiz',
      roles: MANAGER_ONLY,
    },
    {
      key: 'rules',
      label: 'Luật gợi ý',
      icon: 'icon-park-outline:lightning',
      path: '/admin/recommendation/rules',
      roles: MANAGER_ONLY,
    },
    {
      key: 'routine',
      label: 'Routine chống nắng',
      icon: 'icon-park-outline:sun',
      path: '/admin/recommendation/routine',
      roles: MANAGER_ONLY,
    },
  ],
}

export const ADMIN_MENU = [
  {
    type: 'group',
    key: 'group-van-hanh',
    label: 'Vận hành (Nhân viên)',
    children: [
      {
        key: 'pos',
        label: 'Bán hàng tại quầy',
        icon: 'icon-park-outline:shopping-cart',
        path: '/admin/pos',
        roles: BOTH_ROLES,
      },
      {
        key: 'hoa-don',
        label: 'Hóa đơn',
        icon: 'icon-park-outline:bill',
        path: '/admin/orders',
        roles: BOTH_ROLES,
      },
      {
        key: 'hoa-don-detail',
        label: 'Chi tiết hóa đơn',
        icon: 'icon-park-outline:doc-detail',
        path: '/admin/hoa-don',
        roles: BOTH_ROLES,
      },
      PRODUCTS_MENU,
      {
        key: 'reviews',
        label: 'Quản lý đánh giá',
        icon: 'icon-park-outline:star',
        path: '/admin/reviews',
        roles: MANAGER_ONLY,
      },
      {
        key: 'support',
        label: 'Hỗ trợ khách hàng',
        icon: 'icon-park-outline:headset-one',
        path: '/admin/support',
        roles: BOTH_ROLES,
      },
    ],
  },
  {
    type: 'group',
    key: 'group-quan-tri',
    label: 'Quản trị (Quản lý)',
    children: [
      {
        key: 'stats',
        label: 'Thống kê',
        icon: 'icon-park-outline:analysis',
        path: '/admin/stats',
        roles: MANAGER_ONLY,
      },
      {
        key: 'reports',
        label: 'Báo cáo',
        icon: 'icon-park-outline:file-excel',
        path: '/admin/reports',
        roles: MANAGER_ONLY,
      },
      {
        key: 'discounts',
        label: 'Khuyến mãi & Voucher',
        icon: 'carbon:ticket',
        children: [
          {
            key: 'voucher',
            label: 'Phiếu giảm giá',
            icon: 'mdi:ticket-percent',
            path: '/admin/voucher',
            roles: MANAGER_ONLY,
          },
          {
            key: 'sale',
            label: 'Đợt giảm giá',
            icon: 'tabler:discount-2',
            path: '/admin/sale',
            roles: MANAGER_ONLY,
          },
        ],
        roles: MANAGER_ONLY,
      },
      RECOMMENDATION_MENU,
      {
        key: 'uv',
        label: 'UV & Thời tiết',
        icon: 'icon-park-outline:cloudy',
        path: '/admin/uv',
        roles: MANAGER_ONLY,
      },
      {
        key: 'users',
        label: 'Quản lý người dùng',
        icon: 'icon-park-outline:people',
        path: '/admin/users',
        roles: MANAGER_ONLY,
      },
      {
        key: 'config',
        label: 'Cấu hình hệ thống',
        icon: 'icon-park-outline:setting-config',
        path: '/admin/config',
        roles: MANAGER_ONLY,
      },
    ],
  },
]

/**
 * Lấy vai trò hiện tại. Ưu tiên localStorage (sau login), fallback CURRENT_ROLE.
 * Lưu ý: ẩn menu chỉ là UX — phân quyền thật phải chặn ở backend API.
 */
export function getCurrentRole() {
  try {
    const stored = localStorage.getItem('sunova_admin_role')
    if (stored && Object.values(ADMIN_ROLES).includes(stored)) {
      return stored
    }
  } catch {
    // ignore (SSR / private mode)
  }
  return CURRENT_ROLE
}

function isRoleAllowed(item, role) {
  if (!item.roles?.length) return true
  return item.roles.includes(role)
}

/**
 * Lọc menu theo vai trò. Nhóm không còn mục con → ẩn cả nhóm.
 * Mục cha có children chỉ hiện khi còn ít nhất một con được phép.
 */
export function filterMenuByRole(items, role) {
  const result = []

  for (const item of items) {
    if (item.type === 'group') {
      const children = filterMenuByRole(item.children || [], role)
      if (children.length) {
        result.push({ ...item, children })
      }
      continue
    }

    if (item.children?.length) {
      const children = filterMenuByRole(item.children, role)
      if (children.length && isRoleAllowed(item, role)) {
        result.push({ ...item, children })
      }
      continue
    }

    if (isRoleAllowed(item, role)) {
      result.push(item)
    }
  }

  return result
}

/** Tìm các key menu cha cần mở theo route hiện tại */
export function findExpandedKeys(path, items = ADMIN_MENU, parents = []) {
  for (const item of items) {
    if (item.type === 'group') {
      if (item.children?.length) {
        const found = findExpandedKeys(path, item.children, parents)
        if (found.length) return found
      }
      continue
    }
    if (item.path === path) return parents
    if (item.children?.length) {
      const found = findExpandedKeys(path, item.children, [...parents, item.key])
      if (found.length) return found
    }
  }
  return []
}
