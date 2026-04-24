import type { GenealogyNode } from '@/types/api'

// 节点布局信息
export interface NodeLayout {
  id: number
  x: number
  y: number
  width: number
  height: number
  generation: number
}

// 树布局信息
export interface TreeLayout {
  nodes: Map<number, NodeLayout>
  generationCount: number
  maxWidth: number
}

// 节点尺寸
const NODE_WIDTH = 120
const NODE_HEIGHT = 80
const HORIZONTAL_GAP = 40
const VERTICAL_GAP = 100

// 计算树布局
export function calculateTreeLayout(nodes: GenealogyNode[]): TreeLayout {
  const nodeMap = new Map<number, GenealogyNode>()
  const nodeLayouts = new Map<number, NodeLayout>()
  const generationMap = new Map<number, number>()

  // 构建节点映射
  nodes.forEach(node => {
    nodeMap.set(node.id, node)
  })

  // 找出根节点（没有父亲的节点）
  const rootNodes = nodes.filter(node => {
    const parentNode = node.fatherNode
    return !parentNode
  })

  // 按代计算布局
  function calculateGeneration(node: GenealogyNode, generation: number) {
    const existingCount = generationMap.get(generation) || 0
    generationMap.set(generation, existingCount + 1)

    // 计算当前节点位置
    const x = (existingCount - (generationMap.get(generation)! - 1) / 2) * (NODE_WIDTH + HORIZONTAL_GAP)
    const y = generation * (NODE_HEIGHT + VERTICAL_GAP)

    nodeLayouts.set(node.id, {
      id: node.id,
      x,
      y,
      width: NODE_WIDTH,
      height: NODE_HEIGHT,
      generation
    })

    // 递归计算子节点
    if (node.children && node.children.length > 0) {
      node.children.forEach(child => calculateGeneration(child, generation + 1))
    }
  }

  // 从根节点开始计算
  rootNodes.forEach((root, index) => {
    calculateGeneration(root, 0)
  })

  // 计算最大宽度
  let maxWidth = 0
  generationMap.forEach(count => {
    const width = count * NODE_WIDTH + (count - 1) * HORIZONTAL_GAP
    maxWidth = Math.max(maxWidth, width)
  })

  return {
    nodes: nodeLayouts,
    generationCount: generationMap.size,
    maxWidth
  }
}

// 绘制连接线
export function drawConnectionLines(
  ctx: CanvasRenderingContext2D,
  layout: TreeLayout
) {
  // 设置线条样式
  ctx.strokeStyle = '#4A4A4A'
  ctx.lineWidth = 2
  ctx.lineCap = 'round'

  // 绘制亲子连接线
  layout.nodes.forEach((nodeLayout, nodeId) => {
    const node = findNodeById(nodeId)
    if (!node) return

    // 查找父亲节点
    if (node.fatherNode) {
      const fatherLayout = layout.nodes.get(node.fatherNode.id)
      if (fatherLayout) {
        drawParentChildLine(ctx, fatherLayout, nodeLayout)
      }
    }

    // 绘制配偶连接线
    if (node.spouseNode) {
      const spouseLayout = layout.nodes.get(node.spouseNode.id)
      if (spouseLayout) {
        drawSpouseLine(ctx, nodeLayout, spouseLayout)
      }
    }
  })
}

// 绘制父子连接线
function drawParentChildLine(
  ctx: CanvasRenderingContext2D,
  parent: NodeLayout,
  child: NodeLayout
) {
  const startX = parent.x
  const startY = parent.y + parent.height / 2
  const endX = child.x
  const endY = child.y - child.height / 2

  ctx.beginPath()
  ctx.moveTo(startX, startY)
  ctx.lineTo(endX, endY)
  ctx.stroke()
}

// 绘制配偶连接线
function drawSpouseLine(
  ctx: CanvasRenderingContext2D,
  node1: NodeLayout,
  node2: NodeLayout
) {
  const startX = node1.x + node1.width / 2
  const startY = node1.y
  const endX = node2.x - node2.width / 2
  const endY = node2.y

  ctx.beginPath()
  ctx.moveTo(startX, startY)
  ctx.lineTo(endX, endY)
  ctx.strokeStyle = '#5B8C6B' // 石绿色
  ctx.stroke()
  ctx.strokeStyle = '#4A4A4A' // 恢复默认颜色
}

// 根据ID查找节点
let allNodes: GenealogyNode[] = []

export function setNodes(nodes: GenealogyNode[]) {
  allNodes = nodes
}

function findNodeById(id: number): GenealogyNode | undefined {
  return allNodes.find(node => node.id === id)
}

// 绘制树
export function drawTree(
  ctx: CanvasRenderingContext2D,
  layout: TreeLayout,
  nodes: GenealogyNode[],
  options: {
    onNodeClick?: (node: GenealogyNode) => void
    onNodeDbClick?: (node: GenealogyNode) => void
    selectedId?: number | null
  }
) {
  setNodes(nodes)

  nodes.forEach(node => {
    const nodeLayout = layout.nodes.get(node.id)
    if (nodeLayout) {
      drawNode(ctx, node, nodeLayout, options.selectedId === node.id)
    }
  })
}

// 绘制单个节点
function drawNode(
  ctx: CanvasRenderingContext2D,
  node: GenealogyNode,
  layout: NodeLayout,
  isSelected: boolean
) {
  const { x, y, width, height } = layout

  // 保存上下文
  ctx.save()

  // 绘制节点背景
  const gradient = ctx.createLinearGradient(
    x - width / 2,
    y - height / 2,
    x + width / 2,
    y + height / 2
  )

  if (node.gender === 'male') {
    gradient.addColorStop(0, '#3D5A80')
    gradient.addColorStop(1, '#2D4A70')
  } else {
    gradient.addColorStop(0, '#C14A3F')
    gradient.addColorStop(1, '#A63D33')
  }

  // 圆角矩形
  const radius = 8
  ctx.beginPath()
  ctx.moveTo(x - width / 2 + radius, y - height / 2)
  ctx.lineTo(x + width / 2 - radius, y - height / 2)
  ctx.quadraticCurveTo(x + width / 2, y - height / 2, x + width / 2, y - height / 2 + radius)
  ctx.lineTo(x + width / 2, y + height / 2 - radius)
  ctx.quadraticCurveTo(x + width / 2, y + height / 2, x + width / 2 - radius, y + height / 2)
  ctx.lineTo(x - width / 2 + radius, y + height / 2)
  ctx.quadraticCurveTo(x - width / 2, y + height / 2, x - width / 2, y + height / 2 - radius)
  ctx.lineTo(x - width / 2, y - height / 2 + radius)
  ctx.quadraticCurveTo(x - width / 2, y - height / 2, x - width / 2 + radius, y - height / 2)
  ctx.closePath()

  ctx.fillStyle = gradient
  ctx.fill()

  // 绘制阴影
  ctx.shadowColor = 'rgba(0, 0, 0, 0.2)'
  ctx.shadowBlur = 8
  ctx.shadowOffsetY = 4
  ctx.fill()
  ctx.shadowColor = 'transparent'

  // 绘制选中边框
  if (isSelected) {
    ctx.strokeStyle = '#C14A3F'
    ctx.lineWidth = 3
    ctx.stroke()
  }

  // 绘制顶部高光
  ctx.beginPath()
  ctx.moveTo(x - width / 2 + radius * 2, y - height / 2 + 2)
  ctx.lineTo(x + width / 2 - radius * 2, y - height / 2 + 2)
  ctx.strokeStyle = 'rgba(255, 255, 255, 0.3)'
  ctx.lineWidth = 2
  ctx.lineCap = 'round'
  ctx.stroke()

  // 绘制名字
  ctx.fillStyle = '#FFFFFF'
  ctx.font = 'bold 14px "Noto Serif SC", serif'
  ctx.textAlign = 'center'
  ctx.textBaseline = 'middle'
  ctx.fillText(node.name, x, y - 5)

  // 绘制辈分
  ctx.font = '12px "Noto Sans SC", sans-serif'
  ctx.fillStyle = 'rgba(255, 255, 255, 0.8)'
  ctx.fillText(`第${node.generation}代`, x, y + 15)

  // 恢复上下文
  ctx.restore()
}

// 导出绘图工具
export const genealogyCanvasUtils = {
  calculateTreeLayout,
  drawConnectionLines,
  drawTree,
  drawNode,
  NODE_WIDTH,
  NODE_HEIGHT
}
