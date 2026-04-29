import type { Member, CoupleNode } from '@/types/api'

export interface NodeLayout {
  id: string
  x: number
  y: number
  width: number
  height: number
  generation: number
}

export interface TreeLayout {
  nodes: Map<string, NodeLayout>
  generationCount: number
  maxWidth: number
  maxHeight: number
  generations: number[]
  rootNodeId: string | null
  rootX: number
  rootY: number
}

const SINGLE_NODE_WIDTH = 100
const SINGLE_NODE_HEIGHT = 60
const COUPLE_NODE_WIDTH = 200
const COUPLE_NODE_HEIGHT = 70
const HORIZONTAL_GAP = 80
const VERTICAL_GAP = 20
const GENERATION_HEIGHT = 150
const NODE_MARGIN = 30
const BASE_X = 100

export function calculateTreeLayout(
  coupleNodesMap: Map<number, CoupleNode[]>,
  parentChildrenMap: Map<number, number[]>
): TreeLayout {
  const nodes = new Map<string, NodeLayout>()
  const generations: number[] = []

  coupleNodesMap.forEach((_, generation) => {
    if (!generations.includes(generation)) {
      generations.push(generation)
    }
  })
  generations.sort((a, b) => a - b)

  const generationCount = generations.length

  const memberMap = new Map<number, Member>()
  coupleNodesMap.forEach((coupleNodes) => {
    coupleNodes.forEach(cn => {
      if (cn.male) memberMap.set(cn.male.id, cn.male)
      if (cn.female) memberMap.set(cn.female.id, cn.female)
    })
  })

  function findRootCoupleNodes(): CoupleNode[] {
    const childrenOfCouples = new Set<string>()
    coupleNodesMap.forEach((coupleNodes) => {
      coupleNodes.forEach(cn => {
        cn.children.forEach(childId => childrenOfCouples.add(childId))
      })
    })

    const roots: CoupleNode[] = []
    generations.forEach(gen => {
      const nodesInGen = coupleNodesMap.get(gen) || []
      nodesInGen.forEach(cn => {
        if (!childrenOfCouples.has(cn.id)) {
          roots.push(cn)
        }
      })
    })
    return roots
  }

  const rootNodes = findRootCoupleNodes()

  let rootNodeId: string | null = null
  let rootX = 0
  let rootY = 0

  function getNodeWidth(cn: CoupleNode): number {
    return cn.isCouple ? COUPLE_NODE_WIDTH : SINGLE_NODE_WIDTH
  }

  function calculateSubtreeWidth(coupleNode: CoupleNode, processed: Set<string>): number {
    if (processed.has(coupleNode.id)) return 0
    processed.add(coupleNode.id)

    const nodeWidth = getNodeWidth(coupleNode)

    if (coupleNode.children.length === 0) {
      return nodeWidth
    }

    let totalWidth = 0
    coupleNode.children.forEach(childId => {
      const childNode = findCoupleNodeById(childId, coupleNodesMap)
      if (childNode) {
        totalWidth += calculateSubtreeWidth(childNode, processed)
      }
    })
    totalWidth += (coupleNode.children.length - 1) * HORIZONTAL_GAP
    return Math.max(totalWidth, nodeWidth)
  }

  function positionNode(coupleNode: CoupleNode, x: number, y: number): { width: number } {
    const nodeWidth = getNodeWidth(coupleNode)
    const nodeHeight = coupleNode.isCouple ? COUPLE_NODE_HEIGHT : SINGLE_NODE_HEIGHT

    nodes.set(coupleNode.id, {
      id: coupleNode.id,
      x,
      y,
      width: nodeWidth,
      height: nodeHeight,
      generation: coupleNode.generation
    })

    if (coupleNode.children.length === 0) {
      return { width: nodeWidth }
    }

    let totalChildrenWidth = 0
    const childWidths: { id: string, width: number }[] = []
    coupleNode.children.forEach(childId => {
      const childNode = findCoupleNodeById(childId, coupleNodesMap)
      if (childNode) {
        const processed = new Set<string>()
        const childWidth = calculateSubtreeWidth(childNode, processed)
        childWidths.push({ id: childId, width: childWidth })
        totalChildrenWidth += childWidth
      }
    })
    totalChildrenWidth += (coupleNode.children.length - 1) * HORIZONTAL_GAP

    const startX = x + (nodeWidth - totalChildrenWidth) / 2
    let currentX = startX

    coupleNode.children.forEach(childId => {
      const childNode = findCoupleNodeById(childId, coupleNodesMap)
      if (childNode) {
        const childLayout = positionNode(childNode, currentX, y + GENERATION_HEIGHT)
        currentX += childLayout.width + HORIZONTAL_GAP
      }
    })

    return { width: totalChildrenWidth }
  }

  function positionUnplacedNodes() {
    generations.forEach(gen => {
      const nodesInGen = coupleNodesMap.get(gen) || []

      const placedNodes: { cn: CoupleNode, layout: NodeLayout }[] = []
      const unplacedNodes: CoupleNode[] = []

      nodesInGen.forEach(cn => {
        if (nodes.has(cn.id)) {
          placedNodes.push({ cn, layout: nodes.get(cn.id)! })
        } else {
          unplacedNodes.push(cn)
        }
      })

      if (unplacedNodes.length === 0) return

      if (placedNodes.length === 0) {
        let currentX = BASE_X
        unplacedNodes.forEach(cn => {
          const nodeWidth = getNodeWidth(cn)
          const nodeHeight = cn.isCouple ? COUPLE_NODE_HEIGHT : SINGLE_NODE_HEIGHT
          nodes.set(cn.id, {
            id: cn.id,
            x: currentX,
            y: 80 + generations.indexOf(gen) * GENERATION_HEIGHT,
            width: nodeWidth,
            height: nodeHeight,
            generation: cn.generation
          })
          currentX += nodeWidth + HORIZONTAL_GAP
        })
      } else {
        const placedSorted = placedNodes.sort((a, b) => a.layout.x - b.layout.x)
        const firstPlaced = placedSorted[0]
        const lastPlaced = placedSorted[placedSorted.length - 1]

        let currentX = lastPlaced.layout.x + lastPlaced.layout.width + HORIZONTAL_GAP
        unplacedNodes.forEach(cn => {
          const nodeWidth = getNodeWidth(cn)
          const nodeHeight = cn.isCouple ? COUPLE_NODE_HEIGHT : SINGLE_NODE_HEIGHT
          nodes.set(cn.id, {
            id: cn.id,
            x: currentX,
            y: 80 + generations.indexOf(gen) * GENERATION_HEIGHT,
            width: nodeWidth,
            height: nodeHeight,
            generation: cn.generation
          })
          currentX += nodeWidth + HORIZONTAL_GAP
        })
      }
    })
  }

  if (rootNodes.length > 0) {
    const mainRoot = rootNodes[0]
    rootNodeId = mainRoot.id

    let startX = BASE_X
    rootNodes.forEach(root => {
      const processed = new Set<string>()
      const rootWidth = calculateSubtreeWidth(root, processed)
      positionNode(root, startX, 80)
      startX += rootWidth + HORIZONTAL_GAP * 2
    })

    positionUnplacedNodes()

    const rootLayout = nodes.get(rootNodeId)
    if (rootLayout) {
      rootX = rootLayout.x + rootLayout.width / 2
      rootY = rootLayout.y
    }
  }

  let maxWidth = 0
  let maxHeight = 0
  nodes.forEach(layout => {
    maxWidth = Math.max(maxWidth, layout.x + layout.width + NODE_MARGIN)
    maxHeight = Math.max(maxHeight, layout.y + layout.height + NODE_MARGIN)
  })

  return {
    nodes,
    generationCount,
    maxWidth,
    maxHeight,
    generations,
    rootNodeId,
    rootX,
    rootY
  }
}

function findCoupleNodeById(id: string, coupleNodesMap: Map<number, CoupleNode[]>): CoupleNode | null {
  for (const nodes of coupleNodesMap.values()) {
    const found = nodes.find(n => n.id === id)
    if (found) return found
  }
  return null
}

export function drawGenerationLines(
  ctx: CanvasRenderingContext2D,
  layout: TreeLayout,
  canvasWidth: number,
  canvasHeight: number
) {
  // Generation lines removed per user request
}

export function drawConnectionLines(
  ctx: CanvasRenderingContext2D,
  layout: TreeLayout,
  coupleNodesMap: Map<number, CoupleNode[]>
) {
  ctx.save()
  ctx.strokeStyle = '#5D4E37'
  ctx.lineWidth = 2
  ctx.lineCap = 'round'

  coupleNodesMap.forEach((coupleNodes) => {
    coupleNodes.forEach(coupleNode => {
      const parentLayout = layout.nodes.get(coupleNode.id)
      if (!parentLayout) return

      const parentCenterX = parentLayout.x + parentLayout.width / 2
      const parentBottomY = parentLayout.y + parentLayout.height

      coupleNode.children.forEach(childId => {
        const childLayout = layout.nodes.get(childId)
        if (!childLayout) return

        const childCenterX = childLayout.x + childLayout.width / 2
        const childTopY = childLayout.y

        ctx.beginPath()
        ctx.moveTo(parentCenterX, parentBottomY)
        ctx.lineTo(parentCenterX, parentBottomY + 30)
        ctx.lineTo(childCenterX, childTopY - 30)
        ctx.lineTo(childCenterX, childTopY)
        ctx.stroke()
      })
    })
  })

  ctx.restore()
}

export function drawTree(
  ctx: CanvasRenderingContext2D,
  layout: TreeLayout,
  coupleNodesMap: Map<number, CoupleNode[]>,
  options: {
    onNodeClick?: (memberId: number) => void
    onNodeDbClick?: (memberId: number) => void
    selectedId?: number | null
  }
) {
  coupleNodesMap.forEach((coupleNodes) => {
    coupleNodes.forEach(coupleNode => {
      const nodeLayout = layout.nodes.get(coupleNode.id)
      if (nodeLayout) {
        const isSelected = options.selectedId !== undefined && options.selectedId !== null && (
          (coupleNode.male && coupleNode.male.id === options.selectedId) ||
          (coupleNode.female && coupleNode.female.id === options.selectedId)
        )
        drawCoupleNode(ctx, coupleNode, nodeLayout, isSelected)
      }
    })
  })
}

export function drawCoupleNode(
  ctx: CanvasRenderingContext2D,
  coupleNode: CoupleNode,
  layout: NodeLayout,
  isSelected: boolean
) {
  const { x, y, width, height } = layout

  ctx.save()

  const gradient = ctx.createLinearGradient(x, y, x + width, y + height)

  if (coupleNode.isCouple) {
    gradient.addColorStop(0, '#5D4E37')
    gradient.addColorStop(1, '#4A3E2B')
  } else {
    if (coupleNode.male) {
      gradient.addColorStop(0, '#3D5A80')
      gradient.addColorStop(1, '#2D4A70')
    } else if (coupleNode.female) {
      gradient.addColorStop(0, '#8B4A4A')
      gradient.addColorStop(1, '#7A3D3D')
    } else {
      gradient.addColorStop(0, '#666666')
      gradient.addColorStop(1, '#555555')
    }
  }

  const radius = 8
  ctx.beginPath()
  ctx.moveTo(x + radius, y)
  ctx.lineTo(x + width - radius, y)
  ctx.quadraticCurveTo(x + width, y, x + width, y + radius)
  ctx.lineTo(x + width, y + height - radius)
  ctx.quadraticCurveTo(x + width, y + height, x + width - radius, y + height)
  ctx.lineTo(x + radius, y + height)
  ctx.quadraticCurveTo(x, y + height, x, y + height - radius)
  ctx.lineTo(x, y + radius)
  ctx.quadraticCurveTo(x, y, x + radius, y)
  ctx.closePath()

  ctx.fillStyle = gradient
  ctx.shadowColor = 'rgba(0, 0, 0, 0.3)'
  ctx.shadowBlur = 6
  ctx.shadowOffsetY = 3
  ctx.fill()
  ctx.shadowColor = 'transparent'

  if (isSelected) {
    ctx.strokeStyle = '#C9A962'
    ctx.lineWidth = 3
    ctx.stroke()
  }

  ctx.beginPath()
  ctx.moveTo(x + radius * 2, y + 2)
  ctx.lineTo(x + width - radius * 2, y + 2)
  ctx.strokeStyle = 'rgba(255, 255, 255, 0.2)'
  ctx.lineWidth = 1
  ctx.stroke()

  if (coupleNode.isCouple && coupleNode.male && coupleNode.female) {
    const leftNameX = x + 50
    const rightNameX = x + width - 50
    const nameY = y + height / 2

    ctx.fillStyle = '#FFFFFF'
    ctx.font = 'bold 12px "Noto Serif SC", serif'
    ctx.textAlign = 'center'
    ctx.textBaseline = 'middle'
    ctx.fillText(coupleNode.male.name, leftNameX, nameY)

    ctx.fillText(coupleNode.female.name, rightNameX, nameY)

    ctx.fillStyle = '#FF6B6B'
    ctx.font = '16px serif'
    ctx.fillText('♥', x + width / 2, nameY)

    ctx.fillStyle = '#C9A962'
    ctx.font = 'bold 10px "Noto Sans SC", sans-serif'
    ctx.textAlign = 'left'
    ctx.fillText(`第${coupleNode.generation}代`, x + 6, y + 12)
  } else if (coupleNode.male) {
    ctx.fillStyle = '#C9A962'
    ctx.font = 'bold 10px "Noto Sans SC", sans-serif'
    ctx.textAlign = 'left'
    ctx.fillText(`第${coupleNode.male.generation}代`, x + 6, y + 12)

    ctx.fillStyle = '#FFFFFF'
    ctx.font = 'bold 14px "Noto Serif SC", serif'
    ctx.textAlign = 'center'
    ctx.textBaseline = 'middle'
    ctx.fillText(coupleNode.male.name, x + width / 2, y + height / 2 + 5)
  } else if (coupleNode.female) {
    ctx.fillStyle = '#C9A962'
    ctx.font = 'bold 10px "Noto Sans SC", sans-serif'
    ctx.textAlign = 'left'
    ctx.fillText(`第${coupleNode.female.generation}代`, x + 6, y + 12)

    ctx.fillStyle = '#FFFFFF'
    ctx.font = 'bold 14px "Noto Serif SC", serif'
    ctx.textAlign = 'center'
    ctx.textBaseline = 'middle'
    ctx.fillText(coupleNode.female.name, x + width / 2, y + height / 2 + 5)
  }

  ctx.restore()
}

export function isPointInNode(
  x: number,
  y: number,
  layout: NodeLayout
): boolean {
  return (
    x >= layout.x &&
    x <= layout.x + layout.width &&
    y >= layout.y &&
    y <= layout.y + layout.height
  )
}

export function findNodeAtPoint(
  pointX: number,
  pointY: number,
  layout: TreeLayout,
  coupleNodesMap: Map<number, CoupleNode[]>
): CoupleNode | null {
  for (const nodes of coupleNodesMap.values()) {
    for (const coupleNode of nodes) {
      const nodeLayout = layout.nodes.get(coupleNode.id)
      if (nodeLayout && isPointInNode(pointX, pointY, nodeLayout)) {
        return coupleNode
      }
    }
  }
  return null
}
