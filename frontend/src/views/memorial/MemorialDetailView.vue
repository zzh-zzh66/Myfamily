<template>
  <div class="memorial-detail-page">
    <div class="page-header">
      <el-button text @click="goBack">
        <el-icon><ArrowLeft /></el-icon>
        返回纪念堂
      </el-button>
    </div>

    <div v-if="loading" class="loading-state">
      <el-skeleton :rows="10" animated />
    </div>

    <div v-else-if="memorial" class="memorial-content">
      <div class="memorial-card">
        <div class="avatar-section">
          <el-avatar
            :src="memorial.photoUrl || memorial.avatar"
            :size="150"
            class="avatar"
          >
            {{ memorial.name?.charAt(0) }}
          </el-avatar>
        </div>

        <div class="memorial-header">
          <h1 class="name">{{ memorial.name }}</h1>
          <div class="title-badge">{{ memorial.title }}</div>
          <div class="years">
            {{ formatYear(memorial.birthDate) }} - {{ formatYear(memorial.deathDate) }}
          </div>
        </div>
      </div>

      <div class="detail-card" v-if="memorial.bio">
        <h3 class="card-title">人物简介</h3>
        <div class="bio">{{ memorial.bio }}</div>
      </div>

      <div class="detail-card" v-if="parsedAchievements.length > 0">
        <h3 class="card-title">主要成就</h3>
        <ul class="achievement-list">
          <li v-for="(item, index) in parsedAchievements" :key="index" class="achievement-item">
            <span class="achievement-index">{{ index + 1 }}.</span>
            <span class="achievement-text">{{ item }}</span>
          </li>
        </ul>
      </div>

      <div class="action-buttons">
        <el-button @click="goBack">
          <el-icon><ArrowLeft /></el-icon>
          返回纪念堂
        </el-button>
      </div>
    </div>

    <div v-else class="empty-state">
      <el-empty description="纪念人物不存在" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getMemorialDetail } from '@/api/memorial'
import { ArrowLeft } from '@element-plus/icons-vue'
import type { Memorial } from '@/types/api'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const memorial = ref<Memorial | null>(null)

const parsedAchievements = computed<string[]>(() => {
  if (!memorial.value?.achievement) return []
  try {
    if (Array.isArray(memorial.value.achievement)) {
      return memorial.value.achievement
    }
    return JSON.parse(memorial.value.achievement as any)
  } catch {
    return []
  }
})

function formatYear(dateStr?: string): string {
  if (!dateStr) return '未知'
  return dateStr.split('-')[0]
}

async function fetchMemorialDetail() {
  const id = Number(route.params.id)
  if (!id) return

  loading.value = true
  try {
    const res = await getMemorialDetail(id)
    if (res.data) {
      memorial.value = res.data
    }
  } finally {
    loading.value = false
  }
}

function goBack() {
  router.push('/memorial')
}

onMounted(() => {
  fetchMemorialDetail()
})
</script>

<style lang="scss" scoped>
@import '@/assets/styles/variables.scss';
@import '@/assets/styles/mixins.scss';

.memorial-detail-page {
  @include paper-background;
  min-height: 100vh;
  padding: $spacing-md;
}

.page-header {
  margin-bottom: $spacing-lg;

  :deep(.el-button) {
    font-family: $font-family-serif;
  }
}

.loading-state {
  @include card;
  padding: $spacing-lg;
}

.memorial-content {
  max-width: 800px;
  margin: 0 auto;
}

.memorial-card {
  @include card;
  padding: $spacing-xl;
  text-align: center;
  margin-bottom: $spacing-md;
  background: linear-gradient(135deg, rgba($color-primary, 0.05) 0%, rgba($color-primary, 0.02) 100%);
  border: 2px solid rgba($color-primary, 0.1);

  .avatar-section {
    margin-bottom: $spacing-lg;

    .avatar {
      border: 4px solid $color-border-light;
      font-size: 60px;
      font-family: $font-family-serif;
    }
  }

  .memorial-header {
    .name {
      font-family: $font-family-decorative;
      font-size: 36px;
      color: $color-primary;
      margin-bottom: $spacing-sm;
    }

    .title-badge {
      display: inline-block;
      padding: 6px 20px;
      background: rgba($color-primary, 0.1);
      color: $color-primary;
      border-radius: $border-radius-full;
      font-size: $font-size-base;
      margin-bottom: $spacing-sm;
    }

    .years {
      color: $color-text-secondary;
      font-size: $font-size-lg;
    }
  }
}

.detail-card {
  @include card;
  padding: $spacing-lg;
  margin-bottom: $spacing-md;

  .card-title {
    font-family: $font-family-serif;
    font-size: $font-size-lg;
    margin-bottom: $spacing-md;
    padding-bottom: $spacing-sm;
    border-bottom: 2px solid rgba($color-primary, 0.2);
    color: $color-primary;
  }

  .bio {
    color: $color-text-regular;
    line-height: $line-height-base;
    white-space: pre-wrap;
  }
}

.achievement-list {
  list-style: none;
  padding: 0;
  margin: 0;

  .achievement-item {
    display: flex;
    align-items: flex-start;
    padding: $spacing-sm 0;
    padding-left: $spacing-md;
    border-left: 3px solid $color-primary;
    margin-bottom: $spacing-sm;
    line-height: $line-height-base;

    .achievement-index {
      color: $color-primary;
      font-weight: bold;
      margin-right: $spacing-sm;
      flex-shrink: 0;
    }

    .achievement-text {
      color: $color-text-regular;
    }
  }
}

.action-buttons {
  @include flex-center;
  gap: $spacing-md;
  margin-top: $spacing-lg;
}
</style>