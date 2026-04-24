<template>
  <div class="mail-compose-page">
    <div class="page-header">
      <el-button text @click="goBack">
        <el-icon><ArrowLeft /></el-icon>
        返回
      </el-button>
      <h2>写信</h2>
    </div>

    <div class="compose-card">
      <el-form ref="formRef" :model="form" :rules="rules">
        <el-form-item prop="receiverId">
          <template #label>
            <span>收件人</span>
          </template>
          <el-select
            v-model="form.receiverId"
            placeholder="搜索收件人"
            filterable
            remote
            :remote-method="searchReceivers"
            :loading="searching"
            style="width: 100%"
          >
            <el-option
              v-for="user in receiverOptions"
              :key="user.id"
              :label="user.name"
              :value="user.id"
            >
              <div class="receiver-option">
                <el-avatar :src="user.avatar" :size="24">
                  {{ user.name?.charAt(0) }}
                </el-avatar>
                <span>{{ user.name }}</span>
              </div>
            </el-option>
          </el-select>
        </el-form-item>

        <el-form-item prop="subject">
          <el-input
            v-model="form.subject"
            placeholder="主题"
            class="subject-input"
          />
        </el-form-item>

        <el-form-item prop="content">
          <el-input
            v-model="form.content"
            type="textarea"
            :rows="12"
            placeholder="请输入邮件内容..."
            class="content-input"
          />
        </el-form-item>

        <div class="form-actions">
          <el-button type="primary" @click="handleSend" :loading="sending">
            发送
          </el-button>
          <el-button @click="handleSaveDraft">保存草稿</el-button>
          <el-button text @click="goBack">取消</el-button>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'
import { sendMail, saveDraft, searchReceivers as searchReceiversApi } from '@/api/mail'

const route = useRoute()
const router = useRouter()

const formRef = ref()
const sending = ref(false)
const searching = ref(false)
const receiverOptions = ref<{ id: number; name: string; avatar?: string }[]>([])

const form = reactive({
  receiverId: undefined as number | undefined,
  subject: '',
  content: ''
})

const rules = {
  receiverId: [
    { required: true, message: '请选择收件人', trigger: 'change' }
  ],
  subject: [
    { required: true, message: '请输入主题', trigger: 'blur' }
  ],
  content: [
    { required: true, message: '请输入邮件内容', trigger: 'blur' }
  ]
}

async function searchReceivers(keyword: string) {
  if (!keyword) {
    receiverOptions.value = []
    return
  }

  searching.value = true
  try {
    const res = await searchReceiversApi(keyword)
    receiverOptions.value = res.data
  } catch (error) {
    console.error('搜索收件人失败', error)
  } finally {
    searching.value = false
  }
}

async function handleSend() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  sending.value = true
  try {
    await sendMail({
      receiverId: form.receiverId!,
      subject: form.subject,
      content: form.content
    })
    ElMessage.success('发送成功')
    router.push('/mail')
  } catch (error) {
    // 错误已在拦截器处理
  } finally {
    sending.value = false
  }
}

async function handleSaveDraft() {
  try {
    await saveDraft({
      receiverId: form.receiverId,
      subject: form.subject,
      content: form.content
    })
    ElMessage.success('草稿已保存')
    router.push('/mail')
  } catch (error) {
    ElMessage.error('保存失败')
  }
}

function goBack() {
  router.back()
}

onMounted(() => {
  // 如果有预填的收件人ID
  if (route.query.to) {
    form.receiverId = Number(route.query.to)
    searchReceivers('').then(() => {
      // 设置默认值
    })
  }
})
</script>

<style lang="scss" scoped>
@import '@/assets/styles/variables.scss';
@import '@/assets/styles/mixins.scss';

.mail-compose-page {
  @include paper-background;
  min-height: 100vh;
  padding: $spacing-md;
}

.page-header {
  @include flex-between;
  margin-bottom: $spacing-lg;

  h2 {
    font-family: $font-family-serif;
    font-size: $font-size-xl;
  }
}

.compose-card {
  @include card;
  padding: $spacing-lg;
  max-width: 800px;
  margin: 0 auto;
}

.subject-input {
  :deep(.el-input__wrapper) {
    box-shadow: none;
    border-bottom: 1px solid $color-border;
    border-radius: 0;
    padding: 0;

    &:hover,
    &:focus {
      border-bottom-color: $color-primary;
    }
  }
}

.content-input {
  :deep(.el-textarea__inner) {
    font-family: $font-family-serif;
    resize: none;
    border: 1px solid $color-border;
    border-radius: $border-radius-sm;
    padding: $spacing-md;

    &:focus {
      border-color: $color-primary;
    }
  }
}

.form-actions {
  @include flex-start;
  gap: $spacing-sm;
  margin-top: $spacing-md;
}

.receiver-option {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
}
</style>
