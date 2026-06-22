import request from './request'

function createAttributeApi(basePath) {
  return {
    list: () => request.get(`/${basePath}`),
    detail: (id) => request.get(`/${basePath}/detail`, { params: { id } }),
    getMaTiepTheo: () => request.get(`/${basePath}/ma-tiep-theo`),
    add: (payload) => request.post(`/${basePath}/add`, payload),
    update: (id, payload) => request.put(`/${basePath}/update/${id}`, payload),
    remove: (id) => request.delete(`/${basePath}/delete`, { params: { id } }),
  }
}

export const thuongHieuApi = createAttributeApi('thuong-hieu')
export const danhMucApi = createAttributeApi('danh-muc')
export const dangSanPhamApi = createAttributeApi('dang-san-pham')
export const mauSacApi = createAttributeApi('mau-sac')
export const congDungApi = createAttributeApi('cong-dung')
export const thanhPhanApi = createAttributeApi('thanh-phan')
