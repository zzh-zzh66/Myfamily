import type { Member, GenealogyNode, CoupleNode } from '@/types/api'

export function normalizeGender(gender: string): 'male' | 'female' {
  if (!gender) return 'male'
  const g = gender.toLowerCase()
  if (g === 'male' || g === 'm') return 'male'
  return 'female'
}

function debugLog(msg: string, data?: any) {
  console.log(`[GenealogyTree] ${msg}`, data ? JSON.stringify(data, null, 2) : '')
}

/**
 * 将 Member[] 转换为按辈分分组的夫妻组合节点
 */
export function transformToCoupleNodes(members: Member[]): Map<number, CoupleNode[]> {
  if (!members || members.length === 0) return new Map()

  const memberMap = new Map<number, Member>()
  members.forEach(m => memberMap.set(m.id, m))

  const coupleNodesMap = new Map<number, CoupleNode[]>()
  const processedMembers = new Set<number>()

  members.forEach(member => {
    if (processedMembers.has(member.id)) return

    const generation = member.generation
    if (!coupleNodesMap.has(generation)) {
      coupleNodesMap.set(generation, [])
    }

    if (member.spouseId) {
      const spouse = memberMap.get(member.spouseId)
      if (spouse && spouse.generation === member.generation) {
        processedMembers.add(member.id)
        processedMembers.add(spouse.id)

        const memberGender = normalizeGender(member.gender)
        const spouseGender = normalizeGender(spouse.gender)

        const coupleNode: CoupleNode = {
          id: `${member.id}-${spouse.id}`,
          male: memberGender === 'male' ? member : spouse,
          female: spouseGender === 'female' ? spouse : member,
          isCouple: true,
          generation: member.generation,
          children: []
        }
        coupleNodesMap.get(generation)!.push(coupleNode)
        debugLog(`Created coupleNode: ${coupleNode.id}`, { male: coupleNode.male?.name, female: coupleNode.female?.name, generation: coupleNode.generation })
      } else {
        const singleNode = createSingleNode(member)
        coupleNodesMap.get(generation)!.push(singleNode)
        processedMembers.add(member.id)
        debugLog(`Created singleNode (spouse not same gen): ${singleNode.id}`, { name: member.name, spouseId: member.spouseId, spouseGen: spouse?.generation })
      }
    } else {
      const singleNode = createSingleNode(member)
      coupleNodesMap.get(generation)!.push(singleNode)
      processedMembers.add(member.id)
      debugLog(`Created singleNode: ${singleNode.id}`, { name: member.name, generation })
    }
  })

  debugLog('=== Assigning children ===')

  coupleNodesMap.forEach((nodes) => {
    nodes.forEach(node => {
      if (node.male && node.isCouple) {
        const parentGen = node.generation
        const children = members.filter(m =>
          ((m.fatherId === node.male!.id) || (m.motherId === node.male!.id)) &&
          m.generation === parentGen + 1
        )
        node.children = children.map(c => getCoupleNodeId(c, memberMap)).filter(Boolean) as string[]
      } else if (node.male && !node.isCouple) {
        const parentGen = node.generation
        const children = members.filter(m =>
          ((m.fatherId === node.male!.id) || (m.motherId === node.male!.id)) &&
          m.generation === parentGen + 1
        )
        node.children = children.map(c => getCoupleNodeId(c, memberMap)).filter(Boolean) as string[]
      }
    })
  })

  debugLog('=== Final coupleNodesMap ===')
  coupleNodesMap.forEach((nodes, gen) => {
    debugLog(`Generation ${gen}:`, nodes.map(n => ({
      id: n.id,
      isCouple: n.isCouple,
      children: n.children
    })))
  })

  return coupleNodesMap
}

function createSingleNode(member: Member): CoupleNode {
  const gender = normalizeGender(member.gender)
  return {
    id: `single-${member.id}`,
    isCouple: false,
    generation: member.generation,
    children: [],
    ...(gender === 'male' ? { male: member } : { female: member })
  }
}

function getCoupleNodeId(member: Member, memberMap: Map<number, Member>): string | null {
  if (member.spouseId) {
    const spouse = memberMap.get(member.spouseId)
    if (spouse && spouse.generation === member.generation) {
      const memberGender = normalizeGender(member.gender)
      return memberGender === 'male'
        ? `${member.id}-${spouse.id}`
        : `${spouse.id}-${member.id}`
    }
  }
  return `single-${member.id}`
}

/**
 * 构建父子关系映射（基于父亲ID和母亲ID）
 */
export function buildParentChildrenMap(members: Member[]): Map<number, number[]> {
  const map = new Map<number, number[]>()

  members.forEach(member => {
    if (member.fatherId) {
      const children = map.get(member.fatherId) || []
      if (!children.includes(member.id)) {
        children.push(member.id)
      }
      map.set(member.fatherId, children)
    }
    if (member.motherId) {
      const children = map.get(member.motherId) || []
      if (!children.includes(member.id)) {
        children.push(member.id)
      }
      map.set(member.motherId, children)
    }
  })

  return map
}

/**
 * 将扁平的 Member[] 数据转换为树结构的 GenealogyNode[]
 */
export function transformToGenealogyTree(members: Member[]): GenealogyNode[] {
  if (!members || members.length === 0) return []

  const memberMap = new Map<number, GenealogyNode>()
  const childrenMap = new Map<number, GenealogyNode[]>()

  members.forEach(member => {
    const node: GenealogyNode = {
      id: member.id,
      name: member.name,
      gender: normalizeGender(member.gender),
      avatar: member.avatar,
      generation: member.generation,
      spouseId: member.spouseId,
      spouseName: member.spouse,
      children: []
    }
    memberMap.set(member.id, node)

    const fatherId = member.fatherId
    if (fatherId) {
      const siblings = childrenMap.get(fatherId) || []
      siblings.push(node)
      childrenMap.set(fatherId, siblings)
    }
  })

  memberMap.forEach(node => {
    const children = childrenMap.get(node.id) || []
    node.children = children
  })

  const rootNodes = members
    .filter(m => !m.fatherId && !m.motherId)
    .map(m => memberMap.get(m.id)!)
    .filter(Boolean)

  return rootNodes
}
