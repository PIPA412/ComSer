<template>
  <div class="app-container">
    <!-- 搜索栏 -->
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="80px">
      <el-form-item label="居民" prop="ownerId">
        <el-input v-model="queryParams.ownerId" placeholder="居民ID" clearable />
      </el-form-item>
      <el-form-item label="房屋" prop="roomId">
        <el-input v-model="queryParams.roomId" placeholder="房屋ID" clearable />
      </el-form-item>
      <el-form-item label="变更类型" prop="changeType">
        <el-select v-model="queryParams.changeType" placeholder="全部" clearable>
          <el-option label="登记入住" value="登记入住" />
          <el-option label="搬离" value="搬离" />
          <el-option label="身份变更" value="身份变更" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="logList" stripe>
      <el-table-column label="变更类型" prop="changeType" width="100">
        <template #default="scope">
          <el-tag :type="scope.row.changeType === '登记入住' ? 'success' : scope.row.changeType === '搬离' ? 'danger' : 'warning'">
            {{ scope.row.changeType }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="居民ID" prop="ownerId" width="80" />
      <el-table-column label="房屋ID" prop="roomId" width="80" />
      <el-table-column label="变更前" prop="beforeContent" show-overflow-tooltip />
      <el-table-column label="变更后" prop="afterContent" show-overflow-tooltip />
      <el-table-column label="操作人" prop="createBy" width="100" />
      <el-table-column label="操作时间" prop="createTime" width="170" />
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
  </div>
</template>

<script setup name="PropertyChangeLog">
import { listChangeLog } from '@/api/com/property'

const { proxy } = getCurrentInstance()
const queryRef = ref(null)
const logList = ref([])
const loading = ref(true)
const showSearch = ref(true)
const total = ref(0)
const queryParams = ref({ pageNum: 1, pageSize: 15, ownerId: null, roomId: null, changeType: null })

function getList() {
  loading.value = true
  listChangeLog(queryParams.value).then(res => {
    logList.value = res.rows || []
    total.value = res.total || 0
    loading.value = false
  }).catch(() => { loading.value = false })
}
function handleQuery() { queryParams.value.pageNum = 1; getList() }
function resetQuery() { queryRef.value?.resetFields(); handleQuery() }

getList()
</script>
