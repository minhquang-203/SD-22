import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import './styles/main.css'
import '@/composables/useAuth'
import '@/composables/useAdminAuth'
import '@/composables/useCart'
import '@/composables/useAuthModal'
import { toast } from '@/composables/useToast'

const app = createApp(App)

app.config.errorHandler = (err, instance, info) => {
  console.error('[Vue errorHandler]', err, info)
  toast('Đã có lỗi xảy ra, vui lòng thử lại', 'warn')
}

app.config.warnHandler = (msg, instance, trace) => {
  if (import.meta.env.DEV) {
    console.warn('[Vue warn]', msg, trace)
  }
}

app.use(router).mount('#app')
