<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="80px">
      <el-form-item label="维修类型" prop="repairType"><el-input v-model="queryParams.repairType" placeholder="请输入维修类型" clearable /></el-form-item>
      <el-form-item label="状态" prop="status"><el-select v-model="queryParams.status" placeholder="状态" clearable><el-option label="待受理" value="待受理" /><el-option label="处理中" value="处理中" /><el-option label="已完成" value="已完成" /></el-select></el-form-item>
      <el-form-item><el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button><el-button icon="Refresh" @click="resetQuery">重置</el-button></el-form-item>
    </el-form>
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5"><el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['com:repair:add']">新增</el-button></el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>
    <el-table v-loading="loading" :data="repairList">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="报修编号" prop="repairNo" />
      <el-table-column label="维修类型" prop="repairType" />
      <el-table-column label="紧急程度" prop="urgency" />
      <el-table-column label="状态" prop="status" />
      <el-table-column label="报修时间" prop="createTime" />
      <el-table-column label="操作" align="center" width="240">
        <template #default="scope">
          <el-button link type="primary" icon="View" @click="handleDetail(scope.row)">详情</el-button>
          <el-button v-if="scope.row.status === '待受理'" link type="primary" icon="Check" @click="handleAssign(scope.row)" v-hasPermi="['com:repair:assign']">派单</el-button>
          <el-button v-if="scope.row.status === '处理中'" link type="success" icon="CircleCheck" @click="handleFinish(scope.row)" v-hasPermi="['com:repair:finish']">完工</el-button>
        </template>
      </el-table-column>
    </el-table>
    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
  </div>
</template>
<script setup name="Repair">
import { listRepair, assignRepair, finishRepair } from '@/api/com/repair'
const { proxy } = getCurrentInstance()
const repairList = ref([]); const loading = ref(true); const showSearch = ref(true); const total = ref(0)
const queryParams = ref({ pageNum: 1, pageSize: 10, repairType: null, status: null })
function getList() { loading.value = true; listRepair(queryParams.value).then(res => { repairList.value = res.rows; total.value = res.total; loading.value = false }).catch(() => { loading.value = false }) }
function handleQuery() { queryParams.value.pageNum = 1; getList() }
function resetQuery() { queryRef.value?.resetFields(); handleQuery() }
function handleAdd() { /* TODO: 跳转报修表单页 */ }
function handleDetail(row) { /* TODO: 查看详情 */ }
function handleAssign(row) { proxy.$modal.confirm('确认派单?').then(() => assignRepair(row).then(() => { proxy.$modal.msgSuccess('派单成功'); getList() })) }
function handleFinish(row) { proxy.$modal.confirm('确认完工?').then(() => finishRepair(row).then(() => { proxy.$modal.msgSuccess('完工'); getList() })) }
getList()
</script>
