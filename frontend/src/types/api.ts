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
  role?: 'admin' | 'member' | 'ADMIN' | 'MEMBER'
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

// CoupleNode - 夫妻组合节点（用于族谱展示）
export interface CoupleNode {
  id: string  // 组合节点的唯一ID，格式 "maleId-femaleId" 或 "singleId"
  male?: Member    // 男性成员（如果配偶是女性）
  female?: Member  // 女性成员（如果配偶是男性）
  isCouple: boolean  // 是否是夫妻组合节点
  generation: number
  children: string[]  // 子节点的 CoupleNode ID 列表
  fatherId?: string   // 父亲 CoupleNode ID（如果有）
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
  spouseName?: string  // 配偶姓名（用于显示）
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
  attachments?: string
  isRead: number
  isDeleted?: number
  deletedByReceiver?: number
  isDraft?: number
  createdAt: string
  updatedAt?: string
}

// 评论相关类型
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

// 动态相关类型
export interface Post {
  id: number
  familyId?: number
  authorId: number
  authorName: string
  authorAvatar?: string
  title?: string
  content: string
  images?: string[]
  type?: 'EVENT' | 'ACHIEVEMENT' | 'MILESTONE' | 'OTHER'
  status?: 'PENDING' | 'APPROVED' | 'REJECTED'
  viewCount?: number
  likeCount: number
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
