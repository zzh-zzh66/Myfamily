import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { GenealogyNode, Member, CoupleNode } from '@/types/api'
import { getGenealogyTree, getMemberDetail } from '@/api/member'
import { transformToGenealogyTree, transformToCoupleNodes, buildParentChildrenMap, normalizeGender } from '@/utils/genealogyTree'

export interface FilterParams {
  name?: string
  generations?: number[]
  gender?: 'male' | 'female' | ''
  isMarried?: boolean
  isAlive?: boolean
}

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

  // 筛选状态
  const filterParams = ref<FilterParams>({})
  const filteredMemberIds = ref<Set<number>>(new Set())
  const isFilterActive = ref(false)

  // 计算属性
  const hasTreeData = computed(() => treeData.value.length > 0)

  // 方法
  async function fetchTreeData(familyId?: number) {
    console.log('[GenealogyStore] 开始获取族谱数据')
    loading.value = true
    try {
      const res = await getGenealogyTree(familyId)
      console.log('[GenealogyStore] 获取到数据:', res.data?.length, '个成员')
      flatMembers.value = (res.data || []).map(m => ({
        ...m,
        gender: normalizeGender(m.gender)
      }))
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

  function applyFilter(params: FilterParams) {
    filterParams.value = params
    const matchedIds = new Set<number>()

    flatMembers.value.forEach(member => {
      let isMatch = true

      if (params.name && params.name.trim()) {
        isMatch = isMatch && member.name.toLowerCase().includes(params.name.toLowerCase())
      }

      if (params.generations && params.generations.length > 0) {
        isMatch = isMatch && params.generations.includes(member.generation)
      }

      if (params.gender) {
        isMatch = isMatch && member.gender === params.gender
      }

      if (params.isMarried !== undefined) {
        const hasSpouse = !!member.spouseId
        isMatch = isMatch && (hasSpouse === params.isMarried)
      }

      if (params.isAlive !== undefined) {
        const isMemberAlive = !member.deathDate
        isMatch = isMatch && (isMemberAlive === params.isAlive)
      }

      if (isMatch) {
        matchedIds.add(member.id)
      }
    })

    filteredMemberIds.value = matchedIds
    isFilterActive.value = matchedIds.size > 0 || Object.keys(params).length > 0
  }

  function clearFilter() {
    filterParams.value = {}
    filteredMemberIds.value = new Set()
    isFilterActive.value = false
  }

  function searchMembersByName(keyword: string): Member[] {
    if (!keyword || !keyword.trim()) return []
    const lowerKeyword = keyword.toLowerCase()
    return flatMembers.value
      .filter(m => m.name.toLowerCase().includes(lowerKeyword))
      .slice(0, 10)
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
    filterParams,
    filteredMemberIds,
    isFilterActive,
    fetchTreeData,
    fetchMemberDetail,
    selectMember,
    setScale,
    setHoverPoint,
    setOffset,
    resetView,
    clearSelection,
    applyFilter,
    clearFilter,
    searchMembersByName
  }
})
