<template>
  <div class="app-container">
    <!-- 提交表单 -->
    <el-card shadow="never" class="mb20">
      <template #header>
        <div class="card-header">
          <el-icon :size="18"><WarningFilled /></el-icon>
          <span>提交投诉</span>
        </div>
      </template>
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="100px" style="max-width: 800px">
        <el-form-item label="投诉分类" prop="category">
          <el-select v-model="form.category" placeholder="请选择分类" clearable>
            <el-option label="安全问题" value="安全" />
            <el-option label="环境卫生" value="卫生" />
            <el-option label="噪音扰民" value="噪音" />
            <el-option label="设施维修" value="设施" />
            <el-option label="物业服务" value="物业" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>
        <el-form-item label="紧急程度" prop="urgency">
          <el-radio-group v-model="form.urgency">
            <el-radio value="普通">普通</el-radio>
            <el-radio value="紧急">紧急</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="投诉标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入投诉标题（2-100字）" maxlength="100" show-word-limit />
        </el-form-item>
        <el-form-item label="投诉内容" prop="content">
          <el-input v-model="form.content" type="textarea" :rows="5" placeholder="请详细描述您要投诉的问题..." maxlength="500" show-word-limit />
        </el-form-item>
        <el-form-item label="相关图片">
          <image-upload v-model="form.images" />
        </el-form-item>
        <el-form-item>
          <el-button type="danger" :loading="submitting" @click="handleSubmit">
            <el-icon><Promotion /></el-icon>提交投诉
          </el-button>
          <el-button @click="resetForm">清空重填</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 我的投诉列表 -->
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <el-icon :size="18"><List /></el-icon>
          <span>我的投诉记录</span>
        </div>
      </template>
      <el-table v-loading="loading" :data="list" stripe>
        <el-table-column label="标题" prop="title" show-overflow-tooltip />
        <el-table-column label="紧急" prop="urgency" width="80">
          <template #default="scope">
            <el-tag v-if="scope.row.urgency === '紧急'" type="danger" size="small">紧急</el-tag>
            <span v-else style="color:#909399">普通</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" prop="status" width="100">
          <template #default="scope">
            <el-tag :type="statusTag(scope.row.status)">{{ scope.row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="提交时间" prop="createTime" width="170" />
        <el-table-column label="满意度" width="150">
          <template #default="scope">
            <el-rate
              v-if="scope.row.status === '已完成'"
              v-model="scope.row.rating"
              :max="5"
              :disabled="!!scope.row.rating"
              @change="(val) => handleRate(scope.row, val)"
            />
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100">
          <template #default="scope">
            <el-button link type="primary" icon="View" @click="showProgress(scope.row)">进度</el-button>
          </template>
        </el-table-column>
      </el-table>
      <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
    </el-card>

    <!-- 进度查询弹窗 -->
    <el-dialog v-model="progressVisible" title="工单进度" width="500px" destroy-on-close>
      <el-steps v-if="progressRow" direction="vertical" :active="progressActive" process-status="finish" finish-status="success">
        <el-step title="已提交" :description="progressRow.createTime || '-'" />
        <el-step title="已受理" :description="progressRow.acceptTime || '待受理'" :status="progressRow.acceptTime ? 'finish' : 'process'" />
        <el-step title="处理完成" :description="progressRow.finishTime || '处理中'" :status="progressRow.finishTime ? 'finish' : 'wait'" />
        <el-step title="已评价" :description="progressRow.rating ? progressRow.rating + '分' : '待评价'" :status="progressRow.rating ? 'finish' : 'wait'" />
      </el-steps>
      <template #footer>
        <el-button @click="progressVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="ComplaintSubmit">
import { ref, reactive, computed, getCurrentInstance } from 'vue'
import { ElMessage } from 'element-plus'
import { WarningFilled, List, Promotion } from '@element-plus/icons-vue'
import { addComplaint, listMyComplaint, rateComplaint } from '@/api/com/complaint'

const { proxy } = getCurrentInstance()

const formRef = ref(null)
const form = reactive({ category: '', urgency: '普通', title: '', content: '', images: '' })
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
    const res = await listMyComplaint({ ...queryParams, type: '投诉' })
    list.value = res.rows
    total.value = res.total
  } finally { loading.value = false }
}

async function handleSubmit() {
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    submitting.value = true
    try {
      await addComplaint({ category: form.category, urgency: form.urgency, title: form.title, content: form.content, images: form.images, type: '投诉' })
      ElMessage.success('投诉提交成功！')
      form.category = ''; form.urgency = '普通'; form.title = ''; form.content = ''; form.images = ''
      formRef.value.resetFields()
      getList()
    } finally { submitting.value = false }
  })
}

async function handleRate(row, val) {
  await rateComplaint({ complaintId: row.complaintId, rating: val })
  ElMessage.success('评价成功')
  getList()
}

const progressVisible = ref(false)
const progressRow = ref(null)
const progressActive = computed(() => {
  if (!progressRow.value) return 0
  if (progressRow.value.rating) return 4
  if (progressRow.value.finishTime) return 3
  if (progressRow.value.acceptTime) return 2
  return 1
})

function showProgress(row) {
  progressRow.value = row
  progressVisible.value = true
}

function resetForm() {
  form.category = ''; form.urgency = '普通'; form.title = ''; form.content = ''; form.images = ''
  formRef.value?.resetFields()
}

getList()
</script>

<style scoped>
.mb20 { margin-bottom: 20px }
.card-header { display: flex; align-items: center; gap: 8px; font-weight: 600 }
</style>
