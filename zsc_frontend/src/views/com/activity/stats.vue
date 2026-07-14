<template>
  <div class="app-container">
    <el-row :gutter="16" class="mb20">
      <el-col :span="6"><el-statistic title="活动总数" :value="stats.total" /></el-col>
      <el-col :span="6"><el-statistic title="报名中" :value="stats.publishing" /></el-col>
      <el-col :span="6"><el-statistic title="已结束" :value="stats.finished" /></el-col>
      <el-col :span="6"><el-statistic title="签到率" :value="attendRate" suffix="%" :precision="1" /></el-col>
    </el-row>

    <el-row :gutter="16">
      <el-col :span="12">
        <el-card shadow="never">
          <template #header><span class="card-title">活动类型分布</span></template>
          <div ref="typeChart" style="height:300px" />
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="never">
          <template #header><span class="card-title">报名 vs 实到</span></template>
          <div ref="attendChart" style="height:300px" />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup name="ActivityStats">
import { ref, reactive, computed, onMounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import { activityStats } from '@/api/com/activity'

const typeChart = ref(null), attendChart = ref(null)
const stats = reactive({ total: 0, publishing: 0, finished: 0 })
const attendRate = computed(() => {
  if (!stats.totalSignups) return 0
  return (stats.totalAttends / stats.totalSignups * 100)
})

onMounted(async () => {
  const res = await activityStats()
  const data = res.data || []
  stats.total = data.length
  stats.publishing = data.filter(d => d.status === '报名中').length
  stats.finished = data.filter(d => d.status === '已结束').length
  stats.totalSignups = data.reduce((s, d) => s + d.signupCount, 0)
  stats.totalAttends = data.reduce((s, d) => s + d.attendCount, 0)

  // 类型分布
  const typeMap = {}
  data.forEach(d => { const t = d.activityType || '未分类'; typeMap[t] = (typeMap[t] || 0) + 1 })
  await nextTick()
  initTypeChart(Object.entries(typeMap).map(([k, v]) => ({ name: k, value: v })))

  // 报名 vs 实到（取前10）
  const top = data.filter(d => d.signupCount > 0).sort((a, b) => b.signupCount - a.signupCount).slice(0, 10)
  initAttendChart(top)
})

function initTypeChart(d) {
  if (!typeChart.value) return
  echarts.init(typeChart.value).setOption({
    tooltip: { trigger: 'item' },
    series: [{ type: 'pie', radius: ['45%', '70%'], label: { formatter: '{b}\n{d}%' }, data: d }]
  })
}

function initAttendChart(d) {
  if (!attendChart.value) return
  echarts.init(attendChart.value).setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: 100, right: 30, top: 20, bottom: 30 },
    yAxis: { type: 'category', data: d.map(i => i.title.length > 10 ? i.title.slice(0, 10) + '...' : i.title) },
    xAxis: { type: 'value' },
    legend: { data: ['报名', '实到'] },
    series: [
      { type: 'bar', name: '报名', data: d.map(i => i.signupCount), itemStyle: { color: '#409eff' }, barGap: '10%' },
      { type: 'bar', name: '实到', data: d.map(i => i.attendCount), itemStyle: { color: '#67c23a' } }
    ]
  })
}
</script>

<style scoped>
.mb20 { margin-bottom: 20px }
.card-title { font-weight: 600; font-size: 15px }
</style>
