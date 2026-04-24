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
        <el-form-item label="姓名" prop="name">
          <el-input v-model="form.name" placeholder="请输入成员姓名" />
        </el-form-item>

        <el-form-item label="性别" prop="gender">
          <el-radio-group v-model="form.gender">
            <el-radio label="male">男</el-radio>
            <el-radio label="female">女</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="辈分" prop="generation">
          <el-input-number v-model="form.generation" :min="1" :max="100" />
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

        <el-form-item label="籍贯" prop="birthplace">
          <el-input v-model="form.birthplace" placeholder="请输入籍贯" />
        </el-form-item>

        <el-form-item label="配偶姓名" prop="spouse">
          <el-input v-model="form.spouse" placeholder="请输入配偶姓名" />
        </el-form-item>

        <el-form-item label="父亲">
          <el-select v-model="form.fatherId" placeholder="请选择父亲" clearable filterable>
            <el-option
              v-for="m in maleMembers"
              :key="m.id"
              :label="m.name"
              :value="m.id"
            />
          </el-select>
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
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'
import { createMember, getMemberList } from '@/api/member'

const router = useRouter()

const formRef = ref()
const loading = ref(false)
const maleMembers = ref<any[]>([])

const form = reactive({
  name: '',
  gender: 'male' as 'male' | 'female',
  generation: 1,
  birthDate: '',
  deathDate: '',
  birthplace: '',
  spouse: '',
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
  generation: [
    { required: true, message: '请输入辈分', trigger: 'blur' }
  ]
}

async function fetchMaleMembers() {
  try {
    const res = await getMemberList({ gender: 'male', size: 100 })
    maleMembers.value = res.data.records
  } catch (error) {
    console.error('获取成员列表失败', error)
  }
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    await createMember(form)
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
</style>
