<template>
  <div class="genealogy-page">
    <!-- 顶部导航 -->
    <header class="header">
      <div class="header-left">
        <h1 class="logo">MyFamily</h1>
      </div>
      <div class="header-right">
        <el-dropdown @command="handleCommand">
          <span class="user-info">
            <el-avatar :src="userStore.userAvatar" :size="36">
              {{ userStore.userName?.charAt(0) }}
            </el-avatar>
            <span class="username">{{ userStore.userName }}</span>
            <el-icon><ArrowDown /></el-icon>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="profile">个人主页</el-dropdown-item>
              <el-dropdown-item command="settings">设置</el-dropdown-item>
              <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </header>

    <div class="main-layout">
      <!-- 侧边栏 -->
      <aside class="sidebar">
        <nav class="nav-menu">
          <router-link to="/genealogy" class="nav-item active">
            <el-icon><Document /></el-icon>
            <span>族谱</span>
          </router-link>
          <router-link to="/member/add" class="nav-item">
            <el-icon><Plus /></el-icon>
            <span>成员</span>
          </router-link>
          <router-link to="/mail" class="nav-item">
            <el-icon><Message /></el-icon>
            <span>信箱</span>
          </router-link>
          <router-link to="/post" class="nav-item">
            <el-icon><Collection /></el-icon>
            <span>动态</span>
          </router-link>
          <router-link to="/memorial" class="nav-item">
            <el-icon><Goblet /></el-icon>
            <span>纪念堂</span>
          </router-link>
        </nav>
      </aside>

      <!-- 主内容区 -->
      <main class="main-content">
        <div class="genealogy-container">
          <!-- 族谱树工具栏 -->
          <div class="toolbar">
            <div class="toolbar-left">
              <el-button type="primary" @click="handleAddMember">
                <el-icon><Plus /></el-icon>
                添加成员
              </el-button>
            </div>
            <div class="toolbar-right">
              <el-button-group>
                <el-button @click="handleZoomOut" :icon="ZoomOut" />
                <el-button class="zoom-level">{{ Math.round(scale * 100) }}%</el-button>
                <el-button @click="handleZoomIn" :icon="ZoomIn" />
              </el-button-group>
              <el-button @click="handleResetView" :icon="FullScreen">重置</el-button>
            </div>
          </div>

          <!-- 族谱树画布 -->
          <div class="genealogy-canvas-wrapper">
            <div
              ref="canvasContainer"
              class="genealogy-canvas"
              @mousedown="handleMouseDown"
              @mousemove="handleMouseMove"
              @mouseup="handleMouseUp"
              @wheel="handleWheel"
            >
              <GenealogyCanvas
                v-if="genealogyStore.hasTreeData"
                :data="genealogyStore.treeData"
                :scale="scale"
                :offset-x="offsetX"
                :offset-y="offsetY"
                :selected-id="genealogyStore.selectedMemberId"
                @node-click="handleNodeClick"
                @node-dbclick="handleNodeDbClick"
              />
              <div v-else class="empty-state">
                <el-empty description="暂无族谱数据">
                  <el-button type="primary" @click="handleAddRoot">创建族谱</el-button>
                </el-empty>
              </div>
            </div>
          </div>
        </div>
      </main>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { useGenealogyStore } from '@/stores/genealogy'
import GenealogyCanvas from '@/components/genealogy/GenealogyCanvas.vue'
import {
  Document,
  Message,
  Collection,
  Goblet,
  Plus,
  ZoomIn,
  ZoomOut,
  FullScreen,
  ArrowDown
} from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()
const genealogyStore = useGenealogyStore()

const canvasContainer = ref<HTMLElement>()
const scale = ref(1)
const offsetX = ref(0)
const offsetY = ref(0)
const isDragging = ref(false)
const startX = ref(0)
const startY = ref(0)

// 缩放处理
function handleZoomIn() {
  scale.value = Math.min(2, scale.value + 0.1)
}

function handleZoomOut() {
  scale.value = Math.max(0.2, scale.value - 0.1)
}

function handleResetView() {
  scale.value = 1
  offsetX.value = 0
  offsetY.value = 0
}

// 拖拽处理
function handleMouseDown(e: MouseEvent) {
  isDragging.value = true
  startX.value = e.clientX - offsetX.value
  startY.value = e.clientY - offsetY.value
}

function handleMouseMove(e: MouseEvent) {
  if (!isDragging.value) return
  offsetX.value = e.clientX - startX.value
  offsetY.value = e.clientY - startY.value
}

function handleMouseUp() {
  isDragging.value = false
}

function handleWheel(e: WheelEvent) {
  e.preventDefault()
  const delta = e.deltaY > 0 ? -0.1 : 0.1
  scale.value = Math.max(0.2, Math.min(2, scale.value + delta))
}

// 节点操作
function handleNodeClick(node: any) {
  genealogyStore.selectMember(node.id)
}

function handleNodeDbClick(node: any) {
  router.push(`/member/${node.id}`)
}

function handleAddMember() {
  router.push('/member/add')
}

function handleAddRoot() {
  router.push('/member/add')
}

// 用户菜单
function handleCommand(command: string) {
  switch (command) {
    case 'profile':
      router.push('/profile')
      break
    case 'settings':
      router.push('/profile/edit')
      break
    case 'logout':
      ElMessageBox.confirm('确定要退出登录吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        userStore.logout()
      }).catch(() => {})
      break
  }
}

onMounted(() => {
  genealogyStore.fetchTreeData()
})
</script>

<style lang="scss" scoped>
@import '@/assets/styles/variables.scss';
@import '@/assets/styles/mixins.scss';

.genealogy-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
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
      letter-spacing: 2px;
    }
  }

  .header-right {
    .user-info {
      @include flex-center;
      gap: $spacing-xs;
      cursor: pointer;
      padding: $spacing-xs $spacing-sm;
      border-radius: $border-radius-md;
      transition: background $transition-fast ease;

      &:hover {
        background: $color-bg-hover;
      }

      .username {
        font-size: $font-size-sm;
        color: $color-text-regular;
      }
    }
  }
}

.main-layout {
  display: flex;
  flex: 1;
}

.sidebar {
  width: $sidebar-width;
  background: $color-bg-card;
  border-right: 1px solid $color-border-light;
  padding: $spacing-md 0;
  position: sticky;
  top: $header-height;
  height: calc(100vh - #{$header-height});

  @include mobile {
    display: none;
  }
}

.nav-menu {
  .nav-item {
    display: flex;
    align-items: center;
    gap: $spacing-sm;
    padding: 14px $spacing-md;
    color: $color-text-regular;
    font-size: $font-size-base;
    font-family: $font-family-serif;
    border-left: 3px solid transparent;
    transition: all $transition-normal ease;

    &:hover {
      background: $color-bg-hover;
      color: $color-text-primary;
    }

    &.active {
      background: rgba($color-primary, 0.1);
      border-left-color: $color-primary;
      color: $color-primary;
    }
  }
}

.main-content {
  flex: 1;
  padding: $spacing-md;
  @include paper-background;
}

.genealogy-container {
  height: calc(100vh - #{$header-height} - #{$spacing-md * 2});
  display: flex;
  flex-direction: column;
}

.toolbar {
  @include flex-between;
  margin-bottom: $spacing-md;
  padding: $spacing-sm;
  background: $color-bg-card;
  border-radius: $border-radius-md;
  box-shadow: $shadow-sm;

  .toolbar-right {
    display: flex;
    gap: $spacing-sm;

    .zoom-level {
      min-width: 60px;
      font-size: $font-size-sm;
    }
  }
}

.genealogy-canvas-wrapper {
  flex: 1;
  background: $color-bg-card;
  border-radius: $border-radius-md;
  box-shadow: $shadow-md;
  overflow: hidden;
  position: relative;
}

.genealogy-canvas {
  width: 100%;
  height: 100%;
  cursor: grab;
  @include paper-background;

  &:active {
    cursor: grabbing;
  }
}

.empty-state {
  @include flex-center;
  height: 100%;
}
</style>
