import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { User } from '@/types/api'
import { getCurrentUser, logout as logoutApi } from '@/api/auth'
import router from '@/router'

export const useUserStore = defineStore('user', () => {
  // 状态
  const userInfo = ref<User | null>(null)
  const token = ref<string | null>(localStorage.getItem('token'))
  const loading = ref(false)

  // 计算属性
  const isLoggedIn = computed(() => !!token.value)
  const userName = computed(() => userInfo.value?.username || '')
  const userAvatar = computed(() => userInfo.value?.avatar || '')
  const userId = computed(() => userInfo.value?.id || 0)

  // 方法
  async function fetchUserInfo() {
    if (!token.value) return

    loading.value = true
    try {
      const res = await getCurrentUser()
      userInfo.value = res.data
    } catch (error) {
      console.error('获取用户信息失败', error)
      logout()
    } finally {
      loading.value = false
    }
  }

  function setToken(newToken: string) {
    token.value = newToken
    localStorage.setItem('token', newToken)
  }

  function setUserInfo(info: User) {
    userInfo.value = info
  }

  async function logout() {
    try {
      await logoutApi()
    } catch (error) {
      // 忽略错误
    } finally {
      token.value = null
      userInfo.value = null
      localStorage.removeItem('token')
      router.push({ name: 'Login' })
    }
  }

  function login(tokenStr: string, user: User) {
    setToken(tokenStr)
    userInfo.value = user
  }

  return {
    userInfo,
    token,
    loading,
    isLoggedIn,
    userName,
    userAvatar,
    userId,
    fetchUserInfo,
    setToken,
    setUserInfo,
    logout,
    login
  }
})
