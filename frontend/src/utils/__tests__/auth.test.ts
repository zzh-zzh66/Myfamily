import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { getToken, setToken, removeToken, isAuthenticated } from '../auth'

describe('Token Management', () => {
  beforeEach(() => {
    localStorage.clear()
  })

  afterEach(() => {
    localStorage.clear()
  })

  describe('getToken', () => {
    it('should return null when no token exists', () => {
      expect(getToken()).toBeNull()
    })

    it('should return token when it exists', () => {
      localStorage.setItem('token', 'test-token-123')
      expect(getToken()).toBe('test-token-123')
    })
  })

  describe('setToken', () => {
    it('should set token in localStorage', () => {
      setToken('new-token')
      expect(localStorage.getItem('token')).toBe('new-token')
    })
  })

  describe('removeToken', () => {
    it('should remove token from localStorage', () => {
      localStorage.setItem('token', 'test-token')
      removeToken()
      expect(localStorage.getItem('token')).toBeNull()
    })
  })

  describe('isAuthenticated', () => {
    it('should return false when no token exists', () => {
      expect(isAuthenticated()).toBe(false)
    })

    it('should return true when token exists', () => {
      localStorage.setItem('token', 'valid-token')
      expect(isAuthenticated()).toBe(true)
    })

    it('should return false for empty token', () => {
      localStorage.setItem('token', '')
      expect(isAuthenticated()).toBe(false)
    })
  })
})
