import axios from 'axios'
import router from '@/router'
import { formatApiError } from '@/utils/apiError'
import { getAdminToken, useAdminAuth } from '@/composables/useAdminAuth'
import { getCustomerToken } from '@/composables/useAuth'

const CUSTOMER_API_PREFIXES = ['/yeu-thich', '/khach-hang/toi', '/gio-hang', '/online', '/hoa-don/cua-toi']

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
  const isCustomerApi = isCustomerApiUrl(url)

  if (isCustomerApi) {
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
        error.code === 'ERR_BAD_RESPONSE' ||
        String(error.message || '').includes('502')
      return Promise.reject(
        isGateway
          ? 'Không kết nối được backend. Hãy chạy backend trước (start-backend.cmd).'
          : error.message || 'Không kết nối được server',
      )
    }

    const status = error.response.status
    const url = String(error.config?.url || '')
    const isCustomerApi = isCustomerApiUrl(url)
    const isAdminLoginRequest = url.includes('/auth/nhan-vien/dang-nhap')
    const isKhachAuthRequest = url.includes('/auth/khach/')

    if (isCustomerApi && (status === 401 || status === 403)) {
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
      }
      return Promise.reject('Phiên đăng nhập quản trị đã hết hạn, vui lòng đăng nhập lại')
    }

    return Promise.reject(formatApiError(error.response.data))
  },
)

export default request

