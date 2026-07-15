<template>
  <div class="dashboard-container">
    <!-- 顶部工具栏：标题 + 全局筛选 -->
    <el-card shadow="never" class="toolbar-card">
      <div class="toolbar">
        <span class="title">数据驾驶舱</span>
        <div class="filters">
          <el-select v-model="buildingId" placeholder="全部楼栋" clearable style="width:180px" @change="refreshAll">
            <el-option v-for="b in buildings" :key="b.buildingId" :label="b.buildingName" :value="b.buildingId" />
          </el-select>
          <el-select v-model="timeRange" placeholder="时间范围" style="width:140px" @change="refreshAll">
            <el-option label="本月" value="1month" />
            <el-option label="近3个月" value="3months" />
            <el-option label="近半年" value="6months" />
            <el-option label="近1年" value="12months" />
          </el-select>
          <el-button type="primary" @click="refreshAll">刷新</el-button>
        </div>
      </div>
    </el-card>

    <!-- 预警区块 -->
    <div class="alert-section">
      <div v-if="alerts.length > 0" style="margin-bottom:8px">
        <el-alert
          v-for="(a, i) in alerts.slice(0, 3)" :key="i"
          :title="a.title" :type="a.type || 'warning'" show-icon closable
          style="margin-bottom:8px"
          @close="alerts.splice(i, 1)">
          <template #action>
            <el-button v-if="a.recordId" size="small" type="warning" @click="handleAlert(a.recordId)">标记已处理</el-button>
          </template>
        </el-alert>
      </div>
      <el-empty v-if="!alerts.length" description="暂无待处理预警" :image-size="60" />
      <div style="text-align:right;margin-bottom:8px">
        <el-button size="small" @click="showAlertRuleDialog">预警规则</el-button>
        <el-button size="small" @click="showAlertRecordDialog">预警记录</el-button>
      </div>
    </div>

    <!-- 预警规则弹窗 -->
    <el-dialog title="预警规则" v-model="alertRuleVisible" width="700px" top="5vh" append-to-body :close-on-click-modal="false">
      <template #header>
        <span>预警规则</span>
        <el-button size="small" type="primary" style="margin-left:12px" @click="showAddRuleDialog">新增规则</el-button>
      </template>
      <el-table :data="alertRules" v-loading="ruleLoading" size="small" max-height="400" stripe>
        <el-table-column label="规则名称" prop="ruleName" min-width="130" />
        <el-table-column label="指标" prop="metricName" width="100" />
        <el-table-column label="比较" prop="compareType" width="70">
          <template #default="s">{{ {GT:'>', LT:'<', GTE:'>=', LTE:'<='}[s.row.compareType] || s.row.compareType }}</template>
        </el-table-column>
        <el-table-column label="阈值" prop="threshold" width="70" />
        <el-table-column label="状态" prop="status" width="70">
          <template #default="s"><el-tag :type="s.row.status === '0' ? 'success' : 'danger'" size="small">{{ s.row.status === '0' ? '启用' : '停用' }}</el-tag></template>
        </el-table-column>
        <el-table-column label="最近触发" prop="lastTriggerTime" width="150" />
        <el-table-column label="操作" width="150">
          <template #default="s">
            <el-button link type="primary" size="small" @click="showEditRuleDialog(s.row)">编辑</el-button>
            <el-button v-if="s.row.status === '0'" link type="warning" size="small" @click="toggleRule(s.row)">停用</el-button>
            <el-button v-else link type="success" size="small" @click="toggleRule(s.row)">启用</el-button>
            <el-button link type="danger" size="small" @click="deleteRule(s.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <!-- 新增/编辑规则弹窗 -->
    <el-dialog :title="ruleForm.ruleId ? '编辑规则' : '新增规则'" v-model="ruleFormVisible" width="500px" top="20vh" append-to-body :close-on-click-modal="false">
      <el-form :model="ruleForm" label-width="90px" size="small">
        <el-form-item label="规则名称">
          <el-input v-model="ruleForm.ruleName" placeholder="如：收缴率过低" />
        </el-form-item>
        <el-form-item label="指标">
          <el-select v-model="ruleForm.metricKey" placeholder="选择指标" style="width:100%">
            <el-option label="当月收缴率" value="collection_rate" />
            <el-option label="月度投诉量" value="complaint_monthly" />
            <el-option label="报修超时率" value="repair_overdue_rate" />
          </el-select>
        </el-form-item>
        <el-form-item label="指标名称">
          <el-input v-model="ruleForm.metricName" placeholder="展示用名称" />
        </el-form-item>
        <el-form-item label="比较方式">
          <el-select v-model="ruleForm.compareType" style="width:100%">
            <el-option label="大于 (GT)" value="GT" />
            <el-option label="小于 (LT)" value="LT" />
            <el-option label="大于等于 (GTE)" value="GTE" />
            <el-option label="小于等于 (LTE)" value="LTE" />
          </el-select>
        </el-form-item>
        <el-form-item label="阈值">
          <el-input-number v-model="ruleForm.threshold" :min="0" :precision="2" style="width:100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button size="small" @click="ruleFormVisible = false">取消</el-button>
        <el-button size="small" type="primary" @click="saveRule">保存</el-button>
      </template>
    </el-dialog>

    <!-- 预警记录弹窗 -->
    <el-dialog title="预警记录" v-model="alertRecordVisible" width="800px" top="5vh" append-to-body :close-on-click-modal="false">
      <template #header>
        <span>预警记录</span>
        <el-select v-model="recordFilterStatus" placeholder="处置状态" clearable size="small" style="width:120px;margin-left:12px" @change="loadAlertRecords">
          <el-option label="待处理" value="待处理" />
          <el-option label="已处理" value="已处理" />
          <el-option label="已忽略" value="已忽略" />
        </el-select>
      </template>
      <el-table :data="alertRecords" v-loading="recordLoading" size="small" max-height="450" stripe>
        <el-table-column label="规则" prop="ruleName" min-width="120" />
        <el-table-column label="指标" prop="metricName" width="90" />
        <el-table-column label="触发值" prop="triggerValue" width="70">
          <template #default="s"><el-tag type="danger" size="small">{{ s.row.triggerValue }}</el-tag></template>
        </el-table-column>
        <el-table-column label="触发时间" prop="triggerTime" width="150" />
        <el-table-column label="状态" prop="handleStatus" width="80">
          <template #default="s">
            <el-tag :type="s.row.handleStatus === '待处理' ? 'danger' : s.row.handleStatus === '已处理' ? 'success' : 'info'" size="small">{{ s.row.handleStatus }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="处置备注" prop="handleRemark" min-width="120" />
        <el-table-column label="操作" width="160">
          <template #default="s">
            <el-button v-if="s.row.handleStatus === '待处理'" link type="success" size="small" @click="markAlert(s.row, '已处理')">已处理</el-button>
            <el-button v-if="s.row.handleStatus === '待处理'" link type="info" size="small" @click="markAlert(s.row, '已忽略')">忽略</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <el-row :gutter="16" v-loading="loading">
      <!-- ===== 第1行：人口 + 房屋 ===== -->
      <el-col :xs="24" :lg="12">
        <el-card shadow="never" class="panel-card">
          <template #header><span class="panel-title">人口概览</span></template>
          <el-row :gutter="12">
            <el-col :span="6"><stat-card title="总户数" :value="d.population?.totalRooms" /></el-col>
            <el-col :span="6"><stat-card title="入住户数" :value="d.population?.occupiedRooms" color="#67c23a" /></el-col>
            <el-col :span="6"><stat-card title="入住率" :value="d.population?.occupancyRate" suffix="%" /></el-col>
            <el-col :span="6"><stat-card title="总人口" :value="d.population?.totalPopulation" color="#409eff" /></el-col>
          </el-row>
          <el-row :gutter="12" style="margin-top:12px">
            <el-col :span="6"><stat-card title="业主" :value="d.population?.ownerCount" color="#409eff" /></el-col>
            <el-col :span="6"><stat-card title="租户" :value="d.population?.tenantCount" color="#e6a23c" /></el-col>
          </el-row>
          <el-row :gutter="16" style="margin-top:16px">
            <el-col :span="12"><div ref="genderChart" style="height:190px" /></el-col>
            <el-col :span="12"><div ref="typeChart" style="height:190px" /></el-col>
          </el-row>
          <div ref="moveChart" style="height:220px;margin-top:12px" />
          <div ref="buildingOccChart" v-if="d.population?.buildingOccupancyList?.length" :style="{ height: '260px', marginTop: '12px', cursor: 'pointer' }" @click="drillTo('/property')" title="点击查看楼栋详情" />
        </el-card>
      </el-col>

      <el-col :xs="24" :lg="12">
        <el-card shadow="never" class="panel-card">
          <template #header><span class="panel-title">房屋概览</span></template>
          <el-row :gutter="16">
            <el-col :span="12"><div ref="roomStatusChart" style="height:220px" /></el-col>
            <el-col :span="12"><div ref="areaChart" style="height:220px" /></el-col>
          </el-row>
          <div ref="layoutChart" style="height:220px;margin-top:12px" />
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" style="margin-top:16px">
      <!-- ===== 第2行：工单 ===== -->
      <el-col :xs="24" :lg="12">
        <el-card shadow="never" class="panel-card">
          <template #header><span class="panel-title">工单概览</span></template>
          <el-row :gutter="12">
            <el-col :span="6"><stat-card title="今日新增" :value="d.repair?.todayNew" color="#f56c6c" /></el-col>
            <el-col :span="6"><stat-card title="待受理" :value="d.repair?.pendingCount" color="#e6a23c" /></el-col>
            <el-col :span="6"><stat-card title="处理中" :value="d.repair?.processingCount" color="#409eff" /></el-col>
            <el-col :span="6"><stat-card title="待确认" :value="d.repair?.waitingConfirmCount" /></el-col>
          </el-row>
              <div ref="repairTypeChart" v-if="d.repair?.repairTypeTop5?.length" :style="{ height: '180px', marginTop: '12px', cursor: 'pointer' }" @click="drillTo('/repair')" title="点击查看报修列表" />
          <div ref="repairTrendChart" v-if="d.repair?.monthlyTrend?.length" style="height:180px;margin-top:12px" />
          <div ref="workerRankChart" v-if="d.repair?.workerRankList?.length" style="height:200px;margin-top:12px" />
          <el-table v-if="d.repair?.overdueList?.length" :data="d.repair.overdueList" size="small" style="margin-top:12px;cursor:pointer" max-height="200"
            @row-click="drillTo('/repair')">
            <el-table-column label="编号" prop="repairNo" width="140" />
            <el-table-column label="类型" prop="repairType" />
            <el-table-column label="状态" prop="status"><template #default="s"><el-tag :type="s.row.isOverdue ? 'danger' : 'warning'" size="small">{{ s.row.status }}</el-tag></template></el-table-column>
            <el-table-column label="创建时间" prop="createTime" width="140" />
          </el-table>
        </el-card>
      </el-col>

      <!-- ===== 第2行：收费 ===== -->
      <el-col :xs="24" :lg="12">
        <el-card shadow="never" class="panel-card">
          <template #header><span class="panel-title">收费概览</span></template>
          <el-row :gutter="12">
            <el-col :span="6"><stat-card title="本月应收" :value="d.fee?.monthReceivable" suffix="元" color="#409eff" /></el-col>
            <el-col :span="6"><stat-card title="本月实收" :value="d.fee?.monthReceived" suffix="元" color="#67c23a" /></el-col>
            <el-col :span="6"><stat-card title="收缴率" :value="d.fee?.collectionRate" suffix="%" :color="feeRateColor" /></el-col>
            <el-col :span="6"><stat-card title="累计欠费" :value="d.fee?.totalArrears" suffix="元" color="#f56c6c" /></el-col>
          </el-row>
          <el-row style="margin-top:12px" v-if="d.fee?.arrearsOver90DaysCount">
            <el-col :span="24">
              <el-alert :title="`欠费超90天：${d.fee.arrearsOver90DaysCount}户，金额 ¥${d.fee.arrearsOver90DaysAmount || 0}`" type="error" show-icon :closable="false" />
            </el-col>
          </el-row>
          <div ref="feeTrendChart" style="height:220px;margin-top:12px" />
          <el-row :gutter="16" style="margin-top:12px">
            <el-col :span="12"><div ref="feeStructChart" style="height:220px" /></el-col>
            <el-col :span="12"><div ref="feeBuildingChart" v-if="d.fee?.buildingRateList?.length" :style="{ height: '220px', cursor: 'pointer' }" @click="drillTo('/fee')" title="点击查看费用详情" /></el-col>
          </el-row>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" style="margin-top:16px">
      <!-- ===== 第3行：投诉 ===== -->
      <el-col :xs="24" :lg="12">
        <el-card shadow="never" class="panel-card">
          <template #header><span class="panel-title">投诉概览</span></template>
          <el-row :gutter="12">
            <el-col :span="8"><stat-card title="近30天新增" :value="d.complaint?.recentNewCount" color="#f56c6c" /></el-col>
            <el-col :span="8"><stat-card title="待处理" :value="d.complaint?.pendingCount" color="#e6a23c" /></el-col>
            <el-col :span="8"><stat-card title="超时未处理" :value="d.complaint?.overdueCount" color="#f56c6c" /></el-col>
          </el-row>
          <el-row :gutter="16" style="margin-top:12px">
            <el-col :span="12"><div ref="complaintTypeChart" style="height:190px" /></el-col>
            <el-col :span="12"><div ref="satisfactionChart" style="height:190px" /></el-col>
          </el-row>
          <div ref="durationChart" style="height:190px;margin-top:12px" />
        </el-card>
      </el-col>

      <!-- ===== 第3行：活动 ===== -->
      <el-col :xs="24" :lg="12">
        <el-card shadow="never" class="panel-card">
          <template #header><span class="panel-title">活动概览</span></template>
          <el-row :gutter="12">
            <el-col :span="8"><stat-card title="年度场次" :value="d.activity?.totalEvents" color="#409eff" /></el-col>
            <el-col :span="8"><stat-card title="总参与人次" :value="d.activity?.totalParticipants" color="#67c23a" /></el-col>
            <el-col :span="8"><stat-card title="平均签到率" :value="d.activity?.avgCheckInRate" suffix="%" color="#e6a23c" /></el-col>
          </el-row>
          <el-row :gutter="16" style="margin-top:12px">
            <el-col :span="12"><div ref="activityTypeChart" style="height:220px" /></el-col>
            <el-col :span="12"><div ref="activityTrendChart" style="height:220px" /></el-col>
          </el-row>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup name="Dashboard">
import { ref, reactive, computed, onMounted, nextTick, getCurrentInstance } from 'vue'
import { useRouter } from 'vue-router'
import * as echarts from 'echarts'
import { getDashboardAll, getDashboardBuildings, getPendingAlerts, handleAlertRecord, listAlertRules, listAlertRecords, getAllAlertRules, updateAlertRule, addAlertRule, delAlertRule } from '@/api/com/dashboard'

const { proxy } = getCurrentInstance()
const router = useRouter()
const loading = ref(false)
const buildingId = ref(null)
const timeRange = ref('12months')
const buildings = ref([])
const alerts = ref([])

const d = reactive({
  population: null, room: null, repair: null, fee: null, complaint: null, activity: null
})

// 图表 refs
const genderChart = ref(null); const typeChart = ref(null); const moveChart = ref(null); const buildingOccChart = ref(null)
const roomStatusChart = ref(null); const areaChart = ref(null); const layoutChart = ref(null)
const repairTypeChart = ref(null); const repairTrendChart = ref(null); const workerRankChart = ref(null)
const feeTrendChart = ref(null); const feeStructChart = ref(null); const feeBuildingChart = ref(null)
const complaintTypeChart = ref(null); const satisfactionChart = ref(null); const durationChart = ref(null)
const activityTypeChart = ref(null); const activityTrendChart = ref(null)

// 收缴率颜色：低于 80% 红色
const feeRateColor = computed(() => {
  const v = d.fee?.collectionRate
  if (v == null) return '#909399'
  return v < 80 ? '#f56c6c' : '#67c23a'
})

// 下钻跳转
function drillTo(path) {
  router.push(path)
}

async function refreshAll() {
  loading.value = true
  try {
    const [dashRes, alertRes] = await Promise.all([
      getDashboardAll({ buildingId: buildingId.value, timeRange: timeRange.value }),
      getPendingAlerts()
    ])
    Object.assign(d, dashRes.data)
    const pendingList = alertRes.data || []
    alerts.value = pendingList.map(r => ({
      title: `⚠️ ${r.ruleName}：${r.metricName} 触发值=${r.triggerValue}，阈值=${r.threshold}（${r.triggerTime}）`,
      type: 'warning',
      recordId: r.recordId
    }))
    await nextTick()
    renderAllCharts()
  } finally { loading.value = false }
}

function handleAlert(recordId) {
  proxy.$modal.confirm('确认将该预警标记为已处理？').then(() => {
    handleAlertRecord({ recordId, handleStatus: '已处理' }).then(() => {
      proxy.$modal.msgSuccess('已处理')
      refreshAll()
    })
  }).catch(() => {})
}

// ========== 预警规则弹窗 ==========
const alertRuleVisible = ref(false)
const alertRules = ref([])
const ruleLoading = ref(false)

function showAlertRuleDialog() {
  alertRuleVisible.value = true
  ruleLoading.value = true
  getAllAlertRules().then(res => {
    const list = res.data || []
    alertRules.value = list
  }).finally(() => { ruleLoading.value = false })
}

function toggleRule(row) {
  const newStatus = row.status === '0' ? '1' : '0'
  proxy.$modal.confirm(newStatus === '1' ? '确定停用该规则？' : '确定启用该规则？').then(() => {
    updateAlertRule({ ruleId: row.ruleId, status: newStatus }).then(() => {
      proxy.$modal.msgSuccess(newStatus === '1' ? '已停用' : '已启用')
      showAlertRuleDialog()
    })
  }).catch(() => {})
}

// ========== 新增/编辑规则 ==========
const ruleFormVisible = ref(false)
const ruleForm = reactive({ ruleId: null, ruleName: '', metricKey: '', metricName: '', compareType: 'LT', threshold: 0 })

function showAddRuleDialog() {
  ruleForm.ruleId = null
  ruleForm.ruleName = ''
  ruleForm.metricKey = ''
  ruleForm.metricName = ''
  ruleForm.compareType = 'LT'
  ruleForm.threshold = 0
  ruleFormVisible.value = true
}

function showEditRuleDialog(row) {
  Object.assign(ruleForm, row)
  ruleFormVisible.value = true
}

function saveRule() {
  if (!ruleForm.ruleName || !ruleForm.metricKey) {
    proxy.$modal.msgWarning('请填写完整信息')
    return
  }
  const save = ruleForm.ruleId ? updateAlertRule(ruleForm) : addAlertRule(ruleForm)
  save.then(() => {
    proxy.$modal.msgSuccess(ruleForm.ruleId ? '已更新' : '已新增')
    ruleFormVisible.value = false
    showAlertRuleDialog()
  })
}

function deleteRule(row) {
  proxy.$modal.confirm('确定删除规则"' + row.ruleName + '"？').then(() => {
    delAlertRule(row.ruleId).then(() => {
      proxy.$modal.msgSuccess('已删除')
      showAlertRuleDialog()
    })
  }).catch(() => {})
}

// ========== 预警记录弹窗 ==========
const alertRecordVisible = ref(false)
const alertRecords = ref([])
const recordLoading = ref(false)
const recordFilterStatus = ref('')

function showAlertRecordDialog() {
  alertRecordVisible.value = true
  loadAlertRecords()
}

function loadAlertRecords() {
  recordLoading.value = true
  const params = { pageNum: 1, pageSize: 100 }
  if (recordFilterStatus.value) params.handleStatus = recordFilterStatus.value
  listAlertRecords(params).then(res => {
    alertRecords.value = res.rows || []
  }).finally(() => { recordLoading.value = false })
}

function markAlert(row, status) {
  proxy.$modal.confirm('确认标记为' + (status === '已处理' ? '已处理？' : '已忽略？')).then(() => {
    handleAlertRecord({ recordId: row.recordId, handleStatus: status }).then(() => {
      proxy.$modal.msgSuccess('已' + (status === '已处理' ? '处理' : '忽略'))
      loadAlertRecords()
      refreshAll()
    })
  }).catch(() => {})
}

function renderAllCharts() {
  renderPie(genderChart.value, '性别分布', d.population?.genderDistribution)
  renderPie(typeChart.value, '年龄分布', d.population?.ageDistribution)
  renderLine(moveChart.value, d.population?.moveInOutTrend, ['入住', '迁出'], ['moveIn', 'moveOut'])
  renderBar(buildingOccChart.value, '楼栋入住率', d.population?.buildingOccupancyList, 'buildingName', 'occupancyRate', '%')
  renderPie(roomStatusChart.value, '房屋状态', d.room?.statusDistribution)
  renderBarSimple(areaChart.value, '面积分布', d.room?.areaDistribution)
  renderPie(layoutChart.value, '户型分布', d.room?.layoutDistribution)
  renderBarH(repairTypeChart.value, '报修类型 TOP5', d.repair?.repairTypeTop5)
  renderLineSimple(repairTrendChart.value, '报修趋势', d.repair?.monthlyTrend)
  renderWorkerRank(workerRankChart.value, d.repair?.workerRankList)
  renderLineSimple(feeTrendChart.value, '收缴率趋势', mapToRate(d.fee?.monthlyTrend), 'count')
  renderPie(feeStructChart.value, '费用结构', d.fee?.feeStructure)
  renderBarRed(feeBuildingChart.value, '楼栋收缴率', d.fee?.buildingRateList, 'buildingName', 'rate', '%', 80)
  renderPie(complaintTypeChart.value, '投诉类型', d.complaint?.typeDistribution)
  renderLineSimple(satisfactionChart.value, '满意度趋势', d.complaint?.satisfactionTrend, 'avgRating')
  renderLineSimple(durationChart.value, '处理时长(h)', d.complaint?.durationTrend, 'avgHours')
  renderPie(activityTypeChart.value, '活动类型', d.activity?.typeDistribution)
  renderBarTrend(activityTrendChart.value, '活动场次趋势', d.activity?.monthlyTrend, 'count')
}

function renderWorkerRank(dom, list) {
  if (!dom || !list?.length) return
  const chart = echarts.init(dom)
  chart.setOption({
    tooltip: { trigger: 'axis' },
    title: { text: '维修人员效率排名', left: 'center', textStyle: { fontSize: 13 } },
    grid: { left: 80, right: 30, top: 30, bottom: 20 },
    xAxis: { type: 'value' },
    yAxis: { type: 'category', data: list.map(i => i.workerName).reverse(), inverse: true },
    series: [{
      type: 'bar', name: '完成数',
      data: list.map(i => i.completedCount).reverse(),
      barWidth: 14, itemStyle: { borderRadius: [0, 4, 4, 0], color: '#409eff' }
    }]
  })
}

function renderPie(dom, name, dataMap) {
  if (!dom || !dataMap) return
  const chart = echarts.init(dom)
  const data = Object.entries(dataMap).map(([k, v]) => ({ name: k, value: v || 0 }))
  chart.setOption({
    tooltip: { trigger: 'item', formatter: '{b}: {d}%' },
    title: { text: name, left: 'center', top: 0, textStyle: { fontSize: 12 } },
    series: [{ type: 'pie', radius: ['25%', '50%'], center: ['50%', '53%'], label: { formatter: '{b}', fontSize: 9, color: '#666' }, labelLine: { show: true, length: 6, length2: 6 }, data, itemStyle: { borderRadius: 2 } }]
  })
}

function renderBarH(dom, name, entries) {
  if (!dom || !entries?.length) return
  const chart = echarts.init(dom)
  chart.setOption({
    tooltip: { trigger: 'axis' },
    title: { text: name, left: 'center', textStyle: { fontSize: 13 } },
    grid: { left: 80, right: 30, top: 30, bottom: 20 },
    xAxis: { type: 'value' },
    yAxis: { type: 'category', data: entries.map(e => e[0]).reverse(), inverse: true },
    series: [{ type: 'bar', data: entries.map(e => e[1]).reverse(), barWidth: 16, itemStyle: { borderRadius: [0, 4, 4, 0] } }]
  })
}

function renderBar(dom, name, list, xKey, yKey, suffix) {
  if (!dom || !list?.length) return
  const chart = echarts.init(dom)
  chart.setOption({
    tooltip: { trigger: 'axis', valueFormatter: v => v + (suffix || '') },
    title: { text: name, left: 'center', textStyle: { fontSize: 13 } },
    grid: { left: 40, right: 20, top: 40, bottom: 30 },
    xAxis: { type: 'category', data: list.map(i => i[xKey]), axisLabel: { rotate: 30 } },
    yAxis: { type: 'value' },
    series: [{ type: 'bar', data: list.map(i => i[yKey]), barWidth: 20, itemStyle: { borderRadius: [4, 4, 0, 0] } }]
  })
}

function renderBarRed(dom, name, list, xKey, yKey, suffix, threshold) {
  if (!dom || !list?.length) return
  const chart = echarts.init(dom)
  chart.setOption({
    tooltip: { trigger: 'axis', valueFormatter: v => v + (suffix || '') },
    title: { text: name, left: 'center', textStyle: { fontSize: 13 } },
    grid: { left: 40, right: 20, top: 40, bottom: 30 },
    xAxis: { type: 'category', data: list.map(i => i[xKey]), axisLabel: { rotate: 30 } },
    yAxis: { type: 'value' },
    series: [{
      type: 'bar', data: list.map(i => ({
        value: i[yKey],
        itemStyle: { color: i[yKey] < threshold ? '#f56c6c' : '#409eff', borderRadius: [4, 4, 0, 0] }
      })), barWidth: 20
    }]
  })
}

function renderLine(dom, list, names, keys) {
  if (!dom || !list?.length) return
  const chart = echarts.init(dom)
  chart.setOption({
    tooltip: { trigger: 'axis' },
    title: { text: '入住/迁出趋势', left: 'center', textStyle: { fontSize: 13 } },
    grid: { left: 50, right: 20, top: 40, bottom: 40 },
    xAxis: { type: 'category', data: list.map(i => i.month?.substring(5)) },
    yAxis: { type: 'value' },
    series: [
      { name: names[0], type: 'line', data: list.map(i => i[keys[0]]), smooth: true, itemStyle: { color: '#67c23a' } },
      { name: names[1], type: 'line', data: list.map(i => i[keys[1]]), smooth: true, itemStyle: { color: '#f56c6c' } }
    ],
    legend: { bottom: 0 }
  })
}

function renderLineSimple(dom, name, list, key) {
  if (!dom || !list?.length) return
  const chart = echarts.init(dom)
  const keyName = key || 'count'
  chart.setOption({
    tooltip: { trigger: 'axis' },
    title: { text: name, left: 'center', textStyle: { fontSize: 13 } },
    grid: { left: 45, right: 20, top: 40, bottom: 20 },
    xAxis: { type: 'category', data: list.map(i => i.month?.substring ? i.month.substring(5) : i.month) },
    yAxis: { type: 'value' },
    series: [{ type: 'line', data: list.map(i => i[keyName]), smooth: true, areaStyle: { opacity: 0.1 } }]
  })
}

function renderBarSimple(dom, name, dataMap) {
  if (!dom || !dataMap) return
  const chart = echarts.init(dom)
  const keys = Object.keys(dataMap)
  const vals = Object.values(dataMap).map(v => v || 0)
  chart.setOption({
    tooltip: { trigger: 'axis' },
    title: { text: name, left: 'center', textStyle: { fontSize: 13 } },
    grid: { left: 50, right: 20, top: 40, bottom: 40 },
    xAxis: { type: 'category', data: keys, axisLabel: { rotate: 20 } },
    yAxis: { type: 'value' },
    series: [{ type: 'bar', data: vals, barWidth: 24, itemStyle: { borderRadius: [4, 4, 0, 0] } }]
  })
}

function renderBarTrend(dom, name, list, key) {
  if (!dom || !list?.length) return
  const chart = echarts.init(dom)
  const keyName = key || 'count'
  chart.setOption({
    tooltip: { trigger: 'axis' },
    title: { text: name, left: 'center', textStyle: { fontSize: 13 } },
    grid: { left: 45, right: 20, top: 40, bottom: 20 },
    xAxis: { type: 'category', data: list.map(i => i.month?.substring ? i.month.substring(5) : i.month) },
    yAxis: { type: 'value' },
    series: [{ type: 'bar', data: list.map(i => i[keyName]), barWidth: 16, itemStyle: { borderRadius: [4, 4, 0, 0] } }]
  })
}

function mapToRate(list) {
  if (!list) return []
  return list.map(i => ({ month: i.month, count: i.rate || 0 }))
}

onMounted(async () => {
  const res = await getDashboardBuildings()
  buildings.value = res.data || []
  refreshAll()
})
</script>

<style scoped>
.dashboard-container { padding: 16px; background: #f5f7fa; min-height: 100vh }
.toolbar-card { margin-bottom: 16px }
.toolbar { display: flex; align-items: center; justify-content: space-between }
.toolbar .title { font-size: 20px; font-weight: 700; color: #303133 }
.toolbar .filters { display: flex; gap: 12px; align-items: center }
.panel-card { margin-bottom: 0 }
.panel-title { font-weight: 600; font-size: 15px }
.drill-chart { cursor: pointer }
.drill-chart:hover { opacity: 0.85 }
.alert-section { margin-bottom:16px }
</style>
