<template>
  <div class="app-container">
    <el-row :gutter="20" style="margin-bottom:20px;">
      <el-col :span="6"><el-card><div style="text-align:center"><p style="color:#999;margin:0;">待缴金额</p><p style="font-size:24px;color:#e6a23c;font-weight:bold;margin:5px 0;">¥{{ stats.unpaid }}</p></div></el-card></el-col>
      <el-col :span="6"><el-card><div style="text-align:center"><p style="color:#999;margin:0;">本月已缴</p><p style="font-size:24px;color:#67c23a;font-weight:bold;margin:5px 0;">¥{{ stats.paid }}</p></div></el-card></el-col>
      <el-col :span="6"><el-card><div style="text-align:center"><p style="color:#999;margin:0;">欠费笔数</p><p style="font-size:24px;color:#f56c6c;font-weight:bold;margin:5px 0;">{{ stats.overdueCount }}</p></div></el-card></el-col>
      <el-col :span="6"><el-card><div style="text-align:center"><p style="color:#999;margin:0;">本月收缴率</p><p style="font-size:24px;color:#409eff;font-weight:bold;margin:5px 0;">{{ stats.rate }}%</p></div></el-card></el-col>
    </el-row>
    <el-card style="margin-bottom:15px;">
      <el-form :inline="true">
        <el-form-item label="状态"><el-select v-model="status" clearable style="width:120px"><el-option label="未缴" value="未缴"/><el-option label="已缴" value="已缴"/></el-select></el-form-item>
        <el-form-item><el-button type="primary" @click="loadData">查询</el-button></el-form-item>
      </el-form>
    </el-card>
    <el-card>
      <el-table v-loading="loading" :data="list">
        <el-table-column label="账单编号" prop="billNo" width="200"/>
        <el-table-column label="费用项目" prop="itemName" width="120"/>
        <el-table-column label="账单周期" prop="billPeriod" width="100"/>
        <el-table-column label="应收金额" prop="amount" width="100"/>
        <el-table-column label="截止日期" prop="dueDate" width="100"/>
        <el-table-column label="状态" width="80">
          <template #default="s"><el-tag :type="s.row.status==='已缴'?'success':'warning'">{{ s.row.status }}</el-tag></template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="s">
            <el-button link type="primary" @click="showDetail(s.row)">详情</el-button>
            <el-button link type="success" @click="handlePay(s.row)" v-if="s.row.status=='未缴'">缴费</el-button>
          </template>
        </el-table-column>
      </el-table>
      <pagination v-show="total>0" :total="total" v-model:page="pageNum" v-model:limit="pageSize" @pagination="loadData"/>
    </el-card>
    <!-- 详情弹窗 -->
    <el-dialog title="账单详情" v-model="detailOpen" width="500px">
      <el-descriptions :column="1" border>
        <el-descriptions-item label="账单编号">{{ detail.billNo }}</el-descriptions-item>
        <el-descriptions-item label="费用项目">{{ detail.itemName }}</el-descriptions-item>
        <el-descriptions-item label="应收金额">¥{{ detail.amount }}</el-descriptions-item>
        <el-descriptions-item label="实收金额">¥{{ detail.paidAmount || 0 }}</el-descriptions-item>
        <el-descriptions-item label="账单周期">{{ detail.billPeriod }}</el-descriptions-item>
        <el-descriptions-item label="截止日期">{{ detail.dueDate }}</el-descriptions-item>
        <el-descriptions-item label="缴费时间">{{ detail.payTime || '--' }}</el-descriptions-item>
        <el-descriptions-item label="支付方式">{{ detail.payMethod || '--' }}</el-descriptions-item>
        <el-descriptions-item label="状态"><el-tag :type="detail.status==='已缴'?'success':'warning'">{{ detail.status }}</el-tag></el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button type="primary" @click="handlePay(detail)" v-if="detail.status=='未缴'">去缴费</el-button>
        <el-button @click="detailOpen=false">关闭</el-button>
      </template>
    </el-dialog>
    <!-- 缴费弹窗 -->
    <el-dialog title="在线缴费" v-model="payOpen" width="420px">
      <div style="text-align:center;padding:20px;">
        <h3>待缴金额：<span style="color:#e6a23c;font-size:28px;">¥{{ payForm.amount }}</span></h3>
        <p style="color:#999;margin:10px 0;">账单编号：{{ payForm.billNo }}</p>
        <el-radio-group v-model="payForm.payMethod" style="margin:15px 0;">
          <el-radio-button value="微信支付">微信支付</el-radio-button>
          <el-radio-button value="支付宝">支付宝</el-radio-button>
        </el-radio-group>
        <div style="background:#f5f7fa;padding:20px;border-radius:8px;">
          <p style="color:#999;">请使用{{ payForm.payMethod }}扫码支付</p>
          <canvas ref="qrCanvas" width="180" height="180" style="display:block;margin:10px auto;background:#fff;border:1px solid #ddd;"></canvas>
          <p style="font-size:12px;color:#999;margin-top:5px;">（模拟二维码 - 演示用）</p>
        </div>
      </div>
      <template #footer>
        <el-button @click="payOpen=false">取消</el-button>
        <el-button type="success" @click="confirmPay" :loading="payLoading" size="large">我已支付</el-button>
      </template>
    </el-dialog>
  </div>
</template>
<script setup>
import request from '@/utils/request'
const { proxy } = getCurrentInstance()
const list = ref([]); const loading = ref(true); const total = ref(0)
const pageNum = ref(1); const pageSize = ref(10); const status = ref('')
const stats = ref({ unpaid: 0, paid: 0, overdueCount: 0, rate: 100 })
const detailOpen = ref(false); const detail = ref({})
const payOpen = ref(false); const payLoading = ref(false)
const payForm = ref({ billId: null, billNo: '', amount: 0, payMethod: '微信支付' })
const qrCanvas = ref(null)

function drawRealQR() {
  // 使用全局 QRCodeLib（本地 qrcode-generator 库，见 public/qrcode.js）
  const canvas = qrCanvas.value
  if (!canvas || !window.QRCodeLib) {
    console.warn('QRCodeLib 未加载')
    return
  }
  const content = `pay://comser?billNo=${payForm.value.billNo}&amount=${payForm.value.amount}&method=${payForm.value.payMethod === '微信支付' ? 'wechat' : 'alipay'}&ts=${Date.now()}`

  // QRCodeLib API: QRCodeLib(typeNumber, errorCorrectionLevel)
  const qr = window.QRCodeLib(0, 'M')
  qr.addData(content)
  qr.make()

  const cellSize = 4
  const margin = 4
  const moduleCount = qr.getModuleCount()
  const size = moduleCount * cellSize + margin * 2 * cellSize
  canvas.width = size
  canvas.height = size
  const ctx = canvas.getContext('2d')
  ctx.fillStyle = '#fff'
  ctx.fillRect(0, 0, size, size)
  ctx.fillStyle = '#000'
  for (let row = 0; row < moduleCount; row++) {
    for (let col = 0; col < moduleCount; col++) {
      if (qr.isDark(row, col)) {
        ctx.fillRect(
          margin * cellSize + col * cellSize,
          margin * cellSize + row * cellSize,
          cellSize, cellSize
        )
      }
    }
  }
}

function loadData() {
  loading.value = true; request.get('/com/fee/my/bills', { params: { pageNum: pageNum.value, pageSize: pageSize.value, status: status.value } }).then(res => {
    list.value = res.rows; total.value = res.total; loading.value = false
    let unpaid = 0, paid = 0, overdue = 0
    res.rows.forEach(b => { if (b.status === '未缴') { unpaid += b.amount; if (b.dueDate && new Date(b.dueDate) < new Date()) overdue++ } else if (b.status === '已缴') paid += b.amount })
    stats.value = { unpaid, paid, overdueCount: overdue, rate: total.value ? Math.round(paid / (unpaid + paid) * 100) : 100 }
  }).catch(() => { loading.value = false })
}
function showDetail(row) { detail.value = row; detailOpen.value = true }
function handlePay(row) {
  payForm.value = { billId: row.billId, billNo: row.billNo, amount: row.amount, payMethod: '微信支付' }
  payOpen.value = true
  nextTick(() => drawRealQR())
}
function confirmPay() {
  payLoading.value = true; request.put('/com/fee/pay/' + payForm.value.billId, { payMethod: payForm.value.payMethod, status: '成功' })
    .then(() => { proxy.$modal.msgSuccess('缴费成功！已生成电子收据'); payOpen.value = false; loadData(); payLoading.value = false }).catch(() => { payLoading.value = false })
}
loadData()
</script>
