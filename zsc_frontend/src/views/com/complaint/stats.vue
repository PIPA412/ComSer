<template>
  <div class="app-container">
    <!-- 概览卡片 -->
    <el-row :gutter="16" class="mb20">
      <el-col :span="6">
        <el-statistic title="总工单" :value="data.totalCount || 0" />
      </el-col>
      <el-col :span="6">
        <el-statistic title="已完成" :value="data.doneCount || 0" />
      </el-col>
      <el-col :span="6">
        <el-statistic title="已评价" :value="data.ratedCount || 0" />
      </el-col>
      <el-col :span="6">
        <el-statistic title="评价率" :value="ratePercent" suffix="%" :precision="1" />
      </el-col>
    </el-row>

    <!-- 图表区 -->
    <el-row :gutter="16">
      <!-- 投诉类型分布 -->
      <el-col :span="12">
        <el-card shadow="never">
          <template #header><span class="card-title">投诉类型分布</span></template>
          <div ref="typeChart" style="height:300px" />
        </el-card>
      </el-col>
      <!-- 分类分布 -->
      <el-col :span="12">
        <el-card shadow="never">
          <template #header><span class="card-title">分类分布</span></template>
          <div ref="categoryChart" style="height:300px" />
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" class="mt20">
      <!-- 处理时长 -->
      <el-col :span="12">
        <el-card shadow="never">
          <template #header><span class="card-title">处理时长（小时）</span></template>
          <div ref="durationChart" style="height:280px" />
        </el-card>
      </el-col>
      <!-- 满意度趋势 -->
      <el-col :span="12">
        <el-card shadow="never">
          <template #header><span class="card-title">满意度趋势</span></template>
          <div ref="ratingChart" style="height:280px" />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup name="ComplaintStats">
import { ref, reactive, computed, onMounted, getCurrentInstance, nextTick } from 'vue'
import * as echarts from 'echarts'
import { complaintDashboard } from '@/api/com/complaint'

const { proxy } = getCurrentInstance()
const data = reactive({ totalCount: 0, doneCount: 0, ratedCount: 0 })

const ratePercent = computed(() => {
  if (!data.totalCount) return 0
  return (data.ratedCount / data.totalCount * 100)
})

const typeChart = ref(null)
const categoryChart = ref(null)
const durationChart = ref(null)
const ratingChart = ref(null)

onMounted(async () => {
  const res = await complaintDashboard()
  Object.assign(data, res.data)

  await nextTick()
  initTypeChart(res.data.typeDistribution)
  initCategoryChart(res.data.categoryDistribution)
  initDurationChart(res.data)
  initRatingChart(res.data.ratingTrend)
})

function initTypeChart(dist) {
  if (!typeChart.value) return
  const chart = echarts.init(typeChart.value)
  chart.setOption({
    tooltip: { trigger: 'item' },
    legend: { bottom: 0 },
    series: [{
      type: 'pie', radius: ['45%', '70%'], center: ['50%', '45%'],
      label: { show: true, formatter: '{b}\n{d}%' },
      data: Object.entries(dist || {}).map(([k, v]) => ({ name: k, value: v })),
      itemStyle: { borderRadius: 4 }
    }]
  })
}

function initCategoryChart(dist) {
  if (!categoryChart.value) return
  const chart = echarts.init(categoryChart.value)
  chart.setOption({
    tooltip: { trigger: 'item' },
    legend: { bottom: 0 },
    series: [{
      type: 'pie', radius: ['45%', '70%'], center: ['50%', '45%'],
      label: { show: true, formatter: '{b}\n{d}%' },
      data: Object.entries(dist || {}).map(([k, v]) => ({ name: k, value: v })),
      itemStyle: { borderRadius: 4 }
    }]
  })
}

function initDurationChart(d) {
  if (!durationChart.value) return
  const chart = echarts.init(durationChart.value)
  chart.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: 50, right: 30, top: 20, bottom: 30 },
    xAxis: { type: 'category', data: ['最短', '平均', '最长'] },
    yAxis: { type: 'value', name: '小时' },
    series: [{
      type: 'bar',
      data: [
        { value: d.minDurationHours || 0, itemStyle: { color: '#67c23a' } },
        { value: d.avgDurationHours || 0, itemStyle: { color: '#409eff' } },
        { value: d.maxDurationHours || 0, itemStyle: { color: '#f56c6c' } }
      ],
      barWidth: 60,
      label: { show: true, position: 'top', formatter: '{c}h' }
    }]
  })
}

function initRatingChart(trend) {
  if (!ratingChart.value || !trend) return
  const chart = echarts.init(ratingChart.value)
  const months = Object.keys(trend)
  const values = Object.values(trend)
  chart.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: 50, right: 30, top: 20, bottom: 30 },
    xAxis: { type: 'category', data: months },
    yAxis: { type: 'value', min: 1, max: 5, name: '平均分' },
    series: [{
      type: 'line',
      data: values.map(v => Math.round(v * 10) / 10),
      smooth: true,
      areaStyle: { opacity: 0.15 },
      itemStyle: { color: '#e6a23c' },
      lineStyle: { width: 3 }
    }]
  })
}
</script>

<style scoped>
.mb20 { margin-bottom: 20px }
.mt20 { margin-top: 20px }
.card-title { font-weight: 600; font-size: 15px }
</style>
