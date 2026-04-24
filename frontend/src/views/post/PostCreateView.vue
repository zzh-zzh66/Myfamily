<template>
  <div class="post-create-page">
    <div class="page-header">
      <el-button text @click="goBack">
        <el-icon><ArrowLeft /></el-icon>
        返回
      </el-button>
      <h2>发布动态</h2>
    </div>

    <div class="create-card">
      <el-form ref="formRef" :model="form" :rules="rules">
        <el-form-item prop="content">
          <el-input
            v-model="form.content"
            type="textarea"
            :rows="8"
            placeholder="分享家族故事..."
            class="content-input"
            maxlength="2000"
            show-word-limit
          />
        </el-form-item>

        <el-form-item>
          <div class="upload-section">
            <el-upload
              action="#"
              :auto-upload="false"
              :on-change="handleImageChange"
              :file-list="fileList"
              list-type="picture-card"
              :limit="9"
            >
              <el-icon><Plus /></el-icon>
            </el-upload>
          </div>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleSubmit" :loading="loading">
            发布
          </el-button>
          <el-button @click="goBack">取消</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, Plus } from '@element-plus/icons-vue'
import { createPost } from '@/api/post'

const router = useRouter()

const formRef = ref()
const loading = ref(false)
const fileList = ref<any[]>([])

const form = reactive({
  content: '',
  images: [] as string[]
})

const rules = {
  content: [
    { required: true, message: '请输入动态内容', trigger: 'blur' },
    { min: 1, max: 2000, message: '内容长度应在1-2000字之间', trigger: 'blur' }
  ]
}

function handleImageChange(file: any, files: any[]) {
  fileList.value = files
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    await createPost({
      content: form.content,
      images: form.images
    })
    ElMessage.success('发布成功')
    router.push('/post')
  } catch (error) {
    // 错误已在拦截器处理
  } finally {
    loading.value = false
  }
}

function goBack() {
  router.back()
}
</script>

<style lang="scss" scoped>
@import '@/assets/styles/variables.scss';
@import '@/assets/styles/mixins.scss';

.post-create-page {
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

.create-card {
  @include card;
  padding: $spacing-lg;
  max-width: 800px;
  margin: 0 auto;
}

.content-input {
  :deep(.el-textarea__inner) {
    font-family: $font-family-serif;
    font-size: $font-size-base;
    line-height: $line-height-base;
    resize: none;
  }
}

.upload-section {
  :deep(.el-upload-list--picture-card) {
    display: flex;
    flex-wrap: wrap;
    gap: $spacing-xs;
  }
}
</style>
