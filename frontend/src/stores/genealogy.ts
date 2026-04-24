import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { GenealogyNode, Member } from '@/types/api'
import { getGenealogyTree, getMemberDetail } from '@/api/member'

export const useGenealogyStore = defineStore('genealogy', () => {
  // 状态
  const treeData = ref<GenealogyNode[]>([])
  const selectedMemberId = ref<number | null>(null)
  const selectedMember = ref<Member | null>(null)
  const loading = ref(false)
  const scale = ref(1)
  const offsetX = ref(0)
  const offsetY = ref(0)

  // 计算属性
  const hasTreeData = computed(() => treeData.value.length > 0)

  // 方法
  async function fetchTreeData(familyId?: number) {
    loading.value = true
    try {
      const res = await getGenealogyTree(familyId)
      treeData.value = res.data || []
    } catch (error) {
      console.error('获取族谱树失败', error)
      treeData.value = []
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

  function setScale(newScale: number) {
    scale.value = Math.max(0.2, Math.min(2, newScale))
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
    selectedMemberId,
    selectedMember,
    loading,
    scale,
    offsetX,
    offsetY,
    hasTreeData,
    fetchTreeData,
    fetchMemberDetail,
    selectMember,
    setScale,
    setOffset,
    resetView,
    clearSelection
  }
})
