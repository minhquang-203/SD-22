import { ADMIN_ROLES, ROLE_RANK } from '@/constants/adminMenu'

export function getAdminHomePath(role) {
  return role === ADMIN_ROLES.QUAN_LY || role === ADMIN_ROLES.CHU
    ? '/admin/stats'
    : '/admin/pos'
}

export function isManagerOrOwner(role) {
  return role === ADMIN_ROLES.QUAN_LY || role === ADMIN_ROLES.CHU
}

export function getRoleRank(role) {
  return ROLE_RANK[role] ?? 0
}

export function getRoleLabel(role) {
  if (role === ADMIN_ROLES.CHU) return 'Chủ'
  if (role === ADMIN_ROLES.QUAN_LY) return 'Quản lý'
  if (role === ADMIN_ROLES.NHAN_VIEN) return 'Nhân viên'
  return '—'
}

/** Vai trò có thể gán khi tạo/sửa (chỉ cấp thấp hơn người thực hiện). */
export function assignableRoles(actorRole) {
  const actorRank = getRoleRank(actorRole)
  return [ADMIN_ROLES.QUAN_LY, ADMIN_ROLES.NHAN_VIEN].filter(
    (role) => getRoleRank(role) < actorRank,
  )
}

/** Chỉ thao tác lên tài khoản cấp thấp hơn; không tự thao tác chính mình. */
export function canManageStaff(actorRole, actorId, target) {
  if (!target || actorId == null) return false
  if (Number(actorId) === Number(target.id)) return false
  return getRoleRank(target.maVaiTro) < getRoleRank(actorRole)
}

export const STAFF_ACTION_DENIED = 'Không đủ quyền'
