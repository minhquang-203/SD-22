import axios from 'axios'
import router from '@/router'
import { formatApiError } from '@/utils/apiError'
import { getAdminToken, useAdminAuth } from '@/composables/useAdminAuth'
import { getCustomerToken, useAuth } from '@/composables/useAuth'
import { useAuthModal } from '@/composables/useAuthModal'
import { toast } from '@/composables/useToast'

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

/** Chuẩn hoá path axios (baseURL /api đã tách). */
function normalizeApiPath(url) {
  return String(url || '').split('?')[0].replace(/\/+$/, '')
}

/**
 * Hỗ trợ chat: chọn JWT theo endpoint (không ưu tiên admin trên storefront).
 * - POST /ho-tro/phien, POST /ho-tro/tin-nhan → chỉ KHÁCH
 * - GET /ho-tro/phien, POST .../tra-loi, PUT .../da-doc → chỉ NV/QL/CHU
 * - GET .../tin-nhan → cả hai: theo khu /admin hay storefront
 */
function attachHoTroBearer(config, url) {
  const method = String(config.method || 'get').toLowerCase()
  const path = normalizeApiPath(url)
  const onAdmin = router.currentRoute.value.path.startsWith('/admin')

  const isCustomerOnly =
    (method === 'post' && /\/ho-tro\/phien$/.test(path)) ||
    (method === 'post' && /\/ho-tro\/tin-nhan$/.test(path))

  const isAdminOnly =
    (method === 'get' && /\/ho-tro\/phien$/.test(path)) ||
    (method === 'post' && /\/ho-tro\/phien\/\d+\/tra-loi$/.test(path)) ||
    (method === 'put' && /\/ho-tro\/phien\/\d+\/da-doc$/.test(path))

  if (isCustomerOnly) {
    attachBearer(config, getCustomerToken())
    return
  }
  if (isAdminOnly) {
    attachBearer(config, getAdminToken())
    return
  }
  // GET /ho-tro/phien/{id}/tin-nhan
  if (method === 'get' && /\/ho-tro\/phien\/\d+\/tin-nhan$/.test(path)) {
    attachBearer(config, onAdmin ? getAdminToken() : getCustomerToken())
    return
  }
  // Fallback an toàn theo khu
  attachBearer(config, onAdmin ? getAdminToken() : getCustomerToken())
}

function isCustomerHoTroUrl(url, method) {
  const m = String(method || 'get').toLowerCase()
  const path = normalizeApiPath(url)
  return (
    (m === 'post' && /\/ho-tro\/phien$/.test(path)) ||
    (m === 'post' && /\/ho-tro\/tin-nhan$/.test(path)) ||
    (m === 'get' && /\/ho-tro\/phien\/\d+\/tin-nhan$/.test(path))
  )
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
    attachHoTroBearer(config, url)
  } else if (url.includes('/yeu-cau-mua-so-luong-lon')) {
    // POST công khai: gắn token khách nếu có (để BE biết đã đăng nhập)
    attachBearer(config, getCustomerToken())
  } else if (url.includes('/danh-gia/add') || url.includes('/danh-gia/like')) {
    // Đánh giá: token khách nếu đã đăng nhập. Khách vãng lai chứng minh bằng tracking token trong body.
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
    const method = String(error.config?.method || 'get')
    const isCustomerApi = isCustomerApiUrl(url)
    const isCustomerHoTro = url.includes('/ho-tro') && isCustomerHoTroUrl(url, method)
    const isAdminLoginRequest = url.includes('/auth/nhan-vien/dang-nhap')
    const isKhachAuthRequest = url.includes('/auth/khach/')
    const onAdmin = router.currentRoute.value.path.startsWith('/admin')

    // 401 — hết phiên: đăng xuất đúng khu
    if (status === 401 && !isAdminLoginRequest && !isKhachAuthRequest) {
      if (isCustomerApi || isCustomerHoTro || (!onAdmin && getCustomerToken())) {
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
        try {
          useAdminAuth().dangXuat()
        } catch {
          /* ignore */
        }
        if (onAdmin && router.currentRoute.value.path !== '/admin/dang-nhap') {
          router.push({
            path: '/admin/dang-nhap',
            query: {
              redirect: router.currentRoute.value.fullPath,
              expired: '1',
            },
          })
        }
        const msg = 'Phiên đăng nhập đã hết hạn, vui lòng đăng nhập lại'
        toast(msg, 'warn')
        return Promise.reject(msg)
      }
    }

    // 403 — không đủ quyền (không đăng xuất)
    if (status === 403 && !isAdminLoginRequest && !isKhachAuthRequest) {
      // Storefront chat hỗ trợ: tránh chữ "không có quyền" / Forbidden với khách
      if (!onAdmin && isCustomerHoTro) {
        const msg = getCustomerToken()
          ? 'Không kết nối được nhân viên. Vui lòng thử lại.'
          : 'Vui lòng đăng nhập để chat với nhân viên'
        toast(msg, 'warn')
        return Promise.reject(msg)
      }
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
