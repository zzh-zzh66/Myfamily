<template>
  <transition name="slide-right">
    <div v-if="visible" class="member-detail-panel">
      <div class="panel-header">
        <h3 class="panel-title">人物详情</h3>
        <el-button class="close-btn" text @click="handleClose">
          <el-icon><Close /></el-icon>
        </el-button>
      </div>

      <div class="panel-body" v-loading="loading">
        <template v-if="member">
          <div class="member-info">
            <div class="info-section">
              <h4 class="section-title">基本信息</h4>
              <div class="info-grid">
                <div class="info-item">
                  <span class="info-label">姓名</span>
                  <span class="info-value">{{ member.name }}</span>
                </div>
                <div class="info-item">
                  <span class="info-label">性别</span>
                  <span class="info-value">{{ member.gender === 'male' ? '男' : '女' }}</span>
                </div>
                <div class="info-item">
                  <span class="info-label">辈分</span>
                  <span class="info-value">第{{ member.generation }}代</span>
                </div>
                <div class="info-item">
                  <span class="info-label">状态</span>
                  <span class="info-value" :class="isAlive ? 'alive' : 'deceased'">
                    {{ isAlive ? '在世' : '已故' }}
                  </span>
                </div>
                <div class="info-item">
                  <span class="info-label">年龄</span>
                  <span class="info-value">{{ age }}岁</span>
                </div>
                <div v-if="member.birthDate" class="info-item">
                  <span class="info-label">出生日期</span>
                  <span class="info-value">{{ formatDate(member.birthDate) }}</span>
                </div>
                <div v-if="member.deathDate" class="info-item">
                  <span class="info-label">逝世日期</span>
                  <span class="info-value">{{ formatDate(member.deathDate) }}</span>
                </div>
                <div v-if="member.birthplace" class="info-item full-width">
                  <span class="info-label">出生地</span>
                  <span class="info-value">{{ member.birthplace }}</span>
                </div>
              </div>
            </div>

            <div class="info-section">
              <h4 class="section-title">亲属关系</h4>
              <div class="info-grid">
                <div v-if="fatherMember" class="info-item clickable" @click="handleNavigateToMember(fatherMember.id)">
                  <span class="info-label">父亲</span>
                  <span class="info-value link">{{ fatherMember.name }}</span>
                </div>
                <div v-if="motherMember" class="info-item clickable" @click="handleNavigateToMember(motherMember.id)">
                  <span class="info-label">母亲</span>
                  <span class="info-value link">{{ motherMember.name }}</span>
                </div>
                <div v-if="spouseMember" class="info-item clickable" @click="handleNavigateToMember(spouseMember.id)">
                  <span class="info-label">配偶</span>
                  <span class="info-value link">{{ spouseMember.name }}</span>
                </div>
              </div>
            </div>

            <div v-if="childrenMembers.length > 0" class="info-section">
              <h4 class="section-title">子女 ({{ childrenMembers.length }})</h4>
              <div class="children-list">
                <div
                  v-for="child in childrenMembers"
                  :key="child.id"
                  class="child-item clickable"
                  @click="handleNavigateToMember(child.id)"
                >
                  <span class="child-name">{{ child.name }}</span>
                  <span class="child-gender">{{ child.gender === 'male' ? '男' : '女' }}</span>
                </div>
              </div>
            </div>

            <div v-if="member.biography" class="info-section">
              <h4 class="section-title">传记</h4>
              <p class="biography-text">{{ member.biography }}</p>
            </div>
          </div>

          <div class="panel-footer">
            <div class="debug-info">用户角色: {{ userStore.userInfo?.role || '未登录' }}</div>
            <el-button
              v-if="isAdmin"
              type="danger"
              @click="handleShowDeleteOptions"
            >
              <el-icon><Delete /></el-icon>
              删除成员
            </el-button>
            <el-button
              v-else
              type="info"
              disabled
            >
              仅管理员可删除
            </el-button>
          </div>
        </template>

        <el-empty v-else description="暂无数据" />
      </div>

      <el-dialog
        v-model="deleteDialogVisible"
        title="删除成员"
        width="480px"
        :close-on-click-modal="false"
      >
        <template v-if="isCoupleNode">
          <template v-if="hasChildren">
            <p class="delete-tip">该成员处于夫妻关系节点中且有子女，请选择删除方式：</p>
            <div class="delete-options">
              <el-button class="delete-option-btn" @click="handleDivorce">
                <el-icon><Remove /></el-icon>
                离婚（移除一方，子女关系保持不变）
              </el-button>
              <el-button class="delete-option-btn" type="danger" @click="handleKickOut">
                <el-icon><CircleClose /></el-icon>
                踢出族谱（标记为无名氏）
              </el-button>
            </div>
          </template>
          <template v-else>
            <p class="delete-tip">该成员处于夫妻关系节点中，无子女，可选择：</p>
            <div class="delete-options">
              <el-button class="delete-option-btn" @click="handleDivorceNoChildren">
                <el-icon><Remove /></el-icon>
                移除该成员（保留配偶）
              </el-button>
              <el-button class="delete-option-btn" type="danger" @click="handleKickOut">
                <el-icon><CircleClose /></el-icon>
                删除整个夫妻节点
              </el-button>
            </div>
          </template>
        </template>
        <template v-else>
          <p class="delete-tip">确定要删除该成员吗？此操作不可恢复。</p>
          <p v-if="hasChildren" class="delete-warning">该成员有子女，删除后子女的父母关系将被清除。</p>
          <div class="delete-single-options">
            <el-button type="danger" @click="handleDeleteSingle">
              <el-icon><Delete /></el-icon>
              确认删除
            </el-button>
          </div>
        </template>
        <template #footer>
          <el-button @click="deleteDialogVisible = false">取消</el-button>
        </template>
      </el-dialog>

      <el-dialog
        v-model="divorceDialogVisible"
        title="离婚操作"
        width="480px"
        :close-on-click-modal="false"
      >
        <p class="delete-tip">请选择要移除的一方：</p>
        <div class="divorce-options">
          <div
            v-if="member"
            class="divorce-option"
            @click="handleConfirmDivorce(member)"
          >
            <div class="member-avatar">
              {{ member.name.charAt(0) }}
            </div>
            <div class="member-info">
              <div class="member-name">{{ member.name }}</div>
              <div class="member-desc">移除该成员，保留配偶</div>
            </div>
          </div>
          <div
            v-if="spouseMember"
            class="divorce-option"
            @click="handleConfirmDivorce(spouseMember)"
          >
            <div class="member-avatar">
              {{ spouseMember.name.charAt(0) }}
            </div>
            <div class="member-info">
              <div class="member-name">{{ spouseMember.name }}</div>
              <div class="member-desc">移除配偶，保留该成员</div>
            </div>
          </div>
        </div>
        <template #footer>
          <el-button @click="divorceDialogVisible = false">取消</el-button>
        </template>
      </el-dialog>

      <el-dialog
        v-model="kickOutDialogVisible"
        title="踢出族谱"
        width="480px"
        :close-on-click-modal="false"
      >
        <p class="delete-tip">此操作将删除该夫妻节点。</p>
        <p v-if="hasChildren" class="delete-warning">该节点有子女，删除后将标记父母为"无名氏"以保持族谱完整。</p>
        <p v-else class="delete-tip">确定要将两人从族谱中移除吗？此操作不可恢复。</p>
        <template #footer>
          <el-button @click="kickOutDialogVisible = false">取消</el-button>
          <el-button type="danger" @click="handleConfirmKickOut">确定踢出</el-button>
        </template>
      </el-dialog>
    </div>
  </transition>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Close, Delete, Remove, CircleClose } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { useGenealogyStore } from '@/stores/genealogy'
import { deleteMember, updateMember, clearSpouseRelation } from '@/api/member'
import type { Member, CoupleNode } from '@/types/api'

const props = defineProps<{
  visible: boolean
  memberId: number | null
}>()

const emit = defineEmits<{
  (e: 'close'): void
  (e: 'navigate', memberId: number): void
  (e: 'deleted'): void
}>()

const router = useRouter()
const userStore = useUserStore()
const genealogyStore = useGenealogyStore()

const loading = ref(false)
const deleteDialogVisible = ref(false)
const divorceDialogVisible = ref(false)
const kickOutDialogVisible = ref(false)
const currentCoupleNode = ref<CoupleNode | null>(null)

const member = computed(() => {
  if (!props.memberId) return null
  return genealogyStore.flatMembers.find(m => m.id === props.memberId) || null
})

const isAdmin = computed(() => userStore.userInfo?.role?.toUpperCase() === 'ADMIN')

const isAlive = computed(() => {
  if (!member.value) return true
  return !member.value.deathDate
})

const age = computed(() => {
  if (!member.value || !member.value.birthDate) return '未知'
  const birth = new Date(member.value.birthDate)
  const end = member.value.deathDate ? new Date(member.value.deathDate) : new Date()
  const ageNum = end.getFullYear() - birth.getFullYear()
  return Math.max(0, ageNum)
})

const isCoupleNode = computed(() => {
  if (!member.value) return false
  return !!member.value.spouseId
})

const fatherMember = computed(() => {
  if (!member.value?.fatherId) return null
  return genealogyStore.flatMembers.find(m => m.id === member.value!.fatherId) || null
})

const motherMember = computed(() => {
  if (!member.value?.motherId) return null
  return genealogyStore.flatMembers.find(m => m.id === member.value!.motherId) || null
})

const spouseMember = computed(() => {
  if (!member.value?.spouseId) return null
  return genealogyStore.flatMembers.find(m => m.id === member.value!.spouseId) || null
})

const hasChildren = computed(() => {
  if (!props.memberId) return false
  return genealogyStore.flatMembers.some(m => 
    m.fatherId === props.memberId || m.motherId === props.memberId
  )
})

const childrenMembers = computed(() => {
  if (!props.memberId) return []
  return genealogyStore.flatMembers.filter(m => 
    m.fatherId === props.memberId || m.motherId === props.memberId
  )
})

function formatDate(dateStr: string): string {
  const date = new Date(dateStr)
  return date.toLocaleDateString('zh-CN')
}

function getCoupleNodeByMemberId(memberId: number): CoupleNode | null {
  for (const nodes of genealogyStore.coupleNodesMap.values()) {
    for (const node of nodes) {
      if (node.male?.id === memberId || node.female?.id === memberId) {
        return node
      }
    }
  }
  return null
}

function handleClose() {
  emit('close')
}

function handleNavigateToMember(id: number) {
  emit('navigate', id)
}

function handleShowDeleteOptions() {
  currentCoupleNode.value = getCoupleNodeByMemberId(props.memberId!)
  deleteDialogVisible.value = true
}

async function handleDivorce() {
  deleteDialogVisible.value = false
  divorceDialogVisible.value = true
}

async function handleDivorceNoChildren() {
  deleteDialogVisible.value = false
  
  if (!member.value || !member.value.spouseId) return

  const memberName = member.value.name

  try {
    await ElMessageBox.confirm(
      `确定要将 ${memberName} 从夫妻关系中移除吗？移除后配偶将变为单身状态。`,
      '确认移除操作',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    loading.value = true
    console.log('[DivorceNoChildren] 开始离婚操作，移除:', memberName)

    await clearSpouseRelation(member.value.spouseId)
    console.log('[DivorceNoChildren] 清除配偶关系完成')

    await deleteMember(member.value!.id)
    console.log('[DivorceNoChildren] 删除成员完成')

    ElMessage.success('移除成功')
    emit('deleted')
    console.log('[DivorceNoChildren] 触发 deleted 事件')
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('[DivorceNoChildren] 操作失败:', error)
      ElMessage.error('移除失败：' + (error.message || '未知错误'))
    }
  } finally {
    loading.value = false
  }
}

async function handleConfirmDivorce(memberToRemove: Member) {
  try {
    await ElMessageBox.confirm(
      `确定要将 ${memberToRemove.name} 从夫妻关系中移除吗？`,
      '确认离婚操作',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    loading.value = true
    console.log('[ConfirmDivorce] 开始离婚操作，移除:', memberToRemove.name)
    console.log('[ConfirmDivorce] 配偶 ID:', memberToRemove.spouseId)

    await clearSpouseRelation(memberToRemove.spouseId!)
    console.log('[ConfirmDivorce] 清除配偶关系完成')

    await deleteMember(memberToRemove.id)
    console.log('[ConfirmDivorce] 删除成员完成')

    ElMessage.success('离婚操作成功')
    divorceDialogVisible.value = false
    emit('deleted')
    console.log('[ConfirmDivorce] 触发 deleted 事件')
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('[ConfirmDivorce] 操作失败:', error)
      ElMessage.error('删除失败：' + (error.message || '未知错误'))
    }
  } finally {
    loading.value = false
  }
}

async function handleKickOut() {
  deleteDialogVisible.value = false
  kickOutDialogVisible.value = true
}

async function handleConfirmKickOut() {
  try {
    if (!member.value || !member.value.spouseId) return

    loading.value = true
    console.log('[KickOut] 开始踢出操作')

    const spouse = genealogyStore.flatMembers.find(m => m.id === member.value!.spouseId)
    
    if (hasChildren.value) {
      console.log('[KickOut] 有子女，标记为无名氏')
      await updateMember(member.value!.id, { 
        name: '无名氏',
        isVirtual: true,
        spouseId: null,
        spouse: ''
      })
      
      if (spouse) {
        await updateMember(spouse.id, { 
          name: '无名氏',
          isVirtual: true,
          spouseId: null,
          spouse: ''
        })
      }
    } else {
      console.log('[KickOut] 无子女，直接删除')
      await deleteMember(member.value!.id)
      
      if (spouse) {
        await deleteMember(spouse.id)
      }
    }

    ElMessage.success('踢出操作成功')
    kickOutDialogVisible.value = false
    emit('deleted')
    console.log('[KickOut] 触发 deleted 事件')
  } catch (error: any) {
    console.error('[KickOut] 操作失败:', error)
    ElMessage.error('操作失败：' + (error.message || '未知错误'))
  } finally {
    loading.value = false
  }
}

async function handleDeleteSingle() {
  try {
    await ElMessageBox.confirm(
      '确定要删除该成员吗？此操作不可恢复。',
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    loading.value = true
    console.log('[Delete] 开始删除成员 ID:', props.memberId)
    
    const response = await deleteMember(props.memberId!)
    console.log('[Delete] 删除响应:', response)
    
    ElMessage.success('删除成功')
    deleteDialogVisible.value = false
    console.log('[Delete] 触发 deleted 事件')
    emit('deleted')
    
    console.log('[Delete] 开始刷新族谱树')
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('[Delete] 删除失败:', error)
      ElMessage.error('删除失败：' + (error.message || '未知错误'))
    }
  } finally {
    loading.value = false
  }
}
</script>

<style lang="scss" scoped>
@import '@/assets/styles/variables.scss';

.member-detail-panel {
  position: fixed;
  top: 0;
  right: 0;
  width: 380px;
  height: 100vh;
  background: $color-bg-card;
  box-shadow: -4px 0 20px rgba(0, 0, 0, 0.15);
  z-index: 1000;
  display: flex;
  flex-direction: column;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: $spacing-md $spacing-lg;
  border-bottom: 1px solid $color-border-light;
  background: linear-gradient(135deg, $color-bg-card 0%, #F8F4EC 100%);

  .panel-title {
    margin: 0;
    font-size: $font-size-lg;
    font-weight: 600;
    color: $color-text-primary;
    font-family: $font-family-serif;
  }

  .close-btn {
    font-size: 20px;
    color: $color-text-secondary;
    padding: 0;
    &:hover {
      color: $color-text-primary;
    }
  }
}

.panel-body {
  flex: 1;
  overflow-y: auto;
  padding: $spacing-lg;
}

.info-section {
  margin-bottom: $spacing-lg;

  &:last-child {
    margin-bottom: 0;
  }

  .section-title {
    margin: 0 0 $spacing-md 0;
    font-size: $font-size-base;
    font-weight: 600;
    color: $color-text-primary;
    padding-bottom: $spacing-xs;
    border-bottom: 2px solid $color-primary;
    font-family: $font-family-serif;
  }
}

.info-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: $spacing-md;
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 4px;

  &.full-width {
    grid-column: 1 / -1;
  }

  &.clickable {
    cursor: pointer;
    padding: $spacing-sm;
    border-radius: $border-radius-sm;
    transition: all $transition-fast ease;

    &:hover {
      background: rgba($color-primary, 0.08);
    }

    .info-value {
      &.link {
        color: $color-primary;
        text-decoration: underline;
      }
    }
  }

  .info-label {
    font-size: $font-size-xs;
    color: $color-text-secondary;
  }

  .info-value {
    font-size: $font-size-sm;
    color: $color-text-regular;

    &.alive {
      color: #67C23A;
    }

    &.deceased {
      color: #909399;
    }
  }
}

.children-list {
  display: flex;
  flex-direction: column;
  gap: $spacing-sm;
}

.child-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: $spacing-sm $spacing-md;
  background: #F8F4EC;
  border-radius: $border-radius-sm;
  cursor: pointer;
  transition: all $transition-fast ease;

  &:hover {
    background: rgba($color-primary, 0.12);
    transform: translateX(4px);
  }

  .child-name {
    font-size: $font-size-sm;
    color: $color-text-regular;
    font-family: $font-family-serif;
  }

  .child-gender {
    font-size: $font-size-xs;
    color: $color-text-secondary;
  }
}

.biography-text {
  font-size: $font-size-sm;
  line-height: 1.8;
  color: $color-text-regular;
  margin: 0;
  text-indent: 2em;
}

.panel-footer {
  padding: $spacing-md $spacing-lg;
  border-top: 1px solid $color-border-light;
  display: flex;
  flex-direction: column;
  gap: $spacing-sm;
  align-items: center;

  .debug-info {
    font-size: $font-size-xs;
    color: $color-text-secondary;
    margin-bottom: $spacing-xs;
  }
}

.delete-tip {
  font-size: $font-size-sm;
  color: $color-text-regular;
  margin: 0 0 $spacing-md 0;
  line-height: 1.6;
}

.delete-warning {
  font-size: $font-size-sm;
  color: #F56C6C;
  margin: 0;
  line-height: 1.6;
}

.delete-options {
  display: flex;
  flex-direction: column;
  gap: $spacing-md;
  margin-top: $spacing-md;
}

.delete-single-options {
  display: flex;
  justify-content: center;
  margin-top: $spacing-md;
}

.delete-option-btn {
  width: 100%;
  height: 56px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: $spacing-sm;
  font-size: $font-size-base;
}

.divorce-options {
  display: flex;
  flex-direction: column;
  gap: $spacing-md;
  margin-top: $spacing-md;
}

.divorce-option {
  display: flex;
  align-items: center;
  gap: $spacing-md;
  padding: $spacing-md;
  background: #F8F4EC;
  border-radius: $border-radius-md;
  cursor: pointer;
  transition: all $transition-fast ease;
  border: 2px solid transparent;

  &:hover {
    background: rgba($color-primary, 0.08);
    border-color: $color-primary;
  }

  .member-avatar {
    width: 48px;
    height: 48px;
    border-radius: 50%;
    background: $color-primary;
    color: white;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: $font-size-lg;
    font-weight: 600;
    flex-shrink: 0;
  }

  .member-info {
    flex: 1;

    .member-name {
      font-size: $font-size-base;
      font-weight: 600;
      color: $color-text-primary;
      margin-bottom: 4px;
    }

    .member-desc {
      font-size: $font-size-xs;
      color: $color-text-secondary;
    }
  }
}

.slide-right-enter-active,
.slide-right-leave-active {
  transition: transform 0.3s ease;
}

.slide-right-enter-from,
.slide-right-leave-to {
  transform: translateX(100%);
}
</style>
