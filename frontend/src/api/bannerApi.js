import request from './request'

// Storefront — banner đang bật
export function fetchActiveBanners() {
  return request.get('/khach/banners')
}

// Admin
export function getAllBannersAdmin() {
  return request.get('/admin/banners')
}

export function createBannerAdmin(payload) {
  return request.post('/admin/banners', payload)
}

export function updateBannerAdmin(id, payload) {
  return request.put(`/admin/banners/${id}`, payload)
}

export function deleteBannerAdmin(id) {
  return request.delete(`/admin/banners/${id}`)
}

export function uploadBannerImageAdmin(file) {
  const form = new FormData()
  form.append('file', file)
  return request.post('/admin/banners/upload', form)
}
