import request from '@/utils/request'
import type { ApiResponse, PageParams, PageResult, User } from '@/types/api'

export interface LoginParams {
  username: string
  password: string
}

export interface RegisterParams {
  username: string
  password: string
  email: string
  familyName?: string
}

export function login(data: LoginParams) {
  return request.post<ApiResponse<{ token: string; user: User }>>('/auth/login', data)
}

export function register(data: RegisterParams) {
  return request.post<ApiResponse<{ token: string; user: User }>>('/auth/register', data)
}

export function logout() {
  return request.post<ApiResponse<null>>('/auth/logout')
}

export function getCurrentUser() {
  return request.get<ApiResponse<User>>('/auth/me')
}

export function updateUser(data: Partial<User>) {
  return request.put<ApiResponse<User>>('/auth/user', data)
}

export function changePassword(oldPassword: string, newPassword: string) {
  return request.post<ApiResponse<null>>('/auth/change-password', {
    oldPassword,
    newPassword
  })
}
