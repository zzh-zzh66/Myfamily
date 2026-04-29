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
import type { CoupleNode } from '@/types/api'
import {
  calculateTreeLayout,
  drawGenerationLines,
  drawConnectionLines,
  drawTree,
  findNodeAtPoint,
  TreeLayout
} from '@/utils/genealogyCanvas'

const props = defineProps<{
  coupleNodesMap: Map<number, CoupleNode[]>
  parentChildrenMap: Map<number, number[]>
  scale?: number
  offsetX?: number
  offsetY?: number
  selectedId?: number | null
}>()

const emit = defineEmits<{
  (e: 'node-click', memberId: number): void
  (e: 'node-dbclick', memberId: number): void
}>()

const canvasRef = ref<HTMLCanvasElement>()
const layout = ref<TreeLayout | null>(null)
const initialized = ref(false)

function render() {
  const canvas = canvasRef.value
  if (!canvas) return

  const ctx = canvas.getContext('2d')
  if (!ctx) return

  const parent = canvas.parentElement
  if (parent) {
    canvas.width = parent.clientWidth
    canvas.height = parent.clientHeight
  }

  ctx.clearRect(0, 0, canvas.width, canvas.height)

  ctx.save()

  const scale = props.scale || 1
  const offsetX = props.offsetX || 0
  const offsetY = props.offsetY || 0

  ctx.translate(offsetX, offsetY)
  ctx.scale(scale, scale)

  layout.value = calculateTreeLayout(props.coupleNodesMap, props.parentChildrenMap)

  if (layout.value && !initialized.value) {
    initialized.value = true
  }

  if (layout.value) {
    // drawGenerationLines - removed per user request
    drawConnectionLines(ctx, layout.value, props.coupleNodesMap)
    drawTree(ctx, layout.value, props.coupleNodesMap, {
      onNodeClick: (memberId) => emit('node-click', memberId),
      onNodeDbClick: (memberId) => emit('node-dbclick', memberId),
      selectedId: props.selectedId
    })
  }

  ctx.restore()
}

watch(() => props.coupleNodesMap, () => {
  initialized.value = false
  render()
}, { deep: true })
watch(() => props.parentChildrenMap, () => {
  initialized.value = false
  render()
}, { deep: true })
watch(() => props.scale, render)
watch(() => props.offsetX, render)
watch(() => props.offsetY, render)
watch(() => props.selectedId, render)

function handleCanvasClick(e: MouseEvent) {
  if (!layout.value || !canvasRef.value) return

  const rect = canvasRef.value.getBoundingClientRect()
  const clickX = e.clientX - rect.left
  const clickY = e.clientY - rect.top

  const scale = props.scale || 1
  const offsetX = props.offsetX || 0
  const offsetY = props.offsetY || 0

  const actualX = (clickX - offsetX) / scale
  const actualY = (clickY - offsetY) / scale

  const coupleNode = findNodeAtPoint(actualX, actualY, layout.value, props.coupleNodesMap)

  if (coupleNode) {
    const memberId = coupleNode.male?.id || coupleNode.female?.id
    if (memberId) {
      if (e.detail === 2) {
        emit('node-dbclick', memberId)
      } else {
        emit('node-click', memberId)
      }
    }
  }
}

function handleResize() {
  initialized.value = false
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
