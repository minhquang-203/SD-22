import { createApp, h, nextTick } from 'vue'
import { NQrCode } from 'naive-ui'
import InvoiceReceipt from '@/components/invoice/InvoiceReceipt.vue'
import { normalizeInvoice } from '@/utils/invoiceReceipt'
import receiptCss from '@/components/invoice/invoiceReceipt.css?inline'

const FONT_LINK =
  '<link rel="preconnect" href="https://fonts.googleapis.com">' +
  '<link rel="stylesheet" href="https://fonts.googleapis.com/css2?family=Be+Vietnam+Pro:wght@400;600;700;800&display=swap">'

const PRINT_CSS = `
@page { size: 80mm auto; margin: 0; }
* { box-sizing: border-box; margin: 0; padding: 0; }
html, body {
  width: 80mm;
  margin: 0;
  padding: 0;
  background: #fff;
  color: #111;
  -webkit-print-color-adjust: exact;
  print-color-adjust: exact;
  font-family: 'Be Vietnam Pro', Arial, Helvetica, sans-serif;
}
body { width: 80mm; }
.invoice-print-root {
  width: 72mm;
  margin: 0 auto;
  padding: 2mm 0;
}
${receiptCss}
`

function waitFrames(n = 2) {
  return new Promise((resolve) => {
    const step = (left) => {
      if (left <= 0) resolve()
      else requestAnimationFrame(() => step(left - 1))
    }
    step(n)
  })
}

/**
 * Mount InvoiceReceipt vào iframe ẩn và gọi print / Save PDF (khổ 80mm).
 * @param {object} rawInvoice
 * @param {{ mode?: 'print'|'pdf' }} options
 */
export async function printInvoice(rawInvoice, options = {}) {
  const invoice = normalizeInvoice(rawInvoice)
  if (!invoice?.maHoaDon) return

  const mode = options.mode || 'print'
  const prevTitle = document.title
  document.title = `HoaDon_${invoice.maHoaDon}`

  const iframe = document.createElement('iframe')
  iframe.setAttribute('aria-hidden', 'true')
  iframe.style.cssText =
    'position:fixed;right:0;bottom:0;width:0;height:0;border:0;opacity:0;pointer-events:none;'
  document.body.appendChild(iframe)

  const doc = iframe.contentDocument || iframe.contentWindow?.document
  if (!doc) {
    document.title = prevTitle
    iframe.remove()
    return
  }

  doc.open()
  doc.write(
    `<!DOCTYPE html><html><head><meta charset="utf-8"><title>HoaDon_${invoice.maHoaDon}</title>` +
      FONT_LINK +
      `<style>${PRINT_CSS}</style></head><body><div class="invoice-print-root" id="mount"></div></body></html>`,
  )
  doc.close()

  const mountEl = doc.getElementById('mount')
  const app = createApp({
    render: () => h(InvoiceReceipt, { invoice, compact: true }),
  })
  app.component('NQrCode', NQrCode)
  app.mount(mountEl)

  try {
    await nextTick()
    await waitFrames(3)
    await new Promise((r) => setTimeout(r, 180))

    const win = iframe.contentWindow
    if (!win) return

    await new Promise((resolve) => {
      let cleaned = false
      const cleanup = () => {
        if (cleaned) return
        cleaned = true
        try {
          app.unmount()
        } catch {
          /* ignore */
        }
        iframe.remove()
        document.title = prevTitle
        resolve()
      }
      win.addEventListener('afterprint', cleanup, { once: true })
      setTimeout(cleanup, 60_000)
      win.focus()
      void mode
      win.print()
    })
  } catch (err) {
    try {
      app.unmount()
    } catch {
      /* ignore */
    }
    iframe.remove()
    document.title = prevTitle
    throw err
  }
}

export function saveInvoicePdf(rawInvoice, options = {}) {
  return printInvoice(rawInvoice, { ...options, mode: 'pdf' })
}
