// API基础响应类型
export interface ApiResponse<T = any> {
  code: number
  message: string
  data: T
}

// 分页请求参数
export interface PageParams {
  page?: number
  size?: number
}

// 分页响应类型
export interface PageResult<T> {
  records: T[]
  total: number
  page: number
  size: number
  totalPages: number
}

// 用户相关类型
export interface User {
  id: number
  username: string
  email: string
  avatar?: string
  phone?: string
  familyId?: number
  role?: 'admin' | 'member'
  createdAt?: string
  updatedAt?: string
}

// 成员相关类型
export interface Member {
  id: number
  name: string
  gender: 'male' | 'female'
  birthDate?: string
  deathDate?: string
  generation: number
  avatar?: string
  spouse?: string
  fatherId?: number
  motherId?: number
  spouseId?: number
  birthplace?: string
  biography?: string
  achievements?: string
  familyId: number
  isVirtual?: boolean
  createdAt?: string
  updatedAt?: string
}

// 族谱树节点类型（用于前端渲染）
export interface GenealogyNode {
  id: number
  name: string
  gender: 'male' | 'female'
  avatar?: string
  generation: number
  spouse?: string
  spouseId?: number
  children?: GenealogyNode[]
  spouseNode?: GenealogyNode
  fatherNode?: GenealogyNode
  isExpanded?: boolean
  isHighlighted?: boolean
}

// 邮件相关类型
export interface Mail {
  id: number
  familyId?: number
  fromUserId: number
  fromUserName: string
  fromUserAvatar?: string
  toMemberId: number
  toMemberName: string
  toMemberAvatar?: string
  subject: string
  content: string
  isRead: number
  isDeleted?: number
  deletedByReceiver?: number
  createdAt: string
  updatedAt?: string
}

// 动态相关类型
export interface Post {
  id: number
  familyId?: number
  authorId: number
  authorName: string
  authorAvatar?: string
  title?: string
  content: string
  type?: 'EVENT' | 'ACHIEVEMENT' | 'MILESTONE' | 'OTHER'
  status?: 'PENDING' | 'APPROVED' | 'REJECTED'
  viewCount?: number
  likes: number
  comments: number
  isLiked?: boolean
  createdAt: string
  updatedAt?: string
}

// 纪念堂相关类型
export interface Memorial {
  id: number
  memberId: number
  name: string
  birthYear: string
  deathYear: string
  avatar?: string
  title: string
  biography?: string
  createdAt: string
  updatedAt?: string
}

// 家庭相关类型
export interface Family {
  id: number
  name: string
  description?: string
  avatar?: string
  slogan?: string
  region?: string
  foundedYear?: string
  memberCount: number
  createdAt?: string
  updatedAt?: string
}
