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
            <el-avatar :src="person.photoUrl" :size="120" fit="cover">
              {{ person.name?.charAt(0) }}
            </el-avatar>
          </div>
          <div class="memorial-info">
            <h3 class="memorial-name">{{ person.name }}</h3>
            <div class="memorial-years">
              {{ formatYear(person.birthDate) }} - {{ formatYear(person.deathDate) }}
            </div>
            <div class="memorial-title">{{ person.title }}</div>
          </div>
          <div class="memorial-bio" v-if="person.bio">
            {{ truncateText(person.bio, 60) }}
          </div>
          <div class="memorial-achievement" v-if="parseAchievements(person.achievement).length > 0">
            <span class="achievement-label">成就：</span>{{ truncateText(parseAchievements(person.achievement)[0], 40) }}
          </div>
          <el-button text @click="viewDetail(person)">
            查看详情
            <el-icon><ArrowRight /></el-icon>
          </el-button>
        </div>

        <el-empty v-if="!loading && memorialList.length === 0" description="暂无纪念人物">
          <el-button type="primary" @click="handleAddPerson">添加纪念人物</el-button>
        </el-empty>

        <div v-if="!loading && memorialList.length > 0" class="add-btn-wrapper">
          <el-button type="primary" @click="handleAddPerson">
            <el-icon><Plus /></el-icon>
            添加纪念人物
          </el-button>
        </div>
      </div>
    </main>

    <el-dialog v-model="addDialogVisible" title="添加纪念人物" width="550px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="选择成员" required>
          <el-autocomplete
            v-model="form.name"
            :fetch-suggestions="handleMemberSearch"
            placeholder="输入成员姓名搜索"
            :trigger-on-focus="false"
            clearable
            class="member-search-input"
            @select="handleMemberSelect"
          >
            <template #default="{ item }">
              <div class="member-suggestion">
                <span class="suggestion-name">{{ item.name }}</span>
                <span class="suggestion-info">
                  第{{ item.generation }}代 ·
                  {{ item.gender === 'MALE' ? '男' : '女' }}
                </span>
                <span class="suggestion-years">
                  {{ formatYear(item.birthDate) }} - {{ formatYear(item.deathDate) || '在世' }}
                </span>
              </div>
            </template>
          </el-autocomplete>
        </el-form-item>
        <el-form-item label="性别">
          <el-input :model-value="form.gender === 'MALE' ? '男' : form.gender === 'FEMALE' ? '女' : ''" disabled placeholder="自动填充" />
        </el-form-item>
        <el-form-item label="出生日期">
          <el-input :model-value="form.birthDate" disabled placeholder="自动填充" />
        </el-form-item>
        <el-form-item label="逝世日期">
          <el-input :model-value="form.deathDate" disabled placeholder="自动填充" />
        </el-form-item>
        <el-form-item label="头衔" required>
          <el-input v-model="form.title" placeholder="如：家族创始人、贤妻良母" />
        </el-form-item>
        <el-form-item label="简介">
          <el-input v-model="form.bio" type="textarea" :rows="3" placeholder="请输入人物简介" />
        </el-form-item>
        <el-form-item label="主要成就">
          <div class="achievement-input">
            <div
              v-for="(achievement, index) in form.achievements"
              :key="index"
              class="achievement-item"
            >
              <span class="achievement-index">{{ index + 1 }}.</span>
              <el-input
                v-model="form.achievements[index]"
                placeholder="请输入成就内容"
                class="achievement-text"
                @input="(val: string) => updateAchievement(index, val)"
              />
              <el-button
                :type="index === form.achievements.length - 1 ? 'primary' : 'danger'"
                :icon="index === form.achievements.length - 1 ? 'Plus' : 'Delete'"
                circle
                size="small"
                @click="index === form.achievements.length - 1 ? addAchievement() : removeAchievement(index)"
              />
            </div>
            <div v-if="form.achievements.length === 0" class="achievement-add-hint">
              <el-button type="primary" plain @click="addAchievement">
                <el-icon><Plus /></el-icon>
                添加成就
              </el-button>
            </div>
          </div>
        </el-form-item>
        <el-form-item label="照片">
          <div class="photo-upload">
            <el-upload
              action="#"
              :auto-upload="false"
              :show-file-list="false"
              :on-change="handleImageUpload"
              :on-remove="handleImageRemove"
              :on-preview="handlePreview"
              accept="image/*"
            >
              <div class="upload-trigger" v-if="!form.photoUrl">
                <el-icon><Plus /></el-icon>
                <span>上传照片</span>
              </div>
              <el-avatar v-else :src="form.photoUrl" :size="80" />
            </el-upload>
            <div v-if="form.photoUrl" class="upload-actions">
              <el-button size="small" type="primary" @click="previewImage = form.photoUrl; previewVisible = true">查看大图</el-button>
              <el-button size="small" type="danger" @click="handleImageRemove">移除</el-button>
            </div>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="previewVisible" title="照片预览" width="600px">
      <img :src="previewImage" style="width: 100%" />
    </el-dialog>

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
import { ArrowRight, Plus } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import type { Memorial, Member } from '@/types/api'
import { uploadMemorialImage, getMemorialList, searchMemorialMembers, addMemorial } from '@/api/memorial'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const memorialList = ref<Memorial[]>([])
const addDialogVisible = ref(false)
const submitLoading = ref(false)

const form = ref({
  memberId: 0,
  name: '',
  gender: '',
  birthDate: '',
  deathDate: '',
  title: '',
  bio: '',
  photoUrl: '',
  achievements: [] as string[]
})

const uploadLoading = ref(false)
const previewImage = ref('')
const previewVisible = ref(false)

function formatYear(dateStr?: string): string {
  if (!dateStr) return ''
  return dateStr.split('-')[0]
}

function parseAchievements(achievement: any): string[] {
  if (!achievement) return []
  if (Array.isArray(achievement)) return achievement
  try {
    return JSON.parse(achievement)
  } catch {
    return []
  }
}

async function handleMemberSearch(query: string, callback: (results: Member[]) => void) {
  if (!query || query.trim().length < 1) {
    callback([])
    return
  }
  try {
    const res = await searchMemorialMembers(query.trim())
    if (res.data) {
      callback(res.data)
    } else {
      callback([])
    }
  } catch (error) {
    callback([])
  }
}

function handleMemberSelect(member: Member) {
  form.value.memberId = member.id
  form.value.name = member.name || ''
  form.value.gender = member.gender || ''
  form.value.birthDate = member.birthDate || ''
  form.value.deathDate = member.deathDate || ''
}

function resetForm() {
  form.value = {
    memberId: 0,
    name: '',
    gender: '',
    birthDate: '',
    deathDate: '',
    title: '',
    bio: '',
    photoUrl: '',
    achievements: []
  }
}

function addAchievement() {
  form.value.achievements.push('')
}

function removeAchievement(index: number) {
  form.value.achievements.splice(index, 1)
}

function updateAchievement(index: number, value: string) {
  form.value.achievements[index] = value
}

async function fetchMemorialList() {
  loading.value = true
  try {
    const res = await getMemorialList()
    if (res.data?.records) {
      memorialList.value = res.data.records
    } else if (Array.isArray(res.data)) {
      memorialList.value = res.data
    } else {
      memorialList.value = []
    }
  } catch (error) {
    memorialList.value = []
  } finally {
    loading.value = false
  }
}

function viewDetail(person: Memorial) {
  router.push(`/memorial/${person.id}`)
}

function handleAddPerson() {
  resetForm()
  addDialogVisible.value = true
}

async function handleImageUpload(file: any) {
  const rawFile = file.raw || file
  const isImage = rawFile.type?.startsWith('image/') || file.name?.match(/\.(jpg|jpeg|png|gif|bmp|webp)$/i)
  const isLt10M = (rawFile.size || 0) / 1024 / 1024 < 10

  if (!isImage) {
    ElMessage.error('只能上传图片文件')
    return false
  }
  if (!isLt10M) {
    ElMessage.error('图片大小不能超过10MB')
    return false
  }

  uploadLoading.value = true
  try {
    const res = await uploadMemorialImage(rawFile)
    if (res.data) {
      form.value.photoUrl = res.data
      ElMessage.success('图片上传成功')
    }
  } catch (error) {
    ElMessage.error('图片上传失败')
  } finally {
    uploadLoading.value = false
  }
  return false
}

function handleImageRemove() {
  form.value.photoUrl = ''
}

function handlePreview(file: any) {
  previewImage.value = file.url || form.value.photoUrl
  previewVisible.value = true
}

async function handleSubmit() {
  if (!form.value.memberId) {
    ElMessage.warning('请选择族谱成员')
    return
  }
  if (!form.value.title) {
    ElMessage.warning('请输入头衔')
    return
  }

  const validAchievements = form.value.achievements.filter(a => a.trim() !== '')

  submitLoading.value = true
  try {
    await addMemorial({
      memberId: form.value.memberId,
      title: form.value.title,
      bio: form.value.bio,
      photoUrl: form.value.photoUrl,
      achievement: validAchievements.length > 0 ? JSON.stringify(validAchievements) : undefined
    })
    ElMessage.success('添加成功')
    addDialogVisible.value = false
    fetchMemorialList()
  } catch (error) {
    ElMessage.error('添加失败')
  } finally {
    submitLoading.value = false
  }
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

.add-btn-wrapper {
  grid-column: 1 / -1;
  text-align: center;
  margin-top: $spacing-lg;
}

.photo-upload {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: $spacing-sm;

  :deep(.el-upload) {
    border: 1px dashed $color-border;
    border-radius: $border-radius-md;
    cursor: pointer;
    transition: border-color $transition-fast ease;

    &:hover {
      border-color: $color-primary;
    }
  }

  .upload-trigger {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    width: 80px;
    height: 80px;
    color: $color-text-secondary;

    .el-icon {
      font-size: 24px;
      margin-bottom: 4px;
    }

    span {
      font-size: 12px;
    }
  }

  .upload-actions {
    display: flex;
    gap: $spacing-xs;
  }
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
  margin-bottom: $spacing-sm;
}

.memorial-achievement {
  color: $color-text-secondary;
  font-size: $font-size-xs;
  margin-bottom: $spacing-md;
  padding: $spacing-xs $spacing-sm;
  background: rgba($color-primary, 0.05);
  border-radius: $border-radius-sm;
  border-left: 2px solid $color-primary;

  .achievement-label {
    font-weight: bold;
    color: $color-primary;
  }
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

.member-search-input {
  width: 100%;
}

.member-suggestion {
  display: flex;
  flex-direction: column;
  padding: $spacing-xs 0;
  line-height: 1.4;

  .suggestion-name {
    font-weight: bold;
    color: $color-text-primary;
    font-size: $font-size-base;
  }

  .suggestion-info {
    color: $color-text-secondary;
    font-size: $font-size-xs;
  }

  .suggestion-years {
    color: $color-text-secondary;
    font-size: $font-size-xs;
    margin-top: 2px;
  }
}

:deep(.el-autocomplete-suggestion__item) {
  padding: $spacing-xs $spacing-sm;
}

.achievement-input {
  width: 100%;

  .achievement-item {
    display: flex;
    align-items: center;
    gap: $spacing-xs;
    margin-bottom: $spacing-sm;

    .achievement-index {
      width: 24px;
      color: $color-primary;
      font-weight: bold;
      flex-shrink: 0;
    }

    .achievement-text {
      flex: 1;
    }
  }

  .achievement-add-hint {
    margin-top: $spacing-xs;
  }
}
</style>
