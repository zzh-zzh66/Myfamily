<template>
  <div class="memorial-page">
    <header class="header">
      <div class="header-left">
        <h1 class="logo" @click="router.push('/genealogy')">MyFamily</h1>
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
      <!-- 页面标题 -->
      <div class="page-title">
        <h1>纪念堂</h1>
        <p class="subtitle">家族先贤  精神传承</p>
      </div>

      <!-- 水墨山水背景装饰 -->
      <div class="ink-decoration"></div>

      <!-- 纪念人物列表 -->
      <div class="memorial-grid" v-loading="loading">
        <div
          v-for="person in memorialList"
          :key="person.id"
          class="memorial-card"
        >
          <div class="memorial-avatar">
            <el-avatar :src="person.avatar" :size="120" fit="cover">
              {{ person.name?.charAt(0) }}
            </el-avatar>
          </div>
          <div class="memorial-info">
            <h3 class="memorial-name">{{ person.name }}</h3>
            <div class="memorial-years">
              {{ person.birthYear }} - {{ person.deathYear }}
            </div>
            <div class="memorial-title">{{ person.title }}</div>
          </div>
          <div class="memorial-bio" v-if="person.biography">
            {{ truncateText(person.biography, 60) }}
          </div>
          <el-button text @click="viewDetail(person)">
            查看详情
            <el-icon><ArrowRight /></el-icon>
          </el-button>
        </div>

        <el-empty v-if="!loading && memorialList.length === 0" description="暂无纪念人物">
          <el-button type="primary">添加纪念人物</el-button>
        </el-empty>
      </div>
    </main>

    <!-- 底部印章装饰 -->
    <footer class="footer">
      <div class="seal">MyFamily</div>
    </footer>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { truncateText } from '@/utils/format'
import { ArrowRight } from '@element-plus/icons-vue'
import type { Memorial } from '@/types/api'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const memorialList = ref<Memorial[]>([])

async function fetchMemorialList() {
  loading.value = true
  try {
    // TODO: 替换为实际的API调用
    // const res = await getMemorialList()
    // memorialList.value = res.data.records

    // 临时模拟数据
    memorialList.value = []
  } finally {
    loading.value = false
  }
}

function viewDetail(person: Memorial) {
  // 跳转到成员详情页
  router.push(`/member/${person.memberId}`)
}

function handleCommand(command: string) {
  if (command === 'logout') {
    userStore.logout()
  } else if (command === 'profile') {
    router.push('/profile')
  }
}

onMounted(() => {
  fetchMemorialList()
})
</script>

<style lang="scss" scoped>
@import '@/assets/styles/variables.scss';
@import '@/assets/styles/mixins.scss';

.memorial-page {
  min-height: 100vh;
  @include paper-background;
  position: relative;
  overflow: hidden;
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
      cursor: pointer;
    }
  }

  .header-right {
    .user-info {
      cursor: pointer;
    }
  }
}

.main-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: $spacing-xl $spacing-md;
  position: relative;
  z-index: 1;
}

.page-title {
  text-align: center;
  margin-bottom: $spacing-xl;

  h1 {
    font-family: $font-family-decorative;
    font-size: 48px;
    color: $color-primary;
    letter-spacing: 8px;
    margin-bottom: $spacing-sm;
  }

  .subtitle {
    font-family: $font-family-serif;
    font-size: $font-size-lg;
    color: $color-text-secondary;
    letter-spacing: 4px;
  }
}

.ink-decoration {
  position: absolute;
  top: 100px;
  left: 0;
  right: 0;
  height: 300px;
  background: linear-gradient(
    180deg,
    transparent 0%,
    rgba($color-primary, 0.02) 50%,
    transparent 100%
  );
  pointer-events: none;
}

.memorial-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: $spacing-lg;
  margin-top: $spacing-xl;
}

.memorial-card {
  @include card;
  padding: $spacing-lg;
  text-align: center;
  transition: transform $transition-normal ease;

  &:hover {
    transform: translateY(-8px);

    .memorial-avatar {
      .el-avatar {
        transform: scale(1.05);
      }
    }
  }
}

.memorial-avatar {
  margin-bottom: $spacing-md;

  .el-avatar {
    border: 4px solid $color-border-light;
    transition: transform $transition-normal ease;
  }
}

.memorial-info {
  margin-bottom: $spacing-md;

  .memorial-name {
    font-family: $font-family-serif;
    font-size: $font-size-xl;
    margin-bottom: $spacing-xs;
  }

  .memorial-years {
    color: $color-text-secondary;
    font-size: $font-size-sm;
    margin-bottom: $spacing-xs;
  }

  .memorial-title {
    display: inline-block;
    padding: 4px 16px;
    background: rgba($color-primary, 0.1);
    color: $color-primary;
    border-radius: $border-radius-full;
    font-size: $font-size-xs;
  }
}

.memorial-bio {
  color: $color-text-regular;
  font-size: $font-size-sm;
  line-height: $line-height-base;
  margin-bottom: $spacing-md;
}

.footer {
  text-align: center;
  padding: $spacing-xl 0;
  margin-top: $spacing-xl;

  .seal {
    display: inline-block;
    width: 80px;
    height: 80px;
    line-height: 80px;
    text-align: center;
    background: $color-primary;
    color: white;
    font-family: $font-family-decorative;
    font-size: $font-size-sm;
    transform: rotate(-5deg);
    border-radius: $border-radius-sm;
    box-shadow: 0 4px 12px rgba($color-primary, 0.3);
  }
}
</style>
