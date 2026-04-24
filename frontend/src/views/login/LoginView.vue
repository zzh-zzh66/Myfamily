<template>
  <div class="login-page">
    <div class="login-background">
      <!-- 水墨山水背景 -->
      <div class="ink-mountains"></div>
    </div>

    <div class="login-container">
      <div class="login-card">
        <div class="login-header">
          <h1 class="title">MyFamily</h1>
          <p class="subtitle">家族传承</p>
        </div>

        <el-form
          ref="formRef"
          :model="form"
          :rules="rules"
          class="login-form"
          @submit.prevent="handleLogin"
        >
          <el-form-item prop="username">
            <el-input
              v-model="form.username"
              placeholder="用户名"
              size="large"
              :prefix-icon="User"
            />
          </el-form-item>

          <el-form-item prop="password">
            <el-input
              v-model="form.password"
              type="password"
              placeholder="密码"
              size="large"
              :prefix-icon="Lock"
              show-password
            />
          </el-form-item>

          <el-form-item>
            <el-button
              type="primary"
              size="large"
              :loading="loading"
              class="login-button"
              native-type="submit"
            >
              登 录
            </el-button>
          </el-form-item>
        </el-form>

        <div class="login-footer">
          <span>没有账号？</span>
          <router-link to="/register">立即注册</router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { login as loginApi } from '@/api/auth'

const router = useRouter()
const userStore = useUserStore()

const formRef = ref()
const loading = ref(false)

const form = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码至少6个字符', trigger: 'blur' }
  ]
}

async function handleLogin() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    const res = await loginApi(form)
    userStore.login(res.data.token, res.data.user)
    ElMessage.success('登录成功')
    router.push('/genealogy')
  } catch (error) {
    // 错误已在拦截器处理
  } finally {
    loading.value = false
  }
}
</script>

<style lang="scss" scoped>
@import '@/assets/styles/variables.scss';
@import '@/assets/styles/mixins.scss';

.login-page {
  @include ink-transition;
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
}

.login-background {
  position: absolute;
  inset: 0;
  @include paper-background;
}

.ink-mountains {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 60%;
  background: linear-gradient(
    to top,
    rgba($color-primary, 0.05) 0%,
    transparent 50%
  );
  // 水墨山水效果将通过SVG实现
}

.login-container {
  position: relative;
  z-index: 1;
  width: 100%;
  max-width: 420px;
  padding: $spacing-md;
}

.login-card {
  @include card;
  padding: $spacing-xl;
  animation: scaleIn $transition-slow ease;
}

.login-header {
  text-align: center;
  margin-bottom: $spacing-xl;
}

.title {
  font-family: $font-family-decorative;
  font-size: 48px;
  color: $color-primary;
  margin-bottom: $spacing-xs;
  letter-spacing: 4px;
}

.subtitle {
  font-family: $font-family-serif;
  font-size: $font-size-lg;
  color: $color-text-secondary;
  letter-spacing: 8px;
}

.login-form {
  :deep(.el-form-item) {
    margin-bottom: $spacing-md;
  }

  :deep(.el-input__wrapper) {
    padding: 4px 16px;
  }
}

.login-button {
  width: 100%;
  height: 48px;
  font-size: $font-size-lg;
  font-family: $font-family-serif;
  letter-spacing: 8px;
}

.login-footer {
  text-align: center;
  margin-top: $spacing-md;
  color: $color-text-secondary;
  font-size: $font-size-sm;

  a {
    color: $color-primary;
    margin-left: 4px;

    &:hover {
      text-decoration: underline;
    }
  }
}
</style>
