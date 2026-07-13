<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="80px">
      <el-form-item label="类型" prop="type">
        <el-select v-model="queryParams.type" placeholder="类型" clearable>
          <el-option label="投诉" value="投诉" />
          <el-option label="建议" value="建议" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="状态" clearable>
          <el-option label="待受理" value="待受理" />
          <el-option label="处理中" value="处理中" />
          <el-option label="已完成" value="已完成" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
    </el-row>

    <el-table v-loading="loading" :data="complaintList" stripe>
      <el-table-column label="类型" prop="type" width="80">
        <template #default="scope">
          <el-tag :type="scope.row.type === '投诉' ? 'danger' : 'info'">{{ scope.row.type }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="标题" prop="title" show-overflow-tooltip />
      <el-table-column label="提交人" prop="createBy" width="100" />
      <el-table-column label="状态" prop="status" width="100">
        <template #default="scope">
          <el-tag :type="statusTag(scope.row.status)">{{ scope.row.status }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="提交时间" prop="createTime" width="170" />
      <el-table-column label="操作" align="center" width="280">
        <template #default="scope">
          <el-button link type="primary" icon="View" @click="handleDetail(scope.row)">详情</el-button>
          <el-button v-if="scope.row.status === '待受理'" link type="primary" icon="Check" @click="handleAccept(scope.row)" v-hasPermi="['com:complaint:accept']">受理</el-button>
          <el-button v-if="scope.row.status === '处理中'" link type="success" icon="CircleCheck" @click="handleFinish(scope.row)" v-hasPermi="['com:complaint:finish']">完成</el-button>
          <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['com:complaint:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <!-- 详情/反馈弹窗 -->
    <el-dialog v-model="detailVisible" title="投诉详情" width="700px" destroy-on-close>
      <template v-if="currentRow">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="类型">
            <el-tag :type="currentRow.type === '投诉' ? 'danger' : ''">{{ currentRow.type }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="statusTag(currentRow.status)">{{ currentRow.status }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="标题" :span="2">{{ currentRow.title }}</el-descriptions-item>
          <el-descriptions-item label="内容" :span="2">{{ currentRow.content }}</el-descriptions-item>
          <el-descriptions-item label="提交人">{{ currentRow.createBy }}</el-descriptions-item>
          <el-descriptions-item label="提交时间">{{ currentRow.createTime }}</el-descriptions-item>
        </el-descriptions>
        <el-divider>处理反馈</el-divider>
        <el-table :data="feedbackList" stripe>
          <el-table-column label="处理人" prop="createBy" width="100" />
          <el-table-column label="处理说明" prop="description" show-overflow-tooltip />
          <el-table-column label="时间" prop="createTime" width="170" />
        </el-table>
        <el-form :model="feedbackForm" label-width="80px" class="mt20">
          <el-form-item label="处理说明">
            <el-input v-model="feedbackForm.description" type="textarea" :rows="2" placeholder="输入处理说明..." />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="submitFeedback" v-hasPermi="['com:complaint:feedback:add']">提交反馈</el-button>
          </el-form-item>
        </el-form>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="ComplaintManage">
import { ref, reactive, getCurrentInstance } from 'vue'
import { ElMessage } from 'element-plus'
import { listComplaint, delComplaint, acceptComplaint, finishComplaint, listComplaintFeedback, addComplaintFeedback } from '@/api/com/complaint'

const { proxy } = getCurrentInstance()

const queryRef = ref(null)
const loading = ref(false)
const showSearch = ref(true)
const total = ref(0)
const complaintList = ref([])
const queryParams = reactive({ pageNum: 1, pageSize: 10, type: null, status: null })

function statusTag(status) {
  return { '待受理': 'warning', '处理中': 'primary', '已完成': 'success' }[status] || 'info'
}

async function getList() {
  loading.value = true
  try {
    const res = await listComplaint(queryParams)
    complaintList.value = res.rows
    total.value = res.total
  } finally { loading.value = false }
}

function handleQuery() { queryParams.pageNum = 1; getList() }
function resetQuery() { queryRef.value?.resetFields(); handleQuery() }

async function handleDelete(row) {
  proxy.$modal.confirm('确认删除该记录？').then(async () => {
    await delComplaint(row.complaintId)
    ElMessage.success('删除成功')
    getList()
  })
}

async function handleAccept(row) {
  proxy.$modal.confirm('确认受理该投诉？').then(async () => {
    await acceptComplaint(row)
    ElMessage.success('已受理')
    getList()
  })
}

async function handleFinish(row) {
  proxy.$modal.confirm('确认标记为已完成？').then(async () => {
    await finishComplaint(row)
    ElMessage.success('已完成')
    getList()
  })
}

// 详情/反馈
const detailVisible = ref(false)
const currentRow = ref(null)
const feedbackList = ref([])
const feedbackForm = reactive({ complaintId: null, description: '' })

async function handleDetail(row) {
  currentRow.value = row
  feedbackForm.complaintId = row.complaintId
  feedbackForm.description = ''
  detailVisible.value = true
  try {
    const res = await listComplaintFeedback({ complaintId: row.complaintId })
    feedbackList.value = res.rows
  } catch { feedbackList.value = [] }
}

async function submitFeedback() {
  if (!feedbackForm.description) { ElMessage.warning('请输入处理说明'); return }
  await addComplaintFeedback(feedbackForm)
  ElMessage.success('反馈已提交')
  feedbackForm.description = ''
  handleDetail(currentRow.value)
}

getList()
</script>

<style scoped>
.mt20 { margin-top: 20px }
</style>
