<template>
  <div class="app-container">
    <el-form :model="queryParams" :inline="true" label-width="60px">
      <el-form-item label="标题">
        <el-input v-model="queryParams.title" placeholder="搜索活动" clearable style="width:200px" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="16">
      <el-col v-for="item in list" :key="item.activityId" :span="8" class="mb16">
        <el-card shadow="hover" :body-style="{ padding: '16px' }">
          <template #header>
            <div class="card-title">
              <el-tag v-if="item.isTop" type="warning" effect="dark" size="small" style="margin-right:6px">置顶</el-tag>
              <el-tag :type="typeTag(item.activityType)" size="small" style="margin-right:8px">{{ item.activityType }}</el-tag>
              <span>{{ item.title }}</span>
            </div>
          </template>
          <div class="info-row"><el-icon><Clock /></el-icon> {{ item.activityTime }}</div>
          <div class="info-row"><el-icon><Location /></el-icon> {{ item.location }}</div>
          <div class="info-row">
            <el-icon><User /></el-icon> {{ item.actualParticipants || 0 }}{{ item.maxParticipants ? '/' + item.maxParticipants : '' }}人
          </div>
          <div class="info-row">
            <el-icon><Money /></el-icon> {{ item.fee > 0 ? '¥' + item.fee : '免费' }}
            <el-tag v-if="item.targetAudience" size="small" style="margin-left:8px">{{ item.targetAudience }}</el-tag>
          </div>
          <el-alert v-if="isUpcoming(item)" title="活动即将开始，请准时参加！" type="warning" :closable="false" show-icon style="margin-bottom:8px" />
          <el-divider style="margin:10px 0" />
          <div class="card-foot">
            <template v-if="getSignupStatus(item.activityId)">
              <el-tag :type="signupTag(item.activityId)" size="small" style="margin-right:8px">
                {{ getSignupStatus(item.activityId) }}
              </el-tag>
              <el-button v-if="getSignupStatus(item.activityId) === '已拒绝'" type="primary" size="small" @click="handleReSignup(item)">重新报名</el-button>
              <el-tag v-if="getAttendStatus(item.activityId) === '已签到'" type="success" size="small" style="margin-right:4px">已签到</el-tag>
              <el-button v-else type="danger" size="small" plain @click="handleCancel(item)">取消报名</el-button>
              <div v-if="getRejectReason(item.activityId)" style="width:100%;margin-top:6px;font-size:12px;color:#f56c6c">
                原因：{{ getRejectReason(item.activityId) }}
              </div>
            </template>
            <el-button v-else type="primary" size="small" @click="handleSignup(item)">立即报名</el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>
    <!-- 签到按钮 -->
    <div style="text-align:right;margin-bottom:12px">
      <el-button type="success" icon="Check" @click="checkinVisible = true">活动签到</el-button>
    </div>

    <el-empty v-if="!loading && list.length === 0" description="暂无进行中的活动" />

    <!-- 签到弹窗 -->
    <el-dialog v-model="checkinVisible" title="活动签到" width="400px">
      <el-form @submit.prevent="handleCheckin">
        <el-form-item label="签到码">
          <el-input v-model="checkinCode" placeholder="请输入6位签到码" maxlength="6" style="width:200px" />
        </el-form-item>
        <el-form-item>
          <el-button type="success" @click="handleCheckin">确认签到</el-button>
        </el-form-item>
      </el-form>
    </el-dialog>
    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
  </div>
</template>

<script setup name="ActivityResident">
import { ref, reactive, onMounted, getCurrentInstance } from 'vue'
import { ElMessage } from 'element-plus'
import { Clock, Location, User, Money } from '@element-plus/icons-vue'
import { listPublished, addSignup, cancelSignup, mySignups, checkinActivity } from '@/api/com/activity'

const { proxy } = getCurrentInstance()
const loading = ref(false)
const list = ref([])
const total = ref(0)
const mySignupMap = ref({})
const queryParams = reactive({ pageNum: 1, pageSize: 12, title: null })

const typeTag = (t) => ({ '文体活动': 'success', '志愿服务': 'warning', '知识讲座': '', '节日庆典': 'danger', '亲子活动': 'primary', '健康义诊': 'success', '其他': 'info' }[t] || '')
const statusTagMap = { '已报名': '', '待审核': 'warning', '已通过': 'success', '已拒绝': 'danger' }

function getSignupStatus(activityId) { return mySignupMap.value[activityId]?.status }
function getRejectReason(activityId) { return mySignupMap.value[activityId]?.rejectReason }
function getAttendStatus(activityId) { return mySignupMap.value[activityId]?.attendStatus }
function isUpcoming(item) {
  if (!getSignupStatus(item.activityId)) return false
  if (!item.activityTime) return false
  const diff = new Date(item.activityTime).getTime() - Date.now()
  return diff > 0 && diff < 86400000 // 24小时内
}
function signupTag(activityId) { return statusTagMap[getSignupStatus(activityId)] || '' }

async function getList() {
  loading.value = true
  try {
    const res = await listPublished(queryParams)
    list.value = res.rows; total.value = res.total
  } finally { loading.value = false }
}
function handleQuery() { queryParams.pageNum = 1; getList() }
function resetQuery() { queryParams.title = null; handleQuery() }

async function loadMySignups() {
  try {
    const res = await mySignups()
    const map = {}
    ;(res.data || []).forEach(s => { map[s.activityId] = s })
    mySignupMap.value = map
  } catch { mySignupMap.value = {} }
}

async function handleSignup(item) {
  proxy.$modal.confirm(`确认报名「${item.title}」？`).then(async () => {
    await addSignup({ activityId: item.activityId })
    ElMessage.success('报名成功')
    await loadMySignups()
    item.actualParticipants = (item.actualParticipants || 0) + 1
  })
}

async function handleReSignup(item) {
  // 先取消旧报名，再重新报名
  proxy.$modal.confirm('重新报名该活动？').then(async () => {
    await cancelSignup(item.activityId)
    await addSignup({ activityId: item.activityId })
    ElMessage.success('已重新报名')
    await loadMySignups()
  })
}

async function handleCancel(item) {
  proxy.$modal.confirm('确认取消报名？').then(async () => {
    await cancelSignup(item.activityId)
    ElMessage.success('已取消')
    mySignupMap.value[item.activityId] = undefined
    item.actualParticipants = Math.max(0, (item.actualParticipants || 0) - 1)
  })
}

const checkinVisible = ref(false)
const checkinCode = ref('')

async function handleCheckin() {
  if (!checkinCode.value) { ElMessage.warning('请输入签到码'); return }
  try {
    await checkinActivity({ code: checkinCode.value })
    ElMessage.success('签到成功')
    checkinVisible.value = false
    checkinCode.value = ''
    await loadMySignups()
  } catch { /* error handled by interceptor */ }
}

onMounted(async () => {
  await getList()
  await loadMySignups()
})
</script>

<style scoped>
.card-title { font-weight: 600; display: flex; align-items: center }
.info-row { display: flex; align-items: center; gap: 6px; margin-bottom: 8px; font-size: 14px; color: #606266 }
.card-foot { display: flex; flex-wrap: wrap; align-items: center; justify-content: flex-end }
.mb16 { margin-bottom: 16px }
</style>
