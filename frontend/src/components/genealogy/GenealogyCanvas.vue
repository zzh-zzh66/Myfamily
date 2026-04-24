<template>
  <div class="genealogy-canvas-wrapper">
    <canvas ref="canvasRef" class="genealogy-canvas"></canvas>
    <div class="canvas-controls">
      <slot name="controls" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch } from 'vue'
import type { GenealogyNode } from '@/types/api'
import {
  drawTree,
  drawConnectionLines,
  calculateTreeLayout,
  TreeLayout
} from '@/utils/genealogyCanvas'

const props = defineProps<{
  data: GenealogyNode[]
  scale?: number
  offsetX?: number
  offsetY?: number
  selectedId?: number | null
}>()

const emit = defineEmits<{
  (e: 'node-click', node: GenealogyNode): void
  (e: 'node-dbclick', node: GenealogyNode): void
}>()

const canvasRef = ref<HTMLCanvasElement>()
const layout = ref<TreeLayout | null>(null)

function render() {
  const canvas = canvasRef.value
  if (!canvas) return

  const ctx = canvas.getContext('2d')
  if (!ctx) return

  // 设置画布大小
  const parent = canvas.parentElement
  if (parent) {
    canvas.width = parent.clientWidth
    canvas.height = parent.clientHeight
  }

  // 清空画布
  ctx.clearRect(0, 0, canvas.width, canvas.height)

  // 保存上下文状态
  ctx.save()

  // 应用缩放和偏移
  const scale = props.scale || 1
  const offsetX = props.offsetX || 0
  const offsetY = props.offsetY || 0
  ctx.translate(canvas.width / 2 + offsetX, 50 + offsetY)
  ctx.scale(scale, scale)

  // 计算树布局
  layout.value = calculateTreeLayout(props.data)

  // 绘制连接线
  if (layout.value) {
    drawConnectionLines(ctx, layout.value)
  }

  // 绘制节点
  if (layout.value) {
    drawTree(ctx, layout.value, props.data, {
      onNodeClick: (node) => emit('node-click', node),
      onNodeDbClick: (node) => emit('node-dbclick', node),
      selectedId: props.selectedId
    })
  }

  // 恢复上下文状态
  ctx.restore()
}

// 监听数据变化
watch(() => props.data, render, { deep: true })
watch(() => props.scale, render)
watch(() => props.offsetX, render)
watch(() => props.offsetY, render)
watch(() => props.selectedId, render)

// 处理点击事件
function handleCanvasClick(e: MouseEvent) {
  if (!layout.value || !canvasRef.value) return

  const rect = canvasRef.value.getBoundingClientRect()
  const x = e.clientX - rect.left
  const y = e.clientY - rect.top

  // 计算实际坐标（考虑缩放和偏移）
  const scale = props.scale || 1
  const offsetX = props.offsetX || 0
  const offsetY = props.offsetY || 0
  const actualX = (x - canvasRef.value.width / 2 - offsetX) / scale
  const actualY = (y - 50 - offsetY) / scale

  // 遍历节点检测点击
  for (const node of props.data) {
    const nodeLayout = layout.value.nodes.get(node.id)
    if (nodeLayout && isPointInNode(actualX, actualY, nodeLayout)) {
      if (e.detail === 2) {
        emit('node-dbclick', node)
      } else {
        emit('node-click', node)
      }
      return
    }
  }
}

function isPointInNode(
  x: number,
  y: number,
  layout: { x: number; y: number; width: number; height: number }
): boolean {
  return (
    x >= layout.x - layout.width / 2 &&
    x <= layout.x + layout.width / 2 &&
    y >= layout.y - layout.height / 2 &&
    y <= layout.y + layout.height / 2
  )
}

// 窗口大小变化时重新渲染
function handleResize() {
  render()
}

onMounted(() => {
  render()
  canvasRef.value?.addEventListener('click', handleCanvasClick)
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  canvasRef.value?.removeEventListener('click', handleCanvasClick)
  window.removeEventListener('resize', handleResize)
})
</script>

<style lang="scss" scoped>
@import '@/assets/styles/variables.scss';

.genealogy-canvas-wrapper {
  position: relative;
  width: 100%;
  height: 100%;
}

.genealogy-canvas {
  display: block;
  width: 100%;
  height: 100%;
  cursor: grab;

  &:active {
    cursor: grabbing;
  }
}

.canvas-controls {
  position: absolute;
  bottom: $spacing-md;
  right: $spacing-md;
  display: flex;
  gap: $spacing-sm;
}
</style>
