<template>
  <div class="app-container">
    <!-- 提交表单 -->
    <el-card shadow="never" class="mb20">
      <template #header>
        <div class="card-header">
          <el-icon :size="18"><ChatLineSquare /></el-icon>
          <span>提交建议</span>
        </div>
      </template>
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="100px" style="max-width: 800px">
        <el-form-item label="建议标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入建议标题（2-100字）" maxlength="100" show-word-limit />
        </el-form-item>
        <el-form-item label="建议内容" prop="content">
          <el-input v-model="form.content" type="textarea" :rows="5" placeholder="请描述您的建议或想法..." maxlength="500" show-word-limit />
        </el-form-item>
        <el-form-item label="相关图片">
          <image-upload v-model="form.images" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="submitting" @click="handleSubmit">
            <el-icon><Promotion /></el-icon>提交建议
          </el-button>
          <el-button @click="resetForm">清空重填</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 我的建议列表 -->
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <el-icon :size="18"><List /></el-icon>
          <span>我的建议记录</span>
        </div>
      </template>
      <el-table v-loading="loading" :data="list" stripe>
        <el-table-column label="标题" prop="title" show-overflow-tooltip />
        <el-table-column label="状态" prop="status" width="100">
          <template #default="scope">
            <el-tag :type="statusTag(scope.row.status)">{{ scope.row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="提交时间" prop="createTime" width="170" />
        <el-table-column label="处理反馈" prop="remark" show-overflow-tooltip />
      </el-table>
      <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
    </el-card>
  </div>
</template>

<script setup name="SuggestionSubmit">
import { ref, reactive, getCurrentInstance } from 'vue'
import { ElMessage } from 'element-plus'
import { ChatLineSquare, List, Promotion } from '@element-plus/icons-vue'
import { addComplaint, listMyComplaint } from '@/api/com/complaint'

const { proxy } = getCurrentInstance()

const formRef = ref(null)
const form = reactive({ title: '', content: '', images: '' })
const submitting = ref(false)

const formRules = {
  title: [
    { required: true, message: '标题不能为空', trigger: 'blur' },
    { min: 2, max: 100, message: '标题长度在 2 到 100 个字', trigger: 'blur' }
  ],
  content: [
    { required: true, message: '内容不能为空', trigger: 'blur' },
    { min: 5, max: 500, message: '内容长度在 5 到 500 个字', trigger: 'blur' }
  ]
}

const loading = ref(false)
const list = ref([])
const total = ref(0)
const queryParams = reactive({ pageNum: 1, pageSize: 10 })

function statusTag(status) {
  return { '待受理': 'warning', '处理中': 'primary', '已完成': 'success' }[status] || 'info'
}

async function getList() {
  loading.value = true
  try {
    const res = await listMyComplaint({ ...queryParams, type: '建议' })
    list.value = res.rows
    total.value = res.total
  } finally { loading.value = false }
}

async function handleSubmit() {
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    submitting.value = true
    try {
      await addComplaint({ title: form.title, content: form.content, images: form.images, type: '建议' })
      ElMessage.success('建议提交成功！')
      form.title = ''; form.content = ''; form.images = ''
      formRef.value.resetFields()
      getList()
    } finally { submitting.value = false }
  })
}

function resetForm() {
  form.title = ''; form.content = ''; form.images = ''
  formRef.value?.resetFields()
}

getList()
</script>

<style scoped>
.mb20 { margin-bottom: 20px }
.card-header { display: flex; align-items: center; gap: 8px; font-weight: 600 }
</style>
