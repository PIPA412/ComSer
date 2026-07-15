<template>
  <div class="app-container">
    <!-- 搜索栏（仅管理员） -->
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="80px" v-if="isAdmin">
      <el-form-item label="维修类型" prop="repairType">
        <el-select v-model="queryParams.repairType" placeholder="维修类型" clearable @change="handleQuery">
          <el-option label="水电维修" value="水电维修" />
          <el-option label="暖通维修" value="暖通维修" />
          <el-option label="门窗维修" value="门窗维修" />
          <el-option label="电梯维修" value="电梯维修" />
          <el-option label="管道疏通" value="管道疏通" />
          <el-option label="墙面地面" value="墙面地面" />
          <el-option label="其他" value="其他" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="状态" clearable @change="handleQuery">
          <el-option label="待处理" value="待处理" />
          <el-option label="处理中" value="处理中" />
          <el-option label="已完成" value="已完成" />
          <el-option label="已取消" value="已取消" />
        </el-select>
      </el-form-item>
      <el-form-item label="紧急程度" prop="urgency">
        <el-select v-model="queryParams.urgency" placeholder="紧急程度" clearable @change="handleQuery">
          <el-option label="一般" value="一般" />
          <el-option label="紧急" value="紧急" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 按钮栏 -->
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['com:repair:add']">提交报修</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
    </el-row>

    <!-- 标签页（仅管理员） -->
    <el-tabs v-model="activeTab" @tab-change="onTabChange" v-if="isAdmin">
      <el-tab-pane label="全部报修" name="" />
      <el-tab-pane label="待处理" name="待处理" />
      <el-tab-pane label="处理中" name="处理中" />
      <el-tab-pane label="已完成" name="已完成" />
    </el-tabs>

    <!-- 表格 -->
    <el-table v-loading="loading" :data="repairList" :row-class-name="rowClassName">
      <el-table-column type="selection" width="55" align="center" v-if="isAdmin" />
      <el-table-column label="报修编号" prop="repairNo" min-width="140" />
      <el-table-column label="楼栋/门牌" min-width="140">
        <template #default="scope">
          <span v-if="scope.row.buildingName || scope.row.roomNumber">
            {{ scope.row.buildingName || '' }} {{ scope.row.roomNumber || '' }}
          </span>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="维修类型" prop="repairType" min-width="100" />
      <el-table-column label="紧急程度" prop="urgency" width="90">
        <template #default="scope">
          <el-tag :type="scope.row.urgency === '紧急' ? 'danger' : 'info'" size="small">
            {{ scope.row.urgency }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态" prop="status" width="100">
        <template #default="scope">
          <el-tag :type="statusTagType(scope.row.status)" size="small">
            {{ scope.row.status }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="报修描述" prop="description" min-width="140" show-overflow-tooltip />
      <el-table-column label="报修时间" prop="createTime" width="160" />
      <el-table-column label="已过小时" width="95">
        <template #default="scope">
          <span v-if="scope.row.elapsedHours !== null && scope.row.elapsedHours !== undefined"
                :class="{ 'text-danger': scope.row.timeoutWarning }">
            {{ scope.row.elapsedHours }}h
            <el-tag v-if="scope.row.timeoutWarning" type="danger" size="small" style="margin-left:4px">超时</el-tag>
          </span>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="评价" width="80">
        <template #default="scope">
          <span v-if="scope.row.rating">{{ scope.row.rating }}星</span>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="240" fixed="right">
        <template #default="scope">
          <el-button link type="primary" icon="View" @click="handleDetail(scope.row)">详情</el-button>
          <el-button v-if="scope.row.status !== '已完成' && scope.row.status !== '已取消'"
                     link type="warning" icon="Sort"
                     @click="handleStatusSwitch(scope.row)" v-hasPermi="['com:repair:assign']">切换状态</el-button>
          <el-button link type="danger" icon="Delete"
                     @click="handleDelete(scope.row)" v-hasPermi="['com:repair:remove']">删除</el-button>
          <el-button v-if="scope.row.status === '待处理'" link type="danger" icon="Close"
                     @click="handleCancel(scope.row)" v-hasPermi="['com:repair:cancel']">取消</el-button>
          <el-button v-if="scope.row.status === '已完成' && !scope.row.rating" link type="warning" icon="Star"
                     @click="handleRate(scope.row)" v-hasPermi="['com:repair:rate']">评价</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum"
                v-model:limit="queryParams.pageSize" @pagination="getList" />

    <!-- ==================== 状态切换弹窗 ==================== -->
    <el-dialog title="切换状态" v-model="statusSwitchOpen" width="400px" append-to-body>
      <el-form label-width="90px">
        <el-form-item label="报修编号">
          <el-input :model-value="statusSwitchForm.repairNo" disabled />
        </el-form-item>
        <el-form-item label="当前状态">
          <el-tag :type="statusTagType(statusSwitchForm.currentStatus)" size="small">
            {{ statusSwitchForm.currentStatus }}
          </el-tag>
        </el-form-item>
        <el-form-item label="目标状态" prop="newStatus" required>
          <el-select v-model="statusSwitchForm.newStatus" placeholder="请选择目标状态" style="width:100%">
            <el-option v-if="statusSwitchForm.currentStatus === '待处理'" label="处理中" value="处理中" />
            <el-option v-if="statusSwitchForm.currentStatus === '待处理'" label="已完成" value="已完成" />
            <el-option v-if="statusSwitchForm.currentStatus === '待处理'" label="已取消" value="已取消" />
            <el-option v-if="statusSwitchForm.currentStatus === '处理中'" label="待处理" value="待处理" />
            <el-option v-if="statusSwitchForm.currentStatus === '处理中'" label="已完成" value="已完成" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="statusSwitchOpen = false">取 消</el-button>
        <el-button type="primary" @click="submitStatusSwitch" :disabled="!statusSwitchForm.newStatus">确 定</el-button>
      </template>
    </el-dialog>

    <!-- ==================== 提交报修弹窗 ==================== -->
    <el-dialog :title="title" v-model="addOpen" width="600px" append-to-body>
      <el-form ref="addFormRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="所在楼栋" prop="buildingId">
          <el-select v-model="form.buildingId" placeholder="请选择楼栋" style="width:100%"
                     @change="onBuildingChange" filterable>
            <el-option v-for="b in buildingOptions" :key="b.buildingId"
                       :label="b.buildingName" :value="b.buildingId" />
          </el-select>
        </el-form-item>
        <el-form-item label="门牌号" prop="roomId">
          <el-select v-model="form.roomId" placeholder="请先选择楼栋" style="width:100%"
                     filterable :disabled="!form.buildingId" @change="onRoomChange">
            <el-option v-for="r in roomOptions" :key="r.roomId"
                       :label="r.roomNumber" :value="r.roomId" />
          </el-select>
        </el-form-item>
        <el-form-item label="维修类型" prop="repairType">
          <el-select v-model="form.repairType" placeholder="请选择维修类型" style="width:100%">
            <el-option label="水电维修" value="水电维修" />
            <el-option label="暖通维修" value="暖通维修" />
            <el-option label="门窗维修" value="门窗维修" />
            <el-option label="电梯维修" value="电梯维修" />
            <el-option label="管道疏通" value="管道疏通" />
            <el-option label="墙面地面" value="墙面地面" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>
        <el-form-item label="紧急程度" prop="urgency">
          <el-radio-group v-model="form.urgency">
            <el-radio label="一般">一般</el-radio>
            <el-radio label="紧急">紧急</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="报修描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="4" placeholder="请详细描述报修问题" />
        </el-form-item>
        <el-form-item label="图片上传">
          <ImageUpload v-model="form.mediaUrls" :limit="6" :file-size="10"
                       :file-type="['png','jpg','jpeg','gif','mp4']" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addOpen = false">取 消</el-button>
        <el-button type="primary" @click="submitAdd">确 定</el-button>
      </template>
    </el-dialog>

    <!-- ==================== 详情弹窗 ==================== -->
    <el-dialog title="报修详情" v-model="detailOpen" width="750px" append-to-body>
      <template v-if="detailRepair">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="报修编号">{{ detailRepair.repairNo }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="statusTagType(detailRepair.status)" size="small">{{ detailRepair.status }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="楼栋">{{ detailRepair.buildingName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="门牌号">{{ detailRepair.roomNumber || '-' }}</el-descriptions-item>
          <el-descriptions-item label="维修类型">{{ detailRepair.repairType }}</el-descriptions-item>
          <el-descriptions-item label="紧急程度">
            <el-tag :type="detailRepair.urgency === '紧急' ? 'danger' : 'info'" size="small">
              {{ detailRepair.urgency }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="报修时间">{{ detailRepair.createTime }}</el-descriptions-item>
          <el-descriptions-item label="受理时间">{{ detailRepair.acceptTime || '-' }}</el-descriptions-item>
          <el-descriptions-item label="完成时间">{{ detailRepair.finishTime || '-' }}</el-descriptions-item>
          <el-descriptions-item label="报修描述" :span="2">{{ detailRepair.description }}</el-descriptions-item>
          <el-descriptions-item label="图片/视频" :span="2">
            <template v-if="detailRepair.mediaUrls">
              <div style="display:flex; flex-wrap:wrap; gap:8px">
                <el-image v-for="(url, i) in mediaUrlList" :key="i" :src="url"
                          style="width:100px; height:100px" fit="cover"
                          :preview-src-list="mediaUrlList" :initial-index="i" />
              </div>
            </template>
            <span v-else>无</span>
          </el-descriptions-item>
          <el-descriptions-item label="评分" v-if="detailRepair.rating">
            <el-rate :model-value="detailRepair.rating" disabled show-score text-color="#ff9900" />
          </el-descriptions-item>
          <el-descriptions-item label="评价内容" :span="2" v-if="detailRepair.feedback">
            {{ detailRepair.feedback }}
          </el-descriptions-item>
        </el-descriptions>

        <el-divider content-position="left">处理进度</el-divider>
        <el-timeline v-if="detailRecords.length > 0">
          <el-timeline-item v-for="rec in detailRecords" :key="rec.recordId"
                            :timestamp="rec.createTime"
                            :type="recordTimelineType(rec.actionType)"
                            placement="top">
            <p>{{ rec.description }}</p>
          </el-timeline-item>
        </el-timeline>
        <el-empty v-else description="暂无处理记录" :image-size="60" />
      </template>
      <template #footer>
        <el-button @click="detailOpen = false">关 闭</el-button>
        <el-button v-if="detailRepair && detailRepair.status === '已完成' && !detailRepair.rating"
                   type="warning" icon="Star" @click="detailOpen = false; handleRate(detailRepair)">
          去评价
        </el-button>
      </template>
    </el-dialog>

    <!-- ==================== 评价弹窗 ==================== -->
    <el-dialog title="满意度评价" v-model="rateOpen" width="500px" append-to-body>
      <el-form ref="rateFormRef" :model="rateForm" label-width="90px">
        <el-form-item label="报修编号">
          <el-input :model-value="rateForm.repairNo" disabled />
        </el-form-item>
        <el-form-item label="评分" prop="rating" required>
          <el-rate v-model="rateForm.rating" :texts="['非常差','差','一般','好','非常好']" show-text />
        </el-form-item>
        <el-form-item label="评价内容" prop="feedback">
          <el-input v-model="rateForm.feedback" type="textarea" :rows="3" placeholder="请分享您的评价（可选）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="rateOpen = false">取 消</el-button>
        <el-button type="primary" @click="submitRate" :disabled="!rateForm.rating">提 交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="Repair">
import { ref, computed, getCurrentInstance } from 'vue'
import {
  listRepair, myListRepair, getRepair, addRepair, delRepair,
  updateRepairStatus, rateRepair, cancelRepair
} from '@/api/com/repair'
import { getAllBuildings, getRoomByBuilding } from '@/api/com/property'
import useUserStore from '@/store/modules/user'

const { proxy } = getCurrentInstance()
const userStore = useUserStore()

// ==================== 角色判断 ====================
const isAdmin = computed(() => {
  const roles = userStore.roles || []
  // roles可能是字符串数组或对象数组，兼容两种情况
  return roles.some(r => {
    if (typeof r === 'string') return r === 'admin' || r === 'admin_community'
    return r.roleKey === 'admin' || r.roleKey === 'admin_community' || r === 'admin' || r === 'admin_community'
  })
})

// ==================== 列表数据 ====================
const repairList = ref([])
const loading = ref(true)
const showSearch = ref(true)
const total = ref(0)
const activeTab = ref('')
const queryParams = ref({ pageNum: 1, pageSize: 10, repairType: null, status: null, urgency: null })

function getList() {
  loading.value = true
  const params = {}
  Object.keys(queryParams.value).forEach(k => {
    const v = queryParams.value[k]
    if (v !== null && v !== undefined && v !== '') params[k] = v
  })
  // 管理员用 listRepair（所有报修），普通用户用 myListRepair（仅自己）
  const api = isAdmin.value ? listRepair : myListRepair
  api(params).then(res => {
    repairList.value = res.rows || []
    total.value = res.total
    loading.value = false
  }).catch(() => { loading.value = false })
}

function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

function resetQuery() {
  proxy.$refs.queryRef?.resetFields()
  // 重置后同步tab
  activeTab.value = ''
  handleQuery()
}

function onTabChange(tab) {
  // tab与搜索栏状态同步
  queryParams.value.status = tab || null
  queryParams.value.pageNum = 1
  getList()
}

// ==================== 状态样式 ====================
function statusTagType(status) {
  const map = { '待处理': 'info', '处理中': 'warning', '已完成': 'success', '已取消': '' }
  return map[status] || ''
}

function rowClassName({ row }) {
  return row.timeoutWarning ? 'row-timeout-warning' : ''
}

// ==================== 状态切换弹窗（管理员） ====================
const statusSwitchOpen = ref(false)
const statusSwitchForm = ref({ repairId: null, repairNo: '', currentStatus: '', newStatus: null })

function handleStatusSwitch(row) {
  statusSwitchForm.value = {
    repairId: row.repairId,
    repairNo: row.repairNo,
    currentStatus: row.status,
    newStatus: null
  }
  statusSwitchOpen.value = true
}

async function submitStatusSwitch() {
  if (!statusSwitchForm.value.newStatus) return
  const ns = statusSwitchForm.value.newStatus
  try {
    if (ns === '已取消') {
      await cancelRepair(statusSwitchForm.value.repairId)
      proxy.$modal.msgSuccess('已取消')
    } else {
      await updateRepairStatus(statusSwitchForm.value.repairId, ns)
      proxy.$modal.msgSuccess('状态已更新')
    }
    statusSwitchOpen.value = false
    getList()
  } catch (e) {}
}

// ==================== 提交报修 ====================
const addOpen = ref(false)
const title = ref('提交报修')
const buildingOptions = ref([])
const roomOptions = ref([])
const form = ref({
  buildingId: null, buildingName: '', roomId: null, roomNumber: '',
  repairType: null, urgency: '一般', description: '', mediaUrls: ''
})
const rules = {
  buildingId: [{ required: true, message: '请选择楼栋', trigger: 'change' }],
  roomId: [{ required: true, message: '请选择门牌号', trigger: 'change' }],
  repairType: [{ required: true, message: '请选择维修类型', trigger: 'change' }],
  urgency: [{ required: true, message: '请选择紧急程度', trigger: 'change' }],
  description: [{ required: true, message: '请输入报修描述', trigger: 'blur' }]
}

function handleAdd() {
  title.value = '提交报修'
  form.value = {
    buildingId: null, buildingName: '', roomId: null, roomNumber: '',
    repairType: null, urgency: '一般', description: '', mediaUrls: ''
  }
  roomOptions.value = []
  proxy.$refs.addFormRef?.resetFields()
  loadBuildings()
  addOpen.value = true
}

function loadBuildings() {
  getAllBuildings().then(res => {
    buildingOptions.value = res.data || []
  })
}

function onBuildingChange(buildingId) {
  form.value.roomId = null
  form.value.roomNumber = ''
  form.value.buildingName = ''
  roomOptions.value = []
  if (buildingId) {
    // 保存楼栋名称
    const b = buildingOptions.value.find(x => x.buildingId === buildingId)
    if (b) form.value.buildingName = b.buildingName
    getRoomByBuilding(buildingId).then(res => {
      roomOptions.value = res.data || []
    })
  }
}

function onRoomChange(roomId) {
  form.value.roomNumber = ''
  if (roomId) {
    const r = roomOptions.value.find(x => x.roomId === roomId)
    if (r) form.value.roomNumber = r.roomNumber
  }
}

async function submitAdd() {
  try {
    const valid = await proxy.$refs.addFormRef?.validate()
    if (!valid) return
    await addRepair(form.value)
    proxy.$modal.msgSuccess('报修提交成功')
    addOpen.value = false
    queryParams.value.pageNum = 1
    getList()
  } catch (e) {
    // 表单验证失败或请求失败时不做额外处理
  }
}

// ==================== 评价 ====================
const rateOpen = ref(false)
const rateForm = ref({ repairId: null, repairNo: '', rating: 0, feedback: '' })

function handleRate(row) {
  rateForm.value = { repairId: row.repairId, repairNo: row.repairNo, rating: 0, feedback: '' }
  rateOpen.value = true
}

async function submitRate() {
  if (!rateForm.value.rating) { proxy.$modal.msgError('请选择评分'); return }
  try {
    await rateRepair({
      repairId: rateForm.value.repairId,
      rating: rateForm.value.rating,
      feedback: rateForm.value.feedback
    })
    proxy.$modal.msgSuccess('评价成功，感谢您的反馈')
    rateOpen.value = false
    getList()
  } catch (e) {}
}

// ==================== 取消 ====================
async function handleCancel(row) {
  try {
    await proxy.$modal.confirm('确认取消该报修单？')
    await cancelRepair(row.repairId)
    proxy.$modal.msgSuccess('已取消')
    getList()
  } catch (e) {}
}

// ==================== 删除 ====================
async function handleDelete(row) {
  try {
    await proxy.$modal.confirm('确认删除该报修单？')
    await delRepair(row.repairId)
    proxy.$modal.msgSuccess('删除成功')
    getList()
  } catch (e) {}
}

// ==================== 详情 ====================
const detailOpen = ref(false)
const detailRepair = ref(null)
const detailRecords = ref([])

function handleDetail(row) {
  getRepair(row.repairId).then(res => {
    detailRepair.value = res.data.repair
    detailRecords.value = res.data.records || []
    detailOpen.value = true
  })
}

const mediaUrlList = computed(() => {
  if (!detailRepair.value?.mediaUrls) return []
  return detailRepair.value.mediaUrls.split(',').filter(u => u).map(u => {
    if (u.startsWith('http')) return u
    return import.meta.env.VITE_APP_BASE_API + u
  })
})

function recordTimelineType(actionType) {
  const map = { '派单': 'primary', '受理': 'primary', '到场': 'warning', '完工': 'success', '评价': 'warning', '取消': 'danger' }
  return map[actionType] || 'info'
}

// ==================== 初始化 ====================
getList()
</script>

<style scoped>
.row-timeout-warning { background-color: #fef0f0 !important; }
.row-timeout-warning:hover > td { background-color: #fde2e2 !important; }
.text-danger { color: #f56c6c; font-weight: bold; }
</style>
