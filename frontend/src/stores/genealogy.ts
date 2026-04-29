import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { GenealogyNode, Member, CoupleNode } from '@/types/api'
import { getGenealogyTree, getMemberDetail } from '@/api/member'
import { transformToGenealogyTree, transformToCoupleNodes, buildParentChildrenMap } from '@/utils/genealogyTree'

export const useGenealogyStore = defineStore('genealogy', () => {
  // 状态
  const treeData = ref<GenealogyNode[]>([])
  const coupleNodesMap = ref<Map<number, CoupleNode[]>>(new Map())
  const parentChildrenMap = ref<Map<number, number[]>>(new Map())
  const flatMembers = ref<Member[]>([])
  const selectedMemberId = ref<number | null>(null)
  const selectedMember = ref<Member | null>(null)
  const loading = ref(false)
  const scale = ref(1)
  const offsetX = ref(0)
  const offsetY = ref(0)
  const hoverPoint = ref({ x: 0, y: 0 })

  // 计算属性
  const hasTreeData = computed(() => treeData.value.length > 0)

  // 方法
  async function fetchTreeData(familyId?: number) {
    console.log('[GenealogyStore] 开始获取族谱数据')
    loading.value = true
    try {
      const res = await getGenealogyTree(familyId)
      console.log('[GenealogyStore] 获取到数据:', res.data?.length, '个成员')
      flatMembers.value = res.data || []
      console.log('[GenealogyStore] flatMembers 已更新:', flatMembers.value.length)

      treeData.value = transformToGenealogyTree(flatMembers.value)
      coupleNodesMap.value = transformToCoupleNodes(flatMembers.value)
      parentChildrenMap.value = buildParentChildrenMap(flatMembers.value)
      console.log('[GenealogyStore] coupleNodesMap 已更新，generation数:', coupleNodesMap.value.size)
    } catch (error) {
      console.error('获取族谱树失败', error)
      treeData.value = []
      coupleNodesMap.value = new Map()
      parentChildrenMap.value = new Map()
    } finally {
      loading.value = false
    }
  }

  async function fetchMemberDetail(id: number) {
    loading.value = true
    try {
      const res = await getMemberDetail(id)
      selectedMember.value = res.data
      selectedMemberId.value = id
    } catch (error) {
      console.error('获取成员详情失败', error)
      selectedMember.value = null
    } finally {
      loading.value = false
    }
  }

  function selectMember(id: number | null) {
    selectedMemberId.value = id
  }

  function setScale(newScale: number, anchorX?: number, anchorY?: number) {
    const oldScale = scale.value
    const newScaleClamped = Math.max(0.2, Math.min(2, newScale))

    if (anchorX !== undefined && anchorY !== undefined) {
      const ratio = newScaleClamped / oldScale
      offsetX.value = anchorX - ratio * (anchorX - offsetX.value)
      offsetY.value = anchorY - ratio * (anchorY - offsetY.value)
    }

    scale.value = newScaleClamped
  }

  function setHoverPoint(x: number, y: number) {
    hoverPoint.value = { x, y }
  }

  function setOffset(x: number, y: number) {
    offsetX.value = x
    offsetY.value = y
  }

  function resetView() {
    scale.value = 1
    offsetX.value = 0
    offsetY.value = 0
  }

  function clearSelection() {
    selectedMemberId.value = null
    selectedMember.value = null
  }

  return {
    treeData,
    coupleNodesMap,
    parentChildrenMap,
    flatMembers,
    selectedMemberId,
    selectedMember,
    loading,
    scale,
    offsetX,
    offsetY,
    hoverPoint,
    hasTreeData,
    fetchTreeData,
    fetchMemberDetail,
    selectMember,
    setScale,
    setHoverPoint,
    setOffset,
    resetView,
    clearSelection
  }
})
