<template>
  <div>
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5"><el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['com:fee:item:add']">新增</el-button></el-col>
    </el-row>
    <el-table v-loading="loading" :data="itemList">
      <el-table-column label="费用名称" prop="itemName" />
      <el-table-column label="计费方式" prop="chargeType" />
      <el-table-column label="单价(元)" prop="unitPrice" />
      <el-table-column label="计费周期" prop="billingCycle" />
      <el-table-column label="状态" prop="status"><template #default="scope"><dict-tag :options="sys_normal_disable" :value="scope.row.status" /></template></el-table-column>
      <el-table-column label="操作" align="center" width="180">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['com:fee:item:edit']">修改</el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['com:fee:item:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
  </div>
</template>
<script setup name="FeeItem">
import { listFeeItem } from '@/api/com/fee'
const { proxy } = getCurrentInstance()
const itemList = ref([]); const loading = ref(true); const total = ref(0)
const queryParams = ref({ pageNum: 1, pageSize: 10 })
function getList() { loading.value = true; listFeeItem(queryParams.value).then(res => { itemList.value = res.rows; total.value = res.total; loading.value = false }).catch(() => { loading.value = false }) }
function handleAdd() { /* TODO */ }
function handleUpdate(row) { /* TODO */ }
function handleDelete(row) { proxy.$modal.confirm('确认删除?').then(() => { /* TODO: delFeeItem */ getList(); proxy.$modal.msgSuccess('删除成功') }) }
getList()
</script>
