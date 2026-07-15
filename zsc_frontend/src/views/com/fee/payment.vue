<template>
  <div>
    <el-table v-loading="loading" :data="paymentList">
      <el-table-column label="账单编号" prop="billNo" width="160" />
      <el-table-column label="缴费项目" prop="itemName" width="120" />
      <el-table-column label="业主" prop="ownerName" width="100" />
      <el-table-column label="房屋" prop="roomNumber" width="100" />
      <el-table-column label="账单周期" prop="billPeriod" width="100" />
      <el-table-column label="缴费金额" prop="amount" width="100" />
      <el-table-column label="支付方式" prop="payMethod" width="100" />
      <el-table-column label="流水号" prop="transactionNo" width="160" />
      <el-table-column label="支付时间" prop="payTime" width="160"/>
      <el-table-column label="状态" prop="status" width="80">
        <template #default="scope"><el-tag :type="scope.row.status==='成功'?'success':'danger'">{{ scope.row.status }}</el-tag></template>
      </el-table-column>
    </el-table>
    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
  </div>
</template>
<script setup name="FeePayment">
import request from '@/utils/request'
const loading = ref(true); const paymentList = ref([]); const total = ref(0)
const queryParams = ref({ pageNum: 1, pageSize: 10 })
function getList() { loading.value = true; request.get('/com/fee/payment/list', { params: queryParams.value }).then(res => { paymentList.value = res.data || res.rows || []; total.value = res.total || 0; loading.value = false }).catch(() => { loading.value = false }) }
getList()
</script>
