import axios from 'axios'
import router from '@/router'
import { formatApiError } from '@/utils/apiError'
import { getAdminToken, useAdminAuth } from '@/composables/useAdminAuth'
<<<<<<< HEAD
import { getCustomerToken, clearCustomerAuth } from '@/composables/useAuth'
=======
import { getCustomerToken, useAuth } from '@/composables/useAuth'
import { useAuthModal } from '@/composables/useAuthModal'
import { toast } from '@/composables/useToast'
>>>>>>> 1391de8fff197ee69316cdfc8232a7a4b6810198

const CUSTOMER_API_PREFIXES = [
  '/yeu-thich',
  '/khach-hang/toi',
  '/gio-hang',
  '/online',
  '/hoa-don/cua-toi',
  '/khach/quiz/ket-qua',
]

const request = axios.create({
  baseURL: '/api',
  timeout: 120000,
  headers: {
    'Content-Type': 'application/json',
  },
})

function attachBearer(config, token) {
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
}

function isCustomerApiUrl(url) {
  if (url.includes('/online/guest')) return false
  if (url.includes('/hoa-don/tra-cuu')) return false
  return CUSTOMER_API_PREFIXES.some((prefix) => url.includes(prefix))
}

request.interceptors.request.use((config) => {
  if (config.data instanceof FormData) {
    if (config.headers?.set) {
      config.headers.set('Content-Type', undefined)
    } else if (config.headers) {
      delete config.headers['Content-Type']
    }
  }

  const url = String(config.url || '')
  const isHoTro = url.includes('/ho-tro')
  const isCustomerApi = isCustomerApiUrl(url)

  if (isHoTro) {
    const adminToken = getAdminToken()
    if (adminToken) {
      attachBearer(config, adminToken)
    } else {
      attachBearer(config, getCustomerToken())
    }
  } else if (url.includes('/yeu-cau-mua-so-luong-lon')) {
    // POST công khai: gắn token khách nếu có (để BE biết đã đăng nhập)
    attachBearer(config, getCustomerToken())
  } else if (isCustomerApi) {
    attachBearer(config, getCustomerToken())
  } else {
    const adminToken = getAdminToken()
    if (adminToken) {
      attachBearer(config, adminToken)
    }
  }

  return config
})

request.interceptors.response.use(
  (response) => response,
  (error) => {
    if (!error.response) {
      const isGateway =
        error.code === 'ERR_BAD_RESPONSE' || String(error.message || '').includes('502')
      const msg = isGateway
        ? 'Không kết nối được máy chủ. Vui lòng thử lại sau.'
        : 'Mất kết nối mạng. Vui lòng kiểm tra internet và thử lại.'
      toast(msg, 'warn')
      return Promise.reject(msg)
    }

    const status = error.response.status
    const url = String(error.config?.url || '')
    const isCustomerApi = isCustomerApiUrl(url)
    const isAdminLoginRequest = url.includes('/auth/nhan-vien/dang-nhap')
    const isKhachAuthRequest = url.includes('/auth/khach/')
    const onAdmin = router.currentRoute.value.path.startsWith('/admin')

<<<<<<< HEAD
    if (isCustomerApi && (status === 401 || status === 403)) {
      clearCustomerAuth()
      return Promise.reject('Phiên đăng nhập đã hết hạn, vui lòng đăng nhập lại')
    }

    if (!isCustomerApi && !isAdminLoginRequest && !isKhachAuthRequest && getAdminToken() && (status === 401 || status === 403)) {
      useAdminAuth().dangXuat()
      const currentPath = router.currentRoute.value.path
      if (currentPath.startsWith('/admin') && currentPath !== '/admin/dang-nhap') {
        router.push({
          path: '/admin/dang-nhap',
          query: {
            redirect: router.currentRoute.value.fullPath,
            expired: '1',
          },
        })
=======
    // 401 — hết phiên: đăng xuất đúng khu
    if (status === 401 && !isAdminLoginRequest && !isKhachAuthRequest) {
      if (isCustomerApi || (!onAdmin && getCustomerToken())) {
        try {
          useAuth().dangXuat()
        } catch {
          /* ignore */
        }
        try {
          useAuthModal().openAuthModal('login', router.currentRoute.value.fullPath)
        } catch {
          /* ignore */
        }
        const msg = 'Phiên đăng nhập đã hết hạn, vui lòng đăng nhập lại'
        toast(msg, 'warn')
        return Promise.reject(msg)
      }

      if (getAdminToken()) {
        useAdminAuth().dangXuat()
        if (onAdmin && router.currentRoute.value.path !== '/admin/dang-nhap') {
          router.push({
            path: '/admin/dang-nhap',
            query: {
              redirect: router.currentRoute.value.fullPath,
              expired: '1',
            },
          })
        }
        const msg = 'Phiên đăng nhập quản trị đã hết hạn, vui lòng đăng nhập lại'
        toast(msg, 'warn')
        return Promise.reject(msg)
>>>>>>> 1391de8fff197ee69316cdfc8232a7a4b6810198
      }
    }

    // 403 — không đủ quyền (không đăng xuất)
    if (status === 403 && !isAdminLoginRequest && !isKhachAuthRequest) {
      const msg = 'Bạn không có quyền thực hiện thao tác này'
      toast(msg, 'warn')
      return Promise.reject(msg)
    }

    // 5xx
    if (status >= 500) {
      const msg = 'Hệ thống đang gặp sự cố, vui lòng thử lại sau'
      toast(msg, 'warn')
      return Promise.reject(msg)
    }

    const data = error.response.data
    if (data?.code === 'PRICE_CHANGED') {
      return Promise.reject({
        code: 'PRICE_CHANGED',
        message: formatApiError(data),
        details: data.details || {},
      })
    }

    return Promise.reject(formatApiError(data))
  },
)

export default request
