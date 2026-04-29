<template>
  <div class="post-review-page">
    <header class="header">
      <div class="header-left">
        <h1 class="logo" @click="router.push('/genealogy')">MyFamily</h1>
        <el-breadcrumb separator="/">
          <el-breadcrumb-item :to="{ path: '/genealogy' }">首页</el-breadcrumb-item>
          <el-breadcrumb-item>动态审核</el-breadcrumb-item>
        </el-breadcrumb>
      </div>
      <div class="header-right">
        <el-dropdown @command="handleCommand">
          <span class="user-info">
            <el-avatar :src="userStore.userAvatar" :size="36">
              {{ userStore.userName?.charAt(0) }}
            </el-avatar>
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

    <main class="main-content">
      <div class="page-title">
        <h2>动态审核管理</h2>
        <span class="pending-count" v-if="pendingList.length > 0">
          待审核: {{ pendingList.length }}
        </span>
      </div>

      <div v-loading="loading" class="review-list">
        <div v-if="!loading && pendingList.length === 0" class="empty-state">
          <el-empty description="暂无待审核的动态" />
        </div>

        <div v-else class="post-cards">
          <div v-for="post in pendingList" :key="post.id" class="post-card">
            <div class="post-header">
              <el-avatar :src="post.authorAvatar" :size="48">
                {{ post.authorName?.charAt(0) }}
              </el-avatar>
              <div class="author-info">
                <span class="author-name">{{ post.authorName }}</span>
                <span class="post-time">{{ formatDate(post.createdAt, 'YYYY-MM-DD HH:mm') }}</span>
              </div>
              <el-tag type="warning">待审核</el-tag>
            </div>

            <div class="post-content" v-html="post.content"></div>

            <div class="post-actions">
              <el-button type="primary" @click="handleApprove(post.id)">
                <el-icon><Check /></el-icon>
                通过
              </el-button>
              <el-button type="danger" @click="showRejectDialog(post.id)">
                <el-icon><Close /></el-icon>
                拒绝
              </el-button>
            </div>
          </div>
        </div>
      </div>
    </main>

    <el-dialog v-model="rejectDialogVisible" title="拒绝原因" width="400px">
      <el-input
        v-model="rejectReason"
        type="textarea"
        :rows="3"
        placeholder="请输入拒绝原因"
      />
      <template #footer>
        <el-button @click="rejectDialogVisible = false">取消</el-button>
        <el-button type="danger" @click="handleReject" :loading="rejectLoading">确认拒绝</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { getPendingPosts, approvePost, rejectPost } from '@/api/post'
import { formatDate } from '@/utils/format'
import { Check, Close } from '@element-plus/icons-vue'
import type { Post } from '@/types/api'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const pendingList = ref<Post[]>([])
const rejectDialogVisible = ref(false)
const rejectReason = ref('')
const rejectLoading = ref(false)
const currentRejectId = ref<number | null>(null)

async function fetchPendingPosts() {
  loading.value = true
  try {
    const res = await getPendingPosts()
    pendingList.value = res.data
  } catch (error) {
    ElMessage.error('获取待审核动态失败')
  } finally {
    loading.value = false
  }
}

async function handleApprove(postId: number) {
  try {
    await ElMessageBox.confirm('确认通过此动态?', '审核确认', {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await approvePost(postId)
    ElMessage.success('审核通过')
    await fetchPendingPosts()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error('操作失败')
    }
  }
}

function showRejectDialog(postId: number) {
  currentRejectId.value = postId
  rejectReason.value = ''
  rejectDialogVisible.value = true
}

async function handleReject() {
  if (!rejectReason.value.trim()) {
    ElMessage.warning('请输入拒绝原因')
    return
  }

  rejectLoading.value = true
  try {
    await rejectPost(currentRejectId.value!, rejectReason.value)
    ElMessage.success('已拒绝')
    rejectDialogVisible.value = false
    await fetchPendingPosts()
  } catch (error) {
    ElMessage.error('操作失败')
  } finally {
    rejectLoading.value = false
  }
}

function handleCommand(command: string) {
  if (command === 'logout') {
    userStore.logout()
  } else if (command === 'profile') {
    router.push('/profile')
  }
}

onMounted(() => {
  fetchPendingPosts()
})
</script>

<style lang="scss" scoped>
@import '@/assets/styles/variables.scss';
@import '@/assets/styles/mixins.scss';

.post-review-page {
  min-height: 100vh;
  @include paper-background;
}

.header {
  @include flex-between;
  height: $header-height;
  padding: 0 $spacing-md;
  background: linear-gradient(135deg, $color-bg-card 0%, #F8F4EC 100%);
  border-bottom: 1px solid $color-border-light;
  position: sticky;
  top: 0;
  z-index: 100;

  .header-left {
    display: flex;
    align-items: center;
    gap: $spacing-lg;

    .logo {
      font-family: $font-family-decorative;
      font-size: 28px;
      color: $color-primary;
      cursor: pointer;
      margin: 0;
    }
  }

  .header-right {
    display: flex;
    align-items: center;
    gap: $spacing-md;

    .user-info {
      cursor: pointer;
    }
  }
}

.main-content {
  max-width: 900px;
  margin: 0 auto;
  padding: $spacing-lg;
}

.page-title {
  @include flex-between;
  margin-bottom: $spacing-lg;

  h2 {
    font-family: $font-family-serif;
    font-size: $font-size-xl;
    color: $color-text-primary;
  }

  .pending-count {
    background: $color-warning;
    color: white;
    padding: $spacing-xs $spacing-sm;
    border-radius: $border-radius-sm;
    font-size: $font-size-sm;
  }
}

.review-list {
  min-height: 300px;
}

.empty-state {
  padding: $spacing-xl;
}

.post-cards {
  display: flex;
  flex-direction: column;
  gap: $spacing-md;
}

.post-card {
  @include card;
  padding: $spacing-lg;
}

.post-header {
  display: flex;
  align-items: center;
  gap: $spacing-md;
  margin-bottom: $spacing-md;

  .author-info {
    flex: 1;
    @include flex-column;

    .author-name {
      font-weight: 600;
      color: $color-text-primary;
    }

    .post-time {
      font-size: $font-size-sm;
      color: $color-text-secondary;
    }
  }
}

.post-content {
  font-family: $font-family-serif;
  line-height: $line-height-base;
  color: $color-text-regular;
  margin-bottom: $spacing-md;
  padding: $spacing-md;
  background: $color-bg-gray;
  border-radius: $border-radius-sm;
}

.post-actions {
  display: flex;
  gap: $spacing-sm;
  justify-content: flex-end;
}
</style>