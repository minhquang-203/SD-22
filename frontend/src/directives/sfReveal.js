/** Fade-in nhẹ khi vào viewport — chỉ 1 lần. Storefront only. */
export const vSfReveal = {
  mounted(el) {
    if (typeof window === 'undefined') return
    if (window.matchMedia('(prefers-reduced-motion: reduce)').matches) {
      el.classList.add('sf-reveal', 'sf-reveal--in')
      return
    }
    el.classList.add('sf-reveal')
    const io = new IntersectionObserver(
      ([entry]) => {
        if (!entry?.isIntersecting) return
        el.classList.add('sf-reveal--in')
        io.disconnect()
      },
      { threshold: 0.1, rootMargin: '0px 0px -32px 0px' },
    )
    io.observe(el)
    el._sfRevealIo = io
  },
  unmounted(el) {
    el._sfRevealIo?.disconnect()
    delete el._sfRevealIo
  },
}
