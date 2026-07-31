import { ref } from 'vue'

const visible = ref(false)

export function useWelcomeModal() {
  function openWelcomeModal() {
    visible.value = true
  }

  function closeWelcomeModal() {
    visible.value = false
  }

  return {
    visible,
    openWelcomeModal,
    closeWelcomeModal,
  }
}
