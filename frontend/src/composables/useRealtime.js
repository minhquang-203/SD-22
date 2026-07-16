import { Client } from '@stomp/stompjs'
import SockJS from 'sockjs-client'
import { getAdminToken } from '@/composables/useAdminAuth'
import { getCustomerId, getCustomerToken } from '@/composables/useAuth'

function createSession() {
  let client = null
  let activeToken = null
  const entries = []

  function disconnect() {
    try {
      client?.deactivate()
    } catch {
      // ignore
    }
    client = null
    activeToken = null
    entries.forEach((entry) => {
      entry.stompSub = null
    })
  }

  function resubscribeAll() {
    entries.forEach((entry) => {
      try {
        entry.stompSub?.unsubscribe()
      } catch {
        // ignore
      }
      entry.stompSub = null
      if (client?.connected) {
        entry.stompSub = client.subscribe(entry.destination, (frame) => {
          try {
            entry.handler(JSON.parse(frame.body))
          } catch {
            // ignore malformed payload
          }
        })
      }
    })
  }

  function ensureConnected(token) {
    if (!token) return
    if (client?.active && activeToken === token) return

    disconnect()
    activeToken = token
    client = new Client({
      webSocketFactory: () => new SockJS('/ws'),
      connectHeaders: {
        Authorization: `Bearer ${token}`,
      },
      reconnectDelay: 4000,
      heartbeatIncoming: 15000,
      heartbeatOutgoing: 15000,
      onConnect: () => {
        resubscribeAll()
      },
      onStompError: () => {
        // reconnectDelay handles retry
      },
    })
    client.activate()
  }

  function subscribe(destination, handler, getToken) {
    const entry = { destination, handler, stompSub: null }
    entries.push(entry)

    const token = getToken()
    ensureConnected(token)
    if (client?.connected) {
      entry.stompSub = client.subscribe(destination, (frame) => {
        try {
          handler(JSON.parse(frame.body))
        } catch {
          // ignore
        }
      })
    }

    return () => {
      try {
        entry.stompSub?.unsubscribe()
      } catch {
        // ignore
      }
      const idx = entries.indexOf(entry)
      if (idx >= 0) entries.splice(idx, 1)
      if (entries.length === 0) {
        disconnect()
      }
    }
  }

  return { subscribe, disconnect }
}

const adminSession = createSession()
const customerSession = createSession()

export function subscribeAdminOrders(handler) {
  return adminSession.subscribe('/topic/admin/orders', handler, getAdminToken)
}

export function subscribeAdminNotifications(handler) {
  return adminSession.subscribe('/topic/admin/notifications', handler, getAdminToken)
}

export function subscribeCustomerOrders(handler) {
  const customerId = getCustomerId()
  if (!customerId) {
    return () => {}
  }
  return customerSession.subscribe(
    `/topic/customers/${customerId}/orders`,
    handler,
    getCustomerToken,
  )
}
