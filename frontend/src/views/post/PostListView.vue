<template>
  <div class="post-page">
    <header class="header">
      <div class="header-left">
        <h1 class="logo">MyFamily</h1>
      </div>
      <div class="header-right">
        <el-button type="primary" @click="goCreate">
          <el-icon><Plus /></el-icon>
          发布动态
        </el-button>
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
      <div class="post-list" v-loading="loading">
        <div
          v-for="post in postList"
          :key="post.id"
          class="post-card"
          @click="viewPost(post)"
        >
          <div class="post-header">
            <el-avatar :src="post.authorAvatar" :size="48">
              {{ post.authorName?.charAt(0) }}
            </el-avatar>
            <div class="author-info">
              <span class="author-name">{{ post.authorName }}</span>
              <span class="post-time">{{ formatRelativeTime(post.createdAt) }}</span>
            </div>
          </div>

          <div class="post-content">
            <p>{{ post.content }}</p>
          </div>

          <div class="post-images" v-if="post.images && post.images.length > 0">
            <el-image
              v-for="(img, index) in post.images"
              :key="index"
              :src="img"
              :preview-src-list="post.images"
              fit="cover"
              class="post-image"
            />
          </div>

          <div class="post-actions">
            <div class="action-item" :class="{ active: post.isLiked }" @click.stop="toggleLike(post)">
              <el-icon><Star /></el-icon>
              <span>{{ post.likes }}</span>
            </div>
            <div class="action-item" @click.stop="viewPost(post)">
              <el-icon><ChatLineSquare /></el-icon>
              <span>{{ post.comments }}</span>
            </div>
          </div>
        </div>

        <el-empty v-if="!loading && postList.length === 0" description="暂无动态" />
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
    </main>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { getPostList, likePost, unlikePost } from '@/api/post'
import { formatRelativeTime } from '@/utils/format'
import { Plus, Star, ChatLineSquare } from '@element-plus/icons-vue'
import type { Post } from '@/types/api'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const postList = ref<Post[]>([])
const currentPage = ref(1)
const pageSize = ref(20)
const total = ref(0)

async function fetchPostList() {
  loading.value = true
  try {
    const res = await getPostList({ page: currentPage.value, size: pageSize.value })
    postList.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

async function toggleLike(post: Post) {
  try {
    if (post.isLiked) {
      await unlikePost(post.id)
      post.likes--
    } else {
      await likePost(post.id)
      post.likes++
    }
    post.isLiked = !post.isLiked
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

function viewPost(post: Post) {
  router.push(`/post/${post.id}`)
}

function goCreate() {
  router.push('/post/create')
}

function handleSizeChange() {
  currentPage.value = 1
  fetchPostList()
}

function handlePageChange() {
  fetchPostList()
}

function handleCommand(command: string) {
  if (command === 'logout') {
    userStore.logout()
  } else if (command === 'profile') {
    router.push('/profile')
  }
}

onMounted(() => {
  fetchPostList()
})
</script>

<style lang="scss" scoped>
@import '@/assets/styles/variables.scss';
@import '@/assets/styles/mixins.scss';

.post-page {
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
    .logo {
      font-family: $font-family-decorative;
      font-size: 28px;
      color: $color-primary;
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
  max-width: 800px;
  margin: 0 auto;
  padding: $spacing-md;
}

.post-list {
  display: flex;
  flex-direction: column;
  gap: $spacing-md;
}

.post-card {
  @include card;
  padding: $spacing-lg;
  cursor: pointer;
  transition: transform $transition-fast ease;

  &:hover {
    transform: translateY(-2px);
  }
}

.post-header {
  display: flex;
  align-items: center;
  gap: $spacing-md;
  margin-bottom: $spacing-md;

  .author-info {
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

  p {
    white-space: pre-wrap;
  }
}

.post-images {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: $spacing-xs;
  margin-bottom: $spacing-md;

  .post-image {
    width: 100%;
    height: 150px;
    border-radius: $border-radius-sm;
  }
}

.post-actions {
  display: flex;
  gap: $spacing-lg;
  padding-top: $spacing-md;
  border-top: 1px solid $color-border-light;

  .action-item {
    display: flex;
    align-items: center;
    gap: $spacing-xs;
    color: $color-text-secondary;
    cursor: pointer;
    transition: color $transition-fast ease;

    &:hover {
      color: $color-primary;
    }

    &.active {
      color: $color-primary;
    }
  }
}

.pagination {
  margin-top: $spacing-lg;
  justify-content: center;
}
</style>
