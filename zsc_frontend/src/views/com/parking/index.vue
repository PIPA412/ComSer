<template>
  <div class="app-container">
    <el-tabs v-model="activeTab">
      <!-- 车位管理 -->
      <el-tab-pane label="车位管理" name="spot">
        <el-row :gutter="10" class="mb8"><el-col :span="1.5"><el-button type="primary" plain icon="Plus" @click="handleAddSpot">新增车位</el-button></el-col></el-row>
        <el-table v-loading="loading1" :data="spotList">
          <el-table-column label="车位编号" prop="spotCode" width="100"/>
          <el-table-column label="位置" prop="location" width="180"/>
          <el-table-column label="类型" prop="spotType" width="80"/>
          <el-table-column label="月租费" prop="monthlyFee" width="100">
            <template #default="s">¥{{ s.row.monthlyFee || 0 }}</template>
          </el-table-column>
          <el-table-column label="小时计费" prop="hourlyFee" width="100">
            <template #default="s">¥{{ s.row.hourlyFee || 0 }}/小时</template>
          </el-table-column>
          <el-table-column label="状态" prop="status" width="80">
            <template #default="s"><el-tag :type="s.row.status==='空闲'?'success':'warning'">{{ s.row.status }}</el-tag></template>
          </el-table-column>
          <el-table-column label="操作" width="200">
            <template #default="s">
              <el-button link type="primary" icon="Edit" @click="handleUpdateSpot(s.row)">修改</el-button>
              <el-button link type="warning" icon="Refresh" @click="handleRenew(s.row)" v-if="s.row.status==='已占用'">续费</el-button>
              <el-button link type="danger" icon="Delete" @click="handleDeleteSpot(s.row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <!-- 车辆管理 -->
      <el-tab-pane label="车辆管理" name="vehicle">
        <el-row :gutter="10" class="mb8"><el-col :span="1.5"><el-button type="primary" plain icon="Plus" @click="handleAddVehicle">新增车辆</el-button></el-col></el-row>
        <el-table v-loading="loading2" :data="vehicleList">
          <el-table-column label="车牌号" prop="plateNumber" width="100"/>
          <el-table-column label="品牌" prop="brand" width="120"/>
          <el-table-column label="类型" prop="vehicleType" width="80"/>
          <el-table-column label="颜色" prop="color" width="80"/>
          <el-table-column label="车主" prop="ownerName" width="100"/>
          <el-table-column label="车主电话" prop="ownerPhone" width="120"/>
          <el-table-column label="所属房屋" prop="roomNumber" width="100"/>
          <el-table-column label="绑定车位" prop="spotCode" width="100"/>
          <el-table-column label="状态" width="80">
            <template #default="s"><el-tag :type="s.row.status==='0'?'success':'info'">{{ s.row.status==='0'?'正常':'待审核' }}</el-tag></template>
          </el-table-column>
          <el-table-column label="操作" width="200">
            <template #default="s">
              <el-button link type="success" icon="CircleCheck" @click="handleApproveVehicle(s.row)" v-if="s.row.status=='1'">审核</el-button>
              <el-button link type="primary" icon="Edit" @click="handleUpdateVehicle(s.row)">修改</el-button>
              <el-button link type="danger" icon="Delete" @click="handleDeleteVehicle(s.row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
        <pagination v-show="total2 > 0" :total="total2" v-model:page="vp.pageNum" v-model:limit="vp.pageSize" @pagination="getVehicleList" />
      </el-tab-pane>

      <!-- 停车记录 -->
      <el-tab-pane label="停车记录" name="record">
        <el-row :gutter="10" class="mb8"><el-col :span="1.5"><el-button type="primary" plain icon="Plus" @click="handleAddRecord">登记进场</el-button></el-col></el-row>
        <el-table v-loading="loading3" :data="recordList">
          <el-table-column label="车牌号" prop="plateNumber" width="100"/>
          <el-table-column label="车位编号" prop="spotCode" width="100"/>
          <el-table-column label="入场时间" prop="entryTime" width="160"/>
          <el-table-column label="出场时间" prop="exitTime" width="160"/>
          <el-table-column label="费用" prop="fee" width="80">
            <template #default="s">¥{{ s.row.fee || 0 }}</template>
          </el-table-column>
          <el-table-column label="支付状态" prop="payStatus" width="100">
            <template #default="s"><el-tag :type="s.row.payStatus==='已缴'?'success':'danger'">{{ s.row.payStatus||'未缴' }}</el-tag></template>
          </el-table-column>
          <el-table-column label="操作" width="120">
            <template #default="s">
              <el-button link type="success" icon="CircleCheck" @click="handleExit(s.row)" v-if="!s.row.exitTime">登记出场</el-button>
            </template>
          </el-table-column>
        </el-table>
        <pagination v-show="total3 > 0" :total="total3" v-model:page="rp.pageNum" v-model:limit="rp.pageSize" @pagination="getRecordList" />
      </el-tab-pane>

      <!-- 月卡审核 -->
      <el-tab-pane label="月卡审核" name="monthly">
        <el-table v-loading="mLoading" :data="monthlyList" empty-text="暂无月卡申请">
          <el-table-column label="业主" prop="ownerName" width="100"/>
          <el-table-column label="车牌号" prop="plateNumber" width="100"/>
          <el-table-column label="申请车位" prop="spotCode" width="80"/>
          <el-table-column label="月数" prop="months" width="60"/>
          <el-table-column label="金额" prop="amount" width="80"/>
          <el-table-column label="申请时间" prop="createTime" width="160"/>
          <el-table-column label="状态" prop="status" width="100">
            <template #default="s"><el-tag :type="s.row.status==='已通过'?'success':(s.row.status==='已拒绝'?'danger':'warning')">{{ s.row.status }}</el-tag></template>
          </el-table-column>
          <el-table-column label="操作" width="200">
            <template #default="s">
              <el-button link type="success" icon="CircleCheck" @click="handleApprove(s.row,true)" v-if="s.row.status=='待审核'">通过</el-button>
              <el-button link type="danger" icon="Close" @click="handleApprove(s.row,false)" v-if="s.row.status=='待审核'">拒绝</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
    </el-tabs>

    <!-- 弹窗：车位新增/编辑 -->
    <el-dialog :title="spotTitle" v-model="spotOpen" width="500px">
      <el-form ref="sf" :model="spotForm" label-width="100px">
        <el-form-item label="车位编号" prop="spotCode"><el-input v-model="spotForm.spotCode" placeholder="如: A-101" /></el-form-item>
        <el-form-item label="位置" prop="location"><el-input v-model="spotForm.location" placeholder="如: A栋地下1层" /></el-form-item>
        <el-form-item label="类型" prop="spotType">
          <el-select v-model="spotForm.spotType" style="width:100%">
            <el-option label="固定" value="固定" />
            <el-option label="临时" value="临时" />
          </el-select>
        </el-form-item>
        <el-form-item label="月租费" prop="monthlyFee"><el-input-number v-model="spotForm.monthlyFee" :precision="2" :min="0" /></el-form-item>
        <el-form-item label="小时计费" prop="hourlyFee">
          <el-input-number v-model="spotForm.hourlyFee" :precision="2" :min="0" :step="0.5" />
          <span style="margin-left:10px;color:#999;">元/小时（用于临时停车）</span>
        </el-form-item>
      </el-form>
      <template #footer><el-button @click="spotOpen=false">取消</el-button><el-button type="primary" @click="submitSpot">确定</el-button></template>
    </el-dialog>

    <!-- 弹窗：车辆新增/编辑 -->
    <el-dialog :title="vehicleTitle" v-model="vehicleOpen" width="500px">
      <el-form ref="vf" :model="vehicleForm" label-width="100px">
        <el-form-item label="车牌号"><el-input v-model="vehicleForm.plateNumber" placeholder="如: 粤A12345" /></el-form-item>
        <el-form-item label="品牌"><el-input v-model="vehicleForm.brand" placeholder="如: 丰田" /></el-form-item>
        <el-form-item label="类型">
          <el-select v-model="vehicleForm.vehicleType" style="width:100%">
            <el-option label="小型车" value="小型车" />
            <el-option label="大型车" value="大型车" />
            <el-option label="新能源" value="新能源" />
          </el-select>
        </el-form-item>
        <el-form-item label="颜色"><el-input v-model="vehicleForm.color" placeholder="如: 白色" /></el-form-item>
        <el-form-item label="车主">
          <el-select v-model="vehicleForm.ownerId" placeholder="选择车主" style="width:100%" filterable>
            <el-option v-for="o in ownerList" :key="o.ownerId" :label="`${o.ownerName}（${o.phone}）`" :value="o.ownerId" />
          </el-select>
        </el-form-item>
        <el-form-item label="绑定车位">
          <el-select v-model="vehicleForm.spotId" placeholder="选择车位（可空）" style="width:100%" clearable>
            <el-option v-for="s in spotList" :key="s.spotId" :label="s.spotCode" :value="s.spotId" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer><el-button @click="vehicleOpen=false">取消</el-button><el-button type="primary" @click="submitVehicle">确定</el-button></template>
    </el-dialog>

    <!-- 弹窗：登记进场（用车牌和车位下拉，更直观） -->
    <el-dialog title="登记进场" v-model="recordOpen" width="450px">
      <el-form :model="recordForm" label-width="100px">
        <el-form-item label="车牌号">
          <el-select v-model="recordForm.plateNumber" placeholder="选择车牌" style="width:100%" filterable>
            <el-option v-for="v in vehicleList" :key="v.vehicleId" :label="`${v.plateNumber}（${v.brand}）`" :value="v.plateNumber" />
          </el-select>
        </el-form-item>
        <el-form-item label="车位编号">
          <el-select v-model="recordForm.spotCode" placeholder="选择车位" style="width:100%" filterable>
            <el-option v-for="s in spotList" :key="s.spotId" :label="`${s.spotCode}（${s.location}）`" :value="s.spotCode" />
          </el-select>
        </el-form-item>
        <el-form-item label="入场时间">
          <input type="datetime-local" v-model="recordForm.entryTime" style="width:100%;height:32px;padding:0 12px;border:1px solid #dcdfe6;border-radius:4px;" />
        </el-form-item>
      </el-form>
      <template #footer><el-button @click="recordOpen=false">取消</el-button><el-button type="primary" @click="submitRecord" :disabled="!recordForm.plateNumber || !recordForm.spotCode">确定</el-button></template>
    </el-dialog>

    <!-- 弹窗：登记出场（管理员可手动设置出场时间） -->
    <el-dialog title="登记出场" v-model="exitOpen" width="450px">
      <el-descriptions :column="1" border size="small" style="margin-bottom:15px">
        <el-descriptions-item label="车牌号">{{ exitForm.plateNumber }}</el-descriptions-item>
        <el-descriptions-item label="车位编号">{{ exitForm.spotCode }}</el-descriptions-item>
        <el-descriptions-item label="入场时间">{{ exitForm.entryTime }}</el-descriptions-item>
        <el-descriptions-item label="小时计费">¥{{ exitForm.hourlyFee || 5 }}/小时</el-descriptions-item>
      </el-descriptions>
        <el-form :model="exitForm" label-width="100px">
        <el-form-item label="出场时间">
          <input type="datetime-local" v-model="exitForm.exitTime" style="width:100%;height:32px;padding:0 12px;border:1px solid #dcdfe6;border-radius:4px;" />
        </el-form-item>
      </el-form>
      <template #footer><el-button @click="exitOpen=false">取消</el-button><el-button type="primary" @click="submitExit">确定出场</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup name="ParkingManagement">
import { listSpot, addSpot, updateSpot, delSpot, getSpot } from '@/api/com/parking'
import { listVehicle, addVehicle, updateVehicle, delVehicle, getVehicle } from '@/api/com/parking'
import { listParkingRecord } from '@/api/com/parking'
import request from '@/utils/request'
import { useRoute } from 'vue-router'

const route = useRoute()
const activeTab = ref(route.query.tab || 'spot')

// 业主列表（用于车辆管理下拉）
const ownerList = ref([])
function getOwnerList() { request.get('/com/parking/owner/list', { params: { pageNum: 1, pageSize: 500 } }).then(r => { ownerList.value = r.rows || [] }).catch(() => {}) }

// 车位
const spotList = ref([]); const loading1 = ref(true); const spotOpen = ref(false); const spotTitle = ref(''); const spotForm = ref({ spotType: '固定', hourlyFee: 5 })
function getSpotList() {
  loading1.value = true
  listSpot({ pageNum: 1, pageSize: 500 }).then(r => {
    spotList.value = r.rows
    loading1.value = false
  }).catch(() => loading1.value = false)
}
function handleAddSpot() { spotForm.value = { spotType: '固定', hourlyFee: 5, monthlyFee: 200, status: '空闲' }; spotTitle.value = '新增车位'; spotOpen.value = true }
function handleUpdateSpot(r) { getSpot(r.spotId).then(res => { spotForm.value = res.data; spotTitle.value = '修改车位'; spotOpen.value = true }) }
function submitSpot() {
  (spotForm.value.spotId ? updateSpot(spotForm.value) : addSpot(spotForm.value)).then(() => {
    proxy.$modal.msgSuccess('操作成功'); spotOpen.value = false; getSpotList()
  })
}
function handleDeleteSpot(r) { proxy.$modal.confirm('确认删除？').then(() => delSpot(r.spotId).then(() => { getSpotList(); proxy.$modal.msgSuccess('删除成功') })) }
function handleRenew(r) {
  proxy.$prompt('续费月数', '月卡续费').then(({ value }) => {
    request.post('/com/parking/monthly/renew', { spotId: r.spotId, months: value }).then(() => proxy.$modal.msgSuccess('续费成功'))
  })
}

// 车辆
const vehicleList = ref([]); const loading2 = ref(true); const total2 = ref(0); const vehicleOpen = ref(false); const vehicleTitle = ref(''); const vehicleForm = ref({})
const vp = ref({ pageNum: 1, pageSize: 10 })
function getVehicleList() {
  loading2.value = true
  listVehicle(vp.value).then(r => {
    vehicleList.value = r.rows
    total2.value = r.total
    loading2.value = false
  }).catch(() => loading2.value = false)
}
function handleAddVehicle() { vehicleForm.value = { status: '0' }; vehicleTitle.value = '新增车辆'; vehicleOpen.value = true }
function handleUpdateVehicle(r) { getVehicle(r.vehicleId).then(res => { vehicleForm.value = res.data; vehicleTitle.value = '修改车辆'; vehicleOpen.value = true }) }
function submitVehicle() {
  (vehicleForm.value.vehicleId ? updateVehicle(vehicleForm.value) : addVehicle(vehicleForm.value)).then(() => {
    proxy.$modal.msgSuccess('操作成功'); vehicleOpen.value = false; getVehicleList()
  })
}
function handleDeleteVehicle(r) { proxy.$modal.confirm('确认删除？').then(() => delVehicle(r.vehicleId).then(() => { getVehicleList(); proxy.$modal.msgSuccess('删除成功') })) }
function handleApproveVehicle(r) {
  proxy.$modal.confirm('确认审核通过该车辆？车牌号：' + r.plateNumber).then(() => {
    updateVehicle({ vehicleId: r.vehicleId, status: '0' }).then(() => {
      proxy.$modal.msgSuccess('审核通过');
      getVehicleList();
    });
  });
}

// 月卡审核
const monthlyList = ref([]); const mLoading = ref(true)
function getMonthlyList() {
  mLoading.value = true
  request.get('/com/parking/monthly/list', { params: { pageNum: 1, pageSize: 100 } }).then(r => {
    monthlyList.value = r.rows || []
    mLoading.value = false
  }).catch(() => mLoading.value = false)
}
function handleApprove(row, approved) {
  const tip = approved ? '确认通过该月卡申请？' : '确认拒绝该月卡申请？'
  proxy.$modal.confirm(tip).then(() => {
    request.put('/com/parking/monthly/approve', { applyId: row.applyId, approved }).then(() => {
      proxy.$modal.msgSuccess(approved ? '已通过' : '已拒绝')
      getMonthlyList()
    })
  })
}

// 停车记录
const recordList = ref([]); const loading3 = ref(true); const total3 = ref(0); const recordOpen = ref(false); const recordForm = ref({ entryTime: null })
const exitOpen = ref(false); const exitForm = ref({})
const rp = ref({ pageNum: 1, pageSize: 10 })
function getRecordList() {
  loading3.value = true
  listParkingRecord(rp.value).then(r => {
    recordList.value = r.rows
    total3.value = r.total
    loading3.value = false
  }).catch(() => loading3.value = false)
}
function handleAddRecord() {
  // HTML5 datetime-local 用 yyyy-MM-ddTHH:mm 格式
  const now = new Date()
  const y = now.getFullYear()
  const m = String(now.getMonth() + 1).padStart(2, '0')
  const d = String(now.getDate()).padStart(2, '0')
  const hh = String(now.getHours()).padStart(2, '0')
  const mm = String(now.getMinutes()).padStart(2, '0')
  recordForm.value = { entryTime: `${y}-${m}-${d}T${hh}:${mm}` }
  recordOpen.value = true
}
function submitRecord() {
  // 把 HTML5 datetime-local 的 'T' 替换成空格，并补齐秒数
  let entryTime = recordForm.value.entryTime
  if (entryTime && entryTime.includes('T')) {
    entryTime = entryTime.replace('T', ' ') + ':00'
  }
  const data = { ...recordForm.value, entryTime }
  request.post('/com/parking/record', data).then(() => {
    proxy.$modal.msgSuccess('登记成功')
    recordOpen.value = false
    getRecordList()
  })
}
function handleExit(r) {
  // 计算默认出场时间：5小时之后，用 HTML5 datetime-local 格式
  const now = new Date(Date.now() + 5 * 60 * 60 * 1000)
  const y = now.getFullYear()
  const m = String(now.getMonth() + 1).padStart(2, '0')
  const d = String(now.getDate()).padStart(2, '0')
  const hh = String(now.getHours()).padStart(2, '0')
  const mm = String(now.getMinutes()).padStart(2, '0')
  const exitTime = `${y}-${m}-${d}T${hh}:${mm}`
  // 查询车位的小时计费
  const spot = spotList.value.find(s => s.spotId === r.spotId)
  const hourlyFee = spot?.hourlyFee || 5
  exitForm.value = { ...r, exitTime, hourlyFee }
  exitOpen.value = true
}
function submitExit() {
  // 把 HTML5 datetime-local 的 'T' 替换成空格，并补齐秒数
  let exitTime = exitForm.value.exitTime
  if (exitTime && exitTime.includes('T')) {
    exitTime = exitTime.replace('T', ' ') + ':00'
  }
  request.put('/com/parking/record/exit', { recordId: exitForm.value.recordId, exitTime }).then(() => {
    proxy.$modal.msgSuccess('出场登记成功，费用已计算')
    exitOpen.value = false
    getRecordList()
  })
}

const { proxy } = getCurrentInstance()
getOwnerList()
getSpotList(); getVehicleList(); getRecordList(); getMonthlyList()
</script>
