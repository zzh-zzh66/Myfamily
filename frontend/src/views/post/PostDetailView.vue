<template>
  <div class="post-detail-page">
    <div class="page-header">
      <el-button text @click="goBack">
        <el-icon><ArrowLeft /></el-icon>
        返回
      </el-button>
    </div>

    <div v-if="loading" class="loading-state">
      <el-skeleton :rows="10" animated />
    </div>

    <div v-else-if="error" class="error-state">
      <el-result
        icon="error"
        :title="error"
        sub-title="请稍后重试或返回上一页"
      >
        <template #extra>
          <el-button type="primary" @click="goBack">返回</el-button>
        </template>
      </el-result>
    </div>

    <div v-else-if="post" class="post-content">
      <div class="post-card">
        <div class="post-header">
          <el-avatar :src="post.authorAvatar" :size="64">
            {{ post.authorName?.charAt(0) }}
          </el-avatar>
          <div class="author-info">
            <span class="author-name">{{ post.authorName }}</span>
            <span class="post-time">{{ formatDate(post.createdAt, 'YYYY-MM-DD HH:mm') }}</span>
          </div>
        </div>

        <div class="post-body">
          <div class="post-text" v-html="post.content"></div>

          <div
            class="post-images"
            :class="`images-${post.images?.length || 0}`"
            v-if="post.images && post.images.length > 0"
          >
            <el-image
              v-for="(img, index) in post.images"
              :key="index"
              :src="img"
              :preview-src-list="post.images"
              fit="cover"
              class="post-image"
            />
          </div>
        </div>

        <div class="post-actions">
          <div class="action-item" :class="{ active: post.isLiked }" @click="toggleLike">
            <el-icon><Star /></el-icon>
            <span>{{ post.likeCount }} 赞</span>
          </div>
          <div class="action-item">
            <el-icon><ChatLineSquare /></el-icon>
            <span>{{ post.comments }} 评论</span>
          </div>
        </div>
      </div>

      <!-- 评论区 -->
      <div class="comments-section">
        <h3>评论</h3>
        <div class="comment-input">
          <el-input
            v-model="commentContent"
            type="textarea"
            :rows="3"
            placeholder="写下你的评论..."
          />
          <el-button type="primary" @click="submitComment" :loading="commentLoading">
            发布评论
          </el-button>
        </div>

        <div class="comment-list">
          <div v-for="comment in comments" :key="comment.id" class="comment-item">
            <el-avatar :src="comment.authorAvatar" :size="40">
              {{ comment.authorName?.charAt(0) }}
            </el-avatar>
            <div class="comment-body">
              <div class="comment-header">
                <span class="comment-author">{{ comment.authorName }}</span>
                <span class="comment-time">{{ formatRelativeTime(comment.createdAt) }}</span>
              </div>
              <p class="comment-content">{{ comment.content }}</p>
            </div>
          </div>

          <el-empty v-if="comments.length === 0" description="暂无评论" />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, Star, ChatLineSquare } from '@element-plus/icons-vue'
import { getPostDetail, likePost, unlikePost, getPostComments, addComment } from '@/api/post'
import { formatDate, formatRelativeTime } from '@/utils/format'
import type { Post, Comment } from '@/types/api'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const post = ref<Post | null>(null)
const error = ref<string | null>(null)
const comments = ref<Comment[]>([])
const commentContent = ref('')
const commentLoading = ref(false)

async function fetchPostDetail() {
  const id = Number(route.params.id)
  if (!id) return

  loading.value = true
  error.value = null
  try {
    const res = await getPostDetail(id)
    const data = res.data as any
    data.images = data.images ? data.images.split(',').filter((img: string) => img.trim()) : []
    post.value = data
    await fetchComments()
  } catch (err: any) {
    error.value = err.message || '加载失败，请稍后重试'
    post.value = null
  } finally {
    loading.value = false
  }
}

async function fetchComments() {
  const id = Number(route.params.id)
  try {
    const res = await getPostComments(id)
    comments.value = res.data
  } catch (error) {
    console.error('获取评论失败', error)
  }
}

async function toggleLike() {
  if (!post.value) return

  try {
    if (post.value.isLiked) {
      await unlikePost(post.value.id)
      post.value.likeCount = Math.max(0, (post.value.likeCount || 0) - 1)
    } else {
      await likePost(post.value.id)
      post.value.likeCount = (post.value.likeCount || 0) + 1
    }
    post.value.isLiked = !post.value.isLiked
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

async function submitComment() {
  if (!commentContent.value.trim()) return

  commentLoading.value = true
  try {
    await addComment(Number(route.params.id), commentContent.value)
    ElMessage.success('评论成功')
    commentContent.value = ''
    await fetchComments()
  } catch (error) {
    ElMessage.error('评论失败')
  } finally {
    commentLoading.value = false
  }
}

function goBack() {
  router.back()
}

onMounted(() => {
  fetchPostDetail()
})
</script>

<style lang="scss" scoped>
@import '@/assets/styles/variables.scss';
@import '@/assets/styles/mixins.scss';

.post-detail-page {
  @include paper-background;
  min-height: 100vh;
  padding: $spacing-md;
}

.page-header {
  margin-bottom: $spacing-lg;
}

.loading-state {
  @include card;
  padding: $spacing-lg;
}

.error-state {
  max-width: 800px;
  margin: 0 auto;
  padding: $spacing-lg;
}

.post-content {
  max-width: 800px;
  margin: 0 auto;
}

.post-card {
  @include card;
  padding: $spacing-lg;
  margin-bottom: $spacing-lg;
}

.post-header {
  display: flex;
  align-items: center;
  gap: $spacing-md;
  margin-bottom: $spacing-lg;

  .author-info {
    @include flex-column;

    .author-name {
      font-weight: 600;
      font-size: $font-size-lg;
      color: $color-text-primary;
    }

    .post-time {
      font-size: $font-size-sm;
      color: $color-text-secondary;
    }
  }
}

.post-body {
  margin-bottom: $spacing-lg;

  .post-text {
    font-family: $font-family-serif;
    line-height: $line-height-base;
    color: $color-text-regular;
    white-space: pre-wrap;
    margin-bottom: $spacing-md;
  }

  .post-images {
    display: grid;
    gap: $spacing-xs;
    margin-bottom: $spacing-md;

    .post-image {
      width: 100%;
      height: 100%;
      object-fit: cover;
      border-radius: $border-radius-sm;
      cursor: pointer;
    }

    &.images-1 {
      grid-template-columns: 1fr;
      max-height: 500px;

      .post-image {
        max-height: 500px;
        object-fit: contain;
      }
    }

    &.images-2 {
      grid-template-columns: repeat(2, 1fr);
      max-height: 350px;
    }

    &.images-3 {
      grid-template-columns: repeat(3, 1fr);
      max-height: 250px;
    }

    &.images-4 {
      grid-template-columns: repeat(2, 1fr);
      max-height: 250px;
    }

    &.images-5,
    &.images-6 {
      grid-template-columns: repeat(3, 1fr);
      max-height: 250px;
    }

    &.images-7,
    &.images-8,
    &.images-9 {
      grid-template-columns: repeat(3, 1fr);
      max-height: 200px;
    }
  }
}

.post-actions {
  display: flex;
  gap: $spacing-xl;
  padding-top: $spacing-md;
  border-top: 1px solid $color-border-light;

  .action-item {
    display: flex;
    align-items: center;
    gap: $spacing-xs;
    color: $color-text-secondary;
    cursor: pointer;

    &:hover,
    &.active {
      color: $color-primary;
    }
  }
}

.comments-section {
  @include card;
  padding: $spacing-lg;

  h3 {
    font-family: $font-family-serif;
    margin-bottom: $spacing-md;
  }
}

.comment-input {
  margin-bottom: $spacing-lg;

  .el-button {
    margin-top: $spacing-sm;
  }
}

.comment-list {
  display: flex;
  flex-direction: column;
  gap: $spacing-md;
}

.comment-item {
  display: flex;
  gap: $spacing-md;

  .comment-body {
    flex: 1;

    .comment-header {
      @include flex-between;
      margin-bottom: $spacing-xs;

      .comment-author {
        font-weight: 600;
        color: $color-text-primary;
      }

      .comment-time {
        font-size: $font-size-xs;
        color: $color-text-secondary;
      }
    }

    .comment-content {
      color: $color-text-regular;
      line-height: $line-height-base;
    }
  }
}
</style>
