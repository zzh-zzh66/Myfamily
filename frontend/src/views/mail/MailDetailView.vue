<template>
  <div class="mail-detail-page">
    <div class="page-header">
      <el-button text @click="goBack">
        <el-icon><ArrowLeft /></el-icon>
        返回
      </el-button>
      <div class="header-actions">
        <template v-if="mail?.isDraft === 1">
          <el-button type="primary" @click="handleSend">
            <el-icon><Promotion /></el-icon>
            发送
          </el-button>
          <el-button @click="handleEdit">
            <el-icon><Edit /></el-icon>
            编辑
          </el-button>
        </template>
        <template v-else>
          <el-button @click="handleReply">
            <el-icon><Promotion /></el-icon>
            回复
          </el-button>
          <el-button @click="handleForward">
            <el-icon><Right /></el-icon>
            转发
          </el-button>
        </template>
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
            <el-avatar :src="mail.fromUserAvatar" :size="48">
              {{ mail.fromUserName?.charAt(0) }}
            </el-avatar>
            <div class="sender-details">
              <span class="sender-name">{{ mail.fromUserName }}</span>
              <span class="sender-email">{{ mail.fromUserId }}@family.com</span>
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

      <div v-if="attachmentImages.length > 0" class="mail-attachments">
        <div class="attachments-title">附件 ({{ attachmentImages.length }})</div>
        <div class="attachments-grid">
          <el-image
            v-for="(url, index) in attachmentImages"
            :key="index"
            :src="getFullImageUrl(url)"
            :preview-src-list="attachmentImages.map(u => getFullImageUrl(u))"
            :initial-index="index"
            fit="cover"
            class="attachment-image"
          />
        </div>
      </div>

      <div class="mail-footer">
        <template v-if="mail?.isDraft === 1">
          <el-button type="primary" @click="handleSend">
            <el-icon><Promotion /></el-icon>
            发送
          </el-button>
          <el-button @click="handleEdit">
            <el-icon><Edit /></el-icon>
            编辑草稿
          </el-button>
        </template>
        <template v-else>
          <el-button type="primary" @click="handleReply">
            <el-icon><Promotion /></el-icon>
            回复
          </el-button>
          <el-button @click="handleForward">
            <el-icon><Right /></el-icon>
            转发
          </el-button>
        </template>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft, Promotion, Right, Delete, Edit } from '@element-plus/icons-vue'
import { getMailDetail, deleteMail, markAsRead, sendMail } from '@/api/mail'
import { formatDate } from '@/utils/format'
import type { Mail } from '@/types/api'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const mail = ref<Mail | null>(null)

const attachmentImages = computed(() => {
  if (!mail.value?.attachments) return []
  return mail.value.attachments.split(',').filter(Boolean)
})

function getFullImageUrl(url: string) {
  if (!url) return ''
  if (url.startsWith('http') || url.startsWith('/uploads')) {
    return url
  }
  return `/uploads/${url}`
}

async function fetchMailDetail() {
  const id = Number(route.params.id)
  if (!id) return

  loading.value = true
  try {
    const res = await getMailDetail(id)
    mail.value = res.data

    if (res.data.isRead === 0 && res.data.isDraft !== 1) {
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
  router.push({ path: '/mail/compose', query: { replyTo: String(mail.value?.fromUserId), subject: `Re: ${mail.value?.subject}` } })
}

function handleForward() {
  router.push({ path: '/mail/compose', query: { forward: 'true', subject: `Fwd: ${mail.value?.subject}`, content: mail.value?.content } })
}

function handleEdit() {
  router.push({
    path: '/mail/compose',
    query: {
      draftId: String(mail.value?.id),
      to: String(mail.value?.toMemberId),
      subject: mail.value?.subject,
      content: mail.value?.content,
      attachments: mail.value?.attachments || ''
    }
  })
}

async function handleSend() {
  if (!mail.value?.toMemberId || !mail.value?.subject || !mail.value?.content) {
    ElMessage.error('邮件信息不完整')
    return
  }
  const draftId = mail.value.id
  try {
    await sendMail({
      receiverId: mail.value.toMemberId,
      subject: mail.value.subject,
      content: mail.value.content,
      attachments: mail.value.attachments || ''
    })
    await deleteMail(draftId)
    ElMessage.success('发送成功')
    router.push('/mail')
  } catch (error) {
    ElMessage.error('发送失败')
  }
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

.mail-attachments {
  padding: $spacing-md $spacing-lg;
  border-top: 1px solid $color-border-light;

  .attachments-title {
    font-size: $font-size-sm;
    color: $color-text-secondary;
    margin-bottom: $spacing-sm;
  }

  .attachments-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(100px, 1fr));
    gap: $spacing-sm;

    .attachment-image {
      width: 100px;
      height: 100px;
      border-radius: $border-radius-sm;
      cursor: pointer;
    }
  }
}

.mail-footer {
  padding: $spacing-md $spacing-lg;
  border-top: 1px solid $color-border-light;
  @include flex-start;
  gap: $spacing-sm;
}
</style>
