<template>
  <div class="mail-page">
    <!-- 顶部导航 -->
    <header class="header">
      <div class="header-left">
        <h1 class="logo" @click="router.push('/genealogy')">MyFamily</h1>
      </div>
      <div class="header-right">
        <el-dropdown @command="handleCommand">
          <span class="user-info">
            <el-avatar :src="userStore.userAvatar" :size="36">
              {{ userStore.userName?.charAt(0) }}
            </el-avatar>
            <span class="username">{{ userStore.userName }}</span>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="profile">个人主页</el-dropdown-item>
              <el-dropdown-item command="logout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </header>

    <div class="main-layout">
      <!-- 侧边栏 -->
      <aside class="sidebar">
        <el-button type="primary" class="compose-btn" @click="goCompose">
          <el-icon><Edit /></el-icon>
          写信
        </el-button>

        <nav class="nav-menu">
          <div
            class="nav-item"
            :class="{ active: currentFolder === 'inbox' }"
            @click="changeFolder('inbox')"
          >
            <el-icon><Box /></el-icon>
            <span>收件箱</span>
            <el-badge v-if="unreadCount > 0" :value="unreadCount" class="badge" />
          </div>
          <div
            class="nav-item"
            :class="{ active: currentFolder === 'sent' }"
            @click="changeFolder('sent')"
          >
            <el-icon><Promotion /></el-icon>
            <span>发件箱</span>
          </div>
          <div
            class="nav-item"
            :class="{ active: currentFolder === 'drafts' }"
            @click="changeFolder('drafts')"
          >
            <el-icon><Document /></el-icon>
            <span>草稿箱</span>
          </div>
          <div
            class="nav-item"
            :class="{ active: currentFolder === 'trash' }"
            @click="changeFolder('trash')"
          >
            <el-icon><Delete /></el-icon>
            <span>回收站</span>
          </div>
        </nav>
      </aside>

      <!-- 邮件列表 -->
      <main class="main-content">
        <div class="mail-container">
          <div class="toolbar">
            <el-checkbox v-model="selectAll" @change="handleSelectAll" />
            <el-button-group>
              <el-button @click="handleMarkAsRead" :disabled="!hasSelected">
                <el-icon><Check /></el-icon>
                标记已读
              </el-button>
              <el-button @click="handleDelete" :disabled="!hasSelected">
                <el-icon><Delete /></el-icon>
                删除
              </el-button>
            </el-button-group>
          </div>

          <div class="mail-list" v-loading="loading">
            <div
              v-for="mail in mailList"
              :key="mail.id"
              class="mail-item"
              :class="{ unread: !mail.isRead, selected: selectedIds.includes(mail.id) }"
              @click="viewMail(mail)"
            >
              <el-checkbox
                :model-value="selectedIds.includes(mail.id)"
                @click.stop
                @change="toggleSelect(mail.id)"
              />
              <div class="mail-star" @click.stop="toggleStar(mail.id)">
                <el-icon :color="mail.isStarred ? '#D4A84B' : undefined">
                  <Star v-if="mail.isStarred" />
                  <StarFilled v-else />
                </el-icon>
              </div>
              <div class="mail-sender">{{ mail.senderName }}</div>
              <div class="mail-subject">
                <span class="subject-text">{{ mail.subject }}</span>
                <span class="mail-preview"> - {{ mail.content }}</span>
              </div>
              <div class="mail-time">{{ formatRelativeTime(mail.createdAt) }}</div>
            </div>

            <el-empty v-if="!loading && mailList.length === 0" description="暂无邮件" />
          </div>

          <div class="pagination" v-if="total > 0">
            <el-pagination
              v-model:current-page="currentPage"
              v-model:page-size="pageSize"
              :total="total"
              :page-sizes="[10, 20, 50]"
              layout="total, sizes, prev, pager, next"
              @size-change="handleSizeChange"
              @current-change="handlePageChange"
            />
          </div>
        </div>
      </main>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { getMailList, markAsRead, toggleStar, deleteMail, getUnreadCount } from '@/api/mail'
import { formatRelativeTime } from '@/utils/format'
import { Box, Promotion, Document, Delete, Edit, Star, StarFilled, Check } from '@element-plus/icons-vue'
import type { Mail } from '@/types/api'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const mailList = ref<Mail[]>([])
const selectedIds = ref<number[]>([])
const currentFolder = ref<'inbox' | 'sent' | 'drafts' | 'trash'>('inbox')
const currentPage = ref(1)
const pageSize = ref(20)
const total = ref(0)
const unreadCount = ref(0)
const selectAll = ref(false)

const hasSelected = computed(() => selectedIds.value.length > 0)

async function fetchMailList() {
  loading.value = true
  try {
    const res = await getMailList({
      folder: currentFolder.value,
      page: currentPage.value,
      size: pageSize.value
    })
    mailList.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

async function fetchUnreadCount() {
  try {
    const res = await getUnreadCount()
    unreadCount.value = res.data.inbox
  } catch (error) {
    console.error('获取未读数失败', error)
  }
}

function changeFolder(folder: 'inbox' | 'sent' | 'drafts' | 'trash') {
  currentFolder.value = folder
  currentPage.value = 1
  selectedIds.value = []
  selectAll.value = false
  fetchMailList()
}

function viewMail(mail: Mail) {
  router.push(`/mail/${mail.id}`)
}

async function toggleStar(id: number) {
  try {
    await toggleStar(id)
    const mail = mailList.value.find(m => m.id === id)
    if (mail) {
      mail.isStarred = !mail.isStarred
    }
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

async function handleMarkAsRead() {
  try {
    await Promise.all(selectedIds.value.map(id => markAsRead(id)))
    mailList.value.forEach(mail => {
      if (selectedIds.value.includes(mail.id)) {
        mail.isRead = true
      }
    })
    selectedIds.value = []
    ElMessage.success('标记成功')
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

async function handleDelete() {
  try {
    await ElMessageBox.confirm('确定要删除选中的邮件吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await Promise.all(selectedIds.value.map(id => deleteMail(id)))
    ElMessage.success('删除成功')
    fetchMailList()
    fetchUnreadCount()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

function handleSelectAll(val: boolean) {
  selectedIds.value = val ? mailList.value.map(m => m.id) : []
}

function toggleSelect(id: number) {
  const index = selectedIds.value.indexOf(id)
  if (index > -1) {
    selectedIds.value.splice(index, 1)
  } else {
    selectedIds.value.push(id)
  }
}

function handleSizeChange() {
  currentPage.value = 1
  fetchMailList()
}

function handlePageChange() {
  fetchMailList()
}

function goCompose() {
  router.push('/mail/compose')
}

function handleCommand(command: string) {
  if (command === 'logout') {
    userStore.logout()
  } else if (command === 'profile') {
    router.push('/profile')
  }
}

onMounted(() => {
  fetchMailList()
  fetchUnreadCount()
})
</script>

<style lang="scss" scoped>
@import '@/assets/styles/variables.scss';
@import '@/assets/styles/mixins.scss';

.mail-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.header {
  @include flex-between;
  height: $header-height;
  padding: 0 $spacing-md;
  background: linear-gradient(135deg, $color-bg-card 0%, #F8F4EC 100%);
  border-bottom: 1px solid $color-border-light;

  .header-left {
    .logo {
      font-family: $font-family-decorative;
      font-size: 28px;
      color: $color-primary;
      cursor: pointer;
    }
  }

  .header-right {
    .user-info {
      @include flex-center;
      gap: $spacing-xs;
      cursor: pointer;
    }
  }
}

.main-layout {
  display: flex;
  flex: 1;
}

.sidebar {
  width: $sidebar-width;
  background: $color-bg-card;
  border-right: 1px solid $color-border-light;
  padding: $spacing-md;
  position: sticky;
  top: $header-height;
  height: calc(100vh - #{$header-height});

  .compose-btn {
    width: 100%;
    margin-bottom: $spacing-md;
  }

  .nav-menu {
    .nav-item {
      @include flex-start;
      gap: $spacing-sm;
      padding: 12px $spacing-sm;
      color: $color-text-regular;
      font-size: $font-size-sm;
      border-radius: $border-radius-sm;
      cursor: pointer;
      transition: all $transition-fast ease;

      &:hover {
        background: $color-bg-hover;
      }

      &.active {
        background: rgba($color-primary, 0.1);
        color: $color-primary;
      }

      .badge {
        margin-left: auto;
      }
    }
  }
}

.main-content {
  flex: 1;
  @include paper-background;
  padding: $spacing-md;
}

.mail-container {
  background: $color-bg-card;
  border-radius: $border-radius-md;
  box-shadow: $shadow-md;
}

.toolbar {
  @include flex-between;
  padding: $spacing-md;
  border-bottom: 1px solid $color-border-light;
}

.mail-list {
  min-height: 400px;
}

.mail-item {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
  padding: $spacing-md;
  border-bottom: 1px solid $color-border-light;
  cursor: pointer;
  transition: background $transition-fast ease;

  &:hover {
    background: $color-bg-hover;
  }

  &.unread {
    background: rgba($color-primary, 0.03);

    .mail-sender,
    .subject-text {
      font-weight: 600;
    }
  }

  &.selected {
    background: rgba($color-primary, 0.05);
  }

  .mail-sender {
    width: 120px;
    @include text-ellipsis;
  }

  .mail-subject {
    flex: 1;
    @include text-ellipsis;

    .mail-preview {
      color: $color-text-secondary;
    }
  }

  .mail-time {
    width: 80px;
    text-align: right;
    color: $color-text-secondary;
    font-size: $font-size-xs;
  }
}

.pagination {
  padding: $spacing-md;
  border-top: 1px solid $color-border-light;
  justify-content: flex-end;
}
</style>
