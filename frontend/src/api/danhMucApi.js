import request from './request'

export const getDanhMucList = () => request.get('/danh-muc')
export const getThuongHieuList = () => request.get('/thuong-hieu')
export const getDangSanPhamList = () => request.get('/dang-san-pham')
export const getCongDungList = () => request.get('/cong-dung')
export const getThanhPhanList = () => request.get('/thanh-phan')
export const getMauSacList = () => request.get('/mau-sac')
