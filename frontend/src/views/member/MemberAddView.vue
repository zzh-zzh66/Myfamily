<template>
  <div class="member-add-page">
    <div class="page-header">
      <el-button text @click="goBack">
        <el-icon><ArrowLeft /></el-icon>
        返回
      </el-button>
      <h2>添加成员</h2>
    </div>

    <div class="form-card">
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
        class="member-form"
      >
        <el-form-item label="父亲" prop="fatherId">
          <el-select
            v-model="form.fatherId"
            placeholder="请先选择父亲节点"
            clearable
            filterable
            @change="handleFatherChange"
          >
            <el-option
              v-for="m in maleMembers"
              :key="m.id"
              :label="m.name"
              :value="m.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="姓名" prop="name">
          <el-input v-model="form.name" placeholder="请输入成员姓名" />
        </el-form-item>

        <el-form-item label="性别" prop="gender">
          <el-radio-group v-model="form.gender">
            <el-radio value="male">男</el-radio>
            <el-radio value="female">女</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="辈分" prop="generation">
          <el-input-number v-model="form.generation" :min="1" :max="100" disabled />
        </el-form-item>

        <el-form-item label="出生日期" prop="birthDate">
          <el-date-picker
            v-model="form.birthDate"
            type="date"
            placeholder="选择出生日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>

        <el-form-item label="逝世日期" prop="deathDate">
          <el-date-picker
            v-model="form.deathDate"
            type="date"
            placeholder="选择逝世日期（如仍在世请跳过）"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>

        <el-form-item label="配偶姓名" prop="spouse">
          <el-select
            v-model="form.spouseId"
            placeholder="请先选择父亲节点后再选择配偶"
            clearable
            filterable
            :disabled="!form.fatherId"
          >
            <el-option
              v-for="m in potentialSpouses"
              :key="m.id"
              :label="m.name"
              :value="m.id"
            />
          </el-select>
          <div v-if="!form.fatherId" class="spouse-tip">
            请先选择父亲节点后再选择配偶
          </div>
        </el-form-item>

        <el-form-item label="个人简介" prop="biography">
          <el-input
            v-model="form.biography"
            type="textarea"
            :rows="4"
            placeholder="请输入个人简介"
          />
        </el-form-item>

        <el-form-item label="主要成就" prop="achievements">
          <el-input
            v-model="form.achievements"
            type="textarea"
            :rows="4"
            placeholder="请输入主要成就"
          />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleSubmit" :loading="loading">
            保存
          </el-button>
          <el-button @click="goBack">取消</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'
import { createMember, getMemberList, getSingleChildren } from '@/api/member'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const formRef = ref()
const loading = ref(false)
const maleMembers = ref<any[]>([])
const potentialSpouses = ref<any[]>([])

const isAdmin = computed(() => userStore.userInfo?.role?.toUpperCase() === 'ADMIN')

const form = reactive({
  name: '',
  gender: 'male' as 'male' | 'female',
  generation: 1,
  birthDate: '',
  deathDate: '',
  spouseId: undefined as number | undefined,
  fatherId: undefined as number | undefined,
  biography: '',
  achievements: ''
})

const rules = {
  name: [
    { required: true, message: '请输入姓名', trigger: 'blur' }
  ],
  gender: [
    { required: true, message: '请选择性别', trigger: 'change' }
  ],
  fatherId: [
    { required: true, message: '请选择父亲节点', trigger: 'change' }
  ]
}

async function fetchMaleMembers() {
  try {
    const res = await getMemberList({ size: 100 })
    const list = Array.isArray(res.data) ? res.data : (res.data?.records || [])
    maleMembers.value = list.filter((m: any) => m.gender === 'MALE')
  } catch (error) {
    console.error('获取成员列表失败', error)
  }
}

async function fetchPotentialSpouses(fatherId: number) {
  if (!fatherId) {
    potentialSpouses.value = []
    return
  }
  try {
    const res = await getSingleChildren(fatherId)
    potentialSpouses.value = res.data || []
  } catch (error) {
    console.error('获取潜在配偶列表失败', error)
    potentialSpouses.value = []
  }
}

function handleFatherChange(fatherId: number | undefined) {
  form.spouseId = undefined
  form.generation = 1
  if (fatherId) {
    fetchPotentialSpouses(fatherId)
    const father = maleMembers.value.find(m => m.id === fatherId)
    if (father) {
      form.generation = (father.generation || 0) + 1
    }
  } else {
    potentialSpouses.value = []
  }
}

async function handleSubmit() {
  if (!isAdmin.value) {
    ElMessage.error('只有管理员可以添加成员')
    return
  }

  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  if (!form.fatherId) {
    ElMessage.warning('请先选择父亲节点')
    return
  }

  loading.value = true
  try {
    const spouseMember = potentialSpouses.value.find(m => m.id === form.spouseId)
    const submitData = {
      name: form.name,
      gender: form.gender.toUpperCase(),
      generation: form.generation,
      birthDate: form.birthDate || null,
      deathDate: form.deathDate || null,
      spouseName: spouseMember?.name || null,
      spouseId: form.spouseId || null,
      fatherId: form.fatherId
    }
    await createMember(submitData)
    ElMessage.success('添加成功')
    router.push('/genealogy')
  } catch (error) {
    // 错误已在拦截器处理
  } finally {
    loading.value = false
  }
}

function goBack() {
  router.back()
}

onMounted(() => {
  if (!isAdmin.value) {
    ElMessage.error('只有管理员可以添加成员')
    router.push('/genealogy')
    return
  }
  fetchMaleMembers()
})
</script>

<style lang="scss" scoped>
@import '@/assets/styles/variables.scss';
@import '@/assets/styles/mixins.scss';

.member-add-page {
  @include paper-background;
  min-height: 100vh;
  padding: $spacing-md;
}

.page-header {
  @include flex-between;
  margin-bottom: $spacing-lg;

  h2 {
    font-family: $font-family-serif;
    font-size: $font-size-xl;
  }
}

.form-card {
  @include card;
  padding: $spacing-lg;
  max-width: 600px;
  margin: 0 auto;
}

.member-form {
  :deep(.el-form-item__label) {
    font-family: $font-family-sans;
  }

  :deep(.el-input),
  :deep(.el-textarea),
  :deep(.el-select) {
    width: 100%;
  }
}

.spouse-tip {
  font-size: 12px;
  color: #909399;
  line-height: 1.4;
  margin-top: 4px;
}
</style>
