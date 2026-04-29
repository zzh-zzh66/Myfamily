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
      <main class="main-content" :class="{ 'panel-open': genealogyStore.selectedMemberId }">
        <div class="genealogy-container">
          <!-- 族谱树工具栏 -->
          <div class="toolbar">
            <div class="toolbar-left">
              <el-button v-if="isAdmin" type="primary" @click="handleAddMember">
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
              @mouseleave="handleMouseLeave"
              @wheel="handleWheel"
            >
              <GenealogyCanvas
                v-if="genealogyStore.hasTreeData"
                :key="canvasKey"
                :couple-nodes-map="genealogyStore.coupleNodesMap"
                :parent-children-map="genealogyStore.parentChildrenMap"
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

        <!-- 侧边详情面板 -->
        <MemberDetailPanel
          :visible="!!genealogyStore.selectedMemberId"
          :member-id="genealogyStore.selectedMemberId"
          @close="handlePanelClose"
          @navigate="handlePanelNavigate"
          @deleted="handlePanelDeleted"
        />
      </main>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, onMounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { useGenealogyStore } from '@/stores/genealogy'
import GenealogyCanvas from '@/components/genealogy/GenealogyCanvas.vue'
import MemberDetailPanel from '@/components/genealogy/MemberDetailPanel.vue'
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
const canvasKey = ref(0)
const scale = ref(1)
const offsetX = ref(0)
const offsetY = ref(0)
const isDragging = ref(false)
const startX = ref(0)
const startY = ref(0)

const isAdmin = computed(() => userStore.userInfo?.role?.toUpperCase() === 'ADMIN')

const CANVAS_CENTER_X = 500
const CANVAS_CENTER_Y = 100
const ROOT_X_OFFSET = 600
const ROOT_Y_OFFSET = 80

function calculateCentering() {
  if (!genealogyStore.hasTreeData || !canvasContainer.value) return

  nextTick(() => {
    const container = canvasContainer.value
    if (!container) return

    const containerWidth = container.clientWidth
    const containerHeight = container.clientHeight

    const rootX = ROOT_X_OFFSET + 100  // ROOT_X_OFFSET (600) + half couple node width (100)
    const rootY = ROOT_Y_OFFSET + 35   // ROOT_Y_OFFSET (80) + half node height

    offsetX.value = containerWidth / 2 - rootX * scale.value
    offsetY.value = containerHeight / 4 - rootY * scale.value
  })
}

watch(() => genealogyStore.hasTreeData, (hasData) => {
  if (hasData) {
    calculateCentering()
  }
})

function handleZoomIn() {
  const rect = canvasContainer.value?.getBoundingClientRect()
  if (!rect) {
    scale.value = Math.min(2, scale.value + 0.1)
    return
  }

  const centerX = rect.width / 2
  const centerY = rect.height / 4

  const oldScale = scale.value
  const newScale = Math.min(2, scale.value + 0.1)

  offsetX.value = centerX - (centerX - offsetX.value) * (newScale / oldScale)
  offsetY.value = centerY - (centerY - offsetY.value) * (newScale / oldScale)
  scale.value = newScale
}

function handleZoomOut() {
  const rect = canvasContainer.value?.getBoundingClientRect()
  if (!rect) {
    scale.value = Math.max(0.2, scale.value - 0.1)
    return
  }

  const centerX = rect.width / 2
  const centerY = rect.height / 4

  const oldScale = scale.value
  const newScale = Math.max(0.2, scale.value - 0.1)

  offsetX.value = centerX - (centerX - offsetX.value) * (newScale / oldScale)
  offsetY.value = centerY - (centerY - offsetY.value) * (newScale / oldScale)
  scale.value = newScale
}

function handleResetView() {
  scale.value = 1
  calculateCentering()
}

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

function handleMouseLeave() {
  isDragging.value = false
}

function handleWheel(e: WheelEvent) {
  e.preventDefault()
  const rect = canvasContainer.value?.getBoundingClientRect()
  if (!rect) return

  const mouseX = e.clientX - rect.left
  const mouseY = e.clientY - rect.top

  const oldScale = scale.value
  const delta = e.deltaY > 0 ? -0.1 : 0.1
  const newScale = Math.max(0.2, Math.min(2, scale.value + delta))

  offsetX.value = mouseX - (mouseX - offsetX.value) * (newScale / oldScale)
  offsetY.value = mouseY - (mouseY - offsetY.value) * (newScale / oldScale)
  scale.value = newScale
}

function handleNodeClick(memberId: number) {
  genealogyStore.selectMember(memberId)
}

function handleNodeDbClick(memberId: number) {
  router.push(`/member/${memberId}`)
}

function handleAddMember() {
  router.push('/member/add')
}

function handleAddRoot() {
  router.push('/member/add')
}

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

function handlePanelClose() {
  genealogyStore.clearSelection()
}

function handlePanelNavigate(memberId: number) {
  genealogyStore.selectMember(memberId)
}

function handlePanelDeleted() {
  genealogyStore.clearSelection()
  genealogyStore.fetchTreeData().then(() => {
    canvasKey.value++
    nextTick(() => {
      const canvas = document.querySelector('.genealogy-canvas')
      if (canvas) {
        console.log('[GenealogyTreeView] 强制刷新画布')
      }
    })
  })
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
  transition: margin-right 0.3s ease;

  &.panel-open {
    margin-right: 380px;
  }
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
