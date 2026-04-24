<template>
  <div class="register-page">
    <div class="register-background">
      <div class="ink-mountains"></div>
    </div>

    <div class="register-container">
      <div class="register-card">
        <div class="register-header">
          <h1 class="title">MyFamily</h1>
          <p class="subtitle">创建家族账号</p>
        </div>

        <el-form
          ref="formRef"
          :model="form"
          :rules="rules"
          class="register-form"
          @submit.prevent="handleRegister"
        >
          <el-form-item prop="username">
            <el-input
              v-model="form.username"
              placeholder="用户名"
              size="large"
              :prefix-icon="User"
            />
          </el-form-item>

          <el-form-item prop="email">
            <el-input
              v-model="form.email"
              placeholder="邮箱"
              size="large"
              :prefix-icon="Message"
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

          <el-form-item prop="confirmPassword">
            <el-input
              v-model="form.confirmPassword"
              type="password"
              placeholder="确认密码"
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
              class="register-button"
              native-type="submit"
            >
              注 册
            </el-button>
          </el-form-item>
        </el-form>

        <div class="register-footer">
          <span>已有账号？</span>
          <router-link to="/login">立即登录</router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock, Message } from '@element-plus/icons-vue'
import { register as registerApi } from '@/api/auth'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const formRef = ref()
const loading = ref(false)

const form = reactive({
  username: '',
  email: '',
  password: '',
  confirmPassword: ''
})

const validateConfirmPassword = (rule: any, value: string, callback: any) => {
  if (value !== form.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度为3-20个字符', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码至少6个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

async function handleRegister() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    const res = await registerApi({
      username: form.username,
      email: form.email,
      password: form.password
    })
    userStore.login(res.data.token, res.data.user)
    ElMessage.success('注册成功')
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

.register-page {
  @include ink-transition;
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
}

.register-background {
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
}

.register-container {
  position: relative;
  z-index: 1;
  width: 100%;
  max-width: 420px;
  padding: $spacing-md;
}

.register-card {
  @include card;
  padding: $spacing-xl;
  animation: scaleIn $transition-slow ease;
}

.register-header {
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
}

.register-form {
  :deep(.el-form-item) {
    margin-bottom: $spacing-md;
  }

  :deep(.el-input__wrapper) {
    padding: 4px 16px;
  }
}

.register-button {
  width: 100%;
  height: 48px;
  font-size: $font-size-lg;
  font-family: $font-family-serif;
  letter-spacing: 8px;
}

.register-footer {
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
