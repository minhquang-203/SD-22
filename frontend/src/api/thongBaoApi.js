import request from './request'

export const getThongBao = () => request.get('/thong-bao')

export const demThongBaoChuaDoc = () => request.get('/thong-bao/chua-doc')

export const docTatCaThongBao = () => request.post('/thong-bao/doc-tat-ca')

export const docThongBao = (id) => request.post(`/thong-bao/${id}/doc`)
