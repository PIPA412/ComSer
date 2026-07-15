<template>
  <div>
    <el-table v-loading="loading" :data="paymentList" empty-text="暂无缴费记录">
      <el-table-column label="账单编号" prop="billId" width="100"/>
      <el-table-column label="缴费金额" prop="amount" width="100"/>
      <el-table-column label="支付方式" prop="payMethod" width="120"/>
      <el-table-column label="交易流水号" prop="transactionNo" width="200"/>
      <el-table-column label="支付时间" prop="payTime" width="160"/>
      <el-table-column label="状态" prop="status" width="80">
        <template #default="s"><el-tag :type="s.row.status==='成功'?'success':'danger'">{{ s.row.status }}</el-tag></template>
      </el-table-column>
    </el-table>
    <el-empty v-if="!loading && paymentList.length === 0" description="暂无缴费记录" />
    <pagination v-show="total>0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
  </div>
</template>
<script setup>
import request from '@/utils/request'
const { proxy } = getCurrentInstance()
const loading = ref(true); const paymentList = ref([]); const total = ref(0)
const queryParams = ref({ pageNum: 1, pageSize: 10 })
function getList() {
  loading.value = true
  request.get('/com/fee/my/payments', { params: queryParams.value }).then(res => {
    paymentList.value = res.rows
    total.value = res.total
    loading.value = false
  }).catch(() => { loading.value = false })
}
getList()
</script>
