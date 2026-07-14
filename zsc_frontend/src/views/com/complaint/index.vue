<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="80px">
      <el-form-item label="类型" prop="type">
        <el-select v-model="queryParams.type" placeholder="类型" clearable>
          <el-option label="投诉" value="投诉" />
          <el-option label="建议" value="建议" />
        </el-select>
      </el-form-item>
      <el-form-item label="紧急" prop="urgency">
        <el-select v-model="queryParams.urgency" placeholder="紧急程度" clearable style="width:100px">
          <el-option label="紧急" value="紧急" />
          <el-option label="普通" value="普通" />
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

    <el-table v-loading="loading" :data="complaintList">
      <el-table-column label="类型" prop="type" width="80">
        <template #default="scope">
          <el-tag :type="scope.row.type === '投诉' ? 'danger' : 'info'">{{ scope.row.type }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="紧急" prop="urgency" width="80">
        <template #default="scope">
          <el-tag v-if="scope.row.urgency === '紧急'" type="danger" size="small">紧急</el-tag>
          <span v-else style="color:#909399">普通</span>
        </template>
      </el-table-column>
      <el-table-column label="分类" prop="category" width="90" />
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
          <el-button v-if="scope.row.status === '处理中'" link type="warning" icon="Edit" @click="openProgressDialog(scope.row)" v-hasPermi="['com:complaint:feedback:add']">更新</el-button>
          <el-button v-if="scope.row.status === '处理中'" link type="success" icon="CircleCheck" @click="openFinishDialog(scope.row)" v-hasPermi="['com:complaint:finish']">完成</el-button>
          <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['com:complaint:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <!-- 处理完成弹窗 -->
    <el-dialog v-model="finishVisible" title="处理完成" width="550px" destroy-on-close>
      <el-form ref="finishFormRef" :model="finishForm" :rules="finishRules" label-width="80px">
        <el-form-item label="处理说明" prop="description">
          <el-input v-model="finishForm.description" type="textarea" :rows="5" placeholder="请填写处理说明（处理过程、结果等）..." maxlength="500" show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="finishVisible = false">取消</el-button>
        <el-button type="success" :loading="finishing" @click="handleFinish">确认完成</el-button>
      </template>
    </el-dialog>

    <!-- 进度更新弹窗 -->
    <el-dialog v-model="progressUpdateVisible" title="进度更新" width="500px" destroy-on-close>
      <el-form ref="progressFormRef" :model="progressForm" :rules="progressRules" label-width="80px">
        <el-form-item label="进度说明" prop="description">
          <el-input v-model="progressForm.description" type="textarea" :rows="4" placeholder="填写当前处理进展..." maxlength="500" show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="progressUpdateVisible = false">取消</el-button>
        <el-button type="primary" :loading="updating" @click="submitProgress">提交更新</el-button>
      </template>
    </el-dialog>

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
          <el-descriptions-item label="受理时间">{{ currentRow.acceptTime || '-' }}</el-descriptions-item>
          <el-descriptions-item label="完成时间">{{ currentRow.finishTime || '-' }}</el-descriptions-item>
        </el-descriptions>
        <el-divider>处理记录</el-divider>
        <el-timeline v-if="feedbackList.length">
          <el-timeline-item
            v-for="fb in feedbackList"
            :key="fb.feedbackId"
            :timestamp="fb.createTime"
            placement="top"
          >
            <el-card shadow="hover" size="small">
              <p style="margin:0; white-space: pre-wrap">{{ fb.description }}</p>
              <span style="font-size:12px; color:#909399">— {{ fb.createBy }}</span>
            </el-card>
          </el-timeline-item>
        </el-timeline>
        <el-empty v-else description="暂无处理记录" :image-size="60" />
        <el-divider />
        <el-form :model="feedbackForm" label-width="80px">
          <el-form-item label="追加说明">
            <el-input v-model="feedbackForm.description" type="textarea" :rows="2" placeholder="添加补充说明（可选）..." />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="submitFeedback" v-hasPermi="['com:complaint:feedback:add']">提交</el-button>
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
const queryParams = reactive({ pageNum: 1, pageSize: 10, type: null, urgency: null, status: null })

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
  proxy.$modal.confirm('确认受理该投诉/建议？').then(async () => {
    await acceptComplaint(row)
    ElMessage.success('已受理，状态变为"处理中"')
    getList()
  })
}

// ======================== 处理完成弹窗 ========================
const finishVisible = ref(false)
const finishing = ref(false)
const finishFormRef = ref(null)
const finishRow = ref(null)
const finishForm = reactive({ description: '' })
const finishRules = {
  description: [
    { required: true, message: '请填写处理说明', trigger: 'blur' },
    { min: 5, max: 500, message: '长度在 5 到 500 个字', trigger: 'blur' }
  ]
}

function openFinishDialog(row) {
  finishRow.value = row
  finishForm.description = ''
  finishVisible.value = true
}

async function handleFinish() {
  await finishFormRef.value.validate(async (valid) => {
    if (!valid) return
    finishing.value = true
    try {
      // 1. 标记完成
      await finishComplaint(finishRow.value)
      // 2. 保存处理说明
      await addComplaintFeedback({
        complaintId: finishRow.value.complaintId,
        description: finishForm.description
      })
      ElMessage.success('处理完成！')
      finishVisible.value = false
      getList()
    } finally { finishing.value = false }
  })
}

// ======================== 进度更新弹窗 ========================
const progressUpdateVisible = ref(false)
const updating = ref(false)
const progressFormRef = ref(null)
const progressRow = ref(null)
const progressForm = reactive({ description: '' })
const progressRules = {
  description: [
    { required: true, message: '请填写进度说明', trigger: 'blur' },
    { min: 2, max: 500, message: '长度 2-500 字', trigger: 'blur' }
  ]
}

function openProgressDialog(row) {
  progressRow.value = row
  progressForm.description = ''
  progressUpdateVisible.value = true
}

async function submitProgress() {
  await progressFormRef.value.validate(async (valid) => {
    if (!valid) return
    updating.value = true
    try {
      await addComplaintFeedback({
        complaintId: progressRow.value.complaintId,
        description: progressForm.description
      })
      ElMessage.success('进度已更新')
      progressUpdateVisible.value = false
    } finally { updating.value = false }
  })
}

// ======================== 详情弹窗 ========================
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
  if (!feedbackForm.description) { ElMessage.warning('请输入说明内容'); return }
  await addComplaintFeedback(feedbackForm)
  ElMessage.success('已追加')
  feedbackForm.description = ''
  handleDetail(currentRow.value)
}

getList()
</script>

<style scoped>
.mt20 { margin-top: 20px }
</style>
