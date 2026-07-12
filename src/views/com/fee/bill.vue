<template>
  <div>
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5"><el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['com:fee:bill:add']">生成账单</el-button></el-col>
    </el-row>
    <el-table v-loading="loading" :data="billList">
      <el-table-column label="账单编号" prop="billNo" />
      <el-table-column label="账单周期" prop="billPeriod" />
      <el-table-column label="应收金额" prop="amount" />
      <el-table-column label="实收金额" prop="paidAmount" />
      <el-table-column label="状态" prop="status"><template #default="scope"><el-tag :type="scope.row.status === '已缴' ? 'success' : scope.row.status === '逾期' ? 'danger' : 'warning'">{{ scope.row.status }}</el-tag></template></el-table-column>
    </el-table>
    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
  </div>
</template>
<script setup name="FeeBill">
import { listFeeBill } from '@/api/com/fee'
const billList = ref([]); const loading = ref(true); const total = ref(0)
const queryParams = ref({ pageNum: 1, pageSize: 10 })
function getList() { loading.value = true; listFeeBill(queryParams.value).then(res => { billList.value = res.rows; total.value = res.total; loading.value = false }).catch(() => { loading.value = false }) }
function handleAdd() { /* TODO: 生成账单 */ }
getList()
</script>
