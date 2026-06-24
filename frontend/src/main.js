import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import './styles/main.css'
import '@/composables/useAuth'
import '@/composables/useAdminAuth'
import '@/composables/useCart'
import '@/composables/useAuthModal'

createApp(App).use(router).mount('#app')
