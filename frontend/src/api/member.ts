import request from '@/utils/request'
import type { ApiResponse, PageParams, PageResult, Member, GenealogyNode } from '@/types/api'

export interface MemberListParams extends PageParams {
  familyId?: number
  generation?: number
  gender?: 'male' | 'female'
  keyword?: string
}

export function getMemberList(params: MemberListParams) {
  return request.get<ApiResponse<PageResult<Member>>>('/members', { params })
}

export function getMemberDetail(id: number) {
  return request.get<ApiResponse<Member>>(`/members/${id}`)
}

export function createMember(data: Partial<Member>) {
  return request.post<ApiResponse<Member>>('/members', data)
}

export function updateMember(id: number, data: Partial<Member>) {
  return request.put<ApiResponse<Member>>(`/members/${id}`, data)
}

export function deleteMember(id: number) {
  return request.delete<ApiResponse<null>>(`/members/${id}`)
}

export function getGenealogyTree(familyId?: number) {
  return request.get<ApiResponse<Member[]>>('/members/genealogy/tree', {
    params: { familyId }
  })
}

export function getMemberChildren(id: number) {
  return request.get<ApiResponse<Member[]>>(`/members/${id}/children`)
}

export function getMemberSpouse(id: number) {
  return request.get<ApiResponse<Member>>(`/members/${id}/spouse`)
}

export function uploadMemberAvatar(id: number, file: File) {
  const formData = new FormData()
  formData.append('file', file)
  return request.post<ApiResponse<{ avatar: string }>>(
    `/members/${id}/avatar`,
    formData,
    {
      headers: { 'Content-Type': 'multipart/form-data' }
    }
  )
}
