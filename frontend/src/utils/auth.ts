import router from '@/router'
import { useUserStore } from '@/stores/user'

// Token相关
const TOKEN_KEY = 'token'

export function getToken(): string | null {
  return localStorage.getItem(TOKEN_KEY)
}

export function setToken(token: string): void {
  localStorage.setItem(TOKEN_KEY, token)
}

export function removeToken(): void {
  localStorage.removeItem(TOKEN_KEY)
}

// 认证检查
export function isAuthenticated(): boolean {
  return !!getToken()
}

// 需要登录的路由守卫
export function requireAuth(to: string): void {
  if (!isAuthenticated()) {
    router.push({ name: 'Login', query: { redirect: to } })
  }
}

// 退出登录
export function logout(): void {
  const userStore = useUserStore()
  userStore.logout()
}

// 获取登录后重定向的路径
export function getRedirectPath(defaultPath = '/genealogy'): string {
  const query = router.currentRoute.value.query
  return (query.redirect as string) || defaultPath
}
