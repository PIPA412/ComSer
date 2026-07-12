<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="80px">
      <el-form-item label="类型" prop="type"><el-select v-model="queryParams.type" placeholder="类型" clearable><el-option label="投诉" value="投诉" /><el-option label="建议" value="建议" /></el-select></el-form-item>
      <el-form-item label="状态" prop="status"><el-select v-model="queryParams.status" placeholder="状态" clearable><el-option label="待受理" value="待受理" /><el-option label="处理中" value="处理中" /><el-option label="已完成" value="已完成" /></el-select></el-form-item>
      <el-form-item><el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button><el-button icon="Refresh" @click="resetQuery">重置</el-button></el-form-item>
    </el-form>
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5"><el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['com:complaint:add']">新增</el-button></el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>
    <el-table v-loading="loading" :data="complaintList">
      <el-table-column label="类型" prop="type"><template #default="scope"><el-tag :type="scope.row.type === '投诉' ? 'danger' : 'info'">{{ scope.row.type }}</el-tag></template></el-table-column>
      <el-table-column label="标题" prop="title" show-overflow-tooltip />
      <el-table-column label="状态" prop="status" />
      <el-table-column label="提交时间" prop="createTime" />
      <el-table-column label="处理人" prop="handlerId" />
      <el-table-column label="操作" align="center" width="220">
        <template #default="scope">
          <el-button v-if="scope.row.status === '待受理'" link type="primary" icon="Check" @click="handleAccept(scope.row)" v-hasPermi="['com:complaint:accept']">受理</el-button>
          <el-button v-if="scope.row.status === '处理中'" link type="success" icon="CircleCheck" @click="handleFinish(scope.row)" v-hasPermi="['com:complaint:finish']">完成</el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['com:complaint:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
  </div>
</template>
<script setup name="ComplaintManagement">
import { listComplaint, acceptComplaint, finishComplaint } from '@/api/com/complaint'
const { proxy } = getCurrentInstance()
const complaintList = ref([]); const loading = ref(true); const showSearch = ref(true); const total = ref(0)
const queryParams = ref({ pageNum: 1, pageSize: 10, type: null, status: null })
function getList() { loading.value = true; listComplaint(queryParams.value).then(res => { complaintList.value = res.rows; total.value = res.total; loading.value = false }).catch(() => { loading.value = false }) }
function handleQuery() { queryParams.value.pageNum = 1; getList() }
function resetQuery() { queryRef.value?.resetFields(); handleQuery() }
function handleAdd() { /* TODO: 提交表单 */ }
function handleDelete(row) { proxy.$modal.confirm('确认删除?').then(() => { /* TODO */ getList() }) }
function handleAccept(row) { proxy.$modal.confirm('确认受理?').then(() => acceptComplaint(row).then(() => { proxy.$modal.msgSuccess('已受理'); getList() })) }
function handleFinish(row) { proxy.$modal.confirm('确认完成?').then(() => finishComplaint(row).then(() => { proxy.$modal.msgSuccess('已完成'); getList() })) }
getList()
</script>
