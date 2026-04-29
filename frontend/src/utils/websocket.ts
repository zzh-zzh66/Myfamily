import { useUserStore } from '@/stores/user'
import { useGenealogyStore } from '@/stores/genealogy'
import { ElNotification } from 'element-plus'

let ws: WebSocket | null = null
let reconnectTimer: number | null = null
let heartbeatTimer: number | null = null

const RECONNECT_DELAY = 3000
const HEARTBEAT_INTERVAL = 30000

export function connectWebSocket() {
  const userStore = useUserStore()
  const userId = userStore.userId

  if (!userId) {
    console.log('[WebSocket] 未登录，不建立WebSocket连接')
    return
  }

  if (ws && ws.readyState === WebSocket.OPEN) {
    console.log('[WebSocket] 已连接')
    return
  }

  const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
  const host = window.location.host
  const wsUrl = `${protocol}//${host}/ws?userId=${userId}`

  console.log('[WebSocket] 连接中...', wsUrl)

  ws = new WebSocket(wsUrl)

  ws.onopen = () => {
    console.log('[WebSocket] 连接成功')
    startHeartbeat()
  }

  ws.onmessage = (event) => {
    try {
      const message = JSON.parse(event.data)
      handleWebSocketMessage(message)
    } catch (error) {
      console.error('[WebSocket] 解析消息失败', error)
    }
  }

  ws.onclose = () => {
    console.log('[WebSocket] 连接关闭')
    stopHeartbeat()
    scheduleReconnect()
  }

  ws.onerror = (error) => {
    console.error('[WebSocket] 连接错误', error)
  }
}

function handleWebSocketMessage(message: any) {
  const { type, data } = message

  switch (type) {
    case 'NEW_POST':
      handleNewPost(data)
      break
    case 'NEW_COMMENT':
      handleNewComment(data)
      break
    default:
      console.log('[WebSocket] 未知消息类型:', type)
  }
}

function handleNewPost(data: any) {
  console.log('[WebSocket] 收到新动态:', data)

  ElNotification({
    title: '新动态',
    message: `${data.authorName} 发布了新动态: ${data.title || '无标题'}`,
    type: 'info',
    duration: 5000
  })

  // 如果当前在动态页面，刷新列表
  const genealogyStore = useGenealogyStore()
  genealogyStore.fetchTreeData()
}

function handleNewComment(data: any) {
  console.log('[WebSocket] 收到新评论:', data)

  ElNotification({
    title: '新评论',
    message: `${data.authorName} 评论了动态: ${data.content?.substring(0, 30) || '...'}`,
    type: 'info',
    duration: 5000
  })
}

function startHeartbeat() {
  stopHeartbeat()
  heartbeatTimer = window.setInterval(() => {
    if (ws && ws.readyState === WebSocket.OPEN) {
      ws.send(JSON.stringify({ type: 'ping' }))
    }
  }, HEARTBEAT_INTERVAL)
}

function stopHeartbeat() {
  if (heartbeatTimer) {
    clearInterval(heartbeatTimer)
    heartbeatTimer = null
  }
}

function scheduleReconnect() {
  if (reconnectTimer) return

  console.log('[WebSocket] 准备重连...')
  reconnectTimer = window.setTimeout(() => {
    reconnectTimer = null
    connectWebSocket()
  }, RECONNECT_DELAY)
}

export function disconnectWebSocket() {
  if (reconnectTimer) {
    clearTimeout(reconnectTimer)
    reconnectTimer = null
  }
  stopHeartbeat()

  if (ws) {
    ws.close()
    ws = null
  }
  console.log('[WebSocket] 断开连接')
}

export function getWebSocketState() {
  return ws?.readyState ?? WebSocket.CLOSED
}
