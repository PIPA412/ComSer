<template>
  <div class="app-container">
    <!-- 统计概览卡片 -->
    <el-row :gutter="16" class="mb8">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-label">总人口数</div>
          <div class="stat-value">{{ stats.totalPopulation || 0 }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card stat-householder">
          <div class="stat-label">户主数</div>
          <div class="stat-value">{{ stats.householderCount || 0 }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card stat-family">
          <div class="stat-label">家属数</div>
          <div class="stat-value">{{ stats.familyCount || 0 }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card stat-tenant">
          <div class="stat-label">租客数</div>
          <div class="stat-value">{{ stats.tenantCount || 0 }}</div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" class="mb8">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-label">总门牌数</div>
          <div class="stat-value">{{ stats.totalRoomCount || 0 }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card stat-occupied">
          <div class="stat-label">已入住户数</div>
          <div class="stat-value">{{ stats.occupiedCount || 0 }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card stat-vacant">
          <div class="stat-label">空置户数</div>
          <div class="stat-value">{{ stats.vacantCount || 0 }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card stat-rate">
          <div class="stat-label">入住率</div>
          <div class="stat-value">{{ stats.occupancyRate || '0.0%' }}</div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 楼栋入住率排行 -->
    <el-card shadow="never">
      <template #header>
        <span>按楼栋入住率排行</span>
      </template>
      <el-table :data="occupancyRanking" v-loading="rankingLoading" default-sort="{ prop: 'rateValue', order: 'descending' }">
        <el-table-column type="index" label="排名" width="60" />
        <el-table-column label="楼栋名称" prop="buildingName" />
        <el-table-column label="总门牌数" prop="totalRooms" width="100" />
        <el-table-column label="已入住" prop="occupiedRooms" width="100" />
        <el-table-column label="空置" prop="vacantRooms" width="100" />
        <el-table-column label="入住率" prop="occupancyRate" width="100" sortable="'rateValue'">
          <template #default="scope">
            <el-progress :percentage="Math.round(scope.row.rateValue)" :color="rateColor(scope.row.rateValue)" />
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup name="PropertyStatistics">
import { getPropertyStatistics, getOccupancyRate } from '@/api/com/property'

const stats = ref({})
const occupancyRanking = ref([])
const rankingLoading = ref(false)

function rateColor(rate) {
  if (rate >= 80) return '#67c23a'
  if (rate >= 50) return '#e6a23c'
  return '#f56c6c'
}

function loadStats() {
  getPropertyStatistics().then(res => {
    stats.value = res.data || {}
  }).catch(() => {})
}
function loadRanking() {
  rankingLoading.value = true
  getOccupancyRate().then(res => {
    occupancyRanking.value = res.data || []
    rankingLoading.value = false
  }).catch(() => { rankingLoading.value = false })
}

loadStats()
loadRanking()
</script>

<style scoped>
.stat-card { text-align: center; cursor: default; }
.stat-label { font-size: 14px; color: #909399; margin-bottom: 8px; }
.stat-value { font-size: 32px; font-weight: bold; color: #303133; }
.stat-householder .stat-value { color: #67c23a; }
.stat-family .stat-value { color: #409eff; }
.stat-tenant .stat-value { color: #e6a23c; }
.stat-occupied .stat-value { color: #67c23a; }
.stat-vacant .stat-value { color: #909399; }
.stat-rate .stat-value { color: #e6a23c; }
.mb8 { margin-bottom: 16px; }
</style>
