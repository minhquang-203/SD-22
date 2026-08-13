import request from './request'

export const getThongBao = () => request.get('/thong-bao')

export const demThongBaoChuaDoc = () => request.get('/thong-bao/chua-doc')

export const docTatCaThongBao = () => request.post('/thong-bao/doc-tat-ca')

export const docThongBao = (id) => request.post(`/thong-bao/${id}/doc`)

// Chuông thông báo phía khách hàng (storefront)
export const getThongBaoKhach = () => request.get('/khach-hang/toi/thong-bao')

export const demThongBaoKhachChuaDoc = () => request.get('/khach-hang/toi/thong-bao/chua-doc')

export const docTatCaThongBaoKhach = () => request.post('/khach-hang/toi/thong-bao/doc-tat-ca')

export const docThongBaoKhach = (id) => request.post(`/khach-hang/toi/thong-bao/${id}/doc`)
