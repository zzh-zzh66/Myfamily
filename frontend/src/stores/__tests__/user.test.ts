import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useUserStore } from '../user'

vi.mock('@/api/auth', () => ({
  getCurrentUser: vi.fn(),
  logout: vi.fn()
}))

vi.mock('@/router', () => ({
  default: {
    push: vi.fn()
  }
}))

describe('User Store', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    localStorage.clear()
  })

  afterEach(() => {
    localStorage.clear()
  })

  describe('Initial State', () => {
    it('should have correct initial values', () => {
      const userStore = useUserStore()

      expect(userStore.userInfo).toBeNull()
      expect(userStore.token).toBeNull()
      expect(userStore.loading).toBe(false)
      expect(userStore.isLoggedIn).toBe(false)
    })

    it('should read token from localStorage on init', () => {
      localStorage.setItem('token', 'test-token')
      const userStore = useUserStore()

      expect(userStore.token).toBe('test-token')
      expect(userStore.isLoggedIn).toBe(true)
    })
  })

  describe('Computed Properties', () => {
    it('should compute userName correctly', () => {
      const userStore = useUserStore()
      userStore.userInfo = { id: 1, username: 'testuser' } as any

      expect(userStore.userName).toBe('testuser')
    })

    it('should compute userAvatar correctly', () => {
      const userStore = useUserStore()
      userStore.userInfo = { id: 1, username: 'test', avatar: 'avatar.png' } as any

      expect(userStore.userAvatar).toBe('avatar.png')
    })

    it('should compute userId correctly', () => {
      const userStore = useUserStore()
      userStore.userInfo = { id: 42, username: 'test' } as any

      expect(userStore.userId).toBe(42)
    })

    it('should return empty string for userName when no userInfo', () => {
      const userStore = useUserStore()
      userStore.userInfo = null

      expect(userStore.userName).toBe('')
    })
  })

  describe('Actions', () => {
    it('should set token correctly', () => {
      const userStore = useUserStore()

      userStore.setToken('new-token')

      expect(userStore.token).toBe('new-token')
      expect(localStorage.getItem('token')).toBe('new-token')
    })

    it('should set userInfo correctly', () => {
      const userStore = useUserStore()
      const userInfo = { id: 1, username: 'testuser', email: 'test@example.com' }

      userStore.setUserInfo(userInfo as any)

      expect(userStore.userInfo).toEqual(userInfo)
    })

    it('should login and set both token and userInfo', () => {
      const userStore = useUserStore()
      const user = { id: 1, username: 'testuser' } as any

      userStore.login('token-123', user)

      expect(userStore.token).toBe('token-123')
      expect(userStore.userInfo).toEqual(user)
      expect(localStorage.getItem('token')).toBe('token-123')
    })
  })

  describe('Token Management', () => {
    it('should clear token and userInfo on logout', async () => {
      const userStore = useUserStore()
      userStore.token = 'test-token'
      userStore.userInfo = { id: 1, username: 'test' } as any

      await userStore.logout()

      expect(userStore.token).toBeNull()
      expect(userStore.userInfo).toBeNull()
      expect(localStorage.getItem('token')).toBeNull()
    })
  })
})
