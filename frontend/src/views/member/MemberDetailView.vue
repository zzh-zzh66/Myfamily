<template>
  <div class="member-detail-page">
    <!-- 返回导航 -->
    <div class="page-header">
      <el-button text @click="goBack">
        <el-icon><ArrowLeft /></el-icon>
        返回族谱树
      </el-button>
    </div>

    <div v-if="loading" class="loading-state">
      <el-skeleton :rows="10" animated />
    </div>

    <div v-else-if="member" class="member-content">
      <!-- 基本信息卡片 -->
      <div class="info-card">
        <div class="avatar-section">
          <el-avatar
            :src="member.avatar"
            :size="120"
            class="avatar"
            :class="member.gender"
          >
            {{ member.name?.charAt(0) }}
          </el-avatar>
        </div>

        <div class="basic-info">
          <h1 class="name">{{ member.name }}</h1>
          <div class="generation-badge">第{{ member.generation }}代</div>
          <div class="gender-badge" :class="member.gender">
            {{ member.gender === 'male' ? '男' : '女' }}
          </div>
        </div>
      </div>

      <!-- 详细信息 -->
      <div class="detail-card">
        <h3 class="card-title">详细信息</h3>
        <div class="detail-list">
          <div class="detail-item">
            <span class="label">出生日期</span>
            <span class="value">{{ member.birthDate || '未知' }}</span>
          </div>
          <div class="detail-item" v-if="member.deathDate">
            <span class="label">逝世日期</span>
            <span class="value">{{ member.deathDate }}</span>
          </div>
          <div class="detail-item">
            <span class="label">籍贯</span>
            <span class="value">{{ member.birthplace || '未知' }}</span>
          </div>
          <div class="detail-item">
            <span class="label">配偶</span>
            <span class="value">{{ member.spouse || '未知' }}</span>
          </div>
        </div>
      </div>

      <!-- 生平事迹 -->
      <div class="detail-card" v-if="member.biography">
        <h3 class="card-title">生平事迹</h3>
        <div class="biography">{{ member.biography }}</div>
      </div>

      <!-- 成就 -->
      <div class="detail-card" v-if="member.achievements">
        <h3 class="card-title">主要成就</h3>
        <div class="achievements">{{ member.achievements }}</div>
      </div>

      <!-- 操作按钮 -->
      <div class="action-buttons">
        <el-button type="primary" @click="handleEdit">
          <el-icon><Edit /></el-icon>
          编辑
        </el-button>
        <el-button @click="handleSendMail">
          <el-icon><Message /></el-icon>
          发送邮件
        </el-button>
        <el-button type="danger" text @click="handleDelete">
          <el-icon><Delete /></el-icon>
          删除
        </el-button>
      </div>
    </div>

    <div v-else class="empty-state">
      <el-empty description="成员不存在" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useGenealogyStore } from '@/stores/genealogy'
import { deleteMember } from '@/api/member'
import { ArrowLeft, Edit, Message, Delete } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const genealogyStore = useGenealogyStore()

const loading = ref(false)
const member = ref<any>(null)

async function fetchMemberDetail() {
  const id = Number(route.params.id)
  if (!id) return

  loading.value = true
  try {
    await genealogyStore.fetchMemberDetail(id)
    member.value = genealogyStore.selectedMember
  } finally {
    loading.value = false
  }
}

function goBack() {
  router.push('/genealogy')
}

function handleEdit() {
  router.push(`/member/edit/${route.params.id}`)
}

function handleSendMail() {
  router.push({ path: '/mail/compose', query: { to: String(member.value?.id) } })
}

async function handleDelete() {
  try {
    await ElMessageBox.confirm('确定要删除该成员吗？此操作不可恢复。', '警告', {
      confirmButtonText: '确定删除',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await deleteMember(Number(route.params.id))
    ElMessage.success('删除成功')
    router.push('/genealogy')
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

onMounted(() => {
  fetchMemberDetail()
})
</script>

<style lang="scss" scoped>
@import '@/assets/styles/variables.scss';
@import '@/assets/styles/mixins.scss';

.member-detail-page {
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

.member-content {
  max-width: 800px;
  margin: 0 auto;
}

.info-card {
  @include card;
  padding: $spacing-xl;
  text-align: center;
  margin-bottom: $spacing-md;

  .avatar-section {
    margin-bottom: $spacing-md;

    .avatar {
      border: 4px solid $color-border-light;
      font-size: 48px;
      font-family: $font-family-serif;

      &.male {
        border-color: $color-male;
      }

      &.female {
        border-color: $color-female;
      }
    }
  }

  .basic-info {
    .name {
      font-family: $font-family-serif;
      font-size: $font-size-2xl;
      margin-bottom: $spacing-sm;
    }

    .generation-badge {
      display: inline-block;
      padding: 4px 16px;
      background: rgba($color-primary, 0.1);
      color: $color-primary;
      border-radius: $border-radius-full;
      font-size: $font-size-sm;
      margin-right: $spacing-sm;
    }

    .gender-badge {
      display: inline-block;
      padding: 4px 16px;
      border-radius: $border-radius-full;
      font-size: $font-size-sm;
      color: white;

      &.male {
        background: $color-male;
      }

      &.female {
        background: $color-female;
      }
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
    border-bottom: 1px solid $color-border-light;
  }

  .detail-list {
    .detail-item {
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

  .biography,
  .achievements {
    color: $color-text-regular;
    line-height: $line-height-base;
    white-space: pre-wrap;
  }
}

.action-buttons {
  @include flex-center;
  gap: $spacing-md;
  margin-top: $spacing-lg;
}
</style>
