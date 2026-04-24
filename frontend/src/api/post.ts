import request from '@/utils/request'
import type { ApiResponse, PageParams, PageResult, Post } from '@/types/api'

export interface PostListParams extends PageParams {
  authorId?: number
  keyword?: string
  familyId?: number
  type?: string
}

export function getPostList(params: PostListParams) {
  return request.get<ApiResponse<PageResult<Post>>>('/posts', { params })
}

export function getPostDetail(id: number) {
  return request.get<ApiResponse<Post>>(`/posts/${id}`)
}

export function createPost(data: { title?: string; content: string; type?: string }) {
  return request.post<ApiResponse<Post>>('/posts', data)
}

export function updatePost(id: number, data: { content?: string; images?: string[] }) {
  return request.put<ApiResponse<Post>>(`/posts/${id}`, data)
}

export function deletePost(id: number) {
  return request.delete<ApiResponse<null>>(`/posts/${id}`)
}

export function likePost(id: number) {
  return request.post<ApiResponse<void>>(`/posts/${id}/like`)
}

export function unlikePost(id: number) {
  return request.delete<ApiResponse<void>>(`/posts/${id}/like`)
}

export interface Comment {
  id: number
  postId: number
  authorId: number
  authorName: string
  authorAvatar?: string
  content: string
  likeCount?: number
  createdAt: string
}

export function getPostComments(id: number) {
  return request.get<ApiResponse<Comment[]>>(`/posts/${id}/comments`)
}

export function addComment(postId: number, content: string) {
  return request.post<ApiResponse<Comment>>(`/posts/${postId}/comments`, { content })
}

export function deleteComment(postId: number, commentId: number) {
  return request.delete<ApiResponse<null>>(`/posts/${postId}/comments/${commentId}`)
}

export function uploadPostImage(file: File) {
  const formData = new FormData()
  formData.append('file', file)
  return request.post<ApiResponse<{ url: string }>>('/posts/upload-image', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}
