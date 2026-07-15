<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="80px">
      <el-form-item label="活动标题" prop="title"><el-input v-model="queryParams.title" placeholder="请输入标题" clearable /></el-form-item>
      <el-form-item label="状态" prop="status"><el-select v-model="queryParams.status" placeholder="状态" clearable><el-option label="报名中" value="报名中" /><el-option label="进行中" value="进行中" /><el-option label="已结束" value="已结束" /></el-select></el-form-item>
      <el-form-item><el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button><el-button icon="Refresh" @click="resetQuery">重置</el-button></el-form-item>
    </el-form>
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5"><el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['com:activity:add']">新增活动</el-button></el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>
    <el-table v-loading="loading" :data="activityList">
      <el-table-column label="活动标题" prop="title" show-overflow-tooltip />
      <el-table-column label="活动时间" prop="activityTime" />
      <el-table-column label="地点" prop="location" />
      <el-table-column label="类型" prop="activityType" />
      <el-table-column label="报名人数" prop="actualParticipants">
        <template #default="scope">{{ scope.row.actualParticipants }}/{{ scope.row.maxParticipants }}</template>
      </el-table-column>
      <el-table-column label="状态" prop="status" />
      <el-table-column label="操作" align="center" width="180">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['com:activity:edit']">修改</el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['com:activity:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
  </div>
</template>
<script setup name="ActivityManagement">
import { listActivity } from '@/api/com/activity'
const { proxy } = getCurrentInstance()
const activityList = ref([]); const loading = ref(true); const showSearch = ref(true); const total = ref(0)
const queryParams = ref({ pageNum: 1, pageSize: 10, title: null, status: null })
function getList() { loading.value = true; listActivity(queryParams.value).then(res => { activityList.value = res.rows; total.value = res.total; loading.value = false }).catch(() => { loading.value = false }) }
function handleQuery() { queryParams.value.pageNum = 1; getList() }
function resetQuery() { queryRef.value?.resetFields(); handleQuery() }
function handleAdd() { /* TODO: 活动表单 */ }
function handleUpdate(row) { /* TODO */ }
function handleDelete(row) { proxy.$modal.confirm('确认删除?').then(() => { /* TODO */ getList() }) }
getList()
</script>
