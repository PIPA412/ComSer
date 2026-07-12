<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="80px">
      <el-form-item label="访客姓名" prop="visitorName"><el-input v-model="queryParams.visitorName" placeholder="请输入访客姓名" clearable /></el-form-item>
      <el-form-item label="状态" prop="status"><el-select v-model="queryParams.status" placeholder="状态" clearable><el-option label="待审批" value="待审批" /><el-option label="已通过" value="已通过" /><el-option label="已签离" value="已签离" /></el-select></el-form-item>
      <el-form-item><el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button><el-button icon="Refresh" @click="resetQuery">重置</el-button></el-form-item>
    </el-form>
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5"><el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['com:visitor:add']">新增访客</el-button></el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>
    <el-table v-loading="loading" :data="visitorList">
      <el-table-column label="访客姓名" prop="visitorName" />
      <el-table-column label="手机号" prop="visitorPhone" />
      <el-table-column label="来访事由" prop="reason" />
      <el-table-column label="预计时间" prop="expectedTime" />
      <el-table-column label="状态" prop="status"><template #default="scope"><el-tag :type="scope.row.status === '已通过' ? 'success' : scope.row.status === '已拒绝' ? 'danger' : 'warning'">{{ scope.row.status }}</el-tag></template></el-table-column>
      <el-table-column label="操作" align="center" width="200">
        <template #default="scope">
          <el-button v-if="scope.row.status === '待审批'" link type="primary" icon="Check" @click="handleApprove(scope.row)" v-hasPermi="['com:visitor:approve']">通过</el-button>
          <el-button v-if="scope.row.status === '已通过'" link type="success" icon="CircleCheck" @click="handleCheckout(scope.row)" v-hasPermi="['com:visitor:checkout']">签离</el-button>
        </template>
      </el-table-column>
    </el-table>
    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
  </div>
</template>
<script setup name="VisitorManagement">
import { listVisitor, approveVisitor, checkoutVisitor } from '@/api/com/visitor'
const { proxy } = getCurrentInstance()
const visitorList = ref([]); const loading = ref(true); const showSearch = ref(true); const total = ref(0)
const queryParams = ref({ pageNum: 1, pageSize: 10, visitorName: null, status: null })
function getList() { loading.value = true; listVisitor(queryParams.value).then(res => { visitorList.value = res.rows; total.value = res.total; loading.value = false }).catch(() => { loading.value = false }) }
function handleQuery() { queryParams.value.pageNum = 1; getList() }
function resetQuery() { queryRef.value?.resetFields(); handleQuery() }
function handleAdd() { /* TODO: 新增访客表单 */ }
function handleApprove(row) { approveVisitor(row.visitorId).then(() => { proxy.$modal.msgSuccess('已通过'); getList() }) }
function handleCheckout(row) { checkoutVisitor(row.visitorId).then(() => { proxy.$modal.msgSuccess('已签离'); getList() }) }
getList()
</script>
