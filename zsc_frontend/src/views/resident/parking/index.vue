<template>
  <div class="app-container">
    <el-tabs v-model="activeTab">
      <el-tab-pane label="我的车辆" name="vehicle">
        <el-button type="primary" plain icon="Plus" @click="vehicleOpen=true">新增车辆</el-button>
        <el-table v-loading="vLoading" :data="vehicleList" style="margin-top:10px">
          <el-table-column label="车牌号" prop="plateNumber"/><el-table-column label="品牌" prop="brand"/>
          <el-table-column label="类型" prop="vehicleType"/><el-table-column label="颜色" prop="color"/>
          <el-table-column label="状态" width="80"><template #default="s"><el-tag v-if="s.row.status=='0'" type="success">正常</el-tag><el-tag v-else type="info">待审核</el-tag></template></el-table-column>
          <el-table-column label="操作" width="150"><template #default="s"><el-button link type="danger" @click="deleteVehicle(s.row)">删除</el-button></template></el-table-column>
        </el-table>
      </el-tab-pane>
      <el-tab-pane label="月卡管理" name="monthly">
        <div style="margin-bottom:15px;">
          <el-row :gutter="20">
            <el-col :span="12">
              <el-button type="primary" plain @click="cardOpen=true">申请月卡</el-button>
              <h4 style="margin-top:10px;">可用固定车位</h4>
              <el-table v-loading="cLoading" :data="spotList" size="small">
                <el-table-column label="车位编号" prop="spotCode"/><el-table-column label="月租费" prop="monthlyFee"/>
                <el-table-column label="操作" width="80"><template #default="s"><el-button link type="success" @click="applyCard(s.row)" v-if="s.row.status=='空闲'">申请</el-button></template></el-table-column>
              </el-table>
            </el-col>
            <el-col :span="12">
              <h4 style="margin-top:0;">我的月卡</h4>
              <el-table v-loading="mcLoading" :data="myCards" size="small" empty-text="暂无月卡">
                <el-table-column label="车牌" prop="plateNumber" width="100"/>
                <el-table-column label="绑定车位" prop="spotCode" width="80"/>
                <el-table-column label="到期日" prop="expireDate" width="100"/>
                <el-table-column label="操作" width="80">
                  <template #default="s">
                    <el-button link type="warning" @click="handleRenew(s.row)">续费</el-button>
                  </template>
                </el-table-column>
              </el-table>
            </el-col>
          </el-row>
        </div>
        <el-divider />
        <h4>我的申请记录</h4>
        <el-table v-loading="aLoading" :data="applyList" empty-text="暂无申请记录" size="small">
          <el-table-column label="车牌" prop="plateNumber" width="100"/>
          <el-table-column label="车位" prop="spotCode" width="80"/>
          <el-table-column label="金额" prop="amount" width="80"/>
          <el-table-column label="月数" prop="months" width="60"/>
          <el-table-column label="类型" width="60">
            <template #default="s"><el-tag size="small">{{ s.row.remark === '续费' ? '续费' : '新办' }}</el-tag></template>
          </el-table-column>
          <el-table-column label="申请时间" prop="createTime" width="140"/>
          <el-table-column label="状态" width="80">
            <template #default="s"><el-tag :type="s.row.status==='已通过'?'success':(s.row.status==='已拒绝'?'danger':'warning')" size="small">{{ s.row.status }}</el-tag></template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
      <el-tab-pane label="停车记录" name="record">
        <el-table v-loading="rLoading" :data="recordList" style="margin-top:10px">
          <el-table-column label="车辆ID" prop="vehicleId" width="80"/><el-table-column label="入场时间" prop="entryTime" width="160"/>
          <el-table-column label="出场时间" prop="exitTime" width="160"/><el-table-column label="费用" prop="fee" width="80"/>
          <el-table-column label="状态" width="80"><template #default="s"><el-tag :type="s.row.payStatus==='已缴'?'success':'danger'">{{ s.row.payStatus||'未缴' }}</el-tag></template></el-table-column>
        </el-table>
      </el-tab-pane>
    </el-tabs>
    <!-- 新增车辆弹窗 -->
    <el-dialog title="新增车辆" v-model="vehicleOpen" width="400px">
      <el-form :model="vf" label-width="100px">
        <el-form-item label="车牌号"><el-input v-model="vf.plateNumber" placeholder="如: 粤A12345"/></el-form-item>
        <el-form-item label="品牌"><el-input v-model="vf.brand" placeholder="如: 丰田"/></el-form-item>
        <el-form-item label="颜色"><el-input v-model="vf.color" placeholder="如: 白色"/></el-form-item>
        <el-form-item label="类型"><el-select v-model="vf.vehicleType"><el-option label="小型车" value="小型车"/><el-option label="大型车" value="大型车"/><el-option label="新能源" value="新能源"/></el-select></el-form-item>
      </el-form>
      <template #footer><el-button @click="vehicleOpen=false">取消</el-button><el-button type="primary" @click="submitVehicle">提交</el-button></template>
    </el-dialog>
    <!-- 申请月卡弹窗 -->
    <el-dialog title="申请月卡" v-model="cardOpen" width="450px">
      <el-form :model="cardForm" label-width="100px">
        <el-form-item label="选择车辆">
          <el-select v-model="cardForm.vehicleId" placeholder="请选择车辆" style="width:100%" filterable>
            <el-option v-for="v in availableVehicles" :key="v.vehicleId" :label="`${v.plateNumber}（${v.brand}）`" :value="v.vehicleId" />
          </el-select>
        </el-form-item>
        <el-form-item label="选择车位">
          <el-select v-model="cardForm.spotId" placeholder="请选择固定车位" style="width:100%" filterable>
            <el-option v-for="s in spotList" :key="s.spotId" :label="`${s.spotCode}（${s.location}）`" :value="s.spotId" />
          </el-select>
        </el-form-item>
        <el-form-item label="月数">
          <el-input-number v-model="cardForm.months" :min="1" :max="12" />
        </el-form-item>
        <p v-if="cardForm.spotId" style="color:#e6a23c;margin:5px 0;">预计费用：¥{{ selectedSpotFee * cardForm.months }}</p>
      </el-form>
      <template #footer>
        <el-button @click="cardOpen=false">取消</el-button>
        <el-button type="primary" @click="confirmApplyCard" :disabled="!cardForm.vehicleId || !cardForm.spotId">提交申请</el-button>
      </template>
    </el-dialog>
    <!-- 续费弹窗 -->
    <el-dialog title="月卡续费" v-model="renewOpen" width="400px">
      <div v-if="renewForm.spotCode" style="padding:10px 0;">
        <p>续费车辆：<strong>{{ renewForm.plateNumber }}</strong></p>
        <p>绑定车位：<strong>{{ renewForm.spotCode }}</strong></p>
        <p>月租费率：¥<strong>{{ renewForm.monthlyFee }}</strong>/月</p>
        <el-form :model="renewForm" label-width="80px">
          <el-form-item label="续费月数">
            <el-input-number v-model="renewForm.months" :min="1" :max="12" />
          </el-form-item>
        </el-form>
        <p style="color:#e6a23c;">预计费用：¥{{ renewForm.monthlyFee * renewForm.months }}</p>
      </div>
      <template #footer>
        <el-button @click="renewOpen=false">取消</el-button>
        <el-button type="primary" @click="confirmRenew" :disabled="!renewForm.months">提交续费</el-button>
      </template>
    </el-dialog>
  </div>
</template>
<script setup>
import { addVehicle, delVehicle, listSpot } from '@/api/com/parking'
import request from '@/utils/request'
const { proxy } = getCurrentInstance()
const activeTab = ref('vehicle')
const vehicleList=ref([]); const vLoading=ref(true); const vehicleOpen=ref(false); const vf=ref({vehicleType:'小型车'})
function getVehicles() { vLoading.value=true; request.get('/com/parking/my/vehicles',{params:{pageNum:1,pageSize:100}}).then(r=>{ vehicleList.value=r.rows; vLoading.value=false }).catch(()=>vLoading.value=false) }
function submitVehicle() {
  // 后端会自动从当前登录用户获取 ownerId
  vf.value.status = '1';  // 默认为待审核状态
  addVehicle(vf.value).then(() => {
    proxy.$modal.msgSuccess('提交成功，等待审核');
    vehicleOpen.value = false;
    getVehicles();
  });
}
function deleteVehicle(r) { proxy.$modal.confirm('确认删除？').then(()=>delVehicle(r.vehicleId).then(()=>{ getVehicles(); proxy.$modal.msgSuccess('已删除') })) }
const spotList=ref([]); const cLoading=ref(true); const cardOpen=ref(false)
const cardForm = ref({ spotId: null, vehicleId: null, months: 1 })
const applyList=ref([]); const aLoading=ref(true)
const availableVehicles = computed(() => vehicleList.value.filter(v => !v.spotId && v.status === '0'))  // 只显示未绑定车位的车辆
const selectedSpotFee = computed(() => {
  const s = spotList.value.find(s => s.spotId === cardForm.value.spotId)
  return s ? s.monthlyFee || 0 : 0
})
function getSpots() { cLoading.value=true; listSpot({pageNum:1,pageSize:100}).then(r=>{ spotList.value=(r.rows||[]).filter(s=>s.spotType=='固定'); cLoading.value=false }).catch(()=>cLoading.value=false) }
function applyCard(r) {
  // 打开申请月卡弹窗，预选车位
  cardForm.value = { spotId: r.spotId, vehicleId: null, months: 1 }
  cardOpen.value = true
}
function confirmApplyCard() {
  request.post('/com/parking/monthly/apply', { spotId: cardForm.value.spotId, vehicleId: cardForm.value.vehicleId, months: cardForm.value.months }).then(() => {
    proxy.$modal.msgSuccess('申请已提交，等待审批')
    cardOpen.value = false
    getApplyList()
  })
}
function getApplyList() { aLoading.value=true; request.get('/com/parking/monthly/my',{params:{pageNum:1,pageSize:100}}).then(r=>{applyList.value=r.rows;aLoading.value=false}).catch(()=>aLoading.value=false) }

// 我的月卡（已绑定车位的车辆）
const myCards = ref([]); const mcLoading = ref(false)
const renewOpen = ref(false); const renewForm = ref({})
function getMyCards() {
  mcLoading.value = true
  request.get('/com/parking/monthly/my/cards').then(r => {
    myCards.value = r.data || []
    mcLoading.value = false
  }).catch(() => mcLoading.value = false)
}
function handleRenew(row) {
  renewForm.value = { vehicleId: row.vehicleId, plateNumber: row.plateNumber, spotCode: row.spotCode, monthlyFee: row.monthlyFee || 0, months: 1 }
  renewOpen.value = true
}
function confirmRenew() {
  request.post('/com/parking/monthly/renew', { vehicleId: renewForm.value.vehicleId, months: renewForm.value.months }).then(() => {
    proxy.$modal.msgSuccess('续费申请已提交，等待审核')
    renewOpen.value = false
    getMyCards()
    getApplyList()
  })
}

const recordList=ref([]); const rLoading=ref(true)
function getRecords() { rLoading.value=true; request.get('/com/parking/my/records',{params:{pageNum:1,pageSize:100}}).then(r=>{ recordList.value=r.rows; rLoading.value=false }).catch(()=>rLoading.value=false) }
getVehicles(); getSpots(); getApplyList(); getRecords(); getMyCards()
</script>
