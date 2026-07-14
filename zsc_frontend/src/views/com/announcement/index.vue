<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="80px">
      <el-form-item label="标题" prop="title"><el-input v-model="queryParams.title" placeholder="请输入标题" clearable /></el-form-item>
      <el-form-item label="状态" prop="status"><el-select v-model="queryParams.status" placeholder="状态" clearable><el-option label="草稿" value="草稿" /><el-option label="已发布" value="已发布" /></el-select></el-form-item>
      <el-form-item><el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button><el-button icon="Refresh" @click="resetQuery">重置</el-button></el-form-item>
    </el-form>
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5"><el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['com:announcement:add']">新增公告</el-button></el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>
    <el-table v-loading="loading" :data="announcementList">
      <el-table-column label="标题" prop="title" show-overflow-tooltip />
      <el-table-column label="分类" prop="category" />
      <el-table-column label="状态" prop="status"><template #default="scope"><el-tag :type="scope.row.status === '已发布' ? 'success' : 'info'">{{ scope.row.status }}</el-tag></template></el-table-column>
      <el-table-column label="发布时间" prop="publishTime" />
      <el-table-column label="操作" align="center" width="220">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['com:announcement:edit']">修改</el-button>
          <el-button v-if="scope.row.status === '草稿'" link type="success" icon="Upload" @click="handlePublish(scope.row)" v-hasPermi="['com:announcement:publish']">发布</el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['com:announcement:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
  </div>
</template>
<script setup name="AnnouncementManagement">
import { listAnnouncement, publishAnnouncement } from '@/api/com/announcement'
const { proxy } = getCurrentInstance()
const announcementList = ref([]); const loading = ref(true); const showSearch = ref(true); const total = ref(0)
const queryParams = ref({ pageNum: 1, pageSize: 10, title: null, status: null })
function getList() { loading.value = true; listAnnouncement(queryParams.value).then(res => { announcementList.value = res.rows; total.value = res.total; loading.value = false }).catch(() => { loading.value = false }) }
function handleQuery() { queryParams.value.pageNum = 1; getList() }
function resetQuery() { queryRef.value?.resetFields(); handleQuery() }
function handleAdd() { /* TODO: 富文本编辑器表单 */ }
function handleUpdate(row) { /* TODO */ }
function handleDelete(row) { proxy.$modal.confirm('确认删除?').then(() => { /* TODO */ getList() }) }
function handlePublish(row) { publishAnnouncement(row.announcementId).then(() => { proxy.$modal.msgSuccess('发布成功'); getList() }) }
getList()
</script>
