<template>
  <div class="mail-detail-page">
    <div class="page-header">
      <el-button text @click="goBack">
        <el-icon><ArrowLeft /></el-icon>
        返回
      </el-button>
      <div class="header-actions">
        <el-button @click="handleReply">
          <el-icon><Promotion /></el-icon>
          回复
        </el-button>
        <el-button @click="handleForward">
          <el-icon><Right /></el-icon>
          转发
        </el-button>
        <el-button @click="handleDelete">
          <el-icon><Delete /></el-icon>
          删除
        </el-button>
      </div>
    </div>

    <div v-if="loading" class="loading-state">
      <el-skeleton :rows="10" animated />
    </div>

    <div v-else-if="mail" class="mail-content">
      <div class="mail-header">
        <h1 class="subject">{{ mail.subject }}</h1>
        <div class="mail-meta">
          <div class="sender-info">
            <el-avatar :src="mail.senderAvatar" :size="48">
              {{ mail.senderName?.charAt(0) }}
            </el-avatar>
            <div class="sender-details">
              <span class="sender-name">{{ mail.senderName }}</span>
              <span class="sender-email">{{ mail.senderId }}@example.com</span>
            </div>
          </div>
          <div class="mail-time">
            {{ formatDate(mail.createdAt, 'YYYY-MM-DD HH:mm') }}
          </div>
        </div>
      </div>

      <div class="mail-body">
        <div class="envelope-decoration"></div>
        <div class="content">{{ mail.content }}</div>
      </div>

      <div class="mail-footer">
        <el-button type="primary" @click="handleReply">
          <el-icon><Promotion /></el-icon>
          回复
        </el-button>
        <el-button @click="handleForward">
          <el-icon><Right /></el-icon>
          转发
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft, Promotion, Right, Delete } from '@element-plus/icons-vue'
import { getMailDetail, deleteMail, markAsRead } from '@/api/mail'
import { formatDate } from '@/utils/format'
import type { Mail } from '@/types/api'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const mail = ref<Mail | null>(null)

async function fetchMailDetail() {
  const id = Number(route.params.id)
  if (!id) return

  loading.value = true
  try {
    const res = await getMailDetail(id)
    mail.value = res.data

    // 标记为已读
    if (!res.data.isRead) {
      markAsRead(id).catch(() => {})
    }
  } finally {
    loading.value = false
  }
}

function goBack() {
  router.back()
}

function handleReply() {
  router.push({ path: '/mail/compose', query: { replyTo: String(mail.value?.senderId), subject: `Re: ${mail.value?.subject}` } })
}

function handleForward() {
  router.push({ path: '/mail/compose', query: { forward: 'true', subject: `Fwd: ${mail.value?.subject}`, content: mail.value?.content } })
}

async function handleDelete() {
  try {
    await ElMessageBox.confirm('确定要删除这封邮件吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await deleteMail(Number(route.params.id))
    ElMessage.success('删除成功')
    router.push('/mail')
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

onMounted(() => {
  fetchMailDetail()
})
</script>

<style lang="scss" scoped>
@import '@/assets/styles/variables.scss';
@import '@/assets/styles/mixins.scss';

.mail-detail-page {
  @include paper-background;
  min-height: 100vh;
  padding: $spacing-md;
}

.page-header {
  @include flex-between;
  margin-bottom: $spacing-lg;

  .header-actions {
    display: flex;
    gap: $spacing-sm;
  }
}

.loading-state {
  @include card;
  padding: $spacing-lg;
}

.mail-content {
  @include card;
  max-width: 800px;
  margin: 0 auto;
  padding: 0;
  overflow: hidden;
}

.mail-header {
  padding: $spacing-lg;
  border-bottom: 1px solid $color-border-light;

  .subject {
    font-family: $font-family-serif;
    font-size: $font-size-xl;
    margin-bottom: $spacing-md;
  }

  .mail-meta {
    @include flex-between;

    .sender-info {
      display: flex;
      align-items: center;
      gap: $spacing-md;

      .sender-details {
        @include flex-column;

        .sender-name {
          font-weight: 600;
          color: $color-text-primary;
        }

        .sender-email {
          font-size: $font-size-sm;
          color: $color-text-secondary;
        }
      }
    }

    .mail-time {
      color: $color-text-secondary;
      font-size: $font-size-sm;
    }
  }
}

.mail-body {
  padding: $spacing-lg;
  position: relative;

  .envelope-decoration {
    position: absolute;
    top: 0;
    left: $spacing-lg;
    right: $spacing-lg;
    height: 4px;
    background: linear-gradient(90deg, $color-primary, transparent 50%);
    opacity: 0.3;
  }

  .content {
    font-family: $font-family-serif;
    line-height: $line-height-base;
    white-space: pre-wrap;
    color: $color-text-regular;
  }
}

.mail-footer {
  padding: $spacing-md $spacing-lg;
  border-top: 1px solid $color-border-light;
  @include flex-start;
  gap: $spacing-sm;
}
</style>
