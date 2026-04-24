<template>
  <div class="profile-page">
    <header class="header">
      <div class="header-left">
        <el-button text @click="goBack">
          <el-icon><ArrowLeft /></el-icon>
          返回
        </el-button>
      </div>
      <div class="header-right">
        <el-button @click="goEdit">
          <el-icon><Edit /></el-icon>
          编辑资料
        </el-button>
      </div>
    </header>

    <main class="main-content">
      <div class="profile-card">
        <div class="avatar-section">
          <el-avatar :src="userStore.userAvatar" :size="120" class="avatar">
            {{ userStore.userName?.charAt(0) }}
          </el-avatar>
        </div>

        <div class="user-info">
          <h1 class="username">{{ userStore.userName }}</h1>
          <p class="email">{{ user?.email }}</p>
        </div>

        <div class="stats-section">
          <div class="stat-item">
            <span class="stat-value">{{ stats.memberCount }}</span>
            <span class="stat-label">家族成员</span>
          </div>
          <div class="stat-item">
            <span class="stat-value">{{ stats.postCount }}</span>
            <span class="stat-label">发布动态</span>
          </div>
          <div class="stat-item">
            <span class="stat-value">{{ stats.mailCount }}</span>
            <span class="stat-label">发送邮件</span>
          </div>
        </div>
      </div>

      <div class="info-card">
        <h3 class="card-title">个人信息</h3>
        <div class="info-list">
          <div class="info-item">
            <span class="label">用户名</span>
            <span class="value">{{ user?.username }}</span>
          </div>
          <div class="info-item">
            <span class="label">邮箱</span>
            <span class="value">{{ user?.email }}</span>
          </div>
          <div class="info-item">
            <span class="label">手机号</span>
            <span class="value">{{ user?.phone || '未设置' }}</span>
          </div>
          <div class="info-item">
            <span class="label">注册时间</span>
            <span class="value">{{ user?.createdAt ? formatDate(user.createdAt) : '-' }}</span>
          </div>
        </div>
      </div>

      <div class="action-section">
        <el-button @click="goEdit">编辑资料</el-button>
        <el-button @click="handleChangePassword">修改密码</el-button>
        <el-button type="danger" text @click="handleLogout">退出登录</el-button>
      </div>
    </main>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { formatDate } from '@/utils/format'
import { ArrowLeft, Edit } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()

const user = computed(() => userStore.userInfo)

const stats = reactive({
  memberCount: 0,
  postCount: 0,
  mailCount: 0
})

function goBack() {
  router.push('/genealogy')
}

function goEdit() {
  router.push('/profile/edit')
}

function handleChangePassword() {
  // TODO: 实现修改密码功能
  ElMessage.info('功能开发中')
}

async function handleLogout() {
  try {
    await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    userStore.logout()
  } catch {
    // 用户取消
  }
}

onMounted(() => {
  // TODO: 获取用户统计数据
})
</script>

<style lang="scss" scoped>
@import '@/assets/styles/variables.scss';
@import '@/assets/styles/mixins.scss';

.profile-page {
  @include paper-background;
  min-height: 100vh;
}

.header {
  @include flex-between;
  height: $header-height;
  padding: 0 $spacing-md;
  background: linear-gradient(135deg, $color-bg-card 0%, #F8F4EC 100%);
  border-bottom: 1px solid $color-border-light;
}

.main-content {
  max-width: 800px;
  margin: 0 auto;
  padding: $spacing-xl $spacing-md;
}

.profile-card {
  @include card;
  padding: $spacing-xl;
  text-align: center;
  margin-bottom: $spacing-lg;
}

.avatar-section {
  margin-bottom: $spacing-lg;

  .avatar {
    border: 4px solid $color-border-light;
    font-size: 48px;
    background: linear-gradient(135deg, $color-primary, $color-primary-hover);
  }
}

.user-info {
  .username {
    font-family: $font-family-serif;
    font-size: $font-size-2xl;
    margin-bottom: $spacing-xs;
  }

  .email {
    color: $color-text-secondary;
    font-size: $font-size-sm;
  }
}

.stats-section {
  display: flex;
  justify-content: center;
  gap: $spacing-xl;
  margin-top: $spacing-lg;
  padding-top: $spacing-lg;
  border-top: 1px solid $color-border-light;

  .stat-item {
    @include flex-column;
    align-items: center;

    .stat-value {
      font-size: $font-size-xl;
      font-weight: 600;
      color: $color-primary;
    }

    .stat-label {
      font-size: $font-size-sm;
      color: $color-text-secondary;
    }
  }
}

.info-card {
  @include card;
  padding: $spacing-lg;
  margin-bottom: $spacing-lg;

  .card-title {
    font-family: $font-family-serif;
    margin-bottom: $spacing-md;
    padding-bottom: $spacing-sm;
    border-bottom: 1px solid $color-border-light;
  }

  .info-list {
    .info-item {
      @include flex-between;
      padding: $spacing-sm 0;
      border-bottom: 1px dashed $color-border-light;

      &:last-child {
        border-bottom: none;
      }

      .label {
        color: $color-text-secondary;
        font-size: $font-size-sm;
      }

      .value {
        color: $color-text-primary;
      }
    }
  }
}

.action-section {
  @include flex-center;
  gap: $spacing-md;
}
</style>
