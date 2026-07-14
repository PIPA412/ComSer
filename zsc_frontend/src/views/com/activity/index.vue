<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="80px">
      <el-form-item label="标题" prop="title">
        <el-input v-model="queryParams.title" placeholder="活动标题" clearable />
      </el-form-item>
      <el-form-item label="类型" prop="activityType">
        <el-select v-model="queryParams.activityType" placeholder="类型" clearable>
          <el-option label="文体活动" value="文体活动" />
          <el-option label="志愿服务" value="志愿服务" />
          <el-option label="知识讲座" value="知识讲座" />
          <el-option label="节日庆典" value="节日庆典" />
          <el-option label="亲子活动" value="亲子活动" />
          <el-option label="健康义诊" value="健康义诊" />
          <el-option label="其他" value="其他" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="状态" clearable>
          <el-option label="草稿" value="草稿" />
          <el-option label="待审核" value="待审核" />
          <el-option label="报名中" value="报名中" />
          <el-option label="进行中" value="进行中" />
          <el-option label="已结束" value="已结束" />
          <el-option label="已取消" value="已取消" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" @click="openAddDialog" v-hasPermi="['com:activity:add']">新增活动</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
    </el-row>

    <el-table v-loading="loading" :data="list" stripe>
      <el-table-column label="标题" prop="title" show-overflow-tooltip min-width="160" />
      <el-table-column label="类型" prop="activityType" width="100" />
      <el-table-column label="时间" prop="activityTime" width="160" />
      <el-table-column label="地点" prop="location" width="140" />
      <el-table-column label="费用" width="100">
        <template #default="scope">{{ scope.row.fee > 0 ? '¥' + scope.row.fee : '免费' }}</template>
      </el-table-column>
      <el-table-column label="报名" width="80">
        <template #default="scope">{{ scope.row.actualParticipants || 0 }}/{{ scope.row.maxParticipants || 0 }}</template>
      </el-table-column>
      <el-table-column label="状态" prop="status" width="100">
        <template #default="scope">
          <el-tag :type="statusTag(scope.row.status)">{{ scope.row.status }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="320">
        <template #default="scope">
          <el-button link type="primary" icon="View" @click="openDetail(scope.row)">详情</el-button>
          <el-button link type="warning" icon="User" @click="openSignup(scope.row)">报名</el-button>
          <el-button v-if="scope.row.status === '草稿' || scope.row.status === '待审核'" link type="success" icon="Promotion" @click="handlePublish(scope.row)" v-hasPermi="['com:activity:edit']">发布</el-button>
          <el-button v-if="scope.row.status !== '已结束' && scope.row.status !== '已取消'" link type="primary" icon="Edit" @click="openEditDialog(scope.row)" v-hasPermi="['com:activity:edit']">修改</el-button>
          <el-button v-if="scope.row.status === '已结束'" link type="success" icon="Edit" @click="openReview(scope.row)" v-hasPermi="['com:activity:edit']">回顾</el-button>
          <el-button v-if="scope.row.status === '报名中' || scope.row.status === '进行中'" link type="warning" icon="CircleClose" @click="handleFinish(scope.row)" v-hasPermi="['com:activity:edit']">结束</el-button>
          <el-button link :type="scope.row.isTop ? 'warning' : 'info'" :icon="scope.row.isTop ? 'Top' : 'Top'" @click="handleToggleTop(scope.row)" v-hasPermi="['com:activity:edit']">{{ scope.row.isTop ? '取消置顶' : '置顶' }}</el-button>
          <el-button v-if="scope.row.status !== '已结束' && scope.row.status !== '已取消'" link type="danger" icon="Delete" @click="handleCancel(scope.row)" v-hasPermi="['com:activity:remove']">取消</el-button>
        </template>
      </el-table-column>
    </el-table>
    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <!-- 新增/修改弹窗 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '修改活动' : '新增活动'" width="800px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="24">
            <el-form-item label="活动标题" prop="title">
              <el-input v-model="form.title" placeholder="请输入活动标题" maxlength="100" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="活动类型" prop="activityType">
              <el-select v-model="form.activityType" placeholder="选择类型" style="width:100%">
                <el-option label="文体活动" value="文体活动" />
                <el-option label="志愿服务" value="志愿服务" />
                <el-option label="知识讲座" value="知识讲座" />
                <el-option label="节日庆典" value="节日庆典" />
                <el-option label="亲子活动" value="亲子活动" />
                <el-option label="健康义诊" value="健康义诊" />
                <el-option label="其他" value="其他" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态" prop="status">
              <el-select v-model="form.status" placeholder="状态" style="width:100%">
                <el-option label="草稿（暂不发布）" value="草稿" />
                <el-option label="提交审核" value="待审核" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="活动时间" prop="activityTime">
              <el-date-picker v-model="form.activityTime" type="datetime" placeholder="选择时间" style="width:100%" value-format="YYYY-MM-DD HH:mm:ss" :disabled-date="disabledDate" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="报名开始" prop="signupStartTime">
              <el-date-picker v-model="form.signupStartTime" type="datetime" placeholder="开始时间" style="width:100%" value-format="YYYY-MM-DD HH:mm:ss" :disabled-date="disabledDate" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="报名截止" prop="signupDeadline">
              <el-date-picker v-model="form.signupDeadline" type="datetime" placeholder="截止时间" style="width:100%" value-format="YYYY-MM-DD HH:mm:ss" :disabled-date="disabledDate" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="活动地点" prop="location">
              <el-input v-model="form.location" placeholder="地点" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="人数上限" prop="maxParticipants">
              <el-input-number v-model="form.maxParticipants" :min="0" :max="9999" placeholder="0不限" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="费用(元)" prop="fee">
              <el-input-number v-model="form.fee" :min="0" :precision="2" placeholder="0" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="参与对象" prop="targetAudience">
              <el-select v-model="form.targetAudience" placeholder="参与对象" style="width:100%">
                <el-option label="全体居民" value="全体居民" />
                <el-option label="老年人" value="老年人" />
                <el-option label="青少年" value="青少年" />
                <el-option label="儿童" value="儿童" />
                <el-option label="女性" value="女性" />
                <el-option label="党员" value="党员" />
                <el-option label="志愿者" value="志愿者" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="发布范围" prop="publishScope">
              <el-select v-model="form.publishScope" placeholder="发布范围" style="width:100%">
                <el-option label="全社区" value="全社区" />
                <el-option label="本楼栋" value="本楼栋" />
                <el-option label="指定区域" value="指定区域" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="活动详情" prop="content">
              <el-input v-model="form.content" type="textarea" :rows="5" placeholder="活动详情描述..." maxlength="2000" show-word-limit />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="备注">
              <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="备注信息（内部可见）" maxlength="500" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">保存</el-button>
      </template>
    </el-dialog>

    <!-- 活动回顾弹窗 -->
    <el-dialog v-model="reviewVisible" title="活动回顾" width="700px" destroy-on-close>
      <el-form :model="reviewForm" label-width="80px">
        <el-form-item label="活动照片">
          <image-upload v-model="reviewForm.photos" />
        </el-form-item>
        <el-form-item label="活动总结">
          <el-input v-model="reviewForm.review" type="textarea" :rows="8" placeholder="写活动总结..." maxlength="2000" show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="reviewVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveReview">保存回顾</el-button>
      </template>
    </el-dialog>

    <!-- 报名审核弹窗 -->
    <el-dialog v-model="signupVisible" :title="'报名管理 - ' + (signupActivity?.title || '')" width="900px" destroy-on-close>
      <el-row :gutter="10" class="mb8">
        <el-col :span="1.5">
          <el-button type="success" plain :disabled="selectedSignups.length === 0" @click="handleBatchApprove">批量通过</el-button>
        </el-col>
        <el-col :span="1.5">
          <el-button type="danger" plain :disabled="selectedSignups.length === 0" @click="openRejectDialog">批量拒绝</el-button>
        </el-col>
        <el-col :span="1.5">
          <el-button type="primary" plain icon="Download" @click="handleExport">导出Excel</el-button>
        </el-col>
      </el-row>
      <el-table v-loading="signupLoading" :data="signupList" @selection-change="s => selectedSignups = s" stripe>
        <el-table-column type="selection" width="50" />
        <el-table-column label="报名人" prop="createBy" width="100" />
        <el-table-column label="审核" prop="status" width="80">
          <template #default="scope">
            <el-tag :type="signupStatusTag(scope.row.status)">{{ scope.row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="考勤" prop="attendStatus" width="80">
          <template #default="scope">
            <el-tag v-if="scope.row.attendStatus === '已签到'" type="success" size="small">已签到</el-tag>
            <el-tag v-else-if="scope.row.attendStatus === '已缺席'" type="danger" size="small">已缺席</el-tag>
            <span v-else style="color:#c0c4cc">未签到</span>
          </template>
        </el-table-column>
        <el-table-column label="报名时间" prop="createTime" width="170" />
        <el-table-column label="操作" width="80">
          <template #default="scope">
            <el-button v-if="scope.row.attendStatus !== '已签到' && scope.row.attendStatus !== '已缺席'" link type="danger" size="small" @click="handleMarkAbsent(scope.row)">缺席</el-button>
          </template>
        </el-table-column>
      </el-table>
      <pagination v-show="signupTotal > 0" :total="signupTotal" v-model:page="signupParams.pageNum" v-model:limit="signupParams.pageSize" @pagination="loadSignups" />
    </el-dialog>

    <!-- 批量拒绝弹窗 -->
    <el-dialog v-model="rejectVisible" title="批量拒绝" width="500px" append-to-body>
      <el-form :model="rejectForm" label-width="80px">
        <el-form-item label="拒绝原因" required>
          <el-input v-model="rejectForm.reason" type="textarea" :rows="3" placeholder="填写拒绝原因，将通知报名人..." />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="rejectVisible = false">取消</el-button>
        <el-button type="danger" @click="handleBatchReject">确认拒绝</el-button>
      </template>
    </el-dialog>

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailVisible" title="活动详情" width="700px" destroy-on-close>
      <template v-if="currentRow">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="标题" :span="2">{{ currentRow.title }}</el-descriptions-item>
          <el-descriptions-item label="类型">{{ currentRow.activityType }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="statusTag(currentRow.status)">{{ currentRow.status }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="时间">{{ currentRow.activityTime }}</el-descriptions-item>
          <el-descriptions-item label="报名截止">{{ currentRow.signupDeadline || '-' }}</el-descriptions-item>
          <el-descriptions-item label="地点">{{ currentRow.location }}</el-descriptions-item>
          <el-descriptions-item label="人数">{{ currentRow.actualParticipants || 0 }}/{{ currentRow.maxParticipants || '不限' }}</el-descriptions-item>
          <el-descriptions-item label="费用">{{ currentRow.fee > 0 ? '¥' + currentRow.fee : '免费' }}</el-descriptions-item>
          <el-descriptions-item label="参与对象">{{ currentRow.targetAudience || '全体居民' }}</el-descriptions-item>
          <el-descriptions-item label="签到码">
            <el-tag v-if="currentRow.checkinCode" type="success">{{ currentRow.checkinCode }}</el-tag>
            <span v-else>发布后自动生成</span>
          </el-descriptions-item>
          <el-descriptions-item label="发布范围">{{ currentRow.publishScope || '全社区' }}</el-descriptions-item>
          <el-descriptions-item label="发布人">{{ currentRow.createBy }}</el-descriptions-item>
          <el-descriptions-item label="发布时间">{{ currentRow.createTime }}</el-descriptions-item>
          <el-descriptions-item label="详情" :span="2">{{ currentRow.content }}</el-descriptions-item>
        </el-descriptions>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="ActivityManagement">
import { ref, reactive, getCurrentInstance } from 'vue'
import { ElMessage } from 'element-plus'
import { listActivity, addActivity, updateActivity, delActivity, listSignup, batchApproveSignup, batchRejectSignup, exportSignupUrl, markAbsent, saveReview } from '@/api/com/activity'
import request from '@/utils/request'
import { getToken } from '@/utils/auth'

const { proxy } = getCurrentInstance()

const queryRef = ref(null)
const loading = ref(false)
const showSearch = ref(true)
const total = ref(0)
const list = ref([])
const queryParams = reactive({ pageNum: 1, pageSize: 10, title: null, activityType: null, status: null })

function statusTag(s) {
  return { '草稿': '', '待审核': 'warning', '报名中': 'success', '进行中': 'primary', '已结束': 'info', '已取消': 'danger' }[s] || ''
}
function disabledDate(time) { return time.getTime() < Date.now() - 86400000 }

async function getList() {
  loading.value = true
  try {
    const res = await listActivity(queryParams)
    list.value = res.rows; total.value = res.total
  } finally { loading.value = false }
}
function handleQuery() { queryParams.pageNum = 1; getList() }
function resetQuery() { queryRef.value?.resetFields(); handleQuery() }

// ==================== 新增/修改 ====================
const dialogVisible = ref(false)
const isEdit = ref(false)
const submitting = ref(false)
const formRef = ref(null)
const form = reactive({
  activityId: null, title: '', activityType: '', activityTime: '', signupStartTime: '', signupDeadline: '',
  location: '', maxParticipants: 0, fee: 0, targetAudience: '全体居民', publishScope: '全社区',
  content: '', remark: '', status: '草稿'
})
const rules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  activityType: [{ required: true, message: '请选择类型', trigger: 'change' }],
  activityTime: [{ required: true, message: '请选择时间', trigger: 'change' }],
  location: [{ required: true, message: '请输入地点', trigger: 'blur' }]
}

function openAddDialog() {
  isEdit.value = false
  Object.assign(form, { activityId: null, title: '', activityType: '', activityTime: '', signupStartTime: '', signupDeadline: '', location: '', maxParticipants: 0, fee: 0, targetAudience: '全体居民', publishScope: '全社区', content: '', remark: '', status: '草稿' })
  dialogVisible.value = true
}

function openEditDialog(row) {
  isEdit.value = true
  Object.assign(form, { ...row, fee: row.fee || 0, maxParticipants: row.maxParticipants || 0, targetAudience: row.targetAudience || '全体居民', publishScope: row.publishScope || '全社区' })
  dialogVisible.value = true
}

async function handleSubmit() {
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    submitting.value = true
    try {
      if (isEdit.value) {
        await updateActivity(form)
        ElMessage.success('修改成功')
      } else {
        await addActivity(form)
        ElMessage.success('创建成功')
      }
      dialogVisible.value = false
      getList()
    } finally { submitting.value = false }
  })
}

// ==================== 操作 ====================
async function handlePublish(row) {
  proxy.$modal.confirm('确认发布该活动？发布后居民可见并开始报名。').then(async () => {
    await request({ url: '/com/activity/publish', method: 'put', data: { activityId: row.activityId } })
    ElMessage.success('已发布')
    getList()
  })
}

async function handleFinish(row) {
  proxy.$modal.confirm('确认结束该活动？').then(async () => {
    await request({ url: '/com/activity/finish', method: 'put', data: { activityId: row.activityId } })
    ElMessage.success('已结束')
    getList()
  })
}

async function handleToggleTop(row) {
  await request({ url: '/com/activity/top', method: 'put', data: { activityId: row.activityId } })
  ElMessage.success(row.isTop ? '已取消置顶' : '已置顶')
  getList()
}

async function handleCancel(row) {
  proxy.$modal.confirm('确认取消该活动？').then(async () => {
    await delActivity(row.activityId)
    ElMessage.success('已取消')
    getList()
  })
}

// ==================== 详情 ====================
const detailVisible = ref(false)
const currentRow = ref(null)
const reviewVisible = ref(false), reviewRow = ref(null)
const reviewForm = reactive({ photos: '', review: '' })
function openReview(row) { reviewRow.value = row; reviewForm.photos = row.photos || ''; reviewForm.review = row.review || ''; reviewVisible.value = true }
async function handleSaveReview() {
  await saveReview({ activityId: reviewRow.value.activityId, photos: reviewForm.photos, review: reviewForm.review })
  ElMessage.success('回顾已保存'); reviewVisible.value = false; getList()
}

function openDetail(row) { currentRow.value = row; detailVisible.value = true }

// ==================== 报名管理 ====================
const signupVisible = ref(false), signupLoading = ref(false), signupActivity = ref(null)
const signupList = ref([]), signupTotal = ref(0), selectedSignups = ref([])
const signupParams = reactive({ pageNum: 1, pageSize: 10 })
const rejectVisible = ref(false), rejectForm = reactive({ reason: '' })
function signupStatusTag(s) { return { '已报名':'info','待审核':'warning','已通过':'success','已拒绝':'danger' }[s]||'' }
async function openSignup(row) { signupActivity.value=row; signupParams.pageNum=1; signupVisible.value=true; loadSignups() }
async function loadSignups() { if(!signupActivity.value) return; signupLoading.value=true; try { const res=await listSignup({activityId:signupActivity.value.activityId,...signupParams}); signupList.value=res.rows; signupTotal.value=res.total } finally { signupLoading.value=false } }
async function handleBatchApprove() { const ids=selectedSignups.value.map(s=>s.signupId); await batchApproveSignup(ids); ElMessage.success('已通过'); selectedSignups.value=[]; loadSignups() }
function openRejectDialog() { rejectForm.reason=''; rejectVisible.value=true }
async function handleBatchReject() { if(!rejectForm.reason){ElMessage.warning('请填写拒绝原因');return}; const ids=selectedSignups.value.map(s=>s.signupId); await batchRejectSignup(ids,rejectForm.reason); ElMessage.success('已拒绝'); rejectVisible.value=false; selectedSignups.value=[]; loadSignups() }
async function handleMarkAbsent(row) {
  proxy.$modal.confirm('确认标记为缺席？').then(async () => {
    await markAbsent(row.signupId)
    ElMessage.success('已标记缺席')
    loadSignups()
  })
}

function handleExport() {
  if(!signupActivity.value) return
  const token = getToken()
  const xhr = new XMLHttpRequest()
  xhr.open('GET', exportSignupUrl(signupActivity.value.activityId), true)
  xhr.setRequestHeader('Authorization', 'Bearer ' + token)
  xhr.responseType = 'blob'
  xhr.onload = function() {
    if (xhr.status === 200) {
      const blob = xhr.response
      if (blob.type.includes('json')) {
        // 后端返回了JSON错误
        const reader = new FileReader()
        reader.onload = () => { try { const r = JSON.parse(reader.result); ElMessage.error(r.msg || '导出失败') } catch(_) { ElMessage.error('导出失败') } }
        reader.readAsText(blob)
        return
      }
      const url = URL.createObjectURL(blob)
      const a = document.createElement('a')
      a.href = url; a.download = '报名名单.xlsx'; document.body.appendChild(a)
      a.click(); document.body.removeChild(a); URL.revokeObjectURL(url)
    } else {
      ElMessage.error('导出失败')
    }
  }
  xhr.onerror = () => ElMessage.error('网络错误')
  xhr.send()
}

getList()
</script>

<style scoped>
.mb8 { margin-bottom: 8px }
</style>
