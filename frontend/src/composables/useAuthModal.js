import { ref } from 'vue'

const visible = ref(false)
const tab = ref('login')
const redirectAfter = ref(null)

export function useAuthModal() {
  function openAuthModal(nextTab = 'login', redirect = null) {
    tab.value = nextTab
    redirectAfter.value = redirect
    visible.value = true
  }

  function closeAuthModal() {
    visible.value = false
    redirectAfter.value = null
  }

  return {
    visible,
    tab,
    redirectAfter,
    openAuthModal,
    closeAuthModal,
  }
}
