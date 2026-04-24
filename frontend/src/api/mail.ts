import request from '@/utils/request'
import type { ApiResponse, PageParams, PageResult, Mail } from '@/types/api'

export interface MailListParams extends PageParams {
  folder?: 'inbox' | 'sent' | 'drafts' | 'trash'
  isRead?: boolean
  isStarred?: boolean
}

export function getMailList(params: MailListParams) {
  return request.get<ApiResponse<PageResult<Mail>>>('/mails', { params })
}

export function getMailDetail(id: number) {
  return request.get<ApiResponse<Mail>>(`/mails/${id}`)
}

export function sendMail(data: { receiverId: number; subject: string; content: string }) {
  return request.post<ApiResponse<Mail>>('/mails/send', data)
}

export function saveDraft(data: { receiverId?: number; subject?: string; content?: string }) {
  return request.post<ApiResponse<Mail>>('/mails/draft', data)
}

export function deleteMail(id: number) {
  return request.delete<ApiResponse<null>>(`/mails/${id}`)
}

export function permanentlyDeleteMail(id: number) {
  return request.delete<ApiResponse<null>>(`/mails/${id}/permanent`)
}

export function markAsRead(id: number) {
  return request.put<ApiResponse<null>>(`/mails/${id}/read`)
}

export function markAsUnread(id: number) {
  return request.put<ApiResponse<null>>(`/mails/${id}/unread`)
}

export function toggleStar(id: number) {
  return request.put<ApiResponse<null>>(`/mails/${id}/star`)
}

export function getUnreadCount() {
  return request.get<ApiResponse<{ inbox: number; total: number }>>('/mails/unread-count')
}

export function searchReceivers(keyword: string) {
  return request.get<ApiResponse<{ id: number; name: string; avatar?: string }[]>>(
    '/mails/search-receivers',
    { params: { keyword } }
  )
}
