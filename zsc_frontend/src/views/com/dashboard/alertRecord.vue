<template>
  <div class="app-container">
    <el-card shadow="never">
      <template #header>
        <div style="display:flex;justify-content:space-between;align-items:center">
          <span class="card-title">预警记录</span>
          <div>
            <el-select v-model="queryParams.handleStatus" placeholder="处置状态" clearable style="width:140px;margin-right:8px" @change="getList">
              <el-option label="待处理" value="待处理" />
              <el-option label="已处理" value="已处理" />
              <el-option label="已忽略" value="已忽略" />
            </el-select>
            <el-button @click="getList">刷新</el-button>
          </div>
        </div>
      </template>

      <el-table :data="recordList" v-loading="loading" stripe>
        <el-table-column label="规则名称" prop="ruleName" min-width="130" />
        <el-table-column label="指标名称" prop="metricName" width="110" />
        <el-table-column label="触发值" prop="triggerValue" width="80">
          <template #default="s"><el-tag type="danger">{{ s.row.triggerValue }}</el-tag></template>
        </el-table-column>
        <el-table-column label="阈值" prop="threshold" width="80" />
        <el-table-column label="触发时间" prop="triggerTime" width="150" />
        <el-table-column label="处置状态" prop="handleStatus" width="90">
          <template #default="s">
            <el-tag :type="s.row.handleStatus === '待处理' ? 'danger' : s.row.handleStatus === '已处理' ? 'success' : 'info'">
              {{ s.row.handleStatus }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="处置人" prop="handleBy" width="100" />
        <el-table-column label="处置时间" prop="handleTime" width="150" />
        <el-table-column label="处置备注" prop="handleRemark" min-width="140" />
        <el-table-column label="操作" width="180" fixed="right" v-if="allowHandle">
          <template #default="s">
            <el-button v-if="s.row.handleStatus === '待处理'" link type="success" size="small" @click="handleMark(s.row, '已处理')">标记已处理</el-button>
            <el-button v-if="s.row.handleStatus === '待处理'" link type="info" size="small" @click="handleMark(s.row, '已忽略')">忽略</el-button>
          </template>
        </el-table-column>
      </el-table>

      <pagination v-if="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
    </el-card>
  </div>
</template>

<script setup name="AlertRecord">
import { ref, reactive, computed, onMounted, getCurrentInstance } from 'vue'
import { listAlertRecords, handleAlertRecord } from '@/api/com/dashboard'

const { proxy } = getCurrentInstance()
const loading = ref(false)
const recordList = ref([])
const total = ref(0)
const queryParams = reactive({ pageNum: 1, pageSize: 10, handleStatus: '' })

const allowHandle = computed(() => proxy && proxy.$auth && proxy.$auth.hasPermi('com:dashboard:alert'))

function getList() {
  loading.value = true
  const params = { pageNum: queryParams.pageNum, pageSize: queryParams.pageSize }
  if (queryParams.handleStatus) params.handleStatus = queryParams.handleStatus
  listAlertRecords(params).then(res => {
    recordList.value = res.rows || []
    total.value = res.total || 0
  }).finally(() => { loading.value = false })
}

function handleMark(row, status) {
  const msg = status === '已处理' ? '确认标记为已处理？' : '确认忽略该预警？'
  proxy.$modal.confirm(msg).then(() => {
    handleAlertRecord({ recordId: row.recordId, handleStatus: status }).then(() => {
      proxy.$modal.msgSuccess('已' + (status === '已处理' ? '处理' : '忽略'))
      getList()
    })
  }).catch(() => {})
}

onMounted(() => getList())
</script>
