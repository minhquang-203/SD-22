import axios from 'axios'
import { formatApiError } from '@/utils/apiError'

const request = axios.create({
  baseURL: '/api',
  timeout: 120000,
  headers: {
    'Content-Type': 'application/json',
  },
})

request.interceptors.request.use((config) => {
  if (config.data instanceof FormData) {
    if (config.headers?.set) {
      config.headers.set('Content-Type', undefined)
    } else if (config.headers) {
      delete config.headers['Content-Type']
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

    return Promise.reject(formatApiError(error.response.data))
  },
)

export default request
