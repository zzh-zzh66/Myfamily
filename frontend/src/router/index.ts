import { createRouter, createWebHistory, RouteRecordRaw } from 'vue-router'
import { useUserStore } from '@/stores/user'

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    redirect: '/genealogy'
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/LoginView.vue'),
    meta: { title: '登录', requiresAuth: false }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/login/RegisterView.vue'),
    meta: { title: '注册', requiresAuth: false }
  },
  {
    path: '/genealogy',
    name: 'Genealogy',
    component: () => import('@/views/genealogy/GenealogyTreeView.vue'),
    meta: { title: '族谱树', requiresAuth: true }
  },
  {
    path: '/member/:id',
    name: 'MemberDetail',
    component: () => import('@/views/member/MemberDetailView.vue'),
    meta: { title: '成员详情', requiresAuth: true }
  },
  {
    path: '/member/add',
    name: 'MemberAdd',
    component: () => import('@/views/member/MemberAddView.vue'),
    meta: { title: '添加成员', requiresAuth: true }
  },
  {
    path: '/member/edit/:id',
    name: 'MemberEdit',
    component: () => import('@/views/member/MemberEditView.vue'),
    meta: { title: '编辑成员', requiresAuth: true }
  },
  {
    path: '/mail',
    name: 'Mail',
    component: () => import('@/views/mail/MailListView.vue'),
    meta: { title: '信箱', requiresAuth: true }
  },
  {
    path: '/mail/compose',
    name: 'MailCompose',
    component: () => import('@/views/mail/MailComposeView.vue'),
    meta: { title: '写信', requiresAuth: true }
  },
  {
    path: '/mail/:id',
    name: 'MailDetail',
    component: () => import('@/views/mail/MailDetailView.vue'),
    meta: { title: '邮件详情', requiresAuth: true }
  },
  {
    path: '/post',
    name: 'Post',
    component: () => import('@/views/post/PostListView.vue'),
    meta: { title: '动态', requiresAuth: true }
  },
  {
    path: '/post/create',
    name: 'PostCreate',
    component: () => import('@/views/post/PostCreateView.vue'),
    meta: { title: '发布动态', requiresAuth: true }
  },
  {
    path: '/post/:id',
    name: 'PostDetail',
    component: () => import('@/views/post/PostDetailView.vue'),
    meta: { title: '动态详情', requiresAuth: true }
  },
  {
    path: '/memorial',
    name: 'Memorial',
    component: () => import('@/views/memorial/MemorialHallView.vue'),
    meta: { title: '纪念堂', requiresAuth: true }
  },
  {
    path: '/admin/posts',
    name: 'PostReview',
    component: () => import('@/views/admin/PostReviewView.vue'),
    meta: { title: '动态审核', requiresAuth: true, requiresAdmin: true }
  },
  {
    path: '/profile',
    name: 'Profile',
    component: () => import('@/views/profile/ProfileView.vue'),
    meta: { title: '个人主页', requiresAuth: true }
  },
  {
    path: '/profile/edit',
    name: 'ProfileEdit',
    component: () => import('@/views/profile/ProfileEditView.vue'),
    meta: { title: '编辑资料', requiresAuth: true }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  const requiresAuth = to.matched.some(record => record.meta.requiresAuth)
  const requiresAdmin = to.matched.some(record => record.meta.requiresAdmin)

  if (requiresAuth && !userStore.isLoggedIn) {
    next({ name: 'Login', query: { redirect: to.fullPath } })
  } else if ((to.name === 'Login' || to.name === 'Register') && userStore.isLoggedIn) {
    next({ name: 'Genealogy' })
  } else if (requiresAdmin && !userStore.isAdmin) {
    next({ name: 'Genealogy' })
  } else {
    next()
  }
})

// 更新页面标题
router.afterEach((to) => {
  const title = to.meta.title as string
  document.title = title ? `${title} - MyFamily` : 'MyFamily - 家族传承'
})

export default router
