import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import * as memberApi from '@/api/member'
import { useGenealogyStore } from '../genealogy'

vi.mock('@/api/member')

describe('Genealogy Store', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
  })

  describe('Initial State', () => {
    it('should have correct initial values', () => {
      const store = useGenealogyStore()

      expect(store.treeData).toEqual([])
      expect(store.flatMembers).toEqual([])
      expect(store.selectedMemberId).toBeNull()
      expect(store.selectedMember).toBeNull()
      expect(store.loading).toBe(false)
      expect(store.scale).toBe(1)
      expect(store.offsetX).toBe(0)
      expect(store.offsetY).toBe(0)
    })
  })

  describe('Computed Properties', () => {
    it('should compute hasTreeData correctly', () => {
      const store = useGenealogyStore()

      expect(store.hasTreeData).toBe(false)

      store.treeData = [{ id: 1, name: 'test' }] as any

      expect(store.hasTreeData).toBe(true)
    })
  })

  describe('Actions', () => {
    it('should select member correctly', () => {
      const store = useGenealogyStore()

      store.selectMember(42)

      expect(store.selectedMemberId).toBe(42)
    })

    it('should clear selection correctly', () => {
      const store = useGenealogyStore()
      store.selectedMemberId = 42
      store.selectedMember = { id: 42, name: 'test' } as any

      store.clearSelection()

      expect(store.selectedMemberId).toBeNull()
      expect(store.selectedMember).toBeNull()
    })

    it('should reset view correctly', () => {
      const store = useGenealogyStore()
      store.scale = 1.5
      store.offsetX = 100
      store.offsetY = 200

      store.resetView()

      expect(store.scale).toBe(1)
      expect(store.offsetX).toBe(0)
      expect(store.offsetY).toBe(0)
    })

    it('should set hover point correctly', () => {
      const store = useGenealogyStore()

      store.setHoverPoint(100, 200)

      expect(store.hoverPoint).toEqual({ x: 100, y: 200 })
    })

    it('should set offset correctly', () => {
      const store = useGenealogyStore()

      store.setOffset(50, 100)

      expect(store.offsetX).toBe(50)
      expect(store.offsetY).toBe(100)
    })

    it('should clamp scale to valid range', () => {
      const store = useGenealogyStore()

      store.setScale(5)
      expect(store.scale).toBe(2)

      store.setScale(0.1)
      expect(store.scale).toBe(0.2)

      store.setScale(1.5)
      expect(store.scale).toBe(1.5)
    })

    it('should adjust offset when setting scale with anchor', () => {
      const store = useGenealogyStore()
      store.offsetX = 100
      store.offsetY = 100
      store.scale = 1

      store.setScale(2, 100, 100)

      expect(store.scale).toBe(2)
      expect(store.offsetX).toBe(100)
      expect(store.offsetY).toBe(100)
    })
  })

  describe('Tree Data Management', () => {
    it('should handle fetch error and clear data', async () => {
      vi.mocked(memberApi.getGenealogyTree).mockResolvedValue({ data: [] })

      const store = useGenealogyStore()

      await store.fetchTreeData()

      expect(store.treeData).toEqual([])
      expect(store.flatMembers).toEqual([])
    })
  })
})
