import request from '@/utils/request'
import type { ApiResponse, Memorial } from '@/types/api'
import type { Member } from '@/types/api'

export function getMemorialList() {
  return request.get<ApiResponse<Memorial[]>>('/memorials')
}

export function getMemorialDetail(id: number) {
  return request.get<ApiResponse<Memorial>>(`/memorials/${id}`)
}

export function addMemorial(data: Partial<Memorial>) {
  return request.post<ApiResponse<Memorial>>('/memorials', data)
}

export function updateMemorial(id: number, data: Partial<Memorial>) {
  return request.put<ApiResponse<Memorial>>(`/memorials/${id}`, data)
}

export function deleteMemorial(id: number) {
  return request.delete<ApiResponse<null>>(`/memorials/${id}`)
}

export function uploadMemorialImage(file: File) {
  const formData = new FormData()
  formData.append('file', file)
  return request.post<ApiResponse<string>>('/memorials/upload-image', formData)
}

export function searchMemorialMembers(keyword: string) {
  return request.get<ApiResponse<Member[]>>('/memorials/search-members', {
    params: { keyword }
  })
}