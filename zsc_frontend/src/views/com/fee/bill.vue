<template>
  <div>
    <el-form :model="queryParams" :inline="true">
      <el-form-item label="账单周期"><el-input v-model="queryParams.billPeriod" placeholder="例: 2026-07" style="width:140px"/></el-form-item>
      <el-form-item label="状态">
        <el-select v-model="queryParams.status" clearable style="width:120px">
          <el-option label="未缴" value="未缴" /><el-option label="已缴" value="已缴" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="getList">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5"><el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['com:fee:bill:add']">新增账单</el-button></el-col>
      <el-col :span="1.5"><el-button type="success" plain icon="Collection" @click="handleBatch" v-hasPermi="['com:fee:bill:batch']">批量生成</el-button></el-col>
    </el-row>
    <el-table v-loading="loading" :data="billList">
      <el-table-column label="账单编号" prop="billNo" width="220"/>
      <el-table-column label="房间号" prop="roomNumber" width="80"/>
      <el-table-column label="业主" prop="ownerName" width="80"/>
      <el-table-column label="费用项目" prop="itemName" width="120"/>
      <el-table-column label="账单周期" prop="billPeriod" width="100"/>
      <el-table-column label="应收金额" prop="amount" width="100"/>
      <el-table-column label="实收金额" prop="paidAmount" width="100"/>
      <el-table-column label="截止日期" prop="dueDate" width="100"/>
      <el-table-column label="状态" prop="status" width="80">
        <template #default="s"><el-tag :type="s.row.status==='已缴'?'success':(s.row.status==='逾期'?'danger':'warning')">{{ s.row.status }}</el-tag></template>
      </el-table-column>
      <el-table-column label="支付方式" prop="payMethod" width="100"/>
      <el-table-column label="操作" width="220">
        <template #default="s">
          <el-button link type="success" icon="CircleCheck" @click="handlePay(s.row)" v-if="s.row.status=='未缴'" v-hasPermi="['com:fee:pay:mock']">缴费</el-button>
          <el-button link type="warning" icon="Message" @click="handleRemind(s.row)" v-if="s.row.status=='未缴'" v-hasPermi="['com:fee:remind']">催缴</el-button>
          <el-button link type="danger" icon="Delete" @click="handleDelete(s.row)" v-hasPermi="['com:fee:bill:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <!-- 新增弹窗 -->
    <el-dialog title="新增账单" v-model="addOpen" width="500px">
      <el-form :model="addForm" label-width="100px">
        <el-form-item label="房屋">
          <el-select v-model="addForm.roomId" placeholder="请选择房屋" style="width:100%" filterable>
            <el-option v-for="r in roomList" :key="r.roomId" :label="`${r.roomNumber}（${r.area}㎡）`" :value="r.roomId" />
          </el-select>
        </el-form-item>
        <el-form-item label="费用项目">
          <el-select v-model="addForm.itemId" placeholder="请选择费用项目" style="width:100%" filterable @change="onItemChange">
            <el-option v-for="it in itemList" :key="it.itemId" :label="`${it.itemName}（${it.unitPrice}元/${it.billingCycle}）`" :value="it.itemId" />
          </el-select>
        </el-form-item>
        <el-form-item label="应收金额"><el-input-number v-model="addForm.amount" :precision="2" /></el-form-item>
        <el-form-item label="账单周期"><el-input v-model="addForm.billPeriod" placeholder="例: 2026-07" /></el-form-item>
        <el-form-item label="截止日期">
          <input type="date" v-model="addForm.dueDate" style="width:100%;height:32px;padding:0 12px;border:1px solid #dcdfe6;border-radius:4px;" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addOpen=false">取消</el-button>
        <el-button type="primary" @click="submitAdd" :disabled="!addForm.roomId || !addForm.itemId">确定</el-button>
      </template>
    </el-dialog>

    <!-- 模拟缴费弹窗 -->
    <el-dialog title="模拟缴费" v-model="payOpen" width="420px">
      <div style="text-align:center;padding:20px;">
        <h3>待缴金额：<span style="color:#e6a23c;font-size:24px;">¥{{ payForm.amount }}</span></h3>
        <el-radio-group v-model="payForm.payMethod" style="margin:20px 0;">
          <el-radio-button value="微信支付">微信支付</el-radio-button>
          <el-radio-button value="支付宝">支付宝</el-radio-button>
        </el-radio-group>
        <div style="background:#f5f7fa;padding:20px;border-radius:8px;margin:10px 0;">
          <p style="color:#999;">模拟支付二维码（演示）</p>
          <div style="width:150px;height:150px;background:#fff;margin:10px auto;border:1px solid #ddd;display:flex;align-items:center;justify-content:center;font-size:12px;color:#ccc;">二维码展示区域</div>
        </div>
      </div>
      <template #footer>
        <el-button @click="payOpen=false">取消</el-button>
        <el-button type="success" @click="confirmPay" :loading="payLoading">我已支付</el-button>
      </template>
    </el-dialog>
    <!-- 批量生成弹窗 -->
    <el-dialog title="批量生成账单" v-model="batchOpen" width="500px">
      <el-form :model="batchForm" label-width="100px">
        <el-form-item label="账单周期"><el-input v-model="batchForm.billPeriod" placeholder="例: 2026-07" /></el-form-item>
        <el-form-item label="费用项目">
          <el-select v-model="batchForm.itemId" placeholder="请选择费用项目" style="width:100%" filterable @change="onBatchItemChange">
            <el-option v-for="it in itemList" :key="it.itemId" :label="`${it.itemName}（${it.unitPrice}元/${it.billingCycle}）`" :value="it.itemId" />
          </el-select>
        </el-form-item>
        <el-form-item label="目标房屋">
          <el-select v-model="batchForm.roomIds" placeholder="选择房屋（可多选）" style="width:100%" multiple filterable>
            <el-option v-for="r in roomList" :key="r.roomId" :label="`${r.roomNumber}（${r.area}㎡）`" :value="r.roomId" />
          </el-select>
          <div style="font-size:12px;color:#999;margin-top:5px;">不选则给所有已入住房屋生成</div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="batchOpen=false">取消</el-button>
        <el-button type="primary" @click="submitBatch" :disabled="!batchForm.itemId || !batchForm.billPeriod">开始生成</el-button>
      </template>
    </el-dialog>
  </div>
</template>
<script setup name="FeeBill">
import { listFeeBill, addFeeBill, delFeeBill } from '@/api/com/fee'
import { listFeeItem } from '@/api/com/fee'
import request from '@/utils/request'
const { proxy } = getCurrentInstance()
const billList = ref([]); const loading = ref(true); const total = ref(0)
const queryParams = ref({ pageNum: 1, pageSize: 10, billPeriod: '', status: '' })
const addOpen = ref(false); const addForm = ref({})
const payOpen = ref(false); const payLoading = ref(false)
const payForm = ref({ billId: null, amount: 0, payMethod: '微信支付' })
const batchOpen = ref(false); const batchForm = ref({ billPeriod: '', itemId: null, roomIds: [] }); const itemList = ref([]); const roomList = ref([])

function getRoomList() { request.get('/com/fee/room/list', { params: { pageNum: 1, pageSize: 500 } }).then(r => { roomList.value = r.rows || [] }).catch(() => {}) }
function getItemList() { listFeeItem({ pageNum: 1, pageSize: 100 }).then(r => { itemList.value = r.rows || [] }).catch(() => {}) }
function onItemChange(itemId) {
  // 选择费用项目后自动填充应收金额
  const item = itemList.value.find(i => i.itemId === itemId)
  if (item && item.unitPrice) addForm.value.amount = item.unitPrice
}
function onBatchItemChange(itemId) {
  // 批量生成不自动填充
}
function getList() { loading.value = true; listFeeBill(queryParams.value).then(res => { billList.value = res.rows; total.value = res.total; loading.value = false }).catch(() => { loading.value = false }) }
function resetQuery() { queryParams.value = { pageNum: 1, pageSize: 10, billPeriod: '', status: '' }; getList() }
function handleAdd() { addForm.value = {}; getRoomList(); getItemList(); addOpen.value = true }
function submitAdd() { addFeeBill(addForm.value).then(() => { proxy.$modal.msgSuccess('新增成功'); addOpen.value = false; getList() }) }
function handlePay(row) { payForm.value = { billId: row.billId, amount: row.amount, payMethod: '微信支付' }; payOpen.value = true }
function confirmPay() { payLoading.value = true; request.put('/com/fee/pay/' + payForm.value.billId, { payMethod: payForm.value.payMethod, status: '成功' }).then(() => { proxy.$modal.msgSuccess('缴费成功！'); payOpen.value = false; getList(); payLoading.value = false }).catch(() => { payLoading.value = false }) }
function handleRemind(row) { proxy.$modal.confirm('确认发送催缴邮件？').then(() => request.post('/com/fee/remind/' + row.billId).then(() => proxy.$modal.msgSuccess('催缴邮件已发送'))) }
function handleDelete(row) { proxy.$modal.confirm('确认删除？').then(() => delFeeBill(row.billId).then(() => { getList(); proxy.$modal.msgSuccess('删除成功') })) }
function handleBatch() { batchForm.value = { billPeriod: new Date().toISOString().slice(0,7), itemId: null, roomIds: [] }; getRoomList(); getItemList(); batchOpen.value = true }
function submitBatch() { const roomIds = batchForm.value.roomIds && batchForm.value.roomIds.length > 0 ? batchForm.value.roomIds : []; request.post('/com/fee/bill/batch', { itemId: batchForm.value.itemId, billPeriod: batchForm.value.billPeriod, roomIds }).then(r => { proxy.$modal.msgSuccess(r.msg || '生成成功'); batchOpen.value = false; getList() }).catch(() => {}) }
getList(); getRoomList(); getItemList()
</script>
